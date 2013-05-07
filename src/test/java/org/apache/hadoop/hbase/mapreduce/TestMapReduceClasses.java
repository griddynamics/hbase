package org.apache.hadoop.hbase.mapreduce;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.Export.Exporter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

public class TestMapReduceClasses {

  
  @SuppressWarnings({"rawtypes", "unchecked" })
  @Test
  public void testIdentityTableMapper() throws Exception{
    IdentityTableMapper   mapper= new  IdentityTableMapper();
    Scan scan = new Scan();
    Job job= new Job(new Configuration());
    IdentityTableMapper.initJob("table", scan, Exporter.class, job);
    assertEquals(TableInputFormat.class, job.getInputFormatClass());
    assertEquals(Result.class, job.getMapOutputValueClass());
    assertEquals(ImmutableBytesWritable.class, job.getMapOutputKeyClass());
    assertEquals(Exporter.class, job.getMapperClass());
    
    Context context= mock(Context.class);
    ImmutableBytesWritable key= new ImmutableBytesWritable(Bytes.toBytes("key"));
    Result value = new Result(key);
    mapper.map(key, value, context);
    verify(context).write(key, value);
  }    
  
}
