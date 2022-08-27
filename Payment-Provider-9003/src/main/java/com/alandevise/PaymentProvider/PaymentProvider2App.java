package com.alandevise.PaymentProvider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Filename: PaymentProvider2App.java
 * @Package: com.alandevise.PaymentProvider.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年08月27日 22:39
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PaymentProvider2App {
    public static void main(String[] args) {
        SpringApplication.run(PaymentProvider2App.class, args);
    }
}
