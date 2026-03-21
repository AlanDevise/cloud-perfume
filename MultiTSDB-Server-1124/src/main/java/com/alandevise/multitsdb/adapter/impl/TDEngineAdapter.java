package com.alandevise.multitsdb.adapter.impl;

import com.alandevise.multitsdb.adapter.TSDBAdapter;
import com.alandevise.multitsdb.model.QueryResult;
import com.alandevise.multitsdb.model.TimeSeriesData;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class TDEngineAdapter implements TSDBAdapter {
    
    private String host;
    private int port;
    private String username;
    private String password;
    private Connection connection;
    
    public TDEngineAdapter(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }
    
    @Override
    public void init() {
        try {
            String url = String.format("jdbc:TAOS-RS://%s:%d/?user=%s&password=%s",
                    host, port, username, password);
            connection = DriverManager.getConnection(url);
            log.info("TDEngine adapter initialized successfully, host: {}, port: {}", host, port);
        } catch (SQLException e) {
            log.error("Failed to initialize TDEngine connection", e);
            throw new RuntimeException("Failed to initialize TDEngine connection", e);
        }
    }
    
    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                log.info("TDEngine connection closed");
            } catch (SQLException e) {
                log.error("Failed to close TDEngine connection", e);
            }
        }
    }
    
    @Override
    public boolean writeData(String database, TimeSeriesData data) {
        try {
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
    
    @Override
    public boolean batchWriteData(String database, List<TimeSeriesData> dataList) {
        try {
            boolean allSuccess = true;
            for (TimeSeriesData data : dataList) {
                if (!writeData(database, data)) {
                    allSuccess = false;
                }
            }
            return allSuccess;
        } catch (Exception e) {
            log.error("Failed to batch write data to TDEngine", e);
            return false;
        }
    }
    
    @Override
    public QueryResult queryData(String database, String sql) {
        return executeQuery(database, sql, true);
    }

    @Override
    public QueryResult executeSql(String database, String sql) {
        String trimmedSql = sql == null ? "" : sql.trim();
        QueryResult result = new QueryResult();
        result.setColumns(new ArrayList<>());
        result.setRows(new ArrayList<>());

        if (trimmedSql.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("SQL must not be empty");
            return result;
        }

        String upperSql = trimmedSql.toUpperCase();
        if (upperSql.startsWith("SELECT")) {
            return executeQuery(database, trimmedSql, true);
        }
        if (upperSql.startsWith("SHOW") || upperSql.startsWith("DESCRIBE")) {
            return executeQuery(database, trimmedSql, false);
        }

        try (Statement stmt = connection.createStatement()) {
            if (database != null && !database.trim().isEmpty()) {
                stmt.execute("USE " + database);
            }
            boolean hasResultSet = stmt.execute(trimmedSql);
            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
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
            } else {
                Map<String, Object> row = new HashMap<>();
                row.put("affectedRows", stmt.getUpdateCount());
                result.setColumns(new ArrayList<>());
                result.setRows(new ArrayList<>());
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

    private QueryResult executeQuery(String database, String sql, boolean qualifySql) {
        QueryResult result = new QueryResult();
        result.setColumns(new ArrayList<>());
        result.setRows(new ArrayList<>());

        try {
            String fullSql = qualifySql ? qualifyQuerySql(database, sql) : sql;
            fullSql = quoteKnownColumns(database, fullSql);

            try (Statement stmt = connection.createStatement()) {
                if (database != null && !database.trim().isEmpty()) {
                    stmt.execute("USE " + database);
                }
                try (ResultSet rs = stmt.executeQuery(fullSql)) {

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
    
    @Override
    public QueryResult queryDataByTimeRange(String database, String measurement, 
                                            Long startTime, Long endTime, 
                                            int limit) {
        String sql = String.format("SELECT * FROM %s WHERE ts >= %d AND ts <= %d LIMIT %d",
                qualifyTable(database, measurement), startTime, endTime, limit);
        return queryData(database, sql);
    }

    @Override
    public boolean databaseExists(String database) {
        String sql = "SHOW DATABASES";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
    
    @Override
    public boolean createDatabase(String database) {
        try {
            String sql = "CREATE DATABASE IF NOT EXISTS " + database;
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
                log.info("Database created: {}", database);
                return true;
            }
        } catch (SQLException e) {
            log.error("Failed to create database: {}", database, e);
            return false;
        }
    }
    
    @Override
    public boolean dropDatabase(String database) {
        try {
            String sql = "DROP DATABASE IF EXISTS " + database;
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
                log.info("Database dropped: {}", database);
                return true;
            }
        } catch (SQLException e) {
            log.error("Failed to drop database: {}", database, e);
            return false;
        }
    }
    
    @Override
    public String getAdapterName() {
        return "TDEngine";
    }
    
    private String buildInsertSql(String database, TimeSeriesData data) {
        Map<String, Object> valuesByColumn = collectColumnValues(data);
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(qualifyTable(database, data.getMeasurement()));
        sql.append(" (");
        sql.append(quoteIdentifier("ts"));
        
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(data.getTimestamp()));

        for (Map.Entry<String, Object> entry : valuesByColumn.entrySet()) {
            sql.append(",").append(quoteIdentifier(entry.getKey()));
            values.add(toSqlLiteral(entry.getValue()));
        }

        sql.append(") VALUES (").append(String.join(",", values)).append(")");
        return sql.toString();
    }

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

    private void addColumn(String database, String tableName, String columnName, String columnType) throws SQLException {
        String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s",
                qualifyTable(database, tableName), quoteIdentifier(columnName), columnType);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

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

    private Map<String, Object> collectColumnValues(TimeSeriesData data) {
        Map<String, Object> valuesByColumn = new LinkedHashMap<>();
        if (data.getDevice() != null && !data.getDevice().isEmpty()) {
            valuesByColumn.put("device", data.getDevice());
        }
        if (data.getFields() != null) {
            valuesByColumn.putAll(data.getFields());
        }
        if (data.getTags() != null) {
            for (Map.Entry<String, String> entry : data.getTags().entrySet()) {
                valuesByColumn.put(entry.getKey(), entry.getValue());
            }
        }
        return valuesByColumn;
    }

    private String qualifyTable(String database, String tableName) {
        return database + "." + quoteIdentifier(tableName);
    }

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
        if (columnTypes.isEmpty()) {
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

    private String extractTableName(String tableToken) {
        String trimmed = tableToken.trim();
        int dotIndex = trimmed.lastIndexOf('.');
        String rawTableName = dotIndex >= 0 ? trimmed.substring(dotIndex + 1) : trimmed;
        return unquoteIdentifier(rawTableName);
    }

    private String replaceBareIdentifier(String sql, String identifier, String quotedIdentifier) {
        String pattern = "(?i)(?<![`\\w.])" + Pattern.quote(identifier) + "(?![`\\w])";
        return sql.replaceAll(pattern, quotedIdentifier);
    }

    private int findTableTokenEnd(String sql, int startIndex) {
        int endIndex = startIndex;
        while (endIndex < sql.length() && !Character.isWhitespace(sql.charAt(endIndex))) {
            endIndex++;
        }
        return endIndex;
    }

    private String unquoteIdentifier(String identifier) {
        String trimmed = identifier.trim();
        if (trimmed.startsWith("`") && trimmed.endsWith("`") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1).replace("``", "`");
        }
        return trimmed;
    }

    private String quoteIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

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
