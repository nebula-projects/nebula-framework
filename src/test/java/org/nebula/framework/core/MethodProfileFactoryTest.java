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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.nebula.framework.model.MethodProfile;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

interface Mock {

  public void test0(int data);

  public void test1(String data);

  public void test2(String[] data);

  public void test3(int[] data);

  public void test4(float[] data);

  public void test5(List<String> data);

  public void test6(Set<String> data);

  public void test7(Map<String, Integer> data);

  public void test8(List<String[]> data);

  public void test9(List<List<Integer[]>> data);

  public void test10(Map<Integer, List<String[]>> data);

  public void test11();

  public String test12();

  public int test13();

  public String[] test14();

  public int[] test15();

  public List<String> test16();

  public Set<String> test17();

  public Map<String, Integer> test18();

  public List<String[]> test19();

  public List<List<Integer[]>> test20();

  public Map<Integer, List<String[]>> test21();

  public void test22(Map<Integer, List<String[]>> data, int data2);

  public void test23(Map<Integer, List<String[]>> data, int[] data2);

  public void test24(Map<Integer, List<String[]>> data,
                     List<List<Integer[]>> data2);

  public void test25(Map<Integer, List<String[]>> data, String[] data2);

  public void test26(Map<Integer, List<String[]>> data, String[][] data2);

  public void test27(Map<Integer, List<String[]>> data, int[][] data2);

  public void test28(int[][] data);

  public void test29(String[][] data);

}

@RunWith(Parameterized.class)
public class MethodProfileFactoryTest {

  private String methodName;

  private String[] parameterTypes;

  private String returnType;

  public MethodProfileFactoryTest(String methodName, String[] parameterTypes,
                                  String returnType) {
    this.methodName = methodName;
    this.parameterTypes = parameterTypes;
    this.returnType = returnType;
  }

  @Parameters
  public static Collection data() {

    return Arrays.asList(new Object[][]{
        {"test0", new String[]{"int"}, "void"},
        {"test1", new String[]{"java.lang.String"}, "void"},
        {"test2", new String[]{"[Ljava.lang.String;"}, "void"},
        {"test3", new String[]{"[I"}, "void"},
        {"test4", new String[]{"[F"}, "void"},
        {"test5", new String[]{"java.util.List<java.lang.String>"}, "void"},
        {"test6", new String[]{"java.util.Set<java.lang.String>"}, "void"},
        {"test7", new String[]{"java.util.Map<java.lang.String,java.lang.Integer>"}, "void"},
        {"test8", new String[]{"java.util.List<java.lang.String[]>"}, "void"},
        {"test9", new String[]{"java.util.List<java.util.List<java.lang.Integer[]>>"}, "void"},
        {"test10",
         new String[]{"java.util.Map<java.lang.Integer,java.util.List<java.lang.String[]>>"},
         "void"},
        {"test11", new String[]{}, "void"},
        {"test12", new String[]{}, "java.lang.String"},
        {"test13", new String[]{}, "int"},
        {"test14", new String[]{}, "[Ljava.lang.String;"},
        {"test15", new String[]{}, "[I"},
        {"test16", new String[]{}, "java.util.List<java.lang.String>"},
        {"test17", new String[]{}, "java.util.Set<java.lang.String>"},
        {"test18", new String[]{}, "java.util.Map<java.lang.String,java.lang.Integer>"},
        {"test19", new String[]{}, "java.util.List<java.lang.String[]>"},
        {"test20", new String[]{}, "java.util.List<java.util.List<java.lang.Integer[]>>"},
        {"test21", new String[]{},
         "java.util.Map<java.lang.Integer,java.util.List<java.lang.String[]>>"},
        {"test22",
         new String[]{"java.util.Map<java.lang.Integer,java.util.List<java.lang.String[]>>", "int"},
         "void"},
        {"test23",
         new String[]{"java.util.Map<java.lang.Integer,java.util.List<java.lang.String[]>>", "[I"},
         "void"},
        {"test24",
         new String[]{"java.util.Map<java.lang.Integer,java.util.List<java.lang.String[]>>",
                      "java.util.List<java.util.List<java.lang.Integer[]>>"}, "void"},
        {"test25",
         new String[]{"java.util.Map<java.lang.Integer,java.util.List<java.lang.String[]>>",
                      "[Ljava.lang.String;"}, "void"},
        {"test26",
         new String[]{"java.util.Map<java.lang.Integer,java.util.List<java.lang.String[]>>",
                      "[[Ljava.lang.String;"}, "void"},
        {"test27",
         new String[]{"java.util.Map<java.lang.Integer,java.util.List<java.lang.String[]>>", "[[I"},
         "void"},
        {"test28", new String[]{"[[I"}, "void"},
        {"test29", new String[]{"[[Ljava.lang.String;"}, "void"},});

  }

  @Test
  public void testGetParameterTypes() {

    for (Method method : Mock.class.getMethods()) {
      String name = method.getName();

      if (name.equals(methodName)) {
        MethodProfile methodProfile = MethodProfileFactory.create(method);

        assertEquals(methodName, methodProfile.getName());

        assertEquals(returnType, methodProfile.getReturnType());

        assertArrayEquals(parameterTypes, methodProfile.getParameterTypes().toArray(new String[0]));
      }
    }

  }
}
