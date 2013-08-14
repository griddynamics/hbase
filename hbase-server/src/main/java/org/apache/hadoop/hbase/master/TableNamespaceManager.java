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

package org.apache.hadoop.hbase.master;

import java.io.IOException;
import java.util.NavigableSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZKNamespaceManager;
import org.apache.hadoop.hbase.catalog.MetaReader;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.constraint.ConstraintException;
import org.apache.hadoop.hbase.master.handler.CreateTableHandler;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos;
import org.apache.hadoop.hbase.util.Bytes;

import com.google.common.collect.Sets;
import org.apache.hadoop.hbase.util.FSUtils;

/**
 * This is a helper class used to manage the namespace
 * metadata that is stored in TableName.NAMESPACE_TABLE_NAME
 * It also mirrors updates to the ZK store by forwarding updates to
 * {@link org.apache.hadoop.hbase.ZKNamespaceManager}
 */
@InterfaceAudience.Private
public class TableNamespaceManager {
  private static final Log LOG = LogFactory.getLog(TableNamespaceManager.class);

  private Configuration conf;
  private MasterServices masterServices;
  private HTable table;
  private ZKNamespaceManager zkNamespaceManager;

  public TableNamespaceManager(MasterServices masterServices) throws IOException {
    this.masterServices = masterServices;
    this.conf = masterServices.getConfiguration();
  }

  public void start() throws IOException {
    TableName tableName = TableName.NAMESPACE_TABLE_NAME;
    try {
      if (!MetaReader.tableExists(masterServices.getCatalogTracker(),
          tableName)) {
        LOG.info("Namespace table not found. Creating...");
        createNamespaceTable(masterServices);
      }
    } catch (InterruptedException e) {
      throw new IOException("Wait for namespace table assignment interrupted", e);
    }
    table = new HTable(conf, tableName);
    zkNamespaceManager = new ZKNamespaceManager(masterServices.getZooKeeper());
    zkNamespaceManager.start();

    if (get(NamespaceDescriptor.DEFAULT_NAMESPACE.getName()) == null) {
      create(NamespaceDescriptor.DEFAULT_NAMESPACE);
    }
    if (get(NamespaceDescriptor.SYSTEM_NAMESPACE.getName()) == null) {
      create(NamespaceDescriptor.SYSTEM_NAMESPACE);
    }

    ResultScanner scanner = table.getScanner(HTableDescriptor.NAMESPACE_FAMILY_INFO_BYTES);
    try {
      for(Result result : scanner) {
        NamespaceDescriptor ns =
            ProtobufUtil.toNamespaceDescriptor(
                HBaseProtos.NamespaceDescriptor.parseFrom(
                    result.getColumnLatest(HTableDescriptor.NAMESPACE_FAMILY_INFO_BYTES,
                        HTableDescriptor.NAMESPACE_COL_DESC_BYTES).getValue()));
        zkNamespaceManager.update(ns);
      }
    } finally {
      scanner.close();
    }
  }


  public synchronized NamespaceDescriptor get(String name) throws IOException {
    Result res = table.get(new Get(Bytes.toBytes(name)));
    if (res.isEmpty()) {
      return null;
    }
    return
        ProtobufUtil.toNamespaceDescriptor(
            HBaseProtos.NamespaceDescriptor.parseFrom(
                res.getColumnLatest(HTableDescriptor.NAMESPACE_FAMILY_INFO_BYTES,
                    HTableDescriptor.NAMESPACE_COL_DESC_BYTES).getValue()));
  }

  public synchronized void create(NamespaceDescriptor ns) throws IOException {
    if (get(ns.getName()) != null) {
      throw new ConstraintException("Namespace "+ns.getName()+" already exists");
    }
    FileSystem fs = masterServices.getMasterFileSystem().getFileSystem();
    fs.mkdirs(FSUtils.getNamespaceDir(
        masterServices.getMasterFileSystem().getRootDir(), ns.getName()));
    upsert(ns);
  }

  public synchronized void update(NamespaceDescriptor ns) throws IOException {
    if (get(ns.getName()) == null) {
      throw new ConstraintException("Namespace "+ns.getName()+" does not exist");
    }
    upsert(ns);
  }

  private void upsert(NamespaceDescriptor ns) throws IOException {
    Put p = new Put(Bytes.toBytes(ns.getName()));
    p.add(HTableDescriptor.NAMESPACE_FAMILY_INFO_BYTES,
        HTableDescriptor.NAMESPACE_COL_DESC_BYTES,
        ProtobufUtil.toProtoNamespaceDescriptor(ns).toByteArray());
    table.put(p);
    try {
      zkNamespaceManager.update(ns);
    } catch(IOException ex) {
      String msg = "Failed to update namespace information in ZK. Aborting.";
      LOG.fatal(msg, ex);
      masterServices.abort(msg, ex);
    }
  }

  public synchronized void remove(String name) throws IOException {
    if (NamespaceDescriptor.RESERVED_NAMESPACES.contains(name)) {
      throw new ConstraintException("Reserved namespace "+name+" cannot be removed.");
    }
    int tableCount = masterServices.listTableDescriptorsByNamespace(name).size();
    if (tableCount > 0) {
      throw new ConstraintException("Only empty namespaces can be removed. " +
          "Namespace "+name+" has "+tableCount+" tables");
    }
    Delete d = new Delete(Bytes.toBytes(name));
    table.delete(d);
    //don't abort if cleanup isn't complete
    //it will be replaced on new namespace creation
    zkNamespaceManager.remove(name);
    FileSystem fs = masterServices.getMasterFileSystem().getFileSystem();
    for(FileStatus status :
            fs.listStatus(FSUtils.getNamespaceDir(
                masterServices.getMasterFileSystem().getRootDir(), name))) {
      if (!HConstants.HBASE_NON_TABLE_DIRS.contains(status.getPath().getName())) {
        throw new IOException("Namespace directory contains table dir: "+status.getPath());
      }
    }
    if (!fs.delete(FSUtils.getNamespaceDir(
        masterServices.getMasterFileSystem().getRootDir(), name), true)) {
      throw new IOException("Failed to remove namespace: "+name);
    }
  }

  public synchronized NavigableSet<NamespaceDescriptor> list() throws IOException {
    NavigableSet<NamespaceDescriptor> ret =
        Sets.newTreeSet(NamespaceDescriptor.NAMESPACE_DESCRIPTOR_COMPARATOR);
    ResultScanner scanner = table.getScanner(HTableDescriptor.NAMESPACE_FAMILY_INFO_BYTES);
    try {
      for(Result r : scanner) {
          ret.add(ProtobufUtil.toNamespaceDescriptor(
              HBaseProtos.NamespaceDescriptor.parseFrom(
                r.getColumnLatest(HTableDescriptor.NAMESPACE_FAMILY_INFO_BYTES,
                    HTableDescriptor.NAMESPACE_COL_DESC_BYTES).getValue())));
      }
    } finally {
      scanner.close();
    }
    return ret;
  }

  private void createNamespaceTable(MasterServices masterServices) throws IOException, InterruptedException {
    HRegionInfo newRegions[] = new HRegionInfo[]{
        new HRegionInfo(HTableDescriptor.NAMESPACE_TABLEDESC.getTableName(), null, null)};

    //we need to create the table this way to bypass
    //checkInitialized
    masterServices.getExecutorService()
        .submit(new CreateTableHandler(masterServices,
            masterServices.getMasterFileSystem(),
            HTableDescriptor.NAMESPACE_TABLEDESC,
            masterServices.getConfiguration(),
            newRegions,
            masterServices).prepare());
    //wait for region to be online
    int tries = conf.getInt("hbase.master.namespace.init.timeout", 600);
    while(masterServices.getAssignmentManager()
        .getRegionStates().getRegionServerOfRegion(newRegions[0]) == null &&
        tries > 0) {
      Thread.sleep(100);
      tries--;
    }
    if (tries <= 0) {
      throw new IOException("Failed to create namespace table.");
    }
  }
}
