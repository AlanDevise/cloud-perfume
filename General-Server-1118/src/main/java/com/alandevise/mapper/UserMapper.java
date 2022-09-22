package com.alandevise.mapper;

import com.alandevise.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * @Filename: UserMapper.java
 * @Package: com.alandevise.mapper
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-22 13:58
 */

//在对应的Mapper 上面继承基本的类 BaseMapper
//@Mapper
@Repository(value ="userMapper")   //代表持久层
//@Component
public interface UserMapper extends BaseMapper<User> {
    //所有的CRUD操作都已经编写完成
    //不需要像以前一样配置一大堆文件了
}

