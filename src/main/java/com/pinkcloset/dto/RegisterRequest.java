package com.pinkcloset.dto;

import lombok.Data;

/**
 * 注册请求体
 */
@Data
public class RegisterRequest {
    /** 姓名（必填） */
    private String name;
    /** 性别（可选）：男 / 女 / 其他 */
    private String gender;
    /** 年龄（可选） */
    private Integer age;
    /** 手机号（必填，唯一） */
    private String phone;
    /** 邮箱（可选） */
    private String email;
    /** 新密码 */
    private String password;
    /** 确认密码 */
    private String confirmPassword;
}
