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

package org.apache.hadoop.hbase.replication;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterId;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.MediumTests;
import org.apache.hadoop.hbase.Server;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.catalog.CatalogTracker;
import org.apache.hadoop.hbase.zookeeper.ZKClusterId;
import org.apache.hadoop.hbase.zookeeper.ZKUtil;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

@Category(MediumTests.class)
public class TestReplicationStateZKImpl extends TestReplicationStateBasic {

  private static final Log LOG = LogFactory.getLog(TestReplicationStateZKImpl.class);

  private static Configuration conf;
  private static HBaseTestingUtility utility;
  private static ZooKeeperWatcher zkw;
  private static String replicationZNode;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    utility = new HBaseTestingUtility();
    utility.startMiniZKCluster();
    conf = utility.getConfiguration();
    zkw = HBaseTestingUtility.getZooKeeperWatcher(utility);
    String replicationZNodeName = conf.get("zookeeper.znode.replication", "replication");
    replicationZNode = ZKUtil.joinZNode(zkw.baseZNode, replicationZNodeName);
    KEY_ONE = initPeerClusterState("/hbase1");
    KEY_TWO = initPeerClusterState("/hbase2");
  }

  private static String initPeerClusterState(String baseZKNode)
      throws IOException, KeeperException {
    // Set up state nodes of peer clusters
    Configuration testConf = new Configuration(conf);
    testConf.set(HConstants.ZOOKEEPER_ZNODE_PARENT, baseZKNode);
    ZooKeeperWatcher zkw1 = new ZooKeeperWatcher(testConf, "test1", null);
    ReplicationStateInterface rsi = new ReplicationStateImpl(zkw1, testConf, zkw1);
    rsi.init();
    rsi.setState(true);
    rsi.close();
    String fakeRs = ZKUtil.joinZNode(zkw1.rsZNode, "hostname1.example.org:1234");
    ZKUtil.createWithParents(zkw1, fakeRs);
    ZKClusterId.setClusterId(zkw1, new ClusterId());
    return ZKUtil.getZooKeeperClusterKey(testConf);
  }

  @Before
  @Override
  public void setUp() {
    super.setUp();
    DummyServer ds1 = new DummyServer(server1);
    DummyServer ds2 = new DummyServer(server2);
    DummyServer ds3 = new DummyServer(server3);
    try {
      rq1 = new ReplicationQueuesZKImpl(zkw, conf, ds1);
      rq2 = new ReplicationQueuesZKImpl(zkw, conf, ds2);
      rq3 = new ReplicationQueuesZKImpl(zkw, conf, ds3);
      rqc = new ReplicationQueuesClientZKImpl(zkw, conf, ds1);
      rp = new ReplicationPeersZKImpl(zkw, conf, zkw);
      OUR_KEY = ZKUtil.getZooKeeperClusterKey(conf);
    } catch (KeeperException e) {
      fail("Exception thrown: " + e);
    }
  }

  @After
  public void tearDown() throws KeeperException, IOException {
    ZKUtil.deleteNodeRecursively(zkw, replicationZNode);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    utility.shutdownMiniZKCluster();
  }

  static class DummyServer implements Server {
    private String serverName;
    private boolean isAborted = false;
    private boolean isStopped = false;

    public DummyServer(String serverName) {
      this.serverName = serverName;
    }

    @Override
    public Configuration getConfiguration() {
      return conf;
    }

    @Override
    public ZooKeeperWatcher getZooKeeper() {
      return zkw;
    }

    @Override
    public CatalogTracker getCatalogTracker() {
      return null;
    }

    @Override
    public ServerName getServerName() {
      return new ServerName(this.serverName);
    }

    @Override
    public void abort(String why, Throwable e) {
      LOG.info("Aborting " + serverName);
      this.isAborted = true;
    }

    @Override
    public boolean isAborted() {
      return this.isAborted;
    }

    @Override
    public void stop(String why) {
      this.isStopped = true;
    }

    @Override
    public boolean isStopped() {
      return this.isStopped;
    }
  }
}

