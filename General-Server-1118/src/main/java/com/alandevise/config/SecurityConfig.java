package com.alandevise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Filename: SecurityConfig.java
 * @Package: com.alandevise.config
 * @Version: V1.0.0
 * @Description: 1. SpringSecurity配置类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月06日 14:59
 */

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // 重写configure
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 表单提交
        http.formLogin()
                // 当发现/login时，认为是登录，登录页中login action 同名
                .loginProcessingUrl("/login")
                // 自定义登录页
                .loginPage("/login.html")
                // 登录成功后跳转页面，Post请求
                .successForwardUrl("/toMain");

        // 授权认证
        http.authorizeHttpRequests()
                // 除了/login.html不需要认证
                .antMatchers("/login.html").permitAll()
                // 所有请求都必须被验证，所有请求都必须登录之后访问
                .anyRequest().authenticated();

        // 关闭csrf防护
        http.csrf().disable();
    }

    @Bean
    public PasswordEncoder getPw() {
        return new BCryptPasswordEncoder();
    }
}
