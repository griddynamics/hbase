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
package org.apache.hadoop.hbase.mapreduce;

import static org.junit.Assert.*;

import java.io.IOException;

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

@Category(LargeTests.class)
public class TestCopy {
	private static final HBaseTestingUtility UTIL = new HBaseTestingUtility();
	  private static final byte[] ROW1 = Bytes.toBytes("row1");
	  private static final byte[] ROW2 = Bytes.toBytes("row2");
	  private static final String FAMILYA_STRING = "a";
	  private static final String FAMILYB_STRING = "b";
	  private static final byte[] FAMILYA = Bytes.toBytes(FAMILYA_STRING);
	  private static final byte[] FAMILYB = Bytes.toBytes(FAMILYB_STRING);
	  private static final byte[] QUAL = Bytes.toBytes("q");
	
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
	   * Test simple replication case with column mapping
	   * @throws Exception
	   */
	  @Test
	  public void testSimpleCase() throws Exception {
	    String sourceTable = "sourceTable";
	    String targetTable = "targetTable";
	    
	    byte[][] families= {FAMILYA,FAMILYB};
	    
	    HTable t = UTIL.createTable(Bytes.toBytes(sourceTable), families);
	    HTable t2=  UTIL.createTable(Bytes.toBytes(targetTable), families);
	    Put p = new Put(ROW1);
	    p.add(FAMILYA, QUAL, now, "Data11".getBytes("UTF-8"));
	    p.add(FAMILYB, QUAL, now+1, "Data12".getBytes());
	    p.add(FAMILYA, QUAL, now+2, "Data13".getBytes());
	    t.put(p);
	    p = new Put(ROW2);
	    p.add(FAMILYB, QUAL, now, "Dat21".getBytes());
	    p.add(FAMILYA, QUAL, now+1, "Data22".getBytes());
	    p.add(FAMILYB, QUAL, now+2, "Data23".getBytes());
	    t.put(p);

	    String[] args = new String[] {"--new.name="+targetTable,
	    		"--families=a",sourceTable
	    };
	    assertNull(t2.get(new Get(ROW1)).getRow());
	    assertTrue(runCopy(args));

	    assertNotNull(t2.get(new Get(ROW1)).getRow());
	    Result res= t2.get(new Get(ROW1));
	    byte[] b1=res.getValue(FAMILYA, QUAL);
	    assertEquals("Data13", new String(b1));
	    assertNotNull(t2.get(new Get(ROW2)).getRow());
	    res= t2.get(new Get(ROW2));
	    b1=res.getValue(FAMILYB, QUAL);
	    // Data from the family of B is not copied
	    assertNull(b1);
	    System.out.println("ok");

	  }
	  
	  boolean runCopy(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		    // need to make a copy of the configuration because to make sure different temp dirs are used.
		    GenericOptionsParser opts = new GenericOptionsParser(new Configuration(UTIL.getConfiguration()), args);
		    Configuration conf = opts.getConfiguration();
		    args = opts.getRemainingArgs();
		    Job job = CopyTable.createSubmittableJob(conf, args);
		    job.waitForCompletion(false);
		    return job.isSuccessful();
		  }
}
