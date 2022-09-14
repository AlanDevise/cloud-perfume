package com.alandevise.MQTT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Filename: mqttApp.java
 * @Package: com.alandevise.MQTT
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月14日 15:02
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class mqttApp {
    public static void main(String[] args) {
        SpringApplication.run(mqttApp.class, args);
    }
}
