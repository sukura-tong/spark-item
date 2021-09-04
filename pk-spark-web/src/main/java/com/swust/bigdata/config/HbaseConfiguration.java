package com.swust.bigdata.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.hbase.HbaseTemplate;

/**
 * 将类交给 Spring管理
 */
@Configuration
public class HbaseConfiguration {

    // 使用value注解将配置文件里的内容进行注入
//    @Value("${hbase.zookeeper.quorum}")
    private String zookeeperQuorum = "hadoop000";

//    @Value("${hbase.zookeeper.property.clientPort}")
    private String clientPort = "2181";

//    @Value("${zookeeper.znode.parent}")
    private String znodeParent = "/hbase";

    /**
     * 构建hbase模板
     * @return
     */
    @Bean
    public HbaseTemplate hbaseTemplate(){

        org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
        configuration.set("hbase.zookeeper.quorum",zookeeperQuorum);
        configuration.set("hbase.zookeeper.property.clientPort",clientPort);
        configuration.set("zookeeper.znode.parent",znodeParent);

        HbaseTemplate hbaseTemplate = new HbaseTemplate(configuration);

        if (hbaseTemplate == null){
            System.out.println("error");
        }
        return hbaseTemplate;
    }


}
