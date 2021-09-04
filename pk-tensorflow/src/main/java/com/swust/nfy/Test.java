package com.swust.nfy;

import org.apache.spark.SparkConf;

import java.io.*;
import java.util.Scanner;

public class Test {
    static int i = 1;

    public static void main(String[] args) throws Exception {
        int j = 2;
//        System.out.println(j);
//        System.out.println(i);
//        Test test = new Test();
//        test.show();
//        showInt();
        readByFile();
//        boolean b = j == 0;
//        int i = j == 0 ? 1 : 2;
//        int i = 100;
//        short jj = (short) i;
//
//        System.out.println(i >>> 4);
    }

    //    System.out.println();
    public void show() {
        System.out.println(this.i);
    }

    public static void showInt() {
/**
 * 输入一个字符串 --> int 类型 输出
 */
        // 字符串转换成字符型

        // 如何从文件里读数据实现转换  I/O 文件流


        // 如何从控制台 获取这个字符串输入
        Scanner scanner = new Scanner(System.in);
        String data = scanner.nextLine();
        System.out.println(data.getClass());


        // 字符串转换成整形
//        Integer res = Integer.valueOf(data);
//        int res2 = Integer.parseInt(data);

        // 字符串 --> 字符 转换
//        char[] chars = data.toCharArray();
//        char c = chars[0];
//        Character character1 = Character.valueOf(c);
//        character1.
//        System.out.println(character1.getClass());
//        for (char x : chars){
//            System.out.print(x);
//        }


//        Character character = Character.valueOf(data.toCharArray()[0]);
//        System.out.println(character.getClass());
        // 输出整形 数据

    }

    public static void readByFile() throws Exception {

        // 如何从文件里读数据实现转换  I/O 文件流
        //  1  从文件读数据
        // 1.1 文件在哪里
        // 1.2 怎么读 读多少
        // 1.3 如何结束
        //  2  写一个方法 实现数据的转换
        //  3  展示

        // 相对路径
        String input = "./testdata/test.txt";
        // 绝对路径
//        String absInputPath = "F:\\imooc\\testdata\\test.txt";


        // 文件
        // 遇到不会的问题
        // 1 百度
        // 2 追源码
        // 3 读 API
        // 4 问同学
        // idea 开发工具


        String na = input;
        Reader rea = new FileReader(na);
        BufferedReader read = new BufferedReader(rea);

        Dog dog = new Dog();
//        if (dog instanceof ){
//            System.out.println("yes");
//        }else {
//            System.out.println("no");
//        }

//        File file = new File(input);
//        BufferedReader reader = new BufferedReader(new FileReader(file));
//        BufferedReader reader = new BufferedReader(new FileReader(new File(input)));

//        String data = null;
//        while ((data = read.readLine()) != null){
//            System.out.println(data);
//        }
    }
}
