package org.apache.hadoop.hbase.util;

public class ExitException extends RuntimeException{

  private int exitCode=0;
  public ExitException(int exitCode){
    this.exitCode=exitCode;
  }
  
  public int getExitCode(){
    return exitCode;
  }
}
