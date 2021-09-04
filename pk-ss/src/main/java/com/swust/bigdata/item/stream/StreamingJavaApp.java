package com.swust.bigdata.item.stream;

import com.swust.bigdata.item.choose.GetUsersTimeByDay;
import com.swust.bigdata.item.choose.GetUsersTimeByHour;
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
 * @Function 数据流清洗
 */
public class StreamingJavaApp {
    public static void main(String[] args) {
        Duration duration = new Duration(1000);
        SparkConf conf = new SparkConf()
                .setMaster(args[0])
                .setAppName(StreamingJavaApp.class.getSimpleName());
        JavaStreamingContext jsc = new JavaStreamingContext(conf, duration);

        jsc.sparkContext().setLogLevel("Error");

        String groupId = "xuetong";
        String topicName = "access-log-nfy";
        Map<String, Object> params = getStreamParams(groupId);
        Collection<String> topics = getTopics(topicName);

        JavaInputDStream<ConsumerRecord<String, String>> dataStream = getDataStream(jsc, topics, params);

        // TODO ...
        GetUsersTimeByHour.getUsersTimeByHour(dataStream, jsc);
        GetUsersTimeByDay.getUsersTimeByDay(dataStream, jsc);

        jsc.start();
        try {
            jsc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取kafka对接sparkStreaming的配置文件的配置信息
     *
     * @param groupId
     * @return
     */
    public static Map<String, Object> getStreamParams(String groupId, String zookeeperServers) {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", zookeeperServers);
        config.put("key.deserializer", StringDeserializer.class);
        config.put("value.deserializer", StringDeserializer.class);
        config.put("group.id", groupId);
        config.put("auto.offset.reset", "earliest");
        config.put("enable.auto.commit", false);
        return config;
    }

    /***
     * 获取消费者的topic
     * @param topicName
     * @return
     */
    public static Collection<String> getTopics(String topicName) {
        Collection<String> topics = Arrays.asList(topicName);
        return topics;
    }

    /**
     * 获取Dstream对象
     *
     * @param jssc
     * @param topics
     * @param config
     * @return
     */
    public static JavaInputDStream<
            ConsumerRecord<String, String>> getDataStream(JavaStreamingContext jssc,
                                                          Collection<String> topics,
                                                          Map<String, Object> config) {
        JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, config)
        );
        return stream;
    }
}
