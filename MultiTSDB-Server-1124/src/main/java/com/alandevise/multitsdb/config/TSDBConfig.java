package com.alandevise.multitsdb.config;

import com.alandevise.multitsdb.adapter.TSDBAdapter;
import com.alandevise.multitsdb.adapter.impl.IoTDBAdapter;
import com.alandevise.multitsdb.adapter.impl.TDEngineAdapter;
import com.alandevise.multitsdb.constant.TSDBType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class TSDBConfig {
    
    @Autowired
    private TSDBProperties tsdbProperties;
    
    private Map<String, TSDBAdapter> adapterMap = new HashMap<>();
    
    @Bean
    public Map<String, TSDBAdapter> tsdbAdapters() {
        TSDBAdapter iotdbAdapter = null;
        TSDBAdapter tdengineAdapter = null;
        
        if (tsdbProperties.getIotdb().isEnabled()) {
            try {
                iotdbAdapter = new IoTDBAdapter(
                    tsdbProperties.getIotdb().getHost(),
                    tsdbProperties.getIotdb().getPort(),
                    tsdbProperties.getIotdb().getUsername(),
                    tsdbProperties.getIotdb().getPassword(),
                    tsdbProperties.getIotdb().isUseTableModel()
                );
                iotdbAdapter.init();
                adapterMap.put(TSDBType.IOTDB.getType(), iotdbAdapter);

                boolean existed = iotdbAdapter.databaseExists(tsdbProperties.getDefaultDatabase());
                if (existed || iotdbAdapter.createDatabase(tsdbProperties.getDefaultDatabase())) {
                    log.info("IoTDB adapter initialized and default database '{}' {}",
                            tsdbProperties.getDefaultDatabase(), existed ? "already exists" : "created");
                } else {
                    log.warn("IoTDB adapter initialized but failed to ensure default database '{}'",
                            tsdbProperties.getDefaultDatabase());
                }
            } catch (Exception e) {
                log.error("Failed to initialize IoTDB adapter", e);
            }
        }
        
        if (tsdbProperties.getTdengine().isEnabled()) {
            try {
                tdengineAdapter = new TDEngineAdapter(
                    tsdbProperties.getTdengine().getHost(),
                    tsdbProperties.getTdengine().getPort(),
                    tsdbProperties.getTdengine().getUsername(),
                    tsdbProperties.getTdengine().getPassword()
                );
                tdengineAdapter.init();
                adapterMap.put(TSDBType.TDENGINE.getType(), tdengineAdapter);

                boolean existed = tdengineAdapter.databaseExists(tsdbProperties.getDefaultDatabase());
                if (existed || tdengineAdapter.createDatabase(tsdbProperties.getDefaultDatabase())) {
                    log.info("TDEngine adapter initialized and default database '{}' {}",
                            tsdbProperties.getDefaultDatabase(), existed ? "already exists" : "created");
                } else {
                    log.warn("TDEngine adapter initialized but failed to ensure default database '{}'",
                            tsdbProperties.getDefaultDatabase());
                }
            } catch (Exception e) {
                log.error("Failed to initialize TDEngine adapter", e);
            }
        }
        
        return adapterMap;
    }
    
    @PreDestroy
    public void destroy() {
        for (TSDBAdapter adapter : adapterMap.values()) {
            try {
                adapter.close();
            } catch (Exception e) {
                log.error("Failed to close adapter: {}", adapter.getAdapterName(), e);
            }
        }
    }
}
