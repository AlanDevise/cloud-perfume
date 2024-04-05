package com.alandevise.GeneralServer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Filename BusinessException.java
 * @Package com.alandevise.exception
 * @Version V1.0.0
 * @Description 1.
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2023-12-04 17:37
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BusinessException extends RuntimeException{
    private String code;
    private String message;
}
