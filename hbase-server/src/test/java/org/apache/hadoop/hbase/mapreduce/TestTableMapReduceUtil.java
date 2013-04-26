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

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.hbase.SmallTests;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import static org.junit.Assert.*;

@Category(SmallTests.class)
public class TestTableMapReduceUtil {

    @Test
    public void tsest1() throws Exception {
        Configuration configuration = new Configuration();
        Job job = new Job(configuration, "tableName");
        TableMapReduceUtil.initTableMapperJob("Table", new Scan(), Import.Importer.class, Integer.class, String.class, job, false,
                HLogInputFormat.class);
        assertEquals(HLogInputFormat.class, job.getInputFormatClass());
        assertEquals(Import.Importer.class, job.getMapperClass());
        assertEquals(LongWritable.class, job.getOutputKeyClass());
        assertEquals(Text.class, job.getOutputValueClass());
        assertNull(job.getCombinerClass());
        assertEquals("Table", job.getConfiguration().get(TableInputFormat.INPUT_TABLE));
        assertEquals(
                "org.apache.hadoop.io.serializer.WritableSerialization,org.apache.hadoop.hbase.mapreduce.MutationSerialization,org.apache.hadoop.hbase.mapreduce.ResultSerialization,org.apache.hadoop.hbase.mapreduce.KeyValueSerialization",
                job.getConfiguration().get("io.serializations"));

        configuration = new Configuration();
        job = new Job(configuration, "tableName");
        TableMapReduceUtil.initTableMapperJob(Bytes.toBytes("Table"), new Scan(), Import.Importer.class, Integer.class,
                String.class, job, false, HLogInputFormat.class);
        assertEquals(HLogInputFormat.class, job.getInputFormatClass());
        assertEquals(Import.Importer.class, job.getMapperClass());
        assertEquals(LongWritable.class, job.getOutputKeyClass());
        assertEquals(Text.class, job.getOutputValueClass());
        assertNull(job.getCombinerClass());
        assertEquals("Table", job.getConfiguration().get(TableInputFormat.INPUT_TABLE));
        assertEquals(
                "org.apache.hadoop.io.serializer.WritableSerialization,org.apache.hadoop.hbase.mapreduce.MutationSerialization," +
                "org.apache.hadoop.hbase.mapreduce.ResultSerialization,org.apache.hadoop.hbase.mapreduce.KeyValueSerialization",
                job.getConfiguration().get("io.serializations"));

        configuration = new Configuration();
        job = new Job(configuration, "tableName");
        TableMapReduceUtil.initTableMapperJob(Bytes.toBytes("Table"), new Scan(), Import.Importer.class, Integer.class,
                String.class, job);
        assertEquals(HLogInputFormat.class, job.getInputFormatClass());
        assertEquals(Import.Importer.class, job.getMapperClass());
        assertEquals(LongWritable.class, job.getOutputKeyClass());
        assertEquals(Text.class, job.getOutputValueClass());
        assertNull(job.getCombinerClass());
        assertEquals("Table", job.getConfiguration().get(TableInputFormat.INPUT_TABLE));
        assertEquals(
                "org.apache.hadoop.io.serializer.WritableSerialization,org.apache.hadoop.hbase.mapreduce.MutationSerialization," +
                "org.apache.hadoop.hbase.mapreduce.ResultSerialization,org.apache.hadoop.hbase.mapreduce.KeyValueSerialization",
                job.getConfiguration().get("io.serializations"));

        configuration = new Configuration();
        job = new Job(configuration, "tableName");
        TableMapReduceUtil.initTableMapperJob(Bytes.toBytes("Table"), new Scan(), Import.Importer.class, Integer.class,
                String.class, job, false);
        assertEquals(HLogInputFormat.class, job.getInputFormatClass());
        assertEquals(Import.Importer.class, job.getMapperClass());
        assertEquals(LongWritable.class, job.getOutputKeyClass());
        assertEquals(Text.class, job.getOutputValueClass());
        assertNull(job.getCombinerClass());
        assertEquals("Table", job.getConfiguration().get(TableInputFormat.INPUT_TABLE));
        assertEquals(
            "org.apache.hadoop.io.serializer.WritableSerialization,org.apache.hadoop.hbase.mapreduce.MutationSerialization," +
            "org.apache.hadoop.hbase.mapreduce.ResultSerialization,org.apache.hadoop.hbase.mapreduce.KeyValueSerialization",
                job.getConfiguration().get("io.serializations"));

    }

    @Test
    public void testSubMapStatusReporter() throws Exception {
        
        MultithreadedTableMapper.SubMapStatusReporter
        SubMapStatusReporter reporter=
        
        new SubMapStatusReporter();
    }

}
