package com.alandevise.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @Filename: LoginController.java
 * @Package: com.alandevise.controller
 * @Version: V1.0.0
 * @Description: 1. 登录控制类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月06日 11:41
 */

@Controller
@Api(tags = "Login测试接口", value = "登录测试接口")
@Slf4j
public class LoginController {

    // @Secured("ROLE_admin")
    @PreAuthorize("hasRole('abc')")
    @PostMapping("/toMain")
    @ApiOperation("Login基本操作测试-POST")
    public String toMain(HttpServletRequest httpServletRequest) {
        log.info("执行登录方法");
        return "redirect:main.html";
    }

    @PostMapping("/toError")
    @ApiOperation("Login基本操作测试-POST")
    public String toError() {
        log.info("因为登录失败，重新执行登录方法");
        return "redirect:error.html";
    }
}
