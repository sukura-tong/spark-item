package com.imooc.bigdata.utils

import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.kafka010.OffsetRange
import scalikejdbc.{DB, SQL}

object MysqlOffsetManager extends OffsetsManager {

  override def obtainOffsets(groupID: String, topics: Array[String]): Map[TopicPartition, Long] = {
    val metaOffsets: List[(TopicPartition, Long)] = DB.readOnly {
      implicit session => {
        SQL(
          """
            |select * from offset_storage
            |where
            |topic = ?
            |and
            |groupid = ?
            |""".stripMargin)
          .bind(topics.head, groupID)
          .map(rs => {
            val key = new TopicPartition(rs.string("topic"), rs.int("partitions"))
            val value: Long = rs.long("offset")
            (key, value)
          }).list()
          .apply()
      }
    }
    val map: Map[TopicPartition, Long] = metaOffsets.toMap
    map
  }

  override def storeOffsets(groupID: String, offsetRanges: Array[OffsetRange]): Unit = {
    offsetRanges.map(x => {
      DB.autoCommit {
        implicit session =>
          SQL(
            """
              |insert
              |into
              |offset_storage(topic,groupid,partitions,offset)
              |values (?, ?, ?, ?)
              |on duplicate key
              |update
              |offset = ?
              |""".stripMargin)
            .bind(x.topic, groupID, x.partition, x.untilOffset, x.untilOffset)
            .update()
            .apply()
      }
    })
  }
}
