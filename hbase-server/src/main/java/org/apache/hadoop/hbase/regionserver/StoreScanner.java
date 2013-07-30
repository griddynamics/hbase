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

package org.apache.hadoop.hbase.regionserver;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.hbase.DoNotRetryIOException;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.executor.ExecutorService;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.regionserver.ScanInfo;
import org.apache.hadoop.hbase.regionserver.handler.ParallelSeekHandler;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.EnvironmentEdgeManager;

/**
 * Scanner scans both the memstore and the Store. Coalesce KeyValue stream
 * into List<KeyValue> for a single row.
 */
@InterfaceAudience.Private
public class StoreScanner extends NonLazyKeyValueScanner
    implements KeyValueScanner, InternalScanner, ChangedReadersObserver {
  static final Log LOG = LogFactory.getLog(StoreScanner.class);
  protected Store store;
  protected ScanQueryMatcher matcher;
  protected KeyValueHeap heap;
  protected boolean cacheBlocks;

  protected int countPerRow = 0;
  protected int storeLimit = -1;
  protected int storeOffset = 0;

  // Used to indicate that the scanner has closed (see HBASE-1107)
  // Doesnt need to be volatile because it's always accessed via synchronized methods
  protected boolean closing = false;
  protected final boolean isGet;
  protected final boolean explicitColumnQuery;
  protected final boolean useRowColBloom;
  /**
   * A flag that enables StoreFileScanner parallel-seeking
   */
  protected boolean isParallelSeekEnabled = false;
  protected ExecutorService executor;
  protected final Scan scan;
  protected final NavigableSet<byte[]> columns;
  protected final long oldestUnexpiredTS;
  protected final int minVersions;

  /**
   * The number of KVs seen by the scanner. Includes explicitly skipped KVs, but not
   * KVs skipped via seeking to next row/column. TODO: estimate them?
   */
  private long kvsScanned = 0;

  /** We don't ever expect to change this, the constant is just for clarity. */
  static final boolean LAZY_SEEK_ENABLED_BY_DEFAULT = true;
  public static final String STORESCANNER_PARALLEL_SEEK_ENABLE =
      "hbase.storescanner.parallel.seek.enable";

  /** Used during unit testing to ensure that lazy seek does save seek ops */
  protected static boolean lazySeekEnabledGlobally =
      LAZY_SEEK_ENABLED_BY_DEFAULT;

  // if heap == null and lastTop != null, you need to reseek given the key below
  protected KeyValue lastTop = null;

  /** An internal constructor. */
  protected StoreScanner(Store store, boolean cacheBlocks, Scan scan,
      final NavigableSet<byte[]> columns, long ttl, int minVersions) {
    this.store = store;
    this.cacheBlocks = cacheBlocks;
    isGet = scan.isGetScan();
    int numCol = columns == null ? 0 : columns.size();
    explicitColumnQuery = numCol > 0;
    this.scan = scan;
    this.columns = columns;
    oldestUnexpiredTS = EnvironmentEdgeManager.currentTimeMillis() - ttl;
    this.minVersions = minVersions;

    // We look up row-column Bloom filters for multi-column queries as part of
    // the seek operation. However, we also look the row-column Bloom filter
    // for multi-row (non-"get") scans because this is not done in
    // StoreFile.passesBloomFilter(Scan, SortedSet<byte[]>).
    useRowColBloom = numCol > 1 || (!isGet && numCol == 1);
    // The parallel-seeking is on :
    // 1) the config value is *true*
    // 2) store has more than one store file
    if (store != null && ((HStore)store).getHRegion() != null
        && store.getStorefilesCount() > 1) {
      RegionServerServices rsService = ((HStore)store).getHRegion().getRegionServerServices();
      if (rsService == null || !rsService.getConfiguration().getBoolean(
            STORESCANNER_PARALLEL_SEEK_ENABLE, false)) return;
      isParallelSeekEnabled = true;
      executor = rsService.getExecutorService();
    }
  }

  /**
   * Opens a scanner across memstore, snapshot, and all StoreFiles. Assumes we
   * are not in a compaction.
   *
   * @param store who we scan
   * @param scan the spec
   * @param columns which columns we are scanning
   * @throws IOException
   */
  public StoreScanner(Store store, ScanInfo scanInfo, Scan scan, final NavigableSet<byte[]> columns)
                              throws IOException {
    this(store, scan.getCacheBlocks(), scan, columns, scanInfo.getTtl(),
        scanInfo.getMinVersions());
    if (columns != null && scan.isRaw()) {
      throw new DoNotRetryIOException(
          "Cannot specify any column for a raw scan");
    }
    matcher = new ScanQueryMatcher(scan, scanInfo, columns,
        ScanType.USER_SCAN, Long.MAX_VALUE, HConstants.LATEST_TIMESTAMP,
        oldestUnexpiredTS);

    // Pass columns to try to filter out unnecessary StoreFiles.
    List<KeyValueScanner> scanners = getScannersNoCompaction();

    // Seek all scanners to the start of the Row (or if the exact matching row
    // key does not exist, then to the start of the next matching Row).
    // Always check bloom filter to optimize the top row seek for delete
    // family marker.
    if (explicitColumnQuery && lazySeekEnabledGlobally) {
      for (KeyValueScanner scanner : scanners) {
        scanner.requestSeek(matcher.getStartKey(), false, true);
      }
    } else {
      if (!isParallelSeekEnabled) {
        for (KeyValueScanner scanner : scanners) {
          scanner.seek(matcher.getStartKey());
        }
      } else {
        parallelSeek(scanners, matcher.getStartKey());
      }
    }

    // set storeLimit
    this.storeLimit = scan.getMaxResultsPerColumnFamily();

    // set rowOffset
    this.storeOffset = scan.getRowOffsetPerColumnFamily();

    // Combine all seeked scanners with a heap
    heap = new KeyValueHeap(scanners, store.getComparator());

    this.store.addChangedReaderObserver(this);
  }

  /**
   * Used for compactions.<p>
   *
   * Opens a scanner across specified StoreFiles.
   * @param store who we scan
   * @param scan the spec
   * @param scanners ancillary scanners
   * @param smallestReadPoint the readPoint that we should use for tracking
   *          versions
   */
  public StoreScanner(Store store, ScanInfo scanInfo, Scan scan,
      List<? extends KeyValueScanner> scanners, ScanType scanType,
      long smallestReadPoint, long earliestPutTs) throws IOException {
    this(store, scanInfo, scan, scanners, scanType, smallestReadPoint, earliestPutTs, null, null);
  }

  /**
   * Used for compactions that drop deletes from a limited range of rows.<p>
   *
   * Opens a scanner across specified StoreFiles.
   * @param store who we scan
   * @param scan the spec
   * @param scanners ancillary scanners
   * @param smallestReadPoint the readPoint that we should use for tracking versions
   * @param dropDeletesFromRow The inclusive left bound of the range; can be EMPTY_START_ROW.
   * @param dropDeletesToRow The exclusive right bound of the range; can be EMPTY_END_ROW.
   */
  public StoreScanner(Store store, ScanInfo scanInfo, Scan scan,
      List<? extends KeyValueScanner> scanners, long smallestReadPoint, long earliestPutTs,
      byte[] dropDeletesFromRow, byte[] dropDeletesToRow) throws IOException {
    this(store, scanInfo, scan, scanners, ScanType.COMPACT_RETAIN_DELETES, smallestReadPoint,
        earliestPutTs, dropDeletesFromRow, dropDeletesToRow);
  }

  private StoreScanner(Store store, ScanInfo scanInfo, Scan scan,
      List<? extends KeyValueScanner> scanners, ScanType scanType, long smallestReadPoint,
      long earliestPutTs, byte[] dropDeletesFromRow, byte[] dropDeletesToRow) throws IOException {
    this(store, false, scan, null, scanInfo.getTtl(),
        scanInfo.getMinVersions());
    if (dropDeletesFromRow == null) {
      matcher = new ScanQueryMatcher(scan, scanInfo, null, scanType,
          smallestReadPoint, earliestPutTs, oldestUnexpiredTS);
    } else {
      matcher = new ScanQueryMatcher(scan, scanInfo, null, smallestReadPoint,
          earliestPutTs, oldestUnexpiredTS, dropDeletesFromRow, dropDeletesToRow);
    }

    // Filter the list of scanners using Bloom filters, time range, TTL, etc.
    scanners = selectScannersFrom(scanners);

    // Seek all scanners to the initial key
    if (!isParallelSeekEnabled) {
      for (KeyValueScanner scanner : scanners) {
        scanner.seek(matcher.getStartKey());
      }
    } else {
      parallelSeek(scanners, matcher.getStartKey());
    }

    // Combine all seeked scanners with a heap
    heap = new KeyValueHeap(scanners, store.getComparator());
  }

  /** Constructor for testing. */
  StoreScanner(final Scan scan, ScanInfo scanInfo,
      ScanType scanType, final NavigableSet<byte[]> columns,
      final List<KeyValueScanner> scanners) throws IOException {
    this(scan, scanInfo, scanType, columns, scanners,
        HConstants.LATEST_TIMESTAMP);
  }

  // Constructor for testing.
  StoreScanner(final Scan scan, ScanInfo scanInfo,
      ScanType scanType, final NavigableSet<byte[]> columns,
      final List<KeyValueScanner> scanners, long earliestPutTs)
          throws IOException {
    this(null, scan.getCacheBlocks(), scan, columns, scanInfo.getTtl(),
        scanInfo.getMinVersions());
    this.matcher = new ScanQueryMatcher(scan, scanInfo, columns, scanType,
        Long.MAX_VALUE, earliestPutTs, oldestUnexpiredTS);

    // Seek all scanners to the initial key
    if (!isParallelSeekEnabled) {
      for (KeyValueScanner scanner : scanners) {
        scanner.seek(matcher.getStartKey());
      }
    } else {
      parallelSeek(scanners, matcher.getStartKey());
    }
    heap = new KeyValueHeap(scanners, scanInfo.getComparator());
  }

  /**
   * Get a filtered list of scanners. Assumes we are not in a compaction.
   * @return list of scanners to seek
   */
  protected List<KeyValueScanner> getScannersNoCompaction() throws IOException {
    final boolean isCompaction = false;
    return selectScannersFrom(store.getScanners(cacheBlocks, isGet,
        isCompaction, matcher, scan.getStartRow(), scan.getStopRow()));
  }

  /**
   * Filters the given list of scanners using Bloom filter, time range, and
   * TTL.
   */
  protected List<KeyValueScanner> selectScannersFrom(
      final List<? extends KeyValueScanner> allScanners) {
    boolean memOnly;
    boolean filesOnly;
    if (scan instanceof InternalScan) {
      InternalScan iscan = (InternalScan)scan;
      memOnly = iscan.isCheckOnlyMemStore();
      filesOnly = iscan.isCheckOnlyStoreFiles();
    } else {
      memOnly = false;
      filesOnly = false;
    }

    List<KeyValueScanner> scanners =
        new ArrayList<KeyValueScanner>(allScanners.size());

    // We can only exclude store files based on TTL if minVersions is set to 0.
    // Otherwise, we might have to return KVs that have technically expired.
    long expiredTimestampCutoff = minVersions == 0 ? oldestUnexpiredTS :
        Long.MIN_VALUE;

    // include only those scan files which pass all filters
    for (KeyValueScanner kvs : allScanners) {
      boolean isFile = kvs.isFileScanner();
      if ((!isFile && filesOnly) || (isFile && memOnly)) {
        continue;
      }

      if (kvs.shouldUseScanner(scan, columns, expiredTimestampCutoff)) {
        scanners.add(kvs);
      }
    }
    return scanners;
  }

  @Override
  public synchronized KeyValue peek() {
    if (this.heap == null) {
      return this.lastTop;
    }
    return this.heap.peek();
  }

  @Override
  public KeyValue next() {
    // throw runtime exception perhaps?
    throw new RuntimeException("Never call StoreScanner.next()");
  }

  @Override
  public synchronized void close() {
    if (this.closing) return;
    this.closing = true;
    // under test, we dont have a this.store
    if (this.store != null)
      this.store.deleteChangedReaderObserver(this);
    if (this.heap != null)
      this.heap.close();
    this.heap = null; // CLOSED!
    this.lastTop = null; // If both are null, we are closed.
  }

  @Override
  public synchronized boolean seek(KeyValue key) throws IOException {
    // reset matcher state, in case that underlying store changed
    checkReseek();
    return this.heap.seek(key);
  }

  /**
   * Get the next row of values from this Store.
   * @param outResult
   * @param limit
   * @return true if there are more rows, false if scanner is done
   */
  @Override
  public synchronized boolean next(List<KeyValue> outResult, int limit) throws IOException {
    if (checkReseek()) {
      return true;
    }

    // if the heap was left null, then the scanners had previously run out anyways, close and
    // return.
    if (this.heap == null) {
      close();
      return false;
    }

    KeyValue peeked = this.heap.peek();
    if (peeked == null) {
      close();
      return false;
    }

    // only call setRow if the row changes; avoids confusing the query matcher
    // if scanning intra-row
    byte[] row = peeked.getBuffer();
    int offset = peeked.getRowOffset();
    short length = peeked.getRowLength();
    if (limit < 0 || matcher.row == null || !Bytes.equals(row, offset, length, matcher.row,
        matcher.rowOffset, matcher.rowLength)) {
      this.countPerRow = 0;
      matcher.setRow(row, offset, length);
    }

    KeyValue kv;
    KeyValue prevKV = null;

    // Only do a sanity-check if store and comparator are available.
    KeyValue.KVComparator comparator =
        store != null ? store.getComparator() : null;

    int count = 0;
    LOOP: while((kv = this.heap.peek()) != null) {
      ++kvsScanned;
      // Check that the heap gives us KVs in an increasing order.
      assert prevKV == null || comparator == null || comparator.compare(prevKV, kv) <= 0 :
        "Key " + prevKV + " followed by a " + "smaller key " + kv + " in cf " + store;
      prevKV = kv;

      ScanQueryMatcher.MatchCode qcode = matcher.match(kv);
      switch(qcode) {
        case INCLUDE:
        case INCLUDE_AND_SEEK_NEXT_ROW:
        case INCLUDE_AND_SEEK_NEXT_COL:

          Filter f = matcher.getFilter();
          if (f != null) {
            kv = f.transform(kv);
          }

          this.countPerRow++;
          if (storeLimit > -1 &&
              this.countPerRow > (storeLimit + storeOffset)) {
            // do what SEEK_NEXT_ROW does.
            if (!matcher.moreRowsMayExistAfter(kv)) {
              return false;
            }
            reseek(matcher.getKeyForNextRow(kv));
            break LOOP;
          }

          // add to results only if we have skipped #storeOffset kvs
          // also update metric accordingly
          if (this.countPerRow > storeOffset) {
            outResult.add(kv);
            count++;
          }

          if (qcode == ScanQueryMatcher.MatchCode.INCLUDE_AND_SEEK_NEXT_ROW) {
            if (!matcher.moreRowsMayExistAfter(kv)) {
              return false;
            }
            reseek(matcher.getKeyForNextRow(kv));
          } else if (qcode == ScanQueryMatcher.MatchCode.INCLUDE_AND_SEEK_NEXT_COL) {
            reseek(matcher.getKeyForNextColumn(kv));
          } else {
            this.heap.next();
          }

          if (limit > 0 && (count == limit)) {
            break LOOP;
          }
          continue;

        case DONE:
          return true;

        case DONE_SCAN:
          close();
          return false;

        case SEEK_NEXT_ROW:
          // This is just a relatively simple end of scan fix, to short-cut end
          // us if there is an endKey in the scan.
          if (!matcher.moreRowsMayExistAfter(kv)) {
            return false;
          }

          reseek(matcher.getKeyForNextRow(kv));
          break;

        case SEEK_NEXT_COL:
          reseek(matcher.getKeyForNextColumn(kv));
          break;

        case SKIP:
          this.heap.next();
          break;

        case SEEK_NEXT_USING_HINT:
          KeyValue nextKV = matcher.getNextKeyHint(kv);
          if (nextKV != null) {
            reseek(nextKV);
          } else {
            heap.next();
          }
          break;

        default:
          throw new RuntimeException("UNEXPECTED");
      }
    }

    if (count > 0) {
      return true;
    }

    // No more keys
    close();
    return false;
  }

  @Override
  public synchronized boolean next(List<KeyValue> outResult) throws IOException {
    return next(outResult, -1);
  }

  // Implementation of ChangedReadersObserver
  @Override
  public synchronized void updateReaders() throws IOException {
    if (this.closing) return;

    // All public synchronized API calls will call 'checkReseek' which will cause
    // the scanner stack to reseek if this.heap==null && this.lastTop != null.
    // But if two calls to updateReaders() happen without a 'next' or 'peek' then we
    // will end up calling this.peek() which would cause a reseek in the middle of a updateReaders
    // which is NOT what we want, not to mention could cause an NPE. So we early out here.
    if (this.heap == null) return;

    // this could be null.
    this.lastTop = this.peek();

    //DebugPrint.println("SS updateReaders, topKey = " + lastTop);

    // close scanners to old obsolete Store files
    this.heap.close(); // bubble thru and close all scanners.
    this.heap = null; // the re-seeks could be slow (access HDFS) free up memory ASAP

    // Let the next() call handle re-creating and seeking
  }

  /**
   * @return true if top of heap has changed (and KeyValueHeap has to try the
   *         next KV)
   * @throws IOException
   */
  protected boolean checkReseek() throws IOException {
    if (this.heap == null && this.lastTop != null) {
      resetScannerStack(this.lastTop);
      if (this.heap.peek() == null
          || store.getComparator().compareRows(this.lastTop, this.heap.peek()) != 0) {
        LOG.debug("Storescanner.peek() is changed where before = "
            + this.lastTop.toString() + ",and after = " + this.heap.peek());
        this.lastTop = null;
        return true;
      }
      this.lastTop = null; // gone!
    }
    // else dont need to reseek
    return false;
  }

  protected void resetScannerStack(KeyValue lastTopKey) throws IOException {
    if (heap != null) {
      throw new RuntimeException("StoreScanner.reseek run on an existing heap!");
    }

    /* When we have the scan object, should we not pass it to getScanners()
     * to get a limited set of scanners? We did so in the constructor and we
     * could have done it now by storing the scan object from the constructor */
    List<KeyValueScanner> scanners = getScannersNoCompaction();

    if (!isParallelSeekEnabled) {
      for (KeyValueScanner scanner : scanners) {
        scanner.seek(lastTopKey);
      }
    } else {
      parallelSeek(scanners, lastTopKey);
    }

    // Combine all seeked scanners with a heap
    heap = new KeyValueHeap(scanners, store.getComparator());

    // Reset the state of the Query Matcher and set to top row.
    // Only reset and call setRow if the row changes; avoids confusing the
    // query matcher if scanning intra-row.
    KeyValue kv = heap.peek();
    if (kv == null) {
      kv = lastTopKey;
    }
    byte[] row = kv.getBuffer();
    int offset = kv.getRowOffset();
    short length = kv.getRowLength();
    if ((matcher.row == null) || !Bytes.equals(row, offset, length, matcher.row,
        matcher.rowOffset, matcher.rowLength)) {
      this.countPerRow = 0;
      matcher.reset();
      matcher.setRow(row, offset, length);
    }
  }

  @Override
  public synchronized boolean reseek(KeyValue kv) throws IOException {
    //Heap will not be null, if this is called from next() which.
    //If called from RegionScanner.reseek(...) make sure the scanner
    //stack is reset if needed.
    checkReseek();
    if (explicitColumnQuery && lazySeekEnabledGlobally) {
      return heap.requestSeek(kv, true, useRowColBloom);
    }
    return heap.reseek(kv);
  }

  @Override
  public long getSequenceID() {
    return 0;
  }

  /**
   * Seek storefiles in parallel to optimize IO latency as much as possible
   * @param scanners the list {@link KeyValueScanner}s to be read from
   * @param kv the KeyValue on which the operation is being requested
   * @throws IOException
   */
  private void parallelSeek(final List<? extends KeyValueScanner>
      scanners, final KeyValue kv) throws IOException {
    if (scanners.isEmpty()) return;
    int storeFileScannerCount = scanners.size();
    CountDownLatch latch = new CountDownLatch(storeFileScannerCount);
    List<ParallelSeekHandler> handlers = 
        new ArrayList<ParallelSeekHandler>(storeFileScannerCount);
    for (KeyValueScanner scanner : scanners) {
      if (scanner instanceof StoreFileScanner) {
        ParallelSeekHandler seekHandler = new ParallelSeekHandler(scanner, kv,
          MultiVersionConsistencyControl.getThreadReadPoint(), latch);
        executor.submit(seekHandler);
        handlers.add(seekHandler);
      } else {
        scanner.seek(kv);
        latch.countDown();
      }
    }

    try {
      latch.await();
    } catch (InterruptedException ie) {
      throw new InterruptedIOException(ie.getMessage());
    }

    for (ParallelSeekHandler handler : handlers) {
      if (handler.getErr() != null) {
        throw new IOException(handler.getErr());
      }
    }
  }

  /**
   * Used in testing.
   * @return all scanners in no particular order
   */
  List<KeyValueScanner> getAllScannersForTesting() {
    List<KeyValueScanner> allScanners = new ArrayList<KeyValueScanner>();
    KeyValueScanner current = heap.getCurrentForTesting();
    if (current != null)
      allScanners.add(current);
    for (KeyValueScanner scanner : heap.getHeap())
      allScanners.add(scanner);
    return allScanners;
  }

  static void enableLazySeekGlobally(boolean enable) {
    lazySeekEnabledGlobally = enable;
  }

  /**
   * @return The estimated number of KVs seen by this scanner (includes some skipped KVs).
   */
  public long getEstimatedNumberOfKvsScanned() {
    return this.kvsScanned;
  }
}

