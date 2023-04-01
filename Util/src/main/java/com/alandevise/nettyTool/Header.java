package com.alandevise.nettyTool;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @Filename: Header.java
 * @Package: com.alandevise.nettyTool
 * @Version: V1.0.0
 * @Description: 1. 消息头定义
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年04月01日 14:40
 */

@Data
public final class Header {

    private int crcCode = 0xadaf0105;
    /**
     * 消息长度
     */
    private int length;
    /**
     * 会话ID
     */
    private long sessionId;
    /**
     * 消息类型
     */
    private byte type;
    /**
     * 消息优先级
     */
    private byte priority;
    /**
     * 附件
     */
    private Map<String, Object> attachment = new HashMap<>();

}

