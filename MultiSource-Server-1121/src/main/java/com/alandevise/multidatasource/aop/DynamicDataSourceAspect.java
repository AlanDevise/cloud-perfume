package com.alandevise.multidatasource.aop;

import com.alandevise.multidatasource.annotation.MultiDataSource;
import com.alandevise.multidatasource.config.DynamicDataSource;
import com.alandevise.multidatasource.constants.DataSourceType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Filename: MultiDataSource.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1. 动态数据源切面逻辑，哪种方法使用主数据源哪种方法使用备数据源，靠这个切面决定
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */
@Aspect
@Component
public class DynamicDataSourceAspect {

    @Pointcut("execution(* com.alandevise.multidatasource.service.*.get*(..))" +
            "|| execution(* com.alandevise.multidatasource.service.*.find*(..))" +
            "|| execution(* com.alandevise.multidatasource.service.*.select*(..))"
    )
    public void defaultReadPointCut() {

    }

    @Pointcut("execution(* com.alandevise.multidatasource.service.*.update*(..))" +
            "|| execution(* com.alandevise.multidatasource.service.*.delete*(..))" +
            "|| execution(* com.alandevise.multidatasource.service.*.add*(..))" +
            "|| execution(* com.alandevise.multidatasource.service.*.save*(..))"
    )
    public void defaultWriterPointCut() {

    }

    @Pointcut("@annotation(com.alandevise.multidatasource.annotation.MultiDataSource)")
    public void annotationPointCut() {

    }


    @Before("defaultReadPointCut()")
    public void read() {
        DynamicDataSource.forSlave();
    }


    @Before("defaultWriterPointCut()")
    public void write() {
        DynamicDataSource.forMaster();
    }

    @Before("annotationPointCut()")
    public void determineByAnnotation(JoinPoint joinPoint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        MultiDataSource multiDataSource = signature.getMethod().getAnnotation(MultiDataSource.class);
        if (Objects.nonNull(multiDataSource)) {
            if (Objects.equals(DataSourceType.MASTER, multiDataSource.connectTo())) {
                DynamicDataSource.forMaster();

            } else {
                DynamicDataSource.forSlave();
            }
        }

    }


}
