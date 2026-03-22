package com.alandevise.tsdb.core;

import com.alandevise.tsdb.adapter.TSDBAdapter;
import com.alandevise.tsdb.autoconfigure.TSDBAdapterManager;
import com.alandevise.tsdb.autoconfigure.TSDBProperties;
import com.alandevise.tsdb.model.QueryResult;
import com.alandevise.tsdb.model.TimeSeriesData;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统一的多时序库读写工具模板。
 * 调用方通常只需要注入这个类即可读写不同的 TSDB。
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2026-03-22
 */
@Slf4j
public class MultiTSDBTemplate {

    private final TSDBAdapterManager adapterManager;
    private final TSDBProperties properties;

    /**
     * @param adapterManager 适配器管理器，用于获取具体时序库实现
     * @param properties TSDB 配置参数，用于兜底默认类型和默认数据库
     */
    public MultiTSDBTemplate(TSDBAdapterManager adapterManager, TSDBProperties properties) {
        this.adapterManager = adapterManager;
        this.properties = properties;
    }

    /**
     * 写入单条时序数据。
     *
     * @param tsdbType 时序库类型；为空时走 tsdb.default-type，再兜底 iotdb
     * @param database 数据库名；为空时走 tsdb.default-database，再兜底 cloud_platform
     * @param data 时序数据对象；timestamp 为空时自动补当前毫秒时间
     * @return 是否写入成功
     */
    public boolean write(String tsdbType, String database, TimeSeriesData data) {
        TSDBAdapter adapter = requireAdapter(tsdbType);
        return adapter != null && adapter.writeData(resolveDatabase(database), normalizeData(data));
    }

    /**
     * 使用默认时序库类型写入单条数据。
     *
     * @param database 数据库名；为空时走默认数据库
     * @param data 时序数据对象
     * @return 是否写入成功
     */
    public boolean write(String database, TimeSeriesData data) {
        return write(null, database, data);
    }

    /**
     * 批量写入时序数据。
     * IoTDB 树模型下会优先尝试 Tablet 批量写入。
     *
     * @param tsdbType 时序库类型；为空时走默认类型
     * @param database 数据库名；为空时走默认数据库
     * @param dataList 时序数据列表；每条数据 timestamp 为空时自动补当前时间
     * @return 是否全部写入成功
     */
    public boolean batchWrite(String tsdbType, String database, List<TimeSeriesData> dataList) {
        TSDBAdapter adapter = requireAdapter(tsdbType);
        return adapter != null && adapter.batchWriteData(resolveDatabase(database), normalizeDataList(dataList));
    }

    /**
     * 使用默认时序库类型批量写入数据。
     *
     * @param database 数据库名；为空时走默认数据库
     * @param dataList 时序数据列表
     * @return 是否全部写入成功
     */
    public boolean batchWrite(String database, List<TimeSeriesData> dataList) {
        return batchWrite(null, database, dataList);
    }

    /**
     * 执行查询 SQL。
     *
     * @param tsdbType 时序库类型；为空时走默认类型
     * @param database 数据库名；为空时走默认数据库
     * @param sql 查询 SQL；可写完整 SQL，也可只写 measurement/table 名
     * @return 查询结果
     */
    public QueryResult query(String tsdbType, String database, String sql) {
        TSDBAdapter adapter = requireAdapter(tsdbType);
        if (adapter == null) {
            return QueryResult.failure(buildMissingAdapterMessage(resolveType(tsdbType)));
        }
        return adapter.queryData(resolveDatabase(database), sql);
    }

    public QueryResult query(String database, String sql) {
        return query(null, database, sql);
    }

    /**
     * 直接执行 SQL。
     *
     * @param tsdbType 时序库类型；为空时走默认类型
     * @param database 数据库名；为空时走默认数据库
     * @param sql 要执行的 SQL，支持查询类和非查询类语句
     * @return 执行结果
     */
    public QueryResult executeSql(String tsdbType, String database, String sql) {
        TSDBAdapter adapter = requireAdapter(tsdbType);
        if (adapter == null) {
            return QueryResult.failure(buildMissingAdapterMessage(resolveType(tsdbType)));
        }
        return adapter.executeSql(resolveDatabase(database), sql);
    }

    public QueryResult executeSql(String database, String sql) {
        return executeSql(null, database, sql);
    }

