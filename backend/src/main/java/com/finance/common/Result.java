package com.finance.common;

import lombok.Data;

/**
 * 统一 API 响应格式
 */
@Data
public class Result<T> {
    private int code;
    private T data;
    private String message;

    private Result(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(0, data, "success");
    }

    public static <T> Result<T> success() {
        return new Result<>(0, null, "success");
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, null, message);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(1, null, message);
    }
}
