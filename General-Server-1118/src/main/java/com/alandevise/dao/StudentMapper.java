package com.alandevise.dao;

import com.alandevise.entity.Student;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Filename: StudentMapper.java
 * @Package: com.alandevise.dao
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023-01-22 20:22
 */

@Mapper
public interface StudentMapper {
    void insert(@Param("student") Student student);
    int insertSplice(@Param("studentList") List<Student> studentList);
}
