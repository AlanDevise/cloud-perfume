package com.alandevise.easyexcel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Filename: Student.java
 * @Package: com.alandevise.easyexcel.entity
 * @Version: V1.0.0
 * @Description: 1. 学生实体类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月26日 22:23
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    private String name;
    private String gender;
    private Date birthday;
    private String id;
}
