package com.swust.nfy;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple1;
import scala.Tuple2;

import java.util.List;

public class 电工学成绩 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf();
        conf.setMaster("local").setAppName("111");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        String path = "F:\\imooc\\pk-tensorflow\\data\\成绩.csv";
        JavaRDD<String> file = jsc.textFile(path);

        JavaRDD<String> meta = file.map(line -> {
            return line;
        });

        JavaRDD<Tuple2<String, Double>> map = meta.map(line -> {
            String[] split = line.split(",");
            String shiyan = split[1];
            String pingshi = split[2];
            String kaoshi = split[3];

            double test = Double.parseDouble(shiyan) * 0.2 + Double.parseDouble(pingshi) * 0.2 + Double.parseDouble(kaoshi) * 0.8;
            Tuple2<String, Double> tuple2 = new Tuple2<>(split[0], test);
            return tuple2;
        });

        List<Tuple2<String, Double>> collect =
                map.collect();

        for (Tuple2<String ,Double> tuple2 : collect){
            System.out.println(tuple2._1() + " : " + tuple2._2());
        }
    }
}
