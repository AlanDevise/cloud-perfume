package com.alandevise;

import com.alandevise.entity.User;
import com.alandevise.dao.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Filename: MybatisPlusApplicationTests.java
 * @Package: com.alandevise
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-22 14:17
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class MybatisPlusApplicationTests {
    //继承了BaseMapper,所有的方法都来自父类
    //也可以自己编写自己的扩展方法
    @Autowired
    private UserMapper userMapper;

    @Test
    public void contextLoads() {

        //查询全部的用户
        //参数是一个Wrapper 条件构造器，这里先不用 参数写null
        List<User> users = userMapper.selectList(null);
        users.forEach(System.out::println);

    }

    // 测试插入
    @Test
    public void testInsert(){
        User user = new User();
        user.setName("落日很温柔");
        user.setAge(3);
        user.setEmail("2066@qq.com");
        int result = userMapper.insert(user); // 帮我们自动生成id
        System.out.println(result); // 受影响的行数
        System.out.println(user); // 发现，id会自动回填
    }

    // 测试更新
    @Test
    public void testUpdate(){
        User user = new User();
        // 通过条件自动拼接动态sql
        user.setId(Long.parseLong("3"));
        user.setName("Corrine");
        user.setAge(29);
        // 注意：updateById   但是参数是一个 对象！
        int i = userMapper.updateById(user);
        System.out.println(i);
    }

    @Test
    public void testSelect(){
        List<User> users = userMapper.selectByName("Corrine");
        System.out.println(users);
    }

}