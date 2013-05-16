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
    int incrementSucessfulGet = 20000;
    int incrementSucessfulDelete = 3000000;
    int incrementSucessfulPut = 3000000;
    int incrementRequest = incrementSucessfulGet + incrementSucessfulDelete + incrementSucessfulPut;

    int incrementFailedGetRequests = 100;
    int incrementFailedDeleteRequests = 30;
    int incrementFailedPutRequests = 2;

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
    test.incrementRequests(incrementRequest);
    test.incrementSucessfulGetRequests(incrementSucessfulGet);
    test.incrementSucessfulDeleteRequests(incrementSucessfulDelete);
    test.incrementSucessfulPutRequests(incrementSucessfulPut);
    test.incrementFailedGetRequests(incrementFailedGetRequests);
    test.incrementFailedDeleteRequests(incrementFailedDeleteRequests);
    test.incrementFailedPutRequests(incrementFailedPutRequests);

    long finish1 = System.currentTimeMillis();

    test.doUpdates(null);

    long finish2 = System.currentTimeMillis();

    double average = (finish2 + finish1 - start1 - start2) / (2 * 1000);
    double delta = (finish2 - start1 - finish1 + start2) / (2 * 1000);
    // test metrics values
    assertEquals(incrementRequest / average, test.getRequests(), incrementRequest / delta);
    assertEquals(incrementSucessfulGet / average, test.getSucessfulGetCount(),
        incrementSucessfulGet / delta);
    assertEquals(incrementSucessfulDelete / average, test.getSucessfulDeleteCount(),
        incrementSucessfulDelete / delta);
    assertEquals(incrementSucessfulPut / average, test.getSucessfulPutCount(),
        incrementSucessfulPut / delta);
    assertEquals(incrementFailedGetRequests / average, test.getFailedGetCount(),
        incrementFailedGetRequests / delta);
    assertEquals(incrementFailedDeleteRequests / average, test.getFailedDeleteCount(),
        incrementFailedDeleteRequests / delta);
    assertEquals(incrementFailedPutRequests / average, test.getFailedPutCount(),
        incrementFailedPutRequests / delta);
    test.shutdown();
  }
}
