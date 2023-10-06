package com.alandevise.exception;

import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Filename: GlobalExceptionHandler.java
 * @Package: com.alandevise.exception
 * @Version: V1.0.0
 * @Description: 1. 全局异常拦截器
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年10月06日 15:08
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 全局异常拦截
    @ExceptionHandler
    public SaResult handlerException(Exception e) {
        e.printStackTrace();
        return SaResult.error(e.getMessage());
    }
}

