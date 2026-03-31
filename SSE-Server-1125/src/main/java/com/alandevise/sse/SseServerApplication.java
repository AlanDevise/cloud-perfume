package com.alandevise.sse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SSE / WebSocket 示例服务启动类。
 */
@SpringBootApplication
public class SseServerApplication {

    /**
     * 启动 1125 示例服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SseServerApplication.class, args);
    }
}
