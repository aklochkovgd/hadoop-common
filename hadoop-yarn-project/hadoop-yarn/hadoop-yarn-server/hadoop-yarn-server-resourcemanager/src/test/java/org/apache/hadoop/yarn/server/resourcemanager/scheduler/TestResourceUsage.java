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
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceRequest;
import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.event.EventHandler;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.CapturingMatcher;
import org.mockito.internal.matchers.GreaterOrEqual;

/**
 * Test resource usage tracking in {@link SchedulerApplication}
 */
public class TestResourceUsage {

  private static final int MEMORY = 5000000;
  private static final int CORES = 300;
  
  private SchedulerApplication app;
  private RMContainer rmContainer;
  private Container container;
  private SchedulerNode node;
  private ResourceRequest resourceRequest;
  
  @Before
  public void before() {
    ApplicationId appId = ApplicationId.newInstance(System.currentTimeMillis(), 0);
    ApplicationAttemptId attemptId = ApplicationAttemptId.newInstance(appId , 0);
    ContainerId containerId = ContainerId.newInstance(attemptId, 5);
    NodeId nodeId = NodeId.newInstance("localhost", 666);
    ActiveUsersManager activeUsersManager = mock(ActiveUsersManager.class);
    Resource resource = Resource.newInstance(MEMORY, CORES);

    QueueMetrics metrics = mock(QueueMetrics.class);
    Queue queue = mock(Queue.class);
    when(queue.getMetrics()).thenReturn(metrics);

    container = Container.newInstance(containerId, nodeId, null, resource,
        Priority.UNDEFINED, null);
    node = mock(SchedulerNode.class);
    when(node.getRackName()).thenReturn(ResourceRequest.ANY);
    
    rmContainer = mock(RMContainer.class);
    when(rmContainer.getApplicationAttemptId()).thenReturn(attemptId);
    when(rmContainer.getContainer()).thenReturn(container);
    when(rmContainer.getContainerId()).thenReturn(containerId);
    
    resourceRequest = ResourceRequest.newInstance(Priority.UNDEFINED,
        ResourceRequest.ANY, resource, 1);
    
    RMContext rmContext = mock(RMContext.class);
    Dispatcher dispatcher = mock(Dispatcher.class);
    EventHandler<?> eventHandler = mock(EventHandler.class);
    when(dispatcher.getEventHandler()).thenReturn(eventHandler );
    when(rmContext.getDispatcher()).thenReturn(dispatcher);
    
    app = new SchedulerApplication(attemptId, "bob", queue, activeUsersManager,
        rmContext) {
    };
    app.updateResourceRequests(Collections.singletonList(resourceRequest));
  }
  
  @Test(timeout=2000)
  public void testUsageReporting() throws Exception {
    final long sleepMs = 200;
    
    long startTime = System.currentTimeMillis();
    when(rmContainer.getStartTime()).thenReturn(startTime);
    app.allocate(NodeType.NODE_LOCAL, node, Priority.UNDEFINED,
        resourceRequest, container);
    
    Thread.sleep(sleepMs);
    
    // verify that an allocated container add up usage
    SchedulerAppReport report = mock(SchedulerAppReport.class);
    app.fillUsageStats(report);
    verify(report).setMemorySeconds(longThat(new GreaterOrEqual<Long>(
        (long)(sleepMs / 1000.0 * MEMORY))));
    verify(report).setVcoreSeconds(longThat(new GreaterOrEqual<Long>(
        (long)(sleepMs / 1000.0 * CORES))));

    app.containerCompleted(rmContainer, null, null);

    // capture current metrics
    CapturingMatcher<Long> memoryMatcher = new CapturingMatcher<Long>();
    CapturingMatcher<Long> vcoresMatcher = new CapturingMatcher<Long>();
    report = mock(SchedulerAppReport.class);
    app.fillUsageStats(report);
    verify(report).setMemorySeconds(longThat(memoryMatcher));
    verify(report).setVcoreSeconds(longThat(vcoresMatcher));

    Thread.sleep(sleepMs);
    
    // check that no tracking is performed
    report = mock(SchedulerAppReport.class);
    app.fillUsageStats(report);
    verify(report).setMemorySeconds(eq(memoryMatcher.getLastValue()));
    verify(report).setVcoreSeconds(eq(vcoresMatcher.getLastValue()));
  }

}
