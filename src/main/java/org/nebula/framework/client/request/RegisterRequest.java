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
import org.nebula.framework.model.MethodProfile;
import org.nebula.framework.model.WorkflowProfile;

import java.util.List;

public class RegisterRequest extends AbstractRequest {

  private String user;
  private String name;
  private String version;
  private NodeType nodeType;
  private RegistrationInfo registrationInfo;

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public NodeType getNodeType() {
    return nodeType;
  }

  public void setNodeType(NodeType nodeType) {
    this.nodeType = nodeType;
  }

  public RegistrationInfo getRegistrationInfo() {
    return registrationInfo;
  }

  public void setRegistrationInfo(RegistrationInfo registrationInfo) {
    this.registrationInfo = registrationInfo;
  }

  public enum NodeType {
    WORKFLOW, ACTIVITY
  }

  public static class RegistrationInfo {

    private WorkflowProfile workflowProfile;

    private List<String> realms;

    private MethodProfile startProfile;

    private List<MethodProfile> methodProfiles;

    public WorkflowProfile getWorkflowProfile() {
      return workflowProfile;
    }

    public void setWorkflowProfile(WorkflowProfile workflowProfile) {
      this.workflowProfile = workflowProfile;
    }

    public List<String> getRealms() {
      return realms;
    }

    public void setRealms(List<String> realms) {
      this.realms = realms;
    }

    public MethodProfile getStartProfile() {
      return startProfile;
    }

    public void setStartProfile(MethodProfile startProfile) {
      this.startProfile = startProfile;
    }

    public List<MethodProfile> getMethodProfiles() {
      return methodProfiles;
    }

    public void setMethodProfiles(List<MethodProfile> methodProfiles) {
      this.methodProfiles = methodProfiles;
    }
  }

  protected String toSignedSignature(Authorization authorization, String secretKey) {
    return authorization.addField("version",version).addField("nodeType", nodeType).toSignedSignature(secretKey);
  }
}


