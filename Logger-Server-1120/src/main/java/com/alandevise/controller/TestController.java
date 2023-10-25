package com.alandevise.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Filename: TestController.java
 * @Package: com.alandevise.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年10月21日 22:10
 */

@RestController
@RequestMapping("/Logger")
public class TestController {
    private final Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/Test")
    public String test() {
        String msg = "fucking good";
        logger.info("slf4j print info msg:{}", msg);
        logger.debug("slf4j print debug msg:{}", msg);
        return msg;
    }
}