    /**
     * 按时间范围查询数据。
     *
     * @param tsdbType 时序库类型；为空时走默认类型
     * @param database 数据库名；为空时走默认数据库
     * @param measurement 测点名或表名
     * @param startTime 开始时间戳，毫秒
     * @param endTime 结束时间戳，毫秒
     * @param limit 返回条数限制；为空时默认 100
     * @return 查询结果
     */
    public QueryResult queryByTimeRange(String tsdbType, String database, String measurement,
                                        Long startTime, Long endTime, Integer limit) {
        TSDBAdapter adapter = requireAdapter(tsdbType);
        if (adapter == null) {
            return QueryResult.failure(buildMissingAdapterMessage(resolveType(tsdbType)));
        }
        int resolvedLimit = limit == null ? 100 : limit;
        return adapter.queryDataByTimeRange(resolveDatabase(database), measurement, startTime, endTime, resolvedLimit);
    }

    public QueryResult queryByTimeRange(String database, String measurement,
                                        Long startTime, Long endTime, Integer limit) {
        return queryByTimeRange(null, database, measurement, startTime, endTime, limit);
    }

    /**
     * 创建数据库。
     *
     * @param tsdbType 时序库类型；为空时走默认类型
     * @param database 数据库名
     * @return 是否创建成功
     */
    public boolean createDatabase(String tsdbType, String database) {
        TSDBAdapter adapter = requireAdapter(tsdbType);
        return adapter != null && adapter.createDatabase(database);
    }

    public boolean createDatabase(String database) {
        return createDatabase(null, database);
    }

    /**
     * 删除数据库。
     *
     * @param tsdbType 时序库类型；为空时走默认类型
     * @param database 数据库名
     * @return 是否删除成功
     */
    public boolean dropDatabase(String tsdbType, String database) {
        TSDBAdapter adapter = requireAdapter(tsdbType);
        return adapter != null && adapter.dropDatabase(database);
    }

    public boolean dropDatabase(String database) {
        return dropDatabase(null, database);
    }

    /**
     * 获取所有已注册适配器。
     *
     * @return 适配器映射
     */
    public Map<String, TSDBAdapter> getAdapters() {
        return adapterManager.getAdapters();
    }

    /**
     * 获取所有已注册适配器名称。
     *
     * @return 时序库类型集合
     */
    public Collection<String> getAdapterNames() {
        return Collections.unmodifiableSet(adapterManager.getAdapters().keySet());
    }

    /**
     * 获取可用适配器，不存在时记录错误日志。
     *
     * @param tsdbType 时序库类型
     * @return 具体适配器，不存在时返回 null
     */
    private TSDBAdapter requireAdapter(String tsdbType) {
        String resolvedType = resolveType(tsdbType);
        TSDBAdapter adapter = adapterManager.getAdapter(resolvedType);
        if (adapter == null) {
            log.error(buildMissingAdapterMessage(resolvedType));
        }
        return adapter;
    }

    /**
     * 解析最终使用的时序库类型。
     *
     * @param tsdbType 调用方显式传入的时序库类型
     * @return 最终时序库类型，优先级：入参 > 配置默认值 > iotdb
     */
    private String resolveType(String tsdbType) {
        if (tsdbType != null && !tsdbType.trim().isEmpty()) {
            return tsdbType.trim().toLowerCase();
        }
        if (properties.getDefaultType() != null && !properties.getDefaultType().trim().isEmpty()) {
            return properties.getDefaultType().trim().toLowerCase();
        }
        return "iotdb";
    }

    /**
     * 解析最终使用的数据库名。
     *
     * @param database 调用方显式传入的数据库名
     * @return 最终数据库名，优先级：入参 > 配置默认值
     */
    private String resolveDatabase(String database) {
        if (database == null || database.trim().isEmpty()) {
            return properties.getDefaultDatabase();
        }
        return database;
    }

    /**
     * 标准化单条写入数据。
     *
     * @param data 原始时序数据
     * @return 标准化后的时序数据
     */
    private TimeSeriesData normalizeData(TimeSeriesData data) {
        if (data == null) {
            return null;
        }
        // 调用方未传 timestamp 时，统一使用当前时间写入时序库。
        if (data.getTimestamp() == null) {
            data.setTimestamp(System.currentTimeMillis());
        }
        return data;
    }

    /**
     * 标准化批量写入数据列表。
     *
     * @param dataList 原始时序数据列表
     * @return 标准化后的时序数据列表
     */
    private List<TimeSeriesData> normalizeDataList(List<TimeSeriesData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return dataList;
        }
        List<TimeSeriesData> normalized = new ArrayList<>(dataList.size());
        for (TimeSeriesData data : dataList) {
            normalized.add(normalizeData(data));
        }
        return normalized;
    }

    /**
     * 构造适配器不存在时的错误信息。
     *
     * @param tsdbType 时序库类型
     * @return 错误提示信息
     */
    private String buildMissingAdapterMessage(String tsdbType) {
        return "No TSDB adapter available for type: " + tsdbType;
    }
}
