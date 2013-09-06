/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.mapreduce.v2;

import junit.framework.TestCase;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.MiniMRClientCluster;
import org.apache.hadoop.mapred.MiniMRClientClusterBuilder;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.security.authorize.ProxyUsers;

import java.io.IOException;
import java.net.InetAddress;
import java.io.IOException;

public class TestNonExistentJob extends TestCase {

  private MiniDFSCluster dfsCluster = null;
  private MiniMRClientCluster mrCluster = null;

  protected void setUp() throws Exception {
    super.setUp();
    if (System.getProperty("hadoop.log.dir") == null) {
      System.setProperty("hadoop.log.dir", "/tmp");
    }
    int taskTrackers = 2;
    int dataNodes = 2;
    StringBuilder sb = new StringBuilder();
    sb.append("127.0.0.1,localhost");
    for (InetAddress i : InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())) {
      sb.append(",").append(i.getCanonicalHostName());
    }

    JobConf conf = new JobConf();
    conf.set("dfs.block.access.token.enable", "false");
    conf.set("dfs.permissions", "true");
    conf.set("hadoop.security.authentication", "simple");

    dfsCluster = new MiniDFSCluster.Builder(conf).numDataNodes(dataNodes).build();
    FileSystem fileSystem = dfsCluster.getFileSystem();
    fileSystem.mkdirs(new Path("/tmp"));
    fileSystem.mkdirs(new Path("/user"));
    fileSystem.mkdirs(new Path("/hadoop/mapred/system"));
    fileSystem.setPermission(new Path("/tmp"), FsPermission.valueOf("-rwxrwxrwx"));
    fileSystem.setPermission(new Path("/user"), FsPermission.valueOf("-rwxrwxrwx"));
    fileSystem.setPermission(new Path("/hadoop/mapred/system"), FsPermission.valueOf("-rwx------"));
    String nnURI = fileSystem.getUri().toString();
    mrCluster = new MiniMRClientClusterBuilder(getClass(), conf)
        .noOfNMs(taskTrackers)
        .namenode(nnURI)
        .build();
    ProxyUsers.refreshSuperUserGroupsConfiguration(conf);
  }

  protected JobConf getJobConf() throws IOException {
    return new JobConf(mrCluster.getConfig());
  }

  @Override
  protected void tearDown() throws Exception {
    if (mrCluster != null) {
      mrCluster.stop();
    }
    if (dfsCluster != null) {
      dfsCluster.shutdown();
    }
    super.tearDown();
  }

  public void testGetInvalidJob() throws Exception {
    try {
      RunningJob runJob = new JobClient(getJobConf()).getJob(JobID.forName("job_0_0"));
      fail("Exception is expected to thrown ahead!");
    } catch (Exception e) {
      assertTrue(e instanceof IOException);
      assertTrue(e.getMessage().contains("ApplicationNotFoundException"));
    }
  }

}

