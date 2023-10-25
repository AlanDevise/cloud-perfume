package com.alandevise;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Filename: SpringDataESIndexTest.java
 * @Package: PACKAGE_NAME
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年10月25日 23:55
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringDataESIndexTest {
    // 注入ElasticsearchRestTemplate
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    // 创建索引并增加映射配置
    @Test
    public void createIndex() {
        // 创建索引，系统初始化会自动创建索引
        System.out.println("[INFO] 创建索引");
    }

    @Test
    public void deleteIndex() {
        // 创建索引，系统初始化会自动创建索引
        boolean flg = elasticsearchRestTemplate.indexOps(IndexCoordinates.of("product")).delete();
        System.out.println("删除索引对象 = " + flg);
    }

}
