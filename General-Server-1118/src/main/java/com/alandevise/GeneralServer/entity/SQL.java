package com.alandevise.GeneralServer.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Filename: SQL.java
 * @Package: com.alandevise.entity
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023-03-31 20:09
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SQL {
    private String tableName = "student";
    private String columns = "`name`, age, addr, addr_num";
    private String val = "'李毅', '27','深圳市', '123456'";
}
