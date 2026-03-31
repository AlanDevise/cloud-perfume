package com.alandevise.sse.model;

/**
 * 浏览器端指标快照。
 */
public class BrowserMetricsSnapshot {

    private Double cpuUsagePercent;
    private Long usedMemoryBytes;
    private Long totalMemoryBytes;
    private Integer logicalProcessors;

    public Double getCpuUsagePercent() {
        return cpuUsagePercent;
    }

    public void setCpuUsagePercent(Double cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    public Long getUsedMemoryBytes() {
        return usedMemoryBytes;
    }

    public void setUsedMemoryBytes(Long usedMemoryBytes) {
        this.usedMemoryBytes = usedMemoryBytes;
    }

    public Long getTotalMemoryBytes() {
        return totalMemoryBytes;
    }

    public void setTotalMemoryBytes(Long totalMemoryBytes) {
        this.totalMemoryBytes = totalMemoryBytes;
    }

    public Integer getLogicalProcessors() {
        return logicalProcessors;
    }

    public void setLogicalProcessors(Integer logicalProcessors) {
        this.logicalProcessors = logicalProcessors;
    }
}
