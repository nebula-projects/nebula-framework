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

import static org.nebula.framework.utils.JsonUtils.toJson;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.Request;
import org.nebula.framework.client.request.CancelTimerRequest;
import org.nebula.framework.client.request.CompleteDecisionRequest;
import org.nebula.framework.client.request.CompleteWorkflowRequest;
import org.nebula.framework.client.request.ScheduleTimerRequest;
import org.nebula.framework.client.request.StartActivityRequest;
import org.nebula.framework.client.request.StartWorkflowRequest;
import org.nebula.framework.client.response.CancelTimerResponse;
import org.nebula.framework.client.response.ScheduleTimerResponse;
import org.nebula.framework.client.response.StartActivityResponse;
import org.nebula.framework.core.Promise;
import org.nebula.framework.model.ActivityProfile;
import org.nebula.framework.model.Input;
import org.nebula.framework.model.MethodProfile;

import java.util.Arrays;

public class EventEmitter {

  private final static Log log = LogFactory.getLog(EventEmitter.class);

  private int precedingActivityEventId = Integer.MAX_VALUE;

  private NebulaClient nebulaClient;

  private String registrationId;
  private String instanceId;
  private String realm;
  private String realmActId;

  public EventEmitter(NebulaClient nebulaClient, String registrationId, String instanceId,
                      String realm, String realmActId) {
    this.nebulaClient = nebulaClient;
    this.registrationId = registrationId;
    this.instanceId = instanceId;
    this.realm = realm;
    this.realmActId = realmActId;
  }

  public Promise startActivity(ActivityProfile activityProfile, MethodProfile methodProfile,
                               Promise[] inputs, int eventId) {

    log.debug("startActivity for instanceId= " + instanceId + ", realm=" + realm + ", activity="
              + activityProfile.getActivity() + ", method=" + methodProfile.getName());

    StartActivityRequest request = new StartActivityRequest();
    request.setActivityProfile(activityProfile);
    request.setMethodProfile(methodProfile);

    request.setRealms(Arrays.asList(realm));

    String[] _inputs = new String[inputs.length];
    for (int i = 0; i < inputs.length; i++) {
        _inputs[i] = toJson(inputs[i].get());
    }

    Input input = new Input();
    input.setInputs(_inputs);
    request.setInput(input);

    request.setRegistrationId(registrationId);
    request.setInstanceId(instanceId);

    request.setEventId(
        precedingActivityEventId == Integer.MAX_VALUE ? eventId
                                                      : precedingActivityEventId);

    StartActivityResponse response = post(request);
    precedingActivityEventId = response.getEventId();

    log.debug("startActivity eventId=" + precedingActivityEventId);

    return new Promise();
  }

  public int startTimer(int period, int currentEventId) {

    log.debug(
        "startTimer for instanceId= " + instanceId + ", realm=" + realm + ", period=" + period);

    ScheduleTimerRequest request = new ScheduleTimerRequest();
    request.setInstanceId(instanceId);
    request.setRegistrationId(registrationId);
    request.setPeriod(period);
    request.setRealms(Arrays.asList(realm));
    request.setEventId(currentEventId);

    ScheduleTimerResponse response = post(request);
    precedingActivityEventId = response.getEventId();

    log.debug("startTimer eventId=" + precedingActivityEventId);

    return response.getEventId();

  }

  public void cancelTimer(Integer timerId) {

    log.debug(
        "cancelTimer for instanceId= " + instanceId + ", realm=" + realm + ",timerId=" + timerId);

    CancelTimerRequest request = new CancelTimerRequest();
    request.setRegistrationId(registrationId);
    request.setInstanceId(instanceId);
    request.setTimerId(timerId);
    request.setRealms(Arrays.asList(realm));

    CancelTimerResponse response = post(request);
    precedingActivityEventId = response.getEventId();

  }

  public void completeDecision() {

    log.debug(
        "completeDecision for instanceId= " + instanceId + ", realm=" + realm + ", realmActId="
        + realmActId);

    CompleteDecisionRequest request = new CompleteDecisionRequest();

    request.setRegistrationId(registrationId);
    request.setInstanceId(instanceId);
    request.setRealm(realm);
    request.setRealmActId(realmActId);

    post(request);

  }

  public void completeWorkflow(StartWorkflowRequest.StartMode startMode, int eventId) {

    log.debug("completeWorkflow for instanceId= " + instanceId + ", realm=" + realm + ",startMode="
              + startMode);

    CompleteWorkflowRequest request = new CompleteWorkflowRequest();

    request.setRegistrationId(registrationId);
    request.setInstanceId(instanceId);
    request.setEventId(eventId);
    request.setStartMode(startMode);
    request.setRealm(realm);
    request.setRealmActId(realmActId);

    post(request);

  }

  private <T> T post(Request request) throws RuntimeException {
    try {
      return nebulaClient.post(request);
    } catch (Exception e) {
      log.error("Failed to post request " + request, e);
      throw new RuntimeException("can't cancel timer");
    }
  }

}