package com.alandevise.tsdb.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通用查询结果对象。
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2026-03-22
 */
@Data
public class QueryResult {
    /**
     * 返回列名列表。
     */
    private List<String> columns = new ArrayList<>();
    /**
     * 返回数据行列表。
     */
    private List<Map<String, Object>> rows = new ArrayList<>();
    /**
     * 返回行数。
     */
    private int rowCount;
    /**
     * 执行结果说明。
     */
    private String message;
    /**
     * 是否成功。
     */
    private boolean success;

    /**
     * 快速构造失败结果。
     *
     * @param message 失败原因
     * @return 失败结果对象
     */
    public static QueryResult failure(String message) {
        QueryResult result = new QueryResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}
