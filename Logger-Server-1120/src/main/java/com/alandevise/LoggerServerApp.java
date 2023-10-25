package com.alandevise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Filename: LoggerServerApp.java
 * @Package: com.alandevise
 * @Version: V1.0.0
 * @Description: 1. 日志测试服务启动类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年10月21日 22:09
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LoggerServerApp {
    public static void main(String[] args) {
        SpringApplication.run(LoggerServerApp.class, args);
    }
}
