package com.swust.bigdata.controller;

import com.swust.bigdata.domain.User;
import com.swust.bigdata.servie.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class UserController {

    @Resource
    UserService service;

    @GetMapping("/query")
    public List<User> query(){
        List<User> query = service.query();
        return query;
    }

}
