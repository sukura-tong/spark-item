package com.swust.bigdata.service.impl;

import com.swust.bigdata.pojo.KafkaOffset;
import com.swust.bigdata.service.KafkaOffsetManagerService;
import com.swust.bigdata.utils.OffsetJdbcUtils;
import org.apache.kafka.common.TopicPartition;
import org.apache.spark.streaming.kafka010.OffsetRange;

import java.util.*;

public class KafkaOffsetManagerServiceImpl implements KafkaOffsetManagerService {
    @Override
    public Map<TopicPartition, Long> obtainOffsets(String consumerName, Collection<String> topics) {
        //read offset from mysql
        String topicName = topics.iterator().next();
        List<KafkaOffset> metaOffsets = OffsetJdbcUtils.selectByGroupAndTopic(consumerName, topicName);

        List<Map<TopicPartition, Long>> metas = new ArrayList<>();
        for (KafkaOffset metaOffset : metaOffsets) {
            String topic = metaOffset.getTopic();
            String groupid = metaOffset.getGroupid();
            int partitions = metaOffset.getPartitions();
            long offset = metaOffset.getOffset();

            // 构建初始Map
            Map<TopicPartition, Long> meta = new HashMap<>();
            TopicPartition topicPartition = new TopicPartition(topic, partitions);
            meta.put(topicPartition, offset);
//            System.out.println(meta);
            metas.add(meta);
        }
        //将数据转为map
        Map<TopicPartition, Long> offsetStart = metas.get(0);
        return offsetStart;
    }

    @Override
    public void storeOffsets(String consumerName, OffsetRange[] offsetRanges) {
// 保存偏移量到数据库内
        List<OffsetRange> dataOffsets = Arrays.asList(offsetRanges);

        for (OffsetRange line : dataOffsets) {
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
}
