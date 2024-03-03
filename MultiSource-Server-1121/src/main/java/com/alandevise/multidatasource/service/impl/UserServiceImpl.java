package com.alandevise.multidatasource.service.impl;

import com.alandevise.multidatasource.annotation.MultiDataSource;
import com.alandevise.multidatasource.constants.DataSourceType;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alandevise.multidatasource.mapper.UserMapper;
import com.alandevise.multidatasource.po.User;
import com.alandevise.multidatasource.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Filename: UserServiceImpl.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        return user;
    }

    @Override
    public User saveRandomUser() {
        User user = saveUser(User.randomMakeUser());
        userMapper.selectById(user.getId());
        return saveUser(User.randomMakeUser());
    }

    @MultiDataSource(connectTo = DataSourceType.MASTER)
    public User saveUser(User user) {
        userMapper.insert(user);
        return user;
    }


}
