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

package org.nebula.framework.client;

import org.nebula.framework.core.Authorization;

import static org.nebula.framework.utils.JsonUtils.toJson;

public abstract class AbstractRequest implements Request {

  private String accessId;

  public String getAccessId() {
    return accessId;
  }

  public void setAccessId(String accessId) {
    this.accessId = accessId;
  }

  public String toString() {
    return toJson(this);
  }

  public String toSignedSignature(String secretKey) {
    return toSignedSignature(
        Authorization.create().setAccessId(accessId).setRequestType(this.getClass().getSimpleName())
            .addTimestamp(), secretKey);
  }

  protected String toSignedSignature(Authorization authorization, String secretKey){
    return authorization.toSignedSignature(secretKey);
  }

}
