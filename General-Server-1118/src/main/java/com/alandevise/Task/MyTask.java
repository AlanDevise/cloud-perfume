package com.alandevise.Task;

import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

/**
 * @Filename: MyTask.java
 * @Package: com.alandevise.Task
 * @Version: V1.0.0
 * @Description: 1. 新建一个定时任务类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月20日 21:12
 */

public class MyTask extends QuartzJobBean {
    @Override
    protected void executeInternal(@NotNull JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //TODO 这里写定时任务的执行逻辑
        System.out.println("简单的定时任务执行时间："+ new Date());
    }
}
