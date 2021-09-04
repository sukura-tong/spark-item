package com.swust.bigdata.offset;

import com.swust.bigdata.pojo.KafkaOffset;
import com.swust.bigdata.utils.OffsetJdbcUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.TaskContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.*;

import java.util.*;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 将KafkaOffset保存到数据库内
 * 没有insert
 * 有update
 */
public class SaveOffsetToMysql {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setMaster("local[2]")
                .setAppName(SaveOffsetToMysql.class.getSimpleName());

        JavaStreamingContext jscc = new JavaStreamingContext(conf, new Duration(10000));
        jscc.sparkContext().setLogLevel("Error");

        String consumerName = "xiaolu";

        Map<String, Object> params = new HashMap<>();
        params.put("bootstrap.servers", "hadoop000:9092,hadoop000:9093,hadoop000:9094");
        params.put("key.deserializer", StringDeserializer.class);
        params.put("value.deserializer", StringDeserializer.class);
        params.put("group.id", consumerName);
        params.put("auto.offset.reset", "earliest");
        params.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList("nfy-replicated-topic");

        JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(
                jscc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, params)
        );

        stream.foreachRDD(rdd -> {
            if (!rdd.isEmpty()) {
                HasOffsetRanges ranges = (HasOffsetRanges) rdd.rdd();
                OffsetRange[] offsetRanges = ranges.offsetRanges();
                rdd.foreach(line -> {
                    OffsetRange range = offsetRanges[TaskContext.get().partitionId()];
                    System.out.println(range.topic() + " "
                            + range.partition() + " "
                            + range.fromOffset() + " "
                            + range.untilOffset());
                });

                // TODO .

                // 保存偏移量到数据库内
                List<OffsetRange> metaOffsets = Arrays.asList(offsetRanges);

                for (OffsetRange line : metaOffsets) {
                    int partitions = line.partition();
                    String topic = line.topic();
                    String groupid = consumerName;
                    long offset = line.untilOffset();

                    KafkaOffset data = new KafkaOffset();
                    data.setTopic(topic);
                    data.setGroupid(groupid);
                    data.setPartitions(partitions);
                    data.setOffset(offset);

                    // 调用OffsetJdbcUtils 实现
                    OffsetJdbcUtils.insertOrUpdate(data);
                }
            }
        });


        jscc.start();
        try {
            jscc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
