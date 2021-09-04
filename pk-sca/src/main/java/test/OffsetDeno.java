//package test;
//
//import com.swust.bigdata.dao.KafkaOffsetUtilDao;
//import com.swust.bigdata.pojo.KafkaOffset;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//
//import java.util.List;
//
///**
// * @author 雪瞳
// * @Slogan 时钟尚且前行，人怎能就此止步！
// * @Function
// * 使用spring-mybatis整合进行数据库查询功能
// */
//public class OffsetDeno {
//    public static void main(String[] args) {
//        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
//        KafkaOffsetUtilDao mapper = (KafkaOffsetUtilDao) context.getBean("kafkaOffsetUtilDaoMapper");
//        List<KafkaOffset> offsets = mapper.selectAll();
//
//        for (KafkaOffset data : offsets){
//            System.out.println(data.getTopic() + "--" + data.getGroupid() + "--" + data.getPartitions() +"--" + data.getOffset());
//        }
//    }
//}
