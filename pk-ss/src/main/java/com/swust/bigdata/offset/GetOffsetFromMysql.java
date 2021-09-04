package com.swust.bigdata.offset;

import com.swust.bigdata.pojo.KafkaOffset;
import com.swust.bigdata.service.KafkaOffsetManagerService;
//import com.swust.bigdata.ss.MysqlPoolUtils;
import com.swust.bigdata.utils.OffsetJdbcUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.TaskContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 将KafkaOffset 从数据库内读取出来并创建DStream
 * 没有insert
 * 有update
 */
public class GetOffsetFromMysql {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setMaster("local[2]")
                .setAppName(GetOffsetFromMysql.class.getSimpleName());

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

        //read offset from mysql
//        String topicName = topics.iterator().next();
//        List<KafkaOffset> metaOffsets = OffsetJdbcUtils.selectByGroupAndTopic(consumerName, topicName);
//
//        List<Map<TopicPartition, Long>> metas = new ArrayList<>();
//        for (KafkaOffset metaOffset :metaOffsets){
//            String topic = metaOffset.getTopic();
//            String groupid = metaOffset.getGroupid();
//            int partitions = metaOffset.getPartitions();
//            long offset = metaOffset.getOffset();
//
//            // 构建初始Map
//            Map<TopicPartition, Long> meta = new HashMap<>();
//            TopicPartition topicPartition = new TopicPartition(topic, partitions);
//            meta.put(topicPartition,offset);
////            System.out.println(meta);
//            metas.add(meta);
//        }
//        //将数据转为map
//        Map<TopicPartition, Long> offsetStart = metas.get(0);
        ApplicationContext context = OffsetJdbcUtils.getApplicationContext();
        KafkaOffsetManagerService managerService = (KafkaOffsetManagerService) context.getBean("KafkaOffsetManagerService");

        Map<TopicPartition, Long> offsetStart = managerService.obtainOffsets(consumerName, topics);

        JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(
                jscc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, params, offsetStart)
        );

        stream.foreachRDD(ds -> {

            if (!ds.isEmpty()) {
                HasOffsetRanges ranges = (HasOffsetRanges) ds.rdd();
                OffsetRange[] offsetRanges = ranges.offsetRanges();
                ds.foreachPartition(line -> {
                    OffsetRange range = offsetRanges[TaskContext.get().partitionId()];
                    System.out.println(range.topic() + " "
                            + range.partition() + " "
                            + range.fromOffset() + " "
                            + range.untilOffset());
                });

                // TODO .

//               // 保存偏移量到数据库内
//               List<OffsetRange> dataOffsets = Arrays.asList(offsetRanges);
//
//               for (OffsetRange line : dataOffsets){
//                   int partitions = line.partition();
//                   String topic = line.topic();
//                   String groupid = consumerName;
//                   long offset = line.untilOffset();
//
//                   KafkaOffset data = new KafkaOffset();
//                   data.setTopic(topic);
//                   data.setGroupid(groupid);
//                   data.setPartitions(partitions);
//                   data.setOffset(offset);
//
//                   // 调用OffsetJdbcUtils 实现
//                   OffsetJdbcUtils.insertOrUpdate(data);
//               }
                managerService.storeOffsets(consumerName, offsetRanges);
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
