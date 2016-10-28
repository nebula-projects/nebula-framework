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

public class Configuration {

  private int heartbeatInSeconds = 30;

  private int maxEventsCache = 2000;

  private int eventsPageSize = 50;

  private int pollThreads = 1;
  private int maxExecutionThreads = 5;

  public int getHeartbeatInSeconds() {
    return heartbeatInSeconds;
  }

  public void setHeartbeatInSeconds(int heartbeatInSeconds) {
    if (heartbeatInSeconds < 1) {
      throw new IllegalArgumentException("The heartbeatInSeconds should be positive.");
    }
    this.heartbeatInSeconds = heartbeatInSeconds;
  }

  public int getMaxEventsCache() {
    return maxEventsCache;
  }

  public void setMaxEventsCache(int maxEventsCache) {
    if (maxEventsCache < 1) {
      throw new IllegalArgumentException("The maxEventsCache should be positive.");
    }

    if (maxEventsCache < eventsPageSize) {
      throw new IllegalArgumentException(
          "The maxEventsCache should be greater than eventsPageSize.");
    }

    this.maxEventsCache = maxEventsCache;
  }

  public int getEventsPageSize() {
    return eventsPageSize;
  }

  public void setEventsPageSize(int eventsPageSize) {
    if (eventsPageSize < 1) {
      throw new IllegalArgumentException("The eventsPageSize should be positive.");
    }

    if (maxEventsCache < eventsPageSize) {
      throw new IllegalArgumentException(
          "The maxEventsCache should be greater than eventsPageSize.");
    }

    this.eventsPageSize = eventsPageSize;
  }

  public int getPollThreads() {
    return pollThreads;
  }

  public void setPollThreads(int pollThreads) {
    if (pollThreads < 1) {
      throw new IllegalArgumentException("The pollThreads should be positive.");
    }
    this.pollThreads = pollThreads;
  }

  public int getMaxExecutionThreads() {
    return maxExecutionThreads;
  }

  public void setMaxExecutionThreads(int maxExecutionThreads) {
    if (maxExecutionThreads < 1) {
      throw new IllegalArgumentException("The maxExecutionThreads should be positive.");
    }
    this.maxExecutionThreads = maxExecutionThreads;
  }
}
