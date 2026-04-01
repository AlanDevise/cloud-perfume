# cloud-perfume

This project was Alan's practice project.

All the services are registered to Nacos.

Many of them were retrieved from the Internet.

## SSE-Server-1125 压测说明

`SSE-Server-1125` 是一个通过 SSE 和 WebSocket 推送随机测点数据的演示服务。
该模块中还提供了一个本地 Java 压测入口：

`com.alandevise.sse.tools.TelemetryLoadTester`

这个工具可以在本地批量建立 SSE 或 WebSocket 连接，并周期性访问服务端的
`/api/system/metrics`，观察 Java 进程的内存、CPU 和 GC 指标。

### 本次压测参数

- `countOfPoint=50`
- `countOfConn=1000`，持续 `30` 秒
- `countOfConn=5000`，持续约 `5` 分钟

### 压测结果摘要

在 `1000` 连接下，WebSocket 的峰值堆内存略高于 SSE。

- SSE 峰值堆内存：约 `316.72 MB`
- WebSocket 峰值堆内存：约 `340.42 MB`

在 `5000` 连接、约 `5` 分钟持续压测下，两者的差异已经很小。

- SSE 峰值堆内存：约 `1.49 GB`
- WebSocket 峰值堆内存：约 `1.48 GB`

从这次实验结果看，在当前项目和这组测试参数下，SSE 与 WebSocket 对 Java 进程资源的消耗差异不大。
两者的堆内存曲线都明显受到 GC 周期影响，会出现较大的波动，但在更高连接数下，最终峰值已经非常接近。
