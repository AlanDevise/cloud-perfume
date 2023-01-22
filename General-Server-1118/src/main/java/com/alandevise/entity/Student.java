package com.alandevise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Filename: Student.java
 * @Package: com.alandevise.entity
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023-01-22 20:16
 */

@Data
@TableName(value = "student")
public class Student {

    /**  主键  type:自增 */
    @TableId(type = IdType.AUTO)
    private int id;

    /**  名字 */
    private String name;

    /**  年龄 */
    private int age;

    /**  地址 */
    private String addr;

    /**  地址号  @TableField：与表字段映射 */
    @TableField(value = "addr_num")
    private String addrNum;

    public Student(String name, int age, String addr, String addrNum) {
        this.name = name;
        this.age = age;
        this.addr = addr;
        this.addrNum = addrNum;
    }
}
