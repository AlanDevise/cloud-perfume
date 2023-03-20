package com.alandevise.config;

import com.alandevise.Task.MyTask;
import org.quartz.*;
import org.springframework.context.annotation.Bean;

/**
 * @Filename: QuartzConfig.java
 * @Package: com.alandevise.config
 * @Version: V1.0.0
 * @Description: 1. 定时器任务配置类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月20日 21:13
 */

// @Configuration
public class QuartzConfig {
    //指定具体的定时任务类
    @Bean
    public JobDetail uploadTaskDetail() {
        // 将MyTask加入到定时任务中
        return JobBuilder.newJob(MyTask.class).withIdentity("MyTask").storeDurably().build();
    }

    @Bean
    public Trigger uploadTaskTrigger() {
        //TODO 这里设定执行方式
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("*/5 * * * * ?");
        // 返回任务触发器
        return TriggerBuilder.newTrigger().forJob(uploadTaskDetail())
                .withIdentity("MyTask")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
