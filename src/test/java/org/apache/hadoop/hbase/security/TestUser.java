/*
 * Copyright 2010 The Apache Software Foundation
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
package org.apache.hadoop.hbase.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.SmallTests;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.ImmutableSet;

@Category(SmallTests.class)
public class TestUser {
  private static Log LOG = LogFactory.getLog(TestUser.class);

  @Test
  public void testBasicAttributes() throws Exception {
    Configuration conf = HBaseConfiguration.create();
    User user = User.createUserForTesting(conf, "simple", new String[]{"foo"});
    assertEquals("Username should match", "simple", user.getName());
    assertEquals("Short username should match", "simple", user.getShortName());
    // don't test shortening of kerberos names because regular Hadoop doesn't support them
  }

  @Test
  public void testRunAs() throws Exception {
    Configuration conf = HBaseConfiguration.create();
    final User user = User.createUserForTesting(conf, "testuser", new String[]{"foo"});
    final PrivilegedExceptionAction<String> action = new PrivilegedExceptionAction<String>(){
      @Override
      public String run() throws IOException {
          User u = User.getCurrent();
          return u.getName();
      }
    };

    String username = user.runAs(action);
    assertEquals("Current user within runAs() should match",
        "testuser", username);

    // ensure the next run is correctly set
    User user2 = User.createUserForTesting(conf, "testuser2", new String[]{"foo"});
    String username2 = user2.runAs(action);
    assertEquals("Second username should match second user",
        "testuser2", username2);

    // check the exception version
    username = user.runAs(new PrivilegedExceptionAction<String>(){
      @Override
      public String run() throws Exception {
        return User.getCurrent().getName();
      }
    });
    assertEquals("User name in runAs() should match", "testuser", username);

    // verify that nested contexts work
    user2.runAs(new PrivilegedExceptionAction(){
      @Override
      public Object run() throws IOException, InterruptedException{
        String nestedName = user.runAs(action);
        assertEquals("Nest name should match nested user", "testuser", nestedName);
        assertEquals("Current name should match current user",
            "testuser2", User.getCurrent().getName());
        return null;
      }
    });

    //check the non exception version
    username = user.runAs(new PrivilegedAction<String>(){
      @Override
      public String run() {
        try {
          return User.getCurrent().getName();
        } catch (Exception e) {
          LOG.debug(e.getMessage());
        }
        return "empty";
      }
    });

    assertEquals("Current user within runAs() should match", "testuser", username);
  }

  /**
   * Make sure that we're returning a result for the current user.
   * Previously getCurrent() was returning null if not initialized on
   * non-secure Hadoop variants.
   */
  @Test
  public void testGetCurrent() throws Exception {
    User user1 = User.getCurrent();
    assertNotNull(user1.ugi);
    LOG.debug("User1 is "+user1.getName());

    for (int i =0 ; i< 100; i++) {
      User u = User.getCurrent();
      assertNotNull(u);
      assertEquals(user1.getName(), u.getName());
      assertEquals(user1, u);
      assertFalse(user1.equals(null));
      assertFalse(user1.equals(new Object()));
      assertEquals(user1.hashCode(), u.hashCode());
    }
  }

  @Test
  public void testUserGroupNames() throws Exception {
    final String username = "testuser";
    final ImmutableSet<String> singleGroups = ImmutableSet.of("group");
    final Configuration conf = HBaseConfiguration.create();
    User user = User.createUserForTesting(conf, username, singleGroups.toArray(new String[]{}));
    assertUserGroup(user, singleGroups);

    final ImmutableSet<String> multiGroups = ImmutableSet.of("group", "group1", "group2");
    user = User.createUserForTesting(conf, username, multiGroups.toArray(new String[]{}));
    assertUserGroup(user, multiGroups);
  }

  private void assertUserGroup(User user, ImmutableSet<String> groups) {
    assertNotNull("GroupNames should be not null", user.getGroupNames());
    assertTrue("UserGroupNames length should be == " + groups.size(),
        user.getGroupNames().length == groups.size());

    for (String group : user.getGroupNames()) {
      assertTrue("groupName should be in set ", groups.contains(group));
    }    
  }

  @Test
  public void testSecurityForNonSecureHadoop() {

    Configuration conf = HBaseConfiguration.create();
    conf.set(CommonConfigurationKeys.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
    conf.set("hbase.security.authentication", "kerberos");
    assertTrue("Security disabled in security configuration", User.isHBaseSecurityEnabled(conf));

    conf = HBaseConfiguration.create();
    conf.set(CommonConfigurationKeys.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
    assertFalse("Single property shouldn't be enoght for enable security", 
        User.isHBaseSecurityEnabled(conf));

    conf = HBaseConfiguration.create();
    conf.set("hbase.security.authentication", "kerberos");
    assertFalse("Single property shouldn't be enoght for enable security", 
        User.isHBaseSecurityEnabled(conf));
  }

  /**
   *
   * tests for non secure hadoop user
   *
   */
  @Test
  public void testHadoopUserCreation() throws IOException {
    UserGroupInformation userInfo = UserGroupInformation.getCurrentUser();
    User hadoopUser = User.create(userInfo);
    assertTrue(userInfo.equals(hadoopUser.ugi));

    //
    User.IS_SECURE_HADOOP = false;
    userInfo = UserGroupInformation.getCurrentUser();
    hadoopUser = User.create(userInfo);
    //non secure hadoop user
    assertFalse(hadoopUser.isSecurityEnabled());
    assertTrue(userInfo.equals(hadoopUser.ugi));
    User.IS_SECURE_HADOOP = true;

    User.IS_SECURE_HADOOP = false;
    try {
      User.createUserForTesting(HBaseConfiguration.create(),
        "simple", new String[]{"foo"});
    } catch (RuntimeException rex) {
      //expected class not found org.apache.hadoop.security.UnixUserGroupInformation
    }
    User.IS_SECURE_HADOOP = true;

    //with HadoopUser
    //can't find method getCurrentUGI in org.apache.hadoop.security.UserGroupInformation!
    User.IS_SECURE_HADOOP = false;
    try {
      userInfo = UserGroupInformation.getCurrentUser();
      hadoopUser = User.create(userInfo);
      hadoopUser.runAs(new PrivilegedAction<String>() {
        @Override
        public String run() {
          try {
            return User.getCurrent().getName();
          } catch (Exception ex) {
            LOG.debug(ex.getMessage());
          }
          return "empty";
        }
      });
    } catch (RuntimeException rex) {
      rex.printStackTrace();
      //expected
    } catch (Exception ex) {
      fail("UserGroupInformation.getCurrentUGI should be not found");
    }
    User.IS_SECURE_HADOOP = true;
    //

    //with HadoopUser
    //Can't find method getCurrentUGI in org.apache.hadoop.security.UserGroupInformation!
    User.IS_SECURE_HADOOP = false;
    try {
      userInfo = UserGroupInformation.getCurrentUser();
      hadoopUser = User.create(userInfo);

      hadoopUser.runAs(new PrivilegedExceptionAction() {
        @Override
        public Object run() throws Exception {
          return User.getCurrent().getName();
        }
      });
    } catch (RuntimeException rex) {
      LOG.debug(rex.getMessage());
    } catch (Exception ex) {
      fail("UserGroupInformation.getCurrentUGI should be not found");
    }
    User.IS_SECURE_HADOOP = true;
    //
    
   /*
    * Can't find method getCurrentUGI in 
    * org.apache.hadoop.security.UserGroupInformation.getCurrentUGI
    */
    User.IS_SECURE_HADOOP = false;
    try {
      hadoopUser = User.getCurrent();
    } catch (RuntimeException rex) {
      LOG.debug(rex.getMessage());
    } catch (Exception e) {
      fail("UserGroupInformation.getCurrentUGI should be not found");
    }
    User.IS_SECURE_HADOOP = true;

  }

  @org.junit.Rule
  public org.apache.hadoop.hbase.ResourceCheckerJUnitRule cu =
    new org.apache.hadoop.hbase.ResourceCheckerJUnitRule();
}
