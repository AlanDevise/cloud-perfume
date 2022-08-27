package com.alandevise.PaymentProvider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Filename: PaymentProviderApp.java
 * @Package: com.alandevise.PaymentProvider
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年08月27日 18:45
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PaymentProviderApp {
    public static void main(String[] args) {
        SpringApplication.run(PaymentProviderApp.class, args);
    }
}
