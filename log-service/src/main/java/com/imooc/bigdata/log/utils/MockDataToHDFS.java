package com.imooc.bigdata.log.utils;

import com.imooc.bigdata.gener.LogGenerator;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 本方法通过url设定访问的SpringBoot的upload方法实现数据上传操作
 * 通过Log4j 将数据落地到磁盘日志文件
 */
public class MockDataToHDFS {
    //com.imooc.com.imooc.bigdata.gener;
    public static void main(String[] args) throws Exception {
        String url = "http://192.168.9.198:9527/pk-web/upload";
        String code = "22";
        // 该jar包采用module导入方式
        /**
         * java -cp log-generator-1.0.jar com.imooc.com.imooc.bigdata.gen.Mock http://192.168.9.198:9527/pk-web/upload 7B0D69DF86B30950
         */
        LogGenerator.generator(url, code);
//        System.out.println(LogGenerator.class);
    }
}
