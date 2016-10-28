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
import org.junit.runner.RunWith;
import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.core.HeartbeatWorker;
import org.nebula.framework.core.NodeWorker;
import org.nebula.framework.core.WorkflowNodeRegistry;
import org.nebula.framework.retry.RetryException;
import org.nebula.framework.workflow.data.StartWorkflowWithoutParameterImpl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JMockit.class)
public class WorkflowWorkerTest {

  @Tested
  private WorkflowWorker worker;

  @Injectable
  private NebulaClient nebulaClient;

  @Mocked
  private ThreadPoolExecutor executor;

  @Mocked
  private WorkflowNodeRegistry registry;

  @Mocked
  private HeartbeatWorker heartbeatWorker;

  @Test
  public void testAddWorkflow() {

    worker.add(StartWorkflowWithoutParameterImpl.class, Arrays.asList(new String[]{"test"}));

    List<NodeWorker.NodeDefinitionRealm> pdRealms = worker.getNodeDefinitionRealms();

    assertEquals(1, pdRealms.size());
    assertEquals(StartWorkflowWithoutParameterImpl.class,
                 ((WorkflowWorker.ProcessDefinitionRealm) (pdRealms.get(0))).getDefinition()
                     .getWorkflowImplementation());
  }

  @Test
  public void testStart() throws Exception {

    worker.add(StartWorkflowWithoutParameterImpl.class, Arrays.asList(new String[]{"test"}));

    worker.start();

    new Verifications() {
      {
        registry.register(withInstanceOf(ProcessDefinition.class), withInstanceOf(List.class));
        times = 1;
        heartbeatWorker.runOnce();
        times = 1;
        executor.execute(withInstanceOf(WorkflowEventPoller.class));
        times = 1;
        executor.execute(withInstanceOf(HeartbeatWorker.class));
        times = 1;
      }
    };

    assertTrue(worker.isStarted());
  }

  @Test
  public void testStop() throws Exception {

    worker.add(StartWorkflowWithoutParameterImpl.class, Arrays.asList(new String[]{"test"}));

    worker.start();

    worker.stop();

    new Verifications() {
      {
        registry.cancelRegister();
        times = 1;
        executor.shutdown();
        times = 2;
      }
    };

    assertFalse(worker.isStarted());
  }

  @Test(expected = IllegalStateException.class)
  public void testStartWithFailedToRegister() throws Exception {

    new Expectations() {{
      registry.register(withInstanceOf(ProcessDefinition.class), withInstanceOf(List.class));
      result = new RetryException();

    }};

    worker.add(StartWorkflowWithoutParameterImpl.class, Arrays.asList(new String[]{"test"}));

    worker.start();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddWorkflowWithDuplicatedProcessDefinition() {
    worker.add(StartWorkflowWithoutParameterImpl.class, Arrays.asList(new String[]{"test"}));

    worker.add(StartWorkflowWithoutParameterImpl.class, Arrays.asList(new String[]{"test"}));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddWorkflowWithNullProcessDefinition() {
    worker.add(null, Arrays.asList(new String[]{"test"}));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddWorkflowWithoutRealms() {
    worker.add(StartWorkflowWithoutParameterImpl.class, Arrays.asList(new String[]{}));
  }

}