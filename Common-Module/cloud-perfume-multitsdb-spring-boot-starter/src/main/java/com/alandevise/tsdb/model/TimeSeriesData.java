package com.alandevise.tsdb.model;

import lombok.Data;

import java.util.Map;

/**
 * 通用时序数据模型。
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2026-03-22
 */
@Data
public class TimeSeriesData {
    /**
     * 设备标识。
     * IoTDB 树模型下通常会拼到设备路径中；TDEngine 中会作为普通列写入。
     */
    private String device;
    /**
     * 测点名或表名。
     * IoTDB 表模型、TDEngine 中通常映射为表；IoTDB 树模型中用于查询场景标识。
     */
    private String measurement;
    /**
     * 时间戳，单位毫秒。
     * 为空时 starter 会自动补当前时间。
     */
    private Long timestamp;
    /**
     * 业务字段集合。
     * key 为字段名，value 为字段值。
     */
    private Map<String, Object> fields;
    /**
     * 标签字段集合。
     * TDEngine 中会作为普通列写入；IoTDB 树模型下也会按普通字段写入以保持结构一致。
     */
    private Map<String, String> tags;
}
