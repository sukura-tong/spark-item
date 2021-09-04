package com.swust.bigdata.item.utils;

import com.swust.bigdata.pojo.HbaseParams;
import com.swust.bigdata.pojo.HbaseSelRange;
import com.swust.bigdata.pojo.DeleteParams;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function Hbase Java API 编程
 */
public class HbaseClientJavaApiUtils {
    /**
     * 获取Hbase的连接对象
     *
     * @return
     * @throws IOException
     */
    public static Connection getHbaseConnection() throws IOException {
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "hadoop000:2181");
        Connection connection = ConnectionFactory.createConnection(configuration);
        return connection;
    }

    /**
     * 获取Hbase的表
     *
     * @param tname
     * @throws IOException
     */
    public static Table getTabelByTableName(String tname) throws IOException {
        Connection connection = getHbaseConnection();
        TableName tableName = TableName.valueOf(tname);
        Table table = connection.getTable(tableName);
        return table;
    }

    /**
     * 向Hbase中插入数据
     *
     * @param tname
     * @param params
     * @throws IOException
     */
    public static void insertToTable(String tname, List<HbaseParams> params) throws IOException {
        if (params.isEmpty()) {
            return;
        }
        Table table = getTabelByTableName(tname);
        // 设置主键
        String rowKey = params.get(0).getRowKey();
        Put put = new Put(rowKey.getBytes());
        // add data
        for (int i = 0; i < params.size(); i++) {
            HbaseParams hbaseParams = params.get(i);
            String familly = hbaseParams.getFeatureFamilly();
            String fname = hbaseParams.getFeatureName();
            String fvalue = hbaseParams.getFeatureVlaue();
            put.addColumn(familly.getBytes(), fname.getBytes(), fvalue.getBytes());
        }

        table.put(put);

        closeTable(table);
    }

    /***
     * 查询Hbase的内容
     * @param tname
     * @param range
     * @return
     * @throws IOException
     */
    public static List<HbaseParams> selectTableByName(String tname, HbaseSelRange range) throws IOException {
        Table table = getTabelByTableName(tname);
        Scan scan = new Scan();
        // 设置Hbase查询范围 粗粒度
        String start = range.getStart();
        String stop = range.getStop();

        if (start != null) {
            scan.setStartRow(start.getBytes());
        }
        if (stop != null) {
            scan.setStopRow(stop.getBytes());
        }

        // 细粒度
        String family = range.getFamily();
        String coulmn = range.getCoulmn();

        if (family != null) {
            if (coulmn != null) {
                scan.addColumn(family.getBytes(), coulmn.getBytes());
            } else {
                scan.addFamily(family.getBytes());
            }
        }

        ResultScanner scanner = table.getScanner(scan);
        Iterator<Result> iterator = scanner.iterator();

        List<HbaseParams> resdatas = new ArrayList<>();

        while (iterator.hasNext()) {
            Result result = iterator.next();
            while (result.advance()) {
                Cell cell = result.current();
                byte[] cloneFamily = CellUtil.cloneFamily(cell);
                byte[] cloneQualifier = CellUtil.cloneQualifier(cell);
                byte[] cloneRow = CellUtil.cloneRow(cell);
                byte[] cloneValue = CellUtil.cloneValue(cell);

                // 将字节数据转换为字符串
                String familyStr = new String(cloneFamily);
                String qualifierStr = new String(cloneQualifier);
                String rowStr = new String(cloneRow);
                String valueStr = new String(cloneValue);

                HbaseParams resdata = new HbaseParams();
                resdata.setRowKey(familyStr);
                resdata.setFeatureFamilly(qualifierStr);
                resdata.setFeatureName(rowStr);
                resdata.setFeatureVlaue(valueStr);
                resdatas.add(resdata);
            }
        }

        closeTable(table);

        return resdatas;
    }

    /***
     * 删除一条数据
     * @param tname
     * @throws IOException
     */
    public static void deleteTableByRowKey(String tname, DeleteParams params) throws IOException {


        Table table = getTabelByTableName(tname);

//        Configuration configuration = table.getConfiguration();
//
//        HBaseAdmin admin = new HBaseAdmin(configuration);

        // 粗粒度
        String rowkey = params.getRowkey();
        Delete delete = new Delete(rowkey.getBytes());

        String column = params.getColumn();
        String qualifer = params.getQualifer();
        // 细粒度

        if (column != null) {
            if (qualifer != null) {
                delete.addColumn(column.getBytes(), qualifer.getBytes());
            } else {
                delete.addFamily(column.getBytes());
            }
        }
        table.delete(delete);


        closeTable(table);
    }

    /**
     * Hbase数据更新
     *
     * @param tname
     * @param params
     * @throws IOException
     */
    public static void updateTableDataByRowKey(String tname, HbaseParams params) throws IOException {
        if (params == null) {
            return;
        }
        Table table = getTabelByTableName(tname);
        // 借助insert 函数实现数据更新
        String rowKey = params.getRowKey();
        String familly = params.getFeatureFamilly();
        String name = params.getFeatureName();
        String value = params.getFeatureVlaue();

        Put put = new Put(rowKey.getBytes());

        put.addColumn(familly.getBytes(), name.getBytes(), value.getBytes());

        table.put(put);

        closeTable(table);

        return;

    }

    /**
     * 关闭资源
     *
     * @param table
     */
    public static void closeTable(Table table) {
        if (table == null) {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 清空表
     */
    public static void clearTable(String tname) throws IOException {
        Table table = getTabelByTableName(tname);
        // 根据查询获得的所有行进行删除
        List<HbaseParams> params = selectTableByName(tname);

        for (HbaseParams param : params) {
            String rowKey = param.getRowKey();
            Delete delete = new Delete(rowKey.getBytes());
            table.delete(delete);
        }

    }

    /***
     * 方法重载
     * 查询Hbase表内所有内容
     * @param tname
     * @return
     * @throws IOException
     */
    public static List<HbaseParams> selectTableByName(String tname) throws IOException {
        Table table = getTabelByTableName(tname);
        Scan scan = new Scan();
        ResultScanner scanner = table.getScanner(scan);
        Iterator<Result> iterator = scanner.iterator();

        List<HbaseParams> resdatas = new ArrayList<>();

        while (iterator.hasNext()) {
            Result result = iterator.next();
            while (result.advance()) {
                Cell cell = result.current();
                byte[] cloneFamily = CellUtil.cloneFamily(cell);
                byte[] cloneQualifier = CellUtil.cloneQualifier(cell);
                byte[] cloneRow = CellUtil.cloneRow(cell);
                byte[] cloneValue = CellUtil.cloneValue(cell);

                // 将字节数据转换为字符串
                String familyStr = new String(cloneFamily);
                String qualifierStr = new String(cloneQualifier);
                String rowStr = new String(cloneRow);
                String valueStr = new String(cloneValue);

                HbaseParams resdata = new HbaseParams();
                resdata.setRowKey(rowStr);
                resdata.setFeatureFamilly(familyStr);
                resdata.setFeatureName(qualifierStr);
                resdata.setFeatureVlaue(valueStr);
                resdatas.add(resdata);
            }
        }

        closeTable(table);

        return resdatas;
    }

    /***
     * 将字符串类型的数据转为long类型
     * string---> bytes --> long
     * @param data
     * @return
     */
    public static Long getLong(String data) {
        byte[] bytes = data.getBytes();
        // 借助kava.nio.ByteBuffer将数据转化为其余类型数据
        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        long aLong = wrap.getLong();
        return aLong;
    }


    @Test
    public void Test() {
//        clearTable("access-log-hour");

        List<HbaseParams> params = null;
        try {
            params = selectTableByName("access-log-hour");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (HbaseParams param : params) {
            String vlaue = param.getFeatureVlaue();
            String name = param.getFeatureName();
            String rowKey = param.getRowKey();
            String familly = param.getFeatureFamilly();
            Long value = getLong(vlaue);
            System.out.println(rowKey + "--" + familly + "--" + name + "--" + value);
        }
    }

}
