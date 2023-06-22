package com.alandevise.PaymentConsumer.config;

import com.alandevise.PaymentConsumer.message.ProcessMessagesChannelOne;
import com.alandevise.PaymentConsumer.message.ProcessMessagesChannelTwo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @Filename: RedisMessageBroadcastConfig.java
 * @Package: com.alandevise.PaymentConsumer.config
 * @Version: V1.0.0
 * @Description: 1. Redis消息广播配置类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年06月22日 15:25
 */

@Configuration
public class RedisMessageBroadcastConfig {

    private final String ChannelOne = "ChannelOne";

    private final String ChannelTwo = "ChannelTwo";

    /**
     * Redis订阅消息监听器
     *
     * @param connectionFactory 连接工厂
     * @param channelOneAdapter 通道一适配器
     * @param channelTwoAdapter 通道二适配器
     * @return RedisMessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter channelOneAdapter,
                                                   MessageListenerAdapter channelTwoAdapter) {
        // 1. 新建一个Redis消息监听器容器
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        // 2. 设置Redis连接工厂
        listenerContainer.setConnectionFactory(connectionFactory);
        // 3.1 添加监听频道1
        listenerContainer.addMessageListener(channelOneAdapter, new PatternTopic(ChannelOne));
        // 3.2 添加监听频道2
        listenerContainer.addMessageListener(channelTwoAdapter, new PatternTopic(ChannelTwo));
        return listenerContainer;
    }

    /**
     * 委托对象 当我们监听的频道1 有新消息到来时，使用defaultListenerMethod来处理订阅的消息
     * 此处springboot利用反射的技术，使用defaultListenerMethod处理消息
     *
     * @param processMessagesChannelOne 处理消息1
     * @return MessageListenerAdapter
     */
    @Bean
    public MessageListenerAdapter channelOneAdapter(ProcessMessagesChannelOne processMessagesChannelOne) {
        return new MessageListenerAdapter(processMessagesChannelOne, "defaultProcessMethod");
    }

    /**
     * 委托对象 当我们监听的频道2 有新消息到来时，使用defaultListenerMethod来处理订阅的消息
     * 此处springboot利用反射的技术，使用defaultListenerMethod处理消息
     *
     * @param processMessagesChannelTwo 处理消息2
     * @return MessageListenerAdapter
     */
    @Bean
    public MessageListenerAdapter channelTwoAdapter(ProcessMessagesChannelTwo processMessagesChannelTwo) {
        return new MessageListenerAdapter(processMessagesChannelTwo, "defaultProcessMethod");
    }
}
