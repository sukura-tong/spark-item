//package test;
//
//import com.swust.bigdata.dao.KafkaOffsetUtilDao;
//import com.swust.bigdata.intilog.LogRecordInit;
//import com.swust.bigdata.pojo.KafkaOffset;
//import org.apache.ibatis.io.Resources;
//import org.apache.ibatis.session.SqlSession;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.apache.ibatis.session.SqlSessionFactoryBuilder;
//
//import java.io.InputStream;
//import java.util.List;
//
//public class MyBatisTest {
//    public static void main(String[] args) throws Exception {
//
//        LogRecordInit init = new LogRecordInit();
//        init.initLog();
//
//        // 读取配置文件
//        InputStream in = Resources.getResourceAsStream("mybatis-config.xml");
//        // 创建SqlSessionFactory工厂
//        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
//        SqlSessionFactory factory = builder.build(in);
//        //3.使用工厂生产一个SqlSession对象
//        SqlSession session = factory.openSession();
//        //4.使用SqlSession创建dao接口的代理对象
//        KafkaOffsetUtilDao mapper = session.getMapper(KafkaOffsetUtilDao.class);
//        //5.使用代理对象执行方法
//        List<KafkaOffset> datas = mapper.selectAll();
//
//        for (KafkaOffset data : datas) {
//            System.out.println(data.getOffset());
//        }
//        //6.释放资源
//        session.close();
//        in.close();
//    }
//}
