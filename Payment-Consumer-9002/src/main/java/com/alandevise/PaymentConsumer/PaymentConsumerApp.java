package com.alandevise.PaymentConsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Filename: PaymentConsumerApp.java
 * @Package: com.alandevise.PaymentConsumer
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年08月27日 19:23
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PaymentConsumerApp {
    public static void main(String[] args) {
        SpringApplication.run(PaymentConsumerApp.class, args);
    }
}
