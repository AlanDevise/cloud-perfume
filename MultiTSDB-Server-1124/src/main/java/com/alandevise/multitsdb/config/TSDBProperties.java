package com.alandevise.multitsdb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tsdb")
public class TSDBProperties {
    
    private IoTDBConfig iotdb = new IoTDBConfig();
    private TDEngineConfig tdengine = new TDEngineConfig();
    private String defaultDatabase = "cloud_platform";
    
    @Data
    public static class IoTDBConfig {
        private boolean enabled = true;
        private String host = "localhost";
        private int port = 6667;
        private String username = "root";
        private String password = "root";
        private boolean useTableModel = false;
    }
    
    @Data
    public static class TDEngineConfig {
        private boolean enabled = true;
        private String host = "localhost";
        private int port = 6041;
        private String username = "root";
        private String password = "taosdata";
    }
}
