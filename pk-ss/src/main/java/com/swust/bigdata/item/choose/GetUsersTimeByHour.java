package com.swust.bigdata.item.choose;

import com.swust.bigdata.pojo.AccessLogByHour;
import com.swust.bigdata.item.utils.DateFormatUtils;
import com.swust.bigdata.item.utils.HbaseClientJavaApiUtils;
import com.swust.bigdata.service.AccessByHourToHbaseService;
import org.apache.hadoop.hbase.client.Table;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import scala.Tuple2;
import scala.Tuple3;

public class GetUsersTimeByHour {
    public static void getUsersTimeByHour(JavaInputDStream<ConsumerRecord<String, String>> dataStream,
                                          JavaStreamingContext jsc) {

        JavaDStream<Tuple3<String, String, String>> chooseDataStream = dataStream.map(line -> {
            String metadata = line.value();
            String[] words = metadata.split("\t");
            String time = words[0].trim();
            // 格式化时间
            //    1606829727740	0	36.56.226.195	6	65	user6	5469	1.1.2	Android
            String format = DateFormatUtils.getDateFormatUtils(time);
            String chooseTime = words[1].trim();
            String userId = words[5].trim();
            Tuple3<String, String, String> tuple3 = new Tuple3<>(format, userId, chooseTime);
            return tuple3;
        });

        /**
         * 业务需求
         *  获取原始数据中按照小时的用户访问时长，并将计算结果存储到Hbase数据库内
         //         */
        JavaPairDStream<Tuple2<String, String>, Long> logPairDstream = chooseDataStream.mapToPair(line -> {
            String hours = line._1();
            String user = line._2();
            String times = line._3();
            long time = Long.parseLong(times);

            Tuple2<String, String> key = new Tuple2<>(hours, user);
            Tuple2<Tuple2<String, String>, Long> tuple2 = new Tuple2<>(key, time);
            return tuple2;
        });

        JavaPairDStream<Tuple2<String, String>, Long> logDisposeResult = logPairDstream.reduceByKey(new Function2<Long, Long, Long>() {
            @Override
            public Long call(Long one, Long two) throws Exception {
                long res = one + two;
                return res;
            }
        });
//         将数据写入到Hbase中 针对流数据业务处理需求，需要对每个RDD处理后将每个Partition内的数据一起进行存储
        logDisposeResult.foreachRDD(eachRdd -> {
            eachRdd.foreachPartition(eachPartition -> {
                Table table = HbaseClientJavaApiUtils.getTabelByTableName("access-log-hour");
                //将每个Partition内部的数据写入
                while (eachPartition.hasNext()) {

                    Tuple2<Tuple2<String, String>, Long> tuple2 = eachPartition.next();
                    //封装数据对象 使用包+实现类进行业务逻辑处理
                    AccessLogByHour access = new AccessLogByHour();
                    String family = "feature";
                    String fname = "time";

                    access.setFamily(family);
                    access.setFname(fname);
                    access.setValue(tuple2);

                    // 创建service对象
                    ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
                    AccessByHourToHbaseService hbaseService = (AccessByHourToHbaseService) context.getBean("AccessByHourToHbaseService");
                    hbaseService.insertOrUpdateToHbase(table, access);
                }
                // 关闭资源
                HbaseClientJavaApiUtils.closeTable(table);
            });

        });
    }
}
