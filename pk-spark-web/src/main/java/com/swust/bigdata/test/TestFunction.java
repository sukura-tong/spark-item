package com.swust.bigdata.test;

import com.swust.bigdata.servie.RedisServiceItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


public class TestFunction {


    RedisServiceItem serviceItem = new RedisServiceItem();

    public void test(){
        serviceItem.selectAll("2020-12-13");
    }
    public static void main(String[] args) {
        TestFunction testFunction = new TestFunction();
        testFunction.test();
    }
}
