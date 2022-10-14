package com.alandevise.handler;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Filename: MyAccessDeniedHandler.java
 * @Package: com.alandevise.handler
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月15日 0:14
 */

@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 设置响应状态码
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setHeader("Content-Type","application/json;charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.write("{\"status\":\"error\",\"msg\":\"权限不足，请联系管理员\"}");
        printWriter.flush();
        printWriter.close();
    }
}
