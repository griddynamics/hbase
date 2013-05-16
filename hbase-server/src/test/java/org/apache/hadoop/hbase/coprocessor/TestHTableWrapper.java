package org.apache.hadoop.hbase.coprocessor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Coprocessor;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.client.Append;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.master.MasterCoprocessorHost;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.VersionInfo;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import clover.retrotranslator.edu.emory.mathcs.backport.java.util.Arrays;
import static org.junit.Assert.*; 

public class TestHTableWrapper {

  private static HBaseTestingUtility util = new HBaseTestingUtility();

  private static final byte[] TEST_TABLE = Bytes.toBytes("test");
  private static final byte[] TEST_FAMILY = Bytes.toBytes("f1");

  private static final byte[] ROW_A = Bytes.toBytes("aaa");
  private static final byte[] ROW_B = Bytes.toBytes("bbb");
  private static final byte[] ROW_C = Bytes.toBytes("ccc");
  
  private final byte[] qualifierCol1 = Bytes.toBytes("col1");

  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    util.startMiniCluster();
  }
  
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    util.shutdownMiniCluster();
  }
  
  @Before
  public void before()  throws Exception {
    HTable table = util.createTable(TEST_TABLE, TEST_FAMILY);

    Put puta = new Put( ROW_A );
    puta.add(TEST_FAMILY, qualifierCol1, Bytes.toBytes(1));
    table.put(puta);

    Put putb = new Put( ROW_B );
    putb.add(TEST_FAMILY, qualifierCol1, Bytes.toBytes(1));
    table.put(putb);

    Put putc = new Put( ROW_C );
    putc.add(TEST_FAMILY, qualifierCol1, Bytes.toBytes(1));
    table.put(putc);
  }

  @After
  public void after() throws Exception {
    util.deleteTable(TEST_TABLE);
  }
  
  @Test
  public void testHTableInterfaceMethods() throws Exception {
    Configuration conf = util.getConfiguration();
    MasterCoprocessorHost cpHost = util.getMiniHBaseCluster().getMaster().getCoprocessorHost();
    Class<?> implClazz = 
        org.apache.hadoop.hbase.coprocessor.TestRegionObserverScannerOpenHook.EmptyRegionObsever.class;
    cpHost.load(implClazz, Coprocessor.PRIORITY_HIGHEST, conf);
    CoprocessorEnvironment env = cpHost.findCoprocessorEnvironment(implClazz.getName());
    assertEquals(Coprocessor.VERSION, env.getVersion());
    assertEquals(VersionInfo.getVersion(), env.getHBaseVersion());
    final HTableInterface hTableInterface = env.getTable(TEST_TABLE);
    checkHTableInterfaceMethods(hTableInterface);
    cpHost.shutdown(env);
  }
  
  @SuppressWarnings("deprecation")
  private void checkHTableInterfaceMethods(final HTableInterface hTableInterface) throws Exception {
    Configuration confExpected = util.getConfiguration();
    Configuration confActual = hTableInterface.getConfiguration();
    assertTrue(confExpected == confActual);
    
    assertArrayEquals(TEST_TABLE, hTableInterface.getTableName());
    
    boolean initialAutoFlush = hTableInterface.isAutoFlush();  
    hTableInterface.setAutoFlush(false);
    assertFalse(hTableInterface.isAutoFlush());
    hTableInterface.setAutoFlush(true, true);
    assertTrue(hTableInterface.isAutoFlush());
    hTableInterface.setAutoFlush(initialAutoFlush);
    
    long initialWriteBufferSize = hTableInterface.getWriteBufferSize();
    hTableInterface.setWriteBufferSize(12345L);
    assertEquals(12345L, hTableInterface.getWriteBufferSize());
    hTableInterface.setWriteBufferSize(initialWriteBufferSize);
    
    boolean ex = hTableInterface.exists(
        new Get(ROW_A).addColumn(TEST_FAMILY, qualifierCol1));
    assertTrue(ex);
    
    Result rowOrBeforeResult = hTableInterface.getRowOrBefore(ROW_A, TEST_FAMILY);
    assertArrayEquals(ROW_A, rowOrBeforeResult.getRow());
    
    Boolean[] exArray = hTableInterface.exists(
      Arrays.asList(new Get[] { 
        new Get(ROW_A).addColumn(TEST_FAMILY, qualifierCol1),
        new Get(ROW_B).addColumn(TEST_FAMILY, qualifierCol1),
        new Get(ROW_C).addColumn(TEST_FAMILY, qualifierCol1),
        new Get(Bytes.toBytes("does not exist")).addColumn(TEST_FAMILY, qualifierCol1),
        }));
    assertArrayEquals(new Boolean[] { Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE }, exArray);
    
//    Put puta = new Put( ROW_A );
//    puta.add(TEST_FAMILY, Bytes.toBytes("col1"), Bytes.toBytes(1));
//    table.put(puta);
    final byte[] appendValue = Bytes.toBytes("append");
    
    Append append = new Append(qualifierCol1);
    append.add(TEST_FAMILY, qualifierCol1, appendValue);
    Result appendResult = hTableInterface.append(append);
    byte[] rowId = appendResult.getRow();
    
    Get get = new Get(rowId);
    get.addColumn(TEST_FAMILY, qualifierCol1);
    Result result = hTableInterface.get(get);
    System.out.println(result);
    byte[] actualValue = result.getValue(TEST_FAMILY, qualifierCol1);
    assertArrayEquals(appendValue, actualValue);
    
    // TODO: add more checks
    
    hTableInterface.close();
  }
  
}
