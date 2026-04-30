package com.pinkcloset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 超级管理员实体 - 对应数据库 user_root 表
 */
@Data
@TableName("user_root")
public class UserRoot {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 管理员用户名 */
    private String username;

    /** 密码（MD5 存储，不序列化到 JSON 响应） */
    @JsonIgnore
    private String password;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
