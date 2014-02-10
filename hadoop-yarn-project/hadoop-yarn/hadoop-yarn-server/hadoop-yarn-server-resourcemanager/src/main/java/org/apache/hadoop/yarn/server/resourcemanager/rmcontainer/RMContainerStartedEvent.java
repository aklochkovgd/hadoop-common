package org.apache.hadoop.yarn.server.resourcemanager.rmcontainer;

import org.apache.hadoop.yarn.api.records.ContainerId;

public class RMContainerStartedEvent extends RMContainerEvent {

  public RMContainerStartedEvent(ContainerId containerId, long startTime) {
    super(containerId, RMContainerEventType.START, startTime);
  }

}
