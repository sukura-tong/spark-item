package com.imooc.bigdata.ss

import java.sql.Connection

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Duration, StreamingContext}

/**
 * 将数据写入到数据库内
 */
object ForeachRDDToMysql {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName(this.getClass.getName).setMaster("local[2]")
    val jssc = new StreamingContext(conf, new Duration(1000))
    jssc.sparkContext.setLogLevel("Error")

    // get data
    val hostname = "hadoop000"
    val port = 4280
    val inputDstream: ReceiverInputDStream[String] = jssc.socketTextStream(hostname = hostname, port = port)

    // data trans
    val reduceResult: DStream[(String, Int)] = inputDstream.flatMap(_.split(",")).map((_, 1)).reduceByKey(_ + _)
    //  将每一批次的结果写入数据库内
    reduceResult.foreachRDD(rdd => {
      // 每个分区获取一次连接对象
      rdd.foreachPartition(partion => {
        val connection = MysqlUtislScala.getConnection()
        partion.foreach(line => {
          val word = line._1
          val num = line._2
          val sql = s"insert into t_words(word, num) values (${word},${num})"
          // 执行sql命令
          val bool: Boolean = connection.createStatement().execute(sql)
          if (bool) {
            print("insert ok!!!")
          }
        })
        MysqlUtislScala.close(connection)
      })
    })


    jssc.start()
    jssc.awaitTermination()
  }

}
