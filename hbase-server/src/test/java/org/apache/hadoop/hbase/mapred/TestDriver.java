/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.mapred;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import org.apache.hadoop.hbase.SmallTests;
import org.apache.hadoop.hbase.util.LauncherSecurityManager;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(SmallTests.class)
public class TestDriver {

  private final String commonProgramDriverMessage = 
      "An example program must be given as the first argument";
  private final Pattern specificPattern = Pattern
      .compile("rowcounter: Count rows in HBase table");

  @Test
  @SuppressWarnings("deprecation")
  public void testDriverMainMethod() throws Throwable {
    String result = "";
    SecurityManager currentSecurityManager = null;
    final PrintStream currentPrintStream = System.out;
    try {
      currentSecurityManager = System.getSecurityManager();
      new LauncherSecurityManager();
      try {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        System.setOut(new PrintStream(data));
        try {
          Driver.main(new String[] {});
          fail("SecurityException expected");
        } catch (InvocationTargetException ex) {
          assertTrue(ex.getCause() instanceof SecurityException);
          result = data.toString();
          assertTrue(result.contains(commonProgramDriverMessage));
          assertTrue(specificPattern.matcher(result).find());
        }
      } finally {
        System.setOut(currentPrintStream);
      }
    } finally {
      System.setSecurityManager(currentSecurityManager);
    }
  }
}
