package com.alandevise.multidatasource.service;

import com.alandevise.multidatasource.po.User;

/**
 * @Filename: UserService.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */
public interface UserService {

    User getUserById(Long userId);


    User saveRandomUser();
}
