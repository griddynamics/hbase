package org.apache.hadoop.hbase.mapreduce;

import org.apache.hadoop.hbase.util.ExitException;

public class ExitUtil {

  private static boolean test=false;
  public static void exit(int exitCode){
    if(test){
      throw new ExitException(exitCode);
    }else{
      System.exit(exitCode);
    }
  }
  public static void activeTest(){
    test=true;
  }
}
