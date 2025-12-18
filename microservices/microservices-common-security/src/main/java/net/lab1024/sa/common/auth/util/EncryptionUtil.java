package net.lab1024.sa.common.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * 加密工具类
 * <p>
 * 提供多种加密算法支持
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求：
 * - 使用Spring Security标准加密组件
 * - 提供MD5、SHA256、BCrypt等加密方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Component
public class EncryptionUtil {

    /**
     * MD5加密
     * <p>
     * 注意：MD5已不安全，仅用于非敏感数据摘要
     * </p>
     *
     * @param input 原始字符串
     * @return MD5加密后的十六进制字符串
     */
    public String md5(String input) {
        if (input == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(input.getBytes());
    }

    /**
     * SHA256加密
     * <p>
     * 用于数据摘要和哈希计算
     * Spring Boot 3.x中DigestUtils没有sha256相关方法，使用Java标准库MessageDigest实现
     * </p>
     *
     * @param input 原始字符串
     * @return SHA256加密后的十六进制字符串
     */
    public String sha256(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * BCrypt密码加密
     * <p>
     * 企业级密码加密标准，每次加密结果不同
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求：
     * - 每次调用时创建新的BCryptPasswordEncoder实例
     * </p>
     *
     * @param rawPassword 原始密码
     * @return BCrypt加密后的密码
     */
    public String bcryptEncode(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(rawPassword);
    }

    /**
     * BCrypt密码验证
     * <p>
     * 验证原始密码是否与加密后的密码匹配
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求：
     * - 每次调用时创建新的BCryptPasswordEncoder实例
     * </p>
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean bcryptMatches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPassword);
    }
}
