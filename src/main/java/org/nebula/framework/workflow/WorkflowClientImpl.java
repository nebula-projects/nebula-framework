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

import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.request.CancelWorkflowRequest;
import org.nebula.framework.client.request.SignalWorkflowRequest;
import org.nebula.framework.client.request.StartWorkflowRequest;
import org.nebula.framework.client.response.StartWorkflowResponse;
import org.nebula.framework.core.NebulaException;
import org.nebula.framework.core.Promise;
import org.nebula.framework.model.Input;
import org.nebula.framework.model.MethodProfile;
import org.nebula.framework.model.WorkflowProfile;
import org.nebula.framework.utils.Validate;

import java.util.Arrays;
import java.util.List;

public class WorkflowClientImpl implements WorkflowClient {

  private NebulaClient nebulaClient;

  private WorkflowProfile workflowVersion;

  private List<String> realms;

  private ThreadLocal<String> instanceId = new ThreadLocal<String>();

  public WorkflowClientImpl(NebulaClient nebulaClient,
                            WorkflowProfile workflowVersion, List<String> realms) {

    validate(nebulaClient, workflowVersion, realms);

    this.nebulaClient = nebulaClient;
    this.workflowVersion = workflowVersion;
    this.realms = realms;
  }

  protected <T> Promise<T> startWorkflow(String methodName, String[] parameterTypes, Object[] inputs) {

    validate(methodName, parameterTypes, inputs);

    StartWorkflowRequest request = new StartWorkflowRequest();
    request.setRealms(realms);
    request.setWorkflowProfile(workflowVersion);
    request.setUser(nebulaClient.getUser());

    MethodProfile profile = getMethodProfile(methodName, parameterTypes);
    request.setStartProfile(profile);

    Input input = getInput(inputs);
    request.setInput(input);

    try {
      StartWorkflowResponse response = nebulaClient.post(request);
      instanceId.set(response.getInstanceId());
    } catch (Exception e) {
      throw new NebulaException("Failed to startWorkflow", e);
    }
    return new Promise();
  }

  protected void signalWorkflow(String methodName, String[] parameterTypes,
                             Object[] inputs) {

    validate(methodName, parameterTypes, inputs);

    SignalWorkflowRequest request = new SignalWorkflowRequest();
    request.setRealms(realms);
    request.setInstanceId(instanceId.get());

    Input input = getInput(inputs);
    request.setInput(input);

    MethodProfile profile = getMethodProfile(methodName, parameterTypes);
    request.setSignalProfile(profile);

    try {
      nebulaClient.post(request);
    } catch (Exception e) {
      throw new NebulaException("Failed to signalWorkflow", e);
    }
  }

  @Override
  public void cancelWorkflow() {
    cancelWorkflow(instanceId.get(), realms);
  }

  @Override
  public void cancelWorkflow(String instanceId, List<String> realms) {

    CancelWorkflowRequest request = new CancelWorkflowRequest();

    request.setInstanceId(instanceId);
    request.setRealms(realms);

    try {
      nebulaClient.post(request);
    } catch (Exception e) {
      throw new NebulaException("Failed to cancelWorkflow", e);
    }
  }

  @Override
  public String getInstanceId() {
    return instanceId.get();
  }

  private void validate(NebulaClient nebualClient,
                        WorkflowProfile workflowVersion, List<String> realms) {
    Validate.notNull(nebualClient);
    Validate.notEmpty(nebualClient.getUser());

    Validate.notNull(workflowVersion);
    Validate.notEmpty(workflowVersion.getName());
    Validate.notEmpty(workflowVersion.getVersion());

    Validate.notEmpty(realms);
  }

  private void validate(String methodName, String[] parameterTypes, Object[] inputs) {
    Validate.notEmpty(methodName);
    Validate.notNull(parameterTypes);
    Validate.notNull(inputs);

    Validate.isTrue(parameterTypes.length == inputs.length);
  }

  private MethodProfile getMethodProfile(String name, String[] parameterTypes) {

    MethodProfile profile = new MethodProfile();
    profile.setName(name);
    profile.setParameterTypes(Arrays.asList(parameterTypes));

    return profile;
  }

  private Input getInput(Object[] inputs) {

    String[] _inputs = new String[inputs.length];

    for (int i = 0; i < inputs.length; i++) {
        _inputs[i] = toJson(inputs[i]);
    }

    Input input = new Input();
    input.setInputs(_inputs);

    return input;
  }
}
