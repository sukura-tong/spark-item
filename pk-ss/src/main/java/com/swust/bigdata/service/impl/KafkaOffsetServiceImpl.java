package com.swust.bigdata.service.impl;

import com.swust.bigdata.dao.KafkaOffsetUtilDao;
import com.swust.bigdata.pojo.KafkaOffset;
import com.swust.bigdata.service.KafkaOffsetService;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class KafkaOffsetServiceImpl implements KafkaOffsetService {
    /**
     * 查询方法
     *
     * @param context
     * @return
     */
    @Override
    public List<KafkaOffset> selectAll(ApplicationContext context) {
        // 构建业务逻辑对象
        KafkaOffsetUtilDao kafkaOffsetUtilDaoMapper = (KafkaOffsetUtilDao) context.getBean("KafkaOffsetUtilDaoMapper");
        List<KafkaOffset> kafkaOffsets = kafkaOffsetUtilDaoMapper.selectAll();
        return kafkaOffsets;
    }

    /**
     * 插入一条样本
     *
     * @param context
     * @param params
     * @return
     */
    @Override
    public int insertData(ApplicationContext context, KafkaOffset params) {
        KafkaOffsetUtilDao kafkaOffsetUtilDaoMapper = (KafkaOffsetUtilDao) context.getBean("KafkaOffsetUtilDaoMapper");
        int res = kafkaOffsetUtilDaoMapper.insertData(params);
        if (res <= 0) {
            throw new IllegalArgumentException("insert error !!!");
        }
        return res;
    }

    @Override
    public int insertBatchs(ApplicationContext context, List<KafkaOffset> lists) {
        KafkaOffsetUtilDao kafkaOffsetUtilDaoMapper = (KafkaOffsetUtilDao) context.getBean("KafkaOffsetUtilDaoMapper");
        int res = kafkaOffsetUtilDaoMapper.insertBatchs(lists);
        return res;
    }

    @Override
    public int deleteData(ApplicationContext context, String topic, String groupId, int partitionId) {
        KafkaOffsetUtilDao kafkaOffsetUtilDaoMapper = (KafkaOffsetUtilDao) context.getBean("KafkaOffsetUtilDaoMapper");
        // 创建删除对象
        KafkaOffset delete = new KafkaOffset();
        delete.setTopic(topic);
        delete.setGroupid(groupId);
        delete.setPartitions(partitionId);

        int res = kafkaOffsetUtilDaoMapper.deleteData(delete);
        return res;
    }

    @Override
    public int updateData(ApplicationContext context, KafkaOffset params) {
        KafkaOffsetUtilDao kafkaOffsetUtilDaoMapper = (KafkaOffsetUtilDao) context.getBean("KafkaOffsetUtilDaoMapper");
        int res = kafkaOffsetUtilDaoMapper.updateData(params);
        return res;
    }

    @Override
    public int deleteAll(ApplicationContext context) {
        KafkaOffsetUtilDao kafkaOffsetUtilDaoMapper = (KafkaOffsetUtilDao) context.getBean("KafkaOffsetUtilDaoMapper");
        int res = kafkaOffsetUtilDaoMapper.deleteAll();
        return res;
    }

    @Override
    public int insertOrUpdate(ApplicationContext context, KafkaOffset params) {
        KafkaOffsetUtilDao kafkaOffsetUtilDaoMapper = (KafkaOffsetUtilDao) context.getBean("KafkaOffsetUtilDaoMapper");
        int res = kafkaOffsetUtilDaoMapper.insertOrUpdate(params);
        return res;
    }

    @Override
    public List<KafkaOffset> selectByGroupAndTopic(ApplicationContext context, KafkaOffset params) {
        KafkaOffsetUtilDao kafkaOffsetUtilDaoMapper = (KafkaOffsetUtilDao) context.getBean("KafkaOffsetUtilDaoMapper");
        List<KafkaOffset> result = kafkaOffsetUtilDaoMapper.selectByGroupIdAndTopic(params);
        return result;
    }
}
