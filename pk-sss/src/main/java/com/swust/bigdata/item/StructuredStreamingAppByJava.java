package com.swust.bigdata.item;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.StructType;
import redis.clients.jedis.Jedis;
import scala.Option;
import scala.Tuple3;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.TimeoutException;

/**
 * @author 雪瞳
 * @Slogan 忘时，忘物，忘我。
 * @Function 使用Structured对接kafka数据
 */
public class StructuredStreamingAppByJava {
    public static void main(String[] args) {

        SparkSession session = SparkSession.builder()
                .master("local[2]")
                .appName(StructuredStreamingAppByJava.class.getName())
                .getOrCreate();

        session.sparkContext().setLogLevel("Error");

        Dataset<Row> dataFromKafka = null;
        try {
            dataFromKafka = getDataFromKafka(session);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Dataset<Row> cleanMetas = cleanDataByStructuredStreaming(session, dataFromKafka);

        Dataset<Row> countDatasByDay = countDatasByDay(session, cleanMetas);

        try {
            insertDataToRedis(countDatasByDay);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (StreamingQueryException e) {
            e.printStackTrace();
        }

    }

    public static Dataset<Row> countDatasByDay(SparkSession session, Dataset<Row> dataset){

        Column day = dataset.col("day");
        Column province = dataset.col("province");
        Dataset<Row> count = dataset.toDF("timestamp", "day", "province")
                .groupBy(day, province)
                .count();

        return count;

    }

    /**
     * insert into redis
     *
     * @param dataset
     * @throws TimeoutException
     * @throws StreamingQueryException
     */
    public static void insertDataToRedis(Dataset<Row> dataset) throws TimeoutException, StreamingQueryException {
        dataset.writeStream()
                .outputMode(OutputMode.Complete())
                .foreach(new ForeachWriter<Row>() {
                    private Jedis jedis;

                    @Override
                    public boolean open(long partitionId, long epochId) {
                        jedis = RedisConnectionUtils.redisConnection();
                        if (jedis != null) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public void close(Throwable errorOrNull) {
                        if (jedis != null) {
                            jedis.close();
                        }
                    }

                    @Override
                    public void process(Row value) {
//                        Timestamp timestamp = value.getTimestamp(0);
//                        String time = timestamp.toString();
                        String day = value.getString(0);
                        String province = value.getString(1);
                        long nums = value.getLong(2);

                        jedis.hset("day-province-cnts-" + day, province, String.valueOf(nums));
                    }
                })
                .option("checkpointLocation", "./pk-sss/check/java/points")
                .start()
                .awaitTermination();
    }


    /**
     * show data by console
     *
     * @param dataset
     */
    public static void showData(Dataset<Row> dataset) {
        try {
            dataset.writeStream()
                    .format("console")
//                    .outputMode(OutputMode.Complete())
                    .start()
                    .awaitTermination();
        } catch (TimeoutException | StreamingQueryException e) {
            e.printStackTrace();
        }

    }

    /***
     * clean data
     * @param session
     * @param dataset
     * @return
     */
    public static Dataset<Row> cleanDataByStructuredStreaming(SparkSession session, Dataset<Row> dataset) {


        Dataset<String> datas = dataset.as(Encoders.STRING());

        Encoder<Tuple3<Timestamp, String, String>> tuple3 = (Encoder<Tuple3<Timestamp, String, String>>)
                Encoders.tuple(Encoders.TIMESTAMP(),
                        Encoders.STRING(),
                        Encoders.STRING());

        Dataset<Tuple3<Timestamp, String, String>> cleanDatas = datas.map(new MapFunction<String, Tuple3<Timestamp, String, String>>() {
            @Override
            public Tuple3<Timestamp, String, String> call(String value) throws Exception {
                String trim = value.trim();
                String[] words = trim.split("\t");
                String time = words[0];
                String ip = words[2];

                String day = formatTime(time);
                Timestamp timestamp = new Timestamp(Long.parseLong(time));
                String province = IPAnalysicUtil.parseIP(ip);

                Tuple3<Timestamp, String, String> results = new Tuple3<>(timestamp, day, province);

                return results;
            }
        }, tuple3);

        Dataset<Row> watermark = cleanDatas.toDF("timestamp", "day", "province")
                .withWatermark("timestamp", "10 minutes");

        return watermark;
    }


    /***
     * read data from kafka
     * @param session
     * @return
     */
    public static Dataset<Row> getDataFromKafka(SparkSession session) throws IOException {
        int offset = GetOffsetUtils.getOffsetFromCheckPointFile();
        Dataset<Row> load = session.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "hadoop000:9092,hadoop000:9093,hadooop000:9094")
                .option("subscribe", "access-log-nfy")
                .option("startingOffsets", "earliest")
//                .option("startingOffsets", "{\"access-log-nfy\":{\"0\":" + offset + "}}")
                .load();

        // 使用几个CAST 数据就会转换成什么样子
        Dataset<Row> selectExpr = load.selectExpr("CAST(value AS STRING)");
        return selectExpr;
    }

    /**
     * format time
     *
     * @param time
     * @return
     */
    public static String formatTime(String time) {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd");
        String day = format.format(Long.valueOf(time));
        return day;
    }
}
