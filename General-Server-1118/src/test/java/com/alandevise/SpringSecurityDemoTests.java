package com.alandevise;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Filename: SpringSecurityDemoTests.java
 * @Package: com.alandevise
 * @Version: V1.0.0
 * @Description: 1. 加密明文密码测试类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月06日 14:50
 */

@SpringBootTest
public class SpringSecurityDemoTests {

    @Test
    public void contextLoad(){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123");
        System.out.println(encode);
        boolean matches = passwordEncoder.matches("123", encode);
        System.out.println(matches);
    }
}
