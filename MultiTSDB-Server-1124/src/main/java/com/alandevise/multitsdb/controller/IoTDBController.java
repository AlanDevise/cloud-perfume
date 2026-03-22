package com.alandevise.multitsdb.controller;

import com.alandevise.multitsdb.dto.QueryByTimeRangeRequest;
import com.alandevise.multitsdb.dto.QueryDataRequest;
import com.alandevise.multitsdb.dto.Result;
import com.alandevise.multitsdb.dto.WriteDataRequest;
import com.alandevise.multitsdb.service.IoTDBService;
import com.alandevise.tsdb.model.QueryResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iotdb")
public class IoTDBController {

    private final IoTDBService ioTDBService;

    public IoTDBController(IoTDBService ioTDBService) {
        this.ioTDBService = ioTDBService;
    }

    @PostMapping("/write")
    public Result<Boolean> writeData(@RequestBody WriteDataRequest request) {
        return ioTDBService.writeData(request)
                ? Result.success("IoTDB 数据写入成功", true)
                : Result.error("IoTDB 数据写入失败");
    }

    @PostMapping("/query")
    public Result<QueryResult> queryData(@RequestBody QueryDataRequest request) {
        QueryResult result = ioTDBService.queryData(request);
        return result.isSuccess() ? Result.success("IoTDB 查询成功", result) : Result.error(result.getMessage());
    }

    @PostMapping("/queryByTimeRange")
    public Result<QueryResult> queryByTimeRange(@RequestBody QueryByTimeRangeRequest request) {
        QueryResult result = ioTDBService.queryDataByTimeRange(request);
        return result.isSuccess() ? Result.success("IoTDB 查询成功", result) : Result.error(result.getMessage());
    }

    @PostMapping("/executeSql")
    public Result<QueryResult> executeSql(@RequestBody QueryDataRequest request) {
        QueryResult result = ioTDBService.executeSql(request);
        return result.isSuccess() ? Result.success("IoTDB SQL 执行成功", result) : Result.error(result.getMessage());
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("IoTDB 接口运行正常", "OK");
    }
}
