package com.alandevise.superserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Filename: SuperServerApp.java
 * @Package: com.alandevise.superserver.com.alandevise.superserver
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年08月29日 20:36
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SuperServerApp {
    public static void main(String[] args) {
        SpringApplication.run(SuperServerApp.class, args);
    }
}
