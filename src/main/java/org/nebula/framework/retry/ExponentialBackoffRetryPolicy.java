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

package org.nebula.framework.retry;

public class ExponentialBackoffRetryPolicy<T> extends AbstractRetryPolicy<T> {

  private long baseSleepSecs;
  private int maxRetries;

  public ExponentialBackoffRetryPolicy(long baseSleepSecs, int maxRetries) {
    this(baseSleepSecs, maxRetries, null);
  }

  public ExponentialBackoffRetryPolicy(long baseSleepSecs, int maxRetries,
                                       RetryPolicy<T> nextRetryPolicy) {
    this.baseSleepSecs = baseSleepSecs;
    this.maxRetries = maxRetries;
    this.nextRetryPolicy = nextRetryPolicy;
  }

  public boolean shouldRetry(Exception exception, int retries) {
    return retries < maxRetries;
  }

  public void waitFor(int retries) {
    if (retries <= 0) {
      throw new IllegalArgumentException("The retries should be bigger than zero.");
    }

    try {
      intervalSecs = (long) (baseSleepSecs * Math.pow(2, retries - 1));
      wait(1000 * intervalSecs);
    } catch (InterruptedException e) {
      // Ignore the exception.
    }
  }
}
