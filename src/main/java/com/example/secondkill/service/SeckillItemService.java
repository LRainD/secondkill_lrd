package com.example.secondkill.service;


import com.example.secondkill.dto.SeckillUrl;
import com.example.secondkill.entity.SeckillItem;
import com.example.secondkill.entity.User;

import java.util.List;

public interface SeckillItemService {

    List<SeckillItem> getSeckillList();


    SeckillItem getSeckillItem(Integer id);


    SeckillUrl getSeckillUrl(Integer id);

    boolean verifySeckillMD5(int seckillId, String md5);

    boolean executeSeckill(User user, int seckillId);

    void createOrder(User user, int seckillId);
}
