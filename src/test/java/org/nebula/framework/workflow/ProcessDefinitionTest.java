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

import org.junit.Test;
import org.nebula.framework.model.MethodProfile;
import org.nebula.framework.model.WorkflowProfile;
import org.nebula.framework.workflow.data.NoStartMethodWorkflowImpl;
import org.nebula.framework.workflow.data.NoWorkflowAnnotationWorkflowImpl;
import org.nebula.framework.workflow.data.OneSignalMethodWorkflowImpl;
import org.nebula.framework.workflow.data.OneStartMethodWorkflowImpl;
import org.nebula.framework.workflow.data.SignalWithArrayArgWorkflowImpl;
import org.nebula.framework.workflow.data.SpecifiedWorkflowProfileWorkflowImpl;
import org.nebula.framework.workflow.data.TimerStartWorkflowImpl;
import org.nebula.framework.workflow.data.TwoSignalMethodsWorkflowImpl;
import org.nebula.framework.workflow.data.TwoStartMethodWorkflowImpl;
import org.nebula.framework.workflow.data.TwoWorkflowAnnotationWorkflowImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProcessDefinitionTest {

  @Test
  public void testOneStartMethod() {
    ProcessDefinition definition = new ProcessDefinition(
        OneStartMethodWorkflowImpl.class);

    validateStartMethodName("start", definition);
  }

  @Test(expected = RuntimeException.class)
  public void testTwoStartMethods() {
    new ProcessDefinition(TwoStartMethodWorkflowImpl.class);
  }

  @Test(expected = RuntimeException.class)
  public void testNoStartMethod() {
    new ProcessDefinition(NoStartMethodWorkflowImpl.class);
  }

  @Test
  public void testOneSignalMethod() {
    ProcessDefinition definition = new ProcessDefinition(
        OneSignalMethodWorkflowImpl.class);

    validateStartMethodName("start", definition);

    MethodProfile signalProfile = new MethodProfile();
    signalProfile.setName("signal");
    signalProfile.setReturnType("void");

    List<String> parameterTypes = new ArrayList<String>();
    signalProfile.setParameterTypes(parameterTypes);

    assertEquals("signal", definition.getSignalMethod(signalProfile)
        .getName());
  }

  @Test
  public void testTwoSignalMethods() {

    ProcessDefinition definition = new ProcessDefinition(
        TwoSignalMethodsWorkflowImpl.class);

    validateSignalMethod(definition, new ArrayList<String>());

    validateSignalMethod(definition, Arrays.asList(new String[]{"java.lang.String"}));
  }

  private void validateSignalMethod(ProcessDefinition definition, List<String> parameterTypes){
    MethodProfile signalProfile = new MethodProfile();
    signalProfile.setName("signal");
    signalProfile.setReturnType("void");

    signalProfile.setParameterTypes(parameterTypes);

    assertEquals("signal", definition.getSignalMethod(signalProfile)
        .getName());
    assertEquals(parameterTypes.size(), definition.getSignalMethod(signalProfile)
        .getParameterTypes().length);
  }

  @Test(expected = RuntimeException.class)
  public void testNoWorkflowAnnotation() {
    new ProcessDefinition(NoWorkflowAnnotationWorkflowImpl.class);
  }

  @Test(expected = RuntimeException.class)
  public void testTwoWorkflowAnnotations() {
    new ProcessDefinition(TwoWorkflowAnnotationWorkflowImpl.class);
  }

  @Test
  public void testSpecifiedWorkflowProfileAndConfiguration() {
    ProcessDefinition definition = new ProcessDefinition(
        SpecifiedWorkflowProfileWorkflowImpl.class);

    WorkflowProfile profile = definition.getWorkflowProfile();

    assertEquals("specifiedWorkflow", profile.getName());
    assertEquals("2.0", profile.getVersion());

  }

  @Test
  public void testSignalMethodWithArrayArg() {
    ProcessDefinition definition = new ProcessDefinition(
        SignalWithArrayArgWorkflowImpl.class);

    MethodProfile signalProfile = new MethodProfile();
    signalProfile.setName("signal");

    List<String> parameterTypes = new ArrayList<String>();
    signalProfile.setParameterTypes(parameterTypes);

    for (MethodProfile profile : definition.getSignalMethods().keySet()) {
      assertEquals("[Ljava.lang.String;", profile.getParameterTypes().get(0));
    }
  }

  @Test
  public void testTimerStartWorkflow() throws Exception {
    ProcessDefinition definition = new ProcessDefinition(
        TimerStartWorkflowImpl.class);

    WorkflowProfile workflowProfile = definition.getWorkflowProfile();

    assertTrue(workflowProfile.isSerial());
    assertEquals("*/5 * * ? * * *", workflowProfile.getCronExpression());
  }

  private static void validateStartMethodName(String startMethodName, ProcessDefinition definition){
    assertEquals(startMethodName, definition.getStartMethod().keySet().iterator().next().getName());
  }

}
