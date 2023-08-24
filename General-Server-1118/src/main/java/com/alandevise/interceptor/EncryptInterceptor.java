package com.alandevise.interceptor;

import com.alandevise.annotation.Encrypted;
import com.alandevise.annotation.SensitiveData;
import com.alandevise.util.AESUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Objects;

/**
 * @Filename: EncryptInterceptor.java
 * @Package: com.alandevise.interceptor
 * @Version: V1.0.0
 * @Description: 1. 加密拦截器
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:28
 */


@Slf4j
// @Component
@Intercepts({
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class}),
})
public class EncryptInterceptor implements Interceptor, HandlerInterceptor {

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
                    Class<?> parameterObjectClass = parameterObjectMap.get(key).getClass();
                    // 校验该实例的类是否被@SensitiveData所注解
                    SensitiveData sensitiveData = AnnotationUtils.findAnnotation(parameterObjectClass, SensitiveData.class);
                    if (Objects.nonNull(sensitiveData)) {
                        // 取出当前当前类所有字段，传入加密方法
                        Field[] declaredFields = parameterObjectClass.getDeclaredFields();
                        encrypt(declaredFields, ((HashMap<?, ?>) parameterObject).get(key));
                        log.info("已经过加密");
                    }
                    break;
                }
            }
            return invocation.proceed();
        } catch (Exception e) {
            log.error("加密失败", e);
        }
        return invocation.proceed();
    }

    /**
     * 切记配置，否则当前拦截器不会加入拦截器链
     */
    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    public <T> T encrypt(Field[] declaredFields, T paramsObject) throws Exception {
        for (Field field : declaredFields) {
            // 取出所有被EncryptDecryptField注解的字段
            Encrypted sensitiveField = field.getAnnotation(Encrypted.class);
            if (!Objects.isNull(sensitiveField)) {
                field.setAccessible(true);
                Object object = field.get(paramsObject);
                // 暂时只实现String类型的加密
                if (object instanceof String) {
                    String value = (String) object;
                    // 加密  这里我使用自定义的AES加密工具
                    field.set(paramsObject, AESUtils.encrypt(value));
                }
            }
        }
        return paramsObject;
    }
}