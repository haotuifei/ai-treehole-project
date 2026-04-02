package com.zxw.treehole.exception;

import com.zxw.treehole.common.ResultCode;
import lombok.Getter;

/**
 * 可预期的业务异常，由全局处理器转换为统一 JSON
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
