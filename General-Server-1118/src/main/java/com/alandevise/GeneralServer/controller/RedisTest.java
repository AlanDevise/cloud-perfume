package com.alandevise.GeneralServer.controller;

import com.alandevise.GeneralServer.util.IGlobalCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @Filename: RedisTest.java
 * @Package: com.alandevise.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-20 19:02
 */

@RestController
@RequestMapping("/general")
@Api(tags = "Redis测试接口", value = "Redis测试接口")
@Slf4j
public class RedisTest {

    @Resource
    private IGlobalCache iGlobalCache;

    @GetMapping("/redisTest")
    @ApiOperation("Redis基本操作测试")
    public String FirstTest() {

        iGlobalCache.set("key2", "value3"); // set命令，设置redis键值对

        iGlobalCache.lSetAll("list", Arrays.asList("hello", "redis"));  // 一次set 多个 key-value 键值对

        List<Object> list = iGlobalCache.lGet("list", 0, -1);

        System.out.println(list);

        log.info(iGlobalCache.get("key2").toString());

        return iGlobalCache.get("key2").toString();
    }
}
