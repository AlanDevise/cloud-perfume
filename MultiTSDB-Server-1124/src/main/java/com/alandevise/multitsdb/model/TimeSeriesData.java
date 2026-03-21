package com.alandevise.multitsdb.model;

import lombok.Data;

import java.util.Map;

@Data
public class TimeSeriesData {
    private String device;
    private String measurement;
    private Long timestamp;
    private Map<String, Object> fields;
    private Map<String, String> tags;
}
