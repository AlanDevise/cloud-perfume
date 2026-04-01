package com.alandevise.sse.tools;

import com.alandevise.sse.model.ServerMetricsSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地压测工具，用于分别创建大量 SSE / WebSocket 连接，
 * 并定时读取服务端的内存与 CPU 指标。
 *
 * <p>运行示例：</p>
 * <pre>{@code
 * java -cp "target/classes:$(cat cp.txt)" com.alandevise.sse.tools.TelemetryLoadTester \
 *   --mode=sse --baseUrl=http://127.0.0.1:1125 --countOfConn=500 --countOfPoint=5 --durationSeconds=60
 *
 * java -cp "target/classes:$(cat cp.txt)" com.alandevise.sse.tools.TelemetryLoadTester \
 *   --mode=websocket --baseUrl=http://127.0.0.1:1125 --countOfConn=1000 --countOfPoint=5 --durationSeconds=60
 *
 * java -cp "target/classes:$(cat cp.txt)" com.alandevise.sse.tools.TelemetryLoadTester \
 *   --mode=both --scenario=protocol-only --baseUrl=http://127.0.0.1:1125 --countOfConn=500 --countOfPoint=5 --durationSeconds=45
 * }</pre>
 */
public class TelemetryLoadTester {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static void main(String[] args) throws Exception {
        LoadTestConfig config = LoadTestConfig.parse(args);
        TelemetryLoadTester tester = new TelemetryLoadTester();
        tester.run(config);
    }

    private void run(LoadTestConfig config) throws Exception {
        if (config.getMode() == LoadTestMode.BOTH) {
            executePhase(Protocol.SSE, config);
            coolDown(config);
            executePhase(Protocol.WEBSOCKET, config);
            return;
        }

        Protocol protocol = config.getMode() == LoadTestMode.SSE ? Protocol.SSE : Protocol.WEBSOCKET;
        executePhase(protocol, config);
    }

    private void coolDown(LoadTestConfig config) throws InterruptedException {
        if (config.getCoolDownSeconds() <= 0) {
            return;
        }
        System.out.printf("%nCooling down for %d seconds before the next phase...%n",
                config.getCoolDownSeconds());
        TimeUnit.SECONDS.sleep(config.getCoolDownSeconds());
    }

    private void executePhase(Protocol protocol, LoadTestConfig config) throws Exception {
        LoadStats stats = new LoadStats(protocol);
        ServerMetricsPoller poller = new ServerMetricsPoller(
                httpClient,
                objectMapper,
                config.getBaseUrl().resolve("/api/system/metrics"),
                stats);
        LoadPhase phase = protocol == Protocol.SSE
                ? new SseLoadPhase(httpClient, config, stats)
                : new WebSocketLoadPhase(httpClient, config, stats);

        System.out.printf("%n=== %s phase started at %s ===%n",
                protocol.getDisplayName(), Instant.now());
        System.out.printf("Target: %s, countOfConn=%d, countOfPoint=%d, duration=%ds, reportInterval=%ds%n",
                config.getBaseUrl(),
                config.getCountOfConn(),
                config.getCountOfPoint(),
                config.getDurationSeconds(),
                config.getReportIntervalSeconds());
        System.out.printf("Scenario: %s%n", config.getScenario());

        poller.start();
        phase.start();

        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(config.getDurationSeconds());
        try {
            while (System.nanoTime() < deadlineNanos) {
                TimeUnit.SECONDS.sleep(config.getReportIntervalSeconds());
                printStats(stats);
            }
        } finally {
            phase.stop();
            poller.stop();
            printStats(stats);
            System.out.printf("=== %s phase finished at %s ===%n",
                    protocol.getDisplayName(), Instant.now());
        }
    }

