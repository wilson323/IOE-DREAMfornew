package net.lab1024.sa.common.auth.util;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.SystemException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * TOTP（Time-based One-Time Password）工具类
 * <p>
 * 企业级TOTP认证工具，基于RFC 6238标准
 * 严格遵循CLAUDE.md规范：
 * - 纯Java工具类，不使用Spring注解
 * - 提供静态方法供Manager类调用
 * - 不依赖Spring容器
 * </p>
 * <p>
 * 功能：
 * - TOTP代码生成
 * - TOTP代码验证
 * - 支持时间窗口容差
 * - 兼容Google Authenticator等标准TOTP应用
 * </p>
 * <p>
 * 企业级特性：
 * - 基于RFC 6238标准实现
 * - 使用HMAC-SHA1算法
 * - 支持时间窗口验证（防止时钟偏差）
 * - 完整的异常处理
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class TotpUtil {

    /**
     * TOTP算法名称（HMAC-SHA1）
     */
    private static final String TOTP_ALGORITHM = "HmacSHA1";

    /**
     * TOTP时间步长（秒）
     * <p>
     * 默认30秒，与Google Authenticator等标准应用一致
     * </p>
     */
    private static final int TIME_STEP = 30;

    /**
     * TOTP代码长度（位数）
     * <p>
     * 默认6位，与Google Authenticator等标准应用一致
     * </p>
     */
    private static final int CODE_LENGTH = 6;

    /**
     * 默认时间窗口容差
     * <p>
     * 允许前后1个时间窗口的偏差（±30秒）
     * </p>
     */
    private static final int DEFAULT_TIME_WINDOW = 1;

    /**
     * 生成TOTP代码
     * <p>
     * 基于当前时间和密钥生成一次性密码
     * 算法：TOTP = HOTP(K, T) 其中 T = floor((Current Unix Time - T0) / X)
     * </p>
     *
     * @param secretKey Base32编码的密钥
     * @return 6位TOTP代码（字符串格式）
     * @throws IllegalArgumentException 当密钥为空或格式不正确时抛出
     *
     * @example
     * <pre>
     * String secretKey = "JBSWY3DPEHPK3PXP";
     * String totpCode = TotpUtil.generateTotp(secretKey);
     * // 返回: "123456"
     * </pre>
     */
    public static String generateTotp(String secretKey) {
        return generateTotp(secretKey, System.currentTimeMillis() / 1000);
    }

    /**
     * 生成TOTP代码（指定时间戳）
     * <p>
     * 用于测试或特定时间点的代码生成
     * </p>
     *
     * @param secretKey Base32编码的密钥
     * @param timestamp Unix时间戳（秒）
     * @return 6位TOTP代码（字符串格式）
     * @throws IllegalArgumentException 当密钥为空或格式不正确时抛出
     */
    public static String generateTotp(String secretKey, long timestamp) {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("TOTP密钥不能为空");
        }

        try {
            // 1. 解码Base32密钥
            byte[] keyBytes = decodeBase32(secretKey);

            // 2. 计算时间步数
            long timeStep = timestamp / TIME_STEP;

            // 3. 生成HOTP代码
            int hotpCode = generateHotp(keyBytes, timeStep);

            // 4. 格式化为6位数字字符串
            String totpCode = String.format("%0" + CODE_LENGTH + "d", hotpCode);

            log.debug("[TOTP工具] 生成TOTP代码成功，时间步: {}", timeStep);
            return totpCode;

        } catch (Exception e) {
            log.error("[TOTP工具] 生成TOTP代码失败: {}", e.getMessage(), e);
            throw new SystemException("TOTP_GENERATE_ERROR", "生成TOTP代码失败", e);
        }
    }

    /**
     * 验证TOTP代码
     * <p>
     * 验证用户输入的TOTP代码是否正确
     * 支持时间窗口容差，允许前后N个时间窗口的偏差
     * </p>
     *
     * @param secretKey Base32编码的密钥
     * @param userCode 用户输入的TOTP代码
     * @return true-验证通过，false-验证失败
     *
     * @example
     * <pre>
     * String secretKey = "JBSWY3DPEHPK3PXP";
     * String userCode = "123456";
     * boolean isValid = TotpUtil.verifyTotp(secretKey, userCode);
     * // 返回: true（如果代码正确）
     * </pre>
     */
    public static boolean verifyTotp(String secretKey, String userCode) {
        return verifyTotp(secretKey, userCode, DEFAULT_TIME_WINDOW);
    }

    /**
     * 验证TOTP代码（指定时间窗口容差）
     * <p>
     * 允许指定时间窗口容差，用于处理时钟偏差
     * </p>
     *
     * @param secretKey Base32编码的密钥
     * @param userCode 用户输入的TOTP代码
     * @param timeWindow 时间窗口容差（允许前后N个时间窗口）
     * @return true-验证通过，false-验证失败
     */
    public static boolean verifyTotp(String secretKey, String userCode, int timeWindow) {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            log.warn("[TOTP工具] 密钥为空");
            return false;
        }

        if (userCode == null || userCode.trim().isEmpty()) {
            log.warn("[TOTP工具] 用户代码为空");
            return false;
        }

        // 规范化用户代码（去除空格）
        userCode = userCode.trim();

        // 验证代码长度
        if (userCode.length() != CODE_LENGTH) {
            log.warn("[TOTP工具] 代码长度不正确: {}", userCode.length());
            return false;
        }

        try {
            long currentTimestamp = System.currentTimeMillis() / 1000;

            // 在当前时间窗口及前后N个时间窗口内验证
            for (int i = -timeWindow; i <= timeWindow; i++) {
                long testTimestamp = currentTimestamp + (i * TIME_STEP);
                String expectedCode = generateTotp(secretKey, testTimestamp);

                if (userCode.equals(expectedCode)) {
                    log.debug("[TOTP工具] TOTP验证成功，时间窗口偏移: {}", i);
                    return true;
                }
            }

            log.warn("[TOTP工具] TOTP验证失败，用户代码: {}", userCode);
            return false;

        } catch (Exception e) {
            log.error("[TOTP工具] TOTP验证异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成HOTP代码
     * <p>
     * 基于RFC 4226标准实现HOTP（HMAC-based One-Time Password）
     * </p>
     *
     * @param keyBytes 密钥字节数组
     * @param counter 计数器（时间步数）
     * @return HOTP代码（整数）
     */
    private static int generateHotp(byte[] keyBytes, long counter) {
        try {
            // 1. 将计数器转换为8字节数组（大端序）
            byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();

            // 2. 计算HMAC-SHA1
            Mac mac = Mac.getInstance(TOTP_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, TOTP_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmac = mac.doFinal(counterBytes);

            // 3. 动态截取（Dynamic Truncation）
            int offset = hmac[hmac.length - 1] & 0x0F;
            int binary = ((hmac[offset] & 0x7F) << 24)
                    | ((hmac[offset + 1] & 0xFF) << 16)
                    | ((hmac[offset + 2] & 0xFF) << 8)
                    | (hmac[offset + 3] & 0xFF);

            // 4. 取模得到6位数字
            int otp = binary % (int) Math.pow(10, CODE_LENGTH);

            return otp;

        } catch (NoSuchAlgorithmException e) {
            log.error("[TOTP工具] HMAC-SHA1算法不可用", e);
            throw new SystemException("TOTP_HMAC_ALGORITHM_ERROR", "HMAC-SHA1算法不可用", e);
        } catch (InvalidKeyException e) {
            log.error("[TOTP工具] 无效的密钥", e);
            throw new SystemException("TOTP_INVALID_KEY_ERROR", "无效的密钥", e);
        }
    }

    /**
     * Base32解码
     * <p>
     * 将Base32编码的字符串解码为字节数组
     * 支持标准Base32字符集（A-Z, 2-7）
     * </p>
     *
     * @param base32 Base32编码的字符串
     * @return 解码后的字节数组
     */
    private static byte[] decodeBase32(String base32) {
        // Base32字符集
        String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        // 去除空格和连字符
        base32 = base32.replaceAll("[\\s-]", "").toUpperCase();

        if (base32.isEmpty()) {
            throw new IllegalArgumentException("Base32字符串不能为空");
        }

        // 计算输出长度
        int outputLength = (base32.length() * 5) / 8;
        byte[] output = new byte[outputLength];

        int buffer = 0;
        int bitsRemaining = 0;
        int outputIndex = 0;

        for (char c : base32.toCharArray()) {
            int value = base32Chars.indexOf(c);
            if (value == -1) {
                throw new IllegalArgumentException("无效的Base32字符: " + c);
            }

            buffer = (buffer << 5) | value;
            bitsRemaining += 5;

            if (bitsRemaining >= 8) {
                output[outputIndex++] = (byte) (buffer >> (bitsRemaining - 8));
                bitsRemaining -= 8;
            }
        }

        return output;
    }

    /**
     * 生成TOTP密钥（Base32格式）
     * <p>
     * 生成一个随机的Base32编码密钥，用于用户绑定TOTP应用
     * </p>
     *
     * @param length 密钥长度（字节数，默认16字节）
     * @return Base32编码的密钥字符串
     *
     * @example
     * <pre>
     * String secretKey = TotpUtil.generateSecretKey(16);
     * // 返回: "JBSWY3DPEHPK3PXP"
     * </pre>
     */
    public static String generateSecretKey(int length) {
        if (length < 16) {
            length = 16; // 最小16字节
        }

        java.util.Random random = new java.util.Random();
        byte[] keyBytes = new byte[length];
        random.nextBytes(keyBytes);

        return encodeBase32(keyBytes);
    }

    /**
     * 生成TOTP密钥（默认长度）
     * <p>
     * 使用默认16字节长度生成密钥
     * </p>
     *
     * @return Base32编码的密钥字符串
     */
    public static String generateSecretKey() {
        return generateSecretKey(16);
    }

    /**
     * Base32编码
     * <p>
     * 将字节数组编码为Base32字符串
     * </p>
     *
     * @param bytes 字节数组
     * @return Base32编码的字符串
     */
    private static String encodeBase32(byte[] bytes) {
        String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder result = new StringBuilder();

        int buffer = 0;
        int bitsRemaining = 0;

        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsRemaining += 8;

            while (bitsRemaining >= 5) {
                int index = (buffer >> (bitsRemaining - 5)) & 0x1F;
                result.append(base32Chars.charAt(index));
                bitsRemaining -= 5;
            }
        }

        if (bitsRemaining > 0) {
            int index = (buffer << (5 - bitsRemaining)) & 0x1F;
            result.append(base32Chars.charAt(index));
        }

        return result.toString();
    }

    /**
     * 生成TOTP绑定URI
     * <p>
     * 生成用于Google Authenticator等应用扫描的URI
     * 格式：otpauth://totp/{issuer}:{account}?secret={secret}&issuer={issuer}
     * </p>
     *
     * @param account 账户标识（通常是用户名或邮箱）
     * @param secretKey Base32编码的密钥
     * @param issuer 发行者名称（通常是应用名称）
     * @return TOTP绑定URI
     *
     * @example
     * <pre>
     * String uri = TotpUtil.generateTotpUri("user@example.com", "JBSWY3DPEHPK3PXP", "IOE-DREAM");
     * // 返回: "otpauth://totp/IOE-DREAM:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=IOE-DREAM"
     * </pre>
     */
    public static String generateTotpUri(String account, String secretKey, String issuer) {
        try {
            String encodedAccount = java.net.URLEncoder.encode(account, "UTF-8");
            String encodedIssuer = java.net.URLEncoder.encode(issuer, "UTF-8");

            return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    encodedIssuer, encodedAccount, secretKey, encodedIssuer);
        } catch (java.io.UnsupportedEncodingException e) {
            log.error("[TOTP工具] URI编码失败", e);
            throw new SystemException("TOTP_URI_ENCODE_ERROR", "URI编码失败", e);
        }
    }
}

