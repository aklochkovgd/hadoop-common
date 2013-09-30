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

package org.apache.hadoop.yarn.api.protocolrecords;

import java.util.Map;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.yarn.api.ContainerManagementProtocol;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.SerializedException;
import org.apache.hadoop.yarn.util.Records;

/**
 * <p>
 * The response sent by the <code>NodeManager</code> to the
 * <code>ApplicationMaster</code> when asked to send a signal
 * to requested containers.
 * </p>
 * 
 * The response is empty.
 * 
 * @see ContainerManagementProtocol#signalContainers(org.apache.hadoop.yarn.api.SignalContainersRequest)
 */
@Public
@Stable
public abstract class SignalContainersResponse {

  @Private
  @Unstable
  public static SignalContainersResponse newInstance(
      Map<ContainerId, SerializedException> failedRequests) {
    SignalContainersResponse response =
        Records.newRecord(SignalContainersResponse.class);
    response.setFailedRequests(failedRequests);
    return response;
  }
  
  /**
   * Get the containerId-to-exception map in which the exception indicates error
   * from per container for failed requests
   */
  @Public
  @Stable
  public abstract Map<ContainerId, SerializedException> getFailedRequests();

  /**
   * Set the containerId-to-exception map in which the exception indicates error
   * from per container for failed requests
   */
  @Private
  @Unstable
  public abstract void setFailedRequests(
      Map<ContainerId, SerializedException> failedContainers);

}
