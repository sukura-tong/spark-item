package com.swust.bigdata.offset;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.TaskContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 通过Java API显示offset偏移量
 */
public class ShowOffsetByJava {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setMaster("local[2]")
                .setAppName(ShowOffsetByJava.class.getSimpleName());
        JavaStreamingContext jssc = new JavaStreamingContext(conf, new Duration(1000));
        jssc.sparkContext().setLogLevel("Error");

        Map<String, Object> params = new HashMap<>();
        params.put("bootstrap.servers", "hadoop000:9092,hadoop000:9093,hadoop000:9094");
        params.put("key.deserializer", StringDeserializer.class);
        params.put("value.deserializer", StringDeserializer.class);
        params.put("group.id", "xiaolu");
        params.put("auto.offset.reset", "latest");
        params.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList("nfy-replicated-topic");

        JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, params)
        );

        //基于stream读取offset
        stream.foreachRDD(rdd -> {
            HasOffsetRanges ranges = (HasOffsetRanges) rdd.rdd();
            OffsetRange[] offsetRanges = ranges.offsetRanges();
            rdd.foreachPartition(line -> {
                System.out.println(line);
                OffsetRange range = offsetRanges[TaskContext.get().partitionId()];
                System.out.println(range.topic() + " "
                        + range.partition() + " "
                        + range.fromOffset() + " "
                        + range.untilOffset());
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
