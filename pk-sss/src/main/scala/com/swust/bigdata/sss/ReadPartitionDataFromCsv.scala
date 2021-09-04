package com.swust.bigdata.sss

import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * 读取csv数据
 */
object ReadPartitionDataFromCsv {
  def main(args: Array[String]): Unit = {
    val session: SparkSession = SparkSession.builder()
      .appName(this.getClass.getName)
      .master("local[2]")
      .getOrCreate()

    session.sparkContext.setLogLevel("Error")

    readPartitionCsv(session = session)

  }

  def readPartitionCsv(session: SparkSession) = {

    val userSchema: StructType = new StructType()
      .add("id", IntegerType)
      .add("name", StringType)
      .add("city", StringType)
      .add("salay", IntegerType)
    val inpath = "pk-sss/data/partition"

    val lines: DataFrame = session.readStream
      .format("csv")
      .schema(userSchema)
      .load(path = inpath)

    lines.writeStream
      .format("console")
      //      .outputMode(outputMode = OutputMode.Complete())
      .start()
      .awaitTermination()
  }
}
