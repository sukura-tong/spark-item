package com.swust.bigdata.utils;

import com.swust.bigdata.pojo.KafkaOffset;
import com.swust.bigdata.service.KafkaOffsetService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function kafka 偏移量 jdbc工具类
 */
public class OffsetJdbcUtils {
    /***
     * 获取Context上下文对象
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        return context;
    }

    /**
     * 查询数据
     *
     * @return
     */
    public static List<KafkaOffset> selectAll() {
        ApplicationContext context = getApplicationContext();
        KafkaOffsetService service = (KafkaOffsetService) context.getBean("KafkaOffsetService");
        List<KafkaOffset> offsetList = service.selectAll(context);
        return offsetList;
    }

    public static List<KafkaOffset> selectByGroupAndTopic(String groupId, String topic) {
        ApplicationContext context = getApplicationContext();
        KafkaOffsetService service = (KafkaOffsetService) context.getBean("KafkaOffsetService");
        // 创建查询对象
        KafkaOffset params = new KafkaOffset();
        params.setGroupid(groupId);
        params.setTopic(topic);
        List<KafkaOffset> list = service.selectByGroupAndTopic(context, params);
        return list;
    }

    /**
     * 插入一条数据
     *
     * @param params
     * @return
     */
    public static int insertOne(KafkaOffset params) {
        ApplicationContext context = getApplicationContext();
        KafkaOffsetService service = (KafkaOffsetService) context.getBean("KafkaOffsetService");
        int res = service.insertData(context, params);
        // 自动提交事务
        return res;
    }

    public static int insertBatchs(List<KafkaOffset> params) {
        ApplicationContext context = getApplicationContext();
        KafkaOffsetService service = (KafkaOffsetService) context.getBean("KafkaOffsetService");
        int res = service.insertBatchs(context, params);
        return res;
    }

    /**
     * 删除一条数据
     *
     * @param topic
     * @param groupId
     * @param partitionId
     * @return
     */
    public static int delectOne(String topic, String groupId, int partitionId) {
        ApplicationContext context = getApplicationContext();
        KafkaOffsetService service = (KafkaOffsetService) context.getBean("KafkaOffsetService");
        int res = service.deleteData(context, topic, groupId, partitionId);
        return res;
    }

    public static int deleteAll() {
        ApplicationContext context = getApplicationContext();
        KafkaOffsetService service = (KafkaOffsetService) context.getBean("KafkaOffsetService");
        int res = service.deleteAll(context);
        return res;
    }

    /**
     * 更新数据的offset
     *
     * @param params
     * @return
     */
    public static int updateOne(KafkaOffset params) {
        ApplicationContext context = getApplicationContext();
        KafkaOffsetService service = (KafkaOffsetService) context.getBean("KafkaOffsetService");
        int res = service.updateData(context, params);
        return res;
    }

    public static int insertOrUpdate(KafkaOffset params) {
        ApplicationContext context = getApplicationContext();
        KafkaOffsetService service = (KafkaOffsetService) context.getBean("KafkaOffsetService");
        int res = service.insertOrUpdate(context, params);
        return res;
    }

    public static void tets(String[] args) {
        deleteAll();

        KafkaOffset offset = new KafkaOffset();
        offset.setTopic("pk");
        offset.setGroupid("test");
        offset.setPartitions(0);
        offset.setOffset(99);

        insertOrUpdate(offset);

        List<KafkaOffset> lists = selectAll();
        for (KafkaOffset list : lists) {
            System.out.println(list.getTopic() + "--"
                    + list.getGroupid() + "--"
                    + list.getPartitions() + "--"
                    + list.getOffset());
        }

        System.out.println();

        KafkaOffset offset2 = new KafkaOffset();
        offset2.setTopic("pk");
        offset2.setGroupid("test");
        offset2.setPartitions(0);
        offset2.setOffset(199);


        insertOrUpdate(offset2);

        List<KafkaOffset> lists1 = selectAll();
        for (KafkaOffset list : lists1) {
            System.out.println(list.getTopic() + "--"
                    + list.getGroupid() + "--"
                    + list.getPartitions() + "--"
                    + list.getOffset());
        }

        System.out.println();

        KafkaOffset offset3 = new KafkaOffset();
        offset3.setTopic("pk");
        offset3.setGroupid("test");
        offset3.setPartitions(1);
        offset3.setOffset(199);


        insertOrUpdate(offset3);

        List<KafkaOffset> lists2 = selectAll();
        for (KafkaOffset list : lists2) {
            System.out.println(list.getTopic() + "--"
                    + list.getGroupid() + "--"
                    + list.getPartitions() + "--"
                    + list.getOffset());
        }
    }
}
