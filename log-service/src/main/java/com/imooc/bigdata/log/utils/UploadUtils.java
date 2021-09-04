package com.imooc.bigdata.log.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 上传日志数据工具类
 */
public class UploadUtils {
    /**
     * 该方法会调用spring-boot定义的controller控制器，根据传进来的URL进行操作
     *
     * @param path
     * @param log
     */
    public static void upload(String path, String log) {
        try {
            URL url = new URL(path);
            //打开链接 获取连接对象
            URLConnection connection = url.openConnection();
            HttpURLConnection conn = (HttpURLConnection) connection;
            //设置请假方式为post
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            //获取输出流
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(log.getBytes());
            outputStream.flush();
            outputStream.close();
            //查看返回码
            System.err.println(conn.getResponseCode());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
