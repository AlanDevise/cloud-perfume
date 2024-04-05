package com.alandevise.service;

import com.alandevise.dao.UserMapper;
import com.alandevise.entity.User;
import com.alandevise.entity.UserAllInfo;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Filename: UserService.java
 * @Package: com.alandevise.service
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月23日 22:54
 */

@Service
public class UserService implements UserDetailsService {

    @Resource
    UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 教程版，固定生成
        // String password = passwordEncoder.encode("123");
        // System.out.println(password);
        // return new User("admin", password, AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));

        // 1. 查询数据库判断用户名是否存在，如果不存在则抛出UsernameNotFoundException
        UserAllInfo userAllInfo = userMapper.selectByName(username);
        if (userAllInfo == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }

        return new User(
                userAllInfo.getUsername(),
                userAllInfo.getPassword(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(userAllInfo.getRole())
        );
    }
}
