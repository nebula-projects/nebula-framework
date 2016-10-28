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

package org.nebula.framework.utils;

import org.nebula.framework.client.request.CancelTimerRequest;
import org.nebula.framework.client.request.CancelWorkflowRequest;
import org.nebula.framework.client.request.CompleteActivityRequest;
import org.nebula.framework.client.request.CompleteDecisionRequest;
import org.nebula.framework.client.request.CompleteWorkflowRequest;
import org.nebula.framework.client.request.GetEventsRequest;
import org.nebula.framework.client.request.HeartbeatRequest;
import org.nebula.framework.client.request.PollActivityRequest;
import org.nebula.framework.client.request.PollWorkflowRequest;
import org.nebula.framework.client.request.RegisterRequest;
import org.nebula.framework.client.request.ScheduleTimerRequest;
import org.nebula.framework.client.request.SignalWorkflowRequest;
import org.nebula.framework.client.request.StartActivityRequest;
import org.nebula.framework.client.request.StartWorkflowRequest;
import org.nebula.framework.client.response.CancelTimerResponse;
import org.nebula.framework.client.response.CancelWorkflowResponse;
import org.nebula.framework.client.response.CompleteActivityResponse;
import org.nebula.framework.client.response.CompleteDecisionResponse;
import org.nebula.framework.client.response.CompleteWorkflowResponse;
import org.nebula.framework.client.response.GetEventsResponse;
import org.nebula.framework.client.response.HeartbeatResponse;
import org.nebula.framework.client.response.PollActivityResponse;
import org.nebula.framework.client.response.PollWorkflowResponse;
import org.nebula.framework.client.response.RegisterResponse;
import org.nebula.framework.client.response.ScheduleTimerResponse;
import org.nebula.framework.client.response.SignalWorkflowResponse;
import org.nebula.framework.client.response.StartActivityResponse;
import org.nebula.framework.client.response.StartWorkflowResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestMapper {

  private Map<Class, String> requestUriMap = new ConcurrentHashMap<Class, String>();

  private Map<Class, Class> requestResponseMap = new ConcurrentHashMap<Class, Class>();

  private String contextPath = "";

  public RequestMapper(String contextPath) {
    this.contextPath = contextPath;

    requestUriMap.put(StartActivityRequest.class, contextPath + "/activity/start");
    requestUriMap.put(PollActivityRequest.class, contextPath + "/activity/poll");
    requestUriMap.put(CompleteActivityRequest.class,
                      contextPath + "/activity/complete");
    requestUriMap.put(StartWorkflowRequest.class, contextPath + "/workflow/start");
    requestUriMap.put(PollWorkflowRequest.class, contextPath + "/workflow/poll");
    requestUriMap.put(GetEventsRequest.class,
                      contextPath + "/workflow/getEvents");
    requestUriMap.put(CompleteWorkflowRequest.class,
                      contextPath + "/workflow/complete");
    requestUriMap.put(SignalWorkflowRequest.class,
                      contextPath + "/workflow/signal");
    requestUriMap.put(ScheduleTimerRequest.class,
                      contextPath + "/timer/schedule");
    requestUriMap.put(CancelTimerRequest.class,
                      contextPath + "/timer/cancel");
    requestUriMap.put(RegisterRequest.class,
                      contextPath + "/register");
    requestUriMap.put(HeartbeatRequest.class,
                      contextPath + "/heartbeat");
    requestUriMap.put(CompleteDecisionRequest.class, contextPath + "/decision/complete");
    requestUriMap.put(CancelWorkflowRequest.class, contextPath + "/workflow/cancel");

    requestResponseMap.put(StartActivityRequest.class,
                           StartActivityResponse.class);
    requestResponseMap.put(PollActivityRequest.class,
                           PollActivityResponse.class);
    requestResponseMap.put(CompleteActivityRequest.class,
                           CompleteActivityResponse.class);
    requestResponseMap.put(StartWorkflowRequest.class,
                           StartWorkflowResponse.class);
    requestResponseMap.put(PollWorkflowRequest.class,
                           PollWorkflowResponse.class);
    requestResponseMap.put(GetEventsRequest.class,
                           GetEventsResponse.class);
    requestResponseMap.put(CompleteWorkflowRequest.class,
                           CompleteWorkflowResponse.class);
    requestResponseMap.put(SignalWorkflowRequest.class,
                           SignalWorkflowResponse.class);
    requestResponseMap.put(ScheduleTimerRequest.class,
                           ScheduleTimerResponse.class);
    requestResponseMap.put(CancelTimerRequest.class,
                           CancelTimerResponse.class);
    requestResponseMap.put(RegisterRequest.class,
                           RegisterResponse.class);
    requestResponseMap.put(HeartbeatRequest.class,
                           HeartbeatResponse.class);
    requestResponseMap.put(CompleteDecisionRequest.class,
                           CompleteDecisionResponse.class);
    requestResponseMap.put(CancelWorkflowRequest.class,
                           CancelWorkflowResponse.class);
  }

  public void addRequestResponseUri(Class requestClass, Class responseClass, String uri) {
    requestUriMap.put(requestClass, contextPath + uri);
    requestResponseMap.put(requestClass, responseClass);
  }

  public String getUri(Class requestClazz) {
    return requestUriMap.get(requestClazz);
  }

  public Class getResponse(Class requestClazz) {
    return requestResponseMap.get(requestClazz);
  }
}
