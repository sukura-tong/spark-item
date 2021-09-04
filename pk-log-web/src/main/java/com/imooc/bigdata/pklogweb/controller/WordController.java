package com.imooc.bigdata.pklogweb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WordController {

    @GetMapping("/word01")
    public String word01() {
        return "word01";
    }
}
