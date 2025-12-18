package net.lab1024.sa.common.util;

import java.nio.charset.StandardCharsets;

/**
 * AES加密工具类（适配器）
 * <p>
 * 提供AES加密的便捷方法
 * 注意：此工具类使用AESUtil，密钥从环境变量获取（CONFIG_ENCRYPT_KEY）
 * 如果需要指定密钥，请直接使用AESUtil实例
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public class SmartAESUtil {

    private SmartAESUtil() {
        // 私有构造，禁止实例化
    }

    /**
     * Base64编码的AES加密
     * <p>
     * 对数据进行AES加密（密钥从环境变量CONFIG_ENCRYPT_KEY获取）
     * 输出Base64编码的加密数据
     * </p>
     *
     * @param data 原始数据
     * @param key 加密密钥（当前实现忽略此参数，使用环境变量密钥）
     * @return Base64编码的加密数据
     */
    public static String encryptBase64(byte[] data, String key) {
        if (data == null || data.length == 0) {
            return null;
        }

        // 使用AESUtil进行加密（密钥从环境变量获取）
        // 注意：当前AESUtil实现不支持自定义密钥参数
        // 如需指定密钥，应直接使用AESUtil实例并修改密钥获取逻辑
        AESUtil aesUtil = new AESUtil();
        String plaintext = new String(data, StandardCharsets.UTF_8);
        return aesUtil.encrypt(plaintext);
    }

    /**
     * Base64解码的AES解密
     * <p>
     * 对Base64编码的加密数据进行解密（密钥从环境变量CONFIG_ENCRYPT_KEY获取）
     * </p>
     *
     * @param encryptedData Base64编码的加密数据
     * @param key 解密密钥（当前实现忽略此参数，使用环境变量密钥）
     * @return 解密后的数据
     */
    public static byte[] decryptBase64(String encryptedData, String key) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return new byte[0];
        }

        // 使用AESUtil进行解密（密钥从环境变量获取）
        AESUtil aesUtil = new AESUtil();
        String decrypted = aesUtil.decrypt(encryptedData);
        return decrypted != null ? decrypted.getBytes(StandardCharsets.UTF_8) : new byte[0];
    }
}

