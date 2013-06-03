/**
 *
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
package org.apache.hadoop.hbase.mapred;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.SmallTests;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.ImmutableList;

@Category(SmallTests.class)
public class TestGroupingTableMap {

  @Test
  @SuppressWarnings({ "deprecation", "unchecked" })
  public void shouldNotCallCollectonSinceFindUniqueKeyValueMoreThanOnes()
      throws Exception {
    GroupingTableMap gTableMap = null;
    try {
      Result result = mock(Result.class);
      Reporter reporter = mock(Reporter.class);
      gTableMap = new GroupingTableMap();
      Configuration cfg = new Configuration();
      cfg.set(GroupingTableMap.GROUP_COLUMNS,
          "familyA:qualifierA familyB:qualifierB");
      JobConf jobConf = new JobConf(cfg);
      gTableMap.configure(jobConf);

      byte[] row = {};
      ImmutableList<KeyValue> keyValues = of(
          new KeyValue(row, "familyA".getBytes(), "qualifierA".getBytes(),
              Bytes.toBytes("1111")), new KeyValue(row, "familyA".getBytes(),
              "qualifierA".getBytes(), Bytes.toBytes("2222")),
          new KeyValue(row, "familyB".getBytes(), "qualifierB".getBytes(),
              Bytes.toBytes("3333")));
      when(result.list()).thenReturn(keyValues);
      OutputCollector<ImmutableBytesWritable, Result> outputCollectorMock = mock(OutputCollector.class);
      gTableMap.map(null, result, outputCollectorMock, reporter);
      verify(outputCollectorMock, never()).collect(
          any(ImmutableBytesWritable.class), any(Result.class));
    } finally {
      if (gTableMap != null)
        gTableMap.close();
    }
  }

  @Test
  @SuppressWarnings({ "deprecation", "unchecked" })
  public void shouldCreateNewKeyAlthoughExtraKey() throws Exception {
    GroupingTableMap gTableMap = null;
    try {
      Result result = mock(Result.class);
      Reporter reporter = mock(Reporter.class);
      gTableMap = new GroupingTableMap();
      Configuration cfg = new Configuration();
      cfg.set(GroupingTableMap.GROUP_COLUMNS,
          "familyA:qualifierA familyB:qualifierB");
      JobConf jobConf = new JobConf(cfg);
      gTableMap.configure(jobConf);

      byte[] row = {};
      ImmutableList<KeyValue> keyValues = of(
          new KeyValue(row, "familyA".getBytes(), "qualifierA".getBytes(),
              Bytes.toBytes("1111")), new KeyValue(row, "familyB".getBytes(),
              "qualifierB".getBytes(), Bytes.toBytes("2222")),
          new KeyValue(row, "familyC".getBytes(), "qualifierC".getBytes(),
              Bytes.toBytes("3333")));
      when(result.list()).thenReturn(keyValues);
      OutputCollector<ImmutableBytesWritable, Result> outputCollectorMock = mock(OutputCollector.class);
      gTableMap.map(null, result, outputCollectorMock, reporter);
      verify(outputCollectorMock, times(1)).collect(
          any(ImmutableBytesWritable.class), any(Result.class));
    } finally {
      if (gTableMap != null)
        gTableMap.close();
    }
  }

  @Test
  @SuppressWarnings({ "deprecation" })
  public void shouldCreateNewKey() throws Exception {
    GroupingTableMap gTableMap = null;
    try {
      Result result = mock(Result.class);
      Reporter reporter = mock(Reporter.class);
      final byte[] bSeparator = Bytes.toBytes(" ");
      gTableMap = new GroupingTableMap();
      Configuration cfg = new Configuration();
      cfg.set(GroupingTableMap.GROUP_COLUMNS,
          "familyA:qualifierA familyB:qualifierB");
      JobConf jobConf = new JobConf(cfg);
      gTableMap.configure(jobConf);

      final byte[] firstPartKeyValue = Bytes.toBytes("34879512738945");
      final byte[] secondPartKeyValue = Bytes.toBytes("35245142671437");
      byte[] row = {};
      ImmutableList<KeyValue> keyValues = of(
          new KeyValue(row, "familyA".getBytes(), "qualifierA".getBytes(),
              firstPartKeyValue), new KeyValue(row, "familyB".getBytes(),
              "qualifierB".getBytes(), secondPartKeyValue));
      when(result.list()).thenReturn(keyValues);

      OutputCollector<ImmutableBytesWritable, Result> outputCollector = new OutputCollector<ImmutableBytesWritable, Result>() {
        @Override
        public void collect(ImmutableBytesWritable arg, Result result)
            throws IOException {
          assertArrayEquals(com.google.common.primitives.Bytes.concat(
              firstPartKeyValue, bSeparator, secondPartKeyValue),
              arg.copyBytes());
        }
      };

      gTableMap.map(null, result, outputCollector, reporter);
      final byte[] firstPartValue = Bytes.toBytes("238947928");
      final byte[] secondPartValue = Bytes.toBytes("4678456942345");
      byte[][] data = { firstPartValue, secondPartValue };
      ImmutableBytesWritable byteWritable = gTableMap.createGroupKey(data);
      assertArrayEquals(com.google.common.primitives.Bytes.concat(
          firstPartValue, bSeparator, secondPartValue), byteWritable.get());
    } finally {
      if (gTableMap != null)
        gTableMap.close();
    }
  }

  @Test
  @SuppressWarnings({ "deprecation" })
  public void shouldReturnNullFromCreateGroupKey() throws Exception {
    GroupingTableMap gTableMap = null;
    try {
      gTableMap = new GroupingTableMap();
      assertNull(gTableMap.createGroupKey(null));
    } finally {
      if (gTableMap != null)
        gTableMap.close();
    }
  }
}
