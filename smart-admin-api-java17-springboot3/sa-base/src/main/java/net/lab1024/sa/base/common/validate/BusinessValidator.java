package net.lab1024.sa.base.common.validate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 业务验证器
 * 严格遵循repowiki规范：提供统一的业务数据验证功能
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Slf4j
@Component
public class BusinessValidator {

    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * 验证手机号
     *
     * @param phone 手机号
     * @return 验证结果
     */
    public boolean validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证邮箱
     *
     * @param email 邮箱
     * @return 验证结果
     */
    public boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证金额
     *
     * @param amount 金额
     * @param minAmount 最小金额（可为null）
     * @param maxAmount 最大金额（可为null）
     * @return 验证结果
     */
    public boolean validateAmount(BigDecimal amount, BigDecimal minAmount, BigDecimal maxAmount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            return false;
        }

        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            return false;
        }

        return true;
    }

    /**
     * 验证用户ID
     *
     * @param userId 用户ID
     * @return 验证结果
     */
    public boolean validateUserId(Long userId) {
        return userId != null && userId > 0;
    }

    /**
     * 验证设备ID
     *
     * @param deviceId 设备ID
     * @return 验证结果
     */
    public boolean validateDeviceId(Long deviceId) {
        return deviceId != null && deviceId > 0;
    }

    /**
     * 验证消费参数
     *
     * @param params 消费参数
     * @return 验证结果
     */
    public boolean validateConsumeParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return false;
        }

        // 检查必要参数
        if (!params.containsKey("userId") || !validateUserId((Long) params.get("userId"))) {
            return false;
        }

        if (!params.containsKey("amount") || !validateAmount((BigDecimal) params.get("amount"), null, null)) {
            return false;
        }

        if (!params.containsKey("payMethod") || params.get("payMethod") == null) {
            return false;
        }

        return true;
    }

    /**
     * 验证分页参数
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 验证结果
     */
    public boolean validatePageParams(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            return false;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 1000) {
            return false;
        }
        return true;
    }

    /**
     * 验证字符串长度
     *
     * @param str 字符串
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @return 验证结果
     */
    public boolean validateStringLength(String str, Integer minLength, Integer maxLength) {
        if (str == null) {
            return minLength == null || minLength <= 0;
        }

        int length = str.length();
        if (minLength != null && length < minLength) {
            return false;
        }
        if (maxLength != null && length > maxLength) {
            return false;
        }

        return true;
    }
}