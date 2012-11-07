package org.apache.hadoop.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.permission.FsPermission;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * NB: to run this test from an IDE make sure the 
 * folder "hadoop-common-project/hadoop-common/src/main/resources/" is added as a source path.
 * This will allow the system to pick up the "core-default.xml" and "META-INF/services/..." resources 
 * from the class-path in the runtime.
 */
public class TestHarFileSystem2 {

  private static final String ROOT_PATH = System.getProperty("test.build.data", "build/test/data");
  private static final Path rootPath = new Path(new File(ROOT_PATH).getAbsolutePath() + "/localfs");
  private static final Path harPath = new Path(rootPath, "path1/path2/my.har"); // NB: .har suffix is necessary

  private FileSystem localFileSystem;
  private HarFileSystem harFileSystem;
  private Configuration conf;
  
  /*
   * creates and returns fully initialized HarFileSystem  
   */
  private HarFileSystem createHarFileSysten(final Configuration conf) throws Exception {
    localFileSystem = FileSystem.getLocal(conf);
    localFileSystem.initialize(new URI("file:///"), conf);
    localFileSystem.mkdirs(rootPath);
    localFileSystem.mkdirs(harPath);
    final Path indexPath = new Path(harPath, "_index");
    final Path masterIndexPath = new Path(harPath, "_masterindex");
    localFileSystem.createNewFile(indexPath);
      assertTrue(localFileSystem.exists(indexPath));
    localFileSystem.createNewFile(masterIndexPath);
      assertTrue(localFileSystem.exists(masterIndexPath));
    
    writeVersionToMasterIndexImpl(HarFileSystem.VERSION);
      
    final HarFileSystem harFileSystem = new HarFileSystem(localFileSystem);
    final URI uri = new URI("har://" + harPath.toString()); 
    harFileSystem.initialize(uri, conf);
    return harFileSystem;
  }
  
  private void writeVersionToMasterIndexImpl(int version) throws IOException {
    final Path masterIndexPath = new Path(harPath, "_masterindex");
    // write Har version into the master index:
    final FSDataOutputStream fsdos = localFileSystem.create(masterIndexPath);
    try {
      String versionString = version + "\n";
      fsdos.write(versionString.getBytes("UTF-8"));
      fsdos.flush();
    } finally {
      fsdos.close();
    }
  }
  
  @Before
  public void before() throws Exception {
    final File rootDirIoFile = new File(rootPath.toUri().getPath());
    rootDirIoFile.mkdirs();
    if (!rootDirIoFile.exists()) {
      throw new IOException("Failed to create temp directory ["+rootDirIoFile.getAbsolutePath()+"]");
    }
    // create Har to test:
    conf = new Configuration();
    harFileSystem = createHarFileSysten(conf);
  }
  
  @After
  public void after() throws Exception {
    // close Har FS:
    final FileSystem harFS = harFileSystem;
    if (harFS != null) {
      harFS.close();
      harFileSystem = null;
    }
    // cleanup: delete all the temporary files: 
    final File rootDirIoFile = new File(rootPath.toUri().getPath());
    if (rootDirIoFile.exists()) {
      FileUtil.fullyDelete(rootDirIoFile);
    }
    if (rootDirIoFile.exists()) {
      throw new IOException("Failed to delete temp directory ["+rootDirIoFile.getAbsolutePath()+"]");
    }
  }
  
  // ======== Positive tests:
  
  @Test
  public void testPositiveHarFileSystemBasics() throws Exception {
    // check Har version:
    assertEquals(HarFileSystem.VERSION, harFileSystem.getHarVersion());

    // check Har URI:
    final URI harUri = harFileSystem.getUri();
    assertEquals(harPath.toUri().getPath(), harUri.getPath());
    assertEquals("har", harUri.getScheme());
    
    // check Har home path:
    final Path homePath = harFileSystem.getHomeDirectory();
    assertEquals(harPath.toUri().getPath(), homePath.toUri().getPath());

    // check working directory:
    final Path workDirPath0 = harFileSystem.getWorkingDirectory();
    assertEquals(homePath, workDirPath0);
    
    // check that its impossible to reset the working directory (#setWorkingDirectory should have no effect): 
    harFileSystem.setWorkingDirectory(new Path("/foo/bar"));
    assertEquals(workDirPath0, harFileSystem.getWorkingDirectory());
  }
  
  @Test
  public void testPositiveNewHarFsOnTheSameUnderlyingFs() throws Exception {
    // Init 2nd har file system on the same underlying FS, so the
    // metadata gets reused:
    final HarFileSystem hfs = new HarFileSystem(localFileSystem);
    final URI uri = new URI("har://" + harPath.toString()); 
    hfs.initialize(uri, new Configuration());
    // the metadata should be reused from cache:
    assertTrue(hfs.getMetadata() == harFileSystem.getMetadata());
  }  
  
