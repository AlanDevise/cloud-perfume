package com.alandevise.sse.controller;

import com.alandevise.sse.model.ServerMetricsSnapshot;
import com.alandevise.sse.service.SystemMetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暴露服务端运行指标。
 */
@RestController
@RequestMapping("/api/system")
public class SystemMetricsController {

    private final SystemMetricsService systemMetricsService;

    public SystemMetricsController(SystemMetricsService systemMetricsService) {
        this.systemMetricsService = systemMetricsService;
    }

    /**
     * 获取当前服务端 CPU 和内存快照。
     *
     * @return 服务端指标
     */
    @GetMapping("/metrics")
    public ServerMetricsSnapshot metrics() {
        return systemMetricsService.collectSnapshot();
    }
}
