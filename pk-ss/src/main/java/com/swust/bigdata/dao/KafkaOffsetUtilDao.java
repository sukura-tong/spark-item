package com.swust.bigdata.dao;

import com.swust.bigdata.pojo.KafkaOffset;

import java.util.List;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function kafka offset 业务逻辑 数据库dao层
 */
public interface KafkaOffsetUtilDao {
    List<KafkaOffset> selectAll();

    // 单条插入
    int insertData(KafkaOffset params);

    // 批量样本插入
    int insertBatchs(List<KafkaOffset> lists);

    int deleteData(KafkaOffset params);

    int updateData(KafkaOffset params);

    int deleteAll();

    // 插入或更新
    int insertOrUpdate(KafkaOffset params);

    // 特定查询
    List<KafkaOffset> selectByGroupIdAndTopic(KafkaOffset params);
}
