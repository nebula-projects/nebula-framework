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

import org.nebula.framework.annotation.Signal;
import org.nebula.framework.annotation.Start;
import org.nebula.framework.annotation.Workflow;
import org.nebula.framework.core.CronExpression;
import org.nebula.framework.core.MethodProfileFactory;
import org.nebula.framework.model.MethodProfile;
import org.nebula.framework.model.WorkflowProfile;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class ProcessDefinition {

  private Class workflowImplementation;

  private WorkflowProfile workflowProfile;

  private Map<MethodProfile, Method> startMethod = new HashMap<MethodProfile, Method>();

  private Map<MethodProfile, Method> signalMethods = new HashMap<MethodProfile, Method>();

  public ProcessDefinition(Class workflowImplementation) {
    if (workflowImplementation == null) {
      throw new IllegalArgumentException("The workflowImplementation can't be null.");
    }
    this.workflowImplementation = workflowImplementation;

    process();
  }

  private void process() {

    for (Class interfaze : workflowImplementation.getInterfaces()) {

      Workflow workflow = (Workflow) interfaze.getAnnotation(Workflow.class);

      if (workflow != null) {

        ensureUniqueWorkflowAnnotation();

        process(interfaze, workflow);
      }
    }

    if (workflowProfile == null) {
      throw new RuntimeException(
          "No @Workflow annotation for the interface implemented by the class "
          + workflowImplementation);
    }

  }

  private void ensureUniqueWorkflowAnnotation() {
    if (workflowProfile != null) {
      throw new RuntimeException(
          "Only one @Workflow interface is allowed per workflow implementation.");
    }
  }

  private void process(Class interfaze, Workflow workflow) {

    workflowProfile = buildWorkflowProfile(interfaze, workflow);

    for (Method method : interfaze.getMethods()) {
      processMethod(method);
    }

    ensureAtleastOneStartMethod();
  }

  private void processMethod(Method method) {

    processStartMethod(method);

    processSignalMethods(method);
  }

  private void processStartMethod(Method method) {
    Start start = (Start) method.getAnnotation(Start.class);

    if (start != null) {

      ensureUniqueStartMethod();

      MethodProfile startMethodProfile = MethodProfileFactory.create(method);

      ensureTimerStartWithoutArgument(startMethodProfile);

      startMethod.put(startMethodProfile, method);
    }
  }

  private void ensureTimerStartWithoutArgument(MethodProfile startMethodProfile) {
    if (timerStart()) {
      if (startMethodProfile.getParameterTypes().size() > 0) {
        throw new RuntimeException(
            "Arguments are not allowed for Timer start");
      }
    }
  }

  private boolean timerStart() {
    return workflowProfile.getCronExpression() != null;
  }

  private void ensureUniqueStartMethod() {
    if (startMethod.size() != 0) {
      throw new RuntimeException(
          "Only one @Start method is allowed in one @Workflow.");
    }
  }

  private void ensureAtleastOneStartMethod() {
    if (startMethod.size() == 0) {
      throw new RuntimeException(
          "The @Workflow should have one @Start method.");
    }
  }

  private void processSignalMethods(Method method) {
    Signal signal = (Signal) method.getAnnotation(Signal.class);

    if (signal != null) {
      signalMethods.put(MethodProfileFactory.create(method), method);
    }
  }

  private boolean workflowNameExists(Workflow workflow) {
    return workflow.name() != null
           && !workflow.name().trim().equals("");
  }

  private WorkflowProfile buildWorkflowProfile(Class interfaze,
                                               Workflow workflow) {

    WorkflowProfile profile = new WorkflowProfile();

    String name = workflowNameExists(workflow) ? workflow.name() : interfaze
        .getSimpleName();

    profile.setName(name);

    profile.setVersion(workflow.version());
    profile.setSerial(workflow.serial());

    String cronExpression = workflow.cronExpression().trim();

    if (cronExpression.length() != 0) {

      validateCronExpression(cronExpression);

      profile.setCronExpression(cronExpression);
    }

    return profile;
  }

  /**
   * Construct a new cronExpression object for cron expression validation.
   */
  private void validateCronExpression(String cronExpression) {
    try {
      new CronExpression(cronExpression);
    } catch (ParseException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public Class getWorkflowImplementation() {
    return workflowImplementation;
  }

  public Method getStartMethod(MethodProfile startProfile) {
    return startMethod.get(startProfile);
  }

  public Map<MethodProfile, Method> getStartMethod() {
    return startMethod;
  }

  public Map<MethodProfile, Method> getSignalMethods() {
    return signalMethods;
  }

  public Method getSignalMethod(MethodProfile signalProfile) {
    return signalMethods.get(signalProfile);
  }

  public WorkflowProfile getWorkflowProfile() {
    return workflowProfile;
  }

}
