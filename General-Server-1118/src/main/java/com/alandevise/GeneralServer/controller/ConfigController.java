package com.alandevise.GeneralServer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Filename: ConfigController.java
 * @Package: com.alandevise.controller
 * @Version: V1.0.0
 * @Description: 1. Nacos配置中心动态获取值测试
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年08月05日 14:10
 */

@RestController
@RequestMapping("/config")
@RefreshScope   // 运行态期间能够动态刷新
public class ConfigController {

    // 从配置中心取值，默认值为false
    @Value("${useLocalCache:false}")
    private boolean useLocalCache;

    @RequestMapping("/get")
    public boolean get() {
        // 获取本地的私有变量，私有变量值从配置中心动态刷新获取
        return useLocalCache;
    }
}
