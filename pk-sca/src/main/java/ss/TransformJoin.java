package ss;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function transform Java 实现
 */
public class TransformJoin {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName(TransformJoin.class.getName());
        Duration duration = new Duration(1000);
        JavaStreamingContext jssc = new JavaStreamingContext(conf, duration);
        jssc.sparkContext().setLogLevel("Error");

        // filter data
        List<String> list = new ArrayList<>();
        list.add("lxt");
        JavaRDD<String> filters = jssc.sparkContext().parallelize(list);
        JavaPairRDD<String, Boolean> filterRdd = filters.mapToPair(line -> {
            Tuple2<String, Boolean> tuple2 = new Tuple2<>(line, true);
            return tuple2;
        });


        // 对接Socket端数据
        // nc -lk 4280

        int port = 4280;
        String path = "hadoop000";
        JavaReceiverInputDStream<String> lineDatas = jssc.socketTextStream(path, port);

        // 123456,xll
        JavaPairDStream<String, String> pairRdd = lineDatas.mapToPair(line -> {
            String[] words = line.split(",");
            String key = words[1];
//            Tuple2<String, String> value = new Tuple2<>(words[0], words[1]);
//            Tuple2<String, Tuple2<String, String>> tuple2 = new Tuple2<>(key, value);
            Tuple2<String, String> tuple2 = new Tuple2<>(key, line);
            return tuple2;
        });
        // ds <--> rdd
        // transform 必须自己定义返回值内容
        JavaDStream<Tuple2<String, String>> transform = pairRdd.transform(new Function<JavaPairRDD<String, String>, JavaRDD<Tuple2<String, String>>>() {
            @Override
            public JavaRDD<Tuple2<String, String>> call(JavaPairRDD<String, String> rdd) throws Exception {
                JavaPairRDD<String, Tuple2<String, Optional<Boolean>>> leftOuterJoin = rdd.leftOuterJoin(filterRdd);
                JavaPairRDD<String, Tuple2<String, Optional<Boolean>>> filter = leftOuterJoin.filter(line -> {
                    String name = line._2()._1();
                    Optional<Boolean> booleanOptional = line._2()._2();
                    if (booleanOptional.isPresent()) {
                        Boolean tip = booleanOptional.get();
                        if (tip) {
                            return false;
                        }
                        return true;
                    } else {
                        return true;
                    }
                });
                JavaPairRDD<String, String> filterMap = filter.mapToPair(line -> {
                    String key = line._1();
                    String value = line._2()._1();
                    Tuple2<String, String> tuple2 = new Tuple2<>(key, value);
                    return tuple2;
                });
                JavaRDD<Tuple2<String, String>> map = filterMap.map(line -> {
                    Tuple2<String, String> swap = line.swap();
                    return swap;
                });
                return map;
            }
        });
        JavaPairDStream<String, String> stream = transform.mapToPair(line -> {
            String key = line._1();
            String value = line._2();
            Tuple2<String, String> tuple2 = new Tuple2<>(key, value);
            return tuple2;
        });
        stream.foreachRDD(rdd -> {
            rdd.foreach(line -> {
                System.out.println(line);
            });
        });

        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
