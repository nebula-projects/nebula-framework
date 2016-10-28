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

package org.nebula.framework.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.request.RegisterRequest;
import org.nebula.framework.client.response.RegisterResponse;
import org.nebula.framework.retry.FixedBackoffRetryPolicy;
import org.nebula.framework.retry.RetryException;
import org.nebula.framework.retry.RetryExecutor;
import org.nebula.framework.retry.RetryPolicy;

import java.util.ArrayList;
import java.util.List;

import static org.nebula.framework.utils.JsonUtils.toJson;

public abstract class NodeRegistry<T> {

  private final static Log log = LogFactory.getLog(NodeRegistry.class);

  private final static int MAX_REGISTER_RETRIES = Integer.MAX_VALUE;

  protected NebulaClient nebulaClient;

  private List<RetryPolicy<String>> retryPolicies = new ArrayList<RetryPolicy<String>>();

  public NodeRegistry(NebulaClient nebulaClient) {
    this.nebulaClient = nebulaClient;
  }

  protected abstract RegisterRequest createRegisterRequest(T definition, List<String> realms);

  public String register(final T definition, final List<String> realms) throws Exception {

    RetryPolicy<String> retryPolicy = new FixedBackoffRetryPolicy<String>(10, MAX_REGISTER_RETRIES);

    String registrationId = retryPolicy.retry(new RetryExecutor<String>() {
      public String tryIt() throws RetryException {

        RegisterRequest request = createRegisterRequest(definition, realms);

        try {
          return register(request);
        } catch (Exception e) {
          throw new RetryException(e);
        }
      }
    });

    retryPolicies.add(retryPolicy);

    return registrationId;
  }

  public void cancelRegister() {
    for (RetryPolicy retryPolicy : retryPolicies) {
      retryPolicy.cancel();
    }
  }

  private String register(RegisterRequest request) throws Exception {

    log.debug("Start to register " + toJson(request));

    RegisterResponse response = nebulaClient.post(request);

    log.debug("Succeed to register:" + toJson(response));

    return response.getRegistrationId();
  }


}
