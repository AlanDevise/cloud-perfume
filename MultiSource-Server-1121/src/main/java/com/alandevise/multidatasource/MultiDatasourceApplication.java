package com.alandevise.multidatasource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @Filename: MultiDataSourceApplication.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */
@SpringBootApplication
@MapperScan({"com.alandevise.**.mapper"})
public class MultiDatasourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiDatasourceApplication.class, args);
    }

}
