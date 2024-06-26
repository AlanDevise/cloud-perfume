package com.alandevise.logger.config;

import com.alandevise.GeneralServer.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.annotation.Resource;

/**
 * @Filename: AuthorizationServerConfig.java
 * @Package: com.alandevise.logger.config
 * @Version: V1.0.0
 * @Description: 1. 授权服务器配置
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月23日 23:04
 */

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private UserService userService;

    @Resource
    @Qualifier("redisTokenStore")
    private TokenStore tokenStore;

    // [!!!]使用密码模式时需重写此方法
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(userService)
                .tokenStore(tokenStore);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                // 配置clientID
                .withClient("admin")
                // 配置clientSecret
                .secret(passwordEncoder.encode("112233"))
                // 配置访问token的有效期
                .accessTokenValiditySeconds(600)
                // 配置redirect-uri，用于授权成功后重定向跳转
                .redirectUris("https://www.baidu.com")
                // 配置申请的权限范围
                .scopes("all")
                // 配置grant_type，表示授权类型
                // .authorizedGrantTypes("authorization_code"); // 授权码模式
                .authorizedGrantTypes("password");  // 密码模式

    }
}
