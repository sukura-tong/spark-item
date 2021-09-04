package sql;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 使用sparkSQL进行数据处理
 * fun1 读取 加载路径的json数据并格式化
 */
public class SparkSqlAppTest {
    public static void main(String[] args) {
        SparkSession session = SparkSession
                .builder()
                .master("local[2]")
                .appName(SparkSqlAppTest.class.getSimpleName())
                .getOrCreate();
        session.sparkContext().setLogLevel("Error");
        // read

        // 数据输入路径
        String inpath = "./pk-ss/data/employees.json";
        // 格式化类型
        String format = "json";
        Dataset<Row> load = session.read().format(format).load(inpath);

        // 查表
        load.show();
        // 查看表头
        load.printSchema();

        // To do ...

        // 将DataSet 注册为临时表
        String tname = "employee";
        load.createOrReplaceTempView(tname);

        // 使用sparkSession编写SQL查询语句
        Dataset<Row> sqlFilter = session.sql(
                "select name,salary from employee where salary > 3000 and salary < 4000"
        );

        // show
        sqlFilter.show();

        // close
        session.close();
    }
}
