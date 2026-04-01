package com.alandevise.sse.service;

import com.alandevise.sse.model.TelemetryPayload;
import com.alandevise.sse.model.TelemetryPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 统一管理 SSE / WebSocket 两类随机测点推送。
 */
@Service
public class TelemetryStreamService {

    private static final long SSE_TIMEOUT_MS = 0L;
    private static final String FULL_STREAM_MODE = "full";
    private static final String PROTOCOL_ONLY_STREAM_MODE = "protocol-only";

    private final Map<String, SseClientContext> sseClients = new ConcurrentHashMap<>();
    private final Map<String, WebSocketClientContext> webSocketClients = new ConcurrentHashMap<>();
    private final Map<Integer, String> protocolOnlyPayloadCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final SystemMetricsService systemMetricsService;
    private final ObjectMapper objectMapper;

    public TelemetryStreamService(SystemMetricsService systemMetricsService, ObjectMapper objectMapper) {
        this.systemMetricsService = systemMetricsService;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建 SSE 发射器，并立即开始推送随机测点数据。
     *
     * @param pointCount 测点数量
     * @return SSE 发射器
     */
    public SseEmitter createSseEmitter(int pointCount) {
        return createSseEmitter(pointCount, FULL_STREAM_MODE);
    }

    /**
     * 创建 SSE 发射器，并按照指定模式开始推送。
     *
     * @param pointCount 测点数量
     * @param streamMode 推送模式
     * @return SSE 发射器
     */
    public SseEmitter createSseEmitter(int pointCount, String streamMode) {
        int sanitizedPointCount = sanitizePointCount(pointCount);
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        SseClientContext context = new SseClientContext(emitter, sanitizedPointCount, normalizeStreamMode(streamMode));
        sseClients.put(clientId, context);

        emitter.onCompletion(() -> stopSseClient(clientId));
        emitter.onTimeout(() -> stopSseClient(clientId));
        emitter.onError(ex -> stopSseClient(clientId));

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> sendSseFrame(clientId, context), 0L, 1L, TimeUnit.SECONDS);
        context.setFuture(future);
        return emitter;
    }

    /**
     * 注册新的 WebSocket 会话。
     *
     * @param sessionId 会话标识
     */
    public void registerWebSocketClient(String sessionId) {
        webSocketClients.put(sessionId, new WebSocketClientContext());
    }

    /**
     * 更新 WebSocket 连接的测点数量。
     *
     * @param sessionId  会话标识
     * @param pointCount 测点数量
     */
    public void updateWebSocketPointCount(String sessionId, int pointCount) {
        updateWebSocketPointCount(sessionId, pointCount, FULL_STREAM_MODE);
    }

    /**
     * 更新 WebSocket 连接的推送参数。
     *
     * @param sessionId   会话标识
     * @param pointCount  测点数量
     * @param streamMode  推送模式
     */
    public void updateWebSocketPointCount(String sessionId, int pointCount, String streamMode) {
        WebSocketClientContext context = webSocketClients.computeIfAbsent(sessionId, key -> new WebSocketClientContext());
        context.setPointCount(sanitizePointCount(pointCount));
        context.setStreamMode(normalizeStreamMode(streamMode));
        context.setStreaming(true);
    }

    /**
     * 停止指定 WebSocket 会话的数据推送。
     *
     * @param sessionId 会话标识
     */
    public void stopWebSocketStream(String sessionId) {
        WebSocketClientContext context = webSocketClients.get(sessionId);
        if (context != null) {
            context.setStreaming(false);
            cancelFuture(context);
        }
    }

    /**
     * 注销 WebSocket 会话。
     *
     * @param sessionId 会话标识
     */
    public void removeWebSocketClient(String sessionId) {
        WebSocketClientContext context = webSocketClients.remove(sessionId);
        if (context != null) {
            cancelFuture(context);
        }
    }

    /**
     * 判断当前 WebSocket 会话是否处于推送状态。
     *
     * @param sessionId 会话标识
     * @return 是否推送中
     */
    public boolean isWebSocketStreaming(String sessionId) {
        WebSocketClientContext context = webSocketClients.get(sessionId);
        return context != null && context.isStreaming();
    }

    /**
     * 获取当前 WebSocket 会话的测点数量。
     *
     * @param sessionId 会话标识
     * @return 测点数量
     */
    public int getWebSocketPointCount(String sessionId) {
        WebSocketClientContext context = webSocketClients.get(sessionId);
        return context == null ? 5 : context.getPointCount();
    }

    /**
     * 获取指定 WebSocket 会话的推送模式。
     *
     * @param sessionId 会话标识
     * @return 推送模式
     */
    public String getWebSocketStreamMode(String sessionId) {
        WebSocketClientContext context = webSocketClients.get(sessionId);
        return context == null ? FULL_STREAM_MODE : context.getStreamMode();
    }

    /**
     * 生成一次数据包。
     *
     * @param channel    通道名称
     * @param pointCount 测点数量
     * @return 随机数据包
     */
    public TelemetryPayload buildPayload(String channel, int pointCount) {
        int sanitizedPointCount = sanitizePointCount(pointCount);
        List<TelemetryPoint> points = new ArrayList<>(sanitizedPointCount);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 1; index <= sanitizedPointCount; index++) {
            points.add(new TelemetryPoint("P-" + index, random.nextInt(0, 1001)));
        }

