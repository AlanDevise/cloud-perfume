package com.alandevise.GeneralServer.Task;

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
import javax.sql.DataSource;

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
    DataSource dataSource;

    /**
     * 重写的配置任务
     * @param scheduledTaskRegistrar 定时任务注册
     */
    @SneakyThrows
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
     */
    private void process() {
        System.out.println("基于接口定时任务");
    }
}