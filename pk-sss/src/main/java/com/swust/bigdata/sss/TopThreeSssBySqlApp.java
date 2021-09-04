package com.swust.bigdata.sss;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.StructType;
import scala.Function1;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function top3 error!!!!
 */
public class TopThreeSssBySqlApp {
    public static void main(String[] args) {
        SparkSession session = SparkSession.builder()
                .appName(ReadFromCsvBySql.class.getSimpleName())
                .master("local[2]")
                .getOrCreate();
        session.sparkContext().setLogLevel("Error");

        try {
            readDataByCsv(session);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (StreamingQueryException e) {
            e.printStackTrace();
        }
    }

    public static void readDataByCsv(SparkSession session) throws TimeoutException, StreamingQueryException {

        StructType userSchema = new StructType()
                .add("id", "integer")
                .add("name", "string")
                .add("city", "string")
                .add("salay", "integer");

        String inpath = "./pk-sss/data/csv";
        Dataset<Row> csvDataSet = session.readStream()
                .format("csv")
                .option("seq", ";")
                .schema(userSchema)
                .csv(inpath);
        // 排序
        getTopThreeBySort(csvDataSet, session);


    }

    public static void getTopThreeBySort(Dataset<Row> csvDataSet, SparkSession session) {

        csvDataSet.createOrReplaceTempView("user_table");

        String sql = "select * from user_table";

        Dataset<Row> dataset = session.sql(sql);

        Dataset<Row> name = dataset.groupBy("name").count();

        Dataset<Row> sort = name.sort(name.col("salay"));


        try {
            StreamingQuery query = sort.writeStream()
                    .outputMode(OutputMode.Complete())
                    .format("console")
                    .start();

            query.awaitTermination();

        } catch (TimeoutException | StreamingQueryException e) {
            e.printStackTrace();
        }


    }

    /***
     * 使用 sql 语句获取数据的top3
     * 该模块代码 有error
     * @param csvDataSet
     * @param session
     * @return
     */
    public static void getTopThreeBySortError(Dataset<Row> csvDataSet, SparkSession session) throws TimeoutException, StreamingQueryException {
        csvDataSet.createOrReplaceTempView("user_table");

        String sql = "select * from user_table";

        Dataset<Row> dataset = session.sql(sql);


        dataset.writeStream()
                .format("console")
//                .outputMode(OutputMode.Complete())
                .start()
                .awaitTermination();


        Dataset<Row> cache = dataset.cache();
        List<Row> rows = cache.collectAsList();

        Iterator<Row> iterator = rows.iterator();

        List<Row> result = new ArrayList<>();

        while (iterator.hasNext()) {
            Row row = iterator.next();
            Integer elem = (Integer) row.getAs("salay");
            if (result.size() != 3) {
                result.add(row);
            } else {
                int[] index = getIndex(result);
                int min_Index = index[0];
                int min_elem = index[1];
                if (elem > min_elem) {
                    result.remove(min_Index);
                    result.add(row);
                }
            }
        }

        for (Row row : result) {
            StringBuffer buffer = new StringBuffer();
            String distance = " ";
            for (int i = 0; i < row.length(); i++) {
                String element = (String) row.get(i);
                buffer.append(element + distance);
            }
            String str = buffer.toString();
            System.out.println(str.substring(0, str.length() - 1));
        }


    }

    public static int[] getIndex(List<Row> list) {
        int min = (Integer) list.get(0).getAs("salay");
        int index = 0;
        for (int i = 1; i < list.size(); i++) {
            int elem = (Integer) list.get(0).getAs("salay");
            if (elem < min) {
                min = elem;
                index = i;
            }
        }

        int[] res = new int[]{index, min};
        return res;
    }
}
