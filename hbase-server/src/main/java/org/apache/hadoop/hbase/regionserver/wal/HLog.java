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

package org.apache.hadoop.hbase.regionserver.wal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.exceptions.FailedLogCloseException;
import org.apache.hadoop.io.Writable;


@InterfaceAudience.Private
public interface HLog {
  public static final Log LOG = LogFactory.getLog(HLog.class);

  /** File Extension used while splitting an HLog into regions (HBASE-2312) */
  public static final String SPLITTING_EXT = "-splitting";
  public static final boolean SPLIT_SKIP_ERRORS_DEFAULT = false;
  /** The META region's HLog filename extension */
  public static final String META_HLOG_FILE_EXTN = ".meta";

  static final Pattern EDITFILES_NAME_PATTERN = Pattern.compile("-?[0-9]+");
  public static final String RECOVERED_LOG_TMPFILE_SUFFIX = ".temp";

  public interface Reader {

    /**
     * @param fs File system.
     * @param path Path.
     * @param c Config.
     * @param s Input stream that may have been pre-opened by the caller; may be null.
     */
    void init(FileSystem fs, Path path, Configuration c, FSDataInputStream s) throws IOException;

    void close() throws IOException;

    Entry next() throws IOException;

    Entry next(Entry reuse) throws IOException;

    void seek(long pos) throws IOException;

    long getPosition() throws IOException;
    void reset() throws IOException;
  }

  public interface Writer {
    void init(FileSystem fs, Path path, Configuration c) throws IOException;

    void close() throws IOException;

    void sync() throws IOException;

    void append(Entry entry) throws IOException;

    long getLength() throws IOException;
  }

  /**
   * Utility class that lets us keep track of the edit with it's key Only used
   * when splitting logs
   */
  public static class Entry implements Writable {
    private WALEdit edit;
    private HLogKey key;

    public Entry() {
      edit = new WALEdit();
      key = new HLogKey();
    }

    /**
     * Constructor for both params
     *
     * @param edit
     *          log's edit
     * @param key
     *          log's key
     */
    public Entry(HLogKey key, WALEdit edit) {
      super();
      this.key = key;
      this.edit = edit;
    }

    /**
     * Gets the edit
     *
     * @return edit
     */
    public WALEdit getEdit() {
      return edit;
    }

    /**
     * Gets the key
     *
     * @return key
     */
    public HLogKey getKey() {
      return key;
    }

    /**
     * Set compression context for this entry.
     *
     * @param compressionContext
     *          Compression context
     */
    public void setCompressionContext(CompressionContext compressionContext) {
      edit.setCompressionContext(compressionContext);
      key.setCompressionContext(compressionContext);
    }

    @Override
    public String toString() {
      return this.key + "=" + this.edit;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
      this.key.write(dataOutput);
      this.edit.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
      this.key.readFields(dataInput);
      this.edit.readFields(dataInput);
    }
  }

  /**
   * registers WALActionsListener
   *
   * @param listener
   */
  public void registerWALActionsListener(final WALActionsListener listener);

  /**
   * unregisters WALActionsListener
   *
   * @param listener
   */
  public boolean unregisterWALActionsListener(final WALActionsListener listener);

  /**
   * @return Current state of the monotonically increasing file id.
   */
  public long getFilenum();

  /**
   * Called by HRegionServer when it opens a new region to ensure that log
   * sequence numbers are always greater than the latest sequence number of the
   * region being brought on-line.
   *
   * @param newvalue
   *          We'll set log edit/sequence number to this value if it is greater
   *          than the current value.
   */
  public void setSequenceNumber(final long newvalue);

  /**
   * @return log sequence number
   */
  public long getSequenceNumber();

  /**
   * Roll the log writer. That is, start writing log messages to a new file.
   *
   * <p>
   * The implementation is synchronized in order to make sure there's one rollWriter
   * running at any given time.
   *
   * @return If lots of logs, flush the returned regions so next time through we
   *         can clean logs. Returns null if nothing to flush. Names are actual
   *         region names as returned by {@link HRegionInfo#getEncodedName()}
   * @throws org.apache.hadoop.hbase.exceptions.FailedLogCloseException
   * @throws IOException
   */
  public byte[][] rollWriter() throws FailedLogCloseException, IOException;

