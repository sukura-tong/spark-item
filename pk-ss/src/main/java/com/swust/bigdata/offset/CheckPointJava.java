package com.swust.bigdata.offset;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function0;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.StreamingContext;
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
 * @Function 使用Java API维护checkpoint
 * 需要创建JavaStreamingContext工厂
 * 维护失败 四个warning 数据积压
 */
public class CheckPointJava {
    private static String checkpointpath = "./pk-ss/offset/javachecks";

    public static void main(String[] args) {

        JavaStreamingContextFactory contextFactory = new JavaStreamingContextFactory();
        JavaStreamingContext jssc = JavaStreamingContext.getOrCreate(checkpointpath, contextFactory);
        jssc.sparkContext().setLogLevel("Error");

        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class JavaStreamingContextFactory implements Function0<JavaStreamingContext> {

    private String checkpointpath = "./pk-ss/offset/javachecks";

    @Override
    public JavaStreamingContext call() throws Exception {
        SparkConf conf = new SparkConf();
        conf.setMaster("local[2]")
                .setAppName(CheckPointJava.class.getSimpleName());

        JavaStreamingContext jssc = new JavaStreamingContext(conf, new Duration(1000));

        Map<String, Object> kafkaParams = new HashMap<>();

        kafkaParams.put("bootstrap.servers", "localhost:9092,anotherhost:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "nfy");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList("nfy-replicated-topic");

        JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
        );

        stream.foreachRDD(rdd -> {
            long count = rdd.count();
            System.out.println(count);
        });
        jssc.checkpoint(checkpointpath);
        return jssc;
    }
}
