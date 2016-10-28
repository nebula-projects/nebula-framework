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

import org.nebula.framework.annotation.Activity;
import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.core.Configuration;
import org.nebula.framework.core.NodeRegistry;
import org.nebula.framework.core.NodeWorker;
import org.nebula.framework.core.WorkflowNodeRegistry;

import java.util.List;

/**
 * A <code>WorkflowWorker</code> starts a workflow node. Multiple workflow implementation can be
 * added to the worker with a list of realms. The realms are the bridges between {@link
 * org.nebula.framework.annotation.Workflow} and {@link Activity}.
 *
 * @author Guang
 * @since 1.0
 */
public class WorkflowWorker extends NodeWorker<ProcessDefinition> {

  public WorkflowWorker(NebulaClient nebulaClient) {
    this(nebulaClient, new Configuration());
  }

  public WorkflowWorker(NebulaClient nebulaClient, Configuration configuration) {
    super(nebulaClient, configuration);
  }

  protected ProcessDefinition createNodeDefinition(Class nodeImplementation) {
    return new ProcessDefinition(nodeImplementation);
  }

  protected NodeDefinitionRealm createNodeDefinitionRealm(ProcessDefinition processDefinition,
                                                          List<String> realms) {
    return new ProcessDefinitionRealm(processDefinition, realms);
  }

  protected Runnable createEvenPoller(NebulaClient nebulaClient,
                                      ProcessDefinition processDefinition, List<String> realms,
                                      Configuration configuration) {
    return new WorkflowEventPoller(nebulaClient,
                                   processDefinition,
                                   realms,
                                   configuration);
  }

  protected NodeRegistry createNodeRegistry(NebulaClient nebulaClient) {
    return new WorkflowNodeRegistry(nebulaClient);
  }

  public static class ProcessDefinitionRealm
      extends NodeWorker.NodeDefinitionRealm<ProcessDefinition> {

    private ProcessDefinitionRealm(ProcessDefinition processDefinition, List<String> realms) {
      super(processDefinition, realms);
    }

    protected boolean hasDefinition(ProcessDefinition processDefinition) {
      return getDefinition().getWorkflowProfile().equals(
          processDefinition.getWorkflowProfile());
    }

    protected String getNodeInfo() {
      return "Workflow[" + getDefinition().getWorkflowProfile().getName() + "], version["
             + getDefinition().getWorkflowProfile().getVersion() + "]";
    }
  }

}
