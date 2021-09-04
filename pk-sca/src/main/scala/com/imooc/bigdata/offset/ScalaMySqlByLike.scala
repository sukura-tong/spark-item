package com.imooc.bigdata.offset

import scalikejdbc.{DB, SQL}

object ScalaMySqlByLike {
  def main(args: Array[String]): Unit = {
    // 加载配置文件
    scalikejdbc.config.DBs.setupAll()
    query()
    println("----")
    //    insert()
    println("----")
    //    query()
  }

  // 查询
  def query() = {
    DB.readOnly { implicit session =>
      SQL("select * from offset_storage")
        .map(rs => Offset(
          rs.string("topic"),
          rs.string("groupid"),
          rs.int("partitions"),
          rs.long("offset")
        )).list()
        .apply()
    }.foreach(println)
  }

  def update() = {
    DB.autoCommit {
      implicit session =>
        SQL("update offset_storage set offset = ? where topic = ? and groupid = ? and partitions = ?")
          .bind(29, "pk-test", "test-group", 0) // 绑定字段
          .update()
          .apply()
    }
  }

  def insert() = {
    DB.autoCommit {
      implicit session =>
        SQL("insert into offset_storage(topic,groupid,partitions,offset) values(?, ?, ?, ?)")
          .bind("pk-test", "test-group", 3, 99)
          .update()
          .apply()
    }
  }


  def sel() = {
    DB.readOnly {
      implicit session =>
        SQL("select * from tables")
          .map(rs => Offset(
            rs.string(""),
            rs.string(""),
            rs.int(""),
            rs.long("")
          ))
          .update()
          .apply()
    }
  }
}

case class Offset(topic: String, groupId: String, partitionId: Int, offset: Long)
