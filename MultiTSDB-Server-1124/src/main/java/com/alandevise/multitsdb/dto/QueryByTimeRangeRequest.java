package com.alandevise.multitsdb.dto;

import lombok.Data;

@Data
public class QueryByTimeRangeRequest {
    private String database;
    private String measurement;
    private Long startTime;
    private Long endTime;
    private Integer limit;
}
