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
import org.nebula.framework.core.ActivityNodeRegistry;
import org.nebula.framework.core.Configuration;
import org.nebula.framework.core.NodeRegistry;
import org.nebula.framework.core.NodeWorker;

import java.util.List;

public class ActivityWorker extends NodeWorker<ActivityDefinition> {

  public ActivityWorker(NebulaClient nebulaClient) {
    this(nebulaClient, new Configuration());
  }

  public ActivityWorker(NebulaClient nebulaClient, Configuration configuration) {
    super(nebulaClient, configuration);
  }

  protected ActivityDefinition createNodeDefinition(Class nodeImplementation) {
    return new ActivityDefinition(nodeImplementation);
  }

  protected NodeDefinitionRealm createNodeDefinitionRealm(ActivityDefinition activityDefinition,
                                                          List<String> realms) {
    return new ActivityDefinitionRealm(activityDefinition, realms);
  }

  protected Runnable createEvenPoller(NebulaClient nebulaClient,
                                      ActivityDefinition activityDefinition, List<String> realms,
                                      Configuration configuration) {
    return new ActivityEventPoller(nebulaClient, activityDefinition, realms, configuration);
  }

  protected NodeRegistry createNodeRegistry(NebulaClient nebulaClient) {
    return new ActivityNodeRegistry(nebulaClient);
  }

  public static class ActivityDefinitionRealm
      extends NodeWorker.NodeDefinitionRealm<ActivityDefinition> {

    private ActivityDefinitionRealm(ActivityDefinition activityDefinition, List<String> realms) {
      super(activityDefinition, realms);
    }

    protected boolean hasDefinition(ActivityDefinition activityDefinition) {
      return getDefinition().getActivityProfile().equals(
          activityDefinition.getActivityProfile());
    }

    protected String getNodeInfo() {
      return "Activity[" + getDefinition().getActivityProfile().getActivity() + "], version["
             + getDefinition().getActivityProfile().getVersion() + "]";
    }
  }

}
