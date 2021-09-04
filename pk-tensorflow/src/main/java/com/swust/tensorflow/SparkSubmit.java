package com.swust.tensorflow;

import org.apache.commons.io.IOUtils;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.tensorflow.*;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SparkSubmit {
    public static void main(String[] args) throws IOException {

        SparkConf conf = new SparkConf();
//        conf.setMaster("local");
        conf.setAppName("predict");
        JavaSparkContext jsc = new JavaSparkContext(conf);
//        jsc.setLogLevel("Error");
//        String inpath = "./tensorflow/testdata.csv";
        String inpath = args[0];
        JavaRDD<String> file = jsc.textFile(inpath);

        // read data
        JavaRDD<float[][][]> map = file.map(line -> {
            String[] splits = line.split(",");
            float[][][] a = new float[1][1][splits.length - 1];
            for (int i = 0; i < splits.length - 1; i++) {
                a[0][0][i] = Float.parseFloat(splits[i + 1]);
            }
            return a;
        });
        // 广播模型bytes
        String model_path = args[1];
        byte[] graphBytes = IOUtils.toByteArray(new FileInputStream(model_path));
        Broadcast<byte[]> broadcast = jsc.broadcast(graphBytes);
        // 累加器
        // 处理数据
        JavaRDD<Integer> resRdd = map.map(line -> {

            // get basic params
            byte[] value = broadcast.value();
            Graph graph = new Graph();
            graph.importGraphDef(value);
            Session session = new Session(graph);
            Tensor input_x = Tensor.create(line);
            Operation predict = graph.operation("output_y");
            Output output = new Output(predict, 0);
            Session.Runner runner = session.runner();
            //train
            Session.Runner feed = runner.feed("input_x", input_x);
            Session.Runner fetch = feed.fetch(output);
            List<Tensor<?>> out = fetch.run();

            Integer result = null;
            for (Tensor s : out) {
                float[][] t = new float[1][2];
                s.copyTo(t);
                //show data
//                for (float i : t[0])
//                    // 热编码
//                    System.out.println(i);
                int max = getResult(t[0]);
                result = max;
            }
            return result;
        });

        List<Integer> collect = resRdd.collect();
        double rate = getRate(collect, args[2]);
        System.out.println("测试集的验证准确率是" + 100 * rate + "%");
    }

    public static int getResult(float[] datas) {
        int res = 0;
        float max = datas[0];
        for (int i = 0; i < datas.length; i++) {
            if (max < datas[i]) {
                res = i;
                max = datas[i];
            }
        }
        return res;
    }

    public static double getRate(List<Integer> predict, String input) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(input)));
        List<Integer> label = new ArrayList<>();
        String data = null;
        while ((data = reader.readLine()) != null) {
            label.add(Integer.valueOf(data.split(",")[0]));
        }
        int err_num = 0;
        for (int i = 0; i < label.size(); i++) {
            if (Math.abs(label.get(i) - predict.get(i)) > 0.5) {
                err_num++;
            }
        }
        double rate = new BigDecimal((float) (label.size() - err_num) / label.size()).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();

        return rate;
    }
}

