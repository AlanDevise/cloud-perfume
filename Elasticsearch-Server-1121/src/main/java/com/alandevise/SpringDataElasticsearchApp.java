package com.alandevise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Filename: SpringDataElasticsearchApp.java
 * @Package: com.alandevise
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年10月25日 23:33
 */

@SpringBootApplication
@EnableDiscoveryClient
public class SpringDataElasticsearchApp {
    public static void main(String[] args) {
        SpringApplication.run(SpringDataElasticsearchApp.class, args);
    }
}
