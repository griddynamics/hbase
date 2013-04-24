package org.apache.hadoop.hbase.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.IndexBuilder.Map;
import org.apache.hadoop.hbase.mapreduce.SampleUploader.Uploader;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class TestExamples {
    private static HBaseTestingUtility util = new HBaseTestingUtility();
    int counter = 1;

    @SuppressWarnings("unchecked")
    @Test
    public void testSampleUploader() throws Exception {

        Configuration configuration = new Configuration();
        Uploader uploader = new Uploader();
        Mapper<LongWritable, Text, ImmutableBytesWritable, Put>.Context ctx = mock(Context.class);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ImmutableBytesWritable writer = (ImmutableBytesWritable) invocation.getArguments()[0];
                Put put = (Put) invocation.getArguments()[1];
                assertEquals("row", new String(writer.get()));
                assertEquals("row", new String(put.getRow()));
                return null;
            }
        }).when(ctx).write(any(ImmutableBytesWritable.class), any(Put.class));

        uploader.map(null, new Text("row,family,qualifier,value"), ctx);

        Path dir = util.getDataTestDirOnTestFS("testSampleUploader");

        String[] args = { dir.toString(), "simpleTable" };
        Job job = SampleUploader.configureJob(configuration, args);
        assertEquals(SequenceFileInputFormat.class, job.getInputFormatClass());

    }

    @Test
    public void testIndexBuilder() throws Exception {
        Configuration configuration = new Configuration();
        String[] args= {"tableName","columnFamily","column1","column2"};
        IndexBuilder.configureJob(configuration,args);
        assertEquals("tableName", configuration.get("index.tablename"));
        assertEquals("attributes", configuration.get("index.familyname"));
        assertEquals("tableName", configuration.get(TableInputFormat.INPUT_TABLE));
        assertEquals("column1,column2", configuration.get("index.fields"));
        
       
    }
}
