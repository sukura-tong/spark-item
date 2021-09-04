package com.swust.nfy;

import java.util.Scanner;

public class ResultTest {
    public static void main(String[] args) {
//   所谓“水仙花数”是指一个三位数,其各位数字立方和等于该数 ；
        // 1 输入3位数
        // 2 逻辑
        // 每个位数的数字的立方求和
        // 每个位的数字 怎么获取
        // 相加

        Scanner scanner = new Scanner(System.in);
        int i = scanner.nextInt();
        while (i > 999 || i < 100) {
            System.out.println("input error!");
            i = scanner.nextInt();
        }

        int first = i / 100;
        int second = (i - 100 * first) / 10;
        int third = i % 10;

        double sum = Math.pow(first, 3) + Math.pow(second, 3) + Math.pow(third, 3);

        if (sum == Double.valueOf(i)) {
            System.out.println("yes");
        }
    }
}
