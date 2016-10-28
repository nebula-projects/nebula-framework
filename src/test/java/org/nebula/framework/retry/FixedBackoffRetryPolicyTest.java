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

public class FixedBackoffRetryPolicyTest {

  @Test
  public void testRetryWithSuccessfulExecution() {
    FixedBackoffRetryPolicy policy = new FixedBackoffRetryPolicy(1, 4);

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
    assertEquals(1, (long) policy.getIntervalSecs());
  }

  @Test
  public void testRetryReachMaxRetries() {
    FixedBackoffRetryPolicy<String> policy = new FixedBackoffRetryPolicy<String>(1, 3);

    String result = policy.retry(new RetryExecutor<String>() {
      public String tryIt() throws RetryException {
        throw new RetryException();
      }
    });

    assertNull(result);
    assertEquals(3, policy.getRetries());
  }

}