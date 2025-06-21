package com.alandevise.Mediamtx.exception;

/**
 * @Filename: ResourceNotFoundException.java
 * @Package: com.alandevise.Mediamtx.exception
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2025年06月07日 15:43
 */

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}