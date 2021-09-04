package ss;

import org.apache.spark.SparkConf;
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
import java.util.List;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 将流数据写入到本地文件内 java实现
 */
public class OutputWordCountDataToFile {
    public static void main(String[] args) {
        Duration duration = new Duration(1000);
        SparkConf conf = new SparkConf()
                .setAppName("1")
                .setMaster("local[2]");
        JavaStreamingContext jscc = new JavaStreamingContext(conf, duration);
        jscc.ssc().sparkContext().setLogLevel("Error");

        // read data from socket
        JavaReceiverInputDStream<String> inputDStream = jscc.socketTextStream("hadoop000", 4444);

        // wc
        JavaDStream<String> flatMap = inputDStream.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String line) throws Exception {
                String[] words = line.split(",");
                List<String> list = Arrays.asList(words);
                return list.iterator();
            }
        });

        JavaPairDStream<String, Integer> pair = flatMap.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String line) throws Exception {
                Tuple2<String, Integer> tuple2 = new Tuple2<>(line, 1);
                return tuple2;
            }
        });

        JavaPairDStream<String, Integer> result = pair.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        });

        // action
        JavaDStream<Long> count = result.count();
        count.foreachRDD(line -> {
            line.foreach(new VoidFunction<Long>() {
                @Override
                public void call(Long aLong) throws Exception {
                    System.out.println(aLong);
                }
            });
        });
        // 保存
        String prefix = "./pk-ss/save/java";
        result.foreachRDD(rdd -> {
            rdd.saveAsTextFile(prefix);
        });

        jscc.start();
        try {
            jscc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
