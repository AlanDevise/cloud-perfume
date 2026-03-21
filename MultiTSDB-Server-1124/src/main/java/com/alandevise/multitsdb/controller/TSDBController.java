package com.alandevise.multitsdb.controller;

import com.alandevise.multitsdb.dto.QueryByTimeRangeRequest;
import com.alandevise.multitsdb.dto.QueryDataRequest;
import com.alandevise.multitsdb.dto.Result;
import com.alandevise.multitsdb.dto.WriteDataRequest;
import com.alandevise.multitsdb.model.QueryResult;
import com.alandevise.multitsdb.service.TSDBService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/tsdb")
@Api(tags = "多时序数据库管理接口")
public class TSDBController {
    
    @Autowired
    private TSDBService tsdbService;
    
    @PostMapping("/write")
    @ApiOperation("写入数据")
    public Result<Boolean> writeData(@RequestBody WriteDataRequest request) {
        try {
            boolean success = tsdbService.writeData(request);
            if (success) {
                return Result.success("数据写入成功", true);
            } else {
                return Result.error("数据写入失败");
            }
        } catch (Exception e) {
            log.error("写入数据失败", e);
            return Result.error("数据写入异常: " + e.getMessage());
        }
    }
    
    @PostMapping("/query")
    @ApiOperation("查询数据")
    public Result<QueryResult> queryData(@RequestBody QueryDataRequest request) {
        try {
            QueryResult result = tsdbService.queryData(request);
            if (result.isSuccess()) {
                return Result.success("查询成功", result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("查询数据失败", e);
            return Result.error("查询数据异常: " + e.getMessage());
        }
    }

    @PostMapping("/executeSql")
    @ApiOperation("直接执行SQL")
    public Result<QueryResult> executeSql(@RequestBody QueryDataRequest request) {
        try {
            QueryResult result = tsdbService.executeSql(request);
            if (result.isSuccess()) {
                return Result.success("SQL执行成功", result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("执行SQL失败", e);
            return Result.error("执行SQL异常: " + e.getMessage());
        }
    }
    
    @PostMapping("/queryByTimeRange")
    @ApiOperation("按时间范围查询数据")
    public Result<QueryResult> queryDataByTimeRange(@RequestBody QueryByTimeRangeRequest request) {
        try {
            QueryResult result = tsdbService.queryDataByTimeRange(request);
            if (result.isSuccess()) {
                return Result.success("查询成功", result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("查询数据失败", e);
            return Result.error("查询数据异常: " + e.getMessage());
        }
    }
    
    @PostMapping("/createDatabase")
    @ApiOperation("创建数据库")
    public Result<Boolean> createDatabase(
            @ApiParam(value = "时序数据库类型", required = true) @RequestParam String tsdbType,
            @ApiParam(value = "数据库名称", required = true) @RequestParam String database) {
        try {
            boolean success = tsdbService.createDatabase(tsdbType, database);
            if (success) {
                return Result.success("数据库创建成功", true);
            } else {
                return Result.error("数据库创建失败");
            }
        } catch (Exception e) {
            log.error("创建数据库失败", e);
            return Result.error("创建数据库异常: " + e.getMessage());
        }
    }
    
    @PostMapping("/dropDatabase")
    @ApiOperation("删除数据库")
    public Result<Boolean> dropDatabase(
            @ApiParam(value = "时序数据库类型", required = true) @RequestParam String tsdbType,
            @ApiParam(value = "数据库名称", required = true) @RequestParam String database) {
        try {
            boolean success = tsdbService.dropDatabase(tsdbType, database);
            if (success) {
                return Result.success("数据库删除成功", true);
            } else {
                return Result.error("数据库删除失败");
            }
        } catch (Exception e) {
            log.error("删除数据库失败", e);
            return Result.error("删除数据库异常: " + e.getMessage());
        }
    }
    
    @GetMapping("/health")
    @ApiOperation("健康检查")
    public Result<String> health() {
        return Result.success("MultiTSDB服务运行正常", "OK");
    }
    
    @GetMapping("/adapters")
    @ApiOperation("获取所有可用的适配器")
    public Result<Object> getAdapters() {
        try {
            return Result.success("获取适配器列表成功", tsdbService.getAllAdapters().keySet());
        } catch (Exception e) {
            log.error("获取适配器列表失败", e);
            return Result.error("获取适配器列表异常: " + e.getMessage());
        }
    }
}
