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

import java.util.List;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.util.Records;

/**
 * <p>
 * The request sent by the <code>ApplicationMaster</code> to the
 * <code>NodeManager</code> to send a signal to requested containers.
 * </p>
 */
@Public
@Stable
public abstract class SignalContainersRequest {

  @Public
  @Stable
  public static SignalContainersRequest newInstance(
      List<ContainerId> containerIds,
      int signal) {
    SignalContainersRequest request =
        Records.newRecord(SignalContainersRequest.class);
    request.setContainerIds(containerIds);
    request.setSignal(signal);
    return request;
  }
  
  /**
   * Get signal to send to containers. 
   */
  @Public
  @Stable
  public abstract int getSignal();
  
  /**
   * Set signal to send to containers. 
   */
  @Public
  @Stable
  public abstract void setSignal(int signal);

  /**
   * Get the list of <code>ContainerId</code>s of containers to 
   * send the signal to.
   * 
   * @return the list of <code>ContainerId</code>s of containers to 
   *         send the signal to.
   */
  @Public
  @Stable
  public abstract List<ContainerId> getContainerIds();

  /**
   * Set a list of <code>ContainerId</code>s of containers to 
   * send the signal to.
   * 
   * @param containerIds
   *          a list of <code>ContainerId</code>s of containers to 
   * send the signal to.
   */
  @Public
  @Stable
  public abstract void setContainerIds(List<ContainerId> containerIds);
}
