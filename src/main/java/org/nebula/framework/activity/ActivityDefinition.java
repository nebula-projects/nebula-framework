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

import org.nebula.framework.annotation.Activity;
import org.nebula.framework.core.MethodProfileFactory;
import org.nebula.framework.model.ActivityProfile;
import org.nebula.framework.model.MethodProfile;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActivityDefinition {

  private Class activityImplementation;

  private Map<MethodProfile, Method> activityMethods = new HashMap<MethodProfile, Method>();

  private ActivityProfile activityProfile;

  public ActivityDefinition(Class activityImplementation) {

    if (activityImplementation == null) {
      throw new IllegalArgumentException("The activityImplementation can't be null.");
    }

    this.activityImplementation = activityImplementation;

    process();
  }

  public Class getActivityImplementation() {
    return activityImplementation;
  }

  public Method getActivityMethod(MethodProfile methodProfile) {
    return activityMethods.get(methodProfile);
  }

  public Set<MethodProfile> getMethodProfiles() {
    return activityMethods.keySet();
  }

  public ActivityProfile getActivityProfile() {
    return activityProfile;
  }

  private void process() {

    Activity activity = null;

    for (Class interfaze : activityImplementation.getInterfaces()) {
      activity = (Activity) interfaze.getAnnotation(Activity.class);

      ensureUniqueuActivityAnnotation(activity);

      process(interfaze, activity);
    }

  }

  private void ensureUniqueuActivityAnnotation(Activity activity) {
    if (activity != null && activityProfile != null) {
      throw new RuntimeException(
          "Only one @Activity interface is allowed per activity implementation.");
    }
  }

  private void process(Class interfaze, Activity activity) {

    if (activity != null) {
      activityProfile = buildActivityProfile(interfaze, activity);

      for (Method method : interfaze.getMethods()) {

        MethodProfile methodProfile = MethodProfileFactory
            .create(method);

        activityMethods.put(methodProfile, method);
      }

      ensureAtLeastOneMethod();
    }
  }

  private void ensureAtLeastOneMethod() {
    if (activityMethods.size() == 0) {
      throw new RuntimeException(
          "The @Activity  should have one method.");
    }
  }

  private ActivityProfile buildActivityProfile(Class interfaze,
                                               Activity activity) {

    String activityName = activity.name() == null
                          || activity.name().trim().equals("") ? interfaze
                              .getSimpleName() : activity.name();

    String version = activity.version();

    ActivityProfile profile = new ActivityProfile();
    profile.setVersion(version);
    profile.setActivity(activityName);

    return profile;
  }

}
