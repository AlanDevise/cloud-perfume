package com.alandevise.multitsdb.controller;

import com.alandevise.multitsdb.dto.QueryByTimeRangeRequest;
import com.alandevise.multitsdb.dto.QueryDataRequest;
import com.alandevise.multitsdb.dto.Result;
import com.alandevise.multitsdb.dto.WriteDataRequest;
import com.alandevise.multitsdb.model.QueryResult;
import com.alandevise.multitsdb.service.TSDBService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/example")
@Api(tags = "时序数据库使用示例")
public class ExampleController {
    
    @Autowired
    private TSDBService tsdbService;
    
    @PostMapping("/writeSampleData")
    @ApiOperation("写入示例数据")
    public Result<Boolean> writeSampleData(
            @RequestParam(defaultValue = "iotdb") String tsdbType,
            @RequestParam(defaultValue = "cloud_platform") String database) {
        try {
            WriteDataRequest request = new WriteDataRequest();
            request.setTsdbType(tsdbType);
            request.setDatabase(database);
            
            List<WriteDataRequest.TimeSeriesDataDTO> dataList = new ArrayList<>();
            
            WriteDataRequest.TimeSeriesDataDTO data1 = new WriteDataRequest.TimeSeriesDataDTO();
            data1.setDevice("device_001");
            data1.setMeasurement("temperature");
            data1.setTimestamp(System.currentTimeMillis());
            
            Map<String, Object> fields1 = new HashMap<>();
            fields1.put("value", 25.5);
            fields1.put("unit", "celsius");
            data1.setFields(fields1);
            
            Map<String, String> tags1 = new HashMap<>();
            tags1.put("location", "room_1");
            tags1.put("sensor_type", "temperature_sensor");
            data1.setTags(tags1);
            
            dataList.add(data1);
            
            WriteDataRequest.TimeSeriesDataDTO data2 = new WriteDataRequest.TimeSeriesDataDTO();
            data2.setDevice("device_002");
            data2.setMeasurement("humidity");
            data2.setTimestamp(System.currentTimeMillis());
            
            Map<String, Object> fields2 = new HashMap<>();
            fields2.put("value", 65.3);
            fields2.put("unit", "percent");
            data2.setFields(fields2);
            
            Map<String, String> tags2 = new HashMap<>();
            tags2.put("location", "room_2");
            tags2.put("sensor_type", "humidity_sensor");
            data2.setTags(tags2);
            
            dataList.add(data2);
            
            request.setDataList(dataList);
            
            boolean success = tsdbService.writeData(request);
            if (success) {
                return Result.success("示例数据写入成功", true);
            } else {
                return Result.error("示例数据写入失败");
            }
        } catch (Exception e) {
            log.error("写入示例数据失败", e);
            return Result.error("写入示例数据异常: " + e.getMessage());
        }
    }
    
    @PostMapping("/querySampleData")
    @ApiOperation("查询示例数据")
    public Result<QueryResult> querySampleData(
            @RequestParam(defaultValue = "iotdb") String tsdbType,
            @RequestParam(defaultValue = "cloud_platform") String database,
            @RequestParam(defaultValue = "SELECT * FROM temperature") String sql) {
        try {
            QueryDataRequest request = new QueryDataRequest();
            request.setTsdbType(tsdbType);
            request.setDatabase(database);
            request.setSql(sql);
            
            QueryResult result = tsdbService.queryData(request);
            if (result.isSuccess()) {
                return Result.success("查询成功", result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("查询示例数据失败", e);
            return Result.error("查询示例数据异常: " + e.getMessage());
        }
    }
    
    @PostMapping("/queryByTimeRange")
    @ApiOperation("按时间范围查询示例数据")
    public Result<QueryResult> queryByTimeRange(
            @RequestParam(defaultValue = "iotdb") String tsdbType,
            @RequestParam(defaultValue = "cloud_platform") String database,
            @RequestParam String measurement,
            @RequestParam Long startTime,
            @RequestParam Long endTime,
            @RequestParam(defaultValue = "100") Integer limit) {
        try {
            QueryByTimeRangeRequest request = new QueryByTimeRangeRequest();
            request.setTsdbType(tsdbType);
            request.setDatabase(database);
            request.setMeasurement(measurement);
            request.setStartTime(startTime);
            request.setEndTime(endTime);
            request.setLimit(limit);
            
            QueryResult result = tsdbService.queryDataByTimeRange(request);
            if (result.isSuccess()) {
                return Result.success("查询成功", result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("按时间范围查询示例数据失败", e);
            return Result.error("按时间范围查询示例数据异常: " + e.getMessage());
        }
    }
    
    @PostMapping("/batchWriteSampleData")
    @ApiOperation("批量写入示例数据")
    public Result<Boolean> batchWriteSampleData(
            @RequestParam(defaultValue = "iotdb") String tsdbType,
            @RequestParam(defaultValue = "cloud_platform") String database,
            @RequestParam(defaultValue = "10") int count) {
        try {
            WriteDataRequest request = new WriteDataRequest();
            request.setTsdbType(tsdbType);
            request.setDatabase(database);
            
            List<WriteDataRequest.TimeSeriesDataDTO> dataList = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                WriteDataRequest.TimeSeriesDataDTO data = new WriteDataRequest.TimeSeriesDataDTO();
                data.setDevice("device_" + String.format("%03d", i));
                data.setMeasurement("sensor_data");
                data.setTimestamp(currentTime - (i * 1000));
                
                Map<String, Object> fields = new HashMap<>();
                fields.put("temperature", 20 + Math.random() * 10);
                fields.put("humidity", 50 + Math.random() * 20);
                fields.put("pressure", 1000 + Math.random() * 50);
                data.setFields(fields);
                
                Map<String, String> tags = new HashMap<>();
                tags.put("location", "area_" + (i % 5));
                tags.put("device_type", "sensor_" + (i % 3));
                data.setTags(tags);
                
                dataList.add(data);
            }
            
            request.setDataList(dataList);
            
            boolean success = tsdbService.writeData(request);
            if (success) {
                return Result.success("批量写入示例数据成功", true);
            } else {
                return Result.error("批量写入示例数据失败");
            }
        } catch (Exception e) {
            log.error("批量写入示例数据失败", e);
            return Result.error("批量写入示例数据异常: " + e.getMessage());
        }
    }
}
