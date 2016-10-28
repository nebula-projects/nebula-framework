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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.request.CancelWorkflowRequest;
import org.nebula.framework.client.request.SignalWorkflowRequest;
import org.nebula.framework.client.request.StartWorkflowRequest;
import org.nebula.framework.client.response.SignalWorkflowResponse;
import org.nebula.framework.client.response.StartWorkflowResponse;
import org.nebula.framework.model.WorkflowProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class WorkflowClientImplTest {

  //  @Tested
  private WorkflowClientImpl workflowClientImpl;

  @Injectable
  private NebulaClient nebulaClient;

  @Injectable
  private WorkflowProfile workflowProfile;

  private List<String> realms = Arrays.asList(new String[]{"realm"});

  @Before
  public void setUp() {

    new Expectations() {
      {
        nebulaClient.getUser();
        result = "nebula";

        workflowProfile.getName();
        result = "workflow";

        workflowProfile.getVersion();
        result = "version";
      }
    };

    workflowClientImpl = new WorkflowClientImpl(nebulaClient, workflowProfile, realms);
  }


  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullNebulaClient() {
    new WorkflowClientImpl(null, new WorkflowProfile(), new ArrayList<String>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullWorkflowProfile() {
    new WorkflowClientImpl(nebulaClient, null, new ArrayList<String>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullRealms() {
    new WorkflowClientImpl(nebulaClient, new WorkflowProfile(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithEmptyRealms() {
    new WorkflowClientImpl(nebulaClient, new WorkflowProfile(), new ArrayList<String>());
  }

  @Test
  public void testStartWorkflow(@Mocked final StartWorkflowResponse response) throws Exception {

    new Expectations() {
      {
        nebulaClient.post(withInstanceOf(StartWorkflowRequest.class));
        result = response;

        response.getInstanceId();
        result = "1";

      }
    };

    workflowClientImpl.startWorkflow("methodName", new String[]{"parameterTypes"},
                                     new String[]{"parameterTypes"});

    assertEquals("1", workflowClientImpl.getInstanceId());

  }

  @Test
  public void testSignalWorkflow(@Mocked final SignalWorkflowResponse response) throws Exception {

    new Expectations() {
      {
        nebulaClient.post(withInstanceOf(SignalWorkflowRequest.class));
        result = response;
      }
    };

    workflowClientImpl.signalWorkflow("methodName", new String[]{"parameterTypes"},
                                      new String[]{"parameterTypes"});

    new Verifications() {
      {
        nebulaClient.post(withInstanceOf(SignalWorkflowRequest.class));
        times = 1;
      }
    };

  }

  @Test
  public void testCancelWorkflow() throws Exception {

    new Expectations() {
      {
        nebulaClient.post(withInstanceOf(CancelWorkflowRequest.class));
      }
    };

    workflowClientImpl.cancelWorkflow("instanceId", realms);

    new Verifications() {
      {
        nebulaClient.post(withInstanceOf(CancelWorkflowRequest.class));
        times = 1;
      }
    };

  }
}
