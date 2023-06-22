package com.alandevise.PaymentConsumer.message;

import com.alandevise.easyexcel.entity.redisMessageBroadcast.RedisMessageProcess;
import org.springframework.stereotype.Component;

/**
 * @Filename: ProcessMessagesChannelTwo.java
 * @Package: com.alandevise.PaymentConsumer.message
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年06月22日 15:35
 */

@Component
public class ProcessMessagesChannelTwo implements RedisMessageProcess {
    @Override
    public synchronized void defaultProcessMethod(String message) {
        System.out.println("ChannelTwo的处理逻辑");
    }
}
