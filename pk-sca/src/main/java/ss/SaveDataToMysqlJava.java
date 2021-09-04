package ss;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.sql.SQLException;
import java.util.*;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 将数据写入数据库内
 * <p>
 * 代码存在的优化点
 * 1、查取数据时的效率 有冗余代码块
 * 2、使用MyBatis对接数据库
 */
public class SaveDataToMysqlJava {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf();
        conf.setMaster("local[2]").setAppName(SaveDataToMysqlJava.class.getSimpleName());

        JavaStreamingContext jscc = new JavaStreamingContext(conf, new Duration(2000));
        jscc.sparkContext().setLogLevel("Error");

        // to do
        // clear
        try {
            MysqlPoolUtils.deleteTableElements();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
//
        JavaReceiverInputDStream<String> inputDstream = jscc.socketTextStream("hadoop000", 4280);
        JavaDStream<String> flatMap = inputDstream.flatMap(line -> {
            String[] splits = line.split(",");
            Iterator<String> iterator = Arrays.asList(splits).iterator();
            return iterator;
        });
        JavaPairDStream<String, Integer> pair = flatMap.mapToPair(line -> {
            Tuple2<String, Integer> tuple2 = new Tuple2<>(line, 1);
            return tuple2;
        });
        JavaPairDStream<String, Integer> result = pair.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        });
        // 基于foreachRDD保存数据到数据库内
        result.foreachRDD(rdd -> {
            // 对每一个数据批次段创建一次连接对象
//            Connection pooling = MysqlPoolUtils.getPooling();
            rdd.foreachPartition(partition -> {
                // 插入数据
                List<WordNum> list = new ArrayList<>();
                while (partition.hasNext()) {
                    Tuple2<String, Integer> tuple2 = partition.next();
                    String word = tuple2._1();
                    Integer num = tuple2._2();
                    // 创建word Num对象
                    WordNum wordNum = new WordNum();
                    wordNum.setNum(num);
                    wordNum.setWord(word);
                    list.add(wordNum);
                    // 使用JDBC连接对象将word对象对接到mysql表内
//                    int insetTip = MysqlPoolUtils.insetIntoTables(wordNum);
                }
                // 先更新在插入
                if (!list.isEmpty()) {
                    updateData(list);
                }
            });
        });

        // start
        jscc.start();
        try {
            jscc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        jscc.start();
        try {
            jscc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 先拿到数据最后更新表
    public static void updateData(List<WordNum> datas) {
        // 创建k-v对照关系表
        Map<String, Integer> temp = new HashMap<>();
        try {
            ArrayList<WordNum> wordNums = MysqlPoolUtils.selectTales();
            if (!wordNums.isEmpty()) {
                for (WordNum wordNum : wordNums) {
                    String key = wordNum.getWord();
                    int value = wordNum.getNum();
                    // 存在即追加 不存在则创建
                    if (temp.containsKey(key)) {
                        Integer old = temp.get(key);
                        Integer current = old + value;
                        temp.put(key, current);
                    } else {
                        temp.put(key, value);
                    }
                }
            }
            // 将原始数据与传入数据集对接
            if (!datas.isEmpty()) {
                for (WordNum wordNum : datas) {
                    String newKey = wordNum.getWord();
                    int newValue = wordNum.getNum();
                    if (temp.containsKey(newKey)) {
                        Integer old = temp.get(newKey);
                        Integer current = old + newValue;
                        temp.put(newKey, current);
                    } else {
                        temp.put(newKey, newValue);
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 将原始数据封装成List形式进行插入更新
        // 有则更新无则插入
        List<WordNum> result = new ArrayList<>();
        Set<Map.Entry<String, Integer>> entries = temp.entrySet();

        for (Map.Entry<String, Integer> entry : entries) {
            WordNum data = new WordNum();
            String word = entry.getKey();
            Integer value = entry.getValue();
            data.setWord(word);
            data.setNum(value);
            // append
            result.add(data);
        }

        // 调用更新方法向数据库更新元素，如果更新失败 即说明没有这个key 则更正算法为插入数据
        insetOrUpdateTable(result);
    }


    public static void insetOrUpdateTable(List<WordNum> datas) {
        //对数据库是否存在元素 相当于join
        try {
            ArrayList<WordNum> wordNums = MysqlPoolUtils.selectTales();

            // 将原始数据表内的key取出
            ArrayList<String> keys = new ArrayList<>();
            for (WordNum elem : wordNums) {
                String word = elem.getWord();
                keys.add(word);
            }

            if (!datas.isEmpty()) {
                if (wordNums.isEmpty()) {
                    for (WordNum data : datas) {
                        MysqlPoolUtils.insetIntoTables(data);
                    }
                } else {
                    //相当于不为空 则更新value即可
                    for (WordNum data : datas) {
                        // 存在键则更新 反之插入数据
                        String key = data.getWord();
                        int value = data.getNum();
                        if (keys.contains(key)) {
                            //更新数据
                            MysqlPoolUtils.update(data);
                        } else {
                            MysqlPoolUtils.insetIntoTables(data);
                        }
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
