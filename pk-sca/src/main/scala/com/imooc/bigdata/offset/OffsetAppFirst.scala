package com.imooc.bigdata.offset

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Duration, StreamingContext}

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object OffsetAppFirst {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName(this.getClass.getSimpleName)
      .setMaster("local[2]")
    val jssc = new StreamingContext(conf, new Duration(8000))

    jssc.sparkContext.setLogLevel("Error")


    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "hadoop000:9092,hadoop000:9093,hadoop000:9094",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "pk-spark-group-1",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topics = Array("nfy-replicated-topic")
    val stream = KafkaUtils.createDirectStream[String, String](
      jssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    //    stream.map(record => (record.key, record.value))


    stream.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        rdd.foreach(line => {
          println(line)
        })
      }
    })

    jssc.start()
    jssc.awaitTermination()

  }
}
