package com.swust.bigdata.controller;

import com.swust.bigdata.domain.AccessByStruct;
import com.swust.bigdata.servie.GetHttpConnection;
import com.swust.bigdata.servie.RedisServiceItem;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 雪瞳
 * @Slogan 忘时，忘物，忘我。
 * @Function
 * redis connection controller
 */
@Controller
public class RedisConnController {

    @Resource
    RedisServiceItem redisService;

    @Resource
    GetHttpConnection connection;

    @GetMapping("/redis")
    public String showRedis(){
        String day = "2020-12-13";
        List<AccessByStruct> accesses = redisService.selectAll(day);

        List<AccessByStruct> access = getTopSeven(accesses);

        // save data by session
        HttpServletRequest req = connection.getHttpServletRequestByJava();
        HttpSession session = req.getSession();

        JSONObject json = new JSONObject();

        List<String> days = new ArrayList<>();
        List<String> provinces = new ArrayList<>();
        List<String> nums = new ArrayList<>();

        for (AccessByStruct struct : access){
            days.add(struct.getDay());
            provinces.add(struct.getProvince());
            nums.add(struct.getNums());
        }
        json.put("days",days);
        json.put("provinces",provinces);
        json.put("nums",nums);

        session.setAttribute("jsonData",json);
        session.setAttribute("metaData", access);

        return "redis_demo";
    }

    public List<AccessByStruct> getTopSeven(List<AccessByStruct> access){

        List<AccessByStruct> results = new ArrayList<>();


        for (AccessByStruct struct : access){
            String nums = struct.getNums();
            if (results.size() < 7 ){
                results.add(struct);
            }else {
                int[] min = getMin(results);
                int index = min[0];
                results.remove(index);
                results.add(struct);
            }
        }
        return results;
    }

    public int[] getMin(List<AccessByStruct> results){
        int index = 0;
        int min = Integer.valueOf(results.get(0).getNums());
       for (int i = 1; i < results.size(); i++){
           Integer current = Integer.valueOf(results.get(i).getNums());
           if (current < min){
               index = i;
               min = current;
           }
       }

        int[] ints = {index, min};
       return ints;
    }


}
