package com.alandevise.GeneralServer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Filename: fakeUser.java
 * @Package: com.alandevise.GeneralServer.entity
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2024年05月17日 23:43
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class fakeUser {
    private String userId;
    private String username;
    private String userAddress;
}
