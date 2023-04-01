package com.alandevise.nettyTool;

import lombok.Data;

/**
 * @Filename: NettyMessage.java
 * @Package: com.alandevise.nettyTool
 * @Version: V1.0.0
 * @Description: 1. 消息定义
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年04月01日 14:40
 */

@Data
public final class NettyMessage {
    /**
     * 消息头
     */
    private Header header;
    /**
     * 消息体
     */
    private Object body;
}

