package net.lab1024.sa.consume.util;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 消费模块统一类型转换工具类
 * <p>
 * 提供统一的类型转换方法，解决类型不一致问题
 * 包含安全检查和异常处理，确保类型转换的可靠性
 * </p>
 * <p>
 * 支持的转换类型：
 * - 状态转换：Integer <-> String
 * - 金额转换：BigDecimal <-> Long/String
 * - ID转换：Long <-> String
 * - 时间转换：LocalDateTime <-> String
 * - 安全转换：Object -> 目标类型
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public final class ConsumeTypeConverter {

    // ==================== 常量定义 ====================

    /** 默认日期时间格式 */
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** 金额转换比例：元转分 */
    private static final BigDecimal YUAN_TO_FEN = BigDecimal.valueOf(100);

    /** 金额转换比例：分转元 */
    private static final BigDecimal FEN_TO_YUAN = BigDecimal.valueOf(0.01);

    /** 私有构造函数，防止实例化 */
    private ConsumeTypeConverter() {
        throw new AssertionError("工具类不应被实例化");
    }

    // ==================== 状态转换方法 ====================

    /**
     * 状态转换：Integer -> String
     *
     * @param status 整数状态
     * @return 字符串状态
     */
    public static String statusToString(Integer status) {
        if (status == null) {
            return "UNKNOWN";
        }
        switch (status) {
            case 0:
                return "PENDING";
            case 1:
                return "SUCCESS";
            case 2:
                return "FAILED";
            case 3:
                return "CANCELLED";
            case 4:
                return "REFUNDED";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * 状态转换：String -> Integer
     *
     * @param statusStr 字符串状态
     * @return 整数状态
     */
    public static Integer statusToInteger(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return null;
        }
        switch (statusStr.toUpperCase()) {
            case "PENDING":
                return 0;
            case "SUCCESS":
                return 1;
            case "FAILED":
                return 2;
            case "CANCELLED":
                return 3;
            case "REFUNDED":
                return 4;
            default:
                return null;
        }
    }

    /**
     * 判断状态是否成功
     *
     * @param status 整数状态
     * @return 是否成功
     */
    public static boolean isSuccess(Integer status) {
        return Integer.valueOf(1).equals(status);
    }

    /**
     * 判断状态是否失败
     *
     * @param status 整数状态
     * @return 是否失败
     */
    public static boolean isFailed(Integer status) {
        return Integer.valueOf(2).equals(status);
    }

    // ==================== 金额转换方法 ====================

    /**
     * 金额转换：Object -> BigDecimal（安全转换）
     *
     * @param amount 金额对象
     * @return BigDecimal金额
     * @throws IllegalArgumentException 不支持的金额类型
     */
    public static BigDecimal toBigDecimal(Object amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (amount instanceof BigDecimal) {
            return (BigDecimal) amount;
        }
        if (amount instanceof String) {
            try {
                return new BigDecimal(((String) amount).trim());
            } catch (NumberFormatException e) {
                log.debug("[消费类型转换] 金额字符串解析失败，返回0: amount={}, error={}", amount, e.getMessage());
                return BigDecimal.ZERO;
            }
        }
        if (amount instanceof Number) {
            return BigDecimal.valueOf(((Number) amount).doubleValue());
        }
        throw new IllegalArgumentException("不支持的金额类型: " + amount.getClass().getName());
    }

    /**
     * 金额转换：元 -> 分
     *
     * @param yuanAmount 元金额
     * @return 分金额
     */
    public static Long yuanToFen(BigDecimal yuanAmount) {
        if (yuanAmount == null) {
            return 0L;
        }
        return yuanAmount.multiply(YUAN_TO_FEN).longValue();
    }

    /**
     * 金额转换：元 -> 分
     *
     * @param yuanAmount 元金额
     * @return 分金额
     */
    public static Long yuanToFen(Double yuanAmount) {
        if (yuanAmount == null) {
            return 0L;
        }
        return BigDecimal.valueOf(yuanAmount).multiply(YUAN_TO_FEN).longValue();
    }

    /**
     * 金额转换：元 -> 分
     *
     * @param yuanAmount 元金额
     * @return 分金额
     */
    public static Long yuanToFen(String yuanAmount) {
        return yuanToFen(toBigDecimal(yuanAmount));
    }

    /**
     * 金额转换：分 -> 元
     *
     * @param fenAmount 分金额
     * @return 元金额
     */
    public static BigDecimal fenToYuan(Long fenAmount) {
        if (fenAmount == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(fenAmount).multiply(FEN_TO_YUAN);
    }

    /**
     * 金额转换：分 -> 元
     *
     * @param fenAmount 分金额
     * @return 元金额
     */
    public static BigDecimal fenToYuan(Integer fenAmount) {
        if (fenAmount == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(fenAmount).multiply(FEN_TO_YUAN);
    }

    /**
     * 金额格式化：BigDecimal -> String
     *
     * @param amount 金额
     * @return 格式化的金额字符串
     */
    public static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        // 使用RoundingMode替代已废弃的ROUND_HALF_UP常量
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }

    /**
     * 金额格式化：Long -> String（分转元格式化）
     *
     * @param fenAmount 分金额
     * @return 格式化的元字符串
     */
    public static String formatAmount(Long fenAmount) {
        return formatAmount(fenToYuan(fenAmount));
    }

    // ==================== ID转换方法 ====================

    /**
     * ID转换：Object -> String（安全转换）
     *
     * @param id ID对象
     * @return 字符串ID
     */
    public static String idToString(Object id) {
        if (id == null) {
            return null;
        }
        if (id instanceof String) {
            return (String) id;
        }
        if (id instanceof Long || id instanceof Integer) {
            return String.valueOf(id);
        }
        return id.toString();
    }

    /**
     * ID转换：Object -> Long（安全转换）
     *
     * @param id ID对象
     * @return Long类型ID，转换失败返回null
     */
    public static Long idToLong(Object id) {
        if (id == null) {
            return null;
        }
        if (id instanceof Long) {
            return (Long) id;
        }
        if (id instanceof Integer) {
            return ((Integer) id).longValue();
        }
        if (id instanceof String) {
            try {
                return Long.parseLong(((String) id).trim());
            } catch (NumberFormatException e) {
                log.debug("[消费类型转换] ID字符串解析失败，返回null: id={}, error={}", id, e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * ID转换：Object -> Long（安全转换，带默认值）
     *
     * @param id          ID对象
     * @param defaultValue 默认值
     * @return Long类型ID
     */
    public static Long idToLong(Object id, Long defaultValue) {
        Long result = idToLong(id);
        return result != null ? result : defaultValue;
    }

    // ==================== 时间转换方法 ====================

    /**
     * 时间转换：LocalDateTime -> String
     *
     * @param dateTime 时间
     * @return 时间字符串
     */
    public static String dateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
    }

    /**
     * 时间转换：LocalDateTime -> String（指定格式）
     *
     * @param dateTime 时间
     * @param pattern  时间格式
     * @return 时间字符串
     */
    public static String dateTimeToString(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 时间转换：String -> LocalDateTime
     *
     * @param dateTimeStr 时间字符串
     * @return LocalDateTime对象
     */
    public static LocalDateTime stringToDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
        } catch (Exception e) {
            log.debug("[消费类型转换] 日期时间字符串解析失败: dateTimeStr={}, error={}", dateTimeStr, e.getMessage());
            return null;
        }
    }

    // ==================== 通用转换方法 ====================

    /**
     * 安全的对象转换
     *
     * @param value     原始值
     * @param targetType 目标类型
     * @param <T>       目标类型泛型
     * @return 转换后的值，转换失败返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T safeConvert(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return (T) value;
        }
        if (targetType == String.class) {
            return (T) value.toString();
        }
        if (targetType == Long.class && value instanceof Number) {
            return (T) Long.valueOf(((Number) value).longValue());
        }
        if (targetType == Integer.class && value instanceof Number) {
            return (T) Integer.valueOf(((Number) value).intValue());
        }
        if (targetType == BigDecimal.class) {
            return (T) toBigDecimal(value);
        }
        return null;
    }

    /**
     * 安全的对象比较
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否相等
     */
    public static boolean safeEquals(Object obj1, Object obj2) {
        return Objects.equals(obj1, obj2);
    }

    /**
     * 安全的hashCode计算
     *
     * @param obj 对象
     * @return hashCode
     */
    public static int safeHashCode(Object obj) {
        return obj != null ? obj.hashCode() : 0;
    }

    // ==================== 验证方法 ====================

    /**
     * 验证金额是否有效
     *
     * @param amount 金额
     * @return 是否有效
     */
    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 验证金额是否有效（分）
     *
     * @param fenAmount 分金额
     * @return 是否有效
     */
    public static boolean isValidAmount(Long fenAmount) {
        return fenAmount != null && fenAmount > 0;
    }

    /**
     * 验证ID是否有效
     *
     * @param id ID
     * @return 是否有效
     */
    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    /**
     * 验证字符串是否有效
     *
     * @param str 字符串
     * @return 是否有效
     */
    public static boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建安全的BigDecimal（防止null）
     *
     * @param value 值
     * @return BigDecimal对象
     */
    public static BigDecimal safeBigDecimal(Object value) {
        BigDecimal result = toBigDecimal(value);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 创建安全的Long ID（防止null）
     *
     * @param value 值
     * @return Long对象
     */
    public static Long safeLongId(Object value) {
        Long result = idToLong(value);
        return result != null ? result : 0L;
    }

    /**
     * 创建安全的字符串（防止null）
     *
     * @param value 值
     * @return 字符串对象
     */
    public static String safeString(Object value) {
        return value != null ? value.toString() : "";
    }
}



