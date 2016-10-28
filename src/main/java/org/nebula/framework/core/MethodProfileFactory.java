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

import org.nebula.framework.model.MethodProfile;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodProfileFactory {

  private final static Map<Class, String> primitiveClass = new HashMap<Class, String>();

  static {
    primitiveClass.put(byte.class, "B");
    primitiveClass.put(short.class, "S");
    primitiveClass.put(int.class, "I");
    primitiveClass.put(long.class, "J");
    primitiveClass.put(float.class, "F");
    primitiveClass.put(double.class, "D");
    primitiveClass.put(boolean.class, "Z");
    primitiveClass.put(char.class, "C");
  }

  public static MethodProfile create(Method method) {

    MethodProfile methodProfile = new MethodProfile();

    methodProfile.setName(method.getName());

    methodProfile.setReturnType(extractParameterizedType(method
                                                             .getGenericReturnType()));

    Type[] types = method.getGenericParameterTypes();

    List<String> parameterTypes = new ArrayList<String>();

    for (int i = 0; i < types.length; i++) {
      Type type = types[i];

      parameterTypes.add(extractParameterizedType(type));
    }

    methodProfile.setParameterTypes(parameterTypes);

    return methodProfile;
  }

  private static String extractParameterizedType(Type type) {

    if (type instanceof ParameterizedType) {
      return type.toString().replaceAll(" ", "");
    } else if (type instanceof GenericArrayType) {
      return "[" + getArrayTypeName((GenericArrayType) type);
    } else {
      return ((Class) type).getName().replaceAll(" ", "");
    }
  }

  private static String getArrayTypeName(GenericArrayType arrayType) {

    Type type = arrayType.getGenericComponentType();

    if (type instanceof Class) {
      Class clazz = (Class) type;
      if (clazz.isPrimitive()) {
        return primitiveClass.get(clazz);
      } else {
        return "L" + clazz.getName().replaceAll(" ", "") + ";";
      }
    } else if (type instanceof GenericArrayType) {
      return "[" + getArrayTypeName((GenericArrayType) type);
    }

    throw new RuntimeException("The type " + arrayType
                               + " doesn't support.");
  }
}
