package com.example.secondkill.service.imp;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;

import com.example.secondkill.dao.RedisDao;

import com.example.secondkill.dao.SeckillItemDao;
import com.example.secondkill.dao.SeckillOrderDao;
import com.example.secondkill.dto.SeckillUrl;
import com.example.secondkill.entity.SeckillItem;
import com.example.secondkill.entity.SeckillOrder;
import com.example.secondkill.entity.User;
import com.example.secondkill.service.SeckillItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class SeckillItemServiceImpl implements SeckillItemService {


    @Autowired
    SeckillItemDao seckillItemDao;

    @Autowired
    RedisDao redisDao;

    @Autowired
    SeckillOrderDao seckillOrderDao;

    public List<SeckillItem> getSeckillList() {
        return seckillItemDao.getAll();
    }

    public SeckillItem getSeckillItem(Integer id) {
        if (ObjectUtil.isEmpty(id) || id <= 0) {
            return null;
        }

        return seckillItemDao.get(id);
    }

    public SeckillUrl getSeckillUrl(Integer id) {

        // 获取SeckillItem

        // 首先从缓存中获取,根据秒杀ID 获取seckillItem对象
        SeckillItem item = (SeckillItem) redisDao.get(String.valueOf(id));
        if (ObjectUtil.isEmpty(item)) {
            // redis缓存中没有这个数据SeckillItem
            // 从数据库获取

            item = seckillItemDao.get(id);
            if (ObjectUtil.isEmpty(item)) {
                // 数据库中没有对应ID的商品
                return new SeckillUrl(false, id);
            }

            // 保存到redis中
            redisDao.set(String.valueOf(id), item);

            // 同时把库存更新到redis中
            // key = stock_id value number
            redisDao.set("stock_" + id, item.getNumber());
        }

        // 获取到SeckillItem，组装返回对象SeckillUrl
        Date startTime = item.getStartTime();
        Date endTime = item.getEndTime();
        Date nowTime = new Date();

        // 服务器返回秒杀地址，意味着前段JS秒杀组件已经到00了，可以执行抢购了
        // 返回SeckillUrl的条件
        // 1.当前时间 小于 秒杀商品的开始时间，或大于结束时间，不是正常获取秒杀地址的请求
        if (nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()) {

            // 活动已经结束或者还没有开始
            return new SeckillUrl(false, id, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        // [startTime, nowTime ,endTime]
        // 正常返回秒杀地址
        String md5Url = seckillUrlMD5(id);

        return new SeckillUrl(true, md5Url, id, nowTime.getTime(), startTime.getTime(), endTime.getTime());
    }

    // MD5混淆
    private static final String mixKey = "SAD23N*(FY*@";

    private String seckillUrlMD5(int seckillId) {
        // 自己定的，随便写
        String source = seckillId + "," + mixKey;
        String md5Url = DigestUtil.md5Hex(source);
        return md5Url;
    }


    public boolean verifySeckillMD5(int seckillId, String md5) {

        String sMD5 = seckillUrlMD5(seckillId);
        if (StrUtil.isEmpty(md5) || !md5.equals(sMD5)) {
            return false;
        }
        return true;
    }


    public boolean executeSeckill(User user, int seckillId) {

        // key格式：phone_seckillId
        String key = user.getPhone() + "_" + seckillId;

        Integer mSeckillId = (Integer) redisDao.get(key);

        if (!ObjectUtil.isEmpty(mSeckillId)) {
            // 已经请求过，已经下单了。5分钟内部允许同一个用户第二次下单
            return false;
        }
        // redis保存用户下单缓存，key-phone_seckillId，value-seckillId
        // 设置超时时间，5分钟
        redisDao.setex(key, seckillId, 60 * 5);

        // TODO 减库存 lua脚本
        // -1 库存不足
        // -2 不存在
        // 整数是正常操作，减库存成功
        Integer result = redisDao.stockDecr("stock_" + seckillId);
        if (ObjectUtil.isEmpty(result)) {
            return false;
        }
        if (result == -1) {
            // 库存不足
            return false;
        }
        if (result == -2) {
            // key不存在
            return false;
        }
        return true;
    }

    /**
     * 下订单
     */
    @Transactional
    public void createOrder(User user, int seckillId) {
        // 数据库更新库存
        seckillItemDao.updateStock(seckillId);

        // result-->number
        // 更新SeckillItem的库存

        // 1.先去redis里面查库存
        // 2.判断库存是否可以下单（大于0）
        // 3.商品表，订单表，同时需要操作 事物 保证2个操作的一致
        // 4.减库存，下订单

        // TODO 订单生成逻辑

        // 生成秒杀订单并保存
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setSeckillItemId(seckillId);
        // 下单，未支付状态
        System.out.println(user);
        seckillOrder.setState(1);
        seckillOrder.setUserId(user.getId());
        seckillOrder.setCreateTime(new Date());

        seckillOrderDao.insert(seckillOrder);
    }


}
