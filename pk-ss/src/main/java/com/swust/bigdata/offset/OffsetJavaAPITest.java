package com.swust.bigdata.offset;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 基于Java API 实现数据对接
 */
public class OffsetJavaAPITest {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf();
        conf.setMaster("local[2]")
                .setAppName(OffsetJavaAPITest.class.getSimpleName());

        JavaStreamingContext jssc = new JavaStreamingContext(conf, new Duration(2000));
        jssc.sparkContext().setLogLevel("Error");


        // 设置kafka的配置文件信息
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", "hadoop000:9092,hadoop000:9093,hadoop000:9094");
        // 要注意导的序列化包必须是Kafka支持的序列化
        config.put("key.deserializer", StringDeserializer.class);
        config.put("value.deserializer", StringDeserializer.class);
        config.put("group.id", "nfy");
        config.put("auto.offset.reset", "latest");
        config.put("enable.auto.commit", false);


        // 将topic传入集合
//        String[] topic = new String[]{"my-replicated-topic"};
        Collection<String> topics = Arrays.asList("my-replicated-topic");

        // 通过KafkaUtils实现数据对接
        JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, config)
        );
        stream.foreachRDD(rdd -> {
            rdd.foreach(line -> {
                System.out.println(line);
            });
        });


        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
