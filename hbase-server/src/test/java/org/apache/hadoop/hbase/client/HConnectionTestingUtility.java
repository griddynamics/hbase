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
package org.apache.hadoop.hbase.client;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.exceptions.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.client.HConnectionManager.HConnectionImplementation;
import org.mockito.Mockito;

/**
 * {@link HConnection} testing utility.
 */
public class HConnectionTestingUtility {
  /*
   * Not part of {@link HBaseTestingUtility} because this class is not
   * in same package as {@link HConnection}.  Would have to reveal ugly
   * {@link HConnectionManager} innards to HBaseTestingUtility to give it access.
   */
  /**
   * Get a Mocked {@link HConnection} that goes with the passed <code>conf</code>
   * configuration instance.  Minimally the mock will return
   * <code>conf</conf> when {@link HConnection#getConfiguration()} is invoked.
   * Be sure to shutdown the connection when done by calling
   * {@link HConnectionManager#deleteConnection(Configuration)} else it
   * will stick around; this is probably not what you want.
   * @param conf configuration
   * @return HConnection object for <code>conf</code>
   * @throws ZooKeeperConnectionException
   */
  public static HConnection getMockedConnection(final Configuration conf)
  throws ZooKeeperConnectionException {
    HConnectionKey connectionKey = new HConnectionKey(conf);
    synchronized (HConnectionManager.CONNECTION_INSTANCES) {
      HConnectionImplementation connection =
        HConnectionManager.CONNECTION_INSTANCES.get(connectionKey);
      if (connection == null) {
        connection = Mockito.mock(HConnectionImplementation.class);
        Mockito.when(connection.getConfiguration()).thenReturn(conf);
        HConnectionManager.CONNECTION_INSTANCES.put(connectionKey, connection);
      }
      return connection;
    }
  }

  /**
   * Calls {@link #getMockedConnection(Configuration)} and then mocks a few
   * more of the popular {@link HConnection} methods so they do 'normal'
   * operation (see return doc below for list). Be sure to shutdown the
   * connection when done by calling
   * {@link HConnectionManager#deleteConnection(Configuration)} else it
   * will stick around; this is probably not what you want.
   *
   * @param conf Configuration to use
   * @param admin An AdminProtocol; can be null but is usually
   * itself a mock.
   * @param client A ClientProtocol; can be null but is usually
   * itself a mock.
   * @param sn ServerName to include in the region location returned by this
   * <code>connection</code>
   * @param hri HRegionInfo to include in the location returned when
   * getRegionLocation is called on the mocked connection
   * @return Mock up a connection that returns a {@link Configuration} when
   * {@link HConnection#getConfiguration()} is called, a 'location' when
   * {@link HConnection#getRegionLocation(byte[], byte[], boolean)} is called,
   * and that returns the passed {@link AdminProtos.AdminService.BlockingInterface} instance when
   * {@link HConnection#getAdmin(ServerName)} is called, returns the passed
   * {@link ClientProtos.ClientService.BlockingInterface} instance when
   * {@link HConnection#getClient(ServerName)} is called (Be sure to call
   * {@link HConnectionManager#deleteConnection(Configuration)}
   * when done with this mocked Connection.
   * @throws IOException
   */
  public static HConnection getMockedConnectionAndDecorate(final Configuration conf,
      final AdminProtos.AdminService.BlockingInterface admin,
      final ClientProtos.ClientService.BlockingInterface client,
      final ServerName sn, final HRegionInfo hri)
  throws IOException {
    HConnection c = HConnectionTestingUtility.getMockedConnection(conf);
    Mockito.doNothing().when(c).close();
    // Make it so we return a particular location when asked.
    final HRegionLocation loc = new HRegionLocation(hri, sn);
    Mockito.when(c.getRegionLocation((byte[]) Mockito.any(),
        (byte[]) Mockito.any(), Mockito.anyBoolean())).
      thenReturn(loc);
    Mockito.when(c.locateRegion((byte[]) Mockito.any(), (byte[]) Mockito.any())).
      thenReturn(loc);
    if (admin != null) {
      // If a call to getAdmin, return this implementation.
      Mockito.when(c.getAdmin(Mockito.any(ServerName.class))).
        thenReturn(admin);
    }
    if (client != null) {
      // If a call to getClient, return this client.
      Mockito.when(c.getClient(Mockito.any(ServerName.class))).
        thenReturn(client);
    }
    return c;
  }

  /**
   * Get a Mockito spied-upon {@link HConnection} that goes with the passed
   * <code>conf</code> configuration instance.
   * Be sure to shutdown the connection when done by calling
   * {@link HConnectionManager#deleteConnection(Configuration)} else it
   * will stick around; this is probably not what you want.
   * @param conf configuration
   * @return HConnection object for <code>conf</code>
   * @throws ZooKeeperConnectionException
   * @see @link
   * {http://mockito.googlecode.com/svn/branches/1.6/javadoc/org/mockito/Mockito.html#spy(T)}
   */
  public static HConnection getSpiedConnection(final Configuration conf)
  throws IOException {
    HConnectionKey connectionKey = new HConnectionKey(conf);
    synchronized (HConnectionManager.CONNECTION_INSTANCES) {
      HConnectionImplementation connection =
        HConnectionManager.CONNECTION_INSTANCES.get(connectionKey);
      if (connection == null) {
        connection = Mockito.spy(new HConnectionImplementation(conf, true));
        HConnectionManager.CONNECTION_INSTANCES.put(connectionKey, connection);
      }
      return connection;
    }
  }

  /**
   * @return Count of extant connection instances
   */
  public static int getConnectionCount() {
    synchronized (HConnectionManager.CONNECTION_INSTANCES) {
      return HConnectionManager.CONNECTION_INSTANCES.size();
    }
  }
}
