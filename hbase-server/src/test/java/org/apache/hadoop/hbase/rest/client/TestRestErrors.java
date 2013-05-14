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
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.MediumTests;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.rest.HBaseRESTTestingUtility;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;



import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@Category(MediumTests.class)
public class TestRestErrors {

  private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
  private static final HBaseRESTTestingUtility REST_TEST_UTIL =
      new HBaseRESTTestingUtility();
  private static RemoteHTable remoteTable;
  
  private static final byte[] ROW_1 = Bytes.toBytes("testrow1");
  private static final byte[] COLUMN_1 = Bytes.toBytes("a");
  private static final byte[] QUALIFIER_1 = Bytes.toBytes("1");
  private static final byte[] VALUE_1 = Bytes.toBytes("testvalue1");


  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    TEST_UTIL.startMiniCluster();
    REST_TEST_UTIL.startServletContainer(TEST_UTIL.getConfiguration());
    Client fakeClient = mock(Client.class);
    Response response = new Response(509);
    when(fakeClient.get(anyString(),anyString())).thenReturn(response);
    when(fakeClient.delete(anyString())).thenReturn(response);
    byte[] aByte= new byte[0];
    when(fakeClient.put(anyString(),anyString(),any(aByte.getClass()))).thenReturn(response);
    when(fakeClient.post(anyString(),anyString(),any(aByte.getClass()))).thenReturn(response);
    Configuration configuration = TEST_UTIL.getConfiguration();

    configuration.setInt("hbase.rest.client.max.retries", 3);
    configuration.setInt("hbase.rest.client.sleep", 1000);
    
    
    remoteTable = new RemoteHTable(fakeClient, TEST_UTIL.getConfiguration(), "MyTable");
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    remoteTable.close();
    REST_TEST_UTIL.shutdownServletContainer();
    TEST_UTIL.shutdownMiniCluster();
  }
  @Test
  public void testTimeoutExceptionDelete() throws IOException{
    Delete delete= new Delete(Bytes.toBytes("delete"));
    long start= System.currentTimeMillis();
    try{
      remoteTable.delete(delete);
      fail("should be timeout exception!");
    }catch(IOException e){
      assertEquals("java.io.IOException: delete request timed out", e.toString());
    }
    assertTrue((System.currentTimeMillis()-start)>3000);
  }
  @Test
  public void testTimeoutExceptionGet() throws IOException{
    long start= System.currentTimeMillis();
    try{
      remoteTable.get(new Get(Bytes.toBytes("Get")));
      fail("should be timeout exception!");
    }catch(IOException e){
      assertEquals("java.io.IOException: get request timed out", e.toString());
    }
    assertTrue((System.currentTimeMillis()-start)>3000);
  }

  @Test
  public void testTimeoutExceptionPut() throws IOException{
    long start= System.currentTimeMillis();
    try{
      remoteTable.put(new Put(Bytes.toBytes("Row")));
      fail("should be timeout exception!");
    }catch(IOException e){
      assertEquals("java.io.IOException: put request timed out", e.toString());
    }
    assertTrue((System.currentTimeMillis()-start)>3000);
    start= System.currentTimeMillis();
    Put[] puts= {new Put(Bytes.toBytes("Row1")),new Put(Bytes.toBytes("Row2"))};
    try{
      remoteTable.put(Arrays.asList(puts));
      fail("should be timeout exception!");
    }catch(IOException e){
      assertEquals("java.io.IOException: multiput request timed out", e.toString());
    }
    assertTrue((System.currentTimeMillis()-start)>3000);
  }
  @Test
  public void testTimeoutExceptionScanner() throws IOException{
    long start= System.currentTimeMillis();
    try{
      remoteTable.getScanner(new Scan());
    }catch(IOException e){
      assertEquals("java.io.IOException: scan request timed out", e.toString());
    }
    assertTrue((System.currentTimeMillis()-start)>3000);
    start= System.currentTimeMillis();
  }
  
  @Test
  public void testTimeoutExceptionCheckAndPut() throws IOException{
    long start= System.currentTimeMillis();
    Put put = new Put(ROW_1);
    put.add(COLUMN_1, QUALIFIER_1, VALUE_1);

    try{
      remoteTable.checkAndPut(ROW_1, COLUMN_1, QUALIFIER_1, VALUE_1, put );
      fail("should be timeout exception!");
    }catch(IOException e){
      assertEquals("java.io.IOException: checkAndPut request timed out", e.toString());
    }
    assertTrue((System.currentTimeMillis()-start)>3000);
  }
  @Test
  public void testTimeoutExceptionCheckAndDelete() throws IOException{
    long start= System.currentTimeMillis();
    Put put = new Put(ROW_1);
    put.add(COLUMN_1, QUALIFIER_1, VALUE_1);
    Delete delete= new Delete(ROW_1);

    try{
      remoteTable.checkAndDelete(ROW_1, COLUMN_1, QUALIFIER_1,  VALUE_1, delete );
      fail("should be timeout exception!");
    }catch(IOException e){
      assertEquals("java.io.IOException: checkAndDelete request timed out", e.toString());
    }
    assertTrue((System.currentTimeMillis()-start)>3000);
  }
}
