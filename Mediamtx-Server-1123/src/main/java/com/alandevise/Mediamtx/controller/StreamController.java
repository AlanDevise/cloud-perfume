package com.alandevise.Mediamtx.controller;

import com.alandevise.Mediamtx.service.StreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Filename: StreamController.java
 * @Package: com.alandevise.Mediamtx.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2025年06月07日 15:05
 */

@RestController
// @RequestMapping("/api/stream")
@Slf4j
public class StreamController {
    private final StreamService streamService;

    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }

    // @PostMapping("/create")
    // public ResponseEntity<Map<String, String>> createStream(
    //         @RequestParam String rtspUrl) {
    //
    //     String playUrl = streamService.createStreamProxy(rtspUrl);
    //     return ResponseEntity.ok(Map.of(
    //             "streamId", playUrl.substring(playUrl.lastIndexOf('/') + 1),
    //             "hlsUrl", playUrl + "/index.m3u8",
    //             "flvUrl", playUrl,
    //             "webrtcUrl", "ws://localhost:8889/" + playUrl.substring(playUrl.lastIndexOf('/') + 1)
    //     ));
    // }

    @PostMapping("/api/stream/create")
    public ResponseEntity<Map<String, String>> createStream(
            @RequestParam String rtspUrl) {

        log.info("Received RTSP URL: {}", rtspUrl);

        // 返回结构示例
        Map<String, String> response = new HashMap<>();
        response.put("flvUrl", "http://localhost:8888/proxied1");
        response.put("hlsUrl", "http://localhost:8888/proxied1/index.m3u8");
        response.put("webrtcUrl", "ws://localhost:8889/proxied1");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{streamId}")
    public ResponseEntity<Map<String, Object>> getStatus(
            @PathVariable String streamId) {
        return ResponseEntity.ok(streamService.getStreamStatus(streamId));
    }
}