package net.lab1024.sa.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 数据脱敏工具类
 * <p>
 * 功能：对敏感数据进行脱敏处理，包括日志、导出、展示场景
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class DataMaskingUtil {

    /**
     * 脱敏类型枚举
     */
    public enum MaskType {
        /** 中文姓名 */
        CHINESE_NAME,
        /** 身份证号 */
        ID_CARD,
        /** 手机号 */
        PHONE,
        /** 固定电话 */
        FIXED_PHONE,
        /** 邮箱 */
        EMAIL,
        /** 银行卡号 */
        BANK_CARD,
        /** 密码 */
        PASSWORD,
        /** 车牌号 */
        LICENSE_PLATE,
        /** IP地址 */
        IP_ADDRESS,
        /** 地址 */
        ADDRESS,
        /** 自定义脱敏 */
        CUSTOM
    }

    // 正则表达式模式
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{3})\\d{4}(\\d{4})");
    private static final Pattern FIXED_PHONE_PATTERN = Pattern.compile("(\\d{3,4})\\d{4}(\\d{4})");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})\\d{8}(\\d{4})");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})\\d{8,12}(\\d{4})");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(\\w{1,3})\\w*@([\\w\\.\\-]+\\.[a-zA-Z]{2,})");
    private static final Pattern LICENSE_PLATE_PATTERN = Pattern.compile("([\\u4e00-\\u9fa5]{1}[A-Za-z]{1})[A-Za-z0-9]{5}");
    private static final Pattern IP_PATTERN = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");

    /**
     * 默认脱敏符号
     */
    private static final char MASK_CHAR = '*';

    /**
     * 对字符串进行脱敏处理
     *
     * @param content 原始内容
     * @param type    脱敏类型
     * @return 脱敏后的内容
     */
    public static String mask(String content, MaskType type) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        try {
            switch (type) {
                case CHINESE_NAME:
                    return maskChineseName(content);
                case ID_CARD:
                    return maskIdCard(content);
                case PHONE:
                    return maskPhone(content);
                case FIXED_PHONE:
                    return maskFixedPhone(content);
                case EMAIL:
                    return maskEmail(content);
                case BANK_CARD:
                    return maskBankCard(content);
                case PASSWORD:
                    return maskPassword(content);
                case LICENSE_PLATE:
                    return maskLicensePlate(content);
                case IP_ADDRESS:
                    return maskIpAddress(content);
                case ADDRESS:
                    return maskAddress(content);
                default:
                    return maskDefault(content);
            }
        } catch (Exception e) {
            log.warn("[数据脱敏] 脱敏处理异常: type={}, content={}, error={}",
                    type, content, e.getMessage());
            return maskDefault(content);
        }
    }

    /**
     * 中文姓名脱敏
     * 规则：只显示第一个字，其余用*替代
     * 示例：张三 → 张*；欧阳修修 → 欧阳**
     */
    public static String maskChineseName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        int length = name.length();
        if (length == 1) {
            return name;
        } else if (length == 2) {
            return String.valueOf(name.charAt(0)) + MASK_CHAR;
        } else if (length == 3) {
            return String.valueOf(name.charAt(0)) + MASK_CHAR + name.charAt(2);
        } else if (length == 4) {
            return name.substring(0, 2) + MASK_CHAR + MASK_CHAR;
        } else {
            // 复姓保留前两个字
            return name.substring(0, Math.min(2, length)) +
                    repeatChar(MASK_CHAR, length - 2);
        }
    }

    /**
     * 身份证号脱敏
     * 规则：显示前6位和后4位，中间用*替代
     * 示例：110101199001011234 → 110101********1234
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 15) {
            return maskDefault(idCard);
        }

        java.util.regex.Matcher matcher = ID_CARD_PATTERN.matcher(idCard);
        if (matcher.matches()) {
            return matcher.group(1) + repeatChar(MASK_CHAR, 8) + matcher.group(2);
        }

        return maskDefault(idCard);
    }

    /**
     * 手机号脱敏
     * 规则：显示前3位和后4位，中间用*替代
     * 示例：13812345678 → 138****5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return maskDefault(phone);
        }

        java.util.regex.Matcher matcher = PHONE_PATTERN.matcher(phone);
        if (matcher.matches()) {
            return matcher.group(1) + "****" + matcher.group(2);
        }

        return maskDefault(phone);
    }

    /**
     * 固定电话脱敏
     * 规则：显示区号和后4位，中间用*替代
     * 示例：010-12345678 → 010-****5678
     */
    public static String maskFixedPhone(String fixedPhone) {
        if (fixedPhone == null || fixedPhone.length() < 7) {
            return maskDefault(fixedPhone);
        }

        // 移除所有非数字字符
        String digits = fixedPhone.replaceAll("\\D", "");
        java.util.regex.Matcher matcher = FIXED_PHONE_PATTERN.matcher(digits);
        if (matcher.matches()) {
            return matcher.group(1) + "-****-" + matcher.group(2);
        }

        return maskDefault(fixedPhone);
    }

    /**
     * 邮箱脱敏
     * 规则：显示第一个字符和域名，用户名中间用*替代
     * 示例：zhangsan@example.com → z******@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return maskDefault(email);
        }

        java.util.regex.Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (matcher.matches()) {
            String prefix = matcher.group(1);
            String domain = matcher.group(2);
            String maskedPrefix = prefix.charAt(0) + repeatChar(MASK_CHAR, prefix.length() - 1);
            return maskedPrefix + "@" + domain;
        }

        return maskDefault(email);
    }

    /**
     * 银行卡号脱敏
     * 规则：显示前4位和后4位，中间用*替代
     * 示例：6222021234567890 → 6222************7890
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 12) {
            return maskDefault(bankCard);
        }

        java.util.regex.Matcher matcher = BANK_CARD_PATTERN.matcher(bankCard);
        if (matcher.matches()) {
            int maskedLength = matcher.group(2).length();
            return matcher.group(1) + repeatChar(MASK_CHAR, maskedLength) + matcher.group(3);
        }

        return maskDefault(bankCard);
    }

    /**
     * 密码脱敏
     * 规则：全部用*替代
     * 示例：password123 → ***********
     */
    public static String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        return repeatChar(MASK_CHAR, password.length());
    }

    /**
     * 车牌号脱敏
     * 规则：显示省份和字母，数字用*替代
     * 示例：京A12345 → 京A*****；沪B·12345 → 沪B·*****
     */
    public static String maskLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.length() < 7) {
            return maskDefault(licensePlate);
        }

        java.util.regex.Matcher matcher = LICENSE_PLATE_PATTERN.matcher(licensePlate);
        if (matcher.matches()) {
            return licensePlate.substring(0, 2) + "*****";
        }

        return maskDefault(licensePlate);
    }

    /**
     * IP地址脱敏
     * 规则：最后一段用*替代
     * 示例：192.168.1.100 → 192.168.1.*
     */
    public static String maskIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return ipAddress;
        }

        java.util.regex.Matcher matcher = IP_PATTERN.matcher(ipAddress);
        if (matcher.matches()) {
            return matcher.group(1) + "." + matcher.group(2) + "." +
                    matcher.group(3) + ".*";
        }

        return maskDefault(ipAddress);
    }

    /**
     * 地址脱敏
     * 规则：显示前6个字符和后6个字符，中间用*替代
     * 示例：北京市朝阳区XX路XX号 → 北京市朝******号
     */
    public static String maskAddress(String address) {
        if (address == null || address.length() <= 12) {
            return maskDefault(address);
        }

        int keepLength = 6;
        if (address.length() <= keepLength * 2) {
            return maskDefault(address);
        }

        return address.substring(0, keepLength) +
                repeatChar(MASK_CHAR, address.length() - keepLength * 2) +
                address.substring(address.length() - keepLength);
    }

    /**
     * 自定义脱敏
     *
     * @param content      原始内容
     * @param keepStart    保留开头字符数
     * @param keepEnd      保留结尾字符数
     * @return 脱敏后的内容
     */
    public static String maskCustom(String content, int keepStart, int keepEnd) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        int length = content.length();
        if (length <= keepStart + keepEnd) {
            return content;
        }

        return content.substring(0, keepStart) +
                repeatChar(MASK_CHAR, length - keepStart - keepEnd) +
                content.substring(length - keepEnd);
    }

    /**
     * 默认脱敏规则
     * 规则：显示前25%和后25%，中间用*替代
     */
    public static String maskDefault(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        int length = content.length();
        if (length <= 4) {
            return repeatChar(MASK_CHAR, length);
        }

        int keepLength = Math.max(1, length / 4);
        return maskCustom(content, keepLength, keepLength);
    }

    /**
     * 对象转JSON并脱敏（已禁用 - 依赖Jackson）
     * 用于日志记录时的对象脱敏
     *
     * @param obj    对象
     * @param fields 需要脱敏的字段名数组
     * @return 脱敏后的JSON字符串
     */
    public static String maskObject(Object obj, String... fields) {
        // 已禁用：common-core不应依赖Jackson
        // 实际使用时应在业务模块中使用JSON工具处理
        if (obj == null) {
            return "null";
        }
        return obj.toString();
    }

    /**
     * JSON字符串脱敏
     *
     * @param json   JSON字符串
     * @param fields 需要脱敏的字段名数组
     * @return 脱敏后的JSON字符串
     */
    public static String maskJson(String json, String... fields) {
        if (json == null || json.isEmpty() || fields == null || fields.length == 0) {
            return json;
        }

        String result = json;
        for (String field : fields) {
            // 查找 "field":"value" 模式并替换value
            // 这里使用简化的正则，实际使用时可以更精确
            result = result.replaceAll(
                    "(\"" + field + "\":\")([^\"]+)(\")",
                    "$1" + repeatChar(MASK_CHAR, 8) + "$3"
            );
        }
        return result;
    }

    /**
     * 日志脱敏（批量处理）
     * 自动识别常见敏感字段并脱敏
     *
     * @param logContent 日志内容
     * @return 脱敏后的日志内容
     */
    public static String maskLog(String logContent) {
        if (logContent == null || logContent.isEmpty()) {
            return logContent;
        }

        String result = logContent;

        // 识别并脱敏手机号
        result = PHONE_PATTERN.matcher(result).replaceAll("$1****$2");

        // 识别并脱敏身份证号
        result = ID_CARD_PATTERN.matcher(result).replaceAll("$1********$2");

        // 识别并脱敏银行卡号
        result = BANK_CARD_PATTERN.matcher(result).replaceAll("$1********$3");

        // 识别并脱敏邮箱
        result = EMAIL_PATTERN.matcher(result).replaceAll("$1***@$2");

        // 识别并脱敏固定电话
        result = FIXED_PHONE_PATTERN.matcher(result).replaceAll("$1-****-$2");

        return result;
    }

    /**
     * 生成指定长度的重复字符
     */
    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 对象转JSON字符串（已禁用 - 依赖Jackson）
     * 实际项目中应使用项目的JSON工具
     */
    private static String toJsonString(Object obj) {
        // 已禁用：common-core不应依赖Jackson
        throw new UnsupportedOperationException(
            "DataMaskingUtil.toJsonString() 已禁用 - common-core不应依赖Jackson。" +
            "请在业务模块中使用JsonUtils或Jackson直接处理。"
        );
    }

    /**
     * 快速脱敏方法集
     */

    /**
     * 快速脱敏手机号
     */
    public static String maskPhoneQuick(String phone) {
        return maskPhone(phone);
    }

    /**
     * 快速脱敏身份证
     */
    public static String maskIdCardQuick(String idCard) {
        return maskIdCard(idCard);
    }

    /**
     * 快速脱敏姓名
     */
    public static String maskNameQuick(String name) {
        return maskChineseName(name);
    }

    /**
     * 快速脱敏邮箱
     */
    public static String maskEmailQuick(String email) {
        return maskEmail(email);
    }
}
