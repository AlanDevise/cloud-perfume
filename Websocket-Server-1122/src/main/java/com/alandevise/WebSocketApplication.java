package com.alandevise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @Filename: WebSocketApplication.java
 * @Package: com.alandevise
 * @Version: V1.0.0
 * @Description: 1. WebSocketServer启动类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2024年03月18日 22:03
 */

@SpringBootApplication
public class WebSocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebSocketApplication.class, args);
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
