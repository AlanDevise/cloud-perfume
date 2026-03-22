package com.alandevise.tsdb.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * starter 配置项。
 * 对应 application.yml 中的 tsdb.* 配置。
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2026-03-22
 */
@Data
@ConfigurationProperties(prefix = "tsdb")
public class TSDBProperties {

    /**
     * IoTDB 连接配置。
     */
    private IoTDBConfig iotdb = new IoTDBConfig();
    /**
     * TDEngine 连接配置。
     */
    private TDEngineConfig tdengine = new TDEngineConfig();
    /**
     * 默认时序库类型。
     * 当调用工具方法未显式传 tsdbType 时使用，默认 iotdb。
     */
    private String defaultType = "iotdb";
    /**
     * 默认数据库名。
     * 当调用工具方法未显式传 database 时使用，默认 cloud_platform。
     */
    private String defaultDatabase = "cloud_platform";

    /**
     * IoTDB 配置参数。
     * @Author Alan Zhang [initiator@alandevise.com]
     * @Date 2026-03-22
     */
    @Data
    public static class IoTDBConfig {
        /**
         * 是否启用 IoTDB 适配器。
         */
        private boolean enabled = true;
        /**
         * IoTDB 主机地址。
         */
        private String host = "localhost";
        /**
         * IoTDB 端口。
         */
        private int port = 6667;
        /**
         * IoTDB 用户名。
         */
        private String username = "root";
        /**
         * IoTDB 密码。
         */
        private String password = "root";
        /**
         * 是否启用表模型。
         * false 为树模型，true 为表模型。
         */
        private boolean useTableModel = false;
    }

    /**
     * TDEngine 配置参数。
     * @Author Alan Zhang [initiator@alandevise.com]
     * @Date 2026-03-22
     */
    @Data
    public static class TDEngineConfig {
        /**
         * 是否启用 TDEngine 适配器。
         */
        private boolean enabled = true;
        /**
         * TDEngine 主机地址。
         */
        private String host = "localhost";
        /**
         * TDEngine REST/JDBC 端口。
         */
        private int port = 6041;
        /**
         * TDEngine 用户名。
         */
        private String username = "root";
        /**
         * TDEngine 密码。
         */
        private String password = "taosdata";
    }
}
