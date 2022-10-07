package com.alandevise.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Filename: MyAuthenticationSuccessHandler.java
 * @Package: com.alandevise.handler
 * @Version: V1.0.0
 * @Description: 1. 登陆成功处理器
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月07日 11:13
 */

public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final String url;

    public MyAuthenticationSuccessHandler(String url) {
        this.url = url;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 获取登录用户信息
        User user = (User) authentication.getPrincipal();
        System.out.println(user.getUsername());
        System.out.println(user.getPassword());
        System.out.println(user.getAuthorities());

        // 请求转发到配置的url地址
        response.sendRedirect(url);
    }
}
