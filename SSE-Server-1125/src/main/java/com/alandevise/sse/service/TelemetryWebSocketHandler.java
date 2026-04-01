package com.alandevise.sse.service;

import com.alandevise.sse.model.WebSocketCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

/**
 * 基于原生 WebSocket 的随机测点推送处理器。
 */
@Component
public class TelemetryWebSocketHandler extends TextWebSocketHandler {

    private final TelemetryStreamService telemetryStreamService;
    private final ObjectMapper objectMapper;

    public TelemetryWebSocketHandler(TelemetryStreamService telemetryStreamService, ObjectMapper objectMapper) {
        this.telemetryStreamService = telemetryStreamService;
        this.objectMapper = objectMapper;
    }

    /**
     * 建立连接后登记会话。
     *
     * @param session WebSocket 会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        telemetryStreamService.registerWebSocketClient(session.getId());
    }

    /**
     * 处理浏览器下发的开始 / 停止命令。
     *
     * @param session 会话
     * @param message 客户端消息
     * @throws IOException 消息发送失败
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        WebSocketCommand command = objectMapper.readValue(message.getPayload(), WebSocketCommand.class);
        if ("stop".equalsIgnoreCase(command.getAction())) {
            telemetryStreamService.stopWebSocketStream(session.getId());
            return;
        }
        int countOfPoint = command.getCountOfPoint() == null ? 5 : command.getCountOfPoint();
        telemetryStreamService.updateWebSocketPointCount(session.getId(), countOfPoint);
        telemetryStreamService.scheduleWebSocketStream(session.getId(), () -> sendPayload(session));
    }

    /**
     * 清理关闭后的会话。
     *
     * @param session WebSocket 会话
     * @param status  关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        telemetryStreamService.removeWebSocketClient(session.getId());
    }

    /**
     * 服务端发生异常时关闭会话。
     *
     * @param session   WebSocket 会话
     * @param exception 异常对象
     * @throws IOException 关闭失败
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws IOException {
        telemetryStreamService.removeWebSocketClient(session.getId());
        session.close(CloseStatus.SERVER_ERROR);
    }

    /**
     * 发送一次 WebSocket 数据帧。
     *
     * @param session WebSocket 会话
     */
    private void sendPayload(WebSocketSession session) {
        if (!session.isOpen() || !telemetryStreamService.isWebSocketStreaming(session.getId())) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(
                    telemetryStreamService.buildPayload("websocket",
                            telemetryStreamService.getWebSocketPointCount(session.getId())));
            session.sendMessage(new TextMessage(payload));
        } catch (IOException ex) {
            telemetryStreamService.removeWebSocketClient(session.getId());
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {
                // Ignore secondary close failure.
            }
            return;
        }

    }
}
