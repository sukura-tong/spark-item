package com.imooc.bigdata.utils

import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.kafka010.OffsetRange

trait OffsetsManager {
  //  读 groupId 和 topic
  def obtainOffsets(groupID: String, topics: Array[String]): Map[TopicPartition, Long]

  //  写 groupId 和 offsetRanges
  def storeOffsets(groupID: String, offsetRanges: Array[OffsetRange])
}
