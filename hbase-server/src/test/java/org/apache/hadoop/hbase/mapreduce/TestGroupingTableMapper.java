package org.apache.hadoop.hbase.mapreduce;


import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class TestGroupingTableMapper {

    @Test
    public void testGroupingTableMapper() throws Exception{
        
        GroupingTableMapper mapper= new GroupingTableMapper();
        Configuration configuration = new Configuration();
        configuration.set(GroupingTableMapper.GROUP_COLUMNS,"family1:clm family2:clm");
        mapper.setConf(configuration);
        
        Result result= mock(Result.class);
        Mapper<ImmutableBytesWritable,Result,ImmutableBytesWritable,Result>.Context context=mock(Mapper.Context.class);
        context.write(any(ImmutableBytesWritable.class),any(Result.class));
        List<KeyValue> keyValue= new ArrayList<KeyValue>();
        byte[] row= {};
        keyValue.add(new KeyValue(row, "family2".getBytes(), "clm".getBytes(), "value1".getBytes()));
        keyValue.add(new KeyValue(row, "family1".getBytes(), "clm".getBytes(), "value2".getBytes()));
        when(result.list()).thenReturn(keyValue);
        mapper.map(null, result, context);
        // template data
        byte[][] data={"value1".getBytes(),"value2".getBytes()};
        ImmutableBytesWritable ibw=mapper.createGroupKey(data);
        verify(context).write(ibw, result);
    }
    
    
}
