package com.alandevise.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Filename: TFAccrue.java
 * @Package: com.alandevise.entity
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月31日 22:20
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TFAccrue {
    private String tagCode;
    private String tagNo;
    private String tagName;
    private String name;
    private String tagType;
    private String tagClass;
    private String preci;
    private String unit;
    private String compute;
    private String deviceCode;
    private String coefficient;
    private String radix;
    private String maxRange;
    private String minRange;
    private String filterMutational;
    private String mutationalPe;
    private String saveType;
    private String saveCyc;
    private String deadValue;
    private String count;
    private String showModel;
    private String sort;
    private String fertProperty;
    private String details;
    private String pointType;
    private String formula;
    private String fertCode;
}
