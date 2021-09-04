package com.swust.bigdata.servie;

import com.swust.bigdata.domain.User;
import com.swust.bigdata.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * service 层
 */

@Service
public class UserService {

    @Resource
    UserRepository repository;

    public void save(User user){
        repository.save(user);
    }

    public List<User> query(){
        List<User> users = repository.findAll();
        return users;
    }
}
