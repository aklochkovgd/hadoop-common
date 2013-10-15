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
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.util.Records;

/**
 * <p>The request sent by the client to the <code>ResourceManager</code>
 * to fail an application attempt.</p>
 * 
 * <p>The request includes the {@link ApplicationId} of the application the
 * attempt to be failed belongs to.</p>
 * 
 * @see ApplicationClientProtocol#failCurrentAttempt(FailCurrentAttemptRequest)
 */
@Public
@Stable
public abstract class FailCurrentAttemptRequest {

  @Public
  @Stable 
  public static FailCurrentAttemptRequest newInstance(ApplicationId applicationId) {
    FailCurrentAttemptRequest request =
        Records.newRecord(FailCurrentAttemptRequest.class);
    request.setApplicationId(applicationId);
    return request;
  }

  /**
   * Get the <code>ApplicationId</code> of an application the attempt to be
   * failed belongs to.
   * @return <code>ApplicationId</code> of an application the attempt belongs to
   */
  @Public
  @Stable
  public abstract ApplicationId getApplicationId();
  
  @Public
  @Stable
  public abstract void setApplicationId(ApplicationId applicationId);
}
