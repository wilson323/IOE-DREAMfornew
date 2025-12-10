package net.lab1024.sa.common.util;

import java.util.Objects;

/**
 * 字符串工具类
 * <p>
 * 提供常用的字符串操作方法
 * 严格遵循CLAUDE.md规范：
 * - 工具类使用静态方法
 * - 完整的参数验证
 * - 统一的异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class SmartStringUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private SmartStringUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 判断字符串是否为空（null或空字符串）
     *
     * @param str 待判断的字符串
     * @return true表示为空，false表示不为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 待判断的字符串
     * @return true表示不为空，false表示为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为空白（null、空字符串或只包含空白字符）
     *
     * @param str 待判断的字符串
     * @return true表示为空白，false表示不为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空白
     *
     * @param str 待判断的字符串
     * @return true表示不为空白，false表示为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 安全地获取字符串，如果为null则返回空字符串
     *
     * @param str 待处理的字符串
     * @return 处理后的字符串
     */
    public static String safeString(String str) {
        return str == null ? "" : str;
    }

    /**
     * 安全地获取字符串，如果为null则返回默认值
     *
     * @param str        待处理的字符串
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    public static String safeString(String str, String defaultValue) {
        return str == null ? defaultValue : str;
    }

    /**
     * 去除字符串首尾空白字符
     *
     * @param str 待处理的字符串
     * @return 处理后的字符串
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 去除字符串首尾空白字符，如果为null则返回空字符串
     *
     * @param str 待处理的字符串
     * @return 处理后的字符串
     */
    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * 去除字符串首尾空白字符，如果为null或trim后为空则返回null
     *
     * @param str 待处理的字符串
     * @return 处理后的字符串
     */
    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 判断两个字符串是否相等（忽略大小写）
     *
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return true表示相等，false表示不相等
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    /**
     * 判断两个字符串是否相等
     *
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return true表示相等，false表示不相等
     */
    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    /**
     * 截取字符串
     *
     * @param str   待截取的字符串
     * @param start 开始位置（从0开始）
     * @param end   结束位置（不包含）
     * @return 截取后的字符串
     */
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }
        if (start < 0) {
            start = 0;
        }
        if (end > str.length()) {
            end = str.length();
        }
        if (start > end) {
            return "";
        }
        return str.substring(start, end);
    }

    /**
     * 截取字符串（从指定位置到末尾）
     *
     * @param str   待截取的字符串
     * @param start 开始位置（从0开始）
     * @return 截取后的字符串
     */
    public static String substring(String str, int start) {
        if (str == null) {
            return null;
        }
        if (start < 0) {
            start = 0;
        }
        if (start >= str.length()) {
            return "";
        }
        return str.substring(start);
    }
}

