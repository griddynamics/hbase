/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.LargeTests;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.GenericOptionsParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.*;

@Category(LargeTests.class)
public class TestCopy {
  private static final HBaseTestingUtility UTIL = new HBaseTestingUtility();
  private static final byte[] ROW1 = Bytes.toBytes("row1");
  private static final byte[] ROW2 = Bytes.toBytes("row2");
  private static final String FAMILY_A_STRING = "a";
  private static final String FAMILY_B_STRING = "b";
  private static final byte[] FAMILY_A = Bytes.toBytes(FAMILY_A_STRING);
  private static final byte[] FAMILY_B = Bytes.toBytes(FAMILY_B_STRING);
  private static final byte[] QUALIFIER = Bytes.toBytes("q");

  private static long now = System.currentTimeMillis();

  @BeforeClass
  public static void beforeClass() throws Exception {
    UTIL.startMiniCluster();
    UTIL.startMiniMapReduceCluster();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    UTIL.shutdownMiniMapReduceCluster();
    UTIL.shutdownMiniCluster();
  }

  /**
   * Test copy of table from sourceTable to targetTable all rows from family a
   */
  @Test
  public void testCopyTable() throws Exception {
    String sourceTable = "sourceTable";
    String targetTable = "targetTable";

    byte[][] families = { FAMILY_A, FAMILY_B };

    HTable t = UTIL.createTable(Bytes.toBytes(sourceTable), families);
    HTable t2 = UTIL.createTable(Bytes.toBytes(targetTable), families);
    Put p = new Put(ROW1);
    p.add(FAMILY_A, QUALIFIER, now, Bytes.toBytes("Data11"));
    p.add(FAMILY_B, QUALIFIER, now + 1, Bytes.toBytes("Data12"));
    p.add(FAMILY_A, QUALIFIER, now + 2, Bytes.toBytes("Data13"));
    t.put(p);
    p = new Put(ROW2);
    p.add(FAMILY_B, QUALIFIER, now, Bytes.toBytes("Dat21"));
    p.add(FAMILY_A, QUALIFIER, now + 1, Bytes.toBytes("Data22"));
    p.add(FAMILY_B, QUALIFIER, now + 2, Bytes.toBytes("Data23"));
    t.put(p);

    long currentTime = System.currentTimeMillis();
    String[] args = new String[] { "--new.name=" + targetTable, "--families=a:b", "--all.cells",
        "--starttime=" + (currentTime - 100000), "--endtime=" + (currentTime + 100000),
        "--versions=1", sourceTable };
    assertNull(t2.get(new Get(ROW1)).getRow());
    assertTrue(runCopy(args));

    assertNotNull(t2.get(new Get(ROW1)).getRow());
    Result res = t2.get(new Get(ROW1));
    byte[] b1 = res.getValue(FAMILY_B, QUALIFIER);
    assertEquals("Data13", new String(b1));
    assertNotNull(t2.get(new Get(ROW2)).getRow());
    res = t2.get(new Get(ROW2));
    b1 = res.getValue(FAMILY_A, QUALIFIER);
    // Data from the family of B is not copied
    assertNull(b1);

  }

  /**
   * Test main method of CopyTable.
   */
  @Test
  public void testMainMethod() throws Exception {
    String[] emptyArgs = { "-h" };
    PrintStream oldWriter = System.err;
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    PrintStream writer = new PrintStream(data);
    System.setErr(writer);
    try {
      CopyTable.main(emptyArgs);
    } finally {
      System.setErr(oldWriter);
    }
    assertTrue(data.toString().contains("rs.class"));
    assertTrue(data
        .toString()
        .contains(
            "Usage: CopyTable [general options] [--starttime=X] [--endtime=Y] [--new.name=NEW]" +
                " [--peer.adr=ADR] <tablename>"));
    assertTrue(data.toString().contains(
        "rs.impl      hbase.regionserver.impl of the peer cluster"));
    assertTrue(data.toString().contains(
        "starttime    beginning of the time range (unixtime in millis)"));
    assertTrue(data.toString().contains(
        "endtime      end of the time range.  Ignored if no starttime specified."));
    assertTrue(data.toString().contains("versions     number of cell versions to copy"));
    assertTrue(data.toString().contains("new.name     new table's name"));
    assertTrue(data.toString().contains(
        "peer.adr     Address of the peer cluster given in the format"));
    assertTrue(data.toString().contains(
        "all.cells    also copy delete markers and deleted cells"));
    assertTrue(data.toString().contains("tablename    Name of the table to copy"));
  }

  private boolean runCopy(String[] args) throws IOException, InterruptedException,
      ClassNotFoundException {
    GenericOptionsParser opts = new GenericOptionsParser(
        new Configuration(UTIL.getConfiguration()), args);
    Configuration configuration = opts.getConfiguration();
    args = opts.getRemainingArgs();
    Job job = CopyTable.createSubmittableJob(configuration, args);
    job.waitForCompletion(false);
    return job.isSuccessful();
  }
}
