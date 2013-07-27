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
package org.apache.hadoop.hbase.snapshot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.LargeTests;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.exceptions.SnapshotCreationException;
import org.apache.hadoop.hbase.exceptions.TableNotFoundException;
import org.apache.hadoop.hbase.ipc.RpcClient;
import org.apache.hadoop.hbase.ipc.RpcServer;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.ScannerCallable;
import org.apache.hadoop.hbase.master.HMaster;
import org.apache.hadoop.hbase.master.snapshot.SnapshotManager;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription;
import org.apache.hadoop.hbase.regionserver.ConstantSizeRegionSplitPolicy;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.regionserver.HRegionFileSystem;
import org.apache.hadoop.hbase.regionserver.HRegionServer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.FSTableDescriptors;
import org.apache.hadoop.hbase.util.FSUtils;
import org.apache.hadoop.hbase.util.JVMClusterUtil.RegionServerThread;
import org.apache.hadoop.hbase.util.MD5Hash;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test creating/using/deleting snapshots from the client
 * <p>
 * This is an end-to-end test for the snapshot utility
 *
 * TODO This is essentially a clone of TestSnapshotFromClient.  This is worth refactoring this
 * because there will be a few more flavors of snapshots that need to run these tests.
 */
@Category(LargeTests.class)
public class TestFlushSnapshotFromClient {
  private static final Log LOG = LogFactory.getLog(TestFlushSnapshotFromClient.class);
  private static final HBaseTestingUtility UTIL = new HBaseTestingUtility();
  private static final int NUM_RS = 2;
  private static final String STRING_TABLE_NAME = "test";
  private static final byte[] TEST_FAM = Bytes.toBytes("fam");
  private static final byte[] TEST_QUAL = Bytes.toBytes("q");
  private static final byte[] TABLE_NAME = Bytes.toBytes(STRING_TABLE_NAME);
  private final int DEFAULT_NUM_ROWS = 100;

  /**
   * Setup the config for the cluster
   * @throws Exception on failure
   */
  @BeforeClass
  public static void setupCluster() throws Exception {
    ((Log4JLogger)RpcServer.LOG).getLogger().setLevel(Level.ALL);
    ((Log4JLogger)RpcClient.LOG).getLogger().setLevel(Level.ALL);
    ((Log4JLogger)ScannerCallable.LOG).getLogger().setLevel(Level.ALL);
    setupConf(UTIL.getConfiguration());
    UTIL.startMiniCluster(NUM_RS);
  }

  private static void setupConf(Configuration conf) {
    // disable the ui
    conf.setInt("hbase.regionsever.info.port", -1);
    // change the flush size to a small amount, regulating number of store files
    conf.setInt("hbase.hregion.memstore.flush.size", 25000);
    // so make sure we get a compaction when doing a load, but keep around some
    // files in the store
    conf.setInt("hbase.hstore.compaction.min", 10);
    conf.setInt("hbase.hstore.compactionThreshold", 10);
    // block writes if we get to 12 store files
    conf.setInt("hbase.hstore.blockingStoreFiles", 12);
    // drop the number of attempts for the hbase admin
    conf.setInt(HConstants.HBASE_CLIENT_RETRIES_NUMBER, 3);
    // Enable snapshot
    conf.setBoolean(SnapshotManager.HBASE_SNAPSHOT_ENABLED, true);
    conf.set(HConstants.HBASE_REGION_SPLIT_POLICY_KEY,
      ConstantSizeRegionSplitPolicy.class.getName());
  }

  @Before
  public void setup() throws Exception {
    createTable(TABLE_NAME, TEST_FAM);
  }

  @After
  public void tearDown() throws Exception {
    UTIL.deleteTable(TABLE_NAME);
    // and cleanup the archive directory
    try {
      UTIL.getTestFileSystem().delete(
        new Path(UTIL.getDefaultRootDirPath(), HConstants.HFILE_ARCHIVE_DIRECTORY), true);
    } catch (IOException e) {
      LOG.warn("Failure to delete archive directory", e);
    }
    for (SnapshotDescription snapshot: UTIL.getHBaseAdmin().listSnapshots()) {
      UTIL.getHBaseAdmin().deleteSnapshot(snapshot.getName());
    }
    SnapshotTestingUtils.assertNoSnapshots(UTIL.getHBaseAdmin());
  }