    private void printStats(LoadStats stats) {
        MetricsSnapshot metrics = stats.getMetricsSnapshot();
        String metricsText = metrics == null
                ? "serverHeap=N/A peakHeap=N/A serverCpu=N/A gcCount=N/A"
                : String.format(Locale.ROOT,
                "serverHeap=%s peakHeap=%s serverCpu=%.2f%% gcCount=%d",
                formatBytes(metrics.getUsedHeapBytes()),
                formatBytes(stats.getPeakHeapBytes()),
                metrics.getProcessCpuLoadPercent(),
                metrics.getTotalGarbageCollectionCount());

        System.out.printf(Locale.ROOT,
                "[%s] attempted=%d connected=%d active=%d messages=%d bytes=%d errors=%d %s%n",
                stats.getProtocol().getDisplayName(),
                stats.getAttemptedConnections(),
                stats.getConnectedConnections(),
                stats.getActiveConnections(),
                stats.getReceivedMessages(),
                stats.getReceivedBytes(),
                stats.getErrors(),
                metricsText);
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB"};
        double value = bytes;
        int unitIndex = 0;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        return String.format(Locale.ROOT, "%.2f %s", value, units[unitIndex]);
    }

    private enum Protocol {
        SSE("SSE"),
        WEBSOCKET("WebSocket");

        private final String displayName;

