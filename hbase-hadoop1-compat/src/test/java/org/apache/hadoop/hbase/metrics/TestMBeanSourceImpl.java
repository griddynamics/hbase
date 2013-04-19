package org.apache.hadoop.hbase.metrics;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.hadoop.metrics2.util.MBeans;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestMBeanSourceImpl {

    public static interface FooMXBean {
        String getFoo();
    }
    
    /**
     * Tests instantiation of {@link MBeanSourceImpl} through ServiceLoader and
     * an MXBean registration using the obtained instance.
     * An attribute got from the registered bean to check its functionality.
     * Finally, the bean gets unregistered.   
     */
    @Test
    public void testThroughServiceLoader() throws Exception {
        ServiceLoader<MBeanSource> loader = ServiceLoader.load(MBeanSource.class);
        Iterator<MBeanSource> iterator = loader.iterator();
        assertTrue(iterator.hasNext());
        MBeanSource mBeanSource = iterator.next();
        Object mbean = new FooMXBean() {
            @Override
            public String getFoo() {
                return "foo";
            }
        };
        final ObjectName objectName = mBeanSource.register("myService", "myMetric", mbean);
        try {
            assertNotNull(objectName);
            assertEquals("Hadoop:name=myMetric,service=myService", objectName.getCanonicalName());
        
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Object attribute = server.getAttribute(objectName, "Foo");
            assertEquals("foo", attribute);
        } finally {
            MBeans.unregister(objectName);
        }
    }
}
