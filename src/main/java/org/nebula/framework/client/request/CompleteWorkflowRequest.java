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

public class CompleteWorkflowRequest extends AbstractRequest {

  private String registrationId;

  private String instanceId;
  private int eventId;

  private StartWorkflowRequest.StartMode startMode = StartWorkflowRequest.StartMode.NORMAL;

  private String realm;
  private String realmActId;

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public int getEventId() {
    return eventId;
  }

  public void setEventId(int eventId) {
    this.eventId = eventId;
  }

  public StartWorkflowRequest.StartMode getStartMode() {
    return startMode;
  }

  public void setStartMode(StartWorkflowRequest.StartMode startMode) {
    this.startMode = startMode;
  }

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public String getRealmActId() {
    return realmActId;
  }

  public void setRealmActId(String realmActId) {
    this.realmActId = realmActId;
  }

  protected String toSignedSignature(Authorization authorization, String secretKey) {
    return authorization.setRegistrationId(registrationId).setInstanceId(instanceId).addField(
        "eventId", eventId).toSignedSignature(secretKey);
  }

}
