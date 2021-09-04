package com.swust.bigdata.servie;

import com.swust.bigdata.config.HbaseConfiguration;
import com.swust.bigdata.domain.AccessByHour;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author 雪瞳
 * @Slogan 忘时，忘物，忘我。
 * @Function
 * hbase 查询业务逻辑层
 */
@Service
public class HbaseServiceItem {

    @Bean
    public List<AccessByHour> hbaseService(){


        HbaseConfiguration hbase = new HbaseConfiguration();
        HbaseTemplate connection = hbase.hbaseTemplate();

        String tablename = "access-log-hour";
        Scan scan = new Scan();
        List<Result> results = connection.find(tablename, scan, (rowMapper, rowNum)->rowMapper);

        Iterator<Result> iterator = results.iterator();

        List<AccessByHour> access = new ArrayList<>();

        while (iterator.hasNext()){
            Result next = iterator.next();
            while (next.advance()){
                Cell cell = next.current();
                byte[] value = CellUtil.cloneValue(cell);
                byte[] rowKey = CellUtil.cloneRow(cell);
                String rowString = new String(rowKey);
                String valueString = new String(value);

                Long nums = getLong(valueString);


                String[] words = rowString.split("-");

                StringBuffer buffer = new StringBuffer();

                for (int i = 0; i < words.length - 1; i++){
                    buffer.append(words[i]);
                }
                String day = buffer.toString();
                String user = words[words.length - 1];

                AccessByHour data = new AccessByHour();
                data.setDay(day);
                data.setUser(user);
                data.setNum(String.valueOf(nums));

                access.add(data);
            }
        }

        return access;
    }

    /***
     * 将字符串类型的数据转为long类型
     * string---> bytes --> long
     * @param data
     * @return
     */
    public static Long getLong(String data) {
        byte[] bytes = data.getBytes();
        // 借助kava.nio.ByteBuffer将数据转化为其余类型数据
        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        long aLong = wrap.getLong();
        return aLong;
    }
}
