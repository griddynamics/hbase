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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.security;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;

import com.google.common.base.Strings;

public class HBaseKerberosUtils {
  public static final String KRB_PRINCIPAL = "hbase.regionserver.kerberos.principal";
  public static final String KRB_KEYTAB_FILE = "hbase.regionserver.keytab.file";

  public static boolean isKerberosPropertySetted() {
    String krbPrincipal = System.getProperty(KRB_PRINCIPAL);
    String krbKeytab = System.getProperty(KRB_KEYTAB_FILE);
    if (Strings.isNullOrEmpty(krbPrincipal) || Strings.isNullOrEmpty(krbKeytab)) {
      return false;
    }
    return true;
  }

  public static void setPrincipalForTesting(String principal) {
    setSystemProperty(KRB_PRINCIPAL, principal);
  }

  public static void setKeytabFileForTesting(String keytabFile) {
    setSystemProperty(KRB_KEYTAB_FILE, keytabFile);
  }

  private static void setSystemProperty(String propertyName,
      String propertyValue) {
    System.setProperty(propertyName, propertyValue);
  }

  public static String getKeytabFileForTesting() {
    return System.getProperty(KRB_KEYTAB_FILE);
  }

  public static String getPrincipalForTesting() {
    return System.getProperty(KRB_PRINCIPAL);
  }

  /**
   * 
   * @return Configuration
   */
  public static Configuration getSecuredConfiguration() {
    Configuration conf = new Configuration();
    conf.set(CommonConfigurationKeys.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
    conf.set("hbase.security.authentication", "kerberos");
    conf.setBoolean("hbase.security.authorization", true);
    conf.set(KRB_KEYTAB_FILE, System.getProperty(KRB_KEYTAB_FILE));
    conf.set(KRB_PRINCIPAL, System.getProperty(KRB_PRINCIPAL));
    return conf;
  }

  /**
   * 
   * @return Configuration
   */
  public static Configuration getConfigurationWithoutPrincipal() {
    Configuration conf = new Configuration();
    conf.set(CommonConfigurationKeys.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
    conf.set("hbase.security.authentication", "kerberos");
    return conf;
  }
}