  @AfterClass
  public static void cleanupTest() throws Exception {
    try {
      UTIL.shutdownMiniCluster();
    } catch (Exception e) {
      LOG.warn("failure shutting down cluster", e);
    }
  }

  /**
   * Test simple flush snapshotting a table that is online
   * @throws Exception
   */
  @Test
  public void testFlushTableSnapshot() throws Exception {
    HBaseAdmin admin = UTIL.getHBaseAdmin();
    // make sure we don't fail on listing snapshots
    SnapshotTestingUtils.assertNoSnapshots(admin);

    // put some stuff in the table
    HTable table = new HTable(UTIL.getConfiguration(), TABLE_NAME);
    loadData(table, DEFAULT_NUM_ROWS, TEST_FAM);

    // get the name of all the regionservers hosting the snapshotted table
    Set<String> snapshotServers = new HashSet<String>();
    List<RegionServerThread> servers = UTIL.getMiniHBaseCluster().getLiveRegionServerThreads();
    for (RegionServerThread server : servers) {
      if (server.getRegionServer().getOnlineRegions(TABLE_NAME).size() > 0) {
        snapshotServers.add(server.getRegionServer().getServerName().toString());
      }
    }

    LOG.debug("FS state before snapshot:");
    FSUtils.logFileSystemState(UTIL.getTestFileSystem(),
      FSUtils.getRootDir(UTIL.getConfiguration()), LOG);

    // take a snapshot of the enabled table
    String snapshotString = "offlineTableSnapshot";
    byte[] snapshot = Bytes.toBytes(snapshotString);
    admin.snapshot(snapshotString, STRING_TABLE_NAME, SnapshotDescription.Type.FLUSH);
    LOG.debug("Snapshot completed.");

    // make sure we have the snapshot
    List<SnapshotDescription> snapshots = SnapshotTestingUtils.assertOneSnapshotThatMatches(admin,
      snapshot, TABLE_NAME);

    // make sure its a valid snapshot
    FileSystem fs = UTIL.getHBaseCluster().getMaster().getMasterFileSystem().getFileSystem();
    Path rootDir = UTIL.getHBaseCluster().getMaster().getMasterFileSystem().getRootDir();
    LOG.debug("FS state after snapshot:");
    FSUtils.logFileSystemState(UTIL.getTestFileSystem(),
      FSUtils.getRootDir(UTIL.getConfiguration()), LOG);

    SnapshotTestingUtils.confirmSnapshotValid(snapshots.get(0), TABLE_NAME, TEST_FAM, rootDir,
      admin, fs, false, new Path(rootDir, HConstants.HREGION_LOGDIR_NAME), snapshotServers);
  }

  @Test
  public void testSnapshotFailsOnNonExistantTable() throws Exception {
    HBaseAdmin admin = UTIL.getHBaseAdmin();
    // make sure we don't fail on listing snapshots
    SnapshotTestingUtils.assertNoSnapshots(admin);
    String tableName = "_not_a_table";

    // make sure the table doesn't exist
    boolean fail = false;
    do {
    try {
      admin.getTableDescriptor(Bytes.toBytes(tableName));
      fail = true;
      LOG.error("Table:" + tableName + " already exists, checking a new name");
      tableName = tableName+"!";
    } catch (TableNotFoundException e) {
      fail = false;
      }
    } while (fail);

    // snapshot the non-existant table
    try {
      admin.snapshot("fail", tableName, SnapshotDescription.Type.FLUSH);
      fail("Snapshot succeeded even though there is not table.");
    } catch (SnapshotCreationException e) {
      LOG.info("Correctly failed to snapshot a non-existant table:" + e.getMessage());
    }
  }

