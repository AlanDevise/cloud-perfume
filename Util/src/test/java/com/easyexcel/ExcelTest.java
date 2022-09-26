package com.easyexcel;

import com.alandevise.easyexcel.entity.Student;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @Filename: ExcelTest.java
 * @Package: com.easyexcel
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月26日 22:35
 */

@Slf4j
public class ExcelTest {

    /*
     * 工作簿：一个Excel文件就是一个工作簿
     * 工作表：一个sheet就是一个工作表，一个工作簿中可以有多个工作表
     * */
    @Test
    public void readTest() {
        // 1. 获得一个工作簿对象
        /*
        * Params:
            pathName – 要读的Excel文件路径.
            head – 文件中每一行要存储到实体的类型的class.
            readListener – 读监听器，每读一行内容，都会调用一次该对象的invoke，在invoke可以操作使用读取到的数据.
          Returns:
            Excel reader builder.
        * */
        ExcelReaderBuilder readWorkBook = EasyExcel.read("学员信息表.xlsx", Student.class, new StudentListener());

        // 2. 获得一个工作表对象
        ExcelReaderSheetBuilder sheet = readWorkBook.sheet();

        // 3. 读取工作表中的内容
        sheet.doRead();
    }


}
