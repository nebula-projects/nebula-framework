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

package org.nebula.framework.activity;

import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.Request;
import org.nebula.framework.client.Response;
import org.nebula.framework.client.request.PollActivityRequest;
import org.nebula.framework.client.response.PollActivityResponse;
import org.nebula.framework.core.Configuration;
import org.nebula.framework.core.EventPoller;
import org.nebula.framework.core.NebulaException;

import java.util.List;

public class ActivityEventPoller extends EventPoller<ActivityDefinition, PollActivityResponse> {

  public ActivityEventPoller(NebulaClient nebulaClient,
                             ActivityDefinition activityDefinition, List<String> realms,
                             Configuration configuration) {
    super(nebulaClient, activityDefinition, realms, configuration);
  }

  @Override
  protected Request createPollRequest(ActivityDefinition activityDefinition, List<String> realms) {
    PollActivityRequest request = new PollActivityRequest();
    request.setActivity(activityDefinition.getActivityProfile().getActivity());
    request.setVersion(activityDefinition.getActivityProfile().getVersion());
    request.setRealms(realms);

    return request;
  }

  @Override
  protected PollActivityResponse processResponse(PollActivityResponse response) {
    if (Response.Status.SUCCESS == response.getStatus()) {
      return response;
    }

    throw new NebulaException("The response is not success.");
  }

  @Override
  protected Runnable createNodeHandler(NebulaClient nebulaClient,
                                       ActivityDefinition activityDefinition,
                                       Configuration configuration, PollActivityResponse response) {
    return new ActivityHandler(nebulaClient, activityDefinition, response);
  }
}
