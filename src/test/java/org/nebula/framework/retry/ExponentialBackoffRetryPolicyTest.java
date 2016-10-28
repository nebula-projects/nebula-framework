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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nebula.framework.utils.TestUtil.sleep;

public class ExponentialBackoffRetryPolicyTest {

  @Test
  public void testRetryWithSuccessfulExecution() {
    ExponentialBackoffRetryPolicy policy = new ExponentialBackoffRetryPolicy(2, 4);

    final int successIn3rdExecution = 3;

    policy.retry(new RetryExecutor<Void>() {

      private int executionCount = 0;

      public Void tryIt() throws RetryException {
        executionCount++;
        if (executionCount == successIn3rdExecution) {
          return null;
        }

        throw new RetryException();
      }
    });

    assertEquals(3, policy.getRetries());
    assertEquals(4, (long) policy.getIntervalSecs());
  }

  @Test
  public void testRetryWithNextFixedBackoffRetryPolicy() {
    FixedBackoffRetryPolicy fixedBackoffPolicy = new FixedBackoffRetryPolicy(1, 2);
    ExponentialBackoffRetryPolicy<String>
        policy =
        new ExponentialBackoffRetryPolicy<String>(2, 2, fixedBackoffPolicy);

    String result = policy.retry(new RetryExecutor<String>() {
      public String tryIt() throws RetryException {
        throw new RetryException();
      }
    });

    assertNull(result);
    assertEquals(2, policy.getRetries());
    assertEquals(2, fixedBackoffPolicy.getRetries());
  }

  @Test
  public void testRetryReachMaxRetries() {
    ExponentialBackoffRetryPolicy<String> policy = new ExponentialBackoffRetryPolicy<String>(2, 3);

    String result = policy.retry(new RetryExecutor<String>() {
      public String tryIt() throws RetryException {
        throw new RetryException();
      }
    });

    assertNull(result);
    assertEquals(3, policy.getRetries());
  }

  @Test
  public void testRetryCanceled() {
    ExponentialBackoffRetryPolicy<String> policy = new ExponentialBackoffRetryPolicy<String>(2, 3);

    Thread t = new Thread(new RetryPolicyCancellation(policy));
    t.start();

    String result = policy.retry(new RetryExecutor<String>() {
      public String tryIt() throws RetryException {
        throw new RetryException();
      }
    });

    sleep(2);

    assertNull(result);
    assertEquals(2, policy.getRetries());
  }

  @Test
  public void testRetryCanceledWithNextRetryPolicy() {
    FixedBackoffRetryPolicy fixedBackoffPolicy = new FixedBackoffRetryPolicy(1, 4);
    ExponentialBackoffRetryPolicy<String>
        policy =
        new ExponentialBackoffRetryPolicy<String>(2, 2, fixedBackoffPolicy);

    Thread t = new Thread(new RetryPolicyCancellation(policy));
    t.start();

    String result = policy.retry(new RetryExecutor<String>() {
      public String tryIt() throws RetryException {
        throw new RetryException();
      }
    });

    sleep(2);

    assertNull(result);
    assertEquals(2, policy.getRetries());
    assertTrue(policy.isCanceled());
    assertTrue(fixedBackoffPolicy.isCanceled());
  }


  private class RetryPolicyCancellation implements Runnable {

    private RetryPolicy policy;

    public RetryPolicyCancellation(RetryPolicy policy) {
      this.policy = policy;
    }

    public void run() {
      sleep(3);
      policy.cancel();
    }
  }


}
