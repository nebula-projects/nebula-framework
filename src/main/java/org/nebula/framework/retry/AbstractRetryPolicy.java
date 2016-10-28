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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Note: The first retry is executed immediately.
 */
public abstract class AbstractRetryPolicy<T> implements RetryPolicy<T> {

  private final Log log = LogFactory.getLog(getClass());

  protected long intervalSecs;

  protected RetryPolicy<T> nextRetryPolicy;

  private boolean canceled = false;

  private int retries = 0;

  public void cancel() {

    canceled = true;

    synchronized (this) {
      notifyAll();
    }

    if (nextRetryPolicy != null) {
      nextRetryPolicy.cancel();
    }
  }

  public long getIntervalSecs() {
    return intervalSecs;
  }

  public int getRetries() {
    return retries;
  }

  public boolean isCanceled() {
    return canceled;
  }

  public final T retry(RetryExecutor<T> executor) throws RetryException {

    Exception exception = null;

    while (!canceled) {

      retries++;

      try {

        return executor.tryIt();

      } catch (Exception e) {
        log.info("Failed to retry. retries=" + retries + ", intervalSecs=" + intervalSecs);
        exception = e;
      }

      if (!shouldRetry(exception, retries)) {
        break;
      }

      synchronized (this) {
        if (!canceled) {
          waitFor(retries);
        }
      }

    }

    if(!canceled) {
      if(nextRetryPolicy!=null) {
        return nextRetryPolicy.retry(executor);
      }
    }

    throw new RetryException("Failed to retry.");
  }

}
