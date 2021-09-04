//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.imooc.bigdata.gener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class LogGenerator {
    public LogGenerator() {
    }

    public static void generator(String url, String code) throws Exception {
        if (null != code && !code.trim().equals("")) {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            CloseableHttpResponse response = null;

            try {
                HttpGet httpGet = new HttpGet("http://apis.imooc.com/?icode=" + code);
                response = httpClient.execute(httpGet);
                HttpEntity responseEntity = response.getEntity();
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new Exception("请检查网络");
                }

                String content = EntityUtils.toString(responseEntity);
                JSONObject jsonObject = JSON.parseObject(content);
                int data = jsonObject.getInteger("code");
                String[] os = new String[]{"Android", "iOS", "WP"};
                String[] version = new String[]{"1.1.0", "1.1.1", "1.1.2", "1.1.3"};
                long[] catagory = new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L};
                String[] user = new String[]{"user1", "user2", "user3", "user4", "user5", "user6", "user7", "user8", "user9", "user10"};
                Random random = new Random();

                for (int i = 0; i <= 10000; ++i) {
                    Log log = new Log();
                    log.setUser(user[random.nextInt(user.length)]);
                    Thread.sleep(1L);
                    log.setOs(os[random.nextInt(os.length)]);
                    log.setVersion(version[random.nextInt(version.length)]);
                    log.setIp(getRandomIp());
                    long cata = catagory[random.nextInt(catagory.length)];
                    log.setCatagory(cata);
                    log.setNewId(cata + "" + random.nextInt(10));
                    long start = (new Date()).getTime();
                    log.setStart(start);
                    log.setEnd((long) (random.nextInt(10) * 1000));
                    log.setTraffic((long) (5000 + random.nextInt(1000)));
                    System.out.println(log.toString());
                    Utils.upload(url, log.toString());
                }
            } finally {
                try {
                    if (httpClient != null) {
                        httpClient.close();
                    }

                    if (response != null) {
                        response.close();
                    }
                } catch (IOException var25) {
                    var25.printStackTrace();
                }

            }

        } else {
            throw new Exception("code不能为空...");
        }
    }

    public static String getRandomIp() {
        int[][] range = new int[][]{{607649792, 608174079}, {1038614528, 1039007743}, {1783627776, 1784676351}, {2035023872, 2035154943}, {2078801920, 2079064063}, {-1950089216, -1948778497}, {-1425539072, -1425014785}, {-1236271104, -1235419137}, {-770113536, -768606209}, {-569376768, -564133889}};
        Random rdint = new Random();
        int index = rdint.nextInt(10);
        String ip = num2ip(range[index][0] + (new Random()).nextInt(range[index][1] - range[index][0]));
        return ip;
    }

    public static String num2ip(int ip) {
        int[] b = new int[4];
        String x = "";
        b[0] = ip >> 24 & 255;
        b[1] = ip >> 16 & 255;
        b[2] = ip >> 8 & 255;
        b[3] = ip & 255;
        x = Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "." + Integer.toString(b[2]) + "." + Integer.toString(b[3]);
        return x;
    }
}
