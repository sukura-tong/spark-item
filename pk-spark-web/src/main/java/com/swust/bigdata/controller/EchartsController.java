package com.swust.bigdata.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.RequestDispatcher;


/**
 * @author 雪瞳
 * @Slogan 忘时，忘物，忘我。
 * @Function
 * 使用springboot整合echart进行前端界面开发
 */
@Controller
public class EchartsController {

    @GetMapping("/echarts")
    public String showEcharts(){

        System.out.println("guolll");


        return "demo";
    }
}
