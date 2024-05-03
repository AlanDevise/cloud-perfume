package com.alandevise.GeneralServer.Task;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@ConditionalOnProperty(value = "alancfg.open") // 根据配置文件得true/false来判断是否生成此定时任务得类
@EnableScheduling
@Slf4j
public class DbMasterConnTest implements SchedulingConfigurer {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 从配置文件中读取的定时任务执行周期（cron表达式）
     */
    @Value("${alancfg.interval}")
    private String cron;

    /**
     * 数据源
     */
    @Resource
    HikariDataSource dataSource;

    /**
     * 数据库连接
     */
    Connection connection;

    /**
     * 重写的配置任务
     *
     * @param scheduledTaskRegistrar 定时任务注册
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        try {
            scheduledTaskRegistrar.addTriggerTask(this::process,
                    triggerContext -> {
                        if (cron.isEmpty()) {
                            System.out.println("cron is null");
                        }
                        return new CronTrigger(cron).nextExecutionTime(triggerContext);
                    });
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 定时任务逻辑
     *
     * @return
     */
    private void process() {
        try {
            connection = dataSource.getConnection();
            log.info("The database connection is {}", connection);
            Statement statement = connection.createStatement();
            statement.execute("select 1");
        } catch (SQLException e) {
            log.error("执行时出现以下异常：{}", e.getMessage());
            dataSource.evictConnection(connection);
        }finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("关闭连接时出现以下异常：{}",e.getMessage());
                }
            }
        }
        System.out.println("基于接口定时任务");
    }
}