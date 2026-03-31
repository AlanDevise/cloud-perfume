package com.alandevise.sse.service;

import com.alandevise.sse.model.ServerMetricsSnapshot;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Service;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Locale;

/**
 * 采集 JVM 与宿主机的基础运行指标。
 */
@Service
public class SystemMetricsService {

    /**
     * 获取当前进程的 CPU / 内存快照。
     *
     * @return 服务端指标对象
     */
    public ServerMetricsSnapshot collectSnapshot() {
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean operatingSystemMXBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        ServerMetricsSnapshot snapshot = new ServerMetricsSnapshot();
        snapshot.setProcessCpuLoadPercent(toPercent(operatingSystemMXBean.getProcessCpuLoad()));
        snapshot.setSystemCpuLoadPercent(toPercent(operatingSystemMXBean.getSystemCpuLoad()));
        snapshot.setUsedHeapBytes(runtime.totalMemory() - runtime.freeMemory());
        snapshot.setMaxHeapBytes(runtime.maxMemory());
        snapshot.setProcessors(runtime.availableProcessors());
        fillGarbageCollectionMetrics(snapshot, ManagementFactory.getGarbageCollectorMXBeans());
        return snapshot;
    }

    /**
     * 汇总当前 JVM 的垃圾回收指标。
     *
     * @param snapshot                服务端指标对象
     * @param garbageCollectorMXBeans 垃圾回收器集合
     */
    private void fillGarbageCollectionMetrics(ServerMetricsSnapshot snapshot,
                                              List<GarbageCollectorMXBean> garbageCollectorMXBeans) {
        long totalCount = 0L;
        long totalTime = 0L;
        long youngCount = 0L;
        long youngTime = 0L;
        long oldCount = 0L;
        long oldTime = 0L;

        for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            long collectionCount = normalizeNonNegative(garbageCollectorMXBean.getCollectionCount());
            long collectionTime = normalizeNonNegative(garbageCollectorMXBean.getCollectionTime());
            totalCount += collectionCount;
            totalTime += collectionTime;

            if (isYoungGenerationCollector(garbageCollectorMXBean.getName())) {
                youngCount += collectionCount;
                youngTime += collectionTime;
            } else if (isOldGenerationCollector(garbageCollectorMXBean.getName())) {
                oldCount += collectionCount;
                oldTime += collectionTime;
            }
        }

        snapshot.setTotalGarbageCollectionCount(totalCount);
        snapshot.setTotalGarbageCollectionTimeMillis(totalTime);
        snapshot.setYoungGenerationGarbageCollectionCount(youngCount);
        snapshot.setYoungGenerationGarbageCollectionTimeMillis(youngTime);
        snapshot.setOldGenerationGarbageCollectionCount(oldCount);
        snapshot.setOldGenerationGarbageCollectionTimeMillis(oldTime);
    }

    /**
     * 将 0-1 区间的小数转换为百分比。
     *
     * @param ratio 原始比例
     * @return 百分比
     */
    private double toPercent(double ratio) {
        if (ratio < 0) {
            return 0D;
        }
        return Math.round(ratio * 10000D) / 100D;
    }

    /**
     * 判断是否为年轻代垃圾回收器。
     *
     * @param collectorName 回收器名称
     * @return 是否年轻代
     */
    private boolean isYoungGenerationCollector(String collectorName) {
        String name = collectorName.toLowerCase(Locale.ROOT);
        return name.contains("young")
                || name.contains("scavenge")
                || name.contains("new generation")
                || name.contains("copy")
                || name.contains("nursery");
    }

    /**
     * 判断是否为老年代垃圾回收器。
     *
     * @param collectorName 回收器名称
     * @return 是否老年代
     */
    private boolean isOldGenerationCollector(String collectorName) {
        String name = collectorName.toLowerCase(Locale.ROOT);
        return name.contains("old")
                || name.contains("mark")
                || name.contains("mixed")
                || name.contains("compact")
                || name.contains("tenured");
    }

    /**
     * 将负值指标归零。
     *
     * @param value 原始值
     * @return 非负值
     */
    private long normalizeNonNegative(long value) {
        return Math.max(0L, value);
    }
}
