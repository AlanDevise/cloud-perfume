package com.alandevise.Mediamtx.config;

import com.alandevise.Mediamtx.handler.StreamStatsHandler;
import com.alandevise.Mediamtx.service.StreamService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * @Filename: WebSocketConfig.java
 * @Package: com.alandevise.Mediamtx.config
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2025年06月07日 15:07
 */

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private StreamService streamService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new StreamStatsHandler(), "/ws/stream-stats")
                .setAllowedOrigins("*");
    }
}

