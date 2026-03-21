package com.alandevise.multitsdb.dto;

import lombok.Data;

@Data
public class QueryDataRequest {
    private String tsdbType;
    private String database;
    private String sql;
}
