package com.study.task.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.study.task.entity.User;

public interface UserService extends IService<User> {
    User getByUsername(String username);
    User getByWechatId(String wechatId);
}