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

package org.apache.hadoop.hbase.catalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Mocking utility for common hbase:meta functionality
 */
public class MetaMockingUtil {

  /**
   * Returns a Result object constructed from the given region information simulating
   * a catalog table result.
   * @param region the HRegionInfo object or null
   * @return A mocked up Result that fakes a Get on a row in the <code>hbase:meta</code> table.
   * @throws IOException
   */
  public static Result getMetaTableRowResult(final HRegionInfo region)
      throws IOException {
    return getMetaTableRowResult(region, null, null, null);
  }

  /**
   * Returns a Result object constructed from the given region information simulating
   * a catalog table result.
   * @param region the HRegionInfo object or null
   * @param ServerName to use making startcode and server hostname:port in meta or null
   * @return A mocked up Result that fakes a Get on a row in the <code>hbase:meta</code> table.
   * @throws IOException
   */
  public static Result getMetaTableRowResult(final HRegionInfo region, final ServerName sn)
      throws IOException {
    return getMetaTableRowResult(region, sn, null, null);
  }

  /**
   * Returns a Result object constructed from the given region information simulating
   * a catalog table result.
   * @param region the HRegionInfo object or null
   * @param ServerName to use making startcode and server hostname:port in meta or null
   * @param splita daughter region or null
   * @param splitb  daughter region or null
   * @return A mocked up Result that fakes a Get on a row in the <code>hbase:meta</code> table.
   * @throws IOException
   */
  public static Result getMetaTableRowResult(HRegionInfo region, final ServerName sn,
      HRegionInfo splita, HRegionInfo splitb) throws IOException {
    List<Cell> kvs = new ArrayList<Cell>();
    if (region != null) {
      kvs.add(new KeyValue(
        region.getRegionName(),
        HConstants.CATALOG_FAMILY, HConstants.REGIONINFO_QUALIFIER,
        region.toByteArray()));
    }

    if (sn != null) {
      kvs.add(new KeyValue(region.getRegionName(),
        HConstants.CATALOG_FAMILY, HConstants.SERVER_QUALIFIER,
        Bytes.toBytes(sn.getHostAndPort())));
      kvs.add(new KeyValue(region.getRegionName(),
        HConstants.CATALOG_FAMILY, HConstants.STARTCODE_QUALIFIER,
        Bytes.toBytes(sn.getStartcode())));
    }

    if (splita != null) {
      kvs.add(new KeyValue(
          region.getRegionName(),
          HConstants.CATALOG_FAMILY, HConstants.SPLITA_QUALIFIER,
          splita.toByteArray()));
    }

    if (splitb != null) {
      kvs.add(new KeyValue(
          region.getRegionName(),
          HConstants.CATALOG_FAMILY, HConstants.SPLITB_QUALIFIER,
          splitb.toByteArray()));
    }

    //important: sort the kvs so that binary search work
    Collections.sort(kvs, KeyValue.META_COMPARATOR);

    return Result.create(kvs);
  }

  /**
   * @param sn  ServerName to use making startcode and server in meta
   * @param hri Region to serialize into HRegionInfo
   * @return A mocked up Result that fakes a Get on a row in the <code>hbase:meta</code> table.
   * @throws IOException
   */
  public static Result getMetaTableRowResultAsSplitRegion(final HRegionInfo hri, final ServerName sn)
    throws IOException {
    hri.setOffline(true);
    hri.setSplit(true);
    return getMetaTableRowResult(hri, sn);
  }

}
