package com.alandevise.tsdb.adapter;

import com.alandevise.tsdb.model.QueryResult;
import com.alandevise.tsdb.model.TimeSeriesData;

import java.util.List;

/**
 * 时序库适配器统一接口。
 * 用于屏蔽 IoTDB 和 TDEngine 的底层差异。
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2026-03-22
 */
public interface TSDBAdapter {

    /**
     * 初始化底层连接。
     */
    void init();

    /**
     * 关闭底层连接。
     */
    void close();

    /**
     * 写入单条数据。
     *
     * @param database 数据库名
     * @param data 时序数据对象
     * @return 是否写入成功
     */
    boolean writeData(String database, TimeSeriesData data);

    /**
     * 批量写入数据。
     *
     * @param database 数据库名
     * @param dataList 时序数据列表
     * @return 是否全部写入成功
     */
    boolean batchWriteData(String database, List<TimeSeriesData> dataList);

    /**
     * 执行查询语句。
     *
     * @param database 数据库名
     * @param sql 查询 SQL
     * @return 查询结果
     */
    QueryResult queryData(String database, String sql);

    /**
     * 直接执行 SQL。
     *
     * @param database 数据库名
     * @param sql 要执行的 SQL
     * @return 执行结果
     */
    QueryResult executeSql(String database, String sql);

    /**
     * 按时间范围查询数据。
     *
     * @param database 数据库名
     * @param measurement 测点名或表名
     * @param startTime 开始时间戳，毫秒
     * @param endTime 结束时间戳，毫秒
     * @param limit 返回条数限制
     * @return 查询结果
     */
    QueryResult queryDataByTimeRange(String database, String measurement, Long startTime, Long endTime, int limit);

    /**
     * 判断数据库是否存在。
     *
     * @param database 数据库名
     * @return 是否存在
     */
    boolean databaseExists(String database);

    /**
     * 创建数据库。
     *
     * @param database 数据库名
     * @return 是否创建成功
     */
    boolean createDatabase(String database);

    /**
     * 删除数据库。
     *
     * @param database 数据库名
     * @return 是否删除成功
     */
    boolean dropDatabase(String database);

    /**
     * 获取适配器名称。
     *
     * @return 适配器名称
     */
    String getAdapterName();
}
