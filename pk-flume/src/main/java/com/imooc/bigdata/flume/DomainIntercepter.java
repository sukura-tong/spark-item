package com.imooc.bigdata.flume;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 二次开发自定义拦截器
 *
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 1、实现Flume接口 Interceptor 并复写方法
 */
public class DomainIntercepter implements Interceptor {

    List<Event> events;

    //初始化
    @Override
    public void initialize() {
        events = new ArrayList<>();
    }

    //单个事件
    @Override
    public Event intercept(Event event) {
        //获取头信息
        Map<String, String> headers = event.getHeaders();
        //获取消息体
        byte[] body = event.getBody();
        String str = body.toString();

        if (str.contains("imooc")) {
            //给头信息添加标识
            headers.put("type", "imooc");
        } else {
            headers.put("type", "other");
        }
        return event;
    }

    //多个事件
    @Override
    public List<Event> intercept(List<Event> list) {
        events.clear();
        for (Event event : list) {
            Event intercept = intercept(event);
            events.add(intercept);
        }
        return events;
    }

    //资源释放
    @Override
    public void close() {
        events = null;
    }

    //$Bulider
    //自定义拦截器规范 实现Interceptor.Builder
    public static class Builder implements Interceptor.Builder {

        @Override
        public Interceptor build() {
            //返回一个当前类的对象
            return new DomainIntercepter();
        }

        //配置信息
        @Override
        public void configure(Context context) {

        }
    }
}
