package com.alandevise.multitsdb.controller;

import com.alandevise.multitsdb.dto.QueryByTimeRangeRequest;
import com.alandevise.multitsdb.dto.QueryDataRequest;
import com.alandevise.multitsdb.dto.Result;
import com.alandevise.multitsdb.dto.WriteDataRequest;
import com.alandevise.multitsdb.service.TDEngineService;
import com.alandevise.tsdb.model.QueryResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tdengine")
public class TDEngineController {

    private final TDEngineService tdEngineService;

    public TDEngineController(TDEngineService tdEngineService) {
        this.tdEngineService = tdEngineService;
    }

    @PostMapping("/write")
    public Result<Boolean> writeData(@RequestBody WriteDataRequest request) {
        return tdEngineService.writeData(request)
                ? Result.success("TDEngine 数据写入成功", true)
                : Result.error("TDEngine 数据写入失败");
    }

    @PostMapping("/query")
    public Result<QueryResult> queryData(@RequestBody QueryDataRequest request) {
        QueryResult result = tdEngineService.queryData(request);
        return result.isSuccess() ? Result.success("TDEngine 查询成功", result) : Result.error(result.getMessage());
    }

    @PostMapping("/queryByTimeRange")
    public Result<QueryResult> queryByTimeRange(@RequestBody QueryByTimeRangeRequest request) {
        QueryResult result = tdEngineService.queryDataByTimeRange(request);
        return result.isSuccess() ? Result.success("TDEngine 查询成功", result) : Result.error(result.getMessage());
    }

    @PostMapping("/executeSql")
    public Result<QueryResult> executeSql(@RequestBody QueryDataRequest request) {
        QueryResult result = tdEngineService.executeSql(request);
        return result.isSuccess() ? Result.success("TDEngine SQL 执行成功", result) : Result.error(result.getMessage());
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("TDEngine 接口运行正常", "OK");
    }
}
