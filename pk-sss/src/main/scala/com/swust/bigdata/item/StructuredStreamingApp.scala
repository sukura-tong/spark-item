package com.swust.bigdata.item

import java.sql.Timestamp

import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{DataFrame, Dataset, ForeachWriter, Row, SparkSession}
import redis.clients.jedis.Jedis

object StructuredStreamingApp {
  def main(args: Array[String]): Unit = {
    val session: SparkSession = SparkSession.builder()
      .master("local[2]")
      .appName(this.getClass.getSimpleName)
      .getOrCreate()

    session.sparkContext.setLogLevel("Error")


    // read
    val lines: DataFrame = readDataFromKafka(session)

    // clear
    val metas: Dataset[Row] = clearData(session, lines)
    putDataToRedis(metas)
  }

  def putDataToRedis(datas: Dataset[Row]) = {

    datas.writeStream
      .outputMode(outputMode = OutputMode.Append())
      .foreach(new ForeachWriter[Row] {

        var jedis: Jedis = _

        override def open(partitionId: Long, epochId: Long) = {
          jedis = RedisUtils.getJedisClient
          jedis != null
        }

        override def process(value: Row): Unit = {
          val timestamp: String = value.getTimestamp(0).toString
          val day: String = value.getString(1)
          val province: String = value.getString(2)


          jedis.hset("day-province-cnts-" + day, province, timestamp)
        }

        override def close(errorOrNull: Throwable): Unit = {
          if (jedis != null) {
            jedis.close()
          }
        }
      })
      .option("checkpointLocation", "./pk-sss/check/scala/points")
      .start()
      .awaitTermination()

  }

  def showDataByConsole(datas: Dataset[Row]) = {
    datas.writeStream
      .format("console")
      .outputMode(OutputMode.Append())
      .start()
      .awaitTermination()
  }

  def clearData(session: SparkSession, lines: DataFrame) = {

    import session.implicits._

    val metas: Dataset[Row] = lines.as[String]
      .map(x => {
        val words: Array[String] = x.split("\t")
        val time: String = words(0)
        val ip = words(2)

        val timestamp = new Timestamp(time.toLong)
        val day: String = DateUtils.parseToDay(time)
        val province: String = IPUtils.parseIP(ip)

        (timestamp, day, province)
      }).toDF("timestamp", "day", "province")
      .withWatermark("timestamp", "10 minutes")

    metas
  }

  def readDataFromKafka(session: SparkSession) = {
    val loads: DataFrame = session.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "hadoop000:9092,hadoop000:9093,hadoop000:9094")
      .option("subscribe", "access-log-nfy") //  订阅topic
      .option("startingOffsets", "earliest")
      //      .option("startingOffsets", """{"access-log-nfy":{"0":21}}""")
      .load()

    import session.implicits._
    val lines: DataFrame = loads.selectExpr("CAST(value AS STRING)")
    lines
  }
}
