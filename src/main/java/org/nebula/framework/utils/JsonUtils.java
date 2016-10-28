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

package org.nebula.framework.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JsonUtils {

  private final static Log log = LogFactory.getLog(JsonUtils.class);

  private final static ObjectMapper mapper = new ObjectMapper();

  public final static String toJson(Object obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (Exception e) {
      log.error("Failed to convert the object " + obj + " to json.");
      throw new IllegalArgumentException(e);
    }
  }

  public final static <T> T toObject(String json, Class<T> clazz) {
    try {
      return mapper.readValue(json, clazz);
    } catch (Exception e) {
      log.error("Failed to convert the json " + json + " to object.");
      throw new IllegalArgumentException(e);
    }
  }

  public final static <T> T convertValue(Object fromValue, Class<T> toValueType) {
    try {
      return mapper.convertValue(fromValue, toValueType);
    } catch (Exception e) {
      log.error("Failed to convertValue from " + fromValue + " to value type " + toValueType);
      throw new IllegalArgumentException(e);
    }
  }

  public static Object constructFromCanonical(String json, String canonical) {
    try {
      return mapper.readValue(json, TypeFactory.defaultInstance().constructFromCanonical(canonical));
    } catch (Exception e) {
      log.error("Failed to constructFromCanonical: the json " + json + " and the canonical " + canonical);
      throw new IllegalArgumentException(e);
    }
  }

}