package com.swust.nfy;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.tensorflow.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparkTest {
    public static void main(String[] params) throws IOException {

        String[] args = new String[]{
                "/home/hadoop/tensorflowOnSpark/test_x.txt",
                "/home/hadoop/tensorflowOnSpark/model.pb",
                "/home/hadoop/tensorflowOnSpark/test_y.txt"
        };

//        if (args.length < 1) {
//            System.out.println("Usage: please specify the file path");
//            System.exit(1);
//        }
        SparkConf conf = new SparkConf();
        conf.setMaster("local");
        conf.setAppName("test");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        jsc.setLogLevel("Error");
        String inpath = params[0];
        JavaRDD<String> file = jsc.textFile(inpath);

        JavaRDD<float[][]> map = file.map(line -> {
            String[] splits = line.split(",");
            float[][] a = new float[1][splits.length];
            for (int i = 0; i < splits.length; i++) {
                a[0][i] = Float.parseFloat(splits[i]);
            }
            return a;
        });
        // 处理数据
        JavaRDD<Integer> resRdd = map.map(line -> {
            // get basic params
            Map<String, Object> modelParams = getModelParams(params);
            Session session = (Session) modelParams.get("model");
            Output output = (Output) modelParams.get("out");
            Tensor input_x = Tensor.create(line);

            // train datas
            List<Tensor<?>> out = session.runner().feed("input_x", input_x).fetch(output).run();
            // 热编码解码
            Integer result = null;

            for (Tensor s : out) {
                float[][] t = new float[1][10];
                s.copyTo(t);
                //show data
//                for (float i : t[0])
//                    // 热编码
//                    System.out.println(i);
                int max = getResultByFloat(t[0]);
                result = max;
            }
            return result;
        });

//        List<Integer> collect = resRdd.collect();
//        for (int x : collect){
//            System.out.println(x);
//        }

        List<Integer> outValue = getOutValue(params);
        List<Integer> collect = resRdd.collect();
        // 计算准确率
        List<String> value = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            Integer true_data = outValue.get(i);
            Integer predict_data = collect.get(i);
            String s = true_data + "--" + predict_data;
            value.add(s);
            System.out.println(s);
        }
        int err_num = 0;
        for (int i = 0; i < value.size(); i++) {
            String str = value.get(i);
            String[] split = str.split("--");
            int dis = Integer.parseInt(split[0]) - Integer.parseInt(split[1]);
            int abs_dis = Math.abs(dis);
            if (abs_dis != 0) {
                err_num++;
            }
        }


        double rate = new BigDecimal((float) (collect.size() - err_num) / collect.size()).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
        System.out.println("测试集的验证准确率是" + 100 * rate + "%");

    }

    public static List<Integer> getOutValue(String[] params) throws IOException {
        // 获取真实标签值
        String yname = params[1];
        File ypath = new File(yname);
        BufferedReader reader = new BufferedReader(new FileReader(ypath));
        String data = null;
        List<Integer> y_out = new ArrayList<>();

        while ((data = reader.readLine()) != null) {
            System.out.println(data);
            String[] splits = data.split(",");
            int res = getResult(splits);
            y_out.add(res);
        }
//        for (int y : y_out){
//            System.out.println(y);
//        }
        return y_out;
    }

    public static Map<String, Object> getModelParams(String[] params) {
//        load("F:\\学习资料\\15.毕业设计\\小印\\使用CSA优化LSTM\\model2", "mytag");
        SavedModelBundle session = SavedModelBundle.load(params[2], "mytag");
        Session tfSession = session.session();
        Operation operationPredict = session.graph().operation("predict");   //要执行的op
        Output output = new Output(operationPredict, 0);
        Map<String, Object> map = new HashMap<>();
        map.put("model", tfSession);
        map.put("out", output);
        return map;
    }

    public static int getResultByFloat(float[] datas) {
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

    public static int getResult(String[] datas) {
        int res = 0;
        float max = Float.parseFloat(datas[0]);
        for (int i = 0; i < datas.length; i++) {
            if (max < Float.parseFloat(datas[i])) {
                res = i;
                max = Float.parseFloat(datas[i]);
            }
        }
        return res;
    }
}
