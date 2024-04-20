package com.alandevise.PaymentConsumer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    // 可用版本1
    // @GetMapping("/test")
    // String test(@SpringQueryMap TestEntity testEntity);

    // 可用版本2
    @GetMapping("/test")
    String test(@RequestParam("name") String name,
                @RequestParam("id") String id);
}