        Protocol(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private enum LoadTestMode {
        SSE,
        WEBSOCKET,
        BOTH
    }

    private interface LoadPhase {
        void start();

        void stop();
    }

    private static final class LoadTestConfig {
        private final LoadTestMode mode;
        private final URI baseUrl;
        private final int countOfConn;
        private final int countOfPoint;
        private final int durationSeconds;
        private final int reportIntervalSeconds;
        private final int coolDownSeconds;
        private final String scenario;

        private LoadTestConfig(LoadTestMode mode,
                               URI baseUrl,
                               int countOfConn,
                               int countOfPoint,
                               int durationSeconds,
                               int reportIntervalSeconds,
                               int coolDownSeconds,
                               String scenario) {
            this.mode = mode;
            this.baseUrl = baseUrl;
            this.countOfConn = countOfConn;
            this.countOfPoint = countOfPoint;
            this.durationSeconds = durationSeconds;
            this.reportIntervalSeconds = reportIntervalSeconds;
            this.coolDownSeconds = coolDownSeconds;
            this.scenario = scenario;
        }

        public static LoadTestConfig parse(String[] args) {
            Map<String, String> options = new ConcurrentHashMap<>();
            for (String arg : args) {
                if (!arg.startsWith("--") || !arg.contains("=")) {
                    throw new IllegalArgumentException("Unsupported argument: " + arg + System.lineSeparator() + usage());
                }
                String[] parts = arg.substring(2).split("=", 2);
                options.put(parts[0], parts[1]);
            }

            LoadTestMode mode = parseMode(options.getOrDefault("mode", "sse"));
            URI baseUrl = URI.create(options.getOrDefault("baseUrl", "http://127.0.0.1:1125"));
            int countOfConn = parsePositiveInt(options.getOrDefault("countOfConn", "100"), "countOfConn");
            int countOfPoint = parsePositiveInt(options.getOrDefault("countOfPoint", "5"), "countOfPoint");
            int durationSeconds = parsePositiveInt(options.getOrDefault("durationSeconds", "60"), "durationSeconds");
            int reportIntervalSeconds = parsePositiveInt(options.getOrDefault("reportIntervalSeconds", "5"), "reportIntervalSeconds");
            int coolDownSeconds = parseNonNegativeInt(options.getOrDefault("coolDownSeconds", "5"), "coolDownSeconds");
            String scenario = options.getOrDefault("scenario", "full");

            return new LoadTestConfig(mode, baseUrl, countOfConn, countOfPoint,
                    durationSeconds, reportIntervalSeconds, coolDownSeconds, scenario);
        }

        private static LoadTestMode parseMode(String rawMode) {
            switch (rawMode.toLowerCase(Locale.ROOT)) {
                case "sse":
                    return LoadTestMode.SSE;
                case "websocket":
                case "ws":
                    return LoadTestMode.WEBSOCKET;
                case "both":
                    return LoadTestMode.BOTH;
                default:
                    throw new IllegalArgumentException("Unsupported mode: " + rawMode + System.lineSeparator() + usage());
            }
        }

        private static int parsePositiveInt(String rawValue, String fieldName) {
            int value = Integer.parseInt(rawValue);
            if (value <= 0) {
                throw new IllegalArgumentException(fieldName + " must be > 0");
            }
            return value;
        }

        private static int parseNonNegativeInt(String rawValue, String fieldName) {
            int value = Integer.parseInt(rawValue);
            if (value < 0) {
                throw new IllegalArgumentException(fieldName + " must be >= 0");
            }
            return value;
        }

        private static String usage() {
            return "Usage: --mode=sse|websocket|both --baseUrl=http://127.0.0.1:1125 "
                    + "--countOfConn=500 --countOfPoint=5 --durationSeconds=60 "
                    + "[--reportIntervalSeconds=5] [--coolDownSeconds=5] [--scenario=full|protocol-only]";
        }

        public LoadTestMode getMode() {
            return mode;
        }

        public URI getBaseUrl() {
            return baseUrl;
        }

        public int getCountOfConn() {
            return countOfConn;
        }

        public int getCountOfPoint() {
            return countOfPoint;
        }

        public int getDurationSeconds() {
            return durationSeconds;
        }

        public int getReportIntervalSeconds() {
            return reportIntervalSeconds;
        }

        public int getCoolDownSeconds() {
            return coolDownSeconds;
        }

        public String getScenario() {
            return scenario;
        }
    }

    private static final class LoadStats {
        private final Protocol protocol;
        private final AtomicInteger attemptedConnections = new AtomicInteger();
        private final AtomicInteger connectedConnections = new AtomicInteger();
        private final AtomicInteger activeConnections = new AtomicInteger();
        private final AtomicLong receivedMessages = new AtomicLong();
        private final AtomicLong receivedBytes = new AtomicLong();
        private final AtomicLong errors = new AtomicLong();
        private final AtomicInteger loggedErrors = new AtomicInteger();
        private volatile MetricsSnapshot metricsSnapshot;
        private final AtomicLong peakHeapBytes = new AtomicLong();

        private LoadStats(Protocol protocol) {
            this.protocol = protocol;
        }

        public Protocol getProtocol() {
            return protocol;
        }

        public void onAttempt() {
            attemptedConnections.incrementAndGet();
        }

        public void onConnect() {
            connectedConnections.incrementAndGet();
            activeConnections.incrementAndGet();
        }

        public void onDisconnect() {
            activeConnections.updateAndGet(current -> Math.max(0, current - 1));
        }

        public void onMessage(long bytes) {
            receivedMessages.incrementAndGet();
            receivedBytes.addAndGet(Math.max(bytes, 0L));
        }

        public void onError() {
            errors.incrementAndGet();
        }

        public void logError(String category, Throwable throwable) {
            onError();
            if (loggedErrors.getAndIncrement() < 5) {
                System.err.printf("[%s][%s] %s%n",
                        protocol.getDisplayName(),
                        category,
                        throwable == null ? "unknown error" : throwable.toString());
            }
        }

        public void logError(String category, String message) {
            onError();
            if (loggedErrors.getAndIncrement() < 5) {
                System.err.printf("[%s][%s] %s%n", protocol.getDisplayName(), category, message);
            }
        }

        public void updateMetrics(ServerMetricsSnapshot snapshot) {
            metricsSnapshot = new MetricsSnapshot(snapshot);
            peakHeapBytes.accumulateAndGet(snapshot.getUsedHeapBytes(), Math::max);
        }

        public int getAttemptedConnections() {
            return attemptedConnections.get();
        }

        public int getConnectedConnections() {
            return connectedConnections.get();
        }

        public int getActiveConnections() {
            return activeConnections.get();
        }

        public long getReceivedMessages() {
            return receivedMessages.get();
        }

        public long getReceivedBytes() {
            return receivedBytes.get();
        }

        public long getErrors() {
            return errors.get();
        }

        public MetricsSnapshot getMetricsSnapshot() {
            return metricsSnapshot;
        }

        public long getPeakHeapBytes() {
            return peakHeapBytes.get();
        }
    }

    private static final class MetricsSnapshot {
        private final long usedHeapBytes;
        private final double processCpuLoadPercent;
        private final long totalGarbageCollectionCount;

        private MetricsSnapshot(ServerMetricsSnapshot snapshot) {
            this.usedHeapBytes = snapshot.getUsedHeapBytes();
            this.processCpuLoadPercent = snapshot.getProcessCpuLoadPercent();
            this.totalGarbageCollectionCount = snapshot.getTotalGarbageCollectionCount();
        }

        public long getUsedHeapBytes() {
            return usedHeapBytes;
        }

        public double getProcessCpuLoadPercent() {
            return processCpuLoadPercent;
        }

        public long getTotalGarbageCollectionCount() {
            return totalGarbageCollectionCount;
        }
    }

    private static final class ServerMetricsPoller {
        private final HttpClient httpClient;
        private final ObjectMapper objectMapper;
        private final URI metricsUri;
        private final LoadStats stats;
        private final ScheduledExecutorService executorService =
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("metrics-poller"));

        private ServerMetricsPoller(HttpClient httpClient,
                                    ObjectMapper objectMapper,
                                    URI metricsUri,
                                    LoadStats stats) {
            this.httpClient = httpClient;
            this.objectMapper = objectMapper;
            this.metricsUri = metricsUri;
            this.stats = stats;
        }

        public void start() {
            executorService.scheduleAtFixedRate(this::pollOnce, 0L, 1L, TimeUnit.SECONDS);
        }

        private void pollOnce() {
            HttpRequest request = HttpRequest.newBuilder(metricsUri)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    stats.logError("metrics", "Unexpected status: " + response.statusCode());
                    return;
                }
                ServerMetricsSnapshot snapshot =
                        objectMapper.readValue(response.body(), ServerMetricsSnapshot.class);
                stats.updateMetrics(snapshot);
            } catch (Exception ex) {
                stats.logError("metrics", ex);
            }
        }

