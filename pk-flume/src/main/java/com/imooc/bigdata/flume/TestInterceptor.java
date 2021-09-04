package com.imooc.bigdata.flume;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 *
 * @author 雪瞳
 * @Slogan 忘时，忘物，忘我。
 * @Function
 * 实现flume自定义 拦截器
 */
public class TestInterceptor implements Interceptor {

    private List<Event> events;
    // 拦截器初始化
    @Override
    public void initialize() {
       if (events != null){
           events = new ArrayList<>();
       }
    }
    // 单个事件拦截
    @Override
    public Event intercept(Event event) {
        // 获取事件主体
        String body = event.getBody().toString();
        // 获取请求头数据
        Map<String, String> headers = event.getHeaders();

        if (body.contains("xuetong")){
            headers.put("xuetong","success");
        }else {
            headers.put("other","error");
        }

        return event;
    }
    //多个事件拦截
    @Override
    public List<Event> intercept(List<Event> list) {
        // 初始化
        events.clear();
        for (Event event : list){
            Event intercept = intercept(event);
            events.add(intercept);
        }
        return events;
    }

    @Override
    public void close() {
        events = null;
    }
    // 实现Bulider方法
    public static class Builder implements Interceptor.Builder{

        @Override
        public Interceptor build() {
            return new TestInterceptor();
        }

        @Override
        public void configure(Context context) {

        }
    }
}
