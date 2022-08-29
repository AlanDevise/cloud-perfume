package com.alandevise.PaymentConsumer.controller;

import com.alandevise.PaymentConsumer.feign.PaymentProviderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Filename: PaymentConsumerController.java
 * @Package: com.alandevise.PaymentConsumer.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年08月27日 19:28
 */

@RestController
@Slf4j
public class PaymentConsumerController {

    private PaymentProviderClient paymentProviderClient;

    @Autowired
    private void setDependencies(PaymentProviderClient paymentProviderClient) {
        this.paymentProviderClient = paymentProviderClient;
    }

    @GetMapping("/consumerTest")
    String test() {
        return paymentProviderClient.test();
    }
}
