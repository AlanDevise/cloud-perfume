package com.alandevise.GeneralServer.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * @Filename: AESUtils.java
 * @Package: com.alandevise.util
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:10
 */

public class AESUtils {
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "aes-key-lima1995"; // 密钥

    /**
     * 加密
     */
    public static String encrypt(String value) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 解密
     */
    public static String decrypt(String value) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(value);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    public static void main(String[] args) throws Exception {
//        String encryptString = encrypt("6aKenQrFc0YeyOJugPmIRz6rKmijRtpxEVhyYjsL20U=");
//        System.out.println("加密后的密码为：" + encryptString);
        String decryptString = decrypt("L9m8qAbr+N/C8wKGZNQ3QQ==");
        System.out.println("解密后的密码为：" + decryptString);
    }
}
