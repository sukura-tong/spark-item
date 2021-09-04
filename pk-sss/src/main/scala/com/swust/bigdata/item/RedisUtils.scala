package com.swust.bigdata.item

import java.util

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

object RedisUtils {

  private val jedisPoolConfig = new JedisPoolConfig()
  jedisPoolConfig.setMaxTotal(100) //最大连接数
  jedisPoolConfig.setMaxIdle(20) //最大空闲
  jedisPoolConfig.setMinIdle(20) //最小空闲
  jedisPoolConfig.setBlockWhenExhausted(true) //忙碌时是否等待
  jedisPoolConfig.setMaxWaitMillis(500) //忙碌时等待时长 毫秒
  jedisPoolConfig.setTestOnBorrow(true) //每次获得连接的进行测试
  private val jedisPool = new JedisPool(jedisPoolConfig, "hadoop000", 6379)


  def getJedisClient: Jedis = {
    jedisPool.getResource

  }

  def returnResource(jedis: Jedis): Unit = {
    jedisPool.returnResource(jedis)
  }

  def main(args: Array[String]): Unit = {

    getJedisClient.set("nfy", "1000")
    //getJedisClient.set("stu","pk")

    //    getJedisClient.hset("imooc-user-100","name","pk")
    //    getJedisClient.hset("imooc-user-100","age","31")
    //    getJedisClient.hset("imooc-user-100","gender","m")

    //    import scala.collection.JavaConversions._
    //
    //    val result: util.Map[String, String] = getJedisClient.hgetAll("imooc-user-100")
    //
    //    for((k,v) <- result) {
    //      println(k + "-->" + v)
    //    }
  }

}
