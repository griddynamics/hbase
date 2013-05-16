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
import org.apache.hadoop.metrics.MetricsContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


@Category(SmallTests.class)
public class TestRESTMetrics {

  @Test
  public void test1(){
    RESTMetrics test = new RESTMetrics();
    MetricsContext mc= mock(MetricsContext.class);
    test.doUpdates(mc);
    assertEquals(0, test.getRequests(),0.01);
    assertEquals(0, test.getSucessfulDeleteCount(),0.01);
    assertEquals(0, test.getSucessfulPutCount(),0.01);
    assertEquals(0, test.getSucessfulGetCount(),0.01);
    assertEquals(0, test.getFailedDeleteCount(),0.01);
    assertEquals(0, test.getFailedGetCount(),0.01);
    assertEquals(0, test.getFailedPutCount(),0.01);

    long start=System.currentTimeMillis();
    try {
      Thread.sleep(2001);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // couple belts
    assertTrue((System.currentTimeMillis()-start)>2000);
    test.incrementRequests(4);
    test.incrementSucessfulGetRequests(5);
    test.incrementSucessfulDeleteRequests(6);
    test.incrementSucessfulPutRequests(7);
    test.incrementFailedGetRequests(8);
    test.incrementFailedDeleteRequests(9);
    test.incrementFailedPutRequests(10);
    test.doUpdates(null);
    
    assertEquals(2f, test.getRequests(),0.01);
    assertEquals(2.5f, test.getSucessfulGetCount(),0.01);
    assertEquals(3f, test.getSucessfulDeleteCount(),0.01);
    assertEquals(3.5f, test.getSucessfulPutCount(),0.01);
    assertEquals(4f, test.getFailedGetCount(),0.01);
    assertEquals(4.5f, test.getFailedDeleteCount(),0.01);
    assertEquals(5f, test.getFailedPutCount(),0.01);
    
  }
}
