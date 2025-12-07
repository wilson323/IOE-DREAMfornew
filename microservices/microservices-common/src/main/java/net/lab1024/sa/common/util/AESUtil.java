package net.lab1024.sa.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

/**
 * AES加密解密工具类
 * <p>
 * 用于敏感数据的加密和解密
 * 严格遵循CLAUDE.md规范:
 * - 工具类使用静态方法
 * - 完整的异常处理
 * - 详细的日志记录
 * </p>
 * <p>
 * 企业级特性：
 * - AES-128-ECB加密算法
 * - Base64编码输出
 * - 密钥管理（从环境变量或配置中心获取）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class AESUtil {

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加密模式
     */
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 默认密钥（生产环境必须从环境变量或配置中心获取）
     */
    private static final String DEFAULT_KEY = "IOE-DREAM-DEFAULT-KEY-32-BYTES-LONG!";

    /**
     * 获取加密密钥
     * <p>
     * 优先从环境变量获取，否则使用默认密钥
     * 生产环境必须配置CONFIG_ENCRYPT_KEY环境变量
     * </p>
     *
     * @return 加密密钥
     */
    private static String getSecretKey() {
        String secretKey = System.getenv("CONFIG_ENCRYPT_KEY");
        if (secretKey == null || secretKey.isEmpty()) {
            log.warn("[AES加密] 使用默认加密密钥，生产环境请配置CONFIG_ENCRYPT_KEY环境变量");
            return DEFAULT_KEY;
        }
        return secretKey;
    }

    /**
     * AES加密
     * <p>
     * 使用AES-128-ECB算法加密数据
     * 输出Base64编码字符串
     * </p>
     *
     * @param plaintext 明文
     * @return 密文（Base64编码）
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            String secretKey = getSecretKey();
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            log.error("[AES加密] 加密失败", e);
            throw new RuntimeException("AES加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * AES解密
     * <p>
     * 使用AES-128-ECB算法解密数据
     * 输入Base64编码字符串
     * </p>
     *
     * @param ciphertext 密文（Base64编码）
     * @return 明文
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }

        try {
            String secretKey = getSecretKey();
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));

            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("[AES解密] 解密失败", e);
            throw new RuntimeException("AES解密失败: " + e.getMessage(), e);
        }
    }
}
