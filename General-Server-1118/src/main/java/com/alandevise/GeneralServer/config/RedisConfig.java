package com.alandevise.GeneralServer.config;

import com.alandevise.GeneralServer.util.IGlobalCache;
import com.alandevise.GeneralServer.util.Impl.AppRedisCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Filename: RedisConfig.java
 * @Package: com.alandevise.logger.config
 * @Version: V1.0.0
 * @Description: 1. Redis配置类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-20 15:30
 */

// @EnableCaching
@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.database}")
    private Integer database;

    @Value("${spring.redis.port}")
    private Integer port;

    @Value("${spring.redis.password}")
    private String pwd;

    private final String ChannelOne = "ChannelOne";

    private final String ChannelTwo = "ChannelTwo";

    @Primary
    @Bean(name = "jedisPoolConfig")
    // @ConfigurationProperties(prefix = "spring.redis.pool")
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxWaitMillis(10000);
        return jedisPoolConfig;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(JedisPoolConfig jedisPoolConfig) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setDatabase(database);
        redisStandaloneConfiguration.setPassword(pwd);
        redisStandaloneConfiguration.setPort(port);
        JedisClientConfiguration.JedisPoolingClientConfigurationBuilder jpcb = (JedisClientConfiguration.JedisPoolingClientConfigurationBuilder) JedisClientConfiguration.builder();
        jpcb.poolConfig(jedisPoolConfig);
        JedisClientConfiguration jedisClientConfiguration = jpcb.build();
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
    }

    /**
     * 配置redisTemplate针对不同key和value场景下不同序列化的方式
     *
     * @param factory Redis连接工厂
     * @return RedisTemplate<String, Object>
     */
    @Primary
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        Jackson2JsonRedisSerializer redisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        template.setValueSerializer(redisSerializer);
        template.setHashValueSerializer(redisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    IGlobalCache cache(RedisTemplate redisTemplate) {
        return new AppRedisCacheManager(redisTemplate);
    }


    // /**
    //  * Redis订阅消息监听器
    //  *
    //  * @param connectionFactory 连接工厂
    //  * @param channelOneAdapter 通道一适配器
    //  * @param channelTwoAdapter 通道二适配器
    //  * @return RedisMessageListenerContainer
    //  */
    // @Bean
    // public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
    //                                                MessageListenerAdapter channelOneAdapter,
    //                                                MessageListenerAdapter channelTwoAdapter) {
    //     // 1. 新建一个Redis消息监听器容器
    //     RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
    //     // 2. 设置Redis连接工厂
    //     listenerContainer.setConnectionFactory(connectionFactory);
    //     // 3.1 添加监听频道1
    //     listenerContainer.addMessageListener(channelOneAdapter, new PatternTopic(ChannelOne));
    //     // 3.2 添加监听频道2
    //     listenerContainer.addMessageListener(channelTwoAdapter, new PatternTopic(ChannelTwo));
    //     return listenerContainer;
    // }
    //
    // /**
    //  * 委托对象 当我们监听的频道1 有新消息到来时，使用defaultListenerMethod来处理订阅的消息
    //  * 此处springboot利用反射的技术，使用defaultListenerMethod处理消息
    //  *
    //  * @param processMessagesChannelOne 处理消息1
    //  * @return MessageListenerAdapter
    //  */
    // @Bean
    // public MessageListenerAdapter channelOneAdapter(ProcessMessagesChannelOne processMessagesChannelOne) {
    //     return new MessageListenerAdapter(processMessagesChannelOne, "defaultProcessMethod");
    // }
    //
    // /**
    //  * 委托对象 当我们监听的频道2 有新消息到来时，使用defaultListenerMethod来处理订阅的消息
    //  * 此处springboot利用反射的技术，使用defaultListenerMethod处理消息
    //  *
    //  * @param processMessagesChannelTwo 处理消息2
    //  * @return MessageListenerAdapter
    //  */
    // @Bean
    // public MessageListenerAdapter channelTwoAdapter(ProcessMessagesChannelTwo processMessagesChannelTwo) {
    //     return new MessageListenerAdapter(processMessagesChannelTwo, "defaultProcessMethod");
    // }


}