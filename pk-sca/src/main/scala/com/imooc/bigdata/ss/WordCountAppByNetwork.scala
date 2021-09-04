package com.imooc.bigdata.ss

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}

/**
 * 完成基于SparkStreaming的词频统计分析
 * 1 saprkConf ==> ssc
 * 2 业务逻辑
 */
object WordCountAppByNetwork {
  def main(args: Array[String]): Unit = {
    // 入口点
    val conf = new SparkConf()
      .setAppName(this.getClass.getName)
      .setMaster("local[2]")
    // 指定间隔5秒为一个批次
    val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))
    // 设置日志级别
    ssc.sparkContext.setLogLevel("Error")


    // TODO 对接网络数据
    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop000", 9527)

    //   scala 快速写法
    //   val value: DStream[(String, Int)] = lines.flatMap(_.split(",")).map((_, 1)).reduceByKey(_ + _)
    //   value.print()

    // TODO 业务逻辑处理
    //    println(lines.flatMap(_.split(",")).map((_, 1)).reduceByKey(_ + _))

    val wordRdd: DStream[String] = lines.flatMap(line => {
      val splits: Array[String] = line.split(",")
      splits
    })
    val wrodValueRdd: DStream[(String, Int)] = wordRdd.map(line => {
      (line, 1)
    })
    val resultRdd: DStream[(String, Int)] = wrodValueRdd.reduceByKey((line1, line2) => {
      line1 + line2
    })
    //打印
    resultRdd.print()
    //启动流处理
    ssc.start()
    ssc.awaitTermination()
  }

}
