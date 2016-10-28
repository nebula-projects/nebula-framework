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

package org.nebula.framework.core;

import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.request.RegisterRequest;
import org.nebula.framework.model.MethodProfile;
import org.nebula.framework.workflow.ProcessDefinition;

import java.util.ArrayList;
import java.util.List;

public class WorkflowNodeRegistry extends NodeRegistry<ProcessDefinition> {

  public WorkflowNodeRegistry(NebulaClient nebulaClient) {
    super(nebulaClient);
  }

  protected RegisterRequest createRegisterRequest(ProcessDefinition processDefinition,
                                                  List<String> realms) {

    RegisterRequest request = new RegisterRequest();
    request.setUser(nebulaClient.getUser());
    request.setName(processDefinition.getWorkflowProfile().getName());
    request.setVersion(processDefinition.getWorkflowProfile().getVersion());

    request.setNodeType(RegisterRequest.NodeType.WORKFLOW);

    List<MethodProfile> methodProfiles = new ArrayList<MethodProfile>();

    methodProfiles.addAll(processDefinition.getStartMethod().keySet());
    methodProfiles.addAll(processDefinition.getSignalMethods().keySet());

    RegisterRequest.RegistrationInfo registrationInfo = new RegisterRequest.RegistrationInfo();

    registrationInfo.setMethodProfiles(methodProfiles);
    registrationInfo.setStartProfile(processDefinition.getStartMethod().keySet().iterator().next());
    registrationInfo.setRealms(realms);
    registrationInfo.setWorkflowProfile(processDefinition.getWorkflowProfile());

    request.setRegistrationInfo(registrationInfo);

    return request;
  }
}