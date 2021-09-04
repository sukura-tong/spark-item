package com.imooc.bigdata.ss

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}

object TransformApp {
  def main(args: Array[String]): Unit = {
    // Dstream 与 RDD 交互
    val conf: SparkConf = new SparkConf().setMaster("local[2]").setAppName(this.getClass.getSimpleName)
    val ssc = new StreamingContext(conf, Seconds(5))
    ssc.sparkContext.setLogLevel("Error")

    // 过滤数据
    val data = List("lqx")
    val filterInitRdd: RDD[String] = ssc.sparkContext.parallelize(data)
    val valueInitTupleRdd: RDD[(String, Boolean)] = filterInitRdd.map((_, true))


    // 对接9527数据
    // get data
    // 123333,lqx
    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop000", 4280)
    val changeRdd: DStream[(String, String)] = lines.map(x => (x.split(",")(1), x))
    // Dstream 和 RDD 做Join
    val joinRdd: DStream[(String, (String, Option[Boolean]))] = changeRdd.transform(rdd => {
      rdd.leftOuterJoin(valueInitTupleRdd)
    })
    val filterRDD: DStream[(String, (String, Option[Boolean]))] = joinRdd.filter(rdd => {
      rdd._2._2.getOrElse(false) != true
    })
    filterRDD.foreachRDD(rdd => {
      rdd.foreach(print(_))
    })
    ssc.start()
    ssc.awaitTermination()
  }


}
