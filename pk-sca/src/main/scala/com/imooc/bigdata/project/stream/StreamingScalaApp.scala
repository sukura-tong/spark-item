package com.imooc.bigdata.project.stream

import com.imooc.bigdata.project.utils.DateUtils
import com.imooc.bigdata.project.utils.GetStreamConfigParams.getStreamConfigParamsUtils
import com.imooc.bigdata.utils.HbaseClientAPIUtils
import org.apache.hadoop.hbase.client.Table
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Duration, StreamingContext}

object StreamingScalaApp {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName(this.getClass.getName)
    val jssc = new StreamingContext(conf, new Duration(1000))
    jssc.sparkContext.setLogLevel("Error")

    val groupId = "xiaolu"
    val params: Map[String, Object] = getStreamConfigParamsUtils(groupId)

    val topics = Array("access-topic-producer")

    // 获取流数据
    val input: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(
      jssc,
      PreferConsistent,
      Subscribe[String, String](topics, params)
    )
    //    1606829727740	0	36.56.226.195	6	65	user6	5469	1.1.2	Android
    /** *
     * 日期
     * 时长
     * 用户名
     */
    val logStream: DStream[(String, Long, String)] = input.map(elements => {
      val datas: String = elements.value()
      val words: Array[String] = datas.split("\t")
      val time: String = DateUtils.parseToHour(words(0).trim)
      (time, words(1).trim.toLong, words(5).trim)
    })

    //统计用户每个小时的访问时长
    val logStreamMap: DStream[((String, String), Long)] = logStream.map(line => {
      val time: String = line._1
      val value: Long = line._2
      val user: String = line._3

      val key: (String, String) = (time, user)
      (key, value)
    })


    // 按照key聚合
    val logResult: DStream[((String, String), Long)] = logStreamMap.reduceByKey((one, two) => {
      one + two
    })

    // 将结果写入到Hbase
    logResult.foreachRDD(eachRdd => {
      // 流处理每个partition进行一次处理
      eachRdd.foreachPartition(ecahPartition => {
        val table: Table = HbaseClientAPIUtils.getTableName("access-log-hour")
        // 将每个partition里的数据追加到Hbase数据库内
        ecahPartition.foreach(line => {
          //byte[] row, byte[] family, byte[] qualifier long amount
          // 追加数据 不是简单的更新
          table.incrementColumnValue(
            (line._1._1 + "_" + line._1._2).getBytes(),
            "feature".getBytes(),
            "time".getBytes(),
            line._2
          )
        })
        table.close()
      })
    })

    jssc.start()
    jssc.awaitTermination()

  }
}
