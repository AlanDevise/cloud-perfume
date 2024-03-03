package com.alandevise.multidatasource.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Filename: User.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */

@Data
@TableName("t_user")
public class User {


    @ApiModelProperty("用户id")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("手机")
    private String mobile;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;


    public static User randomMakeUser() {
        User user = new User();
        user.setUsername("eric");
        user.setMobile("13000000000");
//        user.setCreatedAt(new Date());
//        user.setUpdatedAt(new Date());
        return user;
    }


}
