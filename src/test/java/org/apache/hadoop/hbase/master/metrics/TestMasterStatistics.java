package org.apache.hadoop.hbase.master.metrics;

import static org.junit.Assert.*;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.hadoop.hbase.SmallTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests {@link MasterMetrics} and access to it through the
 * {@link MasterStatistics} management bean.
 */
@Category(SmallTests.class)
public class TestMasterStatistics {

  @Test
  public void testMasterStatistics() throws Exception {
    MasterMetrics masterMetrics = new MasterMetrics("foo");
    try {
      final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      final ObjectName objectName = new ObjectName(
          "hadoop:name=MasterStatistics,service=Master");

      masterMetrics.doUpdates(null);

      masterMetrics.resetAllMinMax();

      masterMetrics.incrementRequests(10);
      Thread.sleep(1000);

      masterMetrics.addSnapshot(1L);
      masterMetrics.addSnapshotClone(2L);
      masterMetrics.addSnapshotRestore(3L);

      // 3 times added split, average = (5+3+4)/3 = 4
      masterMetrics.addSplit(4L, 5L);
      masterMetrics.addSplit(2L, 3L);
      masterMetrics.addSplit(13L, 4L);

      masterMetrics.doUpdates(null);

      float f = masterMetrics.getRequests();
      assertTrue(0.0f < f && f <= 10.0f);
      Object attribute = server.getAttribute(objectName, "cluster_requests");
      float rq = ((Float) attribute).floatValue();
      assertTrue(0.0f < rq && rq <= 10.0f);

      // NB: these 3 metrics are not pushed upon masterMetrics.doUpdates(),
      // so they always return null:
      attribute = server.getAttribute(objectName, "snapshotTimeNumOps");
      assertEquals(Integer.valueOf(0), attribute);
      attribute = server.getAttribute(objectName, "snapshotRestoreTimeNumOps");
      assertEquals(Integer.valueOf(0), attribute);
      attribute = server.getAttribute(objectName, "snapshotCloneTimeNumOps");
      assertEquals(Integer.valueOf(0), attribute);

      attribute = server.getAttribute(objectName, "splitSizeNumOps");
      assertEquals(Integer.valueOf(3), attribute);
      attribute = server.getAttribute(objectName, "splitSizeAvgTime");
      assertEquals(Long.valueOf(4), attribute);
    } finally {
      masterMetrics.shutdown();
    }
  }

  @Test
  public void testHBaseInfoBean() throws Exception {
    MasterMetrics masterMetrics = new MasterMetrics("foo");
    try {
      final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      // Test Info bean:
      final ObjectName objectName2 = new ObjectName(
          "hadoop:name=Info,service=HBase");
      Object attribute;
      attribute = server.getAttribute(objectName2, "revision");
      assertNotNull(attribute);
      attribute = server.getAttribute(objectName2, "version");
      assertNotNull(attribute);
      attribute = server.getAttribute(objectName2, "hdfsUrl");
      assertNotNull(attribute);
      attribute = server.getAttribute(objectName2, "user");
      assertNotNull(attribute);
    } finally {
      masterMetrics.shutdown();
    }
  }
}
