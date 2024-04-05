package com.alandevise.GeneralServer.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Filename: NacosServiceInstanceChangeNotifier.java
 * @Package: com.alandevise.config
 * @Version: V1.0.0
 * @Description: 1. 监听本服务在Nacos中的上下线信息
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年11月07日 22:49
 */

@Component
@Slf4j
public class NacosServiceInstanceChangeNotifier extends Subscriber<InstancesChangeEvent> {

    @PostConstruct
    public void registerToNotifyCenter() {
        NotifyCenter.registerSubscriber(this);
    }

    // 事件发生时，收到提示
    @Override
    public void onEvent(InstancesChangeEvent instancesChangeEvent) {
        log.info("监听nacos的服务实例变化情况:{}", JSON.toJSONString(instancesChangeEvent));
    }

    @Override
    public Class<? extends Event> subscribeType() {
        return InstancesChangeEvent.class;
    }
}
