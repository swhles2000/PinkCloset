package com.pinkcloset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体 - 对应数据库 user 表
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 姓名（必填） */
    private String name;

    /** 性别：男 / 女 / 其他 */
    private String gender;

    /** 年龄 */
    private Integer age;

    /** 手机号（唯一，可登录） */
    private String phone;

    /** 邮箱（唯一，可登录） */
    private String email;

    /** 密码（明文存储，不序列化到 JSON 响应） */
    @JsonIgnore
    private String password;

    /** 头像路径 */
    private String avatar;

    /** 软删除标记：0=正常，1=已注销 */
    @TableLogic
    private Integer deleted;

    /** 注册时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
