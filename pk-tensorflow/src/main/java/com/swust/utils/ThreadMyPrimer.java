package com.swust.utils;

public class ThreadMyPrimer extends Thread {
    int primer;

    public ThreadMyPrimer(int num) {
        this.primer = num;
    }

    public void run() {
        for (int i = 0; i < primer; i++) {
            System.err.print(primer + "\t");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        ThreadMyPrimer go = new ThreadMyPrimer(10);
        System.out.println(100);
        System.out.println(200);
        // 如何让10和5输出有序 先输出5在输出10
        go.start();
        show();
        System.out.println(300);

    }

    public static void show() {
        System.out.println("show");
        ThreadMyPrimer th = new ThreadMyPrimer(5);
        th.start();
    }
}

