package com.alandevise.GeneralServer.interceptor;

import com.alandevise.GeneralServer.annotation.Encrypted;
import com.alandevise.GeneralServer.annotation.SensitiveData;
import com.alandevise.GeneralServer.util.AESUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
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
 * @Description: 说明
 * 1. 解密拦截器，针对从DB出查询出的结果集进行解密操作。
 * 2. 拦截所有的handleResultSets方法，检查对象是否为包含@SensitiveData注解的类，随后进一步遍历类中所有包含@Encrypted的字段。
 * 3. 对于包含@Encrypted注解的字段进行解密操作。
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:30
 */

@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
@Slf4j
@Component
public class DecryptInterceptor implements Interceptor, HandlerInterceptor {
    /**
     * 解密拦截器具体逻辑
     *
     * @param invocation 代理对象
     * @return java.lang.Object
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 10:32
     */
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
            log.error("淦，解密失败", e);
        }
        return resultObject;
    }

    /**
     * 是否需要解密判断方法
     *
     * @param object 数据对象
     * @return result true：需要解密 false：不需要解密
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 10:33
     */
    private boolean needToDecrypt(Object object) {
        // 获取对象类型
        Class<?> objectClass = object.getClass();
        // 判断是否为SensitiveData对象
        SensitiveData sensitiveData = AnnotationUtils.findAnnotation(objectClass, SensitiveData.class);
        return Objects.nonNull(sensitiveData);
    }

    /**
     * 切记配置，否则当前拦截器不会加入拦截器链
     *
     * @param o 对象
     * @return java.lang.Object
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 10:34
     */
    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    /**
     * 解密方法
     *
     * @param result 数据对象
     * @return result 解密后的数据对象
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 10:35
     */
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