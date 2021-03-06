package com.example.secondkill.controller;


import cn.hutool.core.util.StrUtil;

import com.example.secondkill.entity.User;
import com.example.secondkill.exception.UserException;
import com.example.secondkill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/seckill")
public class UserController {

    @Autowired
    UserService userService;


    @RequestMapping(value = "/index")
    public String index() {
        return "login";
    }


    @RequestMapping(value = "/getUser")
    @ResponseBody
    public User getUser1(int id) {

        return userService.getUser(id);
    }


    // 请求地址 http://localhost:8080/seckill/getUser/1
    @RequestMapping(value = "/getUser/{uid}")
    @ResponseBody
    public User getUser2(@PathVariable("uid") int uid) {
        return userService.getUser(uid);
    }


    // 用户登录
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(String phone, String password, HttpServletRequest request) {

        if (StrUtil.isEmpty(phone) || phone.length() != 11) {
            throw new UserException("手机号不正确");
        }
        if (StrUtil.isEmpty(password)) {
            throw new UserException("密码不正确");
        }

        if (userService.login(phone, password)) {

            // 登录成功
            request.getSession().setAttribute("user", userService.getUserByPhone(phone));
            System.out.println("执行到这里");
            return "forward:/seckill/seckillList";
        }
        System.out.println("前往注册");
        return "regist";
    }


    @RequestMapping(value = "/regist", method = RequestMethod.POST)
    public String regist(String phone, String password1, String password2) {


        // 前段是JS验证，不可靠，可以略过
        // 后端验证，验证参数的合法性
        if (StrUtil.isEmpty(phone) || phone.length() != 11) {
            throw new UserException("手机号不正确");
        }
        if (StrUtil.isEmpty(password1) || StrUtil.isEmpty(password2) || !password1.equals(password2)) {
            throw new UserException("密码不一致");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(password1);

        userService.regist(user);

        return "login";
    }
}
