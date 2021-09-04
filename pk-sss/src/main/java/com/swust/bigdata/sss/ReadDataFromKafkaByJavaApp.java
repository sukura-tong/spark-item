package com.swust.bigdata.sss;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 使用structured Streaming 对接 kafka 内部数据 基于Java API
 */
public class ReadDataFromKafkaByJavaApp {
    public static void main(String[] args) {
        SparkSession session = SparkSession.builder()
                .master("local[2]")
                .appName(ReadDataFromKafkaByJavaApp.class.getSimpleName())
                .getOrCreate();

        session.sparkContext().setLogLevel("Error");

        getDataByKafkaSource(session);

    }

    public static void getDataByKafkaSource(SparkSession session) {
        Dataset<Row> loads = session.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "hadoop000:9092")
                .option("subscribe", "access-sss-topic")
                .load();

        Dataset<Row> expr = loads.selectExpr("CAST(value as STRING)");
        Dataset<String> dataset = expr.as(Encoders.STRING());

        // word count
        Dataset<String> flatMap = dataset.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String line) throws Exception {
                String[] split = line.split(",");
                List<String> list = Arrays.asList(split);
                return list.iterator();
            }
        }, Encoders.STRING());

        Dataset<Row> values = flatMap.groupBy("value")
                .count();

        try {
            StreamingQuery query = values.writeStream()
                    .outputMode(OutputMode.Update())
                    // 指定触发间隔 目前没有发现什么效果
//                    .trigger(Trigger.ProcessingTime("5 seconds"))
                    .format("console")
                    .start();
            query.awaitTermination();
        } catch (TimeoutException | StreamingQueryException e) {
            e.printStackTrace();
        }
    }
}
