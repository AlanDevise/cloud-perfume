package com.alandevise.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @Filename: Product.java
 * @Package: com.alandevise.entity
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年10月25日 23:36
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(indexName = "product")
public class Product {
    @Id
    private Long id;// 商品唯一标识

    @Field(type = FieldType.Text)       // 可分词
    private String title;// 商品名称

    @Field(type = FieldType.Keyword)    // 关键字不可分词
    private String category;// 分类名称

    @Field(type = FieldType.Double)
    private Double price;// 商品价格

    @Field(type = FieldType.Keyword, index = false)     // index = false 无索引不可被查询
    private String images;// 图片地址
}
