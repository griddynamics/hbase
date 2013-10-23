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

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

/**
 * Write to table each key, record pair
 */
@Deprecated
@InterfaceAudience.Public
@InterfaceStability.Stable
public class IdentityTableReduce
extends MapReduceBase
implements TableReduce<ImmutableBytesWritable, Put> {
  @SuppressWarnings("unused")
  private static final Log LOG =
    LogFactory.getLog(IdentityTableReduce.class.getName());

  /**
   * No aggregation, output pairs of (key, record)
   * @param key
   * @param values
   * @param output
   * @param reporter
   * @throws IOException
   */
  public void reduce(ImmutableBytesWritable key, Iterator<Put> values,
      OutputCollector<ImmutableBytesWritable, Put> output,
      Reporter reporter)
      throws IOException {

    while(values.hasNext()) {
      output.collect(key, values.next());
    }
  }
}
