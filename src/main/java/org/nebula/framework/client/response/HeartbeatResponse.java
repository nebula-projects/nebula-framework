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

package org.nebula.framework.client.response;

import org.nebula.framework.client.Response;

public class HeartbeatResponse implements Response {

  private String registrationId;

  private int intervalSecs;
  private Status oldStatus;
  private Status newStatus;
  private String message;
  private int oldPollThreads;
  private int newPollThreads;
  private int oldMaxExecutionThreads;
  private int newMaxExecutionThreads;

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
  }

  public int getIntervalSecs() {
    return intervalSecs;
  }

  public void setIntervalSecs(int intervalSecs) {
    this.intervalSecs = intervalSecs;
  }

  public Status getOldStatus() {
    return oldStatus;
  }

  public void setOldStatus(Status oldStatus) {
    this.oldStatus = oldStatus;
  }

  public Status getNewStatus() {
    return newStatus;
  }

  public void setNewStatus(Status newStatus) {
    this.newStatus = newStatus;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getOldPollThreads() {
    return oldPollThreads;
  }

  public void setOldPollThreads(int oldPollThreads) {
    this.oldPollThreads = oldPollThreads;
  }

  public int getNewPollThreads() {
    return newPollThreads;
  }

  public void setNewPollThreads(int newPollThreads) {
    this.newPollThreads = newPollThreads;
  }

  public int getOldMaxExecutionThreads() {
    return oldMaxExecutionThreads;
  }

  public void setOldMaxExecutionThreads(int oldMaxExecutionThreads) {
    this.oldMaxExecutionThreads = oldMaxExecutionThreads;
  }

  public int getNewMaxExecutionThreads() {
    return newMaxExecutionThreads;
  }

  public void setNewMaxExecutionThreads(int newMaxExecutionThreads) {
    this.newMaxExecutionThreads = newMaxExecutionThreads;
  }

  public static enum Status {RUNNING, BLOCKED, OFFLINE}

}
