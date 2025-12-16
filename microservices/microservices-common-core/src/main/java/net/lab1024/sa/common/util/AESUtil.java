package net.lab1024.sa.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.crypto.Cipher;
import jakarta.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.SystemException;

/**
 * AES加密解密工具类
 * <p>
 * 用于敏感数据的加密和解密
 * 严格遵循CLAUDE.md规范:
 * - 工具类使用实例方法（通过Bean注入使用）
 * - 完整的异常处理
 * - 详细的日志记录
 * </p>
 * <p>
 * **Spring Boot标准方案说明**:
 * - 使用Java标准库`javax.crypto`（Java 17/21标准库，无需迁移）
 * - Spring Security Crypto提供更高级的加密功能（可选替代方案）
 * - 当前实现使用标准库，满足基本加密需求
 * - 如需更高级功能（如密钥轮换、加密算法升级），可考虑迁移到Spring Security Crypto
 * </p>
 * <p>
 * 企业级特性：
 * - AES-128-ECB加密算法
 * - Base64编码输出
 * - 密钥管理（从环境变量或配置中心获取）
 * - 生产环境必须配置CONFIG_ENCRYPT_KEY环境变量
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-01-30 更新注释，说明javax.crypto为标准库，可保留；说明与Spring Security Crypto的关系
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
            throw new SystemException("AES_ENCRYPT_ERROR", "AES加密失败: " + e.getMessage(), e);
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
            throw new SystemException("AES_DECRYPT_ERROR", "AES解密失败: " + e.getMessage(), e);
        }
    }
}
