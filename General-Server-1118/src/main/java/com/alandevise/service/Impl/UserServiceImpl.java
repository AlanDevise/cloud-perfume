package com.alandevise.service.Impl;

import com.alandevise.dao.StudentMapper;
import com.alandevise.entity.User;
import com.alandevise.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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

    // 创建一个堆栈来存储父节点，符合FILO
    static Stack<String> parentId = new Stack<>();
    @Resource
    StudentMapper studentMapper;

    @Override
    public Boolean create(User user) {
        int insert = studentMapper.create(user);
        return insert > 0;
    }

    @Override
    public Boolean batchCreate(User user) {
        String nameStr = user.getName();
        String[] nameArray = nameStr.split(",");
        List<User> newUserList = new ArrayList<>();
        for (String name : nameArray) {
            User newUser = User.builder()
                    .balance(user.getBalance())
                    .password(user.getPassword())
                    .name(name)
                    .build();
            newUserList.add(newUser);
        }
        return studentMapper.batchCreateUser(newUserList) > 0;
    }

    @Override
    public User query(Long id) {
        return studentMapper.selectUser(id);
    }

    @Override
    public List<User> queryByName(String name) {
        return studentMapper.selectUserList(name);
    }
}
