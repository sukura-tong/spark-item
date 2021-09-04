package ss;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function
 */
public class NetworkWordCount {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setAppName("1").setMaster("local[2]");
        Duration duration = new Duration(1000);
        JavaStreamingContext jscc = new JavaStreamingContext(conf, duration);
        jscc.ssc().sparkContext().setLogLevel("Error");

        // 获取数据
        int port = 4444;
        String hostname = "hadoop000";
        JavaReceiverInputDStream<String> metaData = jscc.socketTextStream(hostname, port);

        // 启动
        // wordcount
        JavaDStream<String> flatMap = metaData.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                String[] split = s.split(",");
                return Arrays.asList(split).iterator();
            }
        });

        JavaPairDStream<String, Integer> pairRdd = flatMap.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String line) throws Exception {
                Tuple2<String, Integer> tuple2 = new Tuple2<>(line, 1);
                return tuple2;
            }
        });
        JavaPairDStream<String, Integer> reduceByKey = pairRdd.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer value1, Integer value2) throws Exception {
                return value1 + value2;
            }
        });
        reduceByKey.foreachRDD(new VoidFunction<JavaPairRDD<String, Integer>>() {
            @Override
            public void call(JavaPairRDD<String, Integer> line) throws Exception {
                line.foreach(new VoidFunction<Tuple2<String, Integer>>() {
                    @Override
                    public void call(Tuple2<String, Integer> data) throws Exception {
                        Integer value = data._2();
                        String key = data._1();
                        System.out.println(key + ":" + value);
                    }
                });
            }
        });

        jscc.start();
        jscc.awaitTermination();

    }
}
