package com.swust.bigdata.sss;

import com.swust.bigdata.pojo.EventTimeAndWindows;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import scala.Function1;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 使用eventtime 进行滑动窗口处理
 */
public class EventTimeWindowByJava {
    public static void main(String[] args) {
        SparkSession session = SparkSession.builder()
                .appName(EventTimeWindowByJava.class.getSimpleName())
                .master("local[2]")
                .getOrCreate();

        session.sparkContext().setLogLevel("Error");

        getDataByWindow(session);

    }

    /***
     * 可能仍然存在bug
     * @param session
     */
    public static void getDataByWindow(SparkSession session) {

        Dataset<Row> loads = session.readStream()
                .format("socket")
                .option("host", "hadoop000")
                .option("port", 9999)
                .load();

        Encoder<Tuple2<String, String>> tuple =
                (Encoder<Tuple2<String, String>>) Encoders.tuple(Encoders.STRING(), Encoders.STRING());

        Dataset<Tuple2<String, String>> map = loads.map(new MapFunction<Row, Tuple2<String, String>>() {
            @Override
            public Tuple2<String, String> call(Row value) throws Exception {
                if (value != null) {
                    String metadata = value.toString();
                    char[] array = metadata.toCharArray();
                    if (array.length > 2) {
                        String line = metadata.substring(1, array.length - 1);

                        String[] split = line.split(",");
                        String time = split[0];
                        String word = split[1];
                        Tuple2<String, String> tuple2 = new Tuple2<>(time, word);
                        return tuple2;
                    }
                }
                return new Tuple2<>("null", "null");
            }
        }, tuple);

        Dataset<Row> df = map.toDF("time", "word");

        Dataset<Row> count = df.groupBy(
                functions.window(df.col("time"), "10 minutes", "5 minutes"),
                df.col("word")
        ).count();

        Dataset<Row> sort = count.sort("window");

        try {
            StreamingQuery query = sort.writeStream()
                    .outputMode(OutputMode.Complete())
                    .option("truncate", "false")
                    .format("console")
                    .start();

            query.awaitTermination();
        } catch (TimeoutException | StreamingQueryException e) {
            e.printStackTrace();
        }
    }


    /***
     * 错误方法
     * @param session
     */
    public static void getDataByWindowError(SparkSession session) {

        Dataset<Row> loads = session.readStream()
                .format("socket")
                .option("host", "hadoop000")
                .option("port", 4444)
                .load();

        Dataset<String> dataset = loads.as(Encoders.STRING());

        //TODO ...
//        JavaPairRDD<String, String> pair = dataset.toJavaRDD().mapToPair(line -> {
//            String[] split = line.split(",");
//            String time = split[0];
//            String word = split[1];
//            Tuple2<String, String> tuple2 = new Tuple2<>(time, word);
//            return tuple2;
//        });
//
//        JavaRDD<EventTimeAndWindows> metaRows = pair.map(new Function<Tuple2<String, String>, EventTimeAndWindows>() {
//            @Override
//            public EventTimeAndWindows call(Tuple2<String, String> tuple2) throws Exception {
//                String time = tuple2._1();
//                String word = tuple2._2();
//                EventTimeAndWindows windows = new EventTimeAndWindows();
//                windows.setTime(time);
//                windows.setWord(word);
//                return windows;
//            }
//        });
////
//        Dataset<Row> dataFrame = session.createDataFrame(metaRows, EventTimeAndWindows.class);
//
////        Dataset<Row> result = dataFrame.groupBy(
////                functions.window(dataFrame.col("time"), "10 minutes", "5 minutes"),
////                dataFrame.col("word")
////        ).count();
//
//
//        try {
//            StreamingQuery query = dataset.writeStream()
////                    .outputMode(OutputMode.Complete())
//                    .option("truncate", "false")
//                    .format("console")
//                    .start();
//            query.awaitTermination();
//        } catch (TimeoutException | StreamingQueryException e) {
//            e.printStackTrace();
//        }
    }
}
