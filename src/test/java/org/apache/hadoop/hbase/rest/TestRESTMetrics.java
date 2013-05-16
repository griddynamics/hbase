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
@SuppressWarnings("deprecation")
@Category(SmallTests.class)
public class TestRESTMetrics {

  @Test
  public void testRESTMetrics() throws InterruptedException {
    RESTMetrics test = new RESTMetrics();
    test.doUpdates(null);
    assertEquals(0, test.getRequests(), 0.01);
    assertEquals(0, test.getSucessfulDeleteCount(), 0.01);
    assertEquals(0, test.getSucessfulPutCount(), 0.01);
    assertEquals(0, test.getSucessfulGetCount(), 0.01);
    assertEquals(0, test.getFailedDeleteCount(), 0.01);
    assertEquals(0, test.getFailedGetCount(), 0.01);
    assertEquals(0, test.getFailedPutCount(), 0.01);

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
    test.doUpdates(null);
    // test metrics values
    assertEquals(2f, test.getRequests(), 0.01);
    assertEquals(2.5f, test.getSucessfulGetCount(), 0.01);
    assertEquals(3f, test.getSucessfulDeleteCount(), 0.01);
    assertEquals(3.5f, test.getSucessfulPutCount(), 0.01);
    assertEquals(4f, test.getFailedGetCount(), 0.01);
    assertEquals(4.5f, test.getFailedDeleteCount(), 0.01);
    assertEquals(5f, test.getFailedPutCount(), 0.01);
    test.shutdown();
  }
}
