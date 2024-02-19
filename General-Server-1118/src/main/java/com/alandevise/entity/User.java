package com.alandevise.entity;

import com.alandevise.annotation.Encrypted;
import com.alandevise.annotation.SensitiveData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Filename: User.java
 * @Package: com.alandevise.entity
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-22 13:55
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SensitiveData
public class User {

    private Long id;

    private String name;

    @Encrypted
    private String email;

    private Integer balance;

    @Encrypted
    private String password;

    private Date createTime;

    private Date updateTime;
}

