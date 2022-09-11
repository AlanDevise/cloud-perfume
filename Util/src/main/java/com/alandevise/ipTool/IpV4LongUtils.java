package com.alandevise.ipTool;

/**
 * @Filename: IpV4LongUtils.java
 * @Package: com.alandevise.ipTool
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月03日 09:06
 */

public class IpV4LongUtils {

    private static final IpV4LongUtils INSTANCE = new IpV4LongUtils();

    public static IpV4LongUtils getInstance(){
        return INSTANCE;
    }

    /**
     * 把字符串IPV4转换成long
     *
     * @param ipStr 字符串IP
     * @return IP对应的long值
     */
    private static long ipV42Long(String ipStr) {
        String[] ip = ipStr.split("\\.");
        return (Long.parseLong(ip[0]) << 24) + (Long.parseLong(ip[1]) << 16)
                + (Long.parseLong(ip[2]) << 8) + Long.parseLong(ip[3]);
    }

    /**
     * 把IPV4的long值转换成字符串
     *
     * @param ipLong IP的long值
     * @return long值对应的字符串
     */
    private static String long2IpV4(long ipLong) {
        return (ipLong >>> 24) + "." +
                ((ipLong >>> 16) & 0xFF) + "." +
                ((ipLong >>> 8) & 0xFF) + "." +
                (ipLong & 0xFF);
    }
}
