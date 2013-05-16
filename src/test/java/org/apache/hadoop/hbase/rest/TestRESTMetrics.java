/**
 * Copyright 2011 The Apache Software Foundation
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.hadoop.hbase.rest;

import org.apache.hadoop.hbase.SmallTests;
import org.apache.hadoop.hbase.rest.metrics.RESTMetrics;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import static org.junit.Assert.*;

/**
 * Test RESTMetrics class
 */
@Category(SmallTests.class)
public class TestRESTMetrics {

  @Test
  public void testRESTMetrics() throws InterruptedException {
    RESTMetrics test = new RESTMetrics();
    long start1 = System.currentTimeMillis();

    test.doUpdates(null);
    // started value
    assertEquals(0, test.getRequests(), 0.01);
    assertEquals(0, test.getSucessfulDeleteCount(), 0.01);
    assertEquals(0, test.getSucessfulPutCount(), 0.01);
    assertEquals(0, test.getSucessfulGetCount(), 0.01);
    assertEquals(0, test.getFailedDeleteCount(), 0.01);
    assertEquals(0, test.getFailedGetCount(), 0.01);
    assertEquals(0, test.getFailedPutCount(), 0.01);
    long start2 = System.currentTimeMillis();

    // sleep 2 sec
    Thread.sleep(2001);
    // couple belts
    test.incrementRequests(4);
    test.incrementSucessfulGetRequests(5);
    test.incrementSucessfulDeleteRequests(6);
    test.incrementSucessfulPutRequests(7);
    test.incrementFailedGetRequests(8);
    test.incrementFailedDeleteRequests(9);
    test.incrementFailedPutRequests(10);

    long finish1 = System.currentTimeMillis();

    test.doUpdates(null);

    long finish2 = System.currentTimeMillis();

    double average = (finish2 + finish1 - start1 - start2) / (2 * 1000);
    double delta = (finish2 - start1 - finish1 + start2) / (2 * 1000);
    // test metrics values
    assertEquals(4 / average, test.getRequests(), delta);
    assertEquals(5 / average, test.getSucessfulGetCount(), delta);
    assertEquals(6 / average, test.getSucessfulDeleteCount(), delta);
    assertEquals(7 / average, test.getSucessfulPutCount(), delta);
    assertEquals(8 / average, test.getFailedGetCount(), delta);
    assertEquals(9 / average, test.getFailedDeleteCount(), delta);
    assertEquals(10 / average, test.getFailedPutCount(), delta);
    test.shutdown();
  }
}
