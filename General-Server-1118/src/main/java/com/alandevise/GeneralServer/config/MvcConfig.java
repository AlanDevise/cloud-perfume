package com.alandevise.GeneralServer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Filename: MvcConfig.java
 * @Package: com.alandevise.config
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月02日 21:40
 */

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /*
    * 笔记：新建拦截器后，需要将新建的拦截器注册到容器中，才能够被SpringBoot检测到
    * */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // registry.addInterceptor(new MyInterceptor()).addPathPatterns("/**");
        // registry.addInterceptor(new EncryptInterceptor()).addPathPatterns("/**");
        // registry.addInterceptor(new DecryptInterceptor()).addPathPatterns("/**");
    }
}
