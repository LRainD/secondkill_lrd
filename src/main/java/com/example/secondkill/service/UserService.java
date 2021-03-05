package com.example.secondkill.service;


import com.example.secondkill.entity.User;

public interface UserService {

    User getUser(int id);

    boolean login(String phone, String password);

    void regist(User user);

    User getUserByPhone(String phone);
}
