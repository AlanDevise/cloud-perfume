package com.alandevise.multitsdb.adapter.impl;

import com.alandevise.multitsdb.adapter.TSDBAdapter;
import com.alandevise.multitsdb.model.QueryResult;
import com.alandevise.multitsdb.model.TimeSeriesData;
import lombok.extern.slf4j.Slf4j;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.isession.SessionDataSet;
import org.apache.tsfile.enums.TSDataType;
import org.apache.tsfile.utils.Binary;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class IoTDBAdapter implements TSDBAdapter {
    
    private String host;
    private int port;
    private String username;
    private String password;
    private Session session;
    private boolean useTableModel;
    
    public IoTDBAdapter(String host, int port, String username, String password, boolean useTableModel) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.useTableModel = useTableModel;
    }
    
    @Override
    public void init() {
        try {
            session = new Session(host, port, username, password);
            session.open();
            log.info("IoTDB adapter initialized successfully, host: {}, port: {}, model: {}", 
                    host, port, useTableModel ? "Table" : "Tree");
        } catch (IoTDBConnectionException e) {
            log.error("Failed to initialize IoTDB session", e);
            throw new RuntimeException("Failed to initialize IoTDB session", e);
        }
    }
    
    @Override
    public void close() {
        if (session != null) {
            try {
                session.close();
                log.info("IoTDB session closed");
            } catch (IoTDBConnectionException e) {
                log.error("Failed to close IoTDB session", e);
            }
        }
    }
    
    @Override
    public boolean writeData(String database, TimeSeriesData data) {
        try {
            if (useTableModel) {
                return writeDataTableModel(database, data);
            } else {
                return writeDataTreeModel(database, data);
            }
        } catch (Exception e) {
            log.error("Failed to write data to IoTDB", e);
            return false;
        }
    }
    
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
    
    @Override
    public QueryResult queryData(String database, String sql) {
        return executeQuery(database, sql, true);
    }

    @Override
    public QueryResult executeSql(String database, String sql) {
        String trimmedSql = sql == null ? "" : sql.trim();
        if (trimmedSql.isEmpty()) {
            QueryResult result = new QueryResult();
            result.setSuccess(false);
            result.setMessage("SQL must not be empty");
            result.setColumns(new ArrayList<>());
            result.setRows(new ArrayList<>());
            return result;
        }

        String upperSql = trimmedSql.toUpperCase();
        if (upperSql.startsWith("SELECT") || upperSql.startsWith("SHOW") || upperSql.startsWith("LIST")) {
            return executeQuery(database, trimmedSql, false);
        }

        QueryResult result = new QueryResult();
        result.setColumns(new ArrayList<>());
        result.setRows(new ArrayList<>());
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

    private QueryResult executeQuery(String database, String sql, boolean qualifySql) {
        QueryResult result = new QueryResult();
        result.setColumns(new ArrayList<>());
        result.setRows(new ArrayList<>());
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
                for (int i = 0; i < columnNames.size(); i++) {
                    String columnName = columnNames.get(i);
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
    
    @Override
    public QueryResult queryDataByTimeRange(String database, String measurement, 
                                            Long startTime, Long endTime, 
                                            int limit) {
        String sql = String.format("SELECT * FROM %s.%s WHERE time >= %d AND time <= %d LIMIT %d",
                normalizeDatabase(database), measurement, startTime, endTime, limit);
        return queryData(database, sql);
    }

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
            return false;
        } catch (IoTDBConnectionException | StatementExecutionException e) {
            log.warn("Failed to check IoTDB database existence: {}", database, e);
            return false;
        }
    }
    
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
    
    @Override
    public String getAdapterName() {
        return "IoTDB";
    }
    
    private boolean writeDataTreeModel(String database, TimeSeriesData data) {
        try {
            String devicePath = normalizeDatabase(database) + "." + data.getDevice();
            
            List<String> measurements = new ArrayList<>();
            List<TSDataType> dataTypes = new ArrayList<>();
            List<Object> values = new ArrayList<>();
            
            if (data.getFields() != null) {
                for (Map.Entry<String, Object> entry : data.getFields().entrySet()) {
                    measurements.add(entry.getKey());
                    Object value = entry.getValue();
                    dataTypes.add(getDataType(value));
                    values.add(value);
                }
            }
            
            session.insertRecord(devicePath, data.getTimestamp(), 
                    measurements, dataTypes, values);
            
            log.debug("Tree model data written to {}", devicePath);
            return true;
        } catch (IoTDBConnectionException | StatementExecutionException e) {
            log.error("Failed to write tree model data", e);
            return false;
        }
    }
    
    private boolean writeDataTableModel(String database, TimeSeriesData data) {
        try {
            String tableName = data.getMeasurement();
            
            if (data.getTags() != null && !data.getTags().isEmpty()) {
                String insertSql = buildInsertSql(database, tableName, data);
                session.executeNonQueryStatement(insertSql);
            } else {
                String insertSql = buildSimpleInsertSql(database, tableName, data);
                session.executeNonQueryStatement(insertSql);
            }
            
            log.debug("Table model data written to {}.{}", database, tableName);
            return true;
        } catch (Exception e) {
            log.error("Failed to write table model data", e);
            return false;
        }
    }
    
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
                Object value = entry.getValue();
                if (value instanceof String) {
                    values.add("'" + value + "'");
                } else {
                    values.add(String.valueOf(value));
                }
            }
        }
        
        sql.append(",").append(String.join(",", columns)).append(") VALUES (");
        sql.append(data.getTimestamp()).append(",");
        sql.append(String.join(",", values)).append(")");
        
        return sql.toString();
    }
    
    private String buildSimpleInsertSql(String database, String tableName, TimeSeriesData data) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(database).append(".").append(tableName);
        sql.append(" (time");
        
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        
        if (data.getFields() != null) {
            for (Map.Entry<String, Object> entry : data.getFields().entrySet()) {
                columns.add(entry.getKey());
                Object value = entry.getValue();
                if (value instanceof String) {
                    values.add("'" + value + "'");
                } else {
                    values.add(String.valueOf(value));
                }
            }
        }
        
        sql.append(",").append(String.join(",", columns)).append(") VALUES (");
        sql.append(data.getTimestamp()).append(",");
        sql.append(String.join(",", values)).append(")");
        
        return sql.toString();
    }
    
    private TSDataType getDataType(Object value) {
        if (value == null) {
            return TSDataType.STRING;
        } else if (value instanceof Boolean) {
            return TSDataType.BOOLEAN;
        } else if (value instanceof Integer || value instanceof Long) {
            return TSDataType.INT64;
        } else if (value instanceof Float || value instanceof Double) {
            return TSDataType.DOUBLE;
        } else {
            return TSDataType.STRING;
        }
    }

    private String normalizeDatabase(String database) {
        if (database == null || database.trim().isEmpty()) {
            return "root.cloud_platform";
        }
        return database.startsWith("root.") ? database : "root." + database;
    }

    private boolean isDatabaseAlreadyExists(Exception e) {
        String message = e.getMessage();
        return message != null && message.toLowerCase().contains("has already been created as database");
    }
}
