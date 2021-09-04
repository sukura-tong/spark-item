package com.imooc.bigdata.offset

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Duration, StreamingContext}

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

/** *
 * checkpoint 只能在代码未发生变化时才可以恢复offset
 * 未升级代码
 * 这种操作方式是错误的
 */
object CheckPointScala {
  val checkPath = "./pk-ss/offsets/checkpoints"

  def main(args: Array[String]): Unit = {
    // 通过目录恢复ssc
    val ssc = StreamingContext.getOrCreate(checkpointPath = checkPath, functionToCreateContext _)
    ssc.sparkContext.setLogLevel("Error")
    ssc.start()
    ssc.awaitTermination()
  }

  def functionToCreateContext(): StreamingContext = {
    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName(this.getClass.getSimpleName)
    val jssc = new StreamingContext(conf, new Duration(10000))
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

    stream.foreachRDD(rdd => {
      val num = rdd.count()
      println("-------" + num)
    })

    jssc.checkpoint(checkPath)
    jssc
  }
}
