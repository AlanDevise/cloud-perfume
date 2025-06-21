package com.alandevise.Mediamtx.handler;

import com.alandevise.Mediamtx.service.StreamService;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Filename: StreamStatsHandler.java
 * @Package: com.alandevise.Mediamtx.handler
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2025年06月07日 15:09
 */

public class StreamStatsHandler extends TextWebSocketHandler {

    @Resource
    private StreamService streamService;

    // private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 定时发送流状态
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Map<String, Object> stats = streamService.getAllStreamStatus();
            try {
                session.sendMessage(new TextMessage(new JSONObject(stats).toString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 0, 2, TimeUnit.SECONDS);
    }
}