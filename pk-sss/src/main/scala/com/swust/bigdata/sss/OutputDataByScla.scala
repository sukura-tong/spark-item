package com.swust.bigdata.sss

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{DataFrame, Dataset, ForeachWriter, Row, SparkSession}

/** *
 * 功能可以实现
 * 但是效率过低 简直垃圾
 */
object OutputDataByScla {
  def main(args: Array[String]): Unit = {

    val session: SparkSession = SparkSession.builder()
      .master("local[2]")
      .appName(this.getClass.getSimpleName)
      .getOrCreate()
    session.sparkContext.setLogLevel("Error")
    saveDataToMysql(session)
  }

  def saveDataToKafka(session: SparkSession) = {

    val lines: DataFrame = session.readStream
      .option("host", "hadoop000")
      .option("port", 9999)
      .format("socket")
      .load()

    import session.implicits._
    val metas: Dataset[String] = lines.as[String]

    val results: DataFrame = metas.flatMap(_.split(","))
      .groupBy("value")
      .count()

    results.writeStream
      .format("kafka")
      .outputMode(OutputMode.Update())
      .option("kafka.bootstrap.servers", "hadoop000:9092")
      .option("checkpointLocation", "./pk-sss/kafkacheckpoint")
      .option("topic", "access-sss-topic")
      .start()
      .awaitTermination()
  }


  def saveDataToFile(session: SparkSession) = {

    val lines: DataFrame = session.readStream
      .format("socket")
      .option("host", "hadoop000")
      .option("port", 9999)
      .load()


    import session.implicits._
    val datas: Dataset[String] = lines.as[String]

    val pair: Dataset[(String, String)] = datas.flatMap(_.split(","))
      .map((_, "xuetong"))

    // 注册成表
    val dataFrame: DataFrame = pair
      .toDF("word", "value")

    //    val counts: DataFrame = dataFrame.groupBy("word")
    //      .count()

    dataFrame.writeStream
      .format("json")
      //      .outputMode(outputMode = OutputMode.Complete())
      .option("path", "./pk-sss/output")
      .option("checkpointLocation", "./pk-sss/checkpoint")
      .start()
      .awaitTermination()

  }

  def saveDataToMysql(session: SparkSession) = {

    val lines: DataFrame = session.readStream
      .format("socket")
      .option("host", "hadoop000")
      .option("port", 4444)
      .load()


    import session.implicits._
    val datas: Dataset[String] = lines.as[String]

    val results: DataFrame = datas.flatMap(_.split(","))
      .groupBy("value")
      .count()

    results.repartition(2)
      .writeStream
      .outputMode(outputMode = OutputMode.Complete())
      .foreach(new ForeachWriter[Row] {

        var connection: Connection = _
        var pstmt: PreparedStatement = _
        var batchCount = 0

        override def open(partitionId: Long, epochId: Long) = {
          println(s"open ${connection} and ${pstmt} ...")
          Class.forName("com.mysql.jdbc.Driver")
          connection = DriverManager.getConnection("jdbc:mysql://hadoop000:3306/nfy", "root", "root")

          val sql = "insert into t_words (word,num) value (?,?) " + "on duplicate key update " + "word = ?, num = ?";

          pstmt = connection.prepareStatement(sql)
          if (connection == null) {
            false
          } else {
            true
          }
        }

        override def process(value: Row): Unit = {
          val word: String = value.getString(0)
          val num: Int = value.getLong(1).toInt

          println(s"word = ${word} and num = ${num} ...")

          pstmt.setString(1, word)
          pstmt.setInt(2, num)
          pstmt.setString(3, word)
          pstmt.setInt(4, num)

          // 将数据加入到batch里
          pstmt.addBatch()

          batchCount += 1
          if (batchCount >= 10) {
            pstmt.executeBatch()
            batchCount = 0
          }

        }

        override def close(errorOrNull: Throwable): Unit = {
          // 将未到10条的数据处理掉
          pstmt.executeBatch()
          batchCount = 0
          connection.close()
        }
      })
      .start()
      .awaitTermination()
  }

}
