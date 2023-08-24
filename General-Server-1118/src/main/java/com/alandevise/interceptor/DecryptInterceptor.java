package com.alandevise.interceptor;

import com.alandevise.annotation.Encrypted;
import com.alandevise.annotation.SensitiveData;
import com.alandevise.util.AESUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @Filename: DecryptInterceptor.java
 * @Package: com.alandevise.interceptor
 * @Version: V1.0.0
 * @Description: 1. 解密拦截器
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:30
 */

@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
@Slf4j
// @Component
public class DecryptInterceptor implements Interceptor, HandlerInterceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object resultObject = invocation.proceed();
        try {
            if (Objects.isNull(resultObject)) {
                return null;
            }
            // 基于selectList
            if (resultObject instanceof ArrayList) {
                ArrayList resultList = (ArrayList) resultObject;
                if (!CollectionUtils.isEmpty(resultList) && needToDecrypt(resultList.get(0))) {
                    for (Object result : resultList) {
                        // 逐一解密
                        decrypt(result);
                    }
                    log.info("已经过解密");
                }
                // 基于selectOne
            } else {
                if (needToDecrypt(resultObject)) {
                    AESUtils.decrypt((String) resultObject);
                    log.info("已经过解密");
                }
            }
            return resultObject;
        } catch (Exception e) {
            log.error("解密失败", e);
        }
        return resultObject;
    }

    private boolean needToDecrypt(Object object) {
        Class<?> objectClass = object.getClass();
        SensitiveData sensitiveData = AnnotationUtils.findAnnotation(objectClass, SensitiveData.class);
        return Objects.nonNull(sensitiveData);
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    public <T> T decrypt(T result) throws Exception {
        // 取出resultType的类
        Class<?> resultClass = result.getClass();
        Field[] declaredFields = resultClass.getDeclaredFields();
        for (Field field : declaredFields) {
            // 取出所有被EncryptDecryptField注解的字段
            Encrypted sensitiveField = field.getAnnotation(Encrypted.class);
            if (!Objects.isNull(sensitiveField)) {
                field.setAccessible(true);
                Object object = field.get(result);
                // 只支持String的解密
                if (object instanceof String) {
                    String value = (String) object;
                    // 对注解的字段进行逐一解密
                    String decrypt = AESUtils.decrypt(value);
                    field.set(result, decrypt);
                    log.info(field.get(result).toString());
                }
            }
        }
        return result;
    }
}