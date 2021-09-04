package com.imooc.bigdata.offset

import org.apache.spark.{SparkConf, TaskContext}
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.api.java.{JavaInputDStream, JavaStreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object ShowOffset {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName(this.getClass.getSimpleName)
      .setMaster("local[2]")

    val jssc = new JavaStreamingContext(conf, new Duration(2000))
    jssc.sparkContext.setLogLevel("Error")

    val params = Map[String, Object](
      "bootstrap.servers" -> "hadoop000:9092,hadoop000:9093,hadoop000:9094",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "first",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("nfy-replicated-topic")

    val stream: JavaInputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      jssc,
      PreferConsistent,
      Subscribe[String, String](topics, params)
    )

    stream.foreachRDD { ds =>
      // ds 传入类型是JavaRdd  需要转化为rdd的
      val offsetRanges = ds.rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      ds.foreachPartition { iter =>
        val o: OffsetRange = offsetRanges(TaskContext.get.partitionId)
        println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}")
      }
    }


    jssc.start()
    jssc.awaitTermination()

  }
}
