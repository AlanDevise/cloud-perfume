package com.alandevise.tsdb.adapter.impl;

import com.alandevise.tsdb.adapter.TSDBAdapter;
import com.alandevise.tsdb.model.QueryResult;
import com.alandevise.tsdb.model.TimeSeriesData;
import lombok.extern.slf4j.Slf4j;
import org.apache.iotdb.isession.SessionDataSet;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.tsfile.enums.TSDataType;
import org.apache.tsfile.utils.Binary;
import org.apache.tsfile.write.record.Tablet;
import org.apache.tsfile.write.schema.MeasurementSchema;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * IoTDB 适配器实现。
 * 支持树模型和表模型，并在树模型批量写入时优先尝试 Tablet。
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2026-03-22
 */
@Slf4j
public class IoTDBAdapter implements TSDBAdapter {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean useTableModel;
    private Session session;

    /**
     * @param host IoTDB 主机地址
     * @param port IoTDB 端口
     * @param username IoTDB 用户名
     * @param password IoTDB 密码
     * @param useTableModel 是否启用表模型，false 为树模型
     */
    public IoTDBAdapter(String host, int port, String username, String password, boolean useTableModel) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.useTableModel = useTableModel;
    }

    /**
     * 初始化 IoTDB Session 连接。
     */
    @Override
    public void init() {
        try {
            session = new Session(host, port, username, password);
            session.open();
            log.info("IoTDB adapter initialized successfully, host: {}, port: {}, model: {}",
                    host, port, useTableModel ? "Table" : "Tree");
        } catch (IoTDBConnectionException e) {
            throw new RuntimeException("Failed to initialize IoTDB session", e);
        }
    }

    /**
     * 关闭 IoTDB Session 连接。
     */
    @Override
    public void close() {
        if (session == null) {
            return;
        }
        try {
            session.close();
            log.info("IoTDB session closed");
        } catch (IoTDBConnectionException e) {
            log.error("Failed to close IoTDB session", e);
        }
    }

    /**
     * 写入单条 IoTDB 数据。
     *
     * @param database 数据库名
     * @param data 时序数据对象
     * @return 是否写入成功
     */
    @Override
    public boolean writeData(String database, TimeSeriesData data) {
        try {
            if (useTableModel) {
                return writeDataTableModel(database, data);
            }
            return writeDataTreeModel(database, data);
        } catch (Exception e) {
            log.error("Failed to write data to IoTDB", e);
            return false;
        }
    }

    /**
     * 批量写入 IoTDB 数据。
     *
     * @param database 数据库名
     * @param dataList 时序数据列表
     * @return 是否全部写入成功
     */
    @Override
    public boolean batchWriteData(String database, List<TimeSeriesData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return true;
        }

        // 树模型下优先走 Tablet，提高同设备同 schema 批量写入性能。
        if (!useTableModel) {
            try {
                return batchWriteTreeModel(database, dataList);
            } catch (Exception e) {
                log.warn("Tablet batch write failed, fallback to single-record writes", e);
            }
        }

        boolean allSuccess = true;
        for (TimeSeriesData data : dataList) {
            if (!writeData(database, data)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    /**
     * 执行查询语句。
     *
     * @param database 数据库名
     * @param sql 查询 SQL
     * @return 查询结果
     */
    @Override
    public QueryResult queryData(String database, String sql) {
        return executeQuery(database, sql, true);
    }

    /**
     * 直接执行 IoTDB SQL。
     *
     * @param database 数据库名
     * @param sql 要执行的 SQL
     * @return 执行结果
     */
    @Override
    public QueryResult executeSql(String database, String sql) {
        String trimmedSql = sql == null ? "" : sql.trim();
        if (trimmedSql.isEmpty()) {
            return QueryResult.failure("SQL must not be empty");
        }

        String upperSql = trimmedSql.toUpperCase();
        if (upperSql.startsWith("SELECT") || upperSql.startsWith("SHOW") || upperSql.startsWith("LIST")) {
            return executeQuery(database, trimmedSql, false);
        }

        QueryResult result = new QueryResult();
        try {
            session.executeNonQueryStatement(trimmedSql);
            result.setSuccess(true);
            result.setMessage("SQL executed successfully");
        } catch (IoTDBConnectionException | StatementExecutionException e) {
            log.error("Failed to execute SQL on IoTDB", e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

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
    @Override
    public QueryResult queryDataByTimeRange(String database, String measurement, Long startTime, Long endTime, int limit) {
        String sql = String.format("SELECT * FROM %s.%s WHERE time >= %d AND time <= %d LIMIT %d",
                normalizeDatabase(database), measurement, startTime, endTime, limit);
        return queryData(database, sql);
    }

    /**
     * 判断数据库是否存在。
     *
     * @param database 数据库名
     * @return 是否存在
     */
    @Override
    public boolean databaseExists(String database) {
        String normalizedDatabase = normalizeDatabase(database);
        try {
            SessionDataSet dataSet = session.executeQueryStatement("SHOW DATABASES");
            while (dataSet.hasNext()) {
                org.apache.tsfile.read.common.RowRecord record = dataSet.next();
                if (!record.getFields().isEmpty()) {
                    Object value = record.getFields().get(0).getObjectValue(record.getFields().get(0).getDataType());
                    if (normalizedDatabase.equals(String.valueOf(value))) {
                        dataSet.closeOperationHandle();
                        return true;
                    }
                }
            }
            dataSet.closeOperationHandle();
        } catch (IoTDBConnectionException | StatementExecutionException e) {
            log.warn("Failed to check IoTDB database existence: {}", database, e);
        }
        return false;
    }

    /**
     * 创建数据库。
     *
     * @param database 数据库名
     * @return 是否创建成功
     */
    @Override
    public boolean createDatabase(String database) {
        try {
            session.createDatabase(normalizeDatabase(database));
            log.info("Database created: {}", database);
            return true;
        } catch (IoTDBConnectionException | StatementExecutionException e) {
            if (isDatabaseAlreadyExists(e)) {
                log.info("Database already exists: {}", database);
                return true;
            }
            log.error("Failed to create database: {}", database, e);
            return false;
        }
    }

    /**
     * 删除数据库。
     *
     * @param database 数据库名
     * @return 是否删除成功
     */
    @Override
    public boolean dropDatabase(String database) {
        try {
            session.deleteDatabase(normalizeDatabase(database));
            log.info("Database dropped: {}", database);
            return true;
        } catch (IoTDBConnectionException | StatementExecutionException e) {
            log.error("Failed to drop database: {}", database, e);
            return false;
        }
    }

    /**
     * 获取适配器名称。
     *
     * @return 适配器名称
     */
    @Override
    public String getAdapterName() {
        return "IoTDB";
    }

    /**
     * 执行并组装查询结果。
     *
     * @param database 数据库名
     * @param sql 原始 SQL
     * @param qualifySql 是否自动补全数据库前缀
     * @return 查询结果
     */
    private QueryResult executeQuery(String database, String sql, boolean qualifySql) {
        QueryResult result = new QueryResult();
        String normalizedDatabase = normalizeDatabase(database);
        try {
            String fullSql = sql;
            if (qualifySql) {
                if (!sql.toUpperCase().contains(" FROM ") && !sql.toUpperCase().startsWith("SELECT")) {
                    fullSql = "SELECT * FROM " + normalizedDatabase + "." + sql;
                } else if (!sql.toUpperCase().contains(" FROM ") && sql.toUpperCase().startsWith("SELECT")) {
                    fullSql = sql.replace("FROM", "FROM " + normalizedDatabase + ".");
                } else if (!sql.toUpperCase().contains(normalizedDatabase.toUpperCase())) {
                    fullSql = sql.replace("FROM", "FROM " + normalizedDatabase + ".");
                }
            }

            SessionDataSet dataSet = session.executeQueryStatement(fullSql);
            List<String> columnNames = dataSet.getColumnNames();
            result.setColumns(columnNames);

            List<Map<String, Object>> rows = new ArrayList<>();
            while (dataSet.hasNext()) {
                Map<String, Object> row = new HashMap<>();
                org.apache.tsfile.read.common.RowRecord record = dataSet.next();
                int fieldIndex = 0;
                for (String columnName : columnNames) {
                    if ("Time".equalsIgnoreCase(columnName)) {
                        row.put(columnName, record.getTimestamp());
                        continue;
                    }

                    org.apache.tsfile.read.common.Field field = fieldIndex < record.getFields().size()
                            ? record.getFields().get(fieldIndex)
                            : null;
                    Object value = field == null || field.getDataType() == null
                            ? null
                            : field.getObjectValue(field.getDataType());
                    if (value instanceof Binary) {
                        value = ((Binary) value).getStringValue(StandardCharsets.UTF_8);
                    }
                    row.put(columnName, value);
                    fieldIndex++;
                }
                rows.add(row);
            }

            result.setRows(rows);
            result.setRowCount(rows.size());
            result.setSuccess(true);
            result.setMessage("Query executed successfully");
            dataSet.closeOperationHandle();
        } catch (IoTDBConnectionException | StatementExecutionException e) {
            log.error("Failed to query data from IoTDB", e);
            result.setSuccess(false);
            result.setMessage("Query failed: " + e.getMessage());
        }
        return result;
    }

    /**
     * 使用树模型执行批量写入，满足条件时走 Tablet。
     *
     * @param database 数据库名
     * @param dataList 批量数据列表
     * @return 是否全部写入成功
     * @throws IoTDBConnectionException IoTDB 连接异常
     * @throws StatementExecutionException IoTDB 执行异常
     */
    private boolean batchWriteTreeModel(String database, List<TimeSeriesData> dataList)
            throws IoTDBConnectionException, StatementExecutionException {
        Map<TabletGroupKey, List<TimeSeriesData>> groupedData = groupByDeviceAndSchema(database, dataList);
        boolean allSuccess = true;

        for (Map.Entry<TabletGroupKey, List<TimeSeriesData>> entry : groupedData.entrySet()) {
            List<TimeSeriesData> groupedRows = entry.getValue();
            if (groupedRows.size() <= 1) {
                if (!writeDataTreeModel(database, groupedRows.get(0))) {
                    allSuccess = false;
                }
                continue;
            }

            Tablet tablet = buildTablet(entry.getKey(), groupedRows);
            session.insertTablet(tablet, true);
            log.info("IoTDB tablet batch written to {}, rows={}", entry.getKey().devicePath, tablet.rowSize);
        }

        return allSuccess;
    }

    /**
     * 使用树模型写入单条记录。
     *
     * @param database 数据库名
     * @param data 时序数据对象
     * @return 是否写入成功
     * @throws IoTDBConnectionException IoTDB 连接异常
     * @throws StatementExecutionException IoTDB 执行异常
     */
    private boolean writeDataTreeModel(String database, TimeSeriesData data)
            throws IoTDBConnectionException, StatementExecutionException {
        String devicePath = normalizeDatabase(database) + "." + data.getDevice();

        List<String> measurements = new ArrayList<>();
        List<TSDataType> dataTypes = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, Object> entry : collectTreeModelValues(data).entrySet()) {
            measurements.add(entry.getKey());
            dataTypes.add(getDataType(entry.getValue()));
            values.add(entry.getValue());
        }

        // 树模型下 fields 和 tags 都会被写成普通测点，便于与 TDEngine 保持结构一致。
        session.insertRecord(devicePath, data.getTimestamp(), measurements, dataTypes, values);
        log.debug("Tree model data written to {}", devicePath);
        return true;
    }

    /**
     * 使用表模型写入单条记录。
     *
     * @param database 数据库名
     * @param data 时序数据对象
     * @return 是否写入成功
     * @throws IoTDBConnectionException IoTDB 连接异常
     * @throws StatementExecutionException IoTDB 执行异常
     */
    private boolean writeDataTableModel(String database, TimeSeriesData data)
            throws IoTDBConnectionException, StatementExecutionException {
        String tableName = data.getMeasurement();
        String insertSql = (data.getTags() != null && !data.getTags().isEmpty())
                ? buildInsertSql(database, tableName, data)
                : buildSimpleInsertSql(database, tableName, data);
        session.executeNonQueryStatement(insertSql);
        log.debug("Table model data written to {}.{}", database, tableName);
        return true;
    }

    /**
     * 构造带 tags 的表模型插入 SQL。
     *
     * @param database 数据库名
     * @param tableName 表名
     * @param data 时序数据对象
     * @return 插入 SQL
     */
    private String buildInsertSql(String database, String tableName, TimeSeriesData data) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(database).append(".").append(tableName);

        if (data.getTags() != null && !data.getTags().isEmpty()) {
            sql.append(" USING ");
            List<String> tagConditions = new ArrayList<>();
            for (Map.Entry<String, String> tag : data.getTags().entrySet()) {
                tagConditions.add(tag.getKey() + "='" + tag.getValue() + "'");
            }
            sql.append(String.join(",", tagConditions));
        }

        sql.append(" TAGS ");
        if (data.getTags() != null && !data.getTags().isEmpty()) {
            List<String> tagValues = new ArrayList<>();
            for (String value : data.getTags().values()) {
                tagValues.add("'" + value + "'");
            }
            sql.append(String.join(",", tagValues));
        } else {
            sql.append("''");
        }

        sql.append(" (time");
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        if (data.getFields() != null) {
            for (Map.Entry<String, Object> entry : data.getFields().entrySet()) {
                columns.add(entry.getKey());
                values.add(entry.getValue() instanceof String
                        ? "'" + entry.getValue() + "'"
                        : String.valueOf(entry.getValue()));
            }
        }

        sql.append(",").append(String.join(",", columns)).append(") VALUES (");
        sql.append(data.getTimestamp()).append(",").append(String.join(",", values)).append(")");
        return sql.toString();
    }

    /**
     * 构造不带 tags 的表模型插入 SQL。
     *
     * @param database 数据库名
     * @param tableName 表名
     * @param data 时序数据对象
     * @return 插入 SQL
     */
    private String buildSimpleInsertSql(String database, String tableName, TimeSeriesData data) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(database).append(".").append(tableName);
        sql.append(" (time");

        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        if (data.getFields() != null) {
            for (Map.Entry<String, Object> entry : data.getFields().entrySet()) {
                columns.add(entry.getKey());
                values.add(entry.getValue() instanceof String
                        ? "'" + entry.getValue() + "'"
                        : String.valueOf(entry.getValue()));
            }
        }

        sql.append(",").append(String.join(",", columns)).append(") VALUES (");
        sql.append(data.getTimestamp()).append(",").append(String.join(",", values)).append(")");
        return sql.toString();
    }

    /**
     * 根据字段值推断 IoTDB 数据类型。
     *
     * @param value 字段值
     * @return 对应 IoTDB 数据类型
     */
    private TSDataType getDataType(Object value) {
        if (value == null) {
            return TSDataType.STRING;
        }
        if (value instanceof Boolean) {
            return TSDataType.BOOLEAN;
        }
        if (value instanceof Integer || value instanceof Long) {
            return TSDataType.INT64;
        }
        if (value instanceof Float || value instanceof Double) {
            return TSDataType.DOUBLE;
        }
        return TSDataType.STRING;
    }

    /**
     * 汇总树模型下需要写入的字段。
     *
     * @param data 时序数据对象
     * @return 最终写入字段集合，包含 fields 和 tags
     */
    private Map<String, Object> collectTreeModelValues(TimeSeriesData data) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (data.getFields() != null) {
            values.putAll(data.getFields());
        }
        if (data.getTags() != null) {
            for (Map.Entry<String, String> entry : data.getTags().entrySet()) {
                values.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return values;
    }

    /**
     * 按设备路径和 schema 分组，为 Tablet 批量写入做准备。
     *
     * @param database 数据库名
     * @param dataList 原始数据列表
     * @return 分组结果
     */
    private Map<TabletGroupKey, List<TimeSeriesData>> groupByDeviceAndSchema(String database, List<TimeSeriesData> dataList) {
        Map<TabletGroupKey, List<TimeSeriesData>> groupedData = new LinkedHashMap<>();
        for (TimeSeriesData data : dataList) {
            if (data == null || data.getDevice() == null || data.getMeasurement() == null) {
                continue;
            }
            TabletGroupKey key = buildTabletGroupKey(database, data);
            groupedData.computeIfAbsent(key, ignored -> new ArrayList<>()).add(data);
        }
        return groupedData;
    }

    /**
     * 构造 Tablet 分组 key。
     *
     * @param database 数据库名
     * @param data 时序数据对象
     * @return 分组 key
     */
    private TabletGroupKey buildTabletGroupKey(String database, TimeSeriesData data) {
        String devicePath = normalizeDatabase(database) + "." + data.getDevice();
        List<MeasurementSchema> schemas = buildSchemas(data);
        return new TabletGroupKey(devicePath, schemas);
    }

    /**
     * 根据写入字段构造 MeasurementSchema 列表。
     *
     * @param data 时序数据对象
     * @return schema 列表
     */
    private List<MeasurementSchema> buildSchemas(TimeSeriesData data) {
        List<MeasurementSchema> schemas = new ArrayList<>();
        Map<String, Object> writeValues = collectTreeModelValues(data);
        if (writeValues.isEmpty()) {
            return schemas;
        }

        List<Map.Entry<String, Object>> sortedEntries = new ArrayList<>(writeValues.entrySet());
        sortedEntries.sort(Comparator.comparing(Map.Entry::getKey));
        for (Map.Entry<String, Object> entry : sortedEntries) {
            schemas.add(new MeasurementSchema(entry.getKey(), getDataType(entry.getValue())));
        }
        return schemas;
    }

    /**
     * 根据分组数据构造 IoTDB Tablet。
     *
     * @param key 分组 key
     * @param groupedRows 同组数据列表
     * @return Tablet 对象
     */
    private Tablet buildTablet(TabletGroupKey key, List<TimeSeriesData> groupedRows) {
        Tablet tablet = new Tablet(key.devicePath, key.schemas, groupedRows.size());
        tablet.initBitMaps();

        for (int rowIndex = 0; rowIndex < groupedRows.size(); rowIndex++) {
            TimeSeriesData row = groupedRows.get(rowIndex);
            Map<String, Object> writeValues = collectTreeModelValues(row);
            tablet.addTimestamp(rowIndex, row.getTimestamp());
            for (MeasurementSchema schema : key.schemas) {
                Object value = writeValues.get(schema.getMeasurementId());
                if (value == null) {
                    tablet.bitMaps[key.schemaIndexByName.get(schema.getMeasurementId())].mark(rowIndex);
                    continue;
                }
                tablet.addValue(schema.getMeasurementId(), rowIndex, value);
            }
            tablet.rowSize++;
        }
        return tablet;
    }

    /**
     * 标准化数据库名。
     *
     * @param database 原始数据库名
     * @return 以 root. 开头的数据库名
     */
    private String normalizeDatabase(String database) {
        if (database == null || database.trim().isEmpty()) {
            return "root.cloud_platform";
        }
        return database.startsWith("root.") ? database : "root." + database;
    }

    /**
     * 判断异常是否表示数据库已存在。
     *
     * @param e 异常对象
     * @return 是否为“数据库已存在”异常
     */
    private boolean isDatabaseAlreadyExists(Exception e) {
        String message = e.getMessage();
        return message != null && message.toLowerCase().contains("has already been created as database");
    }

    private static final class TabletGroupKey {
        private final String devicePath;
        private final List<MeasurementSchema> schemas;
        private final Map<String, Integer> schemaIndexByName;

        /**
         * @param devicePath 设备路径
         * @param schemas 测点 schema 列表
         */
        private TabletGroupKey(String devicePath, List<MeasurementSchema> schemas) {
            this.devicePath = devicePath;
            this.schemas = schemas;
            this.schemaIndexByName = new HashMap<>();
            for (int i = 0; i < schemas.size(); i++) {
                schemaIndexByName.put(schemas.get(i).getMeasurementId(), i);
            }
        }

        /**
         * 比较两个分组 key 是否相同。
         *
         * @param obj 待比较对象
         * @return 是否相同
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TabletGroupKey)) {
                return false;
            }
            TabletGroupKey other = (TabletGroupKey) obj;
            return Objects.equals(devicePath, other.devicePath) && sameSchemas(other.schemas);
        }

        /**
         * 计算分组 key 哈希值。
         *
         * @return 哈希值
         */
        @Override
        public int hashCode() {
            int result = Objects.hash(devicePath);
            for (MeasurementSchema schema : schemas) {
                result = 31 * result + Objects.hash(schema.getMeasurementId(), schema.getType());
            }
            return result;
        }

        /**
         * 比较两组 schema 是否完全一致。
         *
         * @param otherSchemas 另一组 schema
         * @return 是否一致
         */
        private boolean sameSchemas(List<MeasurementSchema> otherSchemas) {
            if (schemas.size() != otherSchemas.size()) {
                return false;
            }
            for (int i = 0; i < schemas.size(); i++) {
                MeasurementSchema current = schemas.get(i);
                MeasurementSchema other = otherSchemas.get(i);
                if (!Objects.equals(current.getMeasurementId(), other.getMeasurementId())
                        || current.getType() != other.getType()) {
                    return false;
                }
            }
            return true;
        }
    }
}
