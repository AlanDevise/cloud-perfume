package com.alandevise.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Filename: tUser.java
 * @Package: com.alandevise.entity
 * @Version: V1.0.0
 * @Description: 1. 对应PostgresSQL表中的实体类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年07月15日 22:11
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class tUser {
    private String userId;
    private String username;
    private String userAddress;
}
