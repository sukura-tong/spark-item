package com.swust.bigdata.sss

import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
 * read data from kafka source
 */
object ReadDataFromKafkaSource {
  def main(args: Array[String]): Unit = {
    val session: SparkSession = SparkSession.builder()
      .master("local[2]")
      .appName(this.getClass.getSimpleName)
      .getOrCreate()

    session.sparkContext.setLogLevel("Error")

    readDataByKafkaSource(session)

  }

  def readDataByKafkaSource(session: SparkSession) = {
    val lines: DataFrame = session.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "hadoop000:9092,hadoop000:9093,hadoop000:9094")
      .option("subscribe", "kafka-xiaolu-data") //  订阅topic
      .load()

    // 导入隐式转换
    import session.implicits._

    //    val metas: Dataset[(String, String)] = lines.selectExpr("CAST(key as STRING)", "CAST(value as STRING)")
    //      .as[(String, String)]

    val metas: Dataset[String] = lines.selectExpr("CAST(value as STRING)").as[String]

    val frame: DataFrame = metas.flatMap(_.split(","))
      .groupBy("value")
      .count()

    frame.writeStream
      .outputMode(outputMode = OutputMode.Complete())
      .format("console")
      .start()
      .awaitTermination()
  }
}
