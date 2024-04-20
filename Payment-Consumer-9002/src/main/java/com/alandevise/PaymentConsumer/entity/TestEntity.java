package com.alandevise.PaymentConsumer.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Filename: TestEntity.java
 * @Package: com.alandevise.PaymentConsumer.entity
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2024年04月20日 14:55
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TestEntity {
    private String id;
    private String name;
}