  @Test
  public void testPositiveInitWithoutUnderlyingFS() throws Exception {
    // Init HarFS with no constructor arg, so that the underlying FS object
    // is created on demand or got from cache in #initialize() method.
    final HarFileSystem hfs = new HarFileSystem();
    final URI uri = new URI("har://" + harPath.toString()); 
    hfs.initialize(uri, new Configuration());
  }  

  // ========== Negative:

  @Test
  public void testNegativeInitWithoutIndex() throws Exception {
    // delete the index file:
    final Path indexPath = new Path(harPath, "_index");
    localFileSystem.delete(indexPath, false);
    // now init the HarFs:
    final HarFileSystem hfs = new HarFileSystem(localFileSystem);
    final URI uri = new URI("har://" + harPath.toString());
    try {
      hfs.initialize(uri, new Configuration());
      Assert.fail("Exception expected.");
    } catch (IOException ioe) {
      // ok
    }
  }  
  
  @Test
  public void testNegativeGetHarVersionOnNotInitializedFS() throws Exception {
    final HarFileSystem hfs = new HarFileSystem(localFileSystem);
    try {
      int version = hfs.getHarVersion();
      Assert.fail("Exception expected, but got a Har version " + version + ".");
    } catch (IOException ioe) {
      System.out.println("ok: "+ioe);
    }
  }  
  
  @Test
  public void testNegativeInitWithAnUnsupportedVersion() throws Exception {
    // NB: should wait at least 1 second to ensure the timestamp of the master index will change
    // upon the writing, because Linux seems to update the file modification time with 1 second accuracy: 
    Thread.sleep(1000);
    // write an unsupported version:
    writeVersionToMasterIndexImpl(7777);
    // init the Har:
    final HarFileSystem hfs = new HarFileSystem(localFileSystem);
    
    // the metadata should *not* be reused from cache:
    assertFalse(hfs.getMetadata() == harFileSystem.getMetadata());
    
    final URI uri = new URI("har://" + harPath.toString());
    try {
      hfs.initialize(uri, new Configuration());
      Assert.fail("IOException expected.");
    } catch (IOException ioe) {
      System.out.println("ok: "+ioe);
    }
  }
  
  @Test
  public void testNegativeHarFsModifications() throws Exception {
    // all the modification methods of HarFS must lead to IOE.
    final Path fooPath = new Path(rootPath, "foo/bar");
    localFileSystem.createNewFile(fooPath);
    try {
      harFileSystem.create(fooPath, new FsPermission("+rwx"), true, 1024, (short)88, 1024, null);
      Assert.fail("IOException expected.");
    } catch (IOException ioe) {
      System.out.println("ok: "+ioe);
    }
    
    try {
      harFileSystem.setReplication(fooPath, (short)55);
      Assert.fail("IOException expected.");
    } catch (IOException ioe) {
      System.out.println("ok: "+ioe);
    }
    
    try {
      harFileSystem.delete(fooPath, true);
      Assert.fail("IOException expected.");
    } catch (IOException ioe) {
      System.out.println("ok: "+ioe);
    }
    
    try {
      harFileSystem.mkdirs(fooPath, new FsPermission("+rwx"));
      Assert.fail("IOException expected.");
    } catch (IOException ioe) {
      System.out.println("ok: "+ioe);
    }
    
    final Path indexPath = new Path(harPath, "_index");
    try {
      harFileSystem.copyFromLocalFile(false, indexPath, fooPath);
      Assert.fail("IOException expected.");
    } catch (IOException ioe) {
      System.out.println("ok: "+ioe);
    }
    
    try {
      harFileSystem.startLocalOutput(fooPath, indexPath);
      Assert.fail("IOException expected.");
    } catch (IOException ioe) {
      System.out.println("ok: "+ioe);
    }
    
    try {
      harFileSystem.completeLocalOutput(fooPath, indexPath);
      Assert.fail("IOException expected.");
    } catch (IOException ioe) {
      System.out.println("ok: "+ioe);
    }
    
    try {
      harFileSystem.setOwner(fooPath, "user", "group");
      Assert.fail("IOException expected.");
    } catch (IOException ioe) {
      System.out.println("ok: "+ioe);
    }
    
    try {
      harFileSystem.setPermission(fooPath, new FsPermission("+x"));
      Assert.fail("IOException expected.");
    } catch (IOException ioe) {
      System.out.println("ok: "+ioe);
    }
  }  
  
}