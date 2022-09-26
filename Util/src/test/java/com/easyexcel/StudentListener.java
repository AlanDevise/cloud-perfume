package com.easyexcel;

import com.alandevise.easyexcel.entity.Student;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

/**
 * @Filename: StudentListener.java
 * @Package: com.easyexcel
 * @Version: V1.0.0
 * @Description: 1. 读取学生信息文档监听器类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月26日 22:47
 */

public class StudentListener extends AnalysisEventListener<Student> {

    // readListener – 读监听器，每读一行内容，都会调用一次该对象的invoke，在invoke可以操作使用读取到的数据.
    /*
     * @param student    每次读取到的数据封装对象
     * @param context
     * */
    @Override
    public void invoke(Student student, AnalysisContext context) {
        System.out.println("student = " + student);
    }

    /*
     * 读取完整个文档后调用的方法
     * */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }

}
