package com.pinkcloset.dto;

import lombok.Data;

/**
 * 登录请求体
 * account 可以是手机号或邮箱
 */
@Data
public class LoginRequest {
    /** 账号：手机号 或 邮箱 */
    private String account;
    /** 密码（明文，后端加密后比对） */
    private String password;
}
