//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.imooc.bigdata.gener;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
    public Utils() {
    }

    public static void upload(String path, String log) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStream out = conn.getOutputStream();
            out.write(log.getBytes());
            out.flush();
            out.close();
            int code = conn.getResponseCode();
            System.out.println(code);
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }
}
