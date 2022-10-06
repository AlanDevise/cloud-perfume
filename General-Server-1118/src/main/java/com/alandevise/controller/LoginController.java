package com.alandevise.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Filename: LoginController.java
 * @Package: com.alandevise.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月06日 11:41
 */

@RestController
@RequestMapping("/index")
@Api(tags = "Login测试接口", value = "登录测试接口")
@Slf4j
public class LoginController {

    @RequestMapping("/login")
    @ApiOperation("Login基本操作测试-POST")
    public String Login(){
        log.info("执行登录方法");
        return "login.html";
    }
}
