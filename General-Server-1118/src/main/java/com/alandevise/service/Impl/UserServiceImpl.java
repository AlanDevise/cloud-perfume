package com.alandevise.service.Impl;

import com.alandevise.dao.StudentMapper;
import com.alandevise.entity.User;
import com.alandevise.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Filename: UserServiceImpl.java
 * @Package: com.alandevise.service.Impl
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:44
 */

@Service
public class UserServiceImpl implements UserService {
    @Resource
    StudentMapper studentMapper;

    @Override
    public Boolean create(User user) {
        int insert = studentMapper.create(user);
        return insert > 0;
    }

    @Override
    public User query(Long id) {
        return studentMapper.selectUser(id);
    }
}
