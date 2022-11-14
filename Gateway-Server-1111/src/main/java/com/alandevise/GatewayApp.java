package com.alandevise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Filename: GatewayApp.java
 * @Package: com.alandevise
 * @Version: V1.0.0
 * @Description: 1. Gateway网关主启动类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年11月13日 20:56
 */

@SpringBootApplication
public class GatewayApp {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApp.class,args);
    }
}
