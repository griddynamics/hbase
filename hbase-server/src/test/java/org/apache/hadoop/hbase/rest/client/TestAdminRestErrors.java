/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.apache.hadoop.hbase.rest.client;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MediumTests;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * test a reaction of rest client on server error. client should try some times with timeout.
 */
@Category(MediumTests.class)
public class TestAdminRestErrors {

  private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
  private static RemoteAdmin remoteAdmin;

  private long maxTime = 1500;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Client fakeClient = mock(Client.class);
    Response response = new Response(509);
    when(fakeClient.get(anyString(), anyString())).thenReturn(response);
    when(fakeClient.delete(anyString())).thenReturn(response);
    byte[] aByte = new byte[0];
    when(fakeClient.put(anyString(), anyString(), any(aByte.getClass()))).thenReturn(response);
    when(fakeClient.post(anyString(), anyString(), any(aByte.getClass()))).thenReturn(response);
    Configuration configuration = TEST_UTIL.getConfiguration();

    configuration.setInt("hbase.rest.client.max.retries", 3);
    configuration.setInt("hbase.rest.client.sleep", 600);

    remoteAdmin = new RemoteAdmin(fakeClient, TEST_UTIL.getConfiguration(), "MyTable");
  }

  /**
   * test function RestVersion
   */
  @Test
  public void testTimeoutExceptionRestVersion()  {
    long start = System.currentTimeMillis();
    try {
      remoteAdmin.getRestVersion();
      fail("should be timeout exception!");
    } catch (IOException e) {
      assertEquals("java.io.IOException: get request to /MyTable/version/rest timed out",
          e.toString());
    }
    assertTrue((System.currentTimeMillis() - start) > maxTime);
  }
  
  /**
   * test function getClusterStatus
   */
  @Test
  public void testTimeoutExceptiongetClusterStatus()  {
    long start = System.currentTimeMillis();
    try {
      remoteAdmin.getClusterStatus();
      fail("should be timeout exception!");
    } catch (IOException e) {
      assertEquals("java.io.IOException: get request to /MyTable/status/cluster timed out",
          e.toString());
    }
    assertTrue((System.currentTimeMillis() - start) > maxTime);
  }

  /**
   * test function getClusterVersion
   */
  @Test
  public void testTimeoutExceptiongetClusterVersion() {
    long start = System.currentTimeMillis();
    try {
      remoteAdmin.getClusterVersion();
      fail("should be timeout exception!");
    } catch (IOException e) {
      assertEquals(
          "java.io.IOException: get request to /MyTable/version/cluster request timed out",
          e.toString());
    }
    assertTrue((System.currentTimeMillis() - start) > maxTime);
  }

  /**
   * test function isTableAvailable
   */
  @Test
  public void testTimeoutExceptiongetTableAvailable() {
    long start = System.currentTimeMillis();
    try {
      remoteAdmin.isTableAvailable(Bytes.toBytes("TestTable"));
      fail("should be timeout exception!");
    } catch (IOException e) {
      assertEquals("java.io.IOException: get request to /MyTable/TestTable/exists timed out",
          e.toString());
    }
    assertTrue((System.currentTimeMillis() - start) > maxTime);
  }

  /**
   * test function createTable
   */
  @Test
  public void testTimeoutExceptiongetCreateTable() {
    long start = System.currentTimeMillis();
    try {
      remoteAdmin.createTable(new HTableDescriptor(Bytes.toBytes("TestTable")));
      fail("should be timeout exception!");
    } catch (IOException e) {
      assertEquals("java.io.IOException: create request to /MyTable/TestTable/schema timed out",
          e.toString());
    }
    assertTrue((System.currentTimeMillis() - start) > maxTime);
  }

  /**
   * test function deleteTable
   */
  @Test
  public void testTimeoutExceptiongetDeleteTable() {
    long start = System.currentTimeMillis();
    try {
      remoteAdmin.deleteTable("TestTable");
      fail("should be timeout exception!");
    } catch (IOException e) {
      assertEquals("java.io.IOException: delete request to /MyTable/TestTable/schema timed out",
          e.toString());
    }
    assertTrue((System.currentTimeMillis() - start) > maxTime);
  }

  /**
   * test function getTableList
   */
  @Test
  public void testTimeoutExceptiongetTetTableList() {
    long start = System.currentTimeMillis();
    try {
      remoteAdmin.getTableList();
      fail("should be timeout exception!");
    } catch (IOException e) {
      assertEquals("java.io.IOException: get request to /MyTable/ request timed out",
          e.toString());
    }
    assertTrue((System.currentTimeMillis() - start) > maxTime);
  }

}
