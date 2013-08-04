/**
 *
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
package org.apache.hadoop.hbase.client.replication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Abortable;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.replication.ReplicationFactory;
import org.apache.hadoop.hbase.replication.ReplicationPeers;
import org.apache.hadoop.hbase.replication.ReplicationQueuesClient;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;
import org.apache.zookeeper.KeeperException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * This class provides the administrative interface to HBase cluster
 * replication. In order to use it, the cluster and the client using
 * ReplicationAdmin must be configured with <code>hbase.replication</code>
 * set to true.
 * </p>
 * <p>
 * Adding a new peer results in creating new outbound connections from every
 * region server to a subset of region servers on the slave cluster. Each
 * new stream of replication will start replicating from the beginning of the
 * current HLog, meaning that edits from that past will be replicated.
 * </p>
 * <p>
 * Removing a peer is a destructive and irreversible operation that stops
 * all the replication streams for the given cluster and deletes the metadata
 * used to keep track of the replication state.
 * </p>
 * <p>
 * To see which commands are available in the shell, type
 * <code>replication</code>.
 * </p>
 */
public class ReplicationAdmin implements Closeable {
  private static final Log LOG = LogFactory.getLog(ReplicationAdmin.class);

  private final HConnection connection;
  private final ReplicationQueuesClient replicationQueuesClient;
  private final ReplicationPeers replicationPeers;

  /**
   * Constructor that creates a connection to the local ZooKeeper ensemble.
   * @param conf Configuration to use
   * @throws IOException if the connection to ZK cannot be made
   * @throws RuntimeException if replication isn't enabled.
   */
  public ReplicationAdmin(Configuration conf) throws IOException {
    if (!conf.getBoolean(HConstants.REPLICATION_ENABLE_KEY, false)) {
      throw new RuntimeException("hbase.replication isn't true, please " +
          "enable it in order to use replication");
    }
    this.connection = HConnectionManager.getConnection(conf);
    ZooKeeperWatcher zkw = createZooKeeperWatcher();
    try {
      this.replicationPeers = ReplicationFactory.getReplicationPeers(zkw, conf, this.connection);
      this.replicationPeers.init();
      this.replicationQueuesClient =
          ReplicationFactory.getReplicationQueuesClient(zkw, conf, this.connection);
      this.replicationQueuesClient.init();

    } catch (KeeperException e) {
      throw new IOException("Unable setup the ZooKeeper connection", e);
    }
  }

  private ZooKeeperWatcher createZooKeeperWatcher() throws IOException {
    return new ZooKeeperWatcher(connection.getConfiguration(),
      "Replication Admin", new Abortable() {
      @Override
      public void abort(String why, Throwable e) {
        LOG.error(why, e);
        System.exit(1);
      }

      @Override
      public boolean isAborted() {
        return false;
      }

    });
  }


  /**
   * Add a new peer cluster to replicate to.
   * @param id a short that identifies the cluster
   * @param clusterKey the concatenation of the slave cluster's
   * <code>hbase.zookeeper.quorum:hbase.zookeeper.property.clientPort:zookeeper.znode.parent</code>
   * @throws IllegalStateException if there's already one slave since
   * multi-slave isn't supported yet.
   */
  public void addPeer(String id, String clusterKey) throws IOException {
    this.replicationPeers.addPeer(id, clusterKey);
  }

  /**
   * Removes a peer cluster and stops the replication to it.
   * @param id a short that identifies the cluster
   */
  public void removePeer(String id) throws IOException {
    this.replicationPeers.removePeer(id);
  }

  /**
   * Restart the replication stream to the specified peer.
   * @param id a short that identifies the cluster
   */
  public void enablePeer(String id) throws IOException {
    this.replicationPeers.enablePeer(id);
  }

  /**
   * Stop the replication stream to the specified peer.
   * @param id a short that identifies the cluster
   */
  public void disablePeer(String id) throws IOException {
    this.replicationPeers.disablePeer(id);
  }

  /**
   * Get the number of slave clusters the local cluster has.
   * @return number of slave clusters
   */
  public int getPeersCount() {
    return this.replicationPeers.getAllPeerIds().size();
  }

  /**
   * Map of this cluster's peers for display.
   * @return A map of peer ids to peer cluster keys
   */
  public Map<String, String> listPeers() {
    return this.replicationPeers.getAllPeerClusterKeys();
  }

  @Override
  public void close() throws IOException {
    if (this.connection != null) {
      this.connection.close();
    }
  }
}