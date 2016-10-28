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

import org.nebula.framework.model.ActivityProfile;

public class ActivityStartedEvent extends Event {

  private ActivityProfile activityProfile;

  public ActivityStartedEvent() {
    this(0, 0);
  }

  public ActivityStartedEvent(int eventId, int precedingId) {
    super(EVENT_TYPE.ActivityStartedEvent, eventId, precedingId);
  }

  public ActivityProfile getActivityProfile() {
    return activityProfile;
  }

  public void setActivityProfile(ActivityProfile activityProfile) {
    this.activityProfile = activityProfile;
  }

}
