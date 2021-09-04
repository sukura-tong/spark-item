package ss;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 读取数据从HDFS文件系统
 */
public class WordCountByFileSystem {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName(WordCountByFileSystem.class.getName())
                .setMaster("local[2]");
        JavaStreamingContext jscc = new JavaStreamingContext(conf, new Duration(1000));
        jscc.sparkContext().setLogLevel("Error");

        // read
        JavaDStream<String> fileInput = jscc.textFileStream("./tensorflow/stream/");
        // iterator
        JavaDStream<String> dataSource = fileInput.flatMap(line -> {
            String[] splits = line.split(",");
            return Arrays.asList(splits).iterator();
        });
        // key-value
        JavaPairDStream<String, Integer> tupleRdd = dataSource.mapToPair(line -> {
            Tuple2<String, Integer> tuple2 = new Tuple2<>(line, 1);
            return tuple2;
        });
        // add
        JavaPairDStream<String, Integer> resultRdd = tupleRdd.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        });
        // show
        resultRdd.foreachRDD(line -> {
            line.foreach(data -> {
                System.out.println(data);
            });
        });

        // start
        jscc.start();
        try {
            jscc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
