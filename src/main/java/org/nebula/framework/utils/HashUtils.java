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

public class HashUtils {

  public static String md5(String input) {
    return hash(input, "MD5");
  }

  public static String sha1(String input) {
    return hash(input, "SHA1");
  }

  private static String hash(String input, String algorithm) {
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance(algorithm);
      byte[] array = md.digest(input.getBytes());
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < array.length; ++i) {
        sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
      }
      return sb.toString();
    } catch (Exception e) {
      //ignore
    }
    throw new IllegalArgumentException("The input "+input+" or algorithm " + algorithm +" is illegal");
  }



}