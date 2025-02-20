package com.alandevise.GeneralServer.schedule;

import com.alandevise.GeneralServer.dao.StudentMapper;
import com.alandevise.GeneralServer.entity.tUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Filename: ScheduledTask.java
 * @Package: com.alandevise.schedule
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年06月28日 22:06
 */

@Component
public class ScheduledTask {
    @Resource
    StudentMapper studentMapper;

    @Scheduled(fixedDelay = 10000)
    public void test() {
        System.out.println("[INFO " + System.currentTimeMillis() + "] 定时任务在行动。");
        List<tUser> tUsers = studentMapper.AllUser();
        System.out.println(tUsers);
    }

}
