package com.imooc.bigdata.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class ImoocProducerKafka {
    public static void main(String[] args) {
        // 入口点  KafkaProducer
        Properties properties = new Properties();

        // 去源码拷贝
        properties.put("bootstrap.servers", "hadoop000:9092");
        properties.put("acks", "all");
        // key value 的序列化
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int i = 0; i < 100; i++) {
            String topic = "nfy-replicated-topic";
            String key = i + "";
            String value = i + "";
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            producer.send(record);
        }
        System.out.println("send over!");
        producer.close();

        System.out.println("");
    }
}
