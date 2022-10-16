package com.alandevise;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * @Filename: GeneralServerApp.java
 * @Package: com.alandevise
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-20 15:20
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@MapperScan("com.alandevise.dao")
public class GeneralServerApp {
    public static void main(String[] args) {
        SpringApplication.run(GeneralServerApp.class, args);
    }
}
