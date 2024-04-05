package com.alandevise.GeneralServer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Filename: Encrypted.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1. 需要加解密的字段用这个注解
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Encrypted {

}