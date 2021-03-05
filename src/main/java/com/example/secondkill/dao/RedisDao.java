package com.example.secondkill.dao;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisSentinelConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSentinelPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RedisDao {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    /**
     * 从redis中根据key获取值
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        if (StrUtil.isEmpty(key)) {
            return null;
        }
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 在redis中保存key--value
     *
     * @param key   redis key
     * @param value 值
     * @return
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置一个超时方法的key
     *
     * @param key   保存的key
     * @param value 保存的value
     * @param time  key超时时间
     * @return
     */
    public boolean setex(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    public Integer stockDecr(String key) {


        // -1 库存不足
        // -2 不存在
        // 整数是正常操作，减库存成功

        Integer result = (Integer) redisTemplate.opsForValue().get(key);
        System.out.println(result);
        if (ObjectUtil.isEmpty(result)){
            return -2;
        }

        if (result>0){
            redisTemplate.opsForValue().decrement(key);
            return result-1;
        }else {
            return -1;
        }

    }
}
