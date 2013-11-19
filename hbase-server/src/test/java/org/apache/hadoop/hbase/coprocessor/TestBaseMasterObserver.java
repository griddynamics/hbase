/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.coprocessor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests instantiation of the {@link org.apache.hadoop.hbase.coprocessor.BaseMasterObserver} with
 * the default constructor and invocation of all its methods.
 * Since all the methods are empty (BaseMasterObserver is just an empty adaptive implementation of
 * {@link MasterObserver} interface), here we only assert that no exception happens.
 * The test provides 100% coverage of BaseMasterObserver class.
 */
@Category(SmallTests.class)
public class TestBaseMasterObserver {

  private static final Log LOG = LogFactory.getLog(TestBaseMasterObserver.class);

  @Test
  public void testAllMethods() throws Exception {
    BaseMasterObserver baseMasterObserver = new BaseMasterObserver();
    Class<?> clazz = baseMasterObserver.getClass();
    int count = 0;
    for (Method method: clazz.getMethods()) {
      if (method.getDeclaringClass() != Object.class // ignore Object's methods
            && !Modifier.isStatic(method.getModifiers())) {
        invokeWithNullArgs(method, baseMasterObserver);
        count++;
      }
    }
    LOG.info(count + " methods invoked.");
  }

  private void invokeWithNullArgs(Method method, Object target) throws Exception {
    Class<?>[] paramaterTypes = method.getParameterTypes();
    LOG.debug("invoking ["+method+"]");
    Object[] parameters = composeNullParamaters(paramaterTypes);
    method.invoke(target, parameters);
  }

  private Object[] composeNullParamaters(Class<?>[] classes) throws Exception {
    Object[] paramaters = new Object[classes.length];
    for (int i=0; i<classes.length; i++) {
      Class<?> clazz = classes[i];
      if (clazz.isPrimitive()) {
        if (clazz == boolean.class) {
          paramaters[i] = Boolean.FALSE;
        } else if (clazz == int.class) {
          paramaters[i] = Integer.valueOf(0);
        }
        // other primitive types may added as needed
      }
      // any non-primitive parameter stays null
    }
    return paramaters;
  }
}