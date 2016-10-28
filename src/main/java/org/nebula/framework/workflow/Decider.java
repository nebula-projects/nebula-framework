/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nebula.framework.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebula.framework.core.Promise;
import org.nebula.framework.event.Event;
import org.nebula.framework.event.WorkflowScheduledEvent;
import org.nebula.framework.model.ActivityProfile;
import org.nebula.framework.model.MethodProfile;

/**
 * This class is thread-unsafe. Only one thread is allowed to execute the method one time.
 */
public class Decider implements SignalEventListener {

  private final static Log log = LogFactory.getLog(Decider.class);

  public int unCompletedMethods = 0;

  private WorkflowEventRecords records;
  private EventEmitter eventEmitter;
  private WorkflowInstance workflowInstance;

  private WorkflowEventRecords.WorkflowReplayer workflowReplayer;
  private WorkflowEventRecords.ActivityReplayer activityReplayer;
  private WorkflowEventRecords.ScheduleTimerReplayer scheduleTimerReplayer;
  private WorkflowEventRecords.CancelTimerReplayer cancelTimerReplayer;
  private WorkflowEventRecords.SignalReplayer signalReplayer;

  public Decider(WorkflowEventRecords records, EventEmitter eventEmitter,
                 WorkflowInstance workflowInstance) {
    this.records = records;
    this.eventEmitter = eventEmitter;
    this.workflowInstance = workflowInstance;

    workflowReplayer = records.workflowReplayer();
    activityReplayer = records.activityReplayer();
    scheduleTimerReplayer = records.scheduleTimerReplayer();
    cancelTimerReplayer = records.cancelTimerReplayer();
    signalReplayer = records.signalRelayer();
  }

  private static boolean isReady(Promise[] inputs, Promise... waitfor) {
    if (waitfor != null) {
      for (Promise wait : waitfor) {
        if (!wait.isReady()) {
          return false;
        }
      }
    }

    for (Promise input : inputs) {
      if (!input.isReady()) {
        return false;
      }
    }
    return true;
  }

  public void decide() {

    workflowReplayer.replay();

    if (!workflowReplayer.isWorkflowScheduledReplayed()) {
      throw new RuntimeException("The first event isn't WorkflowScheduledEvent.");
    }

    //don't execute the workflow if the workflow is cancelled or suspended.
    if (!workflowReplayer.isWorkflowCancelled()) {

      WorkflowScheduledEvent workflowScheduledEvent = workflowReplayer.getWorkflowScheduledEvent();

      workflowInstance.startWorkflow(workflowScheduledEvent);

      signalReplayer.replay();
    }

    if (!workflowCompleted() && isStartMethodCompleted()) {
      eventEmitter.completeWorkflow(workflowReplayer.getStartMode(),
                                    records.currentEvent().getEventId());
    } else {
      eventEmitter.completeDecision();
    }

  }

  public Integer decideStartTimer(int secs, Promise<Void> result) {

    unCompletedMethods++;

    scheduleTimerReplayer.replay();

    if (scheduleTimerReplayer.isTimerScheduledReplayed()) {

      if (scheduleTimerReplayer.isTimerCompletedOrCancelledReplayed()) {
        unCompletedMethods--;
        result.set(null);
      }

      return scheduleTimerReplayer.getTimerScheduledEvent().getEventId();

    }

    return eventEmitter.startTimer(secs, records.currentEvent().getEventId());

  }

  public void decideCancelTimer(Integer timerId) {

    unCompletedMethods++;

    cancelTimerReplayer.replay();

    if (cancelTimerReplayer.isTimerCancelledReplayed()) {
      unCompletedMethods--;
      return;
    }

    eventEmitter.cancelTimer(timerId);
  }

  public Promise decideActivity(ActivityProfile activityProfile, MethodProfile methodProfile,
                                Promise[] inputs,
                                Promise... waitfor) {
    unCompletedMethods++;

    activityReplayer.replay();

    if (activityReplayer.isActivityScheduledReplayed()) {

      if (activityReplayer.isActivityCompletedReplayed()) {
        unCompletedMethods--;
        return workflowInstance
            .handleCompletedActivity(activityReplayer.getActivityCompletedEvent());
      } else {
        return new Promise();
      }
    }

    //Determine the activity is ready for run after the activity replay
    if (!isReady(inputs, waitfor)) {
      return new Promise();
    }

    return eventEmitter
        .startActivity(activityProfile, methodProfile, inputs, records.currentEvent().getEventId());
  }

  public boolean isStartMethodCompleted() {
    return unCompletedMethods == 0;
  }

  public boolean workflowCompleted() {
    return workflowReplayer.isWorkflowCompleted();
  }

  public void signalEvent(Event event) {
    workflowInstance.signalEvent(event);
  }

}
