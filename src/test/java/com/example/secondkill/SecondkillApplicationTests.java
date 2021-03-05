package com.example.secondkill;

import com.example.secondkill.dao.RedisDao;
import com.example.secondkill.entity.SeckillItem;
import com.example.secondkill.service.SeckillItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
class SecondkillApplicationTests {


    @Autowired
    SeckillItemService seckillItemService;

    @Autowired
    RedisDao redisDao;

    @Test
    void contextLoads() {
//        List<SeckillItem> list = seckillItemService.getSeckillList();
//        for (SeckillItem i : list){
//            System.out.println(i);
//        }
        System.out.println(redisDao.set("test","test2"));
    }



}
