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

import org.nebula.framework.core.Context;
import org.nebula.framework.core.Promise;
import org.nebula.framework.model.ActivityProfile;
import org.nebula.framework.model.MethodProfile;
import org.nebula.framework.workflow.Decider;

public class ActivityClientImpl implements ActivityClient {

  public <T> Promise<T> scheduleActivity(ActivityProfile profile,
                                         MethodProfile methodProfile, Promise[] inputs,
                                         Promise... waitfor) {

    Decider decider = Context.getContext().getDecider();

    return decider.decideActivity(profile, methodProfile, inputs, waitfor);

  }

}
