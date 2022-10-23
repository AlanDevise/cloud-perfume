package com.alandevise.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * @Filename: ResourceServerConfig.java
 * @Package: com.alandevise.config
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
                .anyRequest().authenticated()
                .and()
                .requestMatchers()
                .antMatchers("/user/**");
    }
}
