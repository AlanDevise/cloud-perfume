package com.alandevise.GeneralServer.Task;

import com.alandevise.GeneralServer.entity.User;
import com.alandevise.GeneralServer.service.UserService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Filename: MyTask1.java
 * @Package: com.alandevise.Task
 * @Version: V1.0.0
 * @Description: 1. 一个定时任务类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月20日 21:25
 */

public class MyTask1 extends QuartzJobBean {

    //验证是否成功可以注入service   之前在ssm当中需要额外进行配置
    @Resource
    private UserService userService;
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        User student = new User(null,"Alan","user@mail.com",1999,"asdfasdf",null,null);
        userService.create(student);
        //TODO 这里写定时任务的执行逻辑
        System.out.println("动态的定时任务执行时间："+ new Date());
    }
}
