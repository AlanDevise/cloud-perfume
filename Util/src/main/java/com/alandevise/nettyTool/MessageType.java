package com.alandevise.nettyTool;

/**
 * @Filename: MessageType.java
 * @Package: com.alandevise.nettyTool
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年04月01日 14:41
 */

public enum MessageType {

    /**
     * 业务请求消息
     */
    SERVICE_REQ((byte) 0),
    /**
     * 业务响应（应答）消息
     */
    SERVICE_RESP((byte) 1),
    /**
     * 业务ONE WAY消息（既是请求消息又是响应消息）
     */
    ONE_WAY((byte) 2),
    /**
     * 握手请求消息
     */
    LOGIN_REQ((byte) 3),
    /**
     * 握手响应（应答）消息
     */
    LOGIN_RESP((byte) 4),
    /**
     * 心跳请求消息
     */
    HEARTBEAT_REQ((byte) 5),
    /**
     * 心跳响应（应答）消息
     */
    HEARTBEAT_RESP((byte) 6);


    private byte value;

    MessageType(byte value) {
        this.value = value;
    }

    public byte value() {
        return value;
    }
}