  @Test(timeout = 60000)
  public void testAsyncFlushSnapshot() throws Exception {
    HBaseAdmin admin = UTIL.getHBaseAdmin();
    SnapshotDescription snapshot = SnapshotDescription.newBuilder().setName("asyncSnapshot")
        .setTable(STRING_TABLE_NAME).setType(SnapshotDescription.Type.FLUSH).build();

    // take the snapshot async
    admin.takeSnapshotAsync(snapshot);

    // constantly loop, looking for the snapshot to complete
    HMaster master = UTIL.getMiniHBaseCluster().getMaster();
    SnapshotTestingUtils.waitForSnapshotToComplete(master, snapshot, 200);
    LOG.info(" === Async Snapshot Completed ===");
    FSUtils.logFileSystemState(UTIL.getTestFileSystem(),
      FSUtils.getRootDir(UTIL.getConfiguration()), LOG);
    // make sure we get the snapshot
    SnapshotTestingUtils.assertOneSnapshotThatMatches(admin, snapshot);
  }

  @Test
  public void testSnapshotStateAfterMerge() throws Exception {
    int numRows = DEFAULT_NUM_ROWS;
    HBaseAdmin admin = UTIL.getHBaseAdmin();
    // make sure we don't fail on listing snapshots
    SnapshotTestingUtils.assertNoSnapshots(admin);
    // load the table so we have some data
    loadData(new HTable(UTIL.getConfiguration(), TABLE_NAME), numRows, TEST_FAM);
    // and wait until everything stabilizes
    waitForTableToBeOnline(TABLE_NAME);

    // Take a snapshot
    String snapshotBeforeMergeName = "snapshotBeforeMerge";
    admin.snapshot(snapshotBeforeMergeName, STRING_TABLE_NAME, SnapshotDescription.Type.FLUSH);

    // Clone the table
    String cloneBeforeMergeName = "cloneBeforeMerge";
    admin.cloneSnapshot(snapshotBeforeMergeName, cloneBeforeMergeName);
    waitForTableToBeOnline(Bytes.toBytes(cloneBeforeMergeName));

    // Merge two regions
    List<HRegionInfo> regions = admin.getTableRegions(TABLE_NAME);
    Collections.sort(regions, new Comparator<HRegionInfo>() {
      public int compare(HRegionInfo r1, HRegionInfo r2) {
        return Bytes.compareTo(r1.getStartKey(), r2.getStartKey());
      }
    });

    int numRegions = admin.getTableRegions(TABLE_NAME).size();
    int numRegionsAfterMerge = numRegions - 2;
    admin.mergeRegions(regions.get(1).getEncodedNameAsBytes(),
        regions.get(2).getEncodedNameAsBytes(), true);
    admin.mergeRegions(regions.get(5).getEncodedNameAsBytes(),
        regions.get(6).getEncodedNameAsBytes(), true);

    // Verify that there's one region less
    waitRegionsAfterMerge(numRegionsAfterMerge);
    assertEquals(numRegionsAfterMerge, admin.getTableRegions(TABLE_NAME).size());

    // Clone the table
    String cloneAfterMergeName = "cloneAfterMerge";
    admin.cloneSnapshot(snapshotBeforeMergeName, cloneAfterMergeName);
    waitForTableToBeOnline(Bytes.toBytes(cloneAfterMergeName));

    verifyRowCount(TABLE_NAME, numRows);
    verifyRowCount(Bytes.toBytes(cloneBeforeMergeName), numRows);
    verifyRowCount(Bytes.toBytes(cloneAfterMergeName), numRows);

    // test that we can delete the snapshot
    UTIL.deleteTable(cloneAfterMergeName);
    UTIL.deleteTable(cloneBeforeMergeName);
  }

