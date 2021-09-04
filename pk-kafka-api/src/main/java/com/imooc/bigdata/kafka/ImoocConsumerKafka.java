package com.imooc.bigdata.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;


public class ImoocConsumerKafka {
    public static void main(String[] args) {

        Properties props = new Properties();

        props.setProperty("bootstrap.servers", "hadoop000:9092");
        props.setProperty("group.id", "pk");
        // 自动提交偏移量
        props.setProperty("enable.auto.commit", "false");
        // 反序列化
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        //  让这个消费者去订阅nfy这个topic
        consumer.subscribe(Arrays.asList("nfy"));

        // 源源不断
        System.out.println();

        while (true) {
            Duration timeout = Duration.ofMillis(1000);
            ConsumerRecords<String, String> records = consumer.poll(timeout);
            for (ConsumerRecord record : records) {
                System.out.println(record.key() + "\t" +
                        record.topic() + "\t" +
                        record.value());
            }
        }
    }
}
