/**
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

package org.apache.hadoop.hbase.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;


/**
 * This class is a wrapper for the implementation of
 * com.sun.management.UnixOperatingSystemMXBean
 * It will decide to use the sun api or its own implementation
 * depending on the runtime (vendor) used.
 */

@InterfaceAudience.Public
@InterfaceStability.Evolving
public class JVM 
{
  static final Logger LOG = LoggerFactory.getLogger(JVM.class);

  private OperatingSystemMXBean osMbean;

  private static final boolean ibmvendor =
    System.getProperty("java.vendor").contains("IBM");
  private static final boolean windows = 
    System.getProperty("os.name").startsWith("Windows");
  private static final boolean linux =
    System.getProperty("os.name").startsWith("Linux");
  private static final String JVMVersion = System.getProperty("java.version");

  /**
   * Constructor. Get the running Operating System instance
   */
  public JVM () {
    this.osMbean = ManagementFactory.getOperatingSystemMXBean();
  }
 
  /**
   * Check if the OS is unix. 
   * 
   * @return whether this is unix or not.
   */
  public static boolean isUnix() {
    if (windows) {
      return false;
    }
    return (ibmvendor ? linux : true);
  }
  
  /**
   * Check if the finish() method of GZIPOutputStream is broken
   * 
   * @return whether GZIPOutputStream.finish() is broken.
   */
  public static boolean isGZIPOutputStreamFinishBroken() {
    return ibmvendor && JVMVersion.contains("1.6.0");
  }

  /**
   * Load the implementation of UnixOperatingSystemMXBean for Oracle jvm
   * and runs the desired method. 
   * @param mBeanMethodName : method to run from the interface UnixOperatingSystemMXBean
   * @return the method result
   */
  private Long runUnixMXBeanMethod (String mBeanMethodName) {  
    Object unixos;
    Class<?> classRef;
    Method mBeanMethod;

    try {
      classRef = Class.forName("com.sun.management.UnixOperatingSystemMXBean");
      if (classRef.isInstance(osMbean)) {
        mBeanMethod = classRef.getMethod(mBeanMethodName, new Class[0]);
        unixos = classRef.cast(osMbean);
        return (Long)mBeanMethod.invoke(unixos);
      }
    }
    catch(Exception e) {
      LOG.warn("Not able to load class or method for" +
          " com.sun.management.UnixOperatingSystemMXBean.", e);
    }
    return null;
  }

  /**
   * Get the number of opened filed descriptor for the runtime jvm.
   * If Oracle java, it will use the com.sun.management interfaces.
   * Otherwise, this methods implements it (linux only).  
   * @return number of open file descriptors for the jvm
   */
  public long getOpenFileDescriptorCount() {

    Long ofdc;
    
    if (!ibmvendor) {
      ofdc = runUnixMXBeanMethod("getOpenFileDescriptorCount");
      return (ofdc != null ? ofdc.longValue () : -1);
    }
    InputStream in = null;
    BufferedReader output = null;
    try {
      //need to get the PID number of the process first
      RuntimeMXBean rtmbean = ManagementFactory.getRuntimeMXBean();
      String rtname = rtmbean.getName();
      String[] pidhost = rtname.split("@");

      //using linux bash commands to retrieve info
      Process p = Runtime.getRuntime().exec(
      new String[] { "bash", "-c",
          "ls /proc/" + pidhost[0] + "/fdinfo | wc -l" });
      in = p.getInputStream();
      output = new BufferedReader(new InputStreamReader(in));
      String openFileDesCount;
      if ((openFileDesCount = output.readLine()) != null)      
             return Long.parseLong(openFileDesCount);
     } catch (IOException ie) {
       LOG.warn("Not able to get the number of open file descriptors", ie);
     } finally {
       if (output != null) {
         try {
           output.close();
         } catch (IOException e) {
           LOG.warn("Not able to close the InputStream", e);
         }
       }
       if (in != null){
         try {
           in.close();
         } catch (IOException e) {
           LOG.warn("Not able to close the InputStream", e);
         }
       }
    }
    return -1;
  }

