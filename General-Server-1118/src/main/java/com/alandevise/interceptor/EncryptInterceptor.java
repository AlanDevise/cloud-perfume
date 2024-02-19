package com.alandevise.interceptor;

import com.alandevise.annotation.Encrypted;
import com.alandevise.annotation.SensitiveData;
import com.alandevise.util.AESUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Field;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Filename: EncryptInterceptor.java
 * @Package: com.alandevise.interceptor
 * @Version: V1.0.0
 * @Description: 说明
 * 1. 加密拦截器，拦截所有的setParameters方法，检查对象是否为包含@SensitiveData注解的类，随后进一步遍历类中所有包含@Encrypted的字段。
 * 2. 能够拦截insert与update两种需要包含setParameters方法的操作
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:28
 */

@Slf4j
@Component
@Intercepts({
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class}),
})
public class EncryptInterceptor implements Interceptor, HandlerInterceptor {

    /**
     * 加密拦截器具体逻辑
     *
     * @param invocation 代理对象
     * @return java.lang.Object
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 10:12
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            ParameterHandler parameterHandler = (ParameterHandler) invocation.getTarget();
            // 获取参数对像，即 mapper 中 paramsType 的实例
            Field parameterField = parameterHandler.getClass().getDeclaredField("parameterObject");
            parameterField.setAccessible(true);
            // 取出实例
            Object parameterObject = parameterField.get(parameterHandler);
            if (parameterObject != null) {
                HashMap parameterObjectMap = (HashMap) parameterObject;
                for (Object key : parameterObjectMap.keySet()) {
                    Object object = parameterObjectMap.get(key);
                    if (object instanceof ArrayList) {
                        // 多条数据情况，强转成ArrayList类型以进行遍历
                        ArrayList<?> dataList = (ArrayList<?>) object;
                        for (Object data : dataList) {
                            // 对单条数据进行处理
                            Class<?> aClass = data.getClass();
                            // 校验该实例的类是否被@SensitiveData所注解
                            SensitiveData sensitiveData = AnnotationUtils.findAnnotation(aClass, SensitiveData.class);
                            if (Objects.nonNull(sensitiveData)) {
                                // 取出当前当前类所有字段，传入加密方法
                                Field[] declaredFields = aClass.getDeclaredFields();
                                encrypt(declaredFields, data);
                                log.info("已经过加密");
                            }
                        }
                    } else {
                        // 单条数据情况
                        Class<?> parameterObjectClass = parameterObjectMap.get(key).getClass();
                        // 校验该实例的类是否被@SensitiveData所注解
                        SensitiveData sensitiveData = AnnotationUtils.findAnnotation(parameterObjectClass, SensitiveData.class);
                        if (Objects.nonNull(sensitiveData)) {
                            // 取出当前当前类所有字段，传入加密方法
                            Field[] declaredFields = parameterObjectClass.getDeclaredFields();
                            encrypt(declaredFields, ((HashMap<?, ?>) parameterObject).get(key));
                            log.info("已经过加密");
                        }
                    }
                    break;
                }
            }
            return invocation.proceed();
        } catch (Exception e) {
            log.error("淦，加密失败了", e);
        }
        return invocation.proceed();
    }

    /**
     * 切记配置，否则当前拦截器不会加入拦截器链
     *
     * @param o 对象
     * @return java.lang.Object
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 10:23
     */
    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    /**
     * 加密方法，将参数对象中带@Encrypted注解的字段值全部进行加密处理，具体的加密方法可以自定义
     *
     * @param declaredFields 声明的字段
     * @param paramsObject   参数对象
     * @return T 返回加密后的对象
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 10:24
     */
    public <T> T encrypt(Field[] declaredFields, T paramsObject) throws Exception {
        for (Field field : declaredFields) {
            // 取出所有被EncryptDecryptField注解的字段
            Encrypted sensitiveField = field.getAnnotation(Encrypted.class);
            if (!Objects.isNull(sensitiveField)) {
                field.setAccessible(true);
                Object object = field.get(paramsObject);
                // 暂时只实现String类型的加密（似乎也只有String类型需要加密）
                if (object instanceof String) {
                    String value = (String) object;
                    // 加密  这里我使用自定义的AES加密工具（可自定义加密方法）
                    field.set(paramsObject, AESUtils.encrypt(value));
                }
            }
        }
        return paramsObject;
    }
}