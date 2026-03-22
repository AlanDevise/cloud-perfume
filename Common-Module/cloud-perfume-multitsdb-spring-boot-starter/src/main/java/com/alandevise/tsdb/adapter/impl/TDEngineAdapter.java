package com.alandevise.tsdb.adapter.impl;

import com.alandevise.tsdb.adapter.TSDBAdapter;
import com.alandevise.tsdb.model.QueryResult;
import com.alandevise.tsdb.model.TimeSeriesData;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * TDEngine 适配器实现。
 * 写入时会自动确保数据库、表和缺失列存在。
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2026-03-22
 */
@Slf4j
public class TDEngineAdapter implements TSDBAdapter {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private Connection connection;

    /**
     * @param host TDEngine 主机地址
     * @param port TDEngine REST/JDBC 端口
     * @param username TDEngine 用户名
     * @param password TDEngine 密码
     */
    public TDEngineAdapter(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * 初始化 TDEngine 连接。
     */
    @Override
    public void init() {
        try {
            String url = String.format("jdbc:TAOS-RS://%s:%d/?user=%s&password=%s",
                    host, port, username, password);
            connection = DriverManager.getConnection(url);
            log.info("TDEngine adapter initialized successfully, host: {}, port: {}", host, port);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize TDEngine connection", e);
        }
    }

    /**
     * 关闭 TDEngine 连接。
     */
    @Override
    public void close() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
            log.info("TDEngine connection closed");
        } catch (SQLException e) {
            log.error("Failed to close TDEngine connection", e);
        }
    }

    /**
     * 写入单条 TDEngine 数据。
     *
     * @param database 数据库名
     * @param data 时序数据对象
     * @return 是否写入成功
     */
    @Override
    public boolean writeData(String database, TimeSeriesData data) {
        try {
            // TDEngine 写入前自动兜底建库、建表、补列。
            ensureTable(database, data);
            String insertSql = buildInsertSql(database, data);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(insertSql);
                log.debug("Data written to TDEngine: {}", insertSql);
                return true;
            }
        } catch (SQLException e) {
            log.error("Failed to write data to TDEngine", e);
            return false;
        }
    }

    /**
     * 批量写入 TDEngine 数据。
     *
     * @param database 数据库名
     * @param dataList 时序数据列表
     * @return 是否全部写入成功
     */
    @Override
    public boolean batchWriteData(String database, List<TimeSeriesData> dataList) {
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
     * 直接执行 TDEngine SQL。
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
        if (upperSql.startsWith("SELECT")) {
            return executeQuery(database, trimmedSql, true);
        }
        if (upperSql.startsWith("SHOW") || upperSql.startsWith("DESCRIBE")) {
            return executeQuery(database, trimmedSql, false);
        }

        QueryResult result = new QueryResult();
        try (Statement stmt = connection.createStatement()) {
            if (shouldUseDatabaseContext(database, trimmedSql)) {
                stmt.execute("USE " + database);
            }
            boolean hasResultSet = stmt.execute(trimmedSql);
            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    fillQueryResult(result, rs);
                }
            } else {
                Map<String, Object> row = new HashMap<>();
                row.put("affectedRows", stmt.getUpdateCount());
                result.getRows().add(row);
                result.setRowCount(1);
            }
            result.setSuccess(true);
            result.setMessage("SQL executed successfully");
        } catch (SQLException e) {
            log.error("Failed to execute SQL on TDEngine", e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    /**
     * 按时间范围查询数据。
     *
     * @param database 数据库名
     * @param measurement 表名
     * @param startTime 开始时间戳，毫秒
     * @param endTime 结束时间戳，毫秒
     * @param limit 返回条数限制
     * @return 查询结果
     */
    @Override
    public QueryResult queryDataByTimeRange(String database, String measurement, Long startTime, Long endTime, int limit) {
        String sql = String.format("SELECT * FROM %s WHERE ts >= %d AND ts <= %d LIMIT %d",
                qualifyTable(database, measurement), startTime, endTime, limit);
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
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
            while (rs.next()) {
                if (database.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        } catch (SQLException e) {
            log.warn("Failed to check TDEngine database existence: {}", database, e);
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
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + database);
            log.info("Database created: {}", database);
            return true;
        } catch (SQLException e) {
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
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP DATABASE IF EXISTS " + database);
            log.info("Database dropped: {}", database);
            return true;
        } catch (SQLException e) {
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
        return "TDEngine";
    }

    /**
     * 执行查询并组装结果。
     *
     * @param database 数据库名
     * @param sql 原始 SQL
     * @param qualifySql 是否自动补全表名前缀
     * @return 查询结果
     */
    private QueryResult executeQuery(String database, String sql, boolean qualifySql) {
        QueryResult result = new QueryResult();
        try {
            String fullSql = qualifySql ? qualifyQuerySql(database, sql) : sql;
            fullSql = quoteKnownColumns(database, fullSql);
            try (Statement stmt = connection.createStatement()) {
                if (shouldUseDatabaseContext(database, fullSql)) {
                    stmt.execute("USE " + database);
                }
                try (ResultSet rs = stmt.executeQuery(fullSql)) {
                    fillQueryResult(result, rs);
                    result.setSuccess(true);
                    result.setMessage("Query executed successfully");
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query data from TDEngine", e);
            result.setSuccess(false);
            result.setMessage("Query failed: " + e.getMessage());
        }
        return result;
    }

    /**
     * 将 ResultSet 转成统一 QueryResult。
     *
     * @param result 结果对象
     * @param rs JDBC 结果集
     * @throws SQLException SQL 异常
     */
    private void fillQueryResult(QueryResult result, ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<String> columnNames = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }
        result.setColumns(columnNames);

        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(columnNames.get(i - 1), rs.getObject(i));
            }
            rows.add(row);
        }
        result.setRows(rows);
        result.setRowCount(rows.size());
    }

    /**
     * 构造插入 SQL。
     *
     * @param database 数据库名
     * @param data 时序数据对象
     * @return 插入 SQL
     */
    private String buildInsertSql(String database, TimeSeriesData data) {
        Map<String, Object> valuesByColumn = collectColumnValues(data);
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(qualifyTable(database, data.getMeasurement()));
        sql.append(" (").append(quoteIdentifier("ts"));

        List<String> values = new ArrayList<>();
        values.add(String.valueOf(data.getTimestamp()));
        for (Map.Entry<String, Object> entry : valuesByColumn.entrySet()) {
            sql.append(",").append(quoteIdentifier(entry.getKey()));
            values.add(toSqlLiteral(entry.getValue()));
        }

        sql.append(") VALUES (").append(String.join(",", values)).append(")");
        return sql.toString();
    }

    /**
     * 确保目标数据库、表和列存在。
     *
     * @param database 数据库名
     * @param data 时序数据对象
     * @throws SQLException SQL 异常
     */
    private void ensureTable(String database, TimeSeriesData data) throws SQLException {
        createDatabase(database);
        String tableName = data.getMeasurement();
        Map<String, String> columnTypes = describeTable(database, tableName);
        if (columnTypes.isEmpty()) {
            createTable(database, tableName, data);
            return;
        }

        Map<String, Object> valuesByColumn = collectColumnValues(data);
        for (Map.Entry<String, Object> entry : valuesByColumn.entrySet()) {
            if (!columnTypes.containsKey(entry.getKey())) {
                addColumn(database, tableName, entry.getKey(), inferColumnType(entry.getValue()));
            }
        }
    }

    /**
     * 首次写入时创建表结构。
     *
     * @param database 数据库名
     * @param tableName 表名
     * @param data 时序数据对象
     * @throws SQLException SQL 异常
     */
    private void createTable(String database, String tableName, TimeSeriesData data) throws SQLException {
        List<String> definitions = new ArrayList<>();
        definitions.add(quoteIdentifier("ts") + " TIMESTAMP");
        Map<String, Object> valuesByColumn = collectColumnValues(data);
        for (Map.Entry<String, Object> entry : valuesByColumn.entrySet()) {
            definitions.add(quoteIdentifier(entry.getKey()) + " " + inferColumnType(entry.getValue()));
        }

        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s)",
                qualifyTable(database, tableName), String.join(", ", definitions));
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * 给已存在的表补充缺失列。
     *
     * @param database 数据库名
     * @param tableName 表名
     * @param columnName 列名
     * @param columnType 列类型
     * @throws SQLException SQL 异常
     */
    private void addColumn(String database, String tableName, String columnName, String columnType) throws SQLException {
        String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s",
                qualifyTable(database, tableName), quoteIdentifier(columnName), columnType);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * 查询表结构信息。
     *
     * @param database 数据库名
     * @param tableName 表名
     * @return 列名到列类型的映射
     * @throws SQLException SQL 异常
     */
    private Map<String, String> describeTable(String database, String tableName) throws SQLException {
        Map<String, String> columnTypes = new HashMap<>();
        String sql = "DESCRIBE " + qualifyTable(database, tableName);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                columnTypes.put(rs.getString("field"), rs.getString("type"));
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Table does not exist")) {
                return columnTypes;
            }
            throw e;
        }
        return columnTypes;
    }

    /**
     * 汇总需要写入的列。
     *
     * @param data 时序数据对象
     * @return 写入列集合，包含 device、fields、tags
     */
    private Map<String, Object> collectColumnValues(TimeSeriesData data) {
        Map<String, Object> valuesByColumn = new LinkedHashMap<>();
        if (data.getDevice() != null && !data.getDevice().isEmpty()) {
            valuesByColumn.put("device", data.getDevice());
        }
        if (data.getFields() != null) {
            valuesByColumn.putAll(data.getFields());
        }
        if (data.getTags() != null) {
            valuesByColumn.putAll(data.getTags());
        }
        return valuesByColumn;
    }

    /**
     * 拼接全限定表名。
     *
     * @param database 数据库名
     * @param tableName 表名
     * @return 全限定表名
     */
    private String qualifyTable(String database, String tableName) {
        return database + "." + quoteIdentifier(tableName);
    }

    /**
     * 自动补全查询 SQL 中的表名前缀。
     *
     * @param database 数据库名
     * @param sql 原始 SQL
     * @return 补全后的 SQL
     */
    private String qualifyQuerySql(String database, String sql) {
        String trimmedSql = sql.trim();
        String upperSql = trimmedSql.toUpperCase();
        if (!upperSql.startsWith("SELECT")) {
            return "SELECT * FROM " + qualifyTable(database, trimmedSql);
        }

        int fromIndex = upperSql.indexOf(" FROM ");
        if (fromIndex < 0) {
            return trimmedSql;
        }

        int tableStart = fromIndex + 6;
        int tableEnd = findTableTokenEnd(trimmedSql, tableStart);
        String tableToken = trimmedSql.substring(tableStart, tableEnd).trim();
        if (tableToken.isEmpty() || tableToken.contains(".")) {
            return trimmedSql;
        }

        String qualifiedTable = qualifyTable(database, unquoteIdentifier(tableToken));
        return trimmedSql.substring(0, tableStart) + qualifiedTable + trimmedSql.substring(tableEnd);
    }

    /**
     * 对已知列名自动补反引号，避免关键字冲突。
     *
     * @param database 数据库名
     * @param sql 原始 SQL
     * @return 处理后的 SQL
     */
    private String quoteKnownColumns(String database, String sql) {
        String upperSql = sql.toUpperCase();
        int fromIndex = upperSql.indexOf(" FROM ");
        if (fromIndex < 0) {
            return sql;
        }

        int tableStart = fromIndex + 6;
        int tableEnd = findTableTokenEnd(sql, tableStart);
        String tableToken = sql.substring(tableStart, tableEnd).trim();
        if (tableToken.isEmpty()) {
            return sql;
        }

        String tableName = extractTableName(tableToken);
        if (tableName == null || tableName.isEmpty()) {
            return sql;
        }

        Map<String, String> columnTypes;
        try {
            columnTypes = describeTable(database, tableName);
        } catch (SQLException e) {
            return sql;
        }

        String normalizedSql = sql;
        for (String columnName : columnTypes.keySet()) {
            if ("ts".equalsIgnoreCase(columnName)) {
                continue;
            }
            normalizedSql = replaceBareIdentifier(normalizedSql, columnName, quoteIdentifier(columnName));
        }
        return normalizedSql;
    }

    /**
     * 从 SQL 中提取表名。
     *
     * @param tableToken 表名片段
     * @return 去掉引号后的表名
     */
    private String extractTableName(String tableToken) {
        String trimmed = tableToken.trim();
        int dotIndex = trimmed.lastIndexOf('.');
        String rawTableName = dotIndex >= 0 ? trimmed.substring(dotIndex + 1) : trimmed;
        return unquoteIdentifier(rawTableName);
    }

    /**
     * 替换裸列名为带反引号的列名。
     *
     * @param sql 原始 SQL
     * @param identifier 原始列名
     * @param quotedIdentifier 带反引号的列名
     * @return 替换后的 SQL
     */
    private String replaceBareIdentifier(String sql, String identifier, String quotedIdentifier) {
        String pattern = "(?i)(?<![`\\w.])" + Pattern.quote(identifier) + "(?![`\\w])";
        return sql.replaceAll(pattern, quotedIdentifier);
    }

    /**
     * 计算表名 token 结束位置。
     *
     * @param sql 原始 SQL
     * @param startIndex 表名起始下标
     * @return 结束下标
     */
    private int findTableTokenEnd(String sql, int startIndex) {
        int endIndex = startIndex;
        while (endIndex < sql.length() && !Character.isWhitespace(sql.charAt(endIndex))) {
            endIndex++;
        }
        return endIndex;
    }

    /**
     * 去掉标识符外层反引号。
     *
     * @param identifier 原始标识符
     * @return 去引号后的标识符
     */
    private String unquoteIdentifier(String identifier) {
        String trimmed = identifier.trim();
        if (trimmed.startsWith("`") && trimmed.endsWith("`") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1).replace("``", "`");
        }
        return trimmed;
    }

    /**
     * 给标识符补上反引号。
     *
     * @param identifier 原始标识符
     * @return 带反引号的标识符
     */
    private String quoteIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    /**
     * 将 Java 值转换为 SQL 字面量。
     *
     * @param value 原始值
     * @return SQL 字面量
     */
    private String toSqlLiteral(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "true" : "false";
        }
        if (value instanceof Number) {
            return String.valueOf(value);
        }
        return "'" + String.valueOf(value).replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    /**
     * 判断当前 SQL 是否需要先切换数据库上下文。
     *
     * @param database 数据库名
     * @param sql 要执行的 SQL
     * @return 是否需要执行 USE database
     */
    private boolean shouldUseDatabaseContext(String database, String sql) {
        if (database == null || database.trim().isEmpty()) {
            return false;
        }

        String normalizedSql = sql == null ? "" : sql.trim().toUpperCase();
        return !(normalizedSql.startsWith("CREATE DATABASE")
                || normalizedSql.startsWith("DROP DATABASE")
                || normalizedSql.equals("SHOW DATABASES"));
    }

    /**
     * 根据字段值推断 TDEngine 列类型。
     *
     * @param value 字段值
     * @return 列类型
     */
    private String inferColumnType(Object value) {
        if (value instanceof Boolean) {
            return "BOOL";
        }
        if (value instanceof Integer) {
            return "INT";
        }
        if (value instanceof Long) {
            return "BIGINT";
        }
        if (value instanceof Float) {
            return "FLOAT";
        }
        if (value instanceof Double) {
            return "DOUBLE";
        }
        return "BINARY(255)";
    }
}
