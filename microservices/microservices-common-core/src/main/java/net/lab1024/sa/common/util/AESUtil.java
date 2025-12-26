package net.lab1024.sa.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * AES加密工具类
 * <p>
 * 提供AES加密解密功能，支持静态方法和实例方法调用
 * </p>
 */
public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String DEFAULT_KEY = "ioedream2025key"; // 默认密钥

    /**
     * 静态方法版本 - 使用默认密钥
     */
    public static String encrypt(String data) {
        return encrypt(data, DEFAULT_KEY);
    }

    /**
     * 静态方法版本 - 使用默认密钥
     */
    public static String decrypt(String data) {
        return decrypt(data, DEFAULT_KEY);
    }

    /**
     * 静态方法版本 - 指定密钥
     */
    public static String encrypt(String data, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * 静态方法版本 - 指定密钥
     */
    public static String decrypt(String data, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}

