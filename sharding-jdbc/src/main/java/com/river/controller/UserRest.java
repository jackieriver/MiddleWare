package com.river.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.river.entity.UserEntity;
import com.river.mapper.UserMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("user")
public class UserRest {

    @Resource
    private UserMapper userMapper;

    @RequestMapping("insert")
    public Long insert(@RequestParam int shardingId){
        UserEntity user = new UserEntity();
        user.setUsername("小李");
        user.setSex("男");
        user.setShardingId(shardingId);
        userMapper.insert(user);
        return user.getId();
    }

    @RequestMapping("select")
    public UserEntity select(@RequestParam int shardingId){
        QueryWrapper<UserEntity> qr = new QueryWrapper<>();
        qr.eq("sharding_id", shardingId);
        return userMapper.selectOne(qr);
    }
}
