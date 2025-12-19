package net.lab1024.sa.common.util;

import java.util.Base64;

/**
 * Base64编码解码工具类
 * <p>
 * 基于Java标准库Base64实现
 * 严格遵循CLAUDE.md规范：使用标准库而非自定义实现
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public class SmartBase64Util {

    private SmartBase64Util() {
        // 私有构造，禁止实例化
    }

    /**
     * Base64编码
     *
     * @param data 原始数据
     * @return Base64编码后的字符串
     */
    public static String encode(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Base64编码（字符串输入）
     *
     * @param data 原始字符串
     * @return Base64编码后的字符串
     */
    public static String encode(String data) {
        if (data == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    /**
     * Base64解码为字节数组
     *
     * @param encoded Base64编码的字符串
     * @return 解码后的字节数组
     */
    public static byte[] decodeToBytes(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return new byte[0];
        }
        try {
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 encoded string: " + encoded, e);
        }
    }

    /**
     * Base64解码为字符串
     *
     * @param encoded Base64编码的字符串
     * @return 解码后的字符串
     */
    public static String decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return null;
        }
        byte[] decoded = decodeToBytes(encoded);
        return new String(decoded);
    }

    /**
     * URL安全的Base64编码
     *
     * @param data 原始数据
     * @return URL安全的Base64编码字符串
     */
    public static String encodeUrlSafe(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getUrlEncoder().encodeToString(data);
    }

    /**
     * URL安全的Base64解码
     *
     * @param encoded URL安全的Base64编码字符串
     * @return 解码后的字节数组
     */
    public static byte[] decodeUrlSafe(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return new byte[0];
        }
        try {
            return Base64.getUrlDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid URL-safe Base64 encoded string: " + encoded, e);
        }
    }
}


