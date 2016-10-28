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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.Response;
import org.nebula.framework.client.request.GetEventsRequest;
import org.nebula.framework.client.request.PollWorkflowRequest;
import org.nebula.framework.client.request.StartWorkflowRequest;
import org.nebula.framework.client.response.GetEventsResponse;
import org.nebula.framework.client.response.PollWorkflowResponse;
import org.nebula.framework.core.Configuration;
import org.nebula.framework.event.Event;
import org.nebula.framework.utils.EventUtil;

import java.util.ArrayList;
import java.util.List;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class WorkflowEventRecordsTest {

  @Tested
  private WorkflowEventRecords workflowEventRecords;

  @Injectable
  private NebulaClient nebulaClient;

  @Injectable
  private String instanceId = "nebula-instanceId";

  @Injectable
  private int pageSize = 50;

  @Injectable
  private int maxCacheSize=100;

  private List<Event> events;

  @Before
  public void setUp() throws Exception {

    events = EventUtil.loadEvent("CustomerOrderWorkflow.events").getEvents();


  }

  @Test
  public void testWorkflowReplayer(@Mocked
                                     final GetEventsResponse response) throws Exception{

    new ReusableExpectations(response);

    WorkflowEventRecords.WorkflowReplayer replayer = workflowEventRecords.workflowReplayer();
    replayer.replay();

    assertTrue(replayer.isWorkflowScheduledReplayed());
    assertTrue(replayer.isWorkflowCompleted());
    assertFalse(replayer.isWorkflowCancelled());

    assertEquals(StartWorkflowRequest.StartMode.NORMAL, replayer.getStartMode());

  }




  private final class ReusableExpectations extends Expectations {


    public ReusableExpectations(final GetEventsResponse response) throws Exception {
      new Expectations() {
        {
          nebulaClient.get(withInstanceOf(GetEventsRequest.class));
          result = response;

          response.getPageNo();
          result = 1;

          response.getTotal();
          result = 11;

          response.getEvents();
          result = events;

        }
      };
    }
  }


}