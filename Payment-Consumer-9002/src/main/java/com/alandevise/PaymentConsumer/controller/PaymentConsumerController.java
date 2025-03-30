package com.alandevise.PaymentConsumer.controller;

import com.alandevise.PaymentConsumer.entity.TestEntity;
import com.alandevise.PaymentConsumer.feign.PaymentProviderClient;
import com.alandevise.api.pay.PaymentReqTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Filename: PaymentConsumerController.java
 * @Package: com.alandevise.PaymentConsumer.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年08月27日 19:28
 */

@RestController
@RequestMapping("/consumer")
@Slf4j
public class PaymentConsumerController {

    @Resource
    private PaymentProviderClient paymentProviderClient;

    @DubboReference
    private PaymentReqTest paymentProviderService;

    @GetMapping("/consumerTest")
    String test() {
        TestEntity testEntity = TestEntity.builder()
                .id("asdfasd")
                .name("Alan")
                .build();
        return paymentProviderClient.test(testEntity.getName(), testEntity.getId());
    }

    @GetMapping("/dubboTest")
    String dubboTest() {
        return paymentProviderService.func();
    }
}
