package com.alandevise.multitsdb.service;

import com.alandevise.multitsdb.dto.QueryByTimeRangeRequest;
import com.alandevise.multitsdb.dto.QueryDataRequest;
import com.alandevise.multitsdb.dto.WriteDataRequest;
import com.alandevise.tsdb.core.MultiTSDBTemplate;
import com.alandevise.tsdb.model.QueryResult;
import com.alandevise.tsdb.model.TimeSeriesData;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseTSDBService {

    private final MultiTSDBTemplate multiTSDBTemplate;

    protected BaseTSDBService(MultiTSDBTemplate multiTSDBTemplate) {
        this.multiTSDBTemplate = multiTSDBTemplate;
    }

    protected abstract String getTsdbType();

    public boolean writeData(WriteDataRequest request) {
        return multiTSDBTemplate.batchWrite(getTsdbType(), request.getDatabase(), convertToTimeSeriesData(request.getDataList()));
    }

    public QueryResult queryData(QueryDataRequest request) {
        return multiTSDBTemplate.query(getTsdbType(), request.getDatabase(), request.getSql());
    }

    public QueryResult executeSql(QueryDataRequest request) {
        return multiTSDBTemplate.executeSql(getTsdbType(), request.getDatabase(), request.getSql());
    }

    public QueryResult queryDataByTimeRange(QueryByTimeRangeRequest request) {
        return multiTSDBTemplate.queryByTimeRange(
                getTsdbType(),
                request.getDatabase(),
                request.getMeasurement(),
                request.getStartTime(),
                request.getEndTime(),
                request.getLimit()
        );
    }

    private List<TimeSeriesData> convertToTimeSeriesData(List<WriteDataRequest.TimeSeriesDataDTO> dataList) {
        List<TimeSeriesData> results = new ArrayList<>();
        if (dataList == null) {
            return results;
        }

        for (WriteDataRequest.TimeSeriesDataDTO dto : dataList) {
            TimeSeriesData data = new TimeSeriesData();
            data.setDevice(dto.getDevice());
            data.setMeasurement(dto.getMeasurement());
            data.setTimestamp(dto.getTimestamp());
            data.setFields(dto.getFields());
            data.setTags(dto.getTags());
            results.add(data);
        }
        return results;
    }
}
