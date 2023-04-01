package com.alandevise.nettyTool;

/**
 * @Filename: ResultType.java
 * @Package: com.alandevise.nettyTool
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年04月01日 14:42
 */

public enum ResultType {
    /**
     * 认证成功
     */
    SUCCESS((byte) 0),
    /**
     * 认证失败
     */
    FAIL((byte) -1),
    ;


    private byte value;

    private ResultType(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }


}

