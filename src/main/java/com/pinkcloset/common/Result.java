package com.pinkcloset.common;

import lombok.Data;

/**
 * 统一 API 响应体
 * 前端所有接口均通过此包装类返回数据
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {

    /** HTTP 业务码：200 成功，其他失败 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    // ---- 静态工厂方法 ----

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "成功";
        r.data = data;
        return r;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = message;
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.message = message;
        return r;
    }
}
