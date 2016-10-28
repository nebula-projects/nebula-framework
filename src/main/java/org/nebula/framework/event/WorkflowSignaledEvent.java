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

import org.nebula.framework.model.Input;
import org.nebula.framework.model.MethodProfile;

import java.util.List;

public class WorkflowSignaledEvent extends Event {

  private Input input;
  private List<String> realms;

  private MethodProfile signalProfile;

  public WorkflowSignaledEvent() {
    this(0, 0);
  }

  public WorkflowSignaledEvent(int eventId, int precedingId) {
    super(EVENT_TYPE.WorkflowSignaledEvent, eventId, precedingId);
  }

  public Input getInput() {
    return input;
  }

  public void setInput(Input input) {
    this.input = input;
  }

  public List<String> getRealms() {
    return realms;
  }

  public void setRealms(List<String> realms) {
    this.realms = realms;
  }

  public MethodProfile getSignalProfile() {
    return signalProfile;
  }

  public void setSignalProfile(MethodProfile signalProfile) {
    this.signalProfile = signalProfile;
  }
}
