package com.alandevise.logger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * @Filename: ResourceServerConfig.java
 * @Package: com.alandevise.logger.config
 * @Version: V1.0.0
 * @Description: 1. 资源服务器配置
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月23日 23:12
 */

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // 所有请求均需被验证
                .anyRequest().authenticated()
                .and()
                .requestMatchers()
                // 放行特定资源
                .antMatchers("/user/**");
    }
}
