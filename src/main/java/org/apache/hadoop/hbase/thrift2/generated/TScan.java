/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.hadoop.hbase.thrift2.generated;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Any timestamps in the columns are ignored, use timeRange to select by timestamp.
 * Max versions defaults to 1.
 */
public class TScan implements org.apache.thrift.TBase<TScan, TScan._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TScan");

  private static final org.apache.thrift.protocol.TField START_ROW_FIELD_DESC = new org.apache.thrift.protocol.TField("startRow", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField STOP_ROW_FIELD_DESC = new org.apache.thrift.protocol.TField("stopRow", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField COLUMNS_FIELD_DESC = new org.apache.thrift.protocol.TField("columns", org.apache.thrift.protocol.TType.LIST, (short)3);
  private static final org.apache.thrift.protocol.TField CACHING_FIELD_DESC = new org.apache.thrift.protocol.TField("caching", org.apache.thrift.protocol.TType.I32, (short)4);
  private static final org.apache.thrift.protocol.TField MAX_VERSIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("maxVersions", org.apache.thrift.protocol.TType.I32, (short)5);
  private static final org.apache.thrift.protocol.TField TIME_RANGE_FIELD_DESC = new org.apache.thrift.protocol.TField("timeRange", org.apache.thrift.protocol.TType.STRUCT, (short)6);
  private static final org.apache.thrift.protocol.TField FILTER_STRING_FIELD_DESC = new org.apache.thrift.protocol.TField("filterString", org.apache.thrift.protocol.TType.STRING, (short)7);
  private static final org.apache.thrift.protocol.TField BATCH_SIZE_FIELD_DESC = new org.apache.thrift.protocol.TField("batchSize", org.apache.thrift.protocol.TType.I32, (short)8);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TScanStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TScanTupleSchemeFactory());
  }

  public ByteBuffer startRow; // optional
  public ByteBuffer stopRow; // optional
  public List<TColumn> columns; // optional
  public int caching; // optional
  public int maxVersions; // optional
  public TTimeRange timeRange; // optional
  public ByteBuffer filterString; // optional
  public int batchSize; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    START_ROW((short)1, "startRow"),
    STOP_ROW((short)2, "stopRow"),
    COLUMNS((short)3, "columns"),
    CACHING((short)4, "caching"),
    MAX_VERSIONS((short)5, "maxVersions"),
    TIME_RANGE((short)6, "timeRange"),
    FILTER_STRING((short)7, "filterString"),
    BATCH_SIZE((short)8, "batchSize");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // START_ROW
          return START_ROW;
        case 2: // STOP_ROW
          return STOP_ROW;
        case 3: // COLUMNS
          return COLUMNS;
        case 4: // CACHING
          return CACHING;
        case 5: // MAX_VERSIONS
          return MAX_VERSIONS;
        case 6: // TIME_RANGE
          return TIME_RANGE;
        case 7: // FILTER_STRING
          return FILTER_STRING;
        case 8: // BATCH_SIZE
          return BATCH_SIZE;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __CACHING_ISSET_ID = 0;
  private static final int __MAXVERSIONS_ISSET_ID = 1;
  private static final int __BATCHSIZE_ISSET_ID = 2;
  private BitSet __isset_bit_vector = new BitSet(3);
  private _Fields optionals[] = {_Fields.START_ROW,_Fields.STOP_ROW,_Fields.COLUMNS,_Fields.CACHING,_Fields.MAX_VERSIONS,_Fields.TIME_RANGE,_Fields.FILTER_STRING,_Fields.BATCH_SIZE};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.START_ROW, new org.apache.thrift.meta_data.FieldMetaData("startRow", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.STOP_ROW, new org.apache.thrift.meta_data.FieldMetaData("stopRow", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.COLUMNS, new org.apache.thrift.meta_data.FieldMetaData("columns", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TColumn.class))));
    tmpMap.put(_Fields.CACHING, new org.apache.thrift.meta_data.FieldMetaData("caching", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.MAX_VERSIONS, new org.apache.thrift.meta_data.FieldMetaData("maxVersions", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.TIME_RANGE, new org.apache.thrift.meta_data.FieldMetaData("timeRange", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TTimeRange.class)));
    tmpMap.put(_Fields.FILTER_STRING, new org.apache.thrift.meta_data.FieldMetaData("filterString", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.BATCH_SIZE, new org.apache.thrift.meta_data.FieldMetaData("batchSize", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TScan.class, metaDataMap);
  }

  public TScan() {
    this.maxVersions = 1;

  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TScan(TScan other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetStartRow()) {
      this.startRow = org.apache.thrift.TBaseHelper.copyBinary(other.startRow);
;
    }
    if (other.isSetStopRow()) {
      this.stopRow = org.apache.thrift.TBaseHelper.copyBinary(other.stopRow);
;
    }
    if (other.isSetColumns()) {
      List<TColumn> __this__columns = new ArrayList<TColumn>();
      for (TColumn other_element : other.columns) {
        __this__columns.add(new TColumn(other_element));
      }
      this.columns = __this__columns;
    }
    this.caching = other.caching;
    this.maxVersions = other.maxVersions;
    if (other.isSetTimeRange()) {
      this.timeRange = new TTimeRange(other.timeRange);
    }
    if (other.isSetFilterString()) {
      this.filterString = org.apache.thrift.TBaseHelper.copyBinary(other.filterString);
;
    }
    this.batchSize = other.batchSize;
  }

  public TScan deepCopy() {
    return new TScan(this);
  }

  @Override
  public void clear() {
    this.startRow = null;
    this.stopRow = null;
    this.columns = null;
    setCachingIsSet(false);
    this.caching = 0;
    this.maxVersions = 1;

    this.timeRange = null;
    this.filterString = null;
    setBatchSizeIsSet(false);
    this.batchSize = 0;
  }

  public byte[] getStartRow() {
    setStartRow(org.apache.thrift.TBaseHelper.rightSize(startRow));
    return startRow == null ? null : startRow.array();
  }

  public ByteBuffer bufferForStartRow() {
    return startRow;
  }

  public TScan setStartRow(byte[] startRow) {
    setStartRow(startRow == null ? (ByteBuffer)null : ByteBuffer.wrap(startRow));
    return this;
  }

  public TScan setStartRow(ByteBuffer startRow) {
    this.startRow = startRow;
    return this;
  }

  public void unsetStartRow() {
    this.startRow = null;
  }

  /** Returns true if field startRow is set (has been assigned a value) and false otherwise */
  public boolean isSetStartRow() {
    return this.startRow != null;
  }

  public void setStartRowIsSet(boolean value) {
    if (!value) {
      this.startRow = null;
    }
  }

  public byte[] getStopRow() {
    setStopRow(org.apache.thrift.TBaseHelper.rightSize(stopRow));
    return stopRow == null ? null : stopRow.array();
  }

  public ByteBuffer bufferForStopRow() {
    return stopRow;
  }

  public TScan setStopRow(byte[] stopRow) {
    setStopRow(stopRow == null ? (ByteBuffer)null : ByteBuffer.wrap(stopRow));
    return this;
  }

  public TScan setStopRow(ByteBuffer stopRow) {
    this.stopRow = stopRow;
    return this;
  }

  public void unsetStopRow() {
    this.stopRow = null;
  }

  /** Returns true if field stopRow is set (has been assigned a value) and false otherwise */
  public boolean isSetStopRow() {
    return this.stopRow != null;
  }

  public void setStopRowIsSet(boolean value) {
    if (!value) {
      this.stopRow = null;
    }
  }

  public int getColumnsSize() {
    return (this.columns == null) ? 0 : this.columns.size();
  }

  public java.util.Iterator<TColumn> getColumnsIterator() {
    return (this.columns == null) ? null : this.columns.iterator();
  }

  public void addToColumns(TColumn elem) {
    if (this.columns == null) {
      this.columns = new ArrayList<TColumn>();
    }
    this.columns.add(elem);
  }

  public List<TColumn> getColumns() {
    return this.columns;
  }

  public TScan setColumns(List<TColumn> columns) {
    this.columns = columns;
    return this;
  }

  public void unsetColumns() {
    this.columns = null;
  }

  /** Returns true if field columns is set (has been assigned a value) and false otherwise */
  public boolean isSetColumns() {
    return this.columns != null;
  }

  public void setColumnsIsSet(boolean value) {
    if (!value) {
      this.columns = null;
    }
  }

  public int getCaching() {
    return this.caching;
  }

  public TScan setCaching(int caching) {
    this.caching = caching;
    setCachingIsSet(true);
    return this;
  }

  public void unsetCaching() {
    __isset_bit_vector.clear(__CACHING_ISSET_ID);
  }

  /** Returns true if field caching is set (has been assigned a value) and false otherwise */
  public boolean isSetCaching() {
    return __isset_bit_vector.get(__CACHING_ISSET_ID);
  }

  public void setCachingIsSet(boolean value) {
    __isset_bit_vector.set(__CACHING_ISSET_ID, value);
  }

  public int getMaxVersions() {
    return this.maxVersions;
  }

  public TScan setMaxVersions(int maxVersions) {
    this.maxVersions = maxVersions;
    setMaxVersionsIsSet(true);
    return this;
  }

  public void unsetMaxVersions() {
    __isset_bit_vector.clear(__MAXVERSIONS_ISSET_ID);
  }

  /** Returns true if field maxVersions is set (has been assigned a value) and false otherwise */
  public boolean isSetMaxVersions() {
    return __isset_bit_vector.get(__MAXVERSIONS_ISSET_ID);
  }

  public void setMaxVersionsIsSet(boolean value) {
    __isset_bit_vector.set(__MAXVERSIONS_ISSET_ID, value);
  }

  public TTimeRange getTimeRange() {
    return this.timeRange;
  }

  public TScan setTimeRange(TTimeRange timeRange) {
    this.timeRange = timeRange;
    return this;
  }

  public void unsetTimeRange() {
    this.timeRange = null;
  }

  /** Returns true if field timeRange is set (has been assigned a value) and false otherwise */
  public boolean isSetTimeRange() {
    return this.timeRange != null;
  }

  public void setTimeRangeIsSet(boolean value) {
    if (!value) {
      this.timeRange = null;
    }
  }

  public byte[] getFilterString() {
    setFilterString(org.apache.thrift.TBaseHelper.rightSize(filterString));
    return filterString == null ? null : filterString.array();
  }

  public ByteBuffer bufferForFilterString() {
    return filterString;
  }

  public TScan setFilterString(byte[] filterString) {
    setFilterString(filterString == null ? (ByteBuffer)null : ByteBuffer.wrap(filterString));
    return this;
  }

  public TScan setFilterString(ByteBuffer filterString) {
    this.filterString = filterString;
    return this;
  }

  public void unsetFilterString() {
    this.filterString = null;
  }

  /** Returns true if field filterString is set (has been assigned a value) and false otherwise */
  public boolean isSetFilterString() {
    return this.filterString != null;
  }

  public void setFilterStringIsSet(boolean value) {
    if (!value) {
      this.filterString = null;
    }
  }

  public int getBatchSize() {
    return this.batchSize;
  }

  public TScan setBatchSize(int batchSize) {
    this.batchSize = batchSize;
    setBatchSizeIsSet(true);
    return this;
  }

  public void unsetBatchSize() {
    __isset_bit_vector.clear(__BATCHSIZE_ISSET_ID);
  }

  /** Returns true if field batchSize is set (has been assigned a value) and false otherwise */
  public boolean isSetBatchSize() {
    return __isset_bit_vector.get(__BATCHSIZE_ISSET_ID);
  }

  public void setBatchSizeIsSet(boolean value) {
    __isset_bit_vector.set(__BATCHSIZE_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case START_ROW:
      if (value == null) {
        unsetStartRow();
      } else {
        setStartRow((ByteBuffer)value);
      }
      break;

    case STOP_ROW:
      if (value == null) {
        unsetStopRow();
      } else {
        setStopRow((ByteBuffer)value);
      }
      break;

    case COLUMNS:
      if (value == null) {
        unsetColumns();
      } else {
        setColumns((List<TColumn>)value);
      }
      break;

    case CACHING:
      if (value == null) {
        unsetCaching();
      } else {
        setCaching((Integer)value);
      }
      break;

    case MAX_VERSIONS:
      if (value == null) {
        unsetMaxVersions();
      } else {
        setMaxVersions((Integer)value);
      }
      break;

    case TIME_RANGE:
      if (value == null) {
        unsetTimeRange();
      } else {
        setTimeRange((TTimeRange)value);
      }
      break;

    case FILTER_STRING:
      if (value == null) {
        unsetFilterString();
      } else {
        setFilterString((ByteBuffer)value);
      }
      break;

    case BATCH_SIZE:
      if (value == null) {
        unsetBatchSize();
      } else {
        setBatchSize((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case START_ROW:
      return getStartRow();

    case STOP_ROW:
      return getStopRow();

    case COLUMNS:
      return getColumns();

    case CACHING:
      return Integer.valueOf(getCaching());

    case MAX_VERSIONS:
      return Integer.valueOf(getMaxVersions());

    case TIME_RANGE:
      return getTimeRange();

    case FILTER_STRING:
      return getFilterString();

    case BATCH_SIZE:
      return Integer.valueOf(getBatchSize());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case START_ROW:
      return isSetStartRow();
    case STOP_ROW:
      return isSetStopRow();
    case COLUMNS:
      return isSetColumns();
    case CACHING:
      return isSetCaching();
    case MAX_VERSIONS:
      return isSetMaxVersions();
    case TIME_RANGE:
      return isSetTimeRange();
    case FILTER_STRING:
      return isSetFilterString();
    case BATCH_SIZE:
      return isSetBatchSize();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TScan)
      return this.equals((TScan)that);
    return false;
  }

  public boolean equals(TScan that) {
    if (that == null)
      return false;

    boolean this_present_startRow = true && this.isSetStartRow();
    boolean that_present_startRow = true && that.isSetStartRow();
    if (this_present_startRow || that_present_startRow) {
      if (!(this_present_startRow && that_present_startRow))
        return false;
      if (!this.startRow.equals(that.startRow))
        return false;
    }

    boolean this_present_stopRow = true && this.isSetStopRow();
    boolean that_present_stopRow = true && that.isSetStopRow();
    if (this_present_stopRow || that_present_stopRow) {
      if (!(this_present_stopRow && that_present_stopRow))
        return false;
      if (!this.stopRow.equals(that.stopRow))
        return false;
    }

    boolean this_present_columns = true && this.isSetColumns();
    boolean that_present_columns = true && that.isSetColumns();
    if (this_present_columns || that_present_columns) {
      if (!(this_present_columns && that_present_columns))
        return false;
      if (!this.columns.equals(that.columns))
        return false;
    }

    boolean this_present_caching = true && this.isSetCaching();
    boolean that_present_caching = true && that.isSetCaching();
    if (this_present_caching || that_present_caching) {
      if (!(this_present_caching && that_present_caching))
        return false;
      if (this.caching != that.caching)
        return false;
    }

    boolean this_present_maxVersions = true && this.isSetMaxVersions();
    boolean that_present_maxVersions = true && that.isSetMaxVersions();
    if (this_present_maxVersions || that_present_maxVersions) {
      if (!(this_present_maxVersions && that_present_maxVersions))
        return false;
      if (this.maxVersions != that.maxVersions)
        return false;
    }

    boolean this_present_timeRange = true && this.isSetTimeRange();
    boolean that_present_timeRange = true && that.isSetTimeRange();
    if (this_present_timeRange || that_present_timeRange) {
      if (!(this_present_timeRange && that_present_timeRange))
        return false;
      if (!this.timeRange.equals(that.timeRange))
        return false;
    }

    boolean this_present_filterString = true && this.isSetFilterString();
    boolean that_present_filterString = true && that.isSetFilterString();
    if (this_present_filterString || that_present_filterString) {
      if (!(this_present_filterString && that_present_filterString))
        return false;
      if (!this.filterString.equals(that.filterString))
        return false;
    }

    boolean this_present_batchSize = true && this.isSetBatchSize();
    boolean that_present_batchSize = true && that.isSetBatchSize();
    if (this_present_batchSize || that_present_batchSize) {
      if (!(this_present_batchSize && that_present_batchSize))
        return false;
      if (this.batchSize != that.batchSize)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(TScan other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    TScan typedOther = (TScan)other;

    lastComparison = Boolean.valueOf(isSetStartRow()).compareTo(typedOther.isSetStartRow());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStartRow()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.startRow, typedOther.startRow);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetStopRow()).compareTo(typedOther.isSetStopRow());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStopRow()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.stopRow, typedOther.stopRow);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetColumns()).compareTo(typedOther.isSetColumns());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetColumns()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.columns, typedOther.columns);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetCaching()).compareTo(typedOther.isSetCaching());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCaching()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.caching, typedOther.caching);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetMaxVersions()).compareTo(typedOther.isSetMaxVersions());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMaxVersions()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.maxVersions, typedOther.maxVersions);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTimeRange()).compareTo(typedOther.isSetTimeRange());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTimeRange()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.timeRange, typedOther.timeRange);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetFilterString()).compareTo(typedOther.isSetFilterString());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFilterString()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.filterString, typedOther.filterString);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetBatchSize()).compareTo(typedOther.isSetBatchSize());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetBatchSize()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.batchSize, typedOther.batchSize);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TScan(");
    boolean first = true;

    if (isSetStartRow()) {
      sb.append("startRow:");
      if (this.startRow == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.startRow, sb);
      }
      first = false;
    }
    if (isSetStopRow()) {
      if (!first) sb.append(", ");
      sb.append("stopRow:");
      if (this.stopRow == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.stopRow, sb);
      }
      first = false;
    }
    if (isSetColumns()) {
      if (!first) sb.append(", ");
      sb.append("columns:");
      if (this.columns == null) {
        sb.append("null");
      } else {
        sb.append(this.columns);
      }
      first = false;
    }
    if (isSetCaching()) {
      if (!first) sb.append(", ");
      sb.append("caching:");
      sb.append(this.caching);
      first = false;
    }
    if (isSetMaxVersions()) {
      if (!first) sb.append(", ");
      sb.append("maxVersions:");
      sb.append(this.maxVersions);
      first = false;
    }
    if (isSetTimeRange()) {
      if (!first) sb.append(", ");
      sb.append("timeRange:");
      if (this.timeRange == null) {
        sb.append("null");
      } else {
        sb.append(this.timeRange);
      }
      first = false;
    }
    if (isSetFilterString()) {
      if (!first) sb.append(", ");
      sb.append("filterString:");
      if (this.filterString == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.filterString, sb);
      }
      first = false;
    }
    if (isSetBatchSize()) {
      if (!first) sb.append(", ");
      sb.append("batchSize:");
      sb.append(this.batchSize);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TScanStandardSchemeFactory implements SchemeFactory {
    public TScanStandardScheme getScheme() {
      return new TScanStandardScheme();
    }
  }

  private static class TScanStandardScheme extends StandardScheme<TScan> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TScan struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // START_ROW
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.startRow = iprot.readBinary();
              struct.setStartRowIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // STOP_ROW
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.stopRow = iprot.readBinary();
              struct.setStopRowIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // COLUMNS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list40 = iprot.readListBegin();
                struct.columns = new ArrayList<TColumn>(_list40.size);
                for (int _i41 = 0; _i41 < _list40.size; ++_i41)
                {
                  TColumn _elem42; // required
                  _elem42 = new TColumn();
                  _elem42.read(iprot);
                  struct.columns.add(_elem42);
                }
                iprot.readListEnd();
              }
              struct.setColumnsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // CACHING
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.caching = iprot.readI32();
              struct.setCachingIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // MAX_VERSIONS
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.maxVersions = iprot.readI32();
              struct.setMaxVersionsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // TIME_RANGE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.timeRange = new TTimeRange();
              struct.timeRange.read(iprot);
              struct.setTimeRangeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 7: // FILTER_STRING
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.filterString = iprot.readBinary();
              struct.setFilterStringIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 8: // BATCH_SIZE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.batchSize = iprot.readI32();
              struct.setBatchSizeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TScan struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.startRow != null) {
        if (struct.isSetStartRow()) {
          oprot.writeFieldBegin(START_ROW_FIELD_DESC);
          oprot.writeBinary(struct.startRow);
          oprot.writeFieldEnd();
        }
      }
      if (struct.stopRow != null) {
        if (struct.isSetStopRow()) {
          oprot.writeFieldBegin(STOP_ROW_FIELD_DESC);
          oprot.writeBinary(struct.stopRow);
          oprot.writeFieldEnd();
        }
      }
      if (struct.columns != null) {
        if (struct.isSetColumns()) {
          oprot.writeFieldBegin(COLUMNS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.columns.size()));
            for (TColumn _iter43 : struct.columns)
            {
              _iter43.write(oprot);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.isSetCaching()) {
        oprot.writeFieldBegin(CACHING_FIELD_DESC);
        oprot.writeI32(struct.caching);
        oprot.writeFieldEnd();
      }
      if (struct.isSetMaxVersions()) {
        oprot.writeFieldBegin(MAX_VERSIONS_FIELD_DESC);
        oprot.writeI32(struct.maxVersions);
        oprot.writeFieldEnd();
      }
      if (struct.timeRange != null) {
        if (struct.isSetTimeRange()) {
          oprot.writeFieldBegin(TIME_RANGE_FIELD_DESC);
          struct.timeRange.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.filterString != null) {
        if (struct.isSetFilterString()) {
          oprot.writeFieldBegin(FILTER_STRING_FIELD_DESC);
          oprot.writeBinary(struct.filterString);
          oprot.writeFieldEnd();
        }
      }
      if (struct.isSetBatchSize()) {
        oprot.writeFieldBegin(BATCH_SIZE_FIELD_DESC);
        oprot.writeI32(struct.batchSize);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TScanTupleSchemeFactory implements SchemeFactory {
    public TScanTupleScheme getScheme() {
      return new TScanTupleScheme();
    }
  }

  private static class TScanTupleScheme extends TupleScheme<TScan> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TScan struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetStartRow()) {
        optionals.set(0);
      }
      if (struct.isSetStopRow()) {
        optionals.set(1);
      }
      if (struct.isSetColumns()) {
        optionals.set(2);
      }
      if (struct.isSetCaching()) {
        optionals.set(3);
      }
      if (struct.isSetMaxVersions()) {
        optionals.set(4);
      }
      if (struct.isSetTimeRange()) {
        optionals.set(5);
      }
      if (struct.isSetFilterString()) {
        optionals.set(6);
      }
      if (struct.isSetBatchSize()) {
        optionals.set(7);
      }
      oprot.writeBitSet(optionals, 8);
      if (struct.isSetStartRow()) {
        oprot.writeBinary(struct.startRow);
      }
      if (struct.isSetStopRow()) {
        oprot.writeBinary(struct.stopRow);
      }
      if (struct.isSetColumns()) {
        {
          oprot.writeI32(struct.columns.size());
          for (TColumn _iter44 : struct.columns)
          {
            _iter44.write(oprot);
          }
        }
      }
      if (struct.isSetCaching()) {
        oprot.writeI32(struct.caching);
      }
      if (struct.isSetMaxVersions()) {
        oprot.writeI32(struct.maxVersions);
      }
      if (struct.isSetTimeRange()) {
        struct.timeRange.write(oprot);
      }
      if (struct.isSetFilterString()) {
        oprot.writeBinary(struct.filterString);
      }
      if (struct.isSetBatchSize()) {
        oprot.writeI32(struct.batchSize);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TScan struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(8);
      if (incoming.get(0)) {
        struct.startRow = iprot.readBinary();
        struct.setStartRowIsSet(true);
      }
      if (incoming.get(1)) {
        struct.stopRow = iprot.readBinary();
        struct.setStopRowIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list45 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.columns = new ArrayList<TColumn>(_list45.size);
          for (int _i46 = 0; _i46 < _list45.size; ++_i46)
          {
            TColumn _elem47; // required
            _elem47 = new TColumn();
            _elem47.read(iprot);
            struct.columns.add(_elem47);
          }
        }
        struct.setColumnsIsSet(true);
      }
      if (incoming.get(3)) {
        struct.caching = iprot.readI32();
        struct.setCachingIsSet(true);
      }
      if (incoming.get(4)) {
        struct.maxVersions = iprot.readI32();
        struct.setMaxVersionsIsSet(true);
      }
      if (incoming.get(5)) {
        struct.timeRange = new TTimeRange();
        struct.timeRange.read(iprot);
        struct.setTimeRangeIsSet(true);
      }
      if (incoming.get(6)) {
        struct.filterString = iprot.readBinary();
        struct.setFilterStringIsSet(true);
      }
      if (incoming.get(7)) {
        struct.batchSize = iprot.readI32();
        struct.setBatchSizeIsSet(true);
      }
    }
  }

}

