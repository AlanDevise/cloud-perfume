package com.alandevise.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @Filename: MySQLTest.java
 * @Package: com.alandevise.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-22 11:23
 */

@RestController
@RequestMapping("/general")
@Api(tags = "MySQL测试接口", value = "MySQL测试接口")
@Slf4j
public class MySQLTest {

    @GetMapping("/getTest")
    @ApiOperation("MySQL基本操作测试-GET")
    public String FirstTest() {

        return null;
    }
}
