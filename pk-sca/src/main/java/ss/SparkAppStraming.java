package ss;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @Author XueTong
 * @Slogan Your story may not have a such happy begining, but that doesn't make who you are.
 * It is the rest of your story, who you choose to be.
 * @Date 2021/5/1 10:37
 * @Function
 */
public class SparkAppStraming {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf();
        conf.setAppName("wordcount").setMaster("local");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        jsc.setLogLevel("Error");

        String path = "./pk-sca/data/read";
        JavaRDD<String> meatods = jsc.textFile(path);

        JavaRDD<String> wordRdds = meatods.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String line) throws Exception {
                char[] words = line.toCharArray();
                List<String> list = new ArrayList<>();
                for (char word : words) {
                    list.add(String.valueOf(word));
                }
                return list.iterator();
            }
        });

        JavaPairRDD<String, Integer> numRdds = wordRdds.mapToPair(line -> {
            int num = 1;
            Tuple2<String, Integer> tuple2 = new Tuple2<>(line, num);
            return tuple2;
        });

        JavaPairRDD<String, Integer> res = numRdds.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });

        res.foreach(line ->{
            System.out.println(line._1 + "---" + line._2);
        });
    }
}
