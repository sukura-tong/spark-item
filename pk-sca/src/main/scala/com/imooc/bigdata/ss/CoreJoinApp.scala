package com.imooc.bigdata.ss

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

object CoreJoinApp {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf()
      .setAppName(this.getClass.getName)
      .setMaster("local[2]")
    val sc = new SparkContext(config = conf)
    sc.setLogLevel("Error")

    // fliter data
    val list = new ListBuffer[(String, Boolean)]()
    list.append(("lqx", true))

    // paralllize to rdd
    val listRdd: RDD[(String, Boolean)] = sc.parallelize(list)

    // init data
    val init = new ListBuffer[(String, String)]()
    init.append(("lqx", "2020,lqx"))
    init.append(("xll", "1997,xll"))
    val initRdd: RDD[(String, String)] = sc.parallelize(init)

    // dispose
    // join
    val filterRdd: RDD[(String, (String, Option[Boolean]))] = initRdd.leftOuterJoin(listRdd)
    filterRdd.foreach(print(_))
    val filter: RDD[(String, (String, Option[Boolean]))] = filterRdd.filter(line => {
      val tip: Option[Boolean] = line._2._2
      tip.getOrElse(false) != true
    })
    val value: RDD[String] = filter.map(_._2._1)
    value.foreach(println(_))

    sc.stop()

  }
}
