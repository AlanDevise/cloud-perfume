package com.alandevise.sse.service;

import com.alandevise.sse.model.TelemetryPayload;
import com.alandevise.sse.model.TelemetryPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    private final Map<String, SseClientContext> sseClients = new ConcurrentHashMap<>();
    private final Map<String, WebSocketClientContext> webSocketClients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final SystemMetricsService systemMetricsService;

    public TelemetryStreamService(SystemMetricsService systemMetricsService) {
        this.systemMetricsService = systemMetricsService;
    }

    /**
     * 创建 SSE 发射器，并立即开始推送随机测点数据。
     *
     * @param pointCount 测点数量
     * @return SSE 发射器
     */
    public SseEmitter createSseEmitter(int pointCount) {
        int sanitizedPointCount = sanitizePointCount(pointCount);
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        SseClientContext context = new SseClientContext(emitter, sanitizedPointCount);
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
        WebSocketClientContext context = webSocketClients.computeIfAbsent(sessionId, key -> new WebSocketClientContext());
        context.setPointCount(sanitizePointCount(pointCount));
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
            context.getEmitter().send(SseEmitter.event()
                    .name("telemetry")
                    .data(buildPayload("sse", context.getPointCount())));
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
     * SSE 推送上下文。
     */
    private static final class SseClientContext {
        private final SseEmitter emitter;
        private final int pointCount;
        private final AtomicBoolean active = new AtomicBoolean(true);
        private volatile ScheduledFuture<?> future;

        private SseClientContext(SseEmitter emitter, int pointCount) {
            this.emitter = emitter;
            this.pointCount = pointCount;
        }

        public SseEmitter getEmitter() {
            return emitter;
        }

        public int getPointCount() {
            return pointCount;
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
        private volatile boolean streaming;
        private volatile ScheduledFuture<?> future;

        public int getPointCount() {
            return pointCount;
        }

        public void setPointCount(int pointCount) {
            this.pointCount = pointCount;
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
