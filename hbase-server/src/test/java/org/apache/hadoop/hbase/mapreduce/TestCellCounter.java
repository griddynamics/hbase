package org.apache.hadoop.hbase.mapreduce;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.LargeTests;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.GenericOptionsParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static junit.framework.Assert.*;

@Category(LargeTests.class)
public class TestCellCounter {
    private static final HBaseTestingUtility UTIL = new HBaseTestingUtility();
    private static final byte[] ROW1 = Bytes.toBytes("row1");
    private static final byte[] ROW2 = Bytes.toBytes("row2");
    private static final String FAMILYA_STRING = "a";
    private static final String FAMILYB_STRING = "b";
    private static final byte[] FAMILYA = Bytes.toBytes(FAMILYA_STRING);
    private static final byte[] FAMILYB = Bytes.toBytes(FAMILYB_STRING);
    private static final byte[] QUAL = Bytes.toBytes("q");

    private static Path FQ_OUTPUT_DIR;
    private static final String OUTPUT_DIR = "target" + File.separator + "test-data"
            + File.separator + "output";
    private static long now = System.currentTimeMillis();

    @BeforeClass
    public static void beforeClass() throws Exception {
        UTIL.startMiniCluster();
        UTIL.startMiniMapReduceCluster();
        FQ_OUTPUT_DIR = new Path(OUTPUT_DIR).makeQualified(new LocalFileSystem());
        FileUtil.fullyDelete(new File(OUTPUT_DIR));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        UTIL.shutdownMiniMapReduceCluster();
        UTIL.shutdownMiniCluster();
    }

    /**
     * Test simple replication case with column mapping
     * 
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testSimpleCase() throws Exception {
        String sourceTable = "sourceTable";

        byte[][] families = { FAMILYA, FAMILYB };
        HTable t = UTIL.createTable(Bytes.toBytes(sourceTable), families);
        Put p = new Put(ROW1);
        p.add(FAMILYA, QUAL, now, "Data11".getBytes("UTF-8"));
        p.add(FAMILYB, QUAL, now + 1, "Data12".getBytes());
        p.add(FAMILYA, QUAL, now + 2, "Data13".getBytes());
        t.put(p);
        p = new Put(ROW2);
        p.add(FAMILYB, QUAL, now, "Dat21".getBytes());
        p.add(FAMILYA, QUAL, now + 1, "Data22".getBytes());
        p.add(FAMILYB, QUAL, now + 2, "Data23".getBytes());
        t.put(p);
        System.out.println("file out:" + FQ_OUTPUT_DIR.toString());
        String[] args = { sourceTable, FQ_OUTPUT_DIR.toString(), ";","^row1"};
        runCount(args);
        FileInputStream inputStream = new FileInputStream(OUTPUT_DIR + File.separator
                + "part-r-00000");
        String data = IOUtils.toString(inputStream);
        inputStream.close();
        System.out.println("data:"+data);
        assertTrue(data.contains("Total Families Across all Rows"+"\t"+"2"));
        assertTrue(data.contains("Total Qualifiers across all Rows"+"\t"+"2"));
        assertTrue(data.contains("Total ROWS"+"\t"+"1"));
        assertTrue(data.contains("b;q"+"\t"+"1"));
        assertTrue(data.contains("a;q"+"\t"+"1"));
        assertTrue(data.contains("row1;a;q_Versions"+"\t"+"2"));
        System.out.println("ok");

    }

    boolean runCount(String[] args) throws IOException, InterruptedException,
            ClassNotFoundException {
        // need to make a copy of the configuration because to make sure
        // different temp dirs are used.
        GenericOptionsParser opts = new GenericOptionsParser(new Configuration(
                UTIL.getConfiguration()), args);
        Configuration conf = opts.getConfiguration();
        args = opts.getRemainingArgs();
        Job job = CellCounter.createSubmittableJob(conf, args);
        job.waitForCompletion(false);
        System.out.println("job:" + job.getJobName() + " id:" + job.getJobID().toString());
        return job.isSuccessful();
    }
}