        TelemetryPayload payload = new TelemetryPayload();
        payload.setChannel(channel);
        payload.setPointCount(sanitizedPointCount);
        payload.setTimestamp(Instant.now());
        payload.setPoints(points);
        payload.setServerMetrics(systemMetricsService.collectSnapshot());
        return payload;
    }

    /**
     * 构造协议层对比实验使用的固定文本 payload。
     *
     * @param pointCount 测点数量
     * @return 固定文本 payload
     */
    public String buildProtocolOnlyPayload(int pointCount) {
        int sanitizedPointCount = sanitizePointCount(pointCount);
        return protocolOnlyPayloadCache.computeIfAbsent(sanitizedPointCount, this::createProtocolOnlyPayload);
    }

    /**
     * 发送一帧 SSE 数据并安排下一次推送。
     *
     * @param clientId 客户端标识
     * @param context  SSE 上下文
     */
    private void sendSseFrame(String clientId, SseClientContext context) {
        if (!context.isActive()) {
            return;
        }
        try {
            Object payload = PROTOCOL_ONLY_STREAM_MODE.equals(context.getStreamMode())
                    ? buildProtocolOnlyPayload(context.getPointCount())
                    : buildPayload("sse", context.getPointCount());
            context.getEmitter().send(SseEmitter.event()
                    .name("telemetry")
                    .data(payload));
        } catch (IOException ex) {
            stopSseClient(clientId);
        }
    }

    /**
     * 关闭并清理 SSE 客户端。
     *
     * @param clientId 客户端标识
     */
    private void stopSseClient(String clientId) {
        SseClientContext context = sseClients.remove(clientId);
        if (context != null) {
            context.setActive(false);
            context.cancelFuture();
        }
    }

    /**
     * 清洗测点数量，确保至少为 1。
     *
     * @param pointCount 输入值
     * @return 安全值
     */
    private int sanitizePointCount(int pointCount) {
        return Math.max(1, pointCount);
    }

    /**
     * 规范化推送模式。
     *
     * @param streamMode 原始模式
     * @return 规范后的模式
     */
    private String normalizeStreamMode(String streamMode) {
        return PROTOCOL_ONLY_STREAM_MODE.equalsIgnoreCase(streamMode)
                ? PROTOCOL_ONLY_STREAM_MODE
                : FULL_STREAM_MODE;
    }

    /**
     * 生成固定 payload 对应的 JSON 字符串，仅在首次命中时创建一次。
     *
     * @param pointCount 测点数量
     * @return JSON 文本
     */
    private String createProtocolOnlyPayload(int pointCount) {
        List<TelemetryPoint> points = new ArrayList<>(pointCount);
        for (int index = 1; index <= pointCount; index++) {
            points.add(new TelemetryPoint("P-" + index, index));
        }

        TelemetryPayload payload = new TelemetryPayload();
        payload.setPointCount(pointCount);
        payload.setPoints(points);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to build protocol-only payload", ex);
        }
    }

    /**
     * SSE 推送上下文。
     */
    private static final class SseClientContext {
        private final SseEmitter emitter;
        private final int pointCount;
        private final String streamMode;
        private final AtomicBoolean active = new AtomicBoolean(true);
        private volatile ScheduledFuture<?> future;

        private SseClientContext(SseEmitter emitter, int pointCount, String streamMode) {
            this.emitter = emitter;
            this.pointCount = pointCount;
            this.streamMode = streamMode;
        }

        public SseEmitter getEmitter() {
            return emitter;
        }

        public int getPointCount() {
            return pointCount;
        }

        public String getStreamMode() {
            return streamMode;
        }

        public boolean isActive() {
            return active.get();
        }

        public void setActive(boolean status) {
            active.set(status);
        }

        public void setFuture(ScheduledFuture<?> future) {
            this.future = future;
        }

        public void cancelFuture() {
            if (future != null) {
                future.cancel(true);
            }
        }
    }

    /**
     * WebSocket 推送上下文。
     */
    private static final class WebSocketClientContext {
        private volatile int pointCount = 5;
        private volatile String streamMode = FULL_STREAM_MODE;
        private volatile boolean streaming;
        private volatile ScheduledFuture<?> future;

        public int getPointCount() {
            return pointCount;
        }

        public void setPointCount(int pointCount) {
            this.pointCount = pointCount;
        }

        public String getStreamMode() {
            return streamMode;
        }

        public void setStreamMode(String streamMode) {
            this.streamMode = streamMode;
        }

        public boolean isStreaming() {
            return streaming;
        }

        public void setStreaming(boolean streaming) {
            this.streaming = streaming;
        }

        public ScheduledFuture<?> getFuture() {
            return future;
        }

        public void setFuture(ScheduledFuture<?> future) {
            this.future = future;
        }
    }

    /**
     * 为 WebSocket 会话启动固定频率推送任务。
     *
     * @param sessionId 会话标识
     * @param task      推送任务
     */
    public void scheduleWebSocketStream(String sessionId, Runnable task) {
        WebSocketClientContext context = webSocketClients.computeIfAbsent(sessionId, key -> new WebSocketClientContext());
        cancelFuture(context);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, 0L, 1L, TimeUnit.SECONDS);
        context.setFuture(future);
    }

    /**
     * 取消已有定时任务。
     *
     * @param context 上下文
     */
    private void cancelFuture(WebSocketClientContext context) {
        ScheduledFuture<?> existingFuture = context.getFuture();
        if (existingFuture != null && !existingFuture.isCancelled()) {
            existingFuture.cancel(true);
        }
    }

    /**
     * 应用退出前关闭调度线程池。
     */
    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdownNow();
    }
}
