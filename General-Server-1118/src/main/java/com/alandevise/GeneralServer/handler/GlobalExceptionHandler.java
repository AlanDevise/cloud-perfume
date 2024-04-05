package com.alandevise.GeneralServer.handler;

import com.alandevise.GeneralServer.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Filename GlobalExceptionHandler1.java
 * @Package com.alandevise.handler
 * @Version V1.0.0
 * @Description 1.
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2023-12-04 17:45
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{
    @ExceptionHandler({BusinessException.class})
    @ResponseStatus(HttpStatus.OK)
    public String BusinessExceptionReply(BusinessException businessException){
        return businessException.getCode()+"\n"+businessException.getMessage();
    }
}
