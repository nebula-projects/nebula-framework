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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.Response;
import org.nebula.framework.client.request.CompleteActivityRequest;
import org.nebula.framework.client.response.PollActivityResponse;
import org.nebula.framework.model.Input;
import org.nebula.framework.retry.FixedBackoffRetryPolicy;
import org.nebula.framework.retry.RetryException;
import org.nebula.framework.retry.RetryExecutor;
import org.nebula.framework.retry.RetryPolicy;
import org.nebula.framework.utils.Validate;

import java.lang.reflect.Method;
import java.util.List;

import static org.nebula.framework.utils.JsonUtils.constructFromCanonical;
import static org.nebula.framework.utils.JsonUtils.toJson;

public class ActivityHandler implements Runnable {

  private final static Log log = LogFactory.getLog(ActivityHandler.class);

  private NebulaClient nebualClient;

  private ActivityDefinition activityDefinition;

  private PollActivityResponse response;

  public ActivityHandler(NebulaClient nebualClient,
                         ActivityDefinition activityDefinition,
                         PollActivityResponse response) {
    this.nebualClient = nebualClient;
    this.activityDefinition = activityDefinition;
    this.response = response;
  }

  public void run() {

    try {

      Method activityMethod = findActivityMethod();

      Object[] args = constructArguments();

      Object result = invokeActivityMethod(activityMethod, args);

      log.debug("The result is:" + result);

      completeActivity(response, result);

    } catch (Exception e) {
      log.error("Failed to handle the activity ", e);
      //If any exception is thrown, this activity will be polled and be handled again after some time.
      //So just log the error message.
    }
  }


  private Object invokeActivityMethod(Method activityMethod, Object[] args) throws Exception {
    return activityMethod.invoke(activityDefinition
                                     .getActivityImplementation().newInstance(), args);
  }

  private Method findActivityMethod() {

    Method activityMethod = activityDefinition
        .getActivityMethod(response.getMethodProfile());

    Validate.notNull(activityMethod, "There is no activity method for "
                                     + response.getMethodProfile());

    return activityMethod;
  }

  private Object[] constructArguments() {

    Input input = response.getInput();
    String[] inputs = input.getInputs();

    return constructArguments(inputs, response.getMethodProfile().getParameterTypes());
  }

  private Object[] constructArguments(String[] inputs, List<String> types) {
    Object[] args = new Object[inputs.length];

    for (int i = 0; i < inputs.length; i++) {

      args[i] = constructFromCanonical(inputs[i], types.get(i));

    }
    return args;
  }

  private void completeActivity(PollActivityResponse response, Object result) {

    final CompleteActivityRequest request = createCompleteActivityRequest(response, result);

    RetryPolicy<Response> retryPolicy = createFixedBackoffRetryPolicy();
    retryPolicy.retry(
        new RetryExecutor<Response>() {
          public Response tryIt() throws RetryException {
            return completeActivity(request);
          }
        });

  }

  private CompleteActivityRequest createCompleteActivityRequest(PollActivityResponse response,
                                                                Object result) {
    CompleteActivityRequest request = new CompleteActivityRequest();
    request.setActivityProfile(response.getActivityProfile());
    request.setMethodProfile(response.getMethodProfile());
    request.setRegistrationId(response.getRegistrationId());
    request.setInstanceId(response.getInstanceId());
    request.setRealm(response.getRealm());
    request.setRealmActId(response.getRealmActId());
    request.setEventId(response.getEventId());

    Input input = new Input();
    input.setInputs(new String[]{toJson(result)});
    request.setInput(input);

    return request;
  }

  private Response completeActivity(CompleteActivityRequest request) {
    try {
      return nebualClient.post(request);
    } catch (Exception e) {
      throw new RetryException("Failed to complete activity.", e);
    }
  }

  private <S> RetryPolicy<S> createFixedBackoffRetryPolicy() {

    int intervalInSecs = 1;
    int maxRetries = 3;

    FixedBackoffRetryPolicy<S> policy =
        new FixedBackoffRetryPolicy<S>(intervalInSecs, maxRetries);

    return policy;
  }

}