  @Test
  public void testTakeSnapshotAfterMerge() throws Exception {
    int numRows = DEFAULT_NUM_ROWS;
    HBaseAdmin admin = UTIL.getHBaseAdmin();
    // make sure we don't fail on listing snapshots
    SnapshotTestingUtils.assertNoSnapshots(admin);
    // load the table so we have some data
    loadData(new HTable(UTIL.getConfiguration(), TABLE_NAME), numRows, TEST_FAM);
    // and wait until everything stabilizes
    waitForTableToBeOnline(TABLE_NAME);

    // Merge two regions
    List<HRegionInfo> regions = admin.getTableRegions(TABLE_NAME);
    Collections.sort(regions, new Comparator<HRegionInfo>() {
      public int compare(HRegionInfo r1, HRegionInfo r2) {
        return Bytes.compareTo(r1.getStartKey(), r2.getStartKey());
      }
    });

    int numRegions = admin.getTableRegions(TABLE_NAME).size();
    int numRegionsAfterMerge = numRegions - 2;
    admin.mergeRegions(regions.get(1).getEncodedNameAsBytes(),
        regions.get(2).getEncodedNameAsBytes(), true);
    admin.mergeRegions(regions.get(5).getEncodedNameAsBytes(),
        regions.get(6).getEncodedNameAsBytes(), true);

    waitRegionsAfterMerge(numRegionsAfterMerge);
    assertEquals(numRegionsAfterMerge, admin.getTableRegions(TABLE_NAME).size());

    // Take a snapshot
    String snapshotName = "snapshotAfterMerge";
    admin.snapshot(snapshotName, STRING_TABLE_NAME, SnapshotDescription.Type.FLUSH);

    // Clone the table
    String cloneName = "cloneMerge";
    admin.cloneSnapshot(snapshotName, cloneName);
    waitForTableToBeOnline(Bytes.toBytes(cloneName));

    verifyRowCount(TABLE_NAME, numRows);
    verifyRowCount(Bytes.toBytes(cloneName), numRows);

    // test that we can delete the snapshot
    UTIL.deleteTable(cloneName);
  }

  /**
   * Basic end-to-end test of simple-flush-based snapshots
   */
  @Test
  public void testFlushCreateListDestroy() throws Exception {
    LOG.debug("------- Starting Snapshot test -------------");
    HBaseAdmin admin = UTIL.getHBaseAdmin();
    // make sure we don't fail on listing snapshots
    SnapshotTestingUtils.assertNoSnapshots(admin);
    // load the table so we have some data
    loadData(new HTable(UTIL.getConfiguration(), TABLE_NAME), DEFAULT_NUM_ROWS, TEST_FAM);
    // and wait until everything stabilizes
    waitForTableToBeOnline(TABLE_NAME);

    String snapshotName = "flushSnapshotCreateListDestroy";
    // test creating the snapshot
    admin.snapshot(snapshotName, STRING_TABLE_NAME, SnapshotDescription.Type.FLUSH);
    logFSTree(FSUtils.getRootDir(UTIL.getConfiguration()));

    // make sure we only have 1 matching snapshot
    List<SnapshotDescription> snapshots = SnapshotTestingUtils.assertOneSnapshotThatMatches(admin,
      snapshotName, STRING_TABLE_NAME);

    // check the directory structure
    FileSystem fs = UTIL.getHBaseCluster().getMaster().getMasterFileSystem().getFileSystem();
    Path rootDir = UTIL.getHBaseCluster().getMaster().getMasterFileSystem().getRootDir();
    Path snapshotDir = SnapshotDescriptionUtils.getCompletedSnapshotDir(snapshots.get(0), rootDir);
    assertTrue(fs.exists(snapshotDir));
    FSUtils.logFileSystemState(UTIL.getTestFileSystem(), snapshotDir, LOG);
    Path snapshotinfo = new Path(snapshotDir, SnapshotDescriptionUtils.SNAPSHOTINFO_FILE);
    assertTrue(fs.exists(snapshotinfo));

    // check the table info
    HTableDescriptor desc = FSTableDescriptors.getTableDescriptor(fs, rootDir, TABLE_NAME);
    HTableDescriptor snapshotDesc = FSTableDescriptors.getTableDescriptor(fs,
      SnapshotDescriptionUtils.getSnapshotsDir(rootDir), Bytes.toBytes(snapshotName));
    assertEquals(desc, snapshotDesc);

    // check the region snapshot for all the regions
    List<HRegionInfo> regions = admin.getTableRegions(TABLE_NAME);
    assertTrue(regions.size() > 1);
    for (HRegionInfo info : regions) {
      String regionName = info.getEncodedName();
      Path regionDir = new Path(snapshotDir, regionName);
      HRegionInfo snapshotRegionInfo = HRegionFileSystem.loadRegionInfoFileContent(fs, regionDir);
      assertEquals(info, snapshotRegionInfo);
      // check to make sure we have the family
      Path familyDir = new Path(regionDir, Bytes.toString(TEST_FAM));
      assertTrue("Missing region " + Bytes.toString(snapshotRegionInfo.getStartKey()),
                 fs.exists(familyDir));

      // make sure we have some file references
      assertTrue(fs.listStatus(familyDir).length > 0);
    }
  }

