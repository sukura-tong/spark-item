package com.swust.cml;


import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;


/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 训练逻辑回归模型
 */
public class LogisticModel {
    public static void main(String[] args) {

//        SparkSession session = SparkSession.builder().appName("logistic").master("local").getOrCreate();
        SparkConf conf = new SparkConf().setMaster("local").setAppName(LogisticModel.class.getName());
        JavaSparkContext jsc = new JavaSparkContext(conf);
//        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(session.sparkContext());
//        SparkContext sc = JavaSparkContext.toSparkContext(jsc);

        jsc.setLogLevel("Error");
        JavaRDD<String> fileRDD = jsc.textFile("./tensorflow/train_gaotie.csv");
        JavaRDD<LabeledPoint> labeledPointJavaRDD = fileRDD.map(new Function<String, LabeledPoint>() {
            @Override
            public LabeledPoint call(String line) throws Exception {
                String[] splits = line.split(",");
                String label = splits[splits.length - 1];

                double[] wd = new double[splits.length - 1];
                for (int i = 0; i < wd.length - 1; i++) {
                    wd[i] = Double.parseDouble(splits[i]);
                }

                LabeledPoint labeledPoint = new LabeledPoint(Double.parseDouble(label), Vectors.dense(wd));
                return labeledPoint;
            }
        });

        //将数据集按比例切分为训练集和测试集
        double[] doubles = new double[]{0.7, 0.3};
        RDD<LabeledPoint> rdd = labeledPointJavaRDD.rdd();
        RDD<LabeledPoint>[] metaDataSource = rdd.randomSplit(doubles, 100L);
        //获取训练集和测试集
        RDD<LabeledPoint> traingData = metaDataSource[0];
        RDD<LabeledPoint> testData = metaDataSource[1];
        //训练模型
        LogisticRegressionWithLBFGS lr = new LogisticRegressionWithLBFGS();
        lr.setNumClasses(2);
        lr.setIntercept(true);
        LogisticRegressionModel model = lr.run(traingData);
        JavaRDD<Double> predictRdd = testData.toJavaRDD().map(new Function<LabeledPoint, Double>() {
            @Override
            public Double call(LabeledPoint labeledPoint) throws Exception {
                double predict = model.predict(labeledPoint.features());
                return predict;
            }
        });
        JavaPairRDD<Double, Double> zipRdd = predictRdd.zip(testData.toJavaRDD().map(new Function<LabeledPoint, Double>() {
            @Override
            public Double call(LabeledPoint labeledPoint) throws Exception {
                return labeledPoint.label();
            }
        }));

        Accumulator<Integer> accumulator = jsc.accumulator(0);
        zipRdd.foreach(new VoidFunction<Tuple2<Double, Double>>() {
            @Override
            public void call(Tuple2<Double, Double> tp) throws Exception {
                Double label = tp._2();
                Double predict = tp._1();
                if (Double.compare(label, predict) == 0) {
                    accumulator.add(1);
                }
            }
        });
        long count = zipRdd.count();
        Integer value = accumulator.value();
        System.err.println("测试集总数目是：" + count);
        System.err.println("测试集预测正确数目是：" + value);
        double rate = value / (double) count;
        System.err.println("逻辑回归模型的正确率是：" + rate * 100 + "%");
//        String path ="./save/model";
//        double  stand = 80.00;
//        if (Double.compare(rate,stand)<0){
//           // model.save(sc,path);
//            System.out.println(model.weights().size());
//        }
    }
}
