package com.alandevise.Mediamtx.service;

import com.alandevise.Mediamtx.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Filename: StreamService.java
 * @Package: com.alandevise.Mediamtx.service
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2025年06月07日 15:04
 */

@Service
@Slf4j
public class StreamService {
    private static final String MEDIAMTX_API = "http://localhost:9997/v2/";
    private final RestTemplate restTemplate;

    public StreamService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * 获取所有流的状态信息
     */
    public Map<String, Object> getAllStreamStatus() {
        String apiUrl = MEDIAMTX_API + "stats";
        try {
            return restTemplate.getForObject(apiUrl, Map.class);
        } catch (RestClientException e) {
            throw new RuntimeException("获取流状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个流的状态
     */
    public Map<String, Object> getStreamStatus(String streamId) {
        String apiUrl = MEDIAMTX_API + "paths/get/" + streamId;
        try {
            return restTemplate.getForObject(apiUrl, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("流不存在: " + streamId);
        } catch (RestClientException e) {
            throw new RuntimeException("获取流状态失败: " + e.getMessage());
        }
    }

    /**
     * 创建流代理
     */
    public String createStreamProxy(String rtspUrl) {
        String streamId = "stream_" + UUID.randomUUID().toString();
        String apiUrl = MEDIAMTX_API + "paths/add/" + streamId;

        Map<String, Object> config = new HashMap<>();
        config.put("source", rtspUrl);
        config.put("sourceProtocol", "tcp");
        config.put("sourceOnDemand", true);
        config.put("runOnInit", "restart");
        config.put("runOnInitRestart", "5s");

        try {
            restTemplate.put(apiUrl, config);
            return "http://localhost:8888/" + streamId;
        } catch (RestClientException e) {
            throw new RuntimeException("创建流失败: " + e.getMessage());
        }
    }

    /**
     * 删除流
     */
    public void deleteStream(String streamId) {
        String apiUrl = MEDIAMTX_API + "paths/delete/" + streamId;
        try {
            restTemplate.delete(apiUrl);
        } catch (RestClientException e) {
            throw new RuntimeException("删除流失败: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 300000) // 每5分钟检查一次
    public void cleanupIdleStreams() {
        Map<String, Object> allStreams = restTemplate.getForObject(
                MEDIAMTX_API, Map.class);

        for (Map.Entry<String, Object> entry : allStreams.entrySet()) {
            Map<String, Object> streamInfo = (Map<String, Object>) entry.getValue();
            Map<String, Object> readers = (Map<String, Object>) streamInfo.get("readers");

            if (readers.isEmpty()) {
                // 无观看者，删除流
                restTemplate.delete(MEDIAMTX_API + entry.getKey());
                log.info("清理空闲流: {}", entry.getKey());
            }
        }
    }
}