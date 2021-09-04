package com.swust.bigdata.sss;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.IntegerType;
import org.apache.spark.sql.types.StructType;

import java.util.concurrent.TimeoutException;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 使用sparkSQL 和 sparkStreaming 对接 CSV数据
 */
public class ReadFromCsvBySql {
    public static void main(String[] args) {
        SparkSession session = SparkSession.builder()
                .appName(ReadFromCsvBySql.class.getSimpleName())
                .master("local[2]")
                .getOrCreate();
        session.sparkContext().setLogLevel("Error");

        readDataByCsv(session);
    }

    public static void readDataByCsv(SparkSession session) {

        StructType userSchema = new StructType()
                .add("id", "integer")
                .add("name", "string")
                .add("city", "string");

        String inpath = "./pk-sss/data/csv";
        Dataset<Row> csvDataSet = session.readStream()
                .format("csv")
                .option("seq", ";")
                .schema(userSchema)
                .csv(inpath);
        // 排序


        try {
            StreamingQuery query = csvDataSet.writeStream()
                    .format("console")
                    .outputMode(OutputMode.Append())
                    .start();
            try {
                query.awaitTermination();
            } catch (StreamingQueryException e) {
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


    }

}
