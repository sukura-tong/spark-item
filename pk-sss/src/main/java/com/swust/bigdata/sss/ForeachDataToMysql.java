package com.swust.bigdata.sss;

import lombok.SneakyThrows;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQueryException;
import scala.Tuple2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 将socket数据进行词频处理后写入mysql
 */
public class ForeachDataToMysql {
    public static void main(String[] args) {
        SparkSession session = SparkSession
                .builder()
                .master("local[2]")
                .appName(ForeachDataToMysql.class.getSimpleName())
                .getOrCreate();

        session.sparkContext().setLogLevel("Error");

        sendDataToMysql(session);
    }


    public static void sendDataToMysql(SparkSession session) {
        Dataset<Row> lines = session.readStream()
                .format("socket")
                .option("host", "hadoop000")
                .option("port", 9999)
                .load();

        Dataset<String> dataset = lines.as(Encoders.STRING());

        Dataset<String> flatMap = dataset.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String str) throws Exception {
                String[] split = str.split(",");
                List<String> list = Arrays.asList(split);
                return list.iterator();
            }
        }, Encoders.STRING());

        // 将其注册为表
        Dataset<Row> word = flatMap.toDF("word");

        RelationalGroupedDataset results = word.groupBy(word.col("word"));

        Dataset<Row> count = results.count();

        // show

        try {
            insertIntoMysql(session, count);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (StreamingQueryException e) {
            e.printStackTrace();
        }

    }


    public static void insertIntoMysql(SparkSession session, Dataset<Row> row) throws TimeoutException, StreamingQueryException {

        row.writeStream()
                .outputMode(OutputMode.Complete())
                .foreach(new ForeachWriter<Row>() {

                    private Connection conn;
                    private PreparedStatement pst;
                    private int batchCount = 0;

                    @SneakyThrows
                    @Override
                    public boolean open(long partitionId, long epochId) {
                        Class.forName("com.mysql.jdbc.Driver");
                        conn = DriverManager.getConnection("jdbc:mysql://hadoop000:3306/nfy", "root", "root");

                        String sql = "insert into t_words (word,num) value (?,?) " +
                                "on duplicate key update " +
                                "word = ?, num = ?";

                        pst = conn.prepareStatement(sql);
                        if (conn != null && pst != null) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @SneakyThrows
                    @Override
                    public void close(Throwable errorOrNull) {
                        pst.executeBatch();
                        conn.close();
                    }

                    @SneakyThrows
                    @Override
                    public void process(Row value) {
                        String word = value.getString(0);
                        Long num = value.getLong(1);
                        int intValue = num.intValue();


                        pst.setString(1, word);
                        pst.setInt(2, intValue);
                        pst.setString(3, word);
                        pst.setInt(4, intValue);

                        pst.addBatch();
                        batchCount++;

                        if (batchCount >= 10) {
                            pst.executeBatch();
                            batchCount = 0;
                        }
                    }
                })
                .start()
                .awaitTermination();
    }
}
