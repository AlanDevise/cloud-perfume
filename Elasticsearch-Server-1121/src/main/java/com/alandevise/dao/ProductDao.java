package com.alandevise.dao;

import com.alandevise.entity.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @Filename: ProductDao.java
 * @Package: com.alandevise.dao
 * @Version: V1.0.0
 * @Description: 1. ElasticsearchRepository<实体对象, 对应主键ID>
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年10月25日 23:48
 */

@Repository
public interface ProductDao extends ElasticsearchRepository<Product, Long> {
}
