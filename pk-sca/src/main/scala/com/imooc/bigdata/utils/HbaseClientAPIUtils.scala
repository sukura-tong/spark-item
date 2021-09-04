package com.imooc.bigdata.utils

import java.util

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{Cell, CellUtil, HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Result, ResultScanner, Scan, Table}
import org.apache.hadoop.hbase.util.Bytes

/** *
 * Hbase API 编程学习
 */
object HbaseClientAPIUtils {
  // 连接Hbase API
  private val configuration: Configuration = HBaseConfiguration.create()
  // 设置Hbase的路径 zookeeper
  configuration.set("hbase.zookeeper.quorum", "hadoop000:2181")

  // 通过HBase工厂设计模式获取Hbase连接对象
  private val connection: Connection = ConnectionFactory.createConnection(configuration)

  def main(args: Array[String]): Unit = {
    //    println(getTableName("person"))
    queryTableElements("person")
  }

  // 获取表名
  def getTableName(tname: String) = {
    val tableName: Table = connection.getTable(TableName.valueOf(tname))
    tableName
  }

  // 插入数据
  def insertToTable(tname: String): Unit = {
    val table: Table = getTableName(tname = tname)
    // 主键
    val put = new Put("4".getBytes())
    // 添加数据
    put.addColumn(Bytes.toBytes("feature"), Bytes.toBytes("name"), Bytes.toBytes("xiaonie"))
    put.addColumn(Bytes.toBytes("feature"), Bytes.toBytes("age"), Bytes.toBytes("23"))
    put.addColumn(Bytes.toBytes("feature"), Bytes.toBytes("sex"), Bytes.toBytes("man"))
    // 添加数据
    table.put(put)
    // 关闭资源
    if (table == null) {
      table.close()
    }
  }

  // 查询数据
  def queryTableElements(tname: String) = {
    val table: Table = getTableName(tname = tname)
    val scan = new Scan()
    // 设置row key start--stop
    // 左闭右开区间 所以获得的元素是 1 和 2
    scan.setStartRow("1".getBytes())
    scan.setStopRow("3".getBytes())

    // 设置family column
    //    scan.addColumn("feature".getBytes(),"name".getBytes())

    val scanner: ResultScanner = table.getScanner(scan)
    // 获取迭代对象
    val resValue: util.Iterator[Result] = scanner.iterator()

    while (resValue.hasNext) {
      val result: Result = resValue.next()
      while (result.advance()) {
        // 获取单元格对象
        val cell: Cell = result.current()
        val row_byte: Array[Byte] = CellUtil.cloneRow(cell)
        val family_byte: Array[Byte] = CellUtil.cloneFamily(cell)
        val qualifier_byte: Array[Byte] = CellUtil.cloneQualifier(cell)
        val value_byte: Array[Byte] = CellUtil.cloneValue(cell)

        // 将字节数组转为字符串
        val row = new String(row_byte)
        val family = new String(family_byte)
        val qualifier = new String(qualifier_byte)
        val value = new String(value_byte)
        // show
        println(row + "--"
          + family + "--"
          + qualifier + "--"
          + value)
      }
    }


    // 关闭资源
    if (table == null) {
      table.close()
    }
  }
}
