package com.alandevise.multitsdb.adapter;

import com.alandevise.multitsdb.model.QueryResult;
import com.alandevise.multitsdb.model.TimeSeriesData;

import java.util.List;

public interface TSDBAdapter {
    
    void init();
    
    void close();
    
    boolean writeData(String database, TimeSeriesData data);
    
    boolean batchWriteData(String database, List<TimeSeriesData> dataList);
    
    QueryResult queryData(String database, String sql);

    QueryResult executeSql(String database, String sql);
    
    QueryResult queryDataByTimeRange(String database, String measurement, 
                                      Long startTime, Long endTime, 
                                      int limit);

    boolean databaseExists(String database);
    
    boolean createDatabase(String database);
    
    boolean dropDatabase(String database);
    
    String getAdapterName();
}
