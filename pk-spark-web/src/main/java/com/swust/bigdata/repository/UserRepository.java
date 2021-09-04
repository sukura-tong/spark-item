package com.swust.bigdata.repository;

import com.swust.bigdata.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 第一个参数是实体类
 * 第二个参数是实体类主键所对应的类型
 * 数据访问层
 */
public interface UserRepository extends JpaRepository<User,Integer> {
}
