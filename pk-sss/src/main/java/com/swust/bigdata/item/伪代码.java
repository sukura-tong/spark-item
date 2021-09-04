//package com.swust.bigdata.item;
//
//import org.apache.spark.api.java.function.MapFunction;
//import org.apache.spark.sql.*;
//import org.apache.spark.sql.streaming.OutputMode;
//import redis.clients.jedis.Jedis;
//import scala.Tuple3;
//
//import java.sql.Timestamp;
//
//public class 伪代码 {
//    public static void main(String[] args) {
//        SparkSession session = SparkSession.builder()
//                .appName("code")
//                .master("local[2]")
//                .getOrCreate();
//
//        Dataset<Row> load = session.readStream()
//                .format("kafka")
//                .option("kafka.bootstrap.servers", "hadoop000:9092,hadoop000:9093,hadooop000:9094")
//                .option("subscribe", "access-log-nfy")
//                .option("startingOffsets", "earliest")
////                .option("startingOffsets", "{\"access-log-nfy\":{\"0\":" + offset + "}}")
//                .load();
//
//        Dataset<Row> row = load.selectExpr("CAST (value AS STRING)");
//
//        Dataset<String> datas = row.as(Encoders.STRING());
//
//        Encoder<Tuple3<Timestamp, String, String>> tuple3 = (Encoder<Tuple3<Timestamp, String, String>>)
//                Encoders.tuple(Encoders.TIMESTAMP(),
//                        Encoders.STRING(),
//                        Encoders.STRING());
//
//        Dataset<Tuple3<Timestamp, String, String>> cleanDatas = datas.map(new MapFunction<String, Tuple3<Timestamp, String, String>>() {
//            @Override
//            public Tuple3<Timestamp, String, String> call(String value) throws Exception {
//                String trim = value.trim();
//                String[] words = trim.split("\t");
//                String time = words[0];
//                String ip = words[2];
//
//                String day = formatTime(time);
//                Timestamp timestamp = new Timestamp(Long.parseLong(time));
//                String province = IPAnalysicUtil.parseIP(ip);
//
//                Tuple3<Timestamp, String, String> results = new Tuple3<>(timestamp, day, province);
//
//                return results;
//            }
//        }, tuple3);
//
//        Dataset<Row> watermark = cleanDatas.toDF("timestamp", "day", "province")
//                .withWatermark("timestamp", "10 minutes");
//
//        Column day = watermark.col("day");
//        Column province = watermark.col("province");
//        Dataset<Row> count = watermark.toDF("timestamp", "day", "province")
//                .groupBy(day, province)
//                .count();
//
//        count.writeStream()
//                .outputMode(OutputMode.Complete())
//                .foreach(new ForeachWriter<Row>() {
//                    private Jedis jedis;
//
//                    @Override
//                    public boolean open(long partitionId, long epochId) {
//                        jedis = RedisConnectionUtils.redisConnection();
//                        if (jedis != null) {
//                            return true;
//                        } else {
//                            return false;
//                        }
//                    }
//
//                    @Override
//                    public void close(Throwable errorOrNull) {
//                        if (jedis != null) {
//                            jedis.close();
//                        }
//                    }
//
//                    @Override
//                    public void process(Row value) {
//                        String day = value.getString(0);
//                        String province = value.getString(1);
//                        long nums = value.getLong(2);
//
//                        jedis.hset("day-province-cnts-" + day, province, String.valueOf(nums));
//                    }
//                })
//                .option("checkpointLocation", "./pk-sss/check/java/points")
//                .start()
//                .awaitTermination();
//
//    }
//}
