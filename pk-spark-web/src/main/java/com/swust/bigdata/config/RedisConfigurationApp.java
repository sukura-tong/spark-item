package com.swust.bigdata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author 雪瞳
 * @Slogan 忘时，忘物，忘我。
 * @Function
 * 使用配置文件注入内容
 */
//@Configuration
public class RedisConfigurationApp {

//    @Bean
    public static RedisConnectionFactory redisConnectionFactory() {

        JedisConnectionFactory connection = new JedisConnectionFactory();
        connection.setHostName("hadoop000");
        connection.setPort(6379);
        connection.setDatabase(0);
        return connection;
    }


//    @Bean
    public static RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redis = new RedisTemplate<>();
        redis.setConnectionFactory(redisConnectionFactory);
        redis.afterPropertiesSet();
        return redis;
    }
}
