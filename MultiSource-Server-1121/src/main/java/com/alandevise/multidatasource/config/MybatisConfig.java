package com.alandevise.multidatasource.config;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.alandevise.multidatasource.handler.MyBatisMeteObjectHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * @Filename: MybatisConfig.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */
@Configuration
public class MybatisConfig {

    @Autowired
    @Qualifier("dynamicDataSource")
    DynamicDataSource dynamicDataSource;

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {

        // mybatis plus在配置多数据源的时候,要手动配置一下MybatisSqlSessionFactoryBean,不然mybatis plus自带的方法会报错
        // 具体查看MybatisPlusAutoConfiguration类如下:
        // @Configuration
        // @ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
        // @ConditionalOnSingleCandidate(DataSource.class)
        // @EnableConfigurationProperties({MybatisPlusProperties.class})
        // @AutoConfigureAfter({DataSourceAutoConfiguration.class, MybatisPlusLanguageDriverAutoConfiguration.class})
        // public class MybatisPlusAutoConfiguration implements InitializingBean
        // 可以看到第三个注解,只有一个候选的DataSource在容器里面,才会执行自动装配的逻辑


        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dynamicDataSource);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        // 由于多数据源自动装配失败,MyBatisMeteObjectHandler注入失败,要手动new
        sqlSessionFactoryBean.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyBatisMeteObjectHandler()));


        SqlSessionFactory factory = sqlSessionFactoryBean.getObject();

        // 配置多个数据源时, yaml文件里面指定的配置居然不生效,要这样来配置,有点奇怪
        factory.getConfiguration().setMapUnderscoreToCamelCase(true);
        return factory;
    }


}
