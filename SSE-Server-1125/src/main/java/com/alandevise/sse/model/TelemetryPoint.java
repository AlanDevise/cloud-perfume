package com.alandevise.sse.model;

/**
 * 单个测点值。
 */
public class TelemetryPoint {

    private String name;
    private int value;

    public TelemetryPoint() {
    }

    public TelemetryPoint(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
