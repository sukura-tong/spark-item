package com.imooc.bigdata.log.utils;

import com.imooc.bigdata.log.domain.Access;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MockData {

    private static final String path = "http://192.168.9.198:9527/pk-web/upload";

    @Test
    public void testUpLoad() throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        for (int i = 1; i <= 100; i++) {

            Thread.sleep(100);
            Access access = new Access();
            access.setId(i);
            access.setName("name" + i);
            access.setTime(sdf.format(new Date()));
            //上传数据
            UploadUtils.upload(path, access.toString());
        }
    }

}
