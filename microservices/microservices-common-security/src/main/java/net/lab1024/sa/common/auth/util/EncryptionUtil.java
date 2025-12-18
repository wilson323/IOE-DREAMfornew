package net.lab1024.sa.common.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

/**
 * 加密工具类
 * <p>
 * 提供多种加密算法支持
 * 严格遵循CLAUDE.md规范：
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

    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数注入PasswordEncoder
     * <p>
     * 使用BCryptPasswordEncoder作为默认密码编码器
     * </p>
     */
    public EncryptionUtil() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

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
     * </p>
     *
     * @param input 原始字符串
     * @return SHA256加密后的十六进制字符串
     */
    public String sha256(String input) {
        if (input == null) {
            return null;
        }
        return DigestUtils.sha256Hex(input);
    }

    /**
     * BCrypt密码加密
     * <p>
     * 企业级密码加密标准，每次加密结果不同
     * </p>
     *
     * @param rawPassword 原始密码
     * @return BCrypt加密后的密码
     */
    public String bcryptEncode(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * BCrypt密码验证
     * <p>
     * 验证原始密码是否与加密后的密码匹配
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
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
