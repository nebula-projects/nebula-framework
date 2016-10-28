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

import static org.nebula.framework.utils.JsonUtils.constructFromCanonical;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebula.framework.core.Promise;
import org.nebula.framework.core.WorkflowContext;
import org.nebula.framework.event.ActivityCompletedEvent;
import org.nebula.framework.event.Event;
import org.nebula.framework.event.WorkflowScheduledEvent;
import org.nebula.framework.event.WorkflowSignaledEvent;
import org.nebula.framework.model.MethodProfile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class WorkflowInstance {

  private final static Log log = LogFactory.getLog(WorkflowInstance.class);

  private ProcessDefinition processDefinition;

  private Object instance;

  public WorkflowInstance(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }

  private static Object[] castByProfile(String[] inputs, String... types) {
    return castByProfile(inputs, Arrays.asList(types));
  }

  private static Object[] castByProfile(String[] inputs, List<String> types) {
    Object[] args = new Object[inputs.length];

    for (int i = 0; i < inputs.length; i++) {

      args[i] = constructFromCanonical(inputs[i], types.get(i));

    }
    return args;
  }

  public void startWorkflow(WorkflowScheduledEvent event) {
    log.debug("startWorkflow instanceId=" + event.getInstanceId());

    String[] inputs = event.getInput().getInputs();
    MethodProfile profile = event.getStartProfile();

    Method startMethod = processDefinition.getStartMethod(profile);

    Object[] args = castByProfile(inputs, profile.getParameterTypes());

    log.debug("processDefinition: "
              + startMethod.getName() + ", class="
              + processDefinition.getWorkflowImplementation() + ", args="
              + args.length + ", arg.type:");

    createInstance(event.getRegistrationId(), event.getInstanceId());

    try {
      startMethod.invoke(instance, args);
    } catch (Exception e) {
      log.error("Failed to start  workflow", e);
      throw new RuntimeException("Failed to start workflow");
    }
  }

  public void signalEvent(Event aEvent) {

    WorkflowSignaledEvent event = (WorkflowSignaledEvent) aEvent;

    log.info("The workflow " + event.getInstanceId() + " is signaled.");

    Method signalMethod = processDefinition.getSignalMethod(event
                                                                .getSignalProfile());

    String[] inputs = event.getInput().getInputs();

    Object[] args = castByProfile(inputs, event.getSignalProfile().getParameterTypes());

    try {
      signalMethod.invoke(instance, args);
    } catch (Exception e) {
      log.error("Failed to signal workflow", e);
      throw new RuntimeException("Failed to signal workflow");
    }
  }

  public Promise handleCompletedActivity(
      ActivityCompletedEvent completedEvent) {

    Object[]
        args =
        castByProfile(completedEvent.getInput().getInputs(),
                      completedEvent.getMethodProfile().getReturnType());
    return new Promise(args[0]);
  }

  private void createInstance(String registrationId, String instanceId) {

    if (!createInstanceWithArgumentConstructor(registrationId, instanceId)) {
      createInstanceWithNoArgumentConstructor();
    }

  }

  private boolean createInstanceWithArgumentConstructor(String registrationId, String instanceId) {
    try {
      Constructor constructor =
          processDefinition.getWorkflowImplementation()
              .getDeclaredConstructor(WorkflowContext.class);

      WorkflowContext workflowContext = new WorkflowContext(registrationId, instanceId);
      instance = constructor.newInstance(workflowContext);

      return true;
    } catch (NoSuchMethodException e) {
      //ignore. Try to find constructor without arguments later
    } catch (Exception e) {
      log.error("Failed to create instance", e);
      throw new RuntimeException("Failed to create instance");
    }

    return false;
  }

  private boolean createInstanceWithNoArgumentConstructor() {
    try {
      Constructor
          constructor =
          processDefinition.getWorkflowImplementation().getDeclaredConstructor();
      constructor.setAccessible(true);
      instance = constructor.newInstance();

      return true;
    } catch (Exception e) {
      log.error("Failed to create instance", e);
      throw new RuntimeException("Failed to create instance");
    }

  }

}