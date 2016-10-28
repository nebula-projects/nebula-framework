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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CacheTest {

  @Test
  public void test() {
    Cache<String> cache = new Cache<String>(new CacheLoader<String>() {

      private boolean first = true;

      @Override
      public List<String> load() {
        if (first) {
          first = false;
          return Arrays.asList(new String[]{"a", "b", "c", "d"});
        }

        return new ArrayList<String>();
      }
    }, 100);

    for (String s : cache) {
      System.out.println("s=" + s);
    }

    for (String s : cache) {
      System.out.println("s2=" + s);
    }
  }

}