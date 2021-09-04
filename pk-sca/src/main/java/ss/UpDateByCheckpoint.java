package ss;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 累加历史流数据
 */
public class UpDateByCheckpoint {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName(UpDateByCheckpoint.class.getName())
                .setMaster("local[2]");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, new Duration(3000));
        jssc.sparkContext().setLogLevel("Error");
        String savePath = "pk-ss/check";
        jssc.checkpoint(savePath);
        // logistic
        String hostName = "hadoop000";
        int port = 4280;
        JavaReceiverInputDStream<String> inputStream = jssc.socketTextStream(hostName, port);

        JavaDStream<String> inputFlatMap = inputStream.flatMap(line -> {
            String[] split = line.split(",");
            return Arrays.asList(split).iterator();
        });

        JavaPairDStream<String, Integer> pairInput = inputFlatMap.mapToPair(line -> {
            Tuple2<String, Integer> tuple2 = new Tuple2<>(line, 1);
            return tuple2;
        });
        // 核心代码 updateStateByKey
        JavaPairDStream<String, Integer> state = pairInput.updateStateByKey(new Function2<List<Integer>, Optional<Integer>, Optional<Integer>>() {
            @Override
            public Optional<Integer> call(List<Integer> list, Optional<Integer> optional) throws Exception {
                int currentValue = 0;
                for (int elem : list) {
                    currentValue += elem;
                }
                int oldValue = 0;
                // 多重逻辑判断
                if (optional != null) {
                    // 是否存在历史数据呢
                    boolean present = optional.isPresent();
                    if (present) {
                        int value = optional.get();
                        oldValue = value;
                    }
                }
                Integer sum = currentValue + oldValue;
                // 封装返回值
                return Optional.of(sum);
            }
        });
        state.foreachRDD(rdd -> {
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
