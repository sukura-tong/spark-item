package com.swust.bigdata.sss

import org.apache.spark.sql.functions.window
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object EventTimeWindowByScala {
  def main(args: Array[String]): Unit = {
    val session: SparkSession = SparkSession.builder()
      .master("local[2]")
      .appName(this.getClass.getName)
      .getOrCreate()

    session.sparkContext.setLogLevel("Error")
    eventTimeWindow(session)
  }

  def eventTimeWindow(session: SparkSession) = {
    val lines: DataFrame = session.readStream
      .format("socket")
      .option("host", "hadoop000")
      .option("port", 4444)
      .load()

    import session.implicits._

    val metas: Dataset[String] = lines.as[String]

    // TODO...
    val mapDatas: Dataset[(String, String)] = metas.map(x => {
      val words: Array[String] = x.split(",")
      val time: String = words(0)
      val word: String = words(1)
      (time, word)
    })

    val frameDatas: DataFrame = mapDatas.toDF("time", "word")

    val windows: DataFrame = frameDatas.groupBy(
      window($"time", "10 minutes", "5 minutes"),
      $"word"
    ).count()

    // 排序
    val results: Dataset[Row] = windows.sort("window")

    results.writeStream
      .format("console")
      .option("truncate", "false")
      .outputMode(outputMode = OutputMode.Complete())
      .start()
      .awaitTermination()


  }
}
