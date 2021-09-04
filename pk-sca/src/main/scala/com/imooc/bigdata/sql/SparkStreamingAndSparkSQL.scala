package com.imooc.bigdata.sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Duration, StreamingContext}

object SparkStreamingAndSparkSQL {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName(this.getClass.getSimpleName)
    val jssc = new StreamingContext(conf, new Duration(1000))
    jssc.sparkContext.setLogLevel("Error")

    val inputValue: ReceiverInputDStream[String] = jssc.socketTextStream("hadoop000", 4280)
    val words: DStream[String] = inputValue.flatMap(_.split(","))

    // 使用sql进行数据处理
    words.foreachRDD(rdd => {
      // 得到sparkSession
      val session: SparkSession = SparkSession
        .builder
        .config(rdd.sparkContext.getConf)
        .getOrCreate()
      // convert RDD[String] to DataFrame
      import session.implicits._
      val frame: DataFrame = rdd.toDF("word")
      // 注册临时表
      frame.createOrReplaceTempView("wc")
      // 使用sql进行查询
      val resultDF: DataFrame = session.sql(
        """
          |select
          |word, count(*) as num
          |from
          |wc
          |group by word
          |""".stripMargin
      )
      resultDF.show()
    })

    jssc.start()
    jssc.awaitTermination()
  }
}
