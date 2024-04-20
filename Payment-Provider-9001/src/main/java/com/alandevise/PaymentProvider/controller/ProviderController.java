package com.alandevise.PaymentProvider.controller;

import com.alandevise.PaymentProvider.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Filename: ProviderController.java
 * @Package: com.alandevise.PaymentProvider.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年08月27日 18:46
 */

@RestController
@Slf4j
public class ProviderController {

    @GetMapping("/test")
    String test(HttpServletRequest request,
                TestEntity testEntity) {
        log.info("[INFO] Received the request from client.");
        return "This message is from provider 9001!" +
                "id is" + testEntity.getId();
    }
}
