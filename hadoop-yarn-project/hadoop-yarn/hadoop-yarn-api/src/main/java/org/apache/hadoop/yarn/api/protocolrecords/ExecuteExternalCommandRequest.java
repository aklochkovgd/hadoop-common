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

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.yarn.api.ApplicationClientProtocol;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.util.Records;

/**
 * <p>The request sent by a client to the <code>ResourceManager</code> to 
 * execute an external command.</p>
 * 
 * <p>The request should include the {@link ContainerId}.</p>
 * 
 * @see ApplicationClientProtocol#executeExternalCommand(ExecuteExternalCommandRequest)
 */
@Public
@Stable
public abstract class ExecuteExternalCommandRequest {

  @Public
  @Stable
  public static ExecuteExternalCommandRequest newInstance(
      ContainerId containerId) {
    ExecuteExternalCommandRequest request =
        Records.newRecord(ExecuteExternalCommandRequest.class);
    request.setContainerId(containerId);
    return request;
  }

  /**
   * Get the <code>ContainerId</code> of a container to manage.
   * @return <code>ContainerId</code> of the container
   */
  @Public
  @Stable
  public abstract ContainerId getContainerId();
  
  /**
   * Set the <code>ContainerId</code> of the container
   * @param containerId <code>ContainerId</code> of the container
   */
  @Public
  @Stable
  public abstract void setContainerId(ContainerId containerId);
}
