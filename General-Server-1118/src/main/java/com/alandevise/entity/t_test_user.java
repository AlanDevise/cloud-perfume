package com.alandevise.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Filename: t_test_user.java
 * @Package: com.alandevise.entity
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年08月24日 23:34
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class t_test_user {
    private int id;
    private String username;
    private int age;
}
