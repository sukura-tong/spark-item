//package sql;
//
//import org.apache.spark.SparkConf;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.sql.Dataset;
//import org.apache.spark.sql.Row;
//import org.apache.spark.sql.SparkSession;
//import org.apache.spark.streaming.Duration;
//import org.apache.spark.streaming.api.java.JavaDStream;
//import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
//import org.apache.spark.streaming.api.java.JavaStreamingContext;
//
//import java.util.Arrays;
//
///**
// * @author 雪瞳
// * @Slogan 时钟尚且前行，人怎能就此止步！
// * @Function 使用sparkSteaming和sparkSQL进行数据整合分析
// * 官网案例
// * http://spark.apache.org/docs/latest/streaming-programming-guide.html#dataframe-and-sql-operations
// */
//public class SparkStreamingToSparkSql {
//    public static void main(String[] args) {
//        SparkConf conf = new SparkConf()
//                .setAppName(SparkStreamingToSparkSql.class.getSimpleName())
//                .setMaster("local[2]");
//        JavaStreamingContext jscc = new JavaStreamingContext(conf, new Duration(1000));
//        jscc.sparkContext().setLogLevel("Error");
//
//        JavaReceiverInputDStream<String> textStream = jscc.socketTextStream("hadoop000", 4280);
//
//        JavaDStream<String> flatMap = textStream.flatMap(line -> {
//            String[] split = line.split(",");
//            return Arrays.asList(split).iterator();
//        });
//
//        // stream 与 sql 整合
//
//        flatMap.foreachRDD(rdd -> {
//            SparkSession session = SparkSession.builder().config(rdd.context().getConf()).getOrCreate();
//            // 注册临时表
//            JavaRDD<JavaRow> rowRdd = rdd.map(line -> {
//                JavaRow javaRow = new JavaRow();
//                javaRow.setWord(line);
//                return javaRow;
//            });
//            // 借助SparkSession将对象转换为Row类型RDD
//            Dataset<Row> dataFrame = session.createDataFrame(rowRdd, JavaRow.class);
//
//            dataFrame.createOrReplaceTempView("words");
//
//            String sqltext = "select word, count(*) as total from words group by word";
//            Dataset<Row> result = session.sql(sqltext);
//
//            result.show();
//        });
//
//        jscc.start();
//        try {
//            jscc.awaitTermination();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
