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

package org.apache.hadoop.yarn.server.resourcemanager.scheduler;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceRequest;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.attempt.RMAppAttemptState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.CapturingMatcher;
import org.mockito.internal.matchers.GreaterOrEqual;

/**
 * Test resource usage tracking in {@link AppSchedulingInfo}
 */
public class TestResourceUsage {

  private final static int MEMORY = 5000000;
  private final static int CORES = 300;
  
  private AppSchedulingInfo info;
  private SchedulerNode node;
  private ResourceRequest request;
  private Container container;
  
  @Before
  public void before() {
    ApplicationId appId = ApplicationId.newInstance(System.currentTimeMillis(), 0);
    ApplicationAttemptId attemptId = ApplicationAttemptId.newInstance(appId , 0);
    
    ActiveUsersManager activeUsersManager = mock(ActiveUsersManager.class);

    QueueMetrics metrics = mock(QueueMetrics.class);
    Queue queue = mock(Queue.class);
    when(queue.getMetrics()).thenReturn(metrics);

    node = mock(SchedulerNode.class);
    when(node.getRackName()).thenReturn(ResourceRequest.ANY);

    Resource resource = Resource.newInstance(MEMORY, CORES);
    request = ResourceRequest.newInstance(Priority.UNDEFINED, 
        ResourceRequest.ANY, resource, 1);
    
    container = mock(Container.class);
    when(container.getNodeId()).thenReturn(NodeId.newInstance("localhost", 666));
    when(container.getResource()).thenReturn(resource);
    
    info = new AppSchedulingInfo(attemptId, "bob", queue, activeUsersManager);
  }
  
  @Test(timeout=2000)
  public void testUsageReporting() throws Exception {
    testUsageReportingInternal(new Runnable() {

      @Override
      public void run() {
        info.release(container);
      }
      
    });
  }
  
  @Test(timeout=2000)
  public void testUsageLeaksPrevention() throws Exception {
    testUsageReportingInternal(new Runnable() {

      @Override
      public void run() {
        info.stop(RMAppAttemptState.FAILED);
      }
      
    });
  }
  
  private void testUsageReportingInternal(Runnable stopRunner) throws Exception {
    final long sleepMs = 200;
    
    info.updateResourceRequests(Collections.singletonList(request));
    info.allocate(NodeType.NODE_LOCAL, node, Priority.UNDEFINED, request, container);
    
    Thread.sleep(sleepMs);
    
    // verify that an allocated container add up usage
    SchedulerAppReport report = mock(SchedulerAppReport.class);
    info.fillUsageStats(report);
    verify(report).setMemorySeconds(longThat(new GreaterOrEqual<Long>(
        (long)(sleepMs / 1000.0 * MEMORY))));
    verify(report).setVcoreSeconds(longThat(new GreaterOrEqual<Long>(
        (long)(sleepMs / 1000.0 * CORES))));

    stopRunner.run();

    // capture current metrics
    CapturingMatcher<Long> memoryMatcher = new CapturingMatcher<Long>();
    CapturingMatcher<Long> vcoresMatcher = new CapturingMatcher<Long>();
    report = mock(SchedulerAppReport.class);
    info.fillUsageStats(report);
    verify(report).setMemorySeconds(longThat(memoryMatcher));
    verify(report).setVcoreSeconds(longThat(vcoresMatcher));

    Thread.sleep(sleepMs);
    
    // check that no tracking is performed
    report = mock(SchedulerAppReport.class);
    info.fillUsageStats(report);
    verify(report).setMemorySeconds(eq(memoryMatcher.getLastValue()));
    verify(report).setVcoreSeconds(eq(vcoresMatcher.getLastValue()));
  }

}
