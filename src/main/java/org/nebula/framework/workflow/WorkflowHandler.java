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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.response.PollWorkflowResponse;
import org.nebula.framework.core.Configuration;
import org.nebula.framework.core.Context;

public class WorkflowHandler implements Runnable {

  private final static Log log = LogFactory.getLog(WorkflowHandler.class);

  private NebulaClient nebulaClient;

  private ProcessDefinition processDefinition;

  private Configuration configuration;

  private PollWorkflowResponse pollWorkflowResponse;

  public WorkflowHandler(NebulaClient nebulaClient,
                         ProcessDefinition processDefinition, Configuration configuration,
                         PollWorkflowResponse response) {
    this.nebulaClient = nebulaClient;
    this.processDefinition = processDefinition;
    this.configuration = configuration;
    this.pollWorkflowResponse = response;
  }

  public void run() {

    String registrationId = pollWorkflowResponse.getRegistrationId();
    String instanceId = pollWorkflowResponse.getInstanceId();
    String realm = pollWorkflowResponse.getRealm();
    String realmActId = pollWorkflowResponse.getRealmActId();

    log.debug("Workflow for instanceId " + instanceId + " start to run.");
    try {

      EventEmitter
          eventEmitter =
          new EventEmitter(nebulaClient, registrationId, instanceId, realm, realmActId);

      WorkflowEventRecords
          records = new WorkflowEventRecords(nebulaClient, instanceId, configuration);

      WorkflowInstance workflowInstance = new WorkflowInstance(processDefinition);

      Decider decider = new Decider(records, eventEmitter, workflowInstance);

      records.setSignalEventListener(decider);

      Context.getContext().setDecider(decider);
      decider.decide();


    } catch (Exception e) {
      log.error("Failed to run handler with registrationId=" + registrationId + ", instanceId="
                + instanceId, e);
    }
  }

}
