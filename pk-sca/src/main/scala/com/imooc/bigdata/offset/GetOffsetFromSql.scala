package com.imooc.bigdata.offset

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.api.java.{JavaInputDStream, JavaStreamingContext}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.apache.spark.{SparkConf, TaskContext}
import scalikejdbc.config.DBs
import scalikejdbc.{DB, HasExtractor, SQL, SQLToList}
import com.imooc.bigdata.utils.MysqlOffsetManager
import com.imooc.bigdata.utils.MysqlOffsetManager.{obtainOffsets, storeOffsets}

object GetOffsetFromSql {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName(this.getClass.getSimpleName)
      .setMaster("local[2]")

    val jssc = new JavaStreamingContext(conf, new Duration(10000))
    jssc.sparkContext.setLogLevel("Error")
    // 加载配置文件
    DBs.setupAll()

    val groupID = "first"
    val params = Map[String, Object](
      "bootstrap.servers" -> "hadoop000:9092,hadoop000:9093,hadoop000:9094",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> groupID,
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics: Array[String] = Array("nfy-replicated-topic")

    // TODO ...

    // get offset from sql


    //    val metaOffsets: List[(TopicPartition, Long)] = DB.readOnly {
    //      implicit session => {
    //        SQL(
    //          """
    //            |select * from offset_storage
    //            |where
    //            |topic = ?
    //            |and
    //            |groupid = ?
    //            |""".stripMargin)
    //          .bind(topics.head, groupID)
    //          .map(rs => {
    //            val key = new TopicPartition(rs.string("topic"), rs.int("partitions"))
    //            val value: Long = rs.long("offset")
    //            (key, value)
    //          }).list()
    //          .apply()
    //      }
    //    }
    //    val map: Map[TopicPartition, Long] = metaOffsets.toMap

    val map = obtainOffsets(groupID = groupID, topics = topics)


    val offsets: Map[TopicPartition, Long] = map

    val stream: JavaInputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      jssc,
      PreferConsistent,
      Subscribe[String, String](topics, params, offsets)
    )

    stream.foreachRDD { ds =>
      // ds 传入类型是JavaRdd  需要转化为rdd的
      if (!ds.isEmpty()) {
        val offsetRanges: Array[OffsetRange] = ds.rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        ds.foreachPartition { iter =>
          val o: OffsetRange = offsetRanges(TaskContext.get.partitionId)
          println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}")
        }
        // TODO ... 业务逻辑
        // TODO ...
        //        offsetRanges.map(x =>{
        //          DB.autoCommit{
        //            implicit session =>
        //              SQL(
        //                """
        //                  |insert
        //                  |into
        //                  |offset_storage(topic,groupid,partitions,offset)
        //                  |values (?, ?, ?, ?)
        //                  |on duplicate key
        //                  |update
        //                  |offset = ?
        //                  |""".stripMargin)
        //                .bind(x.topic,groupID,x.partition,x.untilOffset,x.untilOffset)
        //                .update()
        //                .apply()
        //          }
        //        })
        // 封装
        storeOffsets(groupID, offsetRanges)

      }
    }


    jssc.start()
    jssc.awaitTermination()

  }
}
