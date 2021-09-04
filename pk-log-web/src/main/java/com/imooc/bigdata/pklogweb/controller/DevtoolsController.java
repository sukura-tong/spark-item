package com.imooc.bigdata.pklogweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DevtoolsController {

    @ResponseBody
    @RequestMapping("/test01")
    public String test01() {
        return "test01";
    }

    @ResponseBody
    @RequestMapping("/test02")
    public String test02() {
        return "test02";
    }
}
