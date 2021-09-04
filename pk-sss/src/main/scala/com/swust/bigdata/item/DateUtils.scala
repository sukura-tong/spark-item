package com.swust.bigdata.item

import org.apache.commons.lang3.time.FastDateFormat

object DateUtils {
  // 线程安全
  val TARGET_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd")

  // 解析方法
  def parseToDay(time: String) = {
    val date: String = TARGET_FORMAT.format(time.toLong)
    date
  }

  //  def main(args: Array[String]): Unit = {
  //    val time = "1606829727740"
  //    val str: String = parseToHour(time)
  //    println(str)
  //  }
}