  /**
   * Demonstrate that we reject snapshot requests if there is a snapshot already running on the
   * same table currently running and that concurrent snapshots on different tables can both
   * succeed concurretly.
   */
  @Test(timeout=60000)
  public void testConcurrentSnapshottingAttempts() throws IOException, InterruptedException {
    final String STRING_TABLE2_NAME = STRING_TABLE_NAME + "2";
    final byte[] TABLE2_NAME = Bytes.toBytes(STRING_TABLE2_NAME);

    int ssNum = 20;
    HBaseAdmin admin = UTIL.getHBaseAdmin();
    // make sure we don't fail on listing snapshots
    SnapshotTestingUtils.assertNoSnapshots(admin);
    // create second testing table
    createTable(TABLE2_NAME, TEST_FAM);
    // load the table so we have some data
    loadData(new HTable(UTIL.getConfiguration(), TABLE_NAME), DEFAULT_NUM_ROWS, TEST_FAM);
    loadData(new HTable(UTIL.getConfiguration(), TABLE2_NAME), DEFAULT_NUM_ROWS, TEST_FAM);
    // and wait until everything stabilizes
    waitForTableToBeOnline(TABLE_NAME);
    waitForTableToBeOnline(TABLE2_NAME);

    final CountDownLatch toBeSubmitted = new CountDownLatch(ssNum);
    // We'll have one of these per thread
    class SSRunnable implements Runnable {
      SnapshotDescription ss;
      SSRunnable(SnapshotDescription ss) {
        this.ss = ss;
      }

      @Override
      public void run() {
        try {
          HBaseAdmin admin = UTIL.getHBaseAdmin();
          LOG.info("Submitting snapshot request: " + ClientSnapshotDescriptionUtils.toString(ss));
          admin.takeSnapshotAsync(ss);
        } catch (Exception e) {
          LOG.info("Exception during snapshot request: " + ClientSnapshotDescriptionUtils.toString(
              ss)
              + ".  This is ok, we expect some", e);
        }
        LOG.info("Submitted snapshot request: " + ClientSnapshotDescriptionUtils.toString(ss));
        toBeSubmitted.countDown();
      }
    };

    // build descriptions
    SnapshotDescription[] descs = new SnapshotDescription[ssNum];
    for (int i = 0; i < ssNum; i++) {
      SnapshotDescription.Builder builder = SnapshotDescription.newBuilder();
      builder.setTable((i % 2) == 0 ? STRING_TABLE_NAME : STRING_TABLE2_NAME);
      builder.setName("ss"+i);
      builder.setType(SnapshotDescription.Type.FLUSH);
      descs[i] = builder.build();
    }

    // kick each off its own thread
    for (int i=0 ; i < ssNum; i++) {
      new Thread(new SSRunnable(descs[i])).start();
    }

    // wait until all have been submitted
    toBeSubmitted.await();

    // loop until all are done.
    while (true) {
      int doneCount = 0;
      for (SnapshotDescription ss : descs) {
        try {
          if (admin.isSnapshotFinished(ss)) {
            doneCount++;
          }
        } catch (Exception e) {
          LOG.warn("Got an exception when checking for snapshot " + ss.getName(), e);
          doneCount++;
        }
      }
      if (doneCount == descs.length) {
        break;
      }
      Thread.sleep(100);
    }

    // dump for debugging
    logFSTree(FSUtils.getRootDir(UTIL.getConfiguration()));

    List<SnapshotDescription> taken = admin.listSnapshots();
    int takenSize = taken.size();
    LOG.info("Taken " + takenSize + " snapshots:  " + taken);
    assertTrue("We expect at least 1 request to be rejected because of we concurrently" +
        " issued many requests", takenSize < ssNum && takenSize > 0);

    // Verify that there's at least one snapshot per table
    int t1SnapshotsCount = 0;
    int t2SnapshotsCount = 0;
    for (SnapshotDescription ss : taken) {
      if (ss.getTable().equals(STRING_TABLE_NAME)) {
        t1SnapshotsCount++;
      } else if (ss.getTable().equals(STRING_TABLE2_NAME)) {
        t2SnapshotsCount++;
      }
    }
    assertTrue("We expect at least 1 snapshot of table1 ", t1SnapshotsCount > 0);
    assertTrue("We expect at least 1 snapshot of table2 ", t2SnapshotsCount > 0);

    UTIL.deleteTable(TABLE2_NAME);
  }

