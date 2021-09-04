package com.imooc.bigdata.sql

import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkSqlAppLearn {
  def main(args: Array[String]): Unit = {
    val session: SparkSession = SparkSession.builder()
      .appName(this.getClass.getName)
      .master("local[2]")
      .getOrCreate()

    session.sparkContext.setLogLevel("Error")


    // 读取外部数据
    val inputDataFrame: DataFrame = session.read.format("json").load("./pk-ss/data/employees.json")

    // show
    inputDataFrame.show()
    inputDataFrame.printSchema()


    // TO do ...API & SQL
    // 注册成临时表 使用sql语句进行过滤查取功能
    val tableName = "employee"
    inputDataFrame.createOrReplaceTempView(tableName);

    // 使用sql实现数据查询功能
    val filterDFrame: DataFrame = session.sql(
      """
        |select
        |name,salary
        |from
        |employee
        |where salary > 3000 and salary <= 4000
        |
        |""".stripMargin
    )

    filterDFrame.show()


    session.stop()
  }
}
