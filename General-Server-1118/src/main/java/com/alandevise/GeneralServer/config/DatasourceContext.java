package com.alandevise.GeneralServer.config;

import com.alandevise.GeneralServer.Task.DbMasterConnTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @Filename: DataSourceType.java
 * @Package: com.alandevise.logger.config
 * @Version: V1.0.0
 * @Description: 1. 生成一个Java Bean 以能够在程序任何位置感知连接的数据库类型
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2024年03月02日 00:17
 */
@Component
@Slf4j
public class DatasourceContext implements InitializingBean, EnvironmentAware {

    private String driverClassName;

    static DataType dataType;

    @Override
    public void afterPropertiesSet() {
        if (StringUtils.isNotBlank(String.valueOf(driverClassName))
                && driverClassName.contains("mysql")) {
            dataType = DataType.MYSQL;
        } else {
            dataType = DataType.UNKNOWN;
        }
    }

    public static DataType getDatasourceType() {
        return dataType;
    }

    @Override
    public void setEnvironment(Environment environment) {
        driverClassName = environment.getProperty("spring.datasource.driver-class-name");
    }

    public enum DataType {
        MYSQL,
        PGSQL,
        UNKNOWN,
        ;
    }
}
