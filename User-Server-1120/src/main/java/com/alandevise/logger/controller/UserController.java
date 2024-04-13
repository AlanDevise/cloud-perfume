package com.alandevise.logger.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Filename: UserController.java
 * @Package: com.alandevise.logger.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月23日 23:14
 */

@RestController
@RequestMapping("/user")
public class UserController {

    private static Cookie cookie;

    /**
     * 获取当前用户
     *
     * @param authentication 认证信息
     */
    @GetMapping("/getCurrentUser")
    public Object getCurrentUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {

        String remoteAddr = request.getRemoteAddr();
        System.out.println(remoteAddr);

        String sourceIp = null;

        String ipAddresses = request.getHeader("x-forwarded-for");

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getRemoteAddr();
        }
        if (!StringUtils.isEmpty(ipAddresses)) {
            sourceIp = ipAddresses.split(",")[0];
        }
        System.out.println(sourceIp);

        cookie = new Cookie("GeneratedIP", sourceIp);  //对比入参数据
        response.addCookie(cookie);

        return authentication.getPrincipal();
    }

    /**
     * 获取Cookie中的值
     */
    @GetMapping("/checkAccessTokenWithIP")
    public Object getCookies(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        String remoteAddr = request.getRemoteAddr();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("GeneratedIP")) {
                    if (remoteAddr.equals(cookie.getValue())) {
                        return "AccessToken与IP一致";
                    } else {
                        return "不一致";
                    }
                }
            }
        }
        return "不一致";
    }
}
