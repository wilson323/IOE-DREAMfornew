package net.lab1024.sa.common.auth.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * 密码加密工具类
 * <p>
 * 企业级密码加密工具，使用BCrypt算法
 * 严格遵循CLAUDE.md规范：
 * - 纯Java工具类，不使用Spring注解
 * - 提供静态方法供Manager类调用
 * - 不依赖Spring容器
 * </p>
 * <p>
 * 功能：
 * - BCrypt密码加密
 * - BCrypt密码验证
 * - 密码强度检查
 * </p>
 * <p>
 * 企业级特性：
 * - 使用BCrypt算法（强度10）
 * - 自动生成盐值
 * - 防止彩虹表攻击
 * - 支持密码升级（旧密码格式兼容）
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class PasswordUtil {

    /**
     * BCrypt加密强度（轮数）
     * 10表示2^10=1024轮，平衡安全性和性能
     */
    private static final int BCRYPT_ROUNDS = 10;

    /**
     * 密码最小长度
     */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * 密码最大长度
     */
    private static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * 加密密码
     * <p>
     * 使用BCrypt算法加密密码，自动生成盐值
     * BCrypt哈希值格式：$2a$10$salt+hash（60字符）
     * </p>
     *
     * @param rawPassword 原始密码（明文）
     * @return 加密后的密码（BCrypt哈希值）
     * @throws IllegalArgumentException 当密码为空或长度不符合要求时抛出
     *
     * @example
     * <pre>
     * String rawPassword = "myPassword123";
     * String encrypted = PasswordUtil.encryptPassword(rawPassword);
     * // 返回: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
     * </pre>
     */
    public static String encryptPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        if (rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("密码长度不能少于" + MIN_PASSWORD_LENGTH + "个字符");
        }

        if (rawPassword.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("密码长度不能超过" + MAX_PASSWORD_LENGTH + "个字符");
        }

        try {
            // 使用BCrypt加密，自动生成盐值
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
            log.debug("[密码加密] 密码加密成功，长度: {}", hashedPassword.length());
            return hashedPassword;
        } catch (Exception e) {
            log.error("[密码加密] 密码加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 验证密码
     * <p>
     * 使用BCrypt算法验证密码是否匹配
     * BCrypt会自动从哈希值中提取盐值进行验证
     * </p>
     *
     * @param rawPassword 原始密码（明文）
     * @param hashedPassword 加密后的密码（BCrypt哈希值）
     * @return true-密码匹配，false-密码不匹配
     * @throws IllegalArgumentException 当参数为空时抛出
     *
     * @example
     * <pre>
     * String rawPassword = "myPassword123";
     * String hashedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
     * boolean isValid = PasswordUtil.verifyPassword(rawPassword, hashedPassword);
     * // 返回: true
     * </pre>
     */
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            log.warn("[密码验证] 原始密码为空");
            return false;
        }

        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            log.warn("[密码验证] 加密密码为空");
            return false;
        }

        // 检查是否为BCrypt格式（以$2a$、$2b$或$2y$开头）
        if (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2b$")
                && !hashedPassword.startsWith("$2y$")) {
            log.warn("[密码验证] 加密密码格式不正确，不是BCrypt格式");
            return false;
        }

        try {
            // 使用BCrypt验证密码
            boolean matches = BCrypt.checkpw(rawPassword, hashedPassword);
            if (matches) {
                log.debug("[密码验证] 密码验证成功");
            } else {
                log.warn("[密码验证] 密码验证失败");
            }
            return matches;
        } catch (Exception e) {
            log.error("[密码验证] 密码验证异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证密码（兼容旧密码格式）
     * <p>
     * 支持多种密码格式验证：
     * - BCrypt格式（$2a$10$...）
     * - 旧格式（如果系统有旧密码格式，可以在这里扩展）
     * </p>
     *
     * @param rawPassword 原始密码（明文）
     * @param storedPassword 存储的密码（可能是BCrypt或其他格式）
     * @param salt 盐值（可选，用于旧格式密码）
     * @return true-密码匹配，false-密码不匹配
     *
     * @example
     * <pre>
     * // BCrypt格式
     * boolean isValid = PasswordUtil.verifyPasswordWithSalt("password", "$2a$10$...", null);
     *
     * // 旧格式（如果需要）
     * boolean isValid = PasswordUtil.verifyPasswordWithSalt("password", "oldHash", "salt");
     * </pre>
     */
    public static boolean verifyPasswordWithSalt(String rawPassword, String storedPassword, String salt) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        // 优先使用BCrypt验证
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")) {
            return verifyPassword(rawPassword, storedPassword);
        }

        // 兼容旧格式（如果需要，可以在这里扩展）
        // 例如：MD5、SHA256等旧格式的验证逻辑
        log.warn("[密码验证] 检测到旧格式密码，建议升级为BCrypt格式");
        return false;
    }

    /**
     * 检查密码强度
     * <p>
     * 验证密码是否符合安全要求：
     * - 长度要求
     * - 复杂度要求（可选）
     * </p>
     *
     * @param password 密码
     * @return true-密码强度符合要求，false-不符合要求
     *
     * @example
     * <pre>
     * boolean isStrong = PasswordUtil.isPasswordStrong("MyP@ssw0rd123");
     * // 返回: true
     * </pre>
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }

        // 可以在这里添加复杂度检查
        // 例如：必须包含大小写字母、数字、特殊字符等
        return true;
    }

    /**
     * 生成随机密码
     * <p>
     * 生成符合安全要求的随机密码
     * </p>
     *
     * @param length 密码长度（默认16）
     * @return 随机密码
     *
     * @example
     * <pre>
     * String randomPassword = PasswordUtil.generateRandomPassword(16);
     * // 返回: "aB3$dE5fG7hI9jK1"
     * </pre>
     */
    public static String generateRandomPassword(int length) {
        if (length < MIN_PASSWORD_LENGTH || length > MAX_PASSWORD_LENGTH) {
            length = 16; // 默认长度
        }

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}

