package com.river.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("USER")
public class UserEntity {

    @TableField
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String sex;

    private int shardingId;


}
