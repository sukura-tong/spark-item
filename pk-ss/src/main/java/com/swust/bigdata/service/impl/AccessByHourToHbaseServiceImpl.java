package com.swust.bigdata.service.impl;

import com.swust.bigdata.pojo.AccessLogByHour;
import com.swust.bigdata.service.AccessByHourToHbaseService;
import org.apache.hadoop.hbase.client.Table;
import scala.Tuple2;

import java.io.IOException;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 将传入过来的数据写入到Hbase
 */
public class AccessByHourToHbaseServiceImpl implements AccessByHourToHbaseService {

    @Override
    public void insertOrUpdateToHbase(Table table, AccessLogByHour params) throws IOException {
        // 获取基本信息
        Tuple2<Tuple2<String, String>, Long> value = params.getValue();
        String hour = value._1()._1();
        String user = value._1()._2();
        String rowKey = hour + "-" + user;

        String family = params.getFamily();
        String fname = params.getFname();

        Long data = value._2();
        System.out.println(data);
        // insert or update
        table.incrementColumnValue(
                rowKey.getBytes(),
                family.getBytes(),
                fname.getBytes(),
                data
        );
    }
}
