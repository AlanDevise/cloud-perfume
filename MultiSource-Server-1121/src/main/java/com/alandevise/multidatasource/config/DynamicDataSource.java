package com.alandevise.multidatasource.config;

import com.alandevise.multidatasource.constants.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.*;

/**
 * @Filename: DynamicDataSource.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    private static Map<DataSourceType, List<DataSource>> dataSourceCache = new HashMap<>();

    private static ThreadLocal<DataSource> dataSourceHolder = new ThreadLocal<>();

    private static int slaveDataSourceQuantity = 0;

    private static final Random RANDOM = new Random();

    @Override
    protected Object determineCurrentLookupKey() {
        return dataSourceHolder.get();
    }


    public static void forMaster() {
        dataSourceHolder.set(dataSourceCache.get(DataSourceType.MASTER).get(0));
        log.info("读主数据源");
    }


    public static void forSlave() {

        int idx = RANDOM.nextInt(slaveDataSourceQuantity);
        dataSourceHolder.set(dataSourceCache.get(DataSourceType.SLAVE).get(idx));
        log.info("读数组下标为[{}]的从数据源", idx);

    }


    public static DynamicDataSource builder() {
        return new DynamicDataSource();
    }

    public DynamicDataSource withMasterDataSource(DataSource master) {
        List<DataSource> masterDataSources = dataSourceCache.getOrDefault(DataSourceType.MASTER, new ArrayList<>());
        masterDataSources.add(master);
        dataSourceCache.put(DataSourceType.MASTER, masterDataSources);
        setDefaultTargetDataSource(master);
        return this;
    }

    public DynamicDataSource withSlaveDataSource(DataSource... dataSources) {

        List<DataSource> slaveDataSources = dataSourceCache.getOrDefault(DataSourceType.SLAVE, new ArrayList<>());
        slaveDataSources.addAll(Arrays.asList(dataSources));
        dataSourceCache.put(DataSourceType.SLAVE, slaveDataSources);

        slaveDataSourceQuantity = dataSources.length;
        return this;
    }

    public DynamicDataSource withTargetDataSource(DataSource... defaultDataSources) {

        Map<Object, Object> targetDataSource = new HashMap<>();
        for (DataSource dataSource : defaultDataSources) {
            targetDataSource.put(dataSource, dataSource);
        }
        setTargetDataSources(targetDataSource);
        return this;
    }


}
