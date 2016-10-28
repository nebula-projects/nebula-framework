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

import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.Request;
import org.nebula.framework.client.Response;
import org.nebula.framework.client.request.PollWorkflowRequest;
import org.nebula.framework.client.response.PollWorkflowResponse;
import org.nebula.framework.core.Configuration;
import org.nebula.framework.core.EventPoller;
import org.nebula.framework.core.NebulaException;

import java.util.List;

public class WorkflowEventPoller extends EventPoller<ProcessDefinition, PollWorkflowResponse> {

  public WorkflowEventPoller(NebulaClient nebulaClient,
                             ProcessDefinition processDefinition, List<String> realms,
                             Configuration configuration) {
    super(nebulaClient, processDefinition, realms, configuration);
  }

  @Override
  protected Request createPollRequest(ProcessDefinition processDefinition, List<String> realms) {
    PollWorkflowRequest request = new PollWorkflowRequest();
    request.setRealms(realms);
    return request;
  }

  @Override
  protected PollWorkflowResponse processResponse(PollWorkflowResponse response) {
    if (Response.Status.SUCCESS == response.getStatus()) {
      return response;
    }

    throw new NebulaException("The response is not success.");
  }

  @Override
  protected Runnable createNodeHandler(NebulaClient nebulaClient,
                                       ProcessDefinition processDefinition,
                                       Configuration configuration, PollWorkflowResponse response) {
    return new WorkflowHandler(nebulaClient, processDefinition, configuration, response);
  }

}
