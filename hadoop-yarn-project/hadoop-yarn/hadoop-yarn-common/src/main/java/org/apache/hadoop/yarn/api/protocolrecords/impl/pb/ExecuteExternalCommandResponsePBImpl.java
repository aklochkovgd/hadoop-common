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

package org.apache.hadoop.yarn.api.protocolrecords.impl.pb;


import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.yarn.api.protocolrecords.ExecuteExternalCommandResponse;
import org.apache.hadoop.yarn.proto.YarnServiceProtos.ExecuteExternalCommandResponseProto;
import org.apache.hadoop.yarn.proto.YarnServiceProtos.ExecuteExternalCommandResponseProtoOrBuilder;

import com.google.protobuf.TextFormat;

@Private
@Unstable
public class ExecuteExternalCommandResponsePBImpl 
    extends ExecuteExternalCommandResponse {
	ExecuteExternalCommandResponseProto proto = ExecuteExternalCommandResponseProto.getDefaultInstance();
	ExecuteExternalCommandResponseProto.Builder builder = null;
  boolean viaProto = false;
  
  private String outputTail = null;
  
  public ExecuteExternalCommandResponsePBImpl() {
    builder = ExecuteExternalCommandResponseProto.newBuilder();
  }

  public ExecuteExternalCommandResponsePBImpl(ExecuteExternalCommandResponseProto proto) {
    this.proto = proto;
    viaProto = true;
  }
  
  public ExecuteExternalCommandResponseProto getProto() {
      mergeLocalToProto();
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

  private void mergeLocalToBuilder() {
    if (this.outputTail != null) {
      builder.setOutputTail(this.outputTail);
    }
  }

  private void mergeLocalToProto() {
    if (viaProto) 
      maybeInitBuilder();
    mergeLocalToBuilder();
    proto = builder.build();
    viaProto = true;
  }

  private void maybeInitBuilder() {
    if (viaProto || builder == null) {
      builder = ExecuteExternalCommandResponseProto.newBuilder(proto);
    }
    viaProto = false;
  }
    
  
  @Override
  public String getOutputTail() {
  	ExecuteExternalCommandResponseProtoOrBuilder p = viaProto ? proto : builder;
    if (this.outputTail != null) {
      return this.outputTail;
    }
    if (!p.hasOutputTail()) {
      return null;
    }
    this.outputTail = p.getOutputTail();
    return this.outputTail;
  }

  @Override
  public void setOutputTail(String outputTail) {
    maybeInitBuilder();
    if (outputTail == null) 
      builder.clearOutputTail();
    this.outputTail = outputTail;
  }

	@Override
	public int getExitCode() {
		ExecuteExternalCommandResponseProtoOrBuilder p = viaProto ? proto : builder;
		return p.getExitCode();
	}

	@Override
	public void setExitCode(int exitCode) {
		maybeInitBuilder();
    builder.setExitCode(exitCode);
	}

}  
