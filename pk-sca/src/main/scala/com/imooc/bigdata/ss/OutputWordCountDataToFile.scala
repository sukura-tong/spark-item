package com.imooc.bigdata.ss

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 将数据运算结果存储到外部系统
 * 文件系统
 * 数据库
 */
object OutputWordCountDataToFile {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName(this.getClass.getName)

    val jsc = new StreamingContext(conf, Seconds(5))
    jsc.sparkContext.setLogLevel("Error")

    val inputValueDS: ReceiverInputDStream[String] = jsc.socketTextStream("hadoop000", 4280)

    val addResult: DStream[(String, Int)] = inputValueDS
      .flatMap(x => x.split(","))
      .map((_, 1))
      .reduceByKey(_ + _)

    // action
    addResult.count()

    // saveAsTextFile
    val prefix = "./pk-ss/save/scala"
    addResult.saveAsTextFiles(prefix = prefix)


    // start
    jsc.start()
    jsc.awaitTermination()
  }
}
