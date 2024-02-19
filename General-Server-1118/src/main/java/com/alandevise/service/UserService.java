package com.alandevise.service;

import com.alandevise.entity.User;

import java.util.List;

/**
 * @Filename: UserService.java
 * @Package: com.alandevise.service
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:42
 */

public interface UserService {
    Boolean create(User user);
    Boolean batchCreate(User user);
    User query(Long id);
    List<User> queryByName(String name);
}
