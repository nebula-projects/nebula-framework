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

package org.nebula.framework.client.request;

import org.nebula.framework.client.AbstractRequest;
import org.nebula.framework.core.Authorization;
import org.nebula.framework.model.Input;
import org.nebula.framework.model.MethodProfile;
import org.nebula.framework.model.WorkflowProfile;

import java.util.List;

public class StartWorkflowRequest extends AbstractRequest {

  private String registrationId;
  private String user;
  private WorkflowProfile workflowProfile;
  private List<String> realms;
  private Input input;
  private MethodProfile startProfile;
  private StartMode startMode = StartMode.NORMAL;

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public WorkflowProfile getWorkflowProfile() {
    return workflowProfile;
  }

  public void setWorkflowProfile(WorkflowProfile workflowProfile) {
    this.workflowProfile = workflowProfile;
  }

  public List<String> getRealms() {
    return realms;
  }

  public void setRealms(List<String> realms) {
    this.realms = realms;
  }

  public Input getInput() {
    return input;
  }

  public void setInput(Input input) {
    this.input = input;
  }

  public MethodProfile getStartProfile() {
    return startProfile;
  }

  public void setStartProfile(MethodProfile startProfile) {
    this.startProfile = startProfile;
  }

  public StartMode getStartMode() {
    return startMode;
  }

  public void setStartMode(StartMode startMode) {
    this.startMode = startMode;
  }

  public enum StartMode {
    NORMAL, TIMER_START_SERIAL, TIMER_START_PARALLEL
  }

  protected String toSignedSignature(Authorization authorization, String secretKey) {
    return authorization.addField("workflowName",workflowProfile.getName()).addField("workflowVersion", workflowProfile.getVersion()).toSignedSignature(secretKey);
  }
}
