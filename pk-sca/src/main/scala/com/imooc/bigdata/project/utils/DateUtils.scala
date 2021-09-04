package com.imooc.bigdata.project.utils

import org.apache.commons.lang3.time.FastDateFormat

object DateUtils {
  // 线程安全
  val TARGET_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd-HH")

  // 解析方法
  def parseToHour(time: String) = {
    val date: String = TARGET_FORMAT.format(time.toLong)
    date
  }

  //  def main(args: Array[String]): Unit = {
  //    val time = "1606829727740"
  //    val str: String = parseToHour(time)
  //    println(str)
  //  }
}
