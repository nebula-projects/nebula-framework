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
import org.nebula.framework.client.Response;
import org.nebula.framework.client.request.PollWorkflowRequest;
import org.nebula.framework.client.response.PollWorkflowResponse;
import org.nebula.framework.core.Configuration;

import java.util.List;
import java.util.concurrent.Executors;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import static org.nebula.framework.utils.TestUtil.sleep;

@RunWith(JMockit.class)
public class WorkflowEventPollerTest {

  @Tested
  private WorkflowEventPoller workflowEventPoller;

  @Mocked
  private WorkflowHandler workflowHandler;

  @Injectable
  private NebulaClient nebulaClient;

  @Injectable
  private ProcessDefinition processDefinition;

  @Injectable
  private Configuration configuration;

  @Injectable
  private List<String> realms;

  @Before
  public void setUp() {
    new Expectations() {
      {
        configuration.getMaxExecutionThreads();
        result = 1;
      }
    };

  }

  @Test
  public void testRun(@Mocked final PollWorkflowResponse response) throws Exception {

    new Expectations() {
      {
        nebulaClient.get(withInstanceOf(PollWorkflowRequest.class));
        result = response;

        response.getStatus();
        result= Response.Status.SUCCESS;

        workflowHandler.run();
      }
    };

    Executors.newFixedThreadPool(1).execute(workflowEventPoller);

    sleep(1);

    workflowEventPoller.stop();

    new Verifications() {
      {
        workflowHandler.run();
        workflowEventPoller.createNodeHandler(nebulaClient, processDefinition,configuration,response);
      }
    };
  }

  @Test
  public void testRunWithPollingRetry(@Mocked final PollWorkflowResponse response) throws Exception {

    new Expectations() {
      {
        onInstance(nebulaClient).get(withInstanceOf(PollWorkflowRequest.class));
        result = response;

        response.getStatus();
        returns(null, Response.Status.SUCCESS);

        workflowHandler.run();
      }
    };

    Executors.newFixedThreadPool(1).execute(workflowEventPoller);

    sleep(6);

    workflowEventPoller.stop();

    new Verifications() {
      {
        workflowHandler.run();
      }
    };

  }


}