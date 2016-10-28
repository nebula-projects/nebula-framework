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

public class WorkflowProfile {

  private String name;
  private String version;

  private boolean serial;
  private String cronExpression;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public boolean isSerial() {
    return serial;
  }

  public void setSerial(boolean serial) {
    this.serial = serial;
  }

  public String getCronExpression() {
    return cronExpression;
  }

  public void setCronExpression(String cronExpression) {
    this.cronExpression = cronExpression;
  }

  @Override
  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }

    if (object == this) {
      return true;
    }

    if (getClass() != object.getClass()) {
      return false;
    }

    WorkflowProfile workflowProfile = (WorkflowProfile) object;

    return name.equals(workflowProfile.name) && version.equals(workflowProfile.version);
  }

  @Override
  public int hashCode() {
    return (17 * 37 + name.hashCode()) * 37 + version.hashCode();
  }
}