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
import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.request.GetEventsRequest;
import org.nebula.framework.client.request.StartWorkflowRequest.StartMode;
import org.nebula.framework.client.response.GetEventsResponse;
import org.nebula.framework.core.Cache;
import org.nebula.framework.core.CacheLoader;
import org.nebula.framework.core.Configuration;
import org.nebula.framework.event.ActivityCompletedEvent;
import org.nebula.framework.event.Event;
import org.nebula.framework.event.TimerCancelledEvent;
import org.nebula.framework.event.TimerCompletedEvent;
import org.nebula.framework.event.WorkflowScheduledEvent;

import java.util.ArrayList;
import java.util.List;

import static org.nebula.framework.event.Event.EVENT_TYPE;

public class WorkflowEventRecords {

  private final static Log log = LogFactory.getLog(WorkflowEventRecords.class);

  private Cache<Event> cache;

  private String instanceId;

  private Cache<Event>.CacheIterator eventIterator;

  private SignalEventListener signalEventListener;

  private Event currentEvent;

  public WorkflowEventRecords(final NebulaClient nebulaClient,
                              final String instanceId, final Configuration configuration) {
    this(nebulaClient, instanceId, configuration.getEventsPageSize(),
         configuration.getMaxEventsCache());
  }

  public WorkflowEventRecords(final NebulaClient nebulaClient, final String instanceId,
                              final int pageSize, final int maxCacheSize) {

    checkRange(pageSize, maxCacheSize);

    this.instanceId = instanceId;

    this.cache = new Cache<Event>(new CacheLoader<Event>() {

      private int totalPages = 1;
      private int pageNo = 0;

      public List<Event> load() {

        try {

          if (pageNo < totalPages) {

            GetEventsResponse response = getEvents();

            pageNo = response.getPageNo();
            totalPages = totalPages(response.getTotal());

            log.debug("pageNo=" + pageNo + ", totalPages=" + totalPages);

            for (Event e : response.getEvents()) {
              log.debug("event.id=" + e.getEventId() + ",type=" + e.getEventType());
            }

            return response.getEvents();
          }
        } catch (Exception e) {
          log.error("failed to pull history events", e);
        }

        return new ArrayList<Event>();
      }

      private int totalPages(int total) {
        return (int) Math.ceil(1.0 * total / pageSize);
      }

      private GetEventsResponse getEvents() throws Exception {
        GetEventsRequest request = new GetEventsRequest();
        request.setInstanceId(instanceId);
        request.setPageSize(pageSize);
        request.setPageNo(pageNo + 1);

        return nebulaClient.get(request);

      }
    }, maxCacheSize);

    eventIterator = cache.iterator();
  }

  private void checkRange(int pageSize, int maxCacheSize) {

    if (pageSize <= 0 || maxCacheSize <= 0) {
      throw new IllegalArgumentException("The pageSize/maxCacheSize should be greater than 0.");
    }

    if (pageSize > maxCacheSize) {
      throw new IllegalArgumentException("The pageSize " + pageSize
                                         + " should be smaller than the maxCacheSize.");
    }
  }


  public ScheduleTimerReplayer scheduleTimerReplayer() {

    return new ScheduleTimerReplayer();
  }

  public CancelTimerReplayer cancelTimerReplayer() {

    return new CancelTimerReplayer();
  }

  public ActivityReplayer activityReplayer() {

    return new ActivityReplayer();
  }

  public WorkflowReplayer workflowReplayer() {
    return new WorkflowReplayer();
  }

  public void setSignalEventListener(SignalEventListener signalEventListener) {
    this.signalEventListener = signalEventListener;
  }

  public String getInstanceId() {
    return instanceId;
  }

  private ActivityCompletedEvent findActivityCompletedEventWithPrecedingEvent(
      int eventId) {
    return findEventWithPrecedingId(eventId,
                                    EVENT_TYPE.ActivityCompletedEvent);
  }

  private TimerCompletedEvent findTimerCompletedEventWithPrecedingEvent(int eventId) {

    return findEventWithPrecedingId(eventId, EVENT_TYPE.TimerCompletedEvent);
  }

  private TimerCancelledEvent findTimerCancelledEventWithPrecedingEvent(int eventId) {

    return findEventWithPrecedingId(eventId, EVENT_TYPE.TimerCancelledEvent);
  }

  private <E> E findEventWithPrecedingId(int eventId, EVENT_TYPE eventType) {

    for (Event event : cache) {
      if (event.getPrecedingId() == eventId && event.getEventType() == eventType) {
        return (E) event;
      }
    }

    return null;
  }

  public Event currentEvent() {
    return currentEvent;
  }

  public SignalReplayer signalRelayer() {
    return new SignalReplayer();
  }

  public boolean hasNext() {
    return eventIterator.hasNext();
  }

  public Event next() {
    return eventIterator.next();
  }

  private Event current() {
    return eventIterator.current();
  }

  public abstract class Replayer {

