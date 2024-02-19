package com.alandevise.CommonTool;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @Filename CommonTool.java
 * @Package com.alandevise.CommonTool
 * @Version V1.0.0
 * @Description 1. 公共工具类
 * @Author Alan Zhang [initiator@alandevise.com]
 * @Date 2024-02-19 09:38
 */

public class CommonTool {
    // ObjectToMap
    public static Map<String, Object> getObjectToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<String, Object>();
        Class<?> cla = obj.getClass();
        Field[] fields = cla.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String keyName = field.getName();
            Object value = field.get(obj);
            if (value == null)
                value = "";
            map.put(keyName, value);
        }
        return map;
    }
}
