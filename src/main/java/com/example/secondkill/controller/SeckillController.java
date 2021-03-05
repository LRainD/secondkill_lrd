package com.example.secondkill.controller;


import cn.hutool.core.util.ObjectUtil;

import com.example.secondkill.dto.ResponseResult;
import com.example.secondkill.dto.SeckillUrl;
import com.example.secondkill.entity.SeckillItem;
import com.example.secondkill.entity.User;
import com.example.secondkill.exception.SeckillException;
import com.example.secondkill.exception.UserException;
import com.example.secondkill.service.SeckillItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    SeckillItemService seckillItemService;


    @RequestMapping("/seckillList")
    public ModelAndView seckillList() {
        // 参数Model

        List<SeckillItem> list = seckillItemService.getSeckillList();

        ModelAndView mv = new ModelAndView();

        mv.addObject("list", list);
        mv.setViewName("seckill_list");

        return mv;
    }

    @RequestMapping(value = "/detail/{seckillId}", method = RequestMethod.GET)
    public String seckillDetail(@PathVariable("seckillId") Integer seckillId, Model model) {
        // 参数Model

        SeckillItem item = seckillItemService.getSeckillItem(seckillId);

        if (ObjectUtil.isEmpty(item)) {
            throw new SeckillException("商品参数错误");
        }

        model.addAttribute("item", item);

        return "detail";
    }


    /**
     * 获取服务器时间
     *
     * @return
     */
    @RequestMapping(value = "/now", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult<Long> now() {
        Date now = new Date();
        return new ResponseResult<Long>(true, now.getTime(), "ok");
    }

    /**
     * 获取商品的秒杀地址
     */
    @RequestMapping(value = "/getSeckillUrl/{seckillId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<SeckillUrl> getSeckillUrl(@PathVariable("seckillId") Integer seckillId, HttpSession session) {

        // 判断用户是否登录，没有登录直接报错
        User user = (User) session.getAttribute("user");
        if (ObjectUtil.isEmpty(user)) {
            // 没有登录
            throw new UserException("没有登录");
        }

        ResponseResult<SeckillUrl> result = new ResponseResult<SeckillUrl>();
        // 获取秒杀商品的下单地址URL


        try {
            SeckillUrl seckillUrl = seckillItemService.getSeckillUrl(seckillId);
            result.setData(seckillUrl);
            result.setSuccess(true);
            result.setMessage("ok");
            return result;
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            return result;
        }

    }


    /**
     * 秒杀下单
     */
    @RequestMapping(value = "/execute/{seckillId}/{md5}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<SeckillUrl> executeSeckill(@PathVariable("seckillId") Integer seckillId, @PathVariable("md5") String md5, HttpSession session) {

        ResponseResult<SeckillUrl> result = new ResponseResult<SeckillUrl>();

        //  验证请求的URL是否正确
        boolean access = seckillItemService.verifySeckillMD5(seckillId, md5);
        if (!access) {
            // 请求的URL,MD5验证不通过
            result.setSuccess(false);
            result.setMessage("md5 fail");
            return result;
        }
        // 是否已经登录
        User user = (User) session.getAttribute("user");
        System.out.print("订单user:");
        System.out.println(user);
        if (ObjectUtil.isEmpty(user)) {
            result.setSuccess(false);
            result.setMessage("user not login");
            return result;
        }

        // 限制每一个用户只可以发送一次请求，第二次请求（在5分钟内）不在处理


        // 减库存，redis做并发减库存操作
        boolean success = seckillItemService.executeSeckill(user, seckillId);
        System.out.println("减库存"+success);
        // 下订单
        if (!success) {
            result.setSuccess(false);
            result.setMessage("order fail");
            return result;
        }
        seckillItemService.createOrder(user, seckillId);


        result.setSuccess(true);
        result.setMessage("ok");

        return result;
    }
}
