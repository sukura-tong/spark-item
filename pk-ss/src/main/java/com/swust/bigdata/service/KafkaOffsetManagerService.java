package com.swust.bigdata.service;

import org.apache.kafka.common.TopicPartition;
import org.apache.spark.streaming.kafka010.OffsetRange;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function kafka
 * offset 的封装接口
 */
public interface KafkaOffsetManagerService {
    Map<TopicPartition, Long> obtainOffsets(String consumerName, Collection<String> topics);

    void storeOffsets(String consumerName, OffsetRange[] offsetRanges);
}
