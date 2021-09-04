package com.swust.bigdata.item;

import redis.clients.jedis.Jedis;

/**
 * @author 雪瞳
 * @Slogan 忘时，忘物，忘我。
 * @Function java redis连接池
 */
public class RedisConnectionUtils {
    /**
     * 获取redis连接对象
     *
     * @return
     */
    public static Jedis redisConnection() {
        String host = "hadoop000";
        int port = 6379;
        Jedis jedis = new Jedis(host, port);

        return jedis;
    }

}
