package com.zxw.treehole.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 统一 REST 返回结构
 *
 * @param <T> 业务数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 业务状态码，200 表示成功 */
    private int code;
    private String message;
    private T data;
    /** 毫秒时间戳 */
    private long timestamp;

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> fail(ResultCode rc) {
        return new Result<>(rc.getCode(), rc.getMessage(), null, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> fail(ResultCode rc, String message) {
        return new Result<>(rc.getCode(), message, null, Instant.now().toEpochMilli());
    }
}