    protected void replay() {

      while (hasNext()) {

        currentEvent = current();

        if (interestedEvent()) {
          replayInternal();
          next();
          return;
        } else if (otherInterestedEvent()) {
          //The current replayer won't/can't replay this event, so stop the replaying.
          return;
        } else if (isSignalEvent()) {
          //If the signal event replaying results in other operations such as timer cancellation,
          //which will be tried to replay in CancelTimerReplayer. The cursor should be moved to next,
          //otherwise the WorkflowSignaledEvent would be replayed again and result in endless loop.
          next();

          replaySignal();
        } else {
          //Other events such as ActivityCompletedEvent will be skipped
          //since they replayed in corresponding replayers.
          next();
        }

      }
    }

    private boolean otherInterestedEvent() {
      return currentEvent.isEventType(
          EVENT_TYPE.ActivityScheduledEvent) || currentEvent
                 .isEventType(EVENT_TYPE.TimerScheduledEvent) || currentEvent
                 .isEventType(EVENT_TYPE.TimerCancelledEvent) || currentEvent
                 .isEventType(EVENT_TYPE.WorkflowScheduledEvent);
    }

    private boolean isSignalEvent() {
      return currentEvent.isEventType(EVENT_TYPE.WorkflowSignaledEvent);
    }

    private void replaySignal() {
      if (signalEventListener != null) {
        signalEventListener.signalEvent(currentEvent);
      }
    }

    protected abstract boolean interestedEvent();

    protected abstract void replayInternal();

  }

  public class SignalReplayer extends Replayer {

    protected boolean interestedEvent() {
      return false;
    }

    protected void replayInternal() {

    }
  }

  public class ScheduleTimerReplayer extends Replayer {

    private boolean isTimerScheduledReplayed;

    private boolean isTimerCompletedOrCancelledReplayed;

    private Event timerScheduledEvent;

    protected boolean interestedEvent() {

      isTimerScheduledReplayed = currentEvent.isEventType(EVENT_TYPE.TimerScheduledEvent);

      return isTimerScheduledReplayed;
    }

    protected void replayInternal() {
      timerScheduledEvent = currentEvent;

      //TODO profile.equals(event.getProfile) to make sure that the method is invoked correctly.

      TimerCompletedEvent completedEvent =
          findTimerCompletedEventWithPrecedingEvent(timerScheduledEvent
                                                        .getEventId());

      TimerCancelledEvent cancelledEvent =
          findTimerCancelledEventWithPrecedingEvent(timerScheduledEvent
                                                        .getEventId());
      isTimerCompletedOrCancelledReplayed = completedEvent != null || cancelledEvent != null;
    }

    public boolean isTimerScheduledReplayed() {
      return isTimerScheduledReplayed;
    }

    public boolean isTimerCompletedOrCancelledReplayed() {

      return isTimerCompletedOrCancelledReplayed;
    }

    public Event getTimerScheduledEvent() {
      return timerScheduledEvent;
    }

  }

  public class CancelTimerReplayer extends Replayer {

    private boolean isTimerCancelledReplayed;

    protected boolean interestedEvent() {

      isTimerCancelledReplayed = currentEvent.isEventType(EVENT_TYPE.TimerCancelledEvent);

      return isTimerCancelledReplayed;
    }

    protected void replayInternal() {

    }

    public boolean isTimerCancelledReplayed() {
      return isTimerCancelledReplayed;
    }

  }

  public class ActivityReplayer extends Replayer {

    private boolean isActivityScheduledReplayed;

    private boolean isActivityCompletedReplayed;

    private ActivityCompletedEvent activityCompletedEvent;

    protected boolean interestedEvent() {

      isActivityScheduledReplayed = currentEvent.isEventType(EVENT_TYPE.ActivityScheduledEvent);

      return isActivityScheduledReplayed;
    }

    protected void replayInternal() {
      activityCompletedEvent =
          findActivityCompletedEventWithPrecedingEvent(currentEvent
                                                           .getEventId());

      isActivityCompletedReplayed = activityCompletedEvent != null;
    }

    public boolean isActivityScheduledReplayed() {
      return isActivityScheduledReplayed;
    }

    public boolean isActivityCompletedReplayed() {
      return isActivityCompletedReplayed;
    }

    public ActivityCompletedEvent getActivityCompletedEvent() {
      return activityCompletedEvent;
    }

  }

  public class WorkflowReplayer extends Replayer {

    private boolean isActivityScheduledReplayed;

    private WorkflowScheduledEvent workflowScheduledEvent;

    protected boolean interestedEvent() {

      isActivityScheduledReplayed = currentEvent.isEventType(EVENT_TYPE.WorkflowScheduledEvent);

      return isActivityScheduledReplayed;
    }

    protected void replayInternal() {
      workflowScheduledEvent = (WorkflowScheduledEvent) currentEvent;
    }

    public boolean isWorkflowCancelled() {
      return hasEvent(EVENT_TYPE.WorkflowCancelledEvent);
    }

    public boolean isWorkflowCompleted() {
      return hasEvent(EVENT_TYPE.WorkflowCompletedEvent);
    }

    public StartMode getStartMode() {
      return workflowScheduledEvent.getStartMode();
    }

    private boolean hasEvent(EVENT_TYPE eventType) {
      for (Event event : cache) {
        if (event.getEventType() == eventType) {
          return true;
        }
      }

      return false;
    }

    public boolean isWorkflowScheduledReplayed() {
      return isActivityScheduledReplayed;
    }

    public WorkflowScheduledEvent getWorkflowScheduledEvent() {
      return workflowScheduledEvent;
    }

  }


}