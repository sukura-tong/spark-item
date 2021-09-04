package ss;

import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 自定义数据库连接池
 * 代码优化方向：
 * 1、使用静态数据库连接对象全局赋值只可以赋值一次
 * 2、批量数据执行
 * 3、单表查询效率
 */
public class MysqlPoolUtils {
    private static Connection pooling;

    //get connection
    public static Connection getPooling() throws ClassNotFoundException, SQLException {
        if (pooling == null) {
            Class.forName("com.mysql.jdbc.Driver");
            pooling = DriverManager.getConnection("jdbc:mysql://47.108.28.217:3306/filter", "root", "root");
        }
        return pooling;
    }

    //close
    @SneakyThrows
    public static void closePooling() {
        if (pooling == null) {
            try {
                pooling.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    // insert data
    public static int insetIntoTables(WordNum wordNum) throws SQLException, ClassNotFoundException {
        if (pooling == null) {
//            throw new IllegalArgumentException("pooling is null, please check it again!!!");
            pooling = getPooling();
        }
        // insert
        String sql = "insert into t_words(word,num) values (?,?)";
        int result = 0;
        PreparedStatement prepared;
        try {
            prepared = pooling.prepareStatement(sql);
            // 给占位符赋值
            String word = wordNum.getWord();
            int num = wordNum.getNum();
            prepared.setString(1, word);
            prepared.setInt(2, num);

            // 执行
            int tip = prepared.executeUpdate();
            // 设置日志响应级别
            result = tip;
            if (tip <= 0) {
                throw new IllegalArgumentException("update error,please check it again!!!");
            }
            prepared.close();
//            pooling.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    // select
    public static ArrayList<WordNum> selectTales() throws SQLException, ClassNotFoundException {
        if (pooling == null) {
//            throw new IllegalArgumentException("pooling is null,please check it again!!!");
            pooling = getPooling();
        }
        // 可能查找为空
        ArrayList<WordNum> result = new ArrayList<>();

        String sql = "select * from t_words";
        PreparedStatement statement = pooling.prepareStatement(sql);
        ResultSet sets = statement.executeQuery();

        while (sets.next()) {
            WordNum wordNum = new WordNum();
            String word = sets.getString(1);
            int num = sets.getInt(2);
            wordNum.setWord(word);
            wordNum.setNum(num);
            result.add(wordNum);
        }
        // close
        sets.close();
        statement.close();
//         pooling.close();

        return result;
    }

    // update
    public static int update(WordNum wordNum) throws SQLException, ClassNotFoundException {
        if (pooling == null) {
            pooling = getPooling();
        }
        // 更新数据
        String sql = "update t_words set num = ? where word = ?";
        String word = wordNum.getWord();
        int num = wordNum.getNum();
        PreparedStatement preparedStatement = pooling.prepareStatement(sql);
        // 给占位符赋值
        preparedStatement.setInt(1, num);
        preparedStatement.setString(2, word);
        int result = preparedStatement.executeUpdate();
        if (result <= 0) {
            throw new IllegalArgumentException("update error,please check it again!!!");
        }
        // close
        preparedStatement.close();
//        pooling.close();
        return result;
    }

    // 清空表
    public static void deleteTableElements() throws SQLException, ClassNotFoundException {
        if (pooling == null) {
            pooling = getPooling();
        }
        String sql = "delete from t_words";
        PreparedStatement preparedStatement = pooling.prepareStatement(sql);
        int res = preparedStatement.executeUpdate();
        if (res > 0) {
            System.out.println("clear t_words successfully");
        }
        preparedStatement.close();
    }
}
