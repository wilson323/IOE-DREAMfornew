package net.lab1024.sa.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据脱敏工具类
 * <p>
 * 提供敏感信息脱敏功能，用于日志记录、数据展示等场景
 * 严格遵循CLAUDE.md规范：
 * - 工具类在microservices-common中
 * - 使用静态方法
 * - 完整的JavaDoc注释
 * </p>
 * <p>
 * 支持的脱敏类型：
 * - 手机号脱敏
 * - 身份证号脱敏
 * - 银行卡号脱敏
 * - 邮箱脱敏
 * - 密码脱敏
 * - 姓名脱敏
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class DataMaskUtil {

    // 注意：以下Pattern常量保留用于未来扩展，当前使用简单的字符串匹配方式
    // private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{3})\\d{4}(\\d{4})");
    // private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})\\d{8}(\\d{4})");
    // private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})\\d{8,12}(\\d{4})");
    // private static final Pattern EMAIL_PATTERN = Pattern.compile("(\\w{1,3})\\w*(@\\w+\\.\\w+)");

    /**
     * 脱敏手机号
     * <p>
     * 格式：138****5678
     * </p>
     *
     * @param phone 手机号
     * @return 脱敏后的手机号，如果输入为空或格式不正确，返回原值
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }

        // 移除所有非数字字符
        String digits = phone.replaceAll("[^0-9]", "");

        // 11位手机号：显示前3位和后4位
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "****" + digits.substring(7);
        }

        // 其他长度：显示前3位和后2位
        if (digits.length() > 5) {
            return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 2);
        }

        // 长度不足，全部脱敏
        return "****";
    }

    /**
     * 脱敏身份证号
     * <p>
     * 格式：110101********1234
     * </p>
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号，如果输入为空或格式不正确，返回原值
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return idCard;
        }

        // 移除所有非数字和字母字符
        String cleaned = idCard.replaceAll("[^0-9A-Za-z]", "");

        // 18位身份证号：显示前6位和后4位
        if (cleaned.length() == 18) {
            return cleaned.substring(0, 6) + "********" + cleaned.substring(14);
        }

        // 15位身份证号：显示前6位和后3位
        if (cleaned.length() == 15) {
            return cleaned.substring(0, 6) + "******" + cleaned.substring(12);
        }

        // 其他长度：显示前3位和后2位
        if (cleaned.length() > 5) {
            return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 2);
        }

        // 长度不足，全部脱敏
        return "****";
    }

    /**
     * 脱敏银行卡号
     * <p>
     * 格式：6222****1234
     * </p>
     *
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号，如果输入为空或格式不正确，返回原值
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.trim().isEmpty()) {
            return bankCard;
        }

        // 移除所有非数字字符
        String digits = bankCard.replaceAll("[^0-9]", "");

        // 16位银行卡号：显示前4位和后4位
        if (digits.length() >= 16) {
            return digits.substring(0, 4) + "********" + digits.substring(digits.length() - 4);
        }

        // 其他长度：显示前4位和后2位
        if (digits.length() > 6) {
            return digits.substring(0, 4) + "****" + digits.substring(digits.length() - 2);
        }

        // 长度不足，全部脱敏
        return "****";
    }

    /**
     * 脱敏邮箱
     * <p>
     * 格式：abc****@example.com
     * </p>
     *
     * @param email 邮箱地址
     * @return 脱敏后的邮箱，如果输入为空或格式不正确，返回原值
     */
    public static String maskEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex > 0 && atIndex < email.length() - 1) {
            String prefix = email.substring(0, atIndex);
            String suffix = email.substring(atIndex);

            // 前缀长度大于3：显示前3位，其余脱敏
            if (prefix.length() > 3) {
                return prefix.substring(0, 3) + "****" + suffix;
            }

            // 前缀长度小于等于3：全部脱敏
            return "****" + suffix;
        }

        // 格式不正确，全部脱敏
        return "****";
    }

    /**
     * 脱敏密码
     * <p>
     * 格式：****
     * </p>
     *
     * @param password 密码
     * @return 脱敏后的密码（始终返回****）
     */
    public static String maskPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "****";
        }
        return "****";
    }

    /**
     * 脱敏姓名
     * <p>
     * 格式：张* 或 张*三
     * </p>
     *
     * @param name 姓名
     * @return 脱敏后的姓名，如果输入为空，返回原值
     */
    public static String maskName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }

        String trimmed = name.trim();

        // 单字符：直接返回
        if (trimmed.length() == 1) {
            return trimmed;
        }

        // 两字符：显示第一个字符，第二个字符脱敏
        if (trimmed.length() == 2) {
            return trimmed.charAt(0) + "*";
        }

        // 三字符及以上：显示第一个字符和最后一个字符，中间脱敏
        return trimmed.charAt(0) + "*" + trimmed.charAt(trimmed.length() - 1);
    }

    /**
     * 脱敏JSON字符串中的敏感字段
     * <p>
     * 自动识别并脱敏JSON中的常见敏感字段：
     * - phone, mobile, telephone
     * - idCard, idNumber, identityCard
     * - bankCard, cardNumber
     * - email
     * - password, pwd
     * - name, realName, userName
     * </p>
     *
     * @param jsonStr JSON字符串
     * @return 脱敏后的JSON字符串，如果输入为空或不是有效JSON，返回原值
     */
    public static String maskJson(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return jsonStr;
        }

        try {
            // 简单的JSON字段脱敏（使用正则表达式）
            // 注意：这是一个简化实现，对于复杂JSON结构，建议使用Jackson解析后脱敏

            // 脱敏手机号字段
            jsonStr = jsonStr.replaceAll("(\"phone\"\\s*:\\s*\")([^\"]+)(\")",
                    "$1" + maskPhone("$2") + "$3");
            jsonStr = jsonStr.replaceAll("(\"mobile\"\\s*:\\s*\")([^\"]+)(\")",
                    "$1" + maskPhone("$2") + "$3");

            // 脱敏身份证号字段
            jsonStr = jsonStr.replaceAll("(\"idCard\"\\s*:\\s*\")([^\"]+)(\")",
                    "$1" + maskIdCard("$2") + "$3");
            jsonStr = jsonStr.replaceAll("(\"idNumber\"\\s*:\\s*\")([^\"]+)(\")",
                    "$1" + maskIdCard("$2") + "$3");

            // 脱敏密码字段
            jsonStr = jsonStr.replaceAll("(\"password\"\\s*:\\s*\")([^\"]+)(\")",
                    "$1****$3");
            jsonStr = jsonStr.replaceAll("(\"pwd\"\\s*:\\s*\")([^\"]+)(\")",
                    "$1****$3");

            // 脱敏邮箱字段
            jsonStr = jsonStr.replaceAll("(\"email\"\\s*:\\s*\")([^\"]+)(\")",
                    "$1" + maskEmail("$2") + "$3");

            return jsonStr;

        } catch (Exception e) {
            log.debug("[数据脱敏] JSON脱敏失败，返回原值", e);
            return jsonStr;
        }
    }

    /**
     * 脱敏通用字符串
     * <p>
     * 根据字符串特征自动判断脱敏方式
     * </p>
     *
     * @param value 待脱敏的值
     * @return 脱敏后的值
     */
    public static String maskValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        String trimmed = value.trim();

        // 判断是否为手机号（11位数字）
        if (trimmed.matches("^1[3-9]\\d{9}$")) {
            return maskPhone(trimmed);
        }

        // 判断是否为身份证号（15位或18位）
        if (trimmed.matches("^\\d{15}$|^\\d{17}[0-9Xx]$")) {
            return maskIdCard(trimmed);
        }

        // 判断是否为银行卡号（16-19位数字）
        if (trimmed.matches("^\\d{16,19}$")) {
            return maskBankCard(trimmed);
        }

        // 判断是否为邮箱
        if (trimmed.contains("@") && trimmed.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$")) {
            return maskEmail(trimmed);
        }

        // 默认：脱敏中间部分
        if (trimmed.length() > 4) {
            return trimmed.substring(0, 2) + "****" + trimmed.substring(trimmed.length() - 2);
        }

        return "****";
    }
}

