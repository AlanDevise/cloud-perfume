package com.alandevise.multidatasource.annotation;

import com.alandevise.multidatasource.constants.DataSourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Filename: MultiDataSource.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1. 动态数据源的注解，用来标注当前使用的是主数据源还是备数据源
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MultiDataSource {


    /**
     * 本次访问数据库要连接到的库
     * 默认是从库
     *
     * @return 要连接到主还是从库
     */
    DataSourceType connectTo() default DataSourceType.SLAVE;


}
