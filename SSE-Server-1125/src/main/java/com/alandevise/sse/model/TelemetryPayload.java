package com.alandevise.sse.model;

import java.time.Instant;
import java.util.List;

/**
 * 一次推送的数据包。
 */
public class TelemetryPayload {

    private String channel;
    private int pointCount;
    private Instant timestamp;
    private List<TelemetryPoint> points;
    private ServerMetricsSnapshot serverMetrics;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<TelemetryPoint> getPoints() {
        return points;
    }

    public void setPoints(List<TelemetryPoint> points) {
        this.points = points;
    }

    public ServerMetricsSnapshot getServerMetrics() {
        return serverMetrics;
    }

    public void setServerMetrics(ServerMetricsSnapshot serverMetrics) {
        this.serverMetrics = serverMetrics;
    }
}
