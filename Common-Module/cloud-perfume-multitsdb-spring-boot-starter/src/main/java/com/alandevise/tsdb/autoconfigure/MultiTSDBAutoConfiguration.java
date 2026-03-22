package com.alandevise.tsdb.autoconfigure;

import com.alandevise.tsdb.core.MultiTSDBTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Multi TSDB starter 自动装配入口。
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2026-03-22
 */
@Configuration
@ConditionalOnClass(MultiTSDBTemplate.class)
@EnableConfigurationProperties(TSDBProperties.class)
public class MultiTSDBAutoConfiguration {

    /**
     * 创建适配器管理器。
     *
     * @param properties TSDB 配置参数，包含默认库和各时序库连接信息
     * @return 适配器管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public TSDBAdapterManager tsdbAdapterManager(TSDBProperties properties) {
        return new TSDBAdapterManager(properties);
    }

    /**
     * 创建统一的时序库工具模板。
     *
     * @param adapterManager 适配器管理器，用于按类型获取 IoTDB/TDEngine 适配器
     * @param properties TSDB 配置参数，用于兜底默认类型和默认数据库
     * @return 统一读写模板
     */
    @Bean
    @ConditionalOnMissingBean
    public MultiTSDBTemplate multiTSDBTemplate(TSDBAdapterManager adapterManager, TSDBProperties properties) {
        return new MultiTSDBTemplate(adapterManager, properties);
    }
}
