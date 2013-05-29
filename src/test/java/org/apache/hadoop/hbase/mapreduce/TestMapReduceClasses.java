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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.LargeTests;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.Export.Exporter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

@Category(LargeTests.class)
public class TestMapReduceClasses {

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public void testIdentityTableMapper() throws Exception {
    IdentityTableMapper mapper = new IdentityTableMapper();
    Scan scan = new Scan();
    Job job = new Job(new Configuration());
    IdentityTableMapper.initJob("table", scan, Exporter.class, job);
    assertEquals(TableInputFormat.class, job.getInputFormatClass());
    assertEquals(Result.class, job.getMapOutputValueClass());
    assertEquals(ImmutableBytesWritable.class, job.getMapOutputKeyClass());
    assertEquals(Exporter.class, job.getMapperClass());

    Context context = mock(Context.class);
    ImmutableBytesWritable key = new ImmutableBytesWritable(Bytes.toBytes("key"));
    Result value = new Result(key);
    mapper.map(key, value, context);
    verify(context).write(key, value);
  }

}
