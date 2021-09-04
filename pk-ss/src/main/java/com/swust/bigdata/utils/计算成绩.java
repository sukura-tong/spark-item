package com.swust.bigdata.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class 计算成绩 {
    public static void main(String[] args) throws IOException {
        String path = "F:\\imooc\\pk-tensorflow\\data\\成绩.csv";
        File file = new File(path);
        InputStream in = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        List<Double> lists = new ArrayList<>();
        while ((line = reader.readLine()) != null){

            String[] split = line.split(",");
            String shiyan = split[1];
            String pingshi = split[2];
            String kaoshi = split[3];

            double test = Double.parseDouble(shiyan) * 0.2 + Double.parseDouble(pingshi) + Double.parseDouble(kaoshi) * 0.6;
            lists.add(test);
        }

        for (double EL : lists){
            double ceil = Math.ceil(EL);
            String dat = ceil + "";
            System.out.println(dat.substring(0,2));
        }

    }
}
