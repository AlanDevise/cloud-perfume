package com.alandevise.service.impl;

import com.alandevise.dao.UserMapper;
import com.alandevise.entity.User;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Filename: UserDetailsServiceImpl.java
 * @Package: com.alandevise.service.impl
 * @Version: V1.0.0
 * @Description: 1. UserDetailsService实现类，可以直接针对SpringSecurity默认的登录界面做登录逻辑处理
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月06日 15:02
 */

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. 查询数据库判断用户名是否存在，如果不存在则抛出UsernameNotFoundException
        User users = userMapper.selectByName(username);
        if (users == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }

        // 能够获取用户属性，如姓名，年龄，密码等
        // users.getName();

        // 2. 如果存在，把查询出来的密码（注册时已经加密，不可能是明文）进行解析，或者直接把密码放入构造方法
        return new org.springframework.security.core.userdetails.User(
                username,
                users.getPassword(),
                AuthorityUtils.commaSeparatedStringToAuthorityList("admin,normal,ROLE_admin"));
    }

}
