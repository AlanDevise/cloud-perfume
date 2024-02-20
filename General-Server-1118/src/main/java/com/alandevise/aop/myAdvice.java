package com.alandevise.aop;

import com.alandevise.annotation.Encrypted;
import com.alandevise.annotation.SensitiveData;
import com.alandevise.util.AESUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;

/**
 * @Filename: myAdvice.java
 * @Package: com.alandevise.aop
 * @Version: V1.0.0
 * @Description: 1. Aop切面类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年12月31日 17:54
 */

@Aspect
@Component
@Slf4j
public class myAdvice {

    private final Logger logger = LoggerFactory.getLogger(myAdvice.class);

    // 定义切面
    @Pointcut(value = "execution( * com.alandevise.controller.*.*(..) )")
    public void myPointcut() {

    }

    @Around("myPointcut()")
    public Object myLogger(ProceedingJoinPoint pjp) throws Throwable {

        Object[] args = pjp.getArgs();
        for(Object argument:args){
            // 校验该类是否被@SensitiveData所注解
            SensitiveData sensitiveData = AnnotationUtils.findAnnotation(argument.getClass(), SensitiveData.class);
            if (Objects.nonNull(sensitiveData)) {
                // 取出当前当前类所有字段，传入解密方法
                Field[] declaredFields = argument.getClass().getDeclaredFields();
                decrypt(argument);
                log.info("已经过解密");
            }
        }

        String className = pjp.getTarget().getClass().toString();
        String methodName = pjp.getSignature().getName();
        Object[] array = pjp.getArgs();

        ObjectMapper mapper = new ObjectMapper();

        logger.info("[AOP] 调用前：" + className + ":" + methodName + "传递的参数为：" + mapper.writeValueAsString(array));

        Object obj = pjp.proceed();

        logger.info("[AOP] 调用后：" + className + ":" + methodName + "传递的参数为：" + mapper.writeValueAsString(obj));

        return obj;
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
