package com.example.secondkill.dao;

import com.example.secondkill.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {

    public User getUser(int id);

    void insert(User user);

    User getUserByPhone(String phone);
}
