package ss;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.*;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 日志数据过滤操作 基于Join算子
 */
public class CoreJoinApp {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf();
        conf.setMaster("local[2]").setAppName(CoreJoinApp.class.getName());
        JavaSparkContext jsc = new JavaSparkContext(conf);
        jsc.setLogLevel("Error");

        // 处理过滤数据
        List<Map<String, Integer>> list = new ArrayList<>();
        Map<String, Integer> one = new HashMap<>();
        one.put("lqx", 1);
        list.add(one);

        JavaRDD<Map<String, Integer>> listRdd = jsc.parallelize(list);
        JavaRDD<List<Tuple2<String, Integer>>> flatMap = listRdd.flatMap(line -> {
            Set<Map.Entry<String, Integer>> entries = line.entrySet();
            List<Tuple2<String, Integer>> result = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : entries) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                Tuple2<String, Integer> tuple2 = new Tuple2<>(key, value);
                result.add(tuple2);
            }
            return Arrays.asList(result).iterator();
        });
        // list 再做一次flatMap就会转换成单个RDD
        JavaRDD<Tuple2<String, Integer>> keyVlaueRdd = flatMap.flatMap(line -> {
            return line.iterator();
        });
        JavaPairRDD<String, Integer> listTupleRdd = keyVlaueRdd.mapToPair(line -> {
            String key = line._1();
            Integer value = line._2();
            Tuple2<String, Integer> tuple2 = new Tuple2<>(key, value);
            return tuple2;
        });


        // 处理输入数据
        List<Map<String, String>> input = new ArrayList<>();
        Map<String, String> two = new HashMap<>();
        two.put("lqx", "hll");
        two.put("xt", "yq");
        input.add(two);

        JavaRDD<Map<String, String>> inputRdd = jsc.parallelize(input);

        JavaRDD<List<Tuple2<String, String>>> inputRddFlatMap = inputRdd.flatMap(line -> {
            List<Tuple2<String, String>> result = new ArrayList<>();
            Set<Map.Entry<String, String>> entries = line.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                Tuple2<String, String> tuple2 = new Tuple2<>(key, value);
                result.add(tuple2);
            }
            return Arrays.asList(result).iterator();
        });
        JavaRDD<Tuple2<String, String>> inputJavaRdd = inputRddFlatMap.flatMap(line -> {
            return line.iterator();
        });
        // 转换成从key-value
        JavaPairRDD<String, String> inputTupleRdd = inputJavaRdd.mapToPair(new PairFunction<Tuple2<String, String>, String, String>() {
            @Override
            public Tuple2<String, String> call(Tuple2<String, String> tuple2) throws Exception {
                return new Tuple2<>(tuple2._1(), tuple2._2());
            }
        });

        // 基于Join进行过滤并显示


        // 使用listTupleRdd的key为标准进行过滤
//        List<Tuple2<String, Tuple2<Integer, String>>> join = listTupleRdd.join(inputTupleRdd).collect();
        // left join
//        List<Tuple2<String, Tuple2<Integer, Optional<String>>>> collect = listTupleRdd.leftOuterJoin(inputTupleRdd).collect();

        // 使用listTupleRdd的key为标准进行过滤
        List<Tuple2<String, Tuple2<Integer, String>>> collect = listTupleRdd.join(inputTupleRdd).collect();
        JavaRDD<Tuple2<String, Tuple2<Integer, String>>> join = jsc.parallelize(collect);
        join.foreach(line -> {
            System.out.println(line);
            System.out.println(line._1());
            System.out.println(line._2());
            System.out.println(line._2()._1());
            System.out.println(line._2()._2());
        });

        // left join
        List<Tuple2<String, Tuple2<Integer, Optional<String>>>> collectInput = listTupleRdd.leftOuterJoin(inputTupleRdd).collect();
        JavaRDD<Tuple2<String, Tuple2<Integer, Optional<String>>>> leftJoin = jsc.parallelize(collectInput);
        leftJoin.foreach(line -> {
            System.err.println(line);
            System.err.println(line._1());
            System.err.println(line._2());
            System.err.println(line._2()._1());
            System.err.println(line._2()._2());
        });


    }
}