  private void logFSTree(Path root) throws IOException {
    FSUtils.logFileSystemState(UTIL.getDFSCluster().getFileSystem(), root, LOG);
  }

  private void waitForTableToBeOnline(final byte[] tableName)
  throws IOException, InterruptedException {
    HRegionServer rs = UTIL.getRSForFirstRegionInTable(tableName);
    List<HRegion> onlineRegions = rs.getOnlineRegions(tableName);
    for (HRegion region : onlineRegions) {
      region.waitForFlushesAndCompactions();
    }
    UTIL.getHBaseAdmin().isTableAvailable(tableName);
  }

  private void waitRegionsAfterMerge(final long numRegionsAfterMerge)
      throws IOException, InterruptedException {
    HBaseAdmin admin = UTIL.getHBaseAdmin();
    // Verify that there's one region less
    long startTime = System.currentTimeMillis();
    while (admin.getTableRegions(TABLE_NAME).size() != numRegionsAfterMerge) {
      // This may be flaky... if after 15sec the merge is not complete give up
      // it will fail in the assertEquals(numRegionsAfterMerge).
      if ((System.currentTimeMillis() - startTime) > 15000)
        break;
      Thread.sleep(100);
    }
    waitForTableToBeOnline(TABLE_NAME);
  }

  private void createTable(final byte[] tableName, final byte[]... families)
      throws IOException, InterruptedException {
    HTableDescriptor htd = new HTableDescriptor(tableName);
    for (byte[] family: families) {
      HColumnDescriptor hcd = new HColumnDescriptor(family);
      htd.addFamily(hcd);
    }
    byte[][] splitKeys = new byte[14][];
    byte[] hex = Bytes.toBytes("123456789abcde");
    for (int i = 0; i < splitKeys.length; ++i) {
      splitKeys[i] = new byte[] { hex[i] };
    }
    UTIL.getHBaseAdmin().createTable(htd, splitKeys);
    waitForTableToBeOnline(tableName);
    assertEquals(15, UTIL.getHBaseAdmin().getTableRegions(TABLE_NAME).size());
  }

  public void loadData(final HTable table, int rows, byte[]... families)
      throws IOException, InterruptedException {
    table.setAutoFlush(false);

    // Ensure one row per region
    assertTrue(rows >= 16);
    for (byte k0: Bytes.toBytes("0123456789abcdef")) {
      byte[] k = new byte[] { k0 };
      byte[] value = Bytes.add(Bytes.toBytes(System.currentTimeMillis()), k);
      byte[] key = Bytes.add(k, Bytes.toBytes(MD5Hash.getMD5AsHex(value)));
      putData(table, families, key, value);
      rows--;
    }

    // Add other extra rows. more rows, more files
    while (rows-- > 0) {
      byte[] value = Bytes.add(Bytes.toBytes(System.currentTimeMillis()), Bytes.toBytes(rows));
      byte[] key = Bytes.toBytes(MD5Hash.getMD5AsHex(value));
      putData(table, families, key, value);
    }
    table.flushCommits();

    waitForTableToBeOnline(table.getTableName());
  }

  private void putData(final HTable table, final byte[][] families,
      final byte[] key, final byte[] value) throws IOException {
    Put put = new Put(key);
    put.setDurability(Durability.SKIP_WAL);
    for (byte[] family: families) {
      put.add(family, TEST_QUAL, value);
    }
    table.put(put);
  }

  private void verifyRowCount(final byte[] tableName, long expectedRows) throws IOException {
    HTable table = new HTable(UTIL.getConfiguration(), tableName);
    assertEquals(expectedRows, UTIL.countRows(table));
    table.close();
  }
}
