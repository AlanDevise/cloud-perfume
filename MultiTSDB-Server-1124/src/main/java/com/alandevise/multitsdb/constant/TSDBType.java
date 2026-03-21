package com.alandevise.multitsdb.constant;

public enum TSDBType {
    IOTDB("iotdb"),
    TDENGINE("tdengine");

    private final String type;

    TSDBType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static TSDBType fromType(String type) {
        for (TSDBType tsdbType : values()) {
            if (tsdbType.type.equalsIgnoreCase(type)) {
                return tsdbType;
            }
        }
        throw new IllegalArgumentException("Unknown TSDB type: " + type);
    }
}
