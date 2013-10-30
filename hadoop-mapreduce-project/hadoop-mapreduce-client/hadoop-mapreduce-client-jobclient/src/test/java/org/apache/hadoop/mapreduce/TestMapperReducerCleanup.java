/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.mapreduce;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;
import org.apache.hadoop.test.PathUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMapperReducerCleanup {

  static boolean mapCleanup = false;
  static boolean reduceCleanup = false;
  static boolean recordReaderCleanup = false;
  static boolean recordWriterCleanup = false;
  
  static void reset() {
    mapCleanup = false;
    reduceCleanup = false; 
    recordReaderCleanup = false;
    recordWriterCleanup = false;
  }
  
  private static class FailingMapper
      extends Mapper<LongWritable, Text, LongWritable, Text> {

    /** Map method with different behavior based on the thread id */
    public void map(LongWritable key, Text val, Context c)
        throws IOException, InterruptedException {
      throw new IOException("TestMapperReducerCleanup");
    }

    protected void cleanup(Context context) 
        throws IOException, InterruptedException {
      mapCleanup = true;
      super.cleanup(context);
    }
  }

  private static class TrackingTokenizerMapper 
  extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        context.write(word, one);
      }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void cleanup(org.apache.hadoop.mapreduce.Mapper.Context context)
        throws IOException, InterruptedException {
      mapCleanup = true;
      super.cleanup(context);
    }
    
  }

  private static class FailingReducer
      extends Reducer<LongWritable, Text, LongWritable, LongWritable> {

    public void reduce(LongWritable key, Iterable<Text> vals, Context context)
        throws IOException, InterruptedException {
      throw new IOException("TestMapperReducerCleanup");
    }

    protected void cleanup(Context context) 
        throws IOException, InterruptedException {
      reduceCleanup = true;
      super.cleanup(context);
    }
  }

  @SuppressWarnings("rawtypes")
  private static class TrackingIntSumReducer extends IntSumReducer {

    @SuppressWarnings("unchecked")
    protected void cleanup(Context context) 
        throws IOException, InterruptedException {
      reduceCleanup = true;
      super.cleanup(context);
    }
}

  public static class TrackingTextInputFormat extends TextInputFormat {

    public static class TrackingRecordReader extends LineRecordReader {
      @Override
      public synchronized void close() throws IOException {
        recordReaderCleanup = true;
        super.close();
      }
    }

    @Override
    public RecordReader<LongWritable, Text> createRecordReader(
        InputSplit split, TaskAttemptContext context) {
      return new TrackingRecordReader();
    }
  }

  @SuppressWarnings("rawtypes")
  public static class TrackingTextOutputFormat extends TextOutputFormat {
    
    public static class TrackingRecordWriter extends LineRecordWriter {

      public TrackingRecordWriter(DataOutputStream out) {
        super(out);
      }

      @Override
      public synchronized void close(TaskAttemptContext context)
          throws IOException {
        recordWriterCleanup = true;
        super.close(context);
      }

    }
    
    @Override
    public RecordWriter getRecordWriter(TaskAttemptContext job)
        throws IOException, InterruptedException {
      Configuration conf = job.getConfiguration();

      Path file = getDefaultWorkFile(job, "");
      FileSystem fs = file.getFileSystem(conf);
      FSDataOutputStream fileOut = fs.create(file, false);
      
      return new TrackingRecordWriter(fileOut);
    }
  
  }

  @BeforeClass
  public static void before() throws Exception {
    FileSystem localFs = FileSystem.getLocal(new Configuration());
    localFs.mkdirs(INPUT_PATH);
    localFs.mkdirs(OUTPUT_PATH);
  }

  /**
   * Create a single input file in the input directory.
   * @param dirPath the directory in which the file resides
   * @param id the file id number
   * @param numRecords how many records to write to each file.
   */
  private void createInputFile(Path dirPath, int id, int numRecords)
      throws IOException {
    final String MESSAGE = "This is a line in a file: ";

    Path filePath = new Path(dirPath, "" + id);
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.getLocal(conf);

    OutputStream os = fs.create(filePath);
    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os));

    for (int i = 0; i < numRecords; i++) {
      w.write(MESSAGE + id + " " + i + "\n");
    }

    w.close();
  }

  private final static Path INPUT_PATH = new Path(
      PathUtils.getTestPath(TestMapperReducerCleanup.class), "input");
  private final static Path OUTPUT_PATH = new Path(
      PathUtils.getTestPath(TestMapperReducerCleanup.class), "output");

  private Path createInput() throws IOException {
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.getLocal(conf);

    // Clear the input directory if it exists, first.
    if (fs.exists(INPUT_PATH)) {
      fs.delete(INPUT_PATH, true);
    }

    // Create an input file
    createInputFile(INPUT_PATH, 0, 10);

    return INPUT_PATH;
  }

  @Test
  public void testMapCleanup() throws Exception {
    reset();
    
    Job job = Job.getInstance();

    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.getLocal(conf);

    if (fs.exists(OUTPUT_PATH)) {
      fs.delete(OUTPUT_PATH, true);
    }

    job.setMapperClass(FailingMapper.class);
    job.setInputFormatClass(TrackingTextInputFormat.class);
    job.setOutputFormatClass(TrackingTextOutputFormat.class);
    job.setNumReduceTasks(0);
    FileInputFormat.addInputPath(job, INPUT_PATH);
    FileOutputFormat.setOutputPath(job, OUTPUT_PATH);

    job.waitForCompletion(true);

    Assert.assertTrue(mapCleanup);
    Assert.assertTrue(recordReaderCleanup);
    Assert.assertTrue(recordWriterCleanup);
  }

  @Test
  public void testReduceCleanup() throws Exception {
    reset();
    
    Job job = Job.getInstance();

    Path inputPath = createInput();

    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.getLocal(conf);

    if (fs.exists(OUTPUT_PATH)) {
      fs.delete(OUTPUT_PATH, true);
    }

    job.setMapperClass(TrackingTokenizerMapper.class);
    job.setReducerClass(FailingReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    job.setInputFormatClass(TrackingTextInputFormat.class);
    job.setOutputFormatClass(TrackingTextOutputFormat.class);
    job.setNumReduceTasks(1);
    FileInputFormat.addInputPath(job, inputPath);
    FileOutputFormat.setOutputPath(job, OUTPUT_PATH);

    job.waitForCompletion(true);

    Assert.assertTrue(mapCleanup);
    Assert.assertTrue(reduceCleanup);
    Assert.assertTrue(recordReaderCleanup);
    Assert.assertTrue(recordWriterCleanup);
  }
  
  @Test
  public void testJobSuccessCleanup() throws Exception {
    reset();
    
    Job job = Job.getInstance();

    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.getLocal(conf);

    if (fs.exists(OUTPUT_PATH)) {
      fs.delete(OUTPUT_PATH, true);
    }

    job.setMapperClass(TrackingTokenizerMapper.class);
    job.setReducerClass(TrackingIntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    job.setInputFormatClass(TrackingTextInputFormat.class);
    job.setOutputFormatClass(TrackingTextOutputFormat.class);
    job.setNumReduceTasks(1);
    FileInputFormat.addInputPath(job, INPUT_PATH);
    FileOutputFormat.setOutputPath(job, OUTPUT_PATH);

    job.waitForCompletion(true);

    Assert.assertTrue(mapCleanup);
    Assert.assertTrue(reduceCleanup);
    Assert.assertTrue(recordReaderCleanup);
    Assert.assertTrue(recordWriterCleanup);
  }

}
