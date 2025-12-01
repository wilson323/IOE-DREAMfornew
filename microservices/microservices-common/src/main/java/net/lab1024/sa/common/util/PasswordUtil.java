package net.lab1024.sa.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类
 * <p>
 * 提供密码加密、验证、生成等功能
 * 支持BCrypt加密、MD5加密、随机密码生成等
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
public class PasswordUtil {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final SecureRandom secureRandom = new SecureRandom();

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*_=+-";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;

    /**
     * 使用BCrypt加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        if (SmartStringUtil.isEmpty(rawPassword)) {
            return null;
        }
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (SmartStringUtil.isEmpty(rawPassword) || SmartStringUtil.isEmpty(encodedPassword)) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 生成随机密码
     *
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("密码长度不能小于4位");
        }

        StringBuilder password = new StringBuilder();

        // 确保包含各种类型的字符
        password.append(UPPERCASE.charAt(secureRandom.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(secureRandom.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(secureRandom.nextInt(SPECIAL_CHARS.length())));

        // 填充剩余长度
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(secureRandom.nextInt(ALL_CHARS.length())));
        }

        // 打乱字符顺序
        return shuffleString(password.toString());
    }

    /**
     * 生成默认长度的随机密码（8位）
     *
     * @return 随机密码
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(8);
    }

    /**
     * 生成数字验证码
     *
     * @param length 验证码长度
     * @return 验证码
     */
    public static String generateVerificationCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));
        }
        return code.toString();
    }

    /**
     * 生成6位数字验证码
     *
     * @return 验证码
     */
    public static String generateVerificationCode() {
        return generateVerificationCode(6);
    }

    /**
     * MD5加密（不建议用于密码存储，仅用于数据完整性校验）
     *
     * @param input 输入字符串
     * @return MD5哈希值
     */
    public static String md5(String input) {
        if (SmartStringUtil.isEmpty(input)) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5加密失败", e);
            return null;
        }
    }

    /**
     * SHA-256加密
     *
     * @param input 输入字符串
     * @return SHA-256哈希值
     */
    public static String sha256(String input) {
        if (SmartStringUtil.isEmpty(input)) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes());
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256加密失败", e);
            return null;
        }
    }

    /**
     * 生成盐值
     *
     * @param length 盐值长度
     * @return Base64编码的盐值
     */
    public static String generateSalt(int length) {
        byte[] salt = new byte[length];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 生成默认长度的盐值（16字节）
     *
     * @return Base64编码的盐值
     */
    public static String generateSalt() {
        return generateSalt(16);
    }

    /**
     * 验证密码强度
     *
     * @param password 密码
     * @return 密码强度等级（1-5，5为最强）
     */
    public static int checkPasswordStrength(String password) {
        if (SmartStringUtil.isEmpty(password)) {
            return 0;
        }

        int strength = 0;

        // 长度检查
        if (password.length() >= 8) {
            strength++;
        }
        if (password.length() >= 12) {
            strength++;
        }

        // 字符类型检查
        if (password.matches(".*[a-z].*")) { // 小写字母
            strength++;
        }
        if (password.matches(".*[A-Z].*")) { // 大写字母
            strength++;
        }
        if (password.matches(".*[0-9].*")) { // 数字
            strength++;
        }
        if (password.matches(".*[!@#$%^&*_=+\\-].*")) { // 特殊字符
            strength++;
        }

        return Math.min(strength, 5);
    }

    /**
     * 检查密码是否符合基本要求
     *
     * @param password 密码
     * @return 是否符合要求
     */
    public static boolean isPasswordValid(String password) {
        if (SmartStringUtil.isEmpty(password)) {
            return false;
        }

        // 基本要求：8-20位，包含大小写字母、数字和特殊字符
        return password.length() >= 8 && password.length() <= 20 &&
               password.matches(".*[a-z].*") && // 小写字母
               password.matches(".*[A-Z].*") && // 大写字母
               password.matches(".*[0-9].*") && // 数字
               password.matches(".*[!@#$%^&*_=+\\-].*"); // 特殊字符
    }

    /**
     * 打乱字符串顺序
     *
     * @param input 输入字符串
     * @return 打乱后的字符串
     */
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
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
     * 生成临时密码（8位，包含数字和大小写字母）
     *
     * @return 临时密码
     */
    public static String generateTempPassword() {
        String chars = UPPERCASE + LOWERCASE + DIGITS;
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return password.toString();
    }
}