package com.swust.bigdata.offset;

import com.swust.bigdata.dao.KafkaOffsetUtilDao;
import com.swust.bigdata.intilog.LogRecordInit;
import com.swust.bigdata.pojo.KafkaOffset;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 使用mybatis 进行数据库查询操作
 */

public class OffsetMybatisUtils {
    public static void main(String[] args) throws IOException {
        LogRecordInit init = new LogRecordInit();
        init.initLog();

        // 读取配置文件
        InputStream in = Resources.getResourceAsStream("mybatis-config.xml");
        // 创建SqlSessionFactory工厂
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory factory = builder.build(in);
        //3.使用工厂生产一个SqlSession对象
        SqlSession session = factory.openSession();
        //4.使用SqlSession创建dao接口的代理对象
        KafkaOffsetUtilDao mapper = session.getMapper(KafkaOffsetUtilDao.class);

        List<KafkaOffset> datas = new ArrayList<>();
        for (int i = 14; i < 16; i++) {
            KafkaOffset offset = new KafkaOffset();
            offset.setTopic("pk-test");
            offset.setGroupid("test-group");
            offset.setPartitions(i);
            offset.setOffset(100 + i);
            datas.add(offset);
        }

        int res = mapper.insertBatchs(datas);
        if (res > 0) {
            System.out.println("insert successfully!!!");
            List<KafkaOffset> offsets = mapper.selectAll();
            for (KafkaOffset data : offsets) {
                System.out.println(data.getTopic() + "--" + data.getGroupid() + "--" + data.getPartitions() + "--" + data.getOffset());
            }

        }
        // 事物提交
        session.commit();
        session.close();
    }
}
