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
package org.apache.hadoop.yarn.api.records.impl.pb;


import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.util.Shell.OSType;
import org.apache.hadoop.yarn.api.records.ExternalCommand;
import org.apache.hadoop.yarn.proto.YarnProtos.ExternalCommandProto;
import org.apache.hadoop.yarn.proto.YarnProtos.ExternalCommandProtoOrBuilder;
import org.apache.hadoop.yarn.proto.YarnProtos.OSTypeProto;

import com.google.protobuf.TextFormat;

@Private
@Unstable
public class ExternalCommandPBImpl extends ExternalCommand {
  ExternalCommandProto proto = ExternalCommandProto.getDefaultInstance();
  ExternalCommandProto.Builder builder = null;
  boolean viaProto = false;

  public ExternalCommandPBImpl() {
    builder = ExternalCommandProto.newBuilder();
  }

  public ExternalCommandPBImpl(ExternalCommandProto proto) {
    this.proto = proto;
    viaProto = true;
  }

  public synchronized ExternalCommandProto getProto() {
    mergeLocalToBuilder();
    proto = viaProto ? proto : builder.build();
    viaProto = true;
    return proto;
  }

  @Override
  public int hashCode() {
    return getProto().hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == null)
      return false;
    if (other.getClass().isAssignableFrom(this.getClass())) {
      return this.getProto().equals(this.getClass().cast(other).getProto());
    }
    return false;
  }

  @Override
  public String toString() {
    return TextFormat.shortDebugString(getProto());
  }

  private synchronized void mergeLocalToBuilder() {
  }

  private synchronized void maybeInitBuilder() {
    if (viaProto || builder == null) {
      builder = ExternalCommandProto.newBuilder(proto);
    }
    viaProto = false;
  }

  private OSTypeProto convertToProtoFormat(OSType e) {
    return ProtoUtils.convertToProtoFormat(e);
  }

  private OSType convertFromProtoFormat(OSTypeProto e) {
    return ProtoUtils.convertFromProtoFormat(e);
  }

  @Override
  public OSType getOSType() {
    ExternalCommandProtoOrBuilder p = viaProto ? proto : builder;
    return convertFromProtoFormat(p.getOsType());
  }

  @Override
  public void setOSType(OSType osType) {
    maybeInitBuilder();
    if (osType == null) 
      builder.clearOsType();
    else
      builder.setOsType(convertToProtoFormat(osType));
  }

  @Override
  public String getCommand() {
    ExternalCommandProtoOrBuilder p = viaProto ? proto : builder;
    return p.getCommand();
  }

  @Override
  public void setCommand(String command) {
    maybeInitBuilder();
    if (command == null) 
      builder.clearCommand();
    else
      builder.setCommand(command);
  }

}  
