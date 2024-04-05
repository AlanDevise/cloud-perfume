package com.alandevise.GeneralServer.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Filename: MyInterceptor.java
 * @Package: com.alandevise.interceptor
 * @Version: V1.0.0
 * @Description: 1. 拦截器实现Demo
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月02日 21:35
 */

@Slf4j
public class MyInterceptor implements HandlerInterceptor {

    // private final Logger logger = LoggerFactory.getLogger(myAdvice.class);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        log.info("[Interceptor] 这里是拦截器预处理");
        return true;
    }
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        log.info("[Interceptor] 这里是拦截器后处理");
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        log.info("[Interceptor] 这里是拦截器完成后处理");
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
