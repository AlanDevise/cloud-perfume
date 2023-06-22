package com.alandevise.PaymentConsumer.message;

import com.alandevise.easyexcel.entity.redisMessageBroadcast.RedisMessageProcess;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;

/**
 * @Filename: ProcessMessagesChannelOne.java
 * @Package: com.alandevise.PaymentConsumer.message
 * @Version: V1.0.0
 * @Description: 1. 处理通道1中消息
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年06月22日 15:33
 */

@Component
public class ProcessMessagesChannelOne implements RedisMessageProcess {
    @Override
    public synchronized void defaultProcessMethod(String message) {
        String subject = "";
        JSONObject messageBody = new JSONObject();
        try {
            // 解析消息体
            messageBody = JSONObject.parseObject(message);
            subject = messageBody.getString("subject");
        } catch (Exception e) {
            System.out.println("[ERROR] 消息体解析异常");
            throw e;
        }
        // 针对不同的subject做不同的处理
        switch (subject) {
            case "1":
                dealWithMessageTypeIsOne(messageBody);
                break;
            case "2":
                dealWithMessageTypeIsTwo(messageBody);
                break;
            default:
                System.out.println("[WARNING] 没有对应的消息处理方法，不做任何处理");
                break;
        }
    }

    private void dealWithMessageTypeIsOne(JSONObject message) {
        System.out.println("\n" + System.currentTimeMillis() + "收到了主题为1的消息,具体内容是：" + message.getString("content") + "\n");
    }

    private void dealWithMessageTypeIsTwo(JSONObject message) {
        System.out.println("\n" + System.currentTimeMillis() + "收到了主题为2的消息,具体内容是：" + message.getString("content") + "\n");
    }
}
