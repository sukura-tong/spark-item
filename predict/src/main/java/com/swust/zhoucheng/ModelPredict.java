package com.swust.zhoucheng;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author XueTong
 * @Slogan Your story may not have a such happy begining, but that doesn't make who you are.
 * It is the rest of your story, who you choose to be.
 * @Date 2021/6/22 12:45
 * @Function
 */
public class ModelPredict {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("model-predict");
        JavaSparkContext jsc = new JavaSparkContext(conf);

        Random random = new Random();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 20; i++){
            list.add(random.nextInt(6));
        }
        JavaRDD<Integer> rdd = jsc.parallelize(list);
        JavaRDD<Integer> map = rdd.map(line -> {
            if (line != 0) {
                System.out.println("第" + line + "类故障");
            } else {
                System.out.println("正常");
            }
            return 0;
        });
        map.count();
    }
}
