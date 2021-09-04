package com.imooc.bigdata.ss

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Duration, Seconds, StreamingContext}

/**
 * 使用 SparkStreaming 从HDFS中读取数据
 */
object FileWordCountApp {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
    sparkConf.setMaster("local[2]").setAppName(FileWordCountApp.getClass.getName)
    val ssc = new StreamingContext(sparkConf, Seconds(5))
    ssc.sparkContext.setLogLevel("Error")

    // 该数据源头代码有一些问题
    var path: String = "hdfs://192.168.9.198:8040/input/stream"
    //从HDFS读取数据
    val inputValue: DStream[String] = ssc.textFileStream(path)

    val resultValue: DStream[(String, Int)] = inputValue
      .flatMap(_.split(","))
      .map((_, 1))
      .reduceByKey(_ + _)
    resultValue.foreachRDD(_.foreach(print(_)))
    resultValue.count()
    ssc.start()
    ssc.awaitTermination()
  }
}
