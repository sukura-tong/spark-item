package com.imooc.bigdata.ss

import java.sql.{Connection, DriverManager}

/**
 * 使用mysql工具类实现将数据写入数据库
 */
object MysqlUtislScala {
  def getConnection() = {
    Class.forName("com.mysql.jdbc.Driver")
    val connection: Connection = DriverManager.getConnection("jdbc:mysql://hadoop000:3306/pk", "root", "root")
    connection
  }

  def close(connection: Connection): Unit = {
    if (connection != null) {
      connection.close()
    }
  }
}
