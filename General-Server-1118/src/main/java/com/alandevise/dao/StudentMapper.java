package com.alandevise.dao;

import com.alandevise.entity.SQL;
import com.alandevise.entity.Student;
import com.alandevise.entity.TFAccrue;
import com.alandevise.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;

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

    int create(@Param("user") User user);

    User selectUser(@Param("id") Long id);

    void insertSql(@Param("sql") String sql);

    void insertSql2(@Param("tableName2") String tableName2,
                    @Param("columns") String columns,
                    @Param("val") String val);
    void insertSql3(@Param("SQL") SQL SQL);

    void insertTFAccrue(@Param("TFAccrue") TFAccrue TFAccrue);

    Cursor<TFAccrue> selectInfo(@Param("SQL") String SQL);

    void insertId(@Param("id") String id);
}
