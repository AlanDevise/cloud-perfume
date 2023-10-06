package com.alandevise;

import cn.dev33.satoken.SaManager;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    public static void main(String[] args) throws JsonProcessingException {
        SpringApplication.run(GatewayApp.class, args);
        System.out.println("启动成功，Sa-Token 配置如下：" + SaManager.getConfig());
    }
}
