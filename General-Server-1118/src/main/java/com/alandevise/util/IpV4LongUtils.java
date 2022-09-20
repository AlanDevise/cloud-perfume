package com.alandevise.util;

/**
 * @Filename: IpV4LongUtils.java
 * @Package: com.alandevise.util
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-20 15:41
 */

public class IpV4LongUtils {
    /**
     * 把字符串IPV4转换成long
     *
     * @param ipStr 字符串IP
     * @return IP对应的long值
     */
    public static long ipV42Long(String ipStr) {
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
    public static String long2IpV4(long ipLong) {
        return (ipLong >>> 24) + "." +
                ((ipLong >>> 16) & 0xFF) + "." +
                ((ipLong >>> 8) & 0xFF) + "." +
                (ipLong & 0xFF);
    }

    // 测试实例
    public static void main(String[] args) {
        System.out.println(ipV42Long("192.168.79.51"));
        System.out.println(long2IpV4(Long.parseLong("3232255795")));
    }
}

