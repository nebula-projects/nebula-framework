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

package org.nebula.framework.model;

import static org.nebula.framework.utils.Validate.notEmpty;

public class ActivityProfile {

  private String activity;

  private String version;

  public String getActivity() {
    return activity;
  }

  public void setActivity(String activity) {
    this.activity = activity;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof ActivityProfile)) {
      return false;
    }

    ActivityProfile profile = (ActivityProfile) o;

    if (this.activity == null || profile.activity == null || this.version == null
        || profile.version == null) {
      return false;
    }

    return this.activity.equals(profile.activity) && this.version.equals(profile.version);
  }

  @Override
  public int hashCode() {
    return (17 * 37 + activity.hashCode()) * 37 + version.hashCode();
  }

  public void validate(){
    notEmpty(getActivity(), "The activity name should not be empty");
    notEmpty(getVersion(), "The activity version should not be empty");
  }

}
