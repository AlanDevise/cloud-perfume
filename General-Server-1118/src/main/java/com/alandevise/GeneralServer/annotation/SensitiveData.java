package com.alandevise.GeneralServer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Filename: SensitiveData.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1. 带有敏感字段的类需要加这个注解
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:12
 */

@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveData {
}