package com.alandevise.multitsdb.controller;

import com.alandevise.multitsdb.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("MultiTSDB 服务运行正常", "OK");
    }
}