  /**
   * Roll the log writer. That is, start writing log messages to a new file.
   *
   * <p>
   * The implementation is synchronized in order to make sure there's one rollWriter
   * running at any given time.
   *
   * @param force
   *          If true, force creation of a new writer even if no entries have
   *          been written to the current writer
   * @return If lots of logs, flush the returned regions so next time through we
   *         can clean logs. Returns null if nothing to flush. Names are actual
   *         region names as returned by {@link HRegionInfo#getEncodedName()}
   * @throws org.apache.hadoop.hbase.exceptions.FailedLogCloseException
   * @throws IOException
   */
  public byte[][] rollWriter(boolean force) throws FailedLogCloseException,
      IOException;

  /**
   * Shut down the log.
   *
   * @throws IOException
   */
  public void close() throws IOException;

  /**
   * Shut down the log and delete the log directory
   *
   * @throws IOException
   */
  public void closeAndDelete() throws IOException;

  /**
   * Only used in tests.
   *
   * @param info
   * @param tableName
   * @param edits
   * @param now
   * @param htd
   * @throws IOException
   */
  public void append(HRegionInfo info, byte[] tableName, WALEdit edits,
      final long now, HTableDescriptor htd) throws IOException;

  /**
   * Append a set of edits to the log. Log edits are keyed by (encoded)
   * regionName, rowname, and log-sequence-id. The HLog is not flushed after
   * this transaction is written to the log.
   *
   * @param info
   * @param tableName
   * @param edits
   * @param clusterId
   *          The originating clusterId for this edit (for replication)
   * @param now
   * @return txid of this transaction
   * @throws IOException
   */
  public long appendNoSync(HRegionInfo info, byte[] tableName, WALEdit edits,
      UUID clusterId, final long now, HTableDescriptor htd) throws IOException;

  /**
   * Append a set of edits to the log. Log edits are keyed by (encoded)
   * regionName, rowname, and log-sequence-id. The HLog is flushed after this
   * transaction is written to the log.
   *
   * @param info
   * @param tableName
   * @param edits
   * @param clusterId
   *          The originating clusterId for this edit (for replication)
   * @param now
   * @param htd
   * @return txid of this transaction
   * @throws IOException
   */
  public long append(HRegionInfo info, byte[] tableName, WALEdit edits,
      UUID clusterId, final long now, HTableDescriptor htd) throws IOException;

  public void hsync() throws IOException;

  public void hflush() throws IOException;

  public void sync() throws IOException;

  public void sync(long txid) throws IOException;

  /**
   * Obtain a log sequence number.
   */
  public long obtainSeqNum();

  /**
   * WAL keeps track of the sequence numbers that were not yet flushed from memstores
   * in order to be able to do cleanup. This method tells WAL that some region is about
   * to flush memstore.
   *
   * We stash the oldest seqNum for the region, and let the the next edit inserted in this
   * region be recorded in {@link #append(HRegionInfo, byte[], WALEdit, long, HTableDescriptor)}
   * as new oldest seqnum. In case of flush being aborted, we put the stashed value back;
   * in case of flush succeeding, the seqNum of that first edit after start becomes the
   * valid oldest seqNum for this region.
   *
   * @return current seqNum, to pass on to flushers (who will put it into the metadata of
   *         the resulting file as an upper-bound seqNum for that file), or NULL if flush
   *         should not be started.
   */
  public Long startCacheFlush(final byte[] encodedRegionName);

  /**
   * Complete the cache flush.
   * @param encodedRegionName Encoded region name.
   */
  public void completeCacheFlush(final byte[] encodedRegionName);

  /**
   * Abort a cache flush. Call if the flush fails. Note that the only recovery
   * for an aborted flush currently is a restart of the regionserver so the
   * snapshot content dropped by the failure gets restored to the memstore.v
   * @param encodedRegionName Encoded region name.
   */
  public void abortCacheFlush(byte[] encodedRegionName);

  /**
   * @return Coprocessor host.
   */
  public WALCoprocessorHost getCoprocessorHost();

  /**
   * Get LowReplication-Roller status
   *
   * @return lowReplicationRollEnabled
   */
  public boolean isLowReplicationRollEnabled();

  /** Gets the earliest sequence number in the memstore for this particular region.
   * This can serve as best-effort "recent" WAL number for this region.
   * @param encodedRegionName The region to get the number for.
   * @return The number if present, HConstants.NO_SEQNUM if absent.
   */
  public long getEarliestMemstoreSeqNum(byte[] encodedRegionName);
}
