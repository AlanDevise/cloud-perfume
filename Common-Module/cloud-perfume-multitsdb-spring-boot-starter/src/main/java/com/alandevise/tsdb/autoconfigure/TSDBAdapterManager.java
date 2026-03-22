package com.alandevise.tsdb.autoconfigure;

import com.alandevise.tsdb.adapter.TSDBAdapter;
import com.alandevise.tsdb.adapter.impl.IoTDBAdapter;
import com.alandevise.tsdb.adapter.impl.TDEngineAdapter;
import com.alandevise.tsdb.constant.TSDBType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TSDB 适配器管理器。
 * 负责初始化、缓存和销毁不同类型的时序库适配器。
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2026-03-22
 */
@Slf4j
public class TSDBAdapterManager implements DisposableBean {

    private final TSDBProperties properties;
    private final Map<String, TSDBAdapter> adapters = new LinkedHashMap<>();

    /**
     * @param properties TSDB 配置参数，决定启用哪些适配器以及默认数据库
     */
    public TSDBAdapterManager(TSDBProperties properties) {
        this.properties = properties;
        initializeAdapters();
    }

    /**
     * 获取所有已注册适配器。
     *
     * @return 只读适配器映射，key 为 tsdbType
     */
    public Map<String, TSDBAdapter> getAdapters() {
        return Collections.unmodifiableMap(adapters);
    }

    /**
     * 根据 tsdbType 获取适配器。
     *
     * @param tsdbType 时序库类型，如 iotdb、tdengine；为空时返回第一个可用适配器
     * @return 对应适配器，不存在时返回 null
     */
    public TSDBAdapter getAdapter(String tsdbType) {
        if (adapters.isEmpty()) {
            return null;
        }
        if (tsdbType == null || tsdbType.trim().isEmpty()) {
            return adapters.values().iterator().next();
        }
        return adapters.get(tsdbType.toLowerCase());
    }

    /**
     * 销毁所有已注册适配器，释放底层连接资源。
     */
    @Override
    public void destroy() {
        for (TSDBAdapter adapter : adapters.values()) {
            try {
                adapter.close();
            } catch (Exception e) {
                log.error("Failed to close adapter: {}", adapter.getAdapterName(), e);
            }
        }
    }

    /**
     * 依次初始化所有支持的时序库适配器。
     */
    private void initializeAdapters() {
        initializeIoTDBAdapter();
        initializeTDEngineAdapter();
    }

    /**
     * 初始化 IoTDB 适配器，并确保默认数据库存在。
     */
    private void initializeIoTDBAdapter() {
        if (!properties.getIotdb().isEnabled()) {
            return;
        }

        try {
            TSDBAdapter adapter = new IoTDBAdapter(
                    properties.getIotdb().getHost(),
                    properties.getIotdb().getPort(),
                    properties.getIotdb().getUsername(),
                    properties.getIotdb().getPassword(),
                    properties.getIotdb().isUseTableModel()
            );
            adapter.init();
            adapters.put(TSDBType.IOTDB.getType(), adapter);
            ensureDefaultDatabase(adapter, properties.getDefaultDatabase());
            log.info("IoTDB adapter registered");
        } catch (Exception e) {
            log.error("Failed to initialize IoTDB adapter", e);
        }
    }

    /**
     * 初始化 TDEngine 适配器，并确保默认数据库存在。
     */
    private void initializeTDEngineAdapter() {
        if (!properties.getTdengine().isEnabled()) {
            return;
        }

        try {
            TSDBAdapter adapter = new TDEngineAdapter(
                    properties.getTdengine().getHost(),
                    properties.getTdengine().getPort(),
                    properties.getTdengine().getUsername(),
                    properties.getTdengine().getPassword()
            );
            adapter.init();
            adapters.put(TSDBType.TDENGINE.getType(), adapter);
            ensureDefaultDatabase(adapter, properties.getDefaultDatabase());
            log.info("TDEngine adapter registered");
        } catch (Exception e) {
            log.error("Failed to initialize TDEngine adapter", e);
        }
    }

    /**
     * 启动时保证默认数据库可用，方便调用方省略 database 参数。
     *
     * @param adapter 具体时序库适配器
     * @param database 默认数据库名
     */
    private void ensureDefaultDatabase(TSDBAdapter adapter, String database) {
        boolean existed = adapter.databaseExists(database);
        if (existed || adapter.createDatabase(database)) {
            log.info("{} default database '{}' {}", adapter.getAdapterName(), database,
                    existed ? "already exists" : "created");
            return;
        }
        log.warn("{} failed to ensure default database '{}'", adapter.getAdapterName(), database);
    }
}
