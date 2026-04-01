package com.alandevise.sse.controller;

import com.alandevise.sse.service.TelemetryStreamService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 提供 SSE 数据推送入口。
 */
@RestController
@RequestMapping("/api/telemetry")
public class TelemetrySseController {

    private final TelemetryStreamService telemetryStreamService;

    public TelemetrySseController(TelemetryStreamService telemetryStreamService) {
        this.telemetryStreamService = telemetryStreamService;
    }

    /**
     * 为当前浏览器创建 SSE 长连接。
     *
     * @param countOfPoint 每条连接单次推送的测点数量
     * @return SSE 发射器
     */
    @GetMapping(path = "/sse", produces = "text/event-stream")
    public SseEmitter subscribe(@RequestParam(defaultValue = "5") int countOfPoint) {
        return telemetryStreamService.createSseEmitter(countOfPoint);
    }

    /**
     * 为协议对比实验创建 SSE 长连接。
     *
     * @param countOfPoint 每条连接单次推送的测点数量
     * @return SSE 发射器
     */
    @GetMapping(path = "/protocol/sse", produces = "text/event-stream")
    public SseEmitter protocolOnlySubscribe(@RequestParam(defaultValue = "5") int countOfPoint) {
        return telemetryStreamService.createSseEmitter(countOfPoint, "protocol-only");
    }
}
