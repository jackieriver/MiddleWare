package com.river.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.mapper.UserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserService extends ServiceImpl {

    @Resource
    private UserMapper userMapper;

}
