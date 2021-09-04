package com.swust.utils;

import java.util.Random;

public class ExceptionUtisl {
    public static void main(String[] args) {
        int[][] words = new int[5][6];
        Random random = new Random();
        // 最为基础的异常 书写异常
//        System.out.println(j);
        for (int i = 0; i < words.length; i++) {
            // 逻辑异常
            for (int j = 0; j < words[0].length; j++) {
                words[i][j] = random.nextInt(10);
            }
        }

        for (int[] x : words) {
            for (int y : x) {
                System.out.print(y + "\t");
            }
            System.out.println();
        }
    }
}
