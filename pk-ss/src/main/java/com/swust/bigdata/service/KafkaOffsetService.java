package com.swust.bigdata.service;

import com.swust.bigdata.pojo.KafkaOffset;
import org.apache.ibatis.annotations.AutomapConstructor;
import org.springframework.context.ApplicationContext;

import java.util.List;


public interface KafkaOffsetService {
    // 查询功能
    List<KafkaOffset> selectAll(ApplicationContext context);

    // 插入一条样本
    int insertData(ApplicationContext context, KafkaOffset params);

    // 批量样本插入
    int insertBatchs(ApplicationContext context, List<KafkaOffset> lists);

    // 删除一条样本
    int deleteData(ApplicationContext context, String topic, String groupId, int partitionId);

    // 更新样本
    int updateData(ApplicationContext context, KafkaOffset params);

    // 清空表
    int deleteAll(ApplicationContext context);

    // 插入或更新
    int insertOrUpdate(ApplicationContext context, KafkaOffset params);

    // 特定条件查询
    List<KafkaOffset> selectByGroupAndTopic(ApplicationContext context, KafkaOffset params);
}
