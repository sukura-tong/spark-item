package com.imooc.bigdata.flume;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Domian implements Interceptor {
    List<Event> events;

    @Override
    public void initialize() {
        events = new ArrayList<>();
    }

    @Override
    public Event intercept(Event event) {
        String string = event.getBody().toString();
        Map<String, String> headers = event.getHeaders();

        if (string.contains("imooc")) {
            headers.put("type", "imooc");
        } else {
            headers.put("type", "other");
        }
        return event;
    }

    @Override
    public List<Event> intercept(List<Event> list) {
        events.clear();
        for (Event event : list) {
            Event intercept = intercept(event);
            events.add(intercept);
        }

        return events;
    }

    @Override
    public void close() {
        events = null;
    }

    public static class Builder implements Interceptor.Builder {

        @Override
        public Interceptor build() {
            return new Domian();
        }

        @Override
        public void configure(Context context) {

        }
    }
}
