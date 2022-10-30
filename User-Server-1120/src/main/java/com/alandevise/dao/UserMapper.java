package com.alandevise.dao;

import com.alandevise.entity.UserAllInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * @Filename: UserMapper.java
 * @Package: com.alandevise.dao
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月30日 20:38
 */

@Component
public interface UserMapper extends BaseMapper<UserAllInfo> {
    //所有的CRUD操作都已经编写完成
    //不需要像以前一样配置一大堆文件了
    // [Alan COMMENT] Mybatis Plus 封装了部分常用sql，可直接调用，同时可以自行编写sql

    // 使函数参数对应xml中的参数wxNickName
    UserAllInfo selectByName(@Param("username") String username);

}
