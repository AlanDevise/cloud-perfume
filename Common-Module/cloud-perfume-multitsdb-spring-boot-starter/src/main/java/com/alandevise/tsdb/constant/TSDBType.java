package com.alandevise.tsdb.constant;

/**
 * 支持的时序库类型枚举。
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2026-03-22
 */
public enum TSDBType {
    IOTDB("iotdb"),
    TDENGINE("tdengine");

    private final String type;

    /**
     * @param type 时序库类型编码
     */
    TSDBType(String type) {
        this.type = type;
    }

    /**
     * @return 时序库类型编码
     */
    public String getType() {
        return type;
    }
}
