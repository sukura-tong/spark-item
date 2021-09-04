package com.swust.bigdata.controller;

import com.swust.bigdata.domain.AccessByHour;
import com.swust.bigdata.servie.HbaseServiceItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class HbaseController {

    @Resource
    private HbaseServiceItem service;

    @GetMapping("/hb")
    public List<AccessByHour> selectAll(){
        List<AccessByHour> access = service.hbaseService();
        return access;
    }
}
