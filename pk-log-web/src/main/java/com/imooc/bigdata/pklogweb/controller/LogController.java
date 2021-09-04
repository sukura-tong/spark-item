package com.imooc.bigdata.pklogweb.controller;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 日志服务 ===> /upload ===> 落地磁盘
 * ==> 1 输出到控制台
 * ==> 2 落地到磁盘
 */
@Controller
public class LogController {

    private static final Logger logger = Logger.getLogger(LogController.class);

    @PostMapping("/upload")
    @ResponseBody
    public void upLoad(@RequestBody String info) {
        // 如果使用Post请求方式则必须使用RequestBody
        logger.info(info);
    }
}
