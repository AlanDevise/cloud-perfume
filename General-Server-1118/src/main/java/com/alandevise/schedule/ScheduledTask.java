package com.alandevise.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    @Scheduled(fixedDelay = 10000)
    public static void test() {
        System.out.println("[INFO " + System.currentTimeMillis() + "] 定时任务在行动。");
    }
}
