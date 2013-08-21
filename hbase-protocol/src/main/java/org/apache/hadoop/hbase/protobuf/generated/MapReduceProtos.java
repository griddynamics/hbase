// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: MapReduce.proto

package org.apache.hadoop.hbase.protobuf.generated;

public final class MapReduceProtos {
  private MapReduceProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface ScanMetricsOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // repeated .NameInt64Pair metrics = 1;
    /**
     * <code>repeated .NameInt64Pair metrics = 1;</code>
     */
    java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair> 
        getMetricsList();
    /**
     * <code>repeated .NameInt64Pair metrics = 1;</code>
     */
    org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair getMetrics(int index);
    /**
     * <code>repeated .NameInt64Pair metrics = 1;</code>
     */
    int getMetricsCount();
    /**
     * <code>repeated .NameInt64Pair metrics = 1;</code>
     */
    java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64PairOrBuilder> 
        getMetricsOrBuilderList();
    /**
     * <code>repeated .NameInt64Pair metrics = 1;</code>
     */
    org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64PairOrBuilder getMetricsOrBuilder(
        int index);
  }
  /**
   * Protobuf type {@code ScanMetrics}
   */
  public static final class ScanMetrics extends
      com.google.protobuf.GeneratedMessage
      implements ScanMetricsOrBuilder {
    // Use ScanMetrics.newBuilder() to construct.
    private ScanMetrics(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private ScanMetrics(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final ScanMetrics defaultInstance;
    public static ScanMetrics getDefaultInstance() {
      return defaultInstance;
    }

    public ScanMetrics getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private ScanMetrics(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                metrics_ = new java.util.ArrayList<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair>();
                mutable_bitField0_ |= 0x00000001;
              }
              metrics_.add(input.readMessage(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.PARSER, extensionRegistry));
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
          metrics_ = java.util.Collections.unmodifiableList(metrics_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.internal_static_ScanMetrics_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.internal_static_ScanMetrics_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics.class, org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics.Builder.class);
    }

    public static com.google.protobuf.Parser<ScanMetrics> PARSER =
        new com.google.protobuf.AbstractParser<ScanMetrics>() {
      public ScanMetrics parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new ScanMetrics(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<ScanMetrics> getParserForType() {
      return PARSER;
    }

    // repeated .NameInt64Pair metrics = 1;
    public static final int METRICS_FIELD_NUMBER = 1;
    private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair> metrics_;
    /**
     * <code>repeated .NameInt64Pair metrics = 1;</code>
     */
    public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair> getMetricsList() {
      return metrics_;
    }
    /**
     * <code>repeated .NameInt64Pair metrics = 1;</code>
     */
    public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64PairOrBuilder> 
        getMetricsOrBuilderList() {
      return metrics_;
    }
    /**
     * <code>repeated .NameInt64Pair metrics = 1;</code>
     */
    public int getMetricsCount() {
      return metrics_.size();
    }
    /**
     * <code>repeated .NameInt64Pair metrics = 1;</code>
     */
    public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair getMetrics(int index) {
      return metrics_.get(index);
    }
    /**
     * <code>repeated .NameInt64Pair metrics = 1;</code>
     */
    public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64PairOrBuilder getMetricsOrBuilder(
        int index) {
      return metrics_.get(index);
    }

    private void initFields() {
      metrics_ = java.util.Collections.emptyList();
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      for (int i = 0; i < metrics_.size(); i++) {
        output.writeMessage(1, metrics_.get(i));
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      for (int i = 0; i < metrics_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, metrics_.get(i));
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics)) {
        return super.equals(obj);
      }
      org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics other = (org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics) obj;

      boolean result = true;
      result = result && getMetricsList()
          .equals(other.getMetricsList());
      result = result &&
          getUnknownFields().equals(other.getUnknownFields());
      return result;
    }

    private int memoizedHashCode = 0;
    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptorForType().hashCode();
      if (getMetricsCount() > 0) {
        hash = (37 * hash) + METRICS_FIELD_NUMBER;
        hash = (53 * hash) + getMetricsList().hashCode();
      }
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code ScanMetrics}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetricsOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.internal_static_ScanMetrics_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.internal_static_ScanMetrics_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics.class, org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics.Builder.class);
      }

      // Construct using org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          getMetricsFieldBuilder();
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        if (metricsBuilder_ == null) {
          metrics_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          metricsBuilder_.clear();
        }
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.internal_static_ScanMetrics_descriptor;
      }

      public org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics getDefaultInstanceForType() {
        return org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics.getDefaultInstance();
      }

      public org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics build() {
        org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics buildPartial() {
        org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics result = new org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics(this);
        int from_bitField0_ = bitField0_;
        if (metricsBuilder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001)) {
            metrics_ = java.util.Collections.unmodifiableList(metrics_);
            bitField0_ = (bitField0_ & ~0x00000001);
          }
          result.metrics_ = metrics_;
        } else {
          result.metrics_ = metricsBuilder_.build();
        }
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics) {
          return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics other) {
        if (other == org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics.getDefaultInstance()) return this;
        if (metricsBuilder_ == null) {
          if (!other.metrics_.isEmpty()) {
            if (metrics_.isEmpty()) {
              metrics_ = other.metrics_;
              bitField0_ = (bitField0_ & ~0x00000001);
            } else {
              ensureMetricsIsMutable();
              metrics_.addAll(other.metrics_);
            }
            onChanged();
          }
        } else {
          if (!other.metrics_.isEmpty()) {
            if (metricsBuilder_.isEmpty()) {
              metricsBuilder_.dispose();
              metricsBuilder_ = null;
              metrics_ = other.metrics_;
              bitField0_ = (bitField0_ & ~0x00000001);
              metricsBuilder_ = 
                com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                   getMetricsFieldBuilder() : null;
            } else {
              metricsBuilder_.addAllMessages(other.metrics_);
            }
          }
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos.ScanMetrics) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // repeated .NameInt64Pair metrics = 1;
      private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair> metrics_ =
        java.util.Collections.emptyList();
      private void ensureMetricsIsMutable() {
        if (!((bitField0_ & 0x00000001) == 0x00000001)) {
          metrics_ = new java.util.ArrayList<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair>(metrics_);
          bitField0_ |= 0x00000001;
         }
      }

      private com.google.protobuf.RepeatedFieldBuilder<
          org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64PairOrBuilder> metricsBuilder_;

      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair> getMetricsList() {
        if (metricsBuilder_ == null) {
          return java.util.Collections.unmodifiableList(metrics_);
        } else {
          return metricsBuilder_.getMessageList();
        }
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public int getMetricsCount() {
        if (metricsBuilder_ == null) {
          return metrics_.size();
        } else {
          return metricsBuilder_.getCount();
        }
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair getMetrics(int index) {
        if (metricsBuilder_ == null) {
          return metrics_.get(index);
        } else {
          return metricsBuilder_.getMessage(index);
        }
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public Builder setMetrics(
          int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair value) {
        if (metricsBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureMetricsIsMutable();
          metrics_.set(index, value);
          onChanged();
        } else {
          metricsBuilder_.setMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public Builder setMetrics(
          int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.Builder builderForValue) {
        if (metricsBuilder_ == null) {
          ensureMetricsIsMutable();
          metrics_.set(index, builderForValue.build());
          onChanged();
        } else {
          metricsBuilder_.setMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public Builder addMetrics(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair value) {
        if (metricsBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureMetricsIsMutable();
          metrics_.add(value);
          onChanged();
        } else {
          metricsBuilder_.addMessage(value);
        }
        return this;
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public Builder addMetrics(
          int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair value) {
        if (metricsBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureMetricsIsMutable();
          metrics_.add(index, value);
          onChanged();
        } else {
          metricsBuilder_.addMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public Builder addMetrics(
          org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.Builder builderForValue) {
        if (metricsBuilder_ == null) {
          ensureMetricsIsMutable();
          metrics_.add(builderForValue.build());
          onChanged();
        } else {
          metricsBuilder_.addMessage(builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public Builder addMetrics(
          int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.Builder builderForValue) {
        if (metricsBuilder_ == null) {
          ensureMetricsIsMutable();
          metrics_.add(index, builderForValue.build());
          onChanged();
        } else {
          metricsBuilder_.addMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public Builder addAllMetrics(
          java.lang.Iterable<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair> values) {
        if (metricsBuilder_ == null) {
          ensureMetricsIsMutable();
          super.addAll(values, metrics_);
          onChanged();
        } else {
          metricsBuilder_.addAllMessages(values);
        }
        return this;
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public Builder clearMetrics() {
        if (metricsBuilder_ == null) {
          metrics_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000001);
          onChanged();
        } else {
          metricsBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public Builder removeMetrics(int index) {
        if (metricsBuilder_ == null) {
          ensureMetricsIsMutable();
          metrics_.remove(index);
          onChanged();
        } else {
          metricsBuilder_.remove(index);
        }
        return this;
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.Builder getMetricsBuilder(
          int index) {
        return getMetricsFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64PairOrBuilder getMetricsOrBuilder(
          int index) {
        if (metricsBuilder_ == null) {
          return metrics_.get(index);  } else {
          return metricsBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64PairOrBuilder> 
           getMetricsOrBuilderList() {
        if (metricsBuilder_ != null) {
          return metricsBuilder_.getMessageOrBuilderList();
        } else {
          return java.util.Collections.unmodifiableList(metrics_);
        }
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.Builder addMetricsBuilder() {
        return getMetricsFieldBuilder().addBuilder(
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.getDefaultInstance());
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.Builder addMetricsBuilder(
          int index) {
        return getMetricsFieldBuilder().addBuilder(
            index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.getDefaultInstance());
      }
      /**
       * <code>repeated .NameInt64Pair metrics = 1;</code>
       */
      public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.Builder> 
           getMetricsBuilderList() {
        return getMetricsFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilder<
          org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64PairOrBuilder> 
          getMetricsFieldBuilder() {
        if (metricsBuilder_ == null) {
          metricsBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
              org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64PairOrBuilder>(
                  metrics_,
                  ((bitField0_ & 0x00000001) == 0x00000001),
                  getParentForChildren(),
                  isClean());
          metrics_ = null;
        }
        return metricsBuilder_;
      }

      // @@protoc_insertion_point(builder_scope:ScanMetrics)
    }

    static {
      defaultInstance = new ScanMetrics(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:ScanMetrics)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_ScanMetrics_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_ScanMetrics_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\017MapReduce.proto\032\013hbase.proto\".\n\013ScanMe" +
      "trics\022\037\n\007metrics\030\001 \003(\0132\016.NameInt64PairBB" +
      "\n*org.apache.hadoop.hbase.protobuf.gener" +
      "atedB\017MapReduceProtosH\001\240\001\001"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_ScanMetrics_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_ScanMetrics_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_ScanMetrics_descriptor,
              new java.lang.String[] { "Metrics", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.getDescriptor(),
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
