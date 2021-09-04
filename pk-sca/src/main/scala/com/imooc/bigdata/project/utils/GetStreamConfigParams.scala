package com.imooc.bigdata.project.utils

import org.apache.kafka.common.serialization.StringDeserializer

object GetStreamConfigParams {
  def getStreamConfigParamsUtils(groupID: String) = {
    val params = Map[String, Object](
      "bootstrap.servers" -> "hadoop000:9092,hadoop000:9093,hadoop000:9094",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> groupID,
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    params
  }
}
