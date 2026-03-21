package com.alandevise.multitsdb.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WriteDataRequest {
    private String tsdbType;
    private String database;
    private List<TimeSeriesDataDTO> dataList;
    
    @Data
    public static class TimeSeriesDataDTO {
        private String device;
        private String measurement;
        private Long timestamp;
        private Map<String, Object> fields;
        private Map<String, String> tags;
    }
}
