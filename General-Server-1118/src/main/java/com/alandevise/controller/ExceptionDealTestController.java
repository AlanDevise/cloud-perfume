package com.alandevise.controller;

import com.alandevise.exception.BusinessException;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Filename ExceptionDealTestController.java
 * @Package com.alandevise.controller
 * @Version V1.0.0
 * @Description 1. 测试controller异常统一处理
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2023-12-04 17:35
 */

@RestController
@RequestMapping("/general")
@Api(tags = "Exception统一处理测试接口", value = "Exception统一处理测试接口")
@Slf4j
public class ExceptionDealTestController {
    @RequestMapping(value = "/test1")
    public String test1() {
        throw new BusinessException("1", "test1 错误");
    }

    @RequestMapping(value = "/test2")
    public String test2() {
        throw new BusinessException("2", "test2 错误");
    }

    @RequestMapping(value = "/test3")
    public String test3() {
        throw new BusinessException("3", "test3 错误");
    }
}