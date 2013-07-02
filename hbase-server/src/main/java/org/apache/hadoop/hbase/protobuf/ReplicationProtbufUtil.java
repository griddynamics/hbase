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

package org.apache.hadoop.hbase.protobuf;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.io.SizedCellScanner;
import org.apache.hadoop.hbase.ipc.PayloadCarryingRpcController;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.AdminService;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos;
import org.apache.hadoop.hbase.protobuf.generated.WALProtos;
import org.apache.hadoop.hbase.regionserver.wal.HLog;
import org.apache.hadoop.hbase.regionserver.wal.HLogKey;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;

import com.google.protobuf.ByteString;
import com.google.protobuf.ServiceException;

public class ReplicationProtbufUtil {
  /**
   * Get the HLog entries from a list of protocol buffer WALEntry
   *
   * @param protoList the list of protocol buffer WALEntry
   * @return an array of HLog entries
   */
  public static HLog.Entry[]
      toHLogEntries(final List<AdminProtos.WALEntry> protoList) throws IOException {
    List<HLog.Entry> entries = new ArrayList<HLog.Entry>();
    for (AdminProtos.WALEntry entry: protoList) {
      WALProtos.WALKey walKey = entry.getKey();
      HLogKey key = new HLogKey(walKey);
      WALEdit edit = new WALEdit();
      for (ByteString keyValue: entry.getKeyValueBytesList()) {
        edit.add(new KeyValue(keyValue.toByteArray()));
      }
      if (walKey.getScopesCount() > 0) {
        TreeMap<byte[], Integer> scopes =
          new TreeMap<byte[], Integer>(Bytes.BYTES_COMPARATOR);
        for (WALProtos.FamilyScope scope: walKey.getScopesList()) {
          scopes.put(scope.getFamily().toByteArray(),
            Integer.valueOf(scope.getScopeType().ordinal()));
        }
        key.setScopes(scopes);
      }
      entries.add(new HLog.Entry(key, edit));
    }
    return entries.toArray(new HLog.Entry[entries.size()]);
  }

  /**
   * A helper to replicate a list of HLog entries using admin protocol.
   *
   * @param admin
   * @param entries
   * @throws java.io.IOException
   */
  public static void replicateWALEntry(final AdminService.BlockingInterface admin,
      final HLog.Entry[] entries) throws IOException {
    Pair<AdminProtos.ReplicateWALEntryRequest, CellScanner> p =
      buildReplicateWALEntryRequest(entries);
    try {
      PayloadCarryingRpcController controller = new PayloadCarryingRpcController(p.getSecond());
      admin.replicateWALEntry(controller, p.getFirst());
    } catch (ServiceException se) {
      throw ProtobufUtil.getRemoteException(se);
    }
  }

  /**
   * Create a new ReplicateWALEntryRequest from a list of HLog entries
   *
   * @param entries the HLog entries to be replicated
   * @return a pair of ReplicateWALEntryRequest and a CellScanner over all the WALEdit values
   * found.
   */
  public static Pair<AdminProtos.ReplicateWALEntryRequest, CellScanner>
      buildReplicateWALEntryRequest(final HLog.Entry[] entries) {
    // Accumulate all the KVs seen in here.
    List<List<? extends Cell>> allkvs = new ArrayList<List<? extends Cell>>(entries.length);
    int size = 0;
    WALProtos.FamilyScope.Builder scopeBuilder = WALProtos.FamilyScope.newBuilder();
    AdminProtos.WALEntry.Builder entryBuilder = AdminProtos.WALEntry.newBuilder();
    AdminProtos.ReplicateWALEntryRequest.Builder builder =
      AdminProtos.ReplicateWALEntryRequest.newBuilder();
    for (HLog.Entry entry: entries) {
      entryBuilder.clear();
      WALProtos.WALKey.Builder keyBuilder = entryBuilder.getKeyBuilder();
      HLogKey key = entry.getKey();
      keyBuilder.setEncodedRegionName(
        ByteString.copyFrom(key.getEncodedRegionName()));
      keyBuilder.setTableName(ByteString.copyFrom(key.getTablename()));
      keyBuilder.setLogSequenceNumber(key.getLogSeqNum());
      keyBuilder.setWriteTime(key.getWriteTime());
      UUID clusterId = key.getClusterId();
      if (clusterId != null) {
        HBaseProtos.UUID.Builder uuidBuilder = keyBuilder.getClusterIdBuilder();
        uuidBuilder.setLeastSigBits(clusterId.getLeastSignificantBits());
        uuidBuilder.setMostSigBits(clusterId.getMostSignificantBits());
      }
      WALEdit edit = entry.getEdit();
      NavigableMap<byte[], Integer> scopes = key.getScopes();
      if (scopes != null && !scopes.isEmpty()) {
        for (Map.Entry<byte[], Integer> scope: scopes.entrySet()) {
          scopeBuilder.setFamily(ByteString.copyFrom(scope.getKey()));
          WALProtos.ScopeType scopeType =
              WALProtos.ScopeType.valueOf(scope.getValue().intValue());
          scopeBuilder.setScopeType(scopeType);
          keyBuilder.addScopes(scopeBuilder.build());
        }
      }
      List<KeyValue> kvs = edit.getKeyValues();
      // Add up the size.  It is used later serializing out the kvs.
      for (KeyValue kv: kvs) {
        size += kv.getLength();
      }
      // Collect up the kvs
      allkvs.add(kvs);
      // Write out how many kvs associated with this entry.
      entryBuilder.setAssociatedCellCount(kvs.size());
      builder.addEntry(entryBuilder.build());
    }
    return new Pair<AdminProtos.ReplicateWALEntryRequest, CellScanner>(builder.build(),
      getCellScanner(allkvs, size));
  }

  /**
   * @param cells
   * @return <code>cells</code> packaged as a CellScanner
   */
  static CellScanner getCellScanner(final List<List<? extends Cell>> cells, final int size) {
    return new SizedCellScanner() {
      private final Iterator<List<? extends Cell>> entries = cells.iterator();
      private Iterator<? extends Cell> currentIterator = null;
      private Cell currentCell;

      @Override
      public Cell current() {
        return this.currentCell;
      }

      @Override
      public boolean advance() {
        if (this.currentIterator == null) {
          if (!this.entries.hasNext()) return false;
          this.currentIterator = this.entries.next().iterator();
        }
        if (this.currentIterator.hasNext()) {
          this.currentCell = this.currentIterator.next();
          return true;
        }
        this.currentCell = null;
        this.currentIterator = null;
        return advance();
      }

      @Override
      public long heapSize() {
        return size;
      }
    };
  }
}