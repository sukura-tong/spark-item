package com.imooc.bigdata.hqf

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD

object KmeansCluster {
  def main(args: Array[String]): Unit = {
    //模板代码，指定两个线程模拟在hadoop端的分布式

    val time1=System.currentTimeMillis()
    //屏蔽日志
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("org.apache.jetty.server").setLevel(Level.OFF)
    val conf = new SparkConf().setAppName("kmeans").setMaster("spark://192.168.1.103:7077")
    val sc = new SparkContext(conf)
    //    "hdfs://192.168.1.103:9000/user/data/RandomMatrix1.txt"
    //加载数据
    val data = sc.textFile(args(0))
    //将数据切分成标志格式，并封装成linalg.Vector类型
    val parsedData: RDD[linalg.Vector] = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble)))

    //迭代次数1000次、类簇的个数2个，进行模型训练形成数据模型
    var numClusters=0
    val numIterations = 1000
    for( numClusters <- 1 to 10) {
      //进行训练
      val model = KMeans.train(parsedData, numClusters, numIterations)

      //打印数据模型的中心点
      println("四个中心的点:")
      for (point <- model.clusterCenters) {
        println("  " + point.toString)
      }

      //使用误差平方之和来评估数据模型,统计聚类错误的样本比例
      val cost = model.computeCost(parsedData)
      println("聚类错误的样本比例 = " + cost)
      val wssse = model.computeCost(parsedData)
    }

    val time2=System.currentTimeMillis()

    //对部分点做预测分类
    //  println("点（-3 -3）所属族:" + model.predict(Vectors.dense("-3 -3 -2".split(' ').map(_.toDouble))))
    //  println("点（-2 3）所属族:" + model.predict(Vectors.dense("-2 3 3.0".split(' ').map(_.toDouble))))
    //  println("点（3 3）所属族:" + model.predict(Vectors.dense("3 3 1.2".split(' ').map(_.toDouble))))
    val time= (time2-time1)/1000
    println("运行时间"+time+"s")
    sc.stop()
  }
}
