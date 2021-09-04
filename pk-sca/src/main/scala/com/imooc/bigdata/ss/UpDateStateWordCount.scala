package com.imooc.bigdata.ss

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object UpDateStateWordCount {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName(this.getClass.getName).setMaster("local[2]")
    val context = new StreamingContext(conf, Seconds(5))
    // 实现数据累加 将历史数据存放到该目录
    context.checkpoint("pk-ss")


    val inputDs: ReceiverInputDStream[String] = context.socketTextStream("hadoop000", 4280)

    inputDs
      .flatMap(_.split(","))
      .map((_, 1))
      .updateStateByKey(UpdateFunction _)
      .foreachRDD(rdd => {
        rdd.foreach(print(_))
      })

    // updateByState


    context.start()
    context.awaitTermination()
  }

  // state function
  def UpdateFunction(newValues: Seq[Int], runningCount: Option[Int]): Option[Int] = {
    // 使用新值结合老的数据进行function操作
    val current: Int = newValues.sum
    val old: Int = runningCount.getOrElse(0)
    Some(current + old)
  }
}
