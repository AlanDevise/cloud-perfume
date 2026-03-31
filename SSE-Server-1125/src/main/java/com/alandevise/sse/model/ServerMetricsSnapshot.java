package com.alandevise.sse.model;

/**
 * 服务端指标快照。
 */
public class ServerMetricsSnapshot {

    private double processCpuLoadPercent;
    private double systemCpuLoadPercent;
    private long usedHeapBytes;
    private long maxHeapBytes;
    private int processors;
    private long totalGarbageCollectionCount;
    private long totalGarbageCollectionTimeMillis;
    private long youngGenerationGarbageCollectionCount;
    private long youngGenerationGarbageCollectionTimeMillis;
    private long oldGenerationGarbageCollectionCount;
    private long oldGenerationGarbageCollectionTimeMillis;

    public double getProcessCpuLoadPercent() {
        return processCpuLoadPercent;
    }

    public void setProcessCpuLoadPercent(double processCpuLoadPercent) {
        this.processCpuLoadPercent = processCpuLoadPercent;
    }

    public double getSystemCpuLoadPercent() {
        return systemCpuLoadPercent;
    }

    public void setSystemCpuLoadPercent(double systemCpuLoadPercent) {
        this.systemCpuLoadPercent = systemCpuLoadPercent;
    }

    public long getUsedHeapBytes() {
        return usedHeapBytes;
    }

    public void setUsedHeapBytes(long usedHeapBytes) {
        this.usedHeapBytes = usedHeapBytes;
    }

    public long getMaxHeapBytes() {
        return maxHeapBytes;
    }

    public void setMaxHeapBytes(long maxHeapBytes) {
        this.maxHeapBytes = maxHeapBytes;
    }

    public int getProcessors() {
        return processors;
    }

    public void setProcessors(int processors) {
        this.processors = processors;
    }

    public long getTotalGarbageCollectionCount() {
        return totalGarbageCollectionCount;
    }

    public void setTotalGarbageCollectionCount(long totalGarbageCollectionCount) {
        this.totalGarbageCollectionCount = totalGarbageCollectionCount;
    }

    public long getTotalGarbageCollectionTimeMillis() {
        return totalGarbageCollectionTimeMillis;
    }

    public void setTotalGarbageCollectionTimeMillis(long totalGarbageCollectionTimeMillis) {
        this.totalGarbageCollectionTimeMillis = totalGarbageCollectionTimeMillis;
    }

    public long getYoungGenerationGarbageCollectionCount() {
        return youngGenerationGarbageCollectionCount;
    }

    public void setYoungGenerationGarbageCollectionCount(long youngGenerationGarbageCollectionCount) {
        this.youngGenerationGarbageCollectionCount = youngGenerationGarbageCollectionCount;
    }

    public long getYoungGenerationGarbageCollectionTimeMillis() {
        return youngGenerationGarbageCollectionTimeMillis;
    }

    public void setYoungGenerationGarbageCollectionTimeMillis(long youngGenerationGarbageCollectionTimeMillis) {
        this.youngGenerationGarbageCollectionTimeMillis = youngGenerationGarbageCollectionTimeMillis;
    }

    public long getOldGenerationGarbageCollectionCount() {
        return oldGenerationGarbageCollectionCount;
    }

    public void setOldGenerationGarbageCollectionCount(long oldGenerationGarbageCollectionCount) {
        this.oldGenerationGarbageCollectionCount = oldGenerationGarbageCollectionCount;
    }

    public long getOldGenerationGarbageCollectionTimeMillis() {
        return oldGenerationGarbageCollectionTimeMillis;
    }

    public void setOldGenerationGarbageCollectionTimeMillis(long oldGenerationGarbageCollectionTimeMillis) {
        this.oldGenerationGarbageCollectionTimeMillis = oldGenerationGarbageCollectionTimeMillis;
    }
}
