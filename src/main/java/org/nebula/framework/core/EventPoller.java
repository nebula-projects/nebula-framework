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
import org.nebula.framework.client.Request;
import org.nebula.framework.client.Response;
import org.nebula.framework.retry.ExponentialBackoffRetryPolicy;
import org.nebula.framework.retry.FixedBackoffRetryPolicy;
import org.nebula.framework.retry.RetryException;
import org.nebula.framework.retry.RetryExecutor;
import org.nebula.framework.retry.RetryPolicy;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class EventPoller<E, S extends Response> implements Runnable {

  private final static Log log = LogFactory.getLog(EventPoller.class);

  private NebulaClient nebulaClient;

  private E nodeDefinition;

  private List<String> realms;

  private Configuration configuration;

  private ExecutorService nodeExecutor;

  private RetryPolicy<S> retryPolicy;

  private boolean shouldRun = true;

  private long count;

  public EventPoller(NebulaClient nebulaClient,
                     E nodeDefinition,
                     List<String> realms,
                     Configuration configuration) {
    this.nebulaClient = nebulaClient;
    this.nodeDefinition = nodeDefinition;
    this.realms = realms;
    this.configuration = configuration;

    int maxExecutionThreads = configuration.getMaxExecutionThreads();

    this.nodeExecutor = new ThreadPoolExecutor(1, maxExecutionThreads, 60L,
                                               TimeUnit.SECONDS,
                                               new ArrayBlockingQueue<Runnable>(maxExecutionThreads,
                                                                                true),
                                               new ThreadPoolExecutor.CallerRunsPolicy());
  }

  public void stop() {
    shouldRun = false;
    retryPolicy.cancel();
  }

  public long getPollCount() {
    return count;
  }

  public void run() {

    while (shouldRun) {

      count++;

      final Request request = createPollRequest(nodeDefinition, realms);

      try {
        log.debug("EventPoller start to poll...");

        retryPolicy = createRetryPolicy();

        S response = retryPolicy.retry(
            new RetryExecutor<S>() {
              public S tryIt() throws RetryException {
                return poll(request);
              }
            });

        nodeExecutor
            .execute(createNodeHandler(nebulaClient, nodeDefinition, configuration, response));

      } catch (Exception e) {
        log.error("Failed to poll workflow", e);
      }
    }
  }

  protected abstract Request createPollRequest(E nodeDefinition, List<String> realms);

  protected abstract S processResponse(S response);

  protected abstract Runnable createNodeHandler(NebulaClient nebulaClient, E nodeDefinition,
                                                Configuration configuration, S response);

  private S poll(Request request) {
    try {
      S response = nebulaClient.get(request);
      return processResponse(response);
    } catch (Exception e) {
      //ignore the exception since RetryException will be thrown eventually.
    }

    throw new RetryException();
  }

  private RetryPolicy<S> createRetryPolicy() {

    int maxIntervalSecs = 64;

    FixedBackoffRetryPolicy<S> fixedBackoffPolicy =
        new FixedBackoffRetryPolicy<S>(maxIntervalSecs, Integer.MAX_VALUE);

    //maxRetries = 6, maxIntervalSecs = 64;
    ExponentialBackoffRetryPolicy<S>
        policy = new ExponentialBackoffRetryPolicy<S>(2, 7, fixedBackoffPolicy);

    return policy;
  }

}