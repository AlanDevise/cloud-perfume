package com.alandevise.multidatasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.alandevise.multidatasource.po.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Filename: UserMapper.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {


}
