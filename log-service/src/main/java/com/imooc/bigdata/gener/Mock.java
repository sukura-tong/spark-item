//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.imooc.bigdata.gener;

public class Mock {
    public Mock() {
    }

    //java -cp log-service-1.0-SNAPSHOT-shaded.jar com.imooc.com.imooc.bigdata.gener.Mock http://192.168
//.9.198:9527/pk-web/upload 36B42810E46E662E
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("请传递两个参数：url code");
        } else {
            String url = args[0];
            String code = args[1];
            LogGenerator.generator(url, code);
        }
    }
}