  /**
   * @see java.lang.management.OperatingSystemMXBean#getSystemLoadAverage
   */
  public double getSystemLoadAverage() {
    return osMbean.getSystemLoadAverage();
  }

  /**
   * @return the physical free memory (not the JVM one, as it's not very useful as it depends on
   *  the GC), but the one from the OS as it allows a little bit more to guess if the machine is
   *  overloaded or not).
   */
  public long getFreeMemory() {
    if (ibmvendor){
      return 0;
    }

    Long r =  runUnixMXBeanMethod("getFreePhysicalMemorySize");
    return (r != null ? r : -1);
  }


  /**
   * Workaround to get the current number of process running. Approach is the one described here:
   * http://stackoverflow.com/questions/54686/how-to-get-a-list-of-current-open-windows-process-with-java
   */
  public int getNumberOfRunningProcess(){
    // stubbed as a workaround against ResourceChecker hanging problem.
    // See also http://stackoverflow.com/questions/11756267/executing-process-with-processbuilder-causes-hang-on-waitfor
    // Currently no solution is known for the problem.
    
//    2013-05-22 20:06:21.227    java.lang.Thread.State: WAITING (on object monitor)
//    2013-05-22 20:06:21.227   at java.lang.Object.wait(Native Method)
//    2013-05-22 20:06:21.228   - waiting on <0x00000007de98fd40> (a java.lang.UNIXProcess$Gate)
//    2013-05-22 20:06:21.228   at java.lang.Object.wait(Object.java:485)
//    2013-05-22 20:06:21.228   at java.lang.UNIXProcess$Gate.waitForExit(UNIXProcess.java:64)
//    2013-05-22 20:06:21.229   - locked <0x00000007de98fd40> (a java.lang.UNIXProcess$Gate)
//    2013-05-22 20:06:21.229   at java.lang.UNIXProcess.<init>(UNIXProcess.java:145)
//    2013-05-22 20:06:21.229   at java.lang.ProcessImpl.start(ProcessImpl.java:65)
//    2013-05-22 20:06:21.230   at java.lang.ProcessBuilder.start(ProcessBuilder.java:452)
//    2013-05-22 20:06:21.230   at java.lang.Runtime.exec(Runtime.java:593)
//    2013-05-22 20:06:21.231   at java.lang.Runtime.exec(Runtime.java:431)
//    2013-05-22 20:06:21.231   at java.lang.Runtime.exec(Runtime.java:328)
//    2013-05-22 20:06:21.231   at org.apache.hadoop.hbase.util.JVM.getNumberOfRunningProcess(JVM.java:201)
//    2013-05-22 20:06:21.232   at org.apache.hadoop.hbase.ResourceCheckerJUnitListener$ProcessCountResourceAnalyzer.getVal(ResourceCheckerJUnitListener.java:123)
//    2013-05-22 20:06:21.232   at org.apache.hadoop.hbase.ResourceChecker.fill(ResourceChecker.java:114)
//    2013-05-22 20:06:21.232   at org.apache.hadoop.hbase.ResourceChecker.fillInit(ResourceChecker.java:103)
//    2013-05-22 20:06:21.233   at org.apache.hadoop.hbase.ResourceChecker.start(ResourceChecker.java:186)
//    2013-05-22 20:06:21.233   at org.apache.hadoop.hbase.ResourceCheckerJUnitListener.start(ResourceCheckerJUnitListener.java:156)
//    2013-05-22 20:06:21.233   at org.apache.hadoop.hbase.ResourceCheckerJUnitListener.testStarted(ResourceCheckerJUnitListener.java:179)
//    2013-05-22 20:06:21.234   at org.junit.runner.notification.RunNotifier$3.notifyListener(RunNotifier.java:115)
//    2013-05-22 20:06:21.234   at org.junit.runner.notification.RunNotifier$SafeNotifier.run(RunNotifier.java:61)
//    2013-05-22 20:06:21.234   - locked <0x000000076a368140> (a java.util.Collections$SynchronizedRandomAccessList)
//    2013-05-22 20:06:21.235   at org.junit.runner.notification.RunNotifier.fireTestStarted(RunNotifier.java:112)
//    2013-05-22 20:06:21.235   at org.junit.internal.runners.JUnit38ClassRunner$OldTestClassAdaptingListener.startTest(JUnit38ClassRunner.java:35)
//    2013-05-22 20:06:21.236   at junit.framework.TestResult.startTest(TestResult.java:168)
//    2013-05-22 20:06:21.236   at junit.framework.TestResult.run(TestResult.java:119)
//    2013-05-22 20:06:21.236   at junit.framework.TestCase.run(TestCase.java:129)
//    2013-05-22 20:06:21.237   at junit.framework.TestSuite.runTest(TestSuite.java:255)
//    2013-05-22 20:06:21.237   at junit.framework.TestSuite.run(TestSuite.java:250)
//    2013-05-22 20:06:21.238   at org.junit.internal.runners.JUnit38ClassRunner.run(JUnit38ClassRunner.java:84)
//    2013-05-22 20:06:21.238   at org.junit.runners.Suite.runChild(Suite.java:127)
//    2013-05-22 20:06:21.238   at org.junit.runners.Suite.runChild(Suite.java:26)
//    2013-05-22 20:06:21.239   at org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
//    2013-05-22 20:06:21.239   at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:439)
//    2013-05-22 20:06:21.239   at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:303)
//    2013-05-22 20:06:21.240   at java.util.concurrent.FutureTask.run(FutureTask.java:138)
//    2013-05-22 20:06:21.240   at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:895)
//    2013-05-22 20:06:21.240   at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:918)
//    2013-05-22 20:06:21.241   at java.lang.Thread.run(Thread.java:662)
    
    return 0;
//    if (!isUnix()){
//      return 0;
//    }
//
//    BufferedReader input = null;
//    try {
//      int count = 0;
//      Process p = Runtime.getRuntime().exec("ps -e");
//      input = new BufferedReader(new InputStreamReader(p.getInputStream()));
//      while (input.readLine() != null) {
//        count++;
//      }
//      return count - 1; //  -1 because there is a headline
//    } catch (IOException e) {
//      return -1;
//    }  finally {
//      if (input != null){
//        try {
//          input.close();
//        } catch (IOException ignored) {
//        }
//      }
//    }
  }

  /**
   * Get the number of the maximum file descriptors the system can use.
   * If Oracle java, it will use the com.sun.management interfaces.
   * Otherwise, this methods implements it (linux only).  
   * @return max number of file descriptors the operating system can use.
   */
  public long getMaxFileDescriptorCount() {
    Long mfdc;
    if (!ibmvendor) {
      mfdc = runUnixMXBeanMethod("getMaxFileDescriptorCount");
      return (mfdc != null ? mfdc.longValue () : -1);
    }
    InputStream in = null;
    BufferedReader output = null;
    try {
      //using linux bash commands to retrieve info
      Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", "ulimit -n" });
      in = p.getInputStream();
      output = new BufferedReader(new InputStreamReader(in));
      String maxFileDesCount;
      if ((maxFileDesCount = output.readLine()) != null) return Long.parseLong(maxFileDesCount);
    } catch (IOException ie) {
      LOG.warn("Not able to get the max number of file descriptors", ie);
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          LOG.warn("Not able to close the reader", e);
        }
      }
      if (in != null){
        try {
          in.close();
        } catch (IOException e) {
          LOG.warn("Not able to close the InputStream", e);
        }
      }
    }
    return -1;
 }
}
