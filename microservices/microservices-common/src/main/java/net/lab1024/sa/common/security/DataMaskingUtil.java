package net.lab1024.sa.common.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 数据脱敏工具类
 * <p>
 * 提供各种敏感数据的脱敏功能，确保数据安全
 * 支持手机号、身份证、邮箱、银行卡等常见敏感信息脱敏
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class DataMaskingUtil {

    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{3})\\d{4}(\\d{4})");

    // 身份证正则表达式
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})\\d{8}(\\d{4})");

    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(\\w{1,3})\\w*@(\\w+)");

    // 银行卡号正则表达式
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})\\d+(\\d{4})");
    @SuppressWarnings("unused") // 预留字段，用于未来姓名脱敏功能
    private static final Pattern NAME_PATTERN = Pattern.compile("(.{1})(.*)(.{1})");

    /**
     * 手机号脱敏
     * 格式：138****1234
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return phone;
        }
        return PHONE_PATTERN.matcher(phone).replaceAll("$1****$2");
    }

    /**
     * 身份证脱敏
     * 格式：110101********1234
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (StringUtils.isBlank(idCard) || idCard.length() < 15) {
            return idCard;
        }

        if (idCard.length() == 15) {
            // 15位身份证：保留前6位和后3位
            return idCard.substring(0, 6) + "*******" + idCard.substring(13);
        } else {
            // 18位身份证：保留前6位和后4位
            return ID_CARD_PATTERN.matcher(idCard).replaceAll("$1********$2");
        }
    }

    /**
     * 邮箱脱敏
     * 格式：abc***@domain.com
     *
     * @param email 邮箱地址
     * @return 脱敏后的邮箱地址
     */
    public static String maskEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        // 用户名保留前1-3个字符，其余用*替代
        int keepChars = Math.min(username.length(), 3);
        String maskedUsername = username.substring(0, keepChars) + "***";

        return maskedUsername + domain;
    }

    /**
     * 银行卡号脱敏
     * 格式：6222***********1234
     *
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String maskBankCard(String bankCard) {
        if (StringUtils.isBlank(bankCard) || bankCard.length() < 8) {
            return bankCard;
        }

        // 保留前4位和后4位，中间用*替代
        String firstFour = bankCard.substring(0, 4);
        String lastFour = bankCard.substring(bankCard.length() - 4);
        String middle = "*".repeat(bankCard.length() - 8);

        return firstFour + middle + lastFour;
    }

    /**
     * 姓名脱敏
     * 格式：张*（2字姓名），张**（3字姓名），张***（4字及以上）
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String maskName(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }

        int length = name.length();
        if (length == 1) {
            return name; // 单字姓名不脱敏
        } else if (length == 2) {
            return name.charAt(0) + "*";
        } else {
            return name.charAt(0) + "*".repeat(length - 1);
        }
    }

    /**
     * 地址脱敏
     * 格式：保留前6个字符，其余用***替代
     *
     * @param address 地址
     * @return 脱敏后的地址
     */
    public static String maskAddress(String address) {
        if (StringUtils.isBlank(address) || address.length() <= 6) {
            return "***";
        }

        return address.substring(0, 6) + "***";
    }

    /**
     * 密码脱敏（完全隐藏）
     *
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String maskPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return password;
        }
        return "******";
    }

    /**
     * 通用脱敏方法
     * 根据字段类型自动选择合适的脱敏策略
     *
     * @param value 原始值
     * @param type 字段类型
     * @return 脱敏后的值
     */
    public static String maskByType(String value, String type) {
        if (StringUtils.isBlank(value)) {
            return value;
        }

        String lowerType = type.toLowerCase();

        switch (lowerType) {
            case "phone":
            case "mobile":
            case "telephone":
                return maskPhone(value);
            case "idcard":
            case "id_card":
            case "identity":
                return maskIdCard(value);
            case "email":
            case "email_address":
                return maskEmail(value);
            case "bankcard":
            case "bank_card":
            case "card_number":
                return maskBankCard(value);
            case "name":
            case "username":
            case "real_name":
                return maskName(value);
            case "address":
                return maskAddress(value);
            case "password":
            case "pwd":
                return maskPassword(value);
            default:
                log.debug("未知的脱敏类型: {}, 返回原值", type);
                return value;
        }
    }

    /**
     * 自定义脱敏方法
     * 保留前n位和后m位，中间用*替代
     *
     * @param value 原始值
     * @param keepStart 保留前几位
     * @param keepEnd 保留后几位
     * @return 脱敏后的值
     */
    public static String maskCustom(String value, int keepStart, int keepEnd) {
        if (StringUtils.isBlank(value)) {
            return value;
        }

        int length = value.length();
        if (length <= keepStart + keepEnd) {
            return "*".repeat(length);
        }

        String start = value.substring(0, keepStart);
        String end = value.substring(length - keepEnd);
        String middle = "*".repeat(length - keepStart - keepEnd);

        return start + middle + end;
    }

    /**
     * 批量脱敏方法
     * 对包含敏感信息的字符串进行批量脱敏
     *
     * @param text 原始文本
     * @return 脱敏后的文本
     */
    public static String maskSensitiveText(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        // 依次进行各种脱敏
        String result = text;

        // 手机号脱敏
        result = PHONE_PATTERN.matcher(result).replaceAll("$1****$2");

        // 身份证脱敏
        result = ID_CARD_PATTERN.matcher(result).replaceAll("$1********$2");

        // 邮箱脱敏
        result = EMAIL_PATTERN.matcher(result).replaceAll("$1***@$2");

        // 银行卡脱敏
        result = BANK_CARD_PATTERN.matcher(result).replaceAll("$1********$2");

        return result;
    }

    /**
     * 检查是否包含敏感信息
     *
     * @param text 待检查的文本
     * @return 是否包含敏感信息
     */
    public static boolean containsSensitiveData(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }

        return PHONE_PATTERN.matcher(text).find()
                || ID_CARD_PATTERN.matcher(text).find()
                || EMAIL_PATTERN.matcher(text).find()
                || BANK_CARD_PATTERN.matcher(text).find();
    }
}
