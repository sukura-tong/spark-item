package com.swust.bigdata.sss;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function
 */
public class WordCountSssApp {
    public static void main(String[] args) {
        SparkSession session = SparkSession.builder()
                .appName(WordCountSssApp.class.getSimpleName())
                .master("local[2]")
                .getOrCreate();

        session.sparkContext().setLogLevel("Error");

        Dataset<Row> lines = session.readStream()
                .format("socket")
                .option("host", "hadoop000")
                .option("port", 4444)
                .load();
        //使用隐式转换 将Row类型数据转换为String类型
        Dataset<String> dataset = lines.as(Encoders.STRING());

        Dataset<String> flatMapWords = dataset.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String data) throws Exception {
                String[] split = data.split(",");
                List<String> list = Arrays.asList(split);
                return list.iterator();
            }
            // 需要声明数据类型
        }, Encoders.STRING());

        // add

        Dataset<Row> wc = flatMapWords.groupBy("value")
                .count();

        try {
            wc.writeStream()
                    .outputMode(OutputMode.Complete())
                    .format("console")
                    .start()
                    .awaitTermination();
        } catch (StreamingQueryException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        ;


    }
}

