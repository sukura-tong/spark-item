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
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import scala.Tuple2;
import scala.Tuple3;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 统计用户每天所需要的时长
 */
public class GetUsersTimeByDay {
    public static void getUsersTimeByDay(JavaInputDStream<ConsumerRecord<String, String>> dataStream,
                                         JavaStreamingContext jsc) {

        JavaDStream<Tuple3<String, String, String>> chooseDataStream = dataStream.map(line -> {
            String metadata = line.value();
            String[] words = metadata.split("\t");
            String time = words[0].trim();
            // 格式化时间
            //    1606829727740	0	36.56.226.195	6	65	user6	5469	1.1.2	Android
            String format = DateFormatUtils.getDateFormatUtils(time);
            String day = getTimeByDay(format);


            String chooseTime = words[1].trim();
            String userId = words[5].trim();
            Tuple3<String, String, String> tuple3 = new Tuple3<>(day, userId, chooseTime);
            return tuple3;
        });

        JavaPairDStream<Tuple2<String, String>, Long> pair = chooseDataStream.mapToPair(line -> {
            String day = line._1();
            String user = line._2();
            String time = line._3();
            Long value = Long.valueOf(time);

            Tuple2<String, String> key = new Tuple2<>(day, user);
            Tuple2<Tuple2<String, String>, Long> tuple2 = new Tuple2<>(key, value);
            return tuple2;
        });

        JavaPairDStream<Tuple2<String, String>, Long> resultStream = pair.reduceByKey(new Function2<Long, Long, Long>() {
            @Override
            public Long call(Long one, Long two) throws Exception {
                long time = one + two;
                return time;
            }
        });

        // 将数据存入到Hbase
        resultStream.foreachRDD(eachRdd -> {

            eachRdd.foreachPartition(eachPartition -> {
                Table table = HbaseClientJavaApiUtils.getTabelByTableName("access-log-day");
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

    // YYYY-MM-dd-HH
    public static String getTimeByDay(String metaTime) {
        String[] split = metaTime.split("-");
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < split.length - 1; i++) {
            buffer.append(split[i]);
        }
        return buffer.toString();
    }

    @Test
    public void test() {
        getTimeByDay("YYYY-MM-dd-HH");
    }
}
