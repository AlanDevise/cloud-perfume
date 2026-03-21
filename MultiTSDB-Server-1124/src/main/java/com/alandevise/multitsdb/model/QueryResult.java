package com.alandevise.multitsdb.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryResult {
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private int rowCount;
    private String message;
    private boolean success;
}
