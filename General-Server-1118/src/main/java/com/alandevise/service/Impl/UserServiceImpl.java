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

    /**
     * 创建单个用户
     *
     * @param user 用户对象实体
     * @return java.lang.Boolean
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 10:39
     */
    @Override
    public Boolean create(User user) {
        int insert = studentMapper.create(user);
        return insert > 0;
    }

    /**
     * 批量创建用户
     *
     * @param user 用户对象，多个用户的用户名以逗号分隔，其他的参数均相同
     * @return java.lang.Boolean
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 10:38
     */
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

    /**
     * 根据用户ID更新用户
     *
     * @param user 用户对象实体
     * @return java.lang.Boolean
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 09:55
     */
    @Override
    public Boolean updateUser(User user) {
        int update = studentMapper.updateUser(user);
        return update > 0;
    }

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return com.alandevise.entity.User
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 10:37
     */
    @Override
    public User query(Long id) {
        return studentMapper.selectUser(id);
    }

    /**
     * 根据用户名模糊查询
     *
     * @param name 用户名
     * @return java.util.List<com.alandevise.entity.User>
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 10:38
     */
    @Override
    public List<User> queryByName(String name) {
        return studentMapper.selectUserList(name);
    }
}
