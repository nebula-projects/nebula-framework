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

package org.nebula.framework.client.request;

import org.nebula.framework.client.AbstractRequest;
import org.nebula.framework.core.Authorization;

import java.util.List;

public class PollActivityRequest extends AbstractRequest {

  private String activity;
  private String version;
  private List<String> realms;

  public String getActivity() {
    return activity;
  }

  public void setActivity(String activity) {
    this.activity = activity;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<String> getRealms() {
    return realms;
  }

  public void setRealms(List<String> realms) {
    this.realms = realms;
  }

  protected String toSignedSignature(Authorization authorization, String secretKey) {
    return authorization.addField("activity", activity).addField("version", version).toSignedSignature(secretKey);
  }
}