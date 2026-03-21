package com.alandevise.multitsdb.service;

import com.alandevise.multitsdb.adapter.TSDBAdapter;
import com.alandevise.multitsdb.dto.QueryByTimeRangeRequest;
import com.alandevise.multitsdb.dto.QueryDataRequest;
import com.alandevise.multitsdb.dto.WriteDataRequest;
import com.alandevise.multitsdb.model.QueryResult;
import com.alandevise.multitsdb.model.TimeSeriesData;
import com.alandevise.multitsdb.constant.TSDBType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TSDBService {
    
    @Autowired
    private Map<String, TSDBAdapter> tsdbAdapters;
    
    public boolean writeData(WriteDataRequest request) {
        TSDBAdapter adapter = getAdapter(request.getTsdbType());
        if (adapter == null) {
            log.error("Invalid TSDB type: {}", request.getTsdbType());
            return false;
        }
        
        String database = request.getDatabase();
        if (database == null || database.isEmpty()) {
            database = "cloud_platform";
        }
        
        boolean allSuccess = true;
        for (WriteDataRequest.TimeSeriesDataDTO dto : request.getDataList()) {
            TimeSeriesData data = convertToTimeSeriesData(dto);
            if (!adapter.writeData(database, data)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    public QueryResult queryData(QueryDataRequest request) {
        TSDBAdapter adapter = getAdapter(request.getTsdbType());
        if (adapter == null) {
            QueryResult result = new QueryResult();
            result.setSuccess(false);
            result.setMessage("Invalid TSDB type: " + request.getTsdbType());
            return result;
        }
        
        String database = request.getDatabase();
        if (database == null || database.isEmpty()) {
            database = "cloud_platform";
        }
        
        return adapter.queryData(database, request.getSql());
    }

    public QueryResult executeSql(QueryDataRequest request) {
        TSDBAdapter adapter = getAdapter(request.getTsdbType());
        if (adapter == null) {
            QueryResult result = new QueryResult();
            result.setSuccess(false);
            result.setMessage("Invalid TSDB type: " + request.getTsdbType());
            return result;
        }

        String database = request.getDatabase();
        if (database == null || database.isEmpty()) {
            database = "cloud_platform";
        }

        return adapter.executeSql(database, request.getSql());
    }
    
    public QueryResult queryDataByTimeRange(QueryByTimeRangeRequest request) {
        TSDBAdapter adapter = getAdapter(request.getTsdbType());
        if (adapter == null) {
            QueryResult result = new QueryResult();
            result.setSuccess(false);
            result.setMessage("Invalid TSDB type: " + request.getTsdbType());
            return result;
        }
        
        String database = request.getDatabase();
        if (database == null || database.isEmpty()) {
            database = "cloud_platform";
        }
        
        int limit = request.getLimit() != null ? request.getLimit() : 100;
        
        return adapter.queryDataByTimeRange(database, request.getMeasurement(), 
                request.getStartTime(), request.getEndTime(), limit);
    }
    
    public boolean createDatabase(String tsdbType, String database) {
        TSDBAdapter adapter = getAdapter(tsdbType);
        if (adapter == null) {
            log.error("Invalid TSDB type: {}", tsdbType);
            return false;
        }
        
        return adapter.createDatabase(database);
    }
    
    public boolean dropDatabase(String tsdbType, String database) {
        TSDBAdapter adapter = getAdapter(tsdbType);
        if (adapter == null) {
            log.error("Invalid TSDB type: {}", tsdbType);
            return false;
        }
        
        return adapter.dropDatabase(database);
    }
    
    public Map<String, TSDBAdapter> getAllAdapters() {
        return tsdbAdapters;
    }
    
    private TSDBAdapter getAdapter(String tsdbType) {
        if (tsdbType == null || tsdbType.isEmpty()) {
            return tsdbAdapters.values().stream().findFirst().orElse(null);
        }
        return tsdbAdapters.get(tsdbType.toLowerCase());
    }
    
    private TimeSeriesData convertToTimeSeriesData(WriteDataRequest.TimeSeriesDataDTO dto) {
        TimeSeriesData data = new TimeSeriesData();
        data.setDevice(dto.getDevice());
        data.setMeasurement(dto.getMeasurement());
        data.setTimestamp(dto.getTimestamp());
        data.setFields(dto.getFields());
        data.setTags(dto.getTags());
        return data;
    }
}
