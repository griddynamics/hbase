package org.apache.hadoop.hbase.mapreduce;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.hadoop.hbase.util.LauncherSecurityManager;
import org.junit.Test;

public class TestDriver {

    @Test
    public void testDriver() throws Throwable {

        PrintStream oldPrintStream = System.out;
        SecurityManager SECURITY_MANAGER = System.getSecurityManager();
        new LauncherSecurityManager();
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        String[] args = {};
        System.setOut(new PrintStream(data));
        try {
            System.setOut(new PrintStream(data));

            try {
                Driver.main(args);
                fail("should be SecurityException");
            } catch (InvocationTargetException e) {
                assertTrue(data.toString().contains("An example program must be given as the first argument."));
                assertTrue(data.toString().contains("CellCounter: Count cells in HBase table"));
                assertTrue(data.toString().contains("completebulkload: Complete a bulk data load."));
                assertTrue(data.toString().contains("copytable: Export a table from local cluster to peer cluster"));
                assertTrue(data.toString().contains("export: Write table data to HDFS."));
                assertTrue(data.toString().contains("import: Import data written by Export."));
                assertTrue(data.toString().contains("importtsv: Import data in TSV format."));
                assertTrue(data.toString().contains("rowcounter: Count rows in HBase table"));
            }
        } finally {
            System.setOut(oldPrintStream);
            System.setSecurityManager(SECURITY_MANAGER);
        }

    }
}
