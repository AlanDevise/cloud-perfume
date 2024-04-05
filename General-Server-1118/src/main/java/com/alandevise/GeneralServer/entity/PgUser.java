package com.alandevise.GeneralServer.entity;

import lombok.Data;

/**
 * @Filename: PgUser.java
 * @Package: com.alandevise.entity
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2024年03月23日 22:50
 */

@Data
public class PgUser {
    String userId;
    String username;
    String userAddress;
}
