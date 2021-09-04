package com.swust.bigdata.sss

import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/** *
 * 实现批流统一
 * 从socket获取数据
 * 基于sparkSQL 构建 sparkStreaming
 *
 *
 * 存在问题
 * 工程能够运行，但是延时性很高
 */
object WordCountApp {
  def main(args: Array[String]): Unit = {

    //    System.setProperty( "hadoop.home.dir" , "D:\\Hadoop\\setup\\usr\\hadoop-2.6.5" )

    val spark: SparkSession = SparkSession.builder()
      .master("local[2]")
      .appName(this.getClass.getName).getOrCreate()

    spark.sparkContext.setLogLevel("Error")

    // 导入隐式转换
    import spark.implicits._

    val lines: DataFrame = spark.readStream.format("socket")
      .option("host", "hadoop000")
      .option("port", 9999)
      .load()




    // 基于dataframe编程

    val words: Dataset[String] = lines.as[String].flatMap(_.split(" "))

    val wc: DataFrame = words.groupBy("value").count()

    val query: StreamingQuery = wc.writeStream
      .outputMode("complete")
      .format("console")
      .start()

    query.awaitTermination()

  }
}
