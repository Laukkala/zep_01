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
/**
 * Autogenerated by Thrift Compiler (0.20.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.teragrep.zep_01.interpreter.thrift;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.20.0)", date = "2025-03-14")
public class InterpreterCompletion implements org.apache.thrift.TBase<InterpreterCompletion, InterpreterCompletion._Fields>, java.io.Serializable, Cloneable, Comparable<InterpreterCompletion> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("InterpreterCompletion");

  private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField META_FIELD_DESC = new org.apache.thrift.protocol.TField("meta", org.apache.thrift.protocol.TType.STRING, (short)3);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new InterpreterCompletionStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new InterpreterCompletionTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.lang.String name; // required
  public @org.apache.thrift.annotation.Nullable java.lang.String value; // required
  public @org.apache.thrift.annotation.Nullable java.lang.String meta; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    NAME((short)1, "name"),
    VALUE((short)2, "value"),
    META((short)3, "meta");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // NAME
          return NAME;
        case 2: // VALUE
          return VALUE;
        case 3: // META
          return META;
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
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    @Override
    public short getThriftFieldId() {
      return _thriftId;
    }

    @Override
    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.META, new org.apache.thrift.meta_data.FieldMetaData("meta", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(InterpreterCompletion.class, metaDataMap);
  }

  public InterpreterCompletion() {
  }

  public InterpreterCompletion(
    java.lang.String name,
    java.lang.String value,
    java.lang.String meta)
  {
    this();
    this.name = name;
    this.value = value;
    this.meta = meta;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public InterpreterCompletion(InterpreterCompletion other) {
    if (other.isSetName()) {
      this.name = other.name;
    }
    if (other.isSetValue()) {
      this.value = other.value;
    }
    if (other.isSetMeta()) {
      this.meta = other.meta;
    }
  }

  @Override
  public InterpreterCompletion deepCopy() {
    return new InterpreterCompletion(this);
  }

  @Override
  public void clear() {
    this.name = null;
    this.value = null;
    this.meta = null;
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getName() {
    return this.name;
  }

  public InterpreterCompletion setName(@org.apache.thrift.annotation.Nullable java.lang.String name) {
    this.name = name;
    return this;
  }

  public void unsetName() {
    this.name = null;
  }

  /** Returns true if field name is set (has been assigned a value) and false otherwise */
  public boolean isSetName() {
    return this.name != null;
  }

  public void setNameIsSet(boolean value) {
    if (!value) {
      this.name = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getValue() {
    return this.value;
  }

  public InterpreterCompletion setValue(@org.apache.thrift.annotation.Nullable java.lang.String value) {
    this.value = value;
    return this;
  }

  public void unsetValue() {
    this.value = null;
  }

  /** Returns true if field value is set (has been assigned a value) and false otherwise */
  public boolean isSetValue() {
    return this.value != null;
  }

  public void setValueIsSet(boolean value) {
    if (!value) {
      this.value = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getMeta() {
    return this.meta;
  }

  public InterpreterCompletion setMeta(@org.apache.thrift.annotation.Nullable java.lang.String meta) {
    this.meta = meta;
    return this;
  }

  public void unsetMeta() {
    this.meta = null;
  }

  /** Returns true if field meta is set (has been assigned a value) and false otherwise */
  public boolean isSetMeta() {
    return this.meta != null;
  }

  public void setMetaIsSet(boolean value) {
    if (!value) {
      this.meta = null;
    }
  }

  @Override
  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case NAME:
      if (value == null) {
        unsetName();
      } else {
        setName((java.lang.String)value);
      }
      break;

    case VALUE:
      if (value == null) {
        unsetValue();
      } else {
        setValue((java.lang.String)value);
      }
      break;

    case META:
      if (value == null) {
        unsetMeta();
      } else {
        setMeta((java.lang.String)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  @Override
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case NAME:
      return getName();

    case VALUE:
      return getValue();

    case META:
      return getMeta();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  @Override
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case NAME:
      return isSetName();
    case VALUE:
      return isSetValue();
    case META:
      return isSetMeta();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that instanceof InterpreterCompletion)
      return this.equals((InterpreterCompletion)that);
    return false;
  }

  public boolean equals(InterpreterCompletion that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_name = true && this.isSetName();
    boolean that_present_name = true && that.isSetName();
    if (this_present_name || that_present_name) {
      if (!(this_present_name && that_present_name))
        return false;
      if (!this.name.equals(that.name))
        return false;
    }

    boolean this_present_value = true && this.isSetValue();
    boolean that_present_value = true && that.isSetValue();
    if (this_present_value || that_present_value) {
      if (!(this_present_value && that_present_value))
        return false;
      if (!this.value.equals(that.value))
        return false;
    }

    boolean this_present_meta = true && this.isSetMeta();
    boolean that_present_meta = true && that.isSetMeta();
    if (this_present_meta || that_present_meta) {
      if (!(this_present_meta && that_present_meta))
        return false;
      if (!this.meta.equals(that.meta))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetName()) ? 131071 : 524287);
    if (isSetName())
      hashCode = hashCode * 8191 + name.hashCode();

    hashCode = hashCode * 8191 + ((isSetValue()) ? 131071 : 524287);
    if (isSetValue())
      hashCode = hashCode * 8191 + value.hashCode();

    hashCode = hashCode * 8191 + ((isSetMeta()) ? 131071 : 524287);
    if (isSetMeta())
      hashCode = hashCode * 8191 + meta.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(InterpreterCompletion other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.compare(isSetName(), other.isSetName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, other.name);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.compare(isSetValue(), other.isSetValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, other.value);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.compare(isSetMeta(), other.isSetMeta());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMeta()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.meta, other.meta);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  @Override
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  @Override
  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  @Override
  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("InterpreterCompletion(");
    boolean first = true;

    sb.append("name:");
    if (this.name == null) {
      sb.append("null");
    } else {
      sb.append(this.name);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("value:");
    if (this.value == null) {
      sb.append("null");
    } else {
      sb.append(this.value);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("meta:");
    if (this.meta == null) {
      sb.append("null");
    } else {
      sb.append(this.meta);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class InterpreterCompletionStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    @Override
    public InterpreterCompletionStandardScheme getScheme() {
      return new InterpreterCompletionStandardScheme();
    }
  }

  private static class InterpreterCompletionStandardScheme extends org.apache.thrift.scheme.StandardScheme<InterpreterCompletion> {

    @Override
    public void read(org.apache.thrift.protocol.TProtocol iprot, InterpreterCompletion struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.name = iprot.readString();
              struct.setNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.value = iprot.readString();
              struct.setValueIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // META
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.meta = iprot.readString();
              struct.setMetaIsSet(true);
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

    @Override
    public void write(org.apache.thrift.protocol.TProtocol oprot, InterpreterCompletion struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.name != null) {
        oprot.writeFieldBegin(NAME_FIELD_DESC);
        oprot.writeString(struct.name);
        oprot.writeFieldEnd();
      }
      if (struct.value != null) {
        oprot.writeFieldBegin(VALUE_FIELD_DESC);
        oprot.writeString(struct.value);
        oprot.writeFieldEnd();
      }
      if (struct.meta != null) {
        oprot.writeFieldBegin(META_FIELD_DESC);
        oprot.writeString(struct.meta);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class InterpreterCompletionTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    @Override
    public InterpreterCompletionTupleScheme getScheme() {
      return new InterpreterCompletionTupleScheme();
    }
  }

  private static class InterpreterCompletionTupleScheme extends org.apache.thrift.scheme.TupleScheme<InterpreterCompletion> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, InterpreterCompletion struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetName()) {
        optionals.set(0);
      }
      if (struct.isSetValue()) {
        optionals.set(1);
      }
      if (struct.isSetMeta()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetName()) {
        oprot.writeString(struct.name);
      }
      if (struct.isSetValue()) {
        oprot.writeString(struct.value);
      }
      if (struct.isSetMeta()) {
        oprot.writeString(struct.meta);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, InterpreterCompletion struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.name = iprot.readString();
        struct.setNameIsSet(true);
      }
      if (incoming.get(1)) {
        struct.value = iprot.readString();
        struct.setValueIsSet(true);
      }
      if (incoming.get(2)) {
        struct.meta = iprot.readString();
        struct.setMetaIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

