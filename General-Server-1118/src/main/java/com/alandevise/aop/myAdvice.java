package com.alandevise.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
public class myAdvice {

    private final Logger logger = LoggerFactory.getLogger(myAdvice.class);

    // 定义切面
    @Pointcut(value = "execution( * com.alandevise.controller.*.*(..) )")
    public void myPointcut() {

    }

    @Around("myPointcut()")
    public Object myLogger(ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getTarget().getClass().toString();
        String methodName = pjp.getSignature().getName();
        Object[] array = pjp.getArgs();

        ObjectMapper mapper = new ObjectMapper();

        logger.info("[AOP] 调用前：" + className + ":" + methodName + "传递的参数为：" + mapper.writeValueAsString(array));

        Object obj = pjp.proceed();

        logger.info("[AOP] 调用后：" + className + ":" + methodName + "传递的参数为：" + mapper.writeValueAsString(obj));

        return obj;
    }
}
