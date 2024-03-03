package com.alandevise.multidatasource.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * @Filename: DataSourceConfig.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1. 主备数据源初始化bean的逻辑
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */
@Configuration
public class DataSourceConfig {

    @Bean(name = "master")
    @ConfigurationProperties("spring.datasource.master")
    public DataSource masterDataSource() {
        // 写数据源
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "slave")
    @ConfigurationProperties("spring.datasource.slave")
    public DataSource slave1DataSource() {
        // 读数据源
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "dynamicDataSource")
    public DynamicDataSource dynamicDataSource() {

        return DynamicDataSource.builder()
                .withMasterDataSource(masterDataSource())
                .withSlaveDataSource(slave1DataSource())
                .withTargetDataSource(masterDataSource(), slave1DataSource());
    }


    @Bean
    public PlatformTransactionManager transactionManager() {
        // 配置事务管理, 使用事务时在方法头部添加@Transactional注解即可
        return new DataSourceTransactionManager(dynamicDataSource());
    }
}
