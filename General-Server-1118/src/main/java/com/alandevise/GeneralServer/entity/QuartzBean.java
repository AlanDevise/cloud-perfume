package com.alandevise.GeneralServer.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Filename: QuartzBean.java
 * @Package: com.alandevise.entity
 * @Version: V1.0.0
 * @Description: 1. 定时任务相关实体类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月20日 21:21
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuartzBean {
    /** 任务id */
    private String  id;

    /** 任务名称 */
    private String jobName;

    /** 任务执行类 */
    private String jobClass;

    /** 任务状态 启动还是暂停*/
    private Integer status;

    /** 任务运行时间表达式 */
    private String cronExpression;
}