        public void stop() {
            executorService.shutdownNow();
        }
    }

    private static final class SseLoadPhase implements LoadPhase {
        private final HttpClient httpClient;
        private final LoadTestConfig config;
        private final LoadStats stats;
        private final ExecutorService readerPool =
                Executors.newCachedThreadPool(new NamedThreadFactory("sse-reader"));
        private final List<SseConnection> connections = new ArrayList<>();
        private final AtomicBoolean stopped = new AtomicBoolean(false);

        private SseLoadPhase(HttpClient httpClient, LoadTestConfig config, LoadStats stats) {
            this.httpClient = httpClient;
            this.config = config;
            this.stats = stats;
        }

        @Override
        public void start() {
            String path = "protocol-only".equalsIgnoreCase(config.getScenario())
                    ? "/api/telemetry/protocol/sse?countOfPoint="
                    : "/api/telemetry/sse?countOfPoint=";
            URI uri = config.getBaseUrl().resolve(path + config.getCountOfPoint());
            for (int i = 0; i < config.getCountOfConn(); i++) {
                stats.onAttempt();
                SseConnection connection = new SseConnection();
                connections.add(connection);
                HttpRequest request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(15))
                        .header("Accept", "text/event-stream")
                        .GET()
                        .build();
                connection.future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
                connection.future.whenComplete((response, throwable) -> {
                    if (throwable != null || stopped.get()) {
                        connection.close(stats);
                        if (throwable != null) {
                            stats.logError("connect", throwable);
                        }
                        return;
                    }
                    if (response.statusCode() != 200) {
                        stats.logError("connect", "Unexpected status: " + response.statusCode());
                        connection.close(stats);
                        return;
                    }
                    connection.stream = response.body();
                    connection.connected = true;
                    stats.onConnect();
                    readerPool.submit(() -> consumeStream(connection));
                });
            }
        }

        private void consumeStream(SseConnection connection) {
            try (InputStream inputStream = connection.stream;
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                long payloadBytes = 0L;
                boolean hasEventData = false;
                while (!stopped.get() && (line = reader.readLine()) != null) {
                    payloadBytes += line.getBytes(StandardCharsets.UTF_8).length;
                    if (line.startsWith("data:")) {
                        hasEventData = true;
                    } else if (line.isEmpty() && hasEventData) {
                        stats.onMessage(payloadBytes);
                        payloadBytes = 0L;
                        hasEventData = false;
                    }
                }
            } catch (IOException ex) {
                if (!stopped.get()) {
                    stats.logError("stream", ex);
                }
            } finally {
                connection.close(stats);
            }
        }

        @Override
        public void stop() {
            if (!stopped.compareAndSet(false, true)) {
                return;
            }
            for (SseConnection connection : connections) {
                connection.cancelPending();
                connection.close(stats);
            }
            readerPool.shutdownNow();
        }
    }

    private static final class SseConnection {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private volatile CompletableFuture<HttpResponse<InputStream>> future;
        private volatile InputStream stream;
        private volatile boolean connected;

        private void cancelPending() {
            if (future != null && !future.isDone()) {
                future.cancel(true);
            }
        }

        private void close(LoadStats stats) {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                    // Ignore close failure.
                }
            }
            if (connected) {
                stats.onDisconnect();
            }
        }
    }

    private static final class WebSocketLoadPhase implements LoadPhase {
        private final HttpClient httpClient;
        private final LoadTestConfig config;
        private final LoadStats stats;
        private final List<WebSocketClient> clients = new ArrayList<>();
        private final AtomicBoolean stopped = new AtomicBoolean(false);

        private WebSocketLoadPhase(HttpClient httpClient, LoadTestConfig config, LoadStats stats) {
            this.httpClient = httpClient;
            this.config = config;
            this.stats = stats;
        }

        @Override
        public void start() {
            URI wsUri = toWebSocketUri(config.getBaseUrl()).resolve("/ws/telemetry");
            for (int i = 0; i < config.getCountOfConn(); i++) {
                stats.onAttempt();
                WebSocketClient listener = new WebSocketClient(
                        stats, config.getCountOfPoint(), config.getScenario(), stopped);
                clients.add(listener);
                httpClient.newWebSocketBuilder()
                        .connectTimeout(CONNECT_TIMEOUT)
                        .buildAsync(wsUri, listener)
                        .whenComplete((webSocket, throwable) -> {
                            if (throwable != null && !stopped.get()) {
                                stats.logError("connect", throwable);
                                listener.markDisconnected();
                            }
                        });
            }
        }

        @Override
        public void stop() {
            if (!stopped.compareAndSet(false, true)) {
                return;
            }
            for (WebSocketClient client : clients) {
                client.stop();
            }
        }

        private URI toWebSocketUri(URI baseUrl) {
            String scheme = Objects.equals(baseUrl.getScheme(), "https") ? "wss" : "ws";
            try {
                return new URI(
                        scheme,
                        baseUrl.getUserInfo(),
                        baseUrl.getHost(),
                        baseUrl.getPort(),
                        baseUrl.getPath(),
                        null,
                        null);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid baseUrl: " + baseUrl, ex);
            }
        }
    }

    private static final class WebSocketClient implements WebSocket.Listener {
        private final LoadStats stats;
        private final int countOfPoint;
        private final String scenario;
        private final AtomicBoolean globalStopped;
        private final AtomicBoolean disconnected = new AtomicBoolean(false);
        private final StringBuilder messageBuffer = new StringBuilder();
        private volatile WebSocket webSocket;

        private WebSocketClient(LoadStats stats, int countOfPoint, String scenario, AtomicBoolean globalStopped) {
            this.stats = stats;
            this.countOfPoint = countOfPoint;
            this.scenario = scenario;
            this.globalStopped = globalStopped;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            this.webSocket = webSocket;
            stats.onConnect();
            webSocket.request(1);
            webSocket.sendText(
                    "{\"action\":\"start\",\"countOfPoint\":" + countOfPoint + ",\"scenario\":\"" + scenario + "\"}",
                    true);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            messageBuffer.append(data);
            if (last) {
                stats.onMessage(messageBuffer.toString().getBytes(StandardCharsets.UTF_8).length);
                messageBuffer.setLength(0);
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            markDisconnected();
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            if (!globalStopped.get()) {
                stats.logError("stream", error);
            }
            markDisconnected();
        }

        private void stop() {
            if (webSocket == null) {
                return;
            }
            try {
                webSocket.sendText("{\"action\":\"stop\"}", true)
                        .orTimeout(2, TimeUnit.SECONDS)
                        .exceptionally(throwable -> null);
                webSocket.abort();
            } catch (Exception ignored) {
                // Ignore stop failure.
            } finally {
                markDisconnected();
            }
        }

        private void markDisconnected() {
            if (disconnected.compareAndSet(false, true)) {
                stats.onDisconnect();
            }
        }
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger counter = new AtomicInteger(1);

        private NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName(prefix + "-" + counter.getAndIncrement());
            return thread;
        }
    }
}
