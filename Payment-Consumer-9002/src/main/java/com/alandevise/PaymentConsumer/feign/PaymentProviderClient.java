package com.alandevise.PaymentConsumer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Filename: PaymentProviderClient.java
 * @Package: com.alandevise.PaymentConsumer.feign
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年08月27日 19:26
 */

@FeignClient(value = "Payment-Provider")
public interface PaymentProviderClient {
    @GetMapping("/test")
    String test();
}
