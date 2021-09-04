package com.swust.bigdata.servie;

import com.swust.bigdata.config.RedisConfigurationApp;
import com.swust.bigdata.domain.AccessByStruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class RedisServiceItem {



    RedisTemplate<String, String> redisTemplate;

    public void getRedisConnection(){
        redisTemplate = RedisConfigurationApp.redisTemplate(RedisConfigurationApp.redisConnectionFactory());
    }


    public List<AccessByStruct> selectAll(String day){

        getRedisConnection();

        List<AccessByStruct> resluts = new ArrayList<>();

        String key = "day-province-cnts-" + day;

        Map<String, String> all = HGetAll(key);

        Set<Map.Entry<String, String>> entries = all.entrySet();

        for (Map.Entry<String, String> entry : entries){
            String province = entry.getKey();
            String nums = entry.getValue();
//            System.out.println(province + "--" + nums);

            AccessByStruct data = new AccessByStruct();
            data.setProvince(province);
            data.setNums(nums);
            data.setDay(day);
            resluts.add(data);

        }
        return resluts;
    }

    public Map<String,String> HGetAll(String key){

        Map<String, String> execute = (Map<String, String>)redisTemplate.execute((RedisCallback<Map<String, String>>) connection -> {
            Map<byte[], byte[]> map = connection.hGetAll(key.getBytes());
            Map<String, String> res = new HashMap<>(map.size());

            Set<Map.Entry<byte[], byte[]>> entries = map.entrySet();

            for (Map.Entry<byte[], byte[]> entry : entries) {
                byte[] entryKey = entry.getKey();
                String keyString = new String(entryKey);
                byte[] value = entry.getValue();
                String valueString = new String(value);

                res.put(keyString, valueString);
            }
            return res;
        });

        return execute;
    }

}
