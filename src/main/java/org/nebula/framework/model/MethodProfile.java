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

package org.nebula.framework.model;

import java.util.ArrayList;
import java.util.List;

import static org.nebula.framework.utils.Validate.notEmpty;
import static org.nebula.framework.utils.Validate.notNull;

public class MethodProfile {

  private String name;
  private List<String> parameterTypes = new ArrayList<String>();
  private String returnType;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getParameterTypes() {
    return parameterTypes;
  }

  public void setParameterTypes(List<String> parameterTypes) {
    this.parameterTypes = parameterTypes;
  }

  public String getReturnType() {
    return returnType;
  }

  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof MethodProfile)) {
      return false;
    }
    MethodProfile profile = (MethodProfile) o;

    if (this.name == null || profile.name == null) {
      return false;
    }

    if (!this.name.equals(profile.name)) {
      return false;
    }
    //the method name and parametertypes is the identity of a method.
//		if (this.returnType == null || profile.returnType == null) {
//			return false;
//		}
//
//		if (!this.returnType.equals(profile.returnType)) {
//			return false;
//		}

    int thisParametersLength = this.parameterTypes.size();
    int parametersLength = profile.parameterTypes.size();

    if (thisParametersLength != parametersLength) {
      return false;
    }
    for (int i = 0; i < thisParametersLength; i++) {
      if (!this.parameterTypes.get(i).equals(
          profile.parameterTypes.get(i))) {
        return false;
      }
    }

    return true;

  }

  @Override
  public int hashCode() {
    int hash = name.hashCode();
    for (String parameterType : parameterTypes) {
      hash += parameterType.hashCode();
    }
    return hash;
  }

  public void validate(){
    notEmpty(getName(), "The method profile name should not be empty");
    notEmpty(getReturnType(), "The method profile returnType should not be empty");
    notNull(getParameterTypes(), "The method profile parameters should not be null");
  }
}
