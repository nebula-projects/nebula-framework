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

package org.nebula.framework.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class Event {

  private EVENT_TYPE eventType;

  ;
  private int eventId;
  private int precedingId;
  private long timestamp;
  private String instanceId;
  private String registrationId;

  public Event() {
  }

  public Event(EVENT_TYPE eventType, int eventId, int precedingId) {
    this.eventType = eventType;
    this.eventId = eventId;
    this.precedingId = precedingId;
    this.timestamp = System.currentTimeMillis();
  }

  public EVENT_TYPE getEventType() {
    return eventType;
  }

  public void setEventType(EVENT_TYPE eventType) {
    this.eventType = eventType;
  }

  public int getEventId() {
    return eventId;
  }

  public void setEventId(int eventId) {
    this.eventId = eventId;
  }

  public int getPrecedingId() {
    return precedingId;
  }

  public void setPrecedingId(int precedingId) {
    this.precedingId = precedingId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
  }

  public boolean isEventType(EVENT_TYPE eventType) {
    return this.eventType == eventType;
  }

  public enum EVENT_TYPE {
    ActivityScheduledEvent, ActivityStartedEvent, ActivityCompletedEvent,
    TimerScheduledEvent, TimerCancelledEvent, TimerCompletedEvent,
    WorkflowScheduledEvent, WorkflowSignaledEvent, WorkflowStartedEvent,
    WorkflowCompletedEvent, WorkflowCancelledEvent
  }
}
