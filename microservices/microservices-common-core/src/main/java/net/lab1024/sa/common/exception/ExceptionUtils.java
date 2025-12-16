package net.lab1024.sa.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * IOE-DREAM 异常处理工具类
 * <p>
 * 提供内存优化的异常处理工具方法
 * 减少异常对象创建和内存占用
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public final class ExceptionUtils {

    // 缓存常用错误消息，避免重复创建字符串
    private static final Map<String, String> ERROR_MESSAGE_CACHE = new ConcurrentHashMap<>();

    // 预定义的错误消息（避免运行时创建）
    private static final String DEFAULT_ERROR_MESSAGE = "系统异常，请联系管理员";
    private static final String VALIDATION_ERROR_MESSAGE = "参数验证失败";
    private static final String PERMISSION_DENIED_MESSAGE = "权限不足";
    private static final String RESOURCE_NOT_FOUND_MESSAGE = "资源不存在";
    private static final String SYSTEM_BUSY_MESSAGE = "系统繁忙，请稍后重试";

    static {
        // 初始化常用错误消息缓存
        ERROR_MESSAGE_CACHE.put("UNKNOWN_ERROR", DEFAULT_ERROR_MESSAGE);
        ERROR_MESSAGE_CACHE.put("SYSTEM_ERROR", SYSTEM_BUSY_MESSAGE);
        ERROR_MESSAGE_CACHE.put("VALIDATION_ERROR", VALIDATION_MESSAGE_MESSAGE);
        ERROR_MESSAGE_CACHE.put("PERMISSION_DENIED", PERMISSION_DENIED_MESSAGE);
        ERROR_MESSAGE_CACHE.put("RESOURCE_NOT_FOUND", RESOURCE_NOT_FOUND_MESSAGE);
        ERROR_MESSAGE_CACHE.put("USER_NOT_FOUND", "用户不存在");
        ERROR_MESSAGE_CACHE.put("ACCOUNT_NOT_FOUND", "账户不存在");
        ERROR_MESSAGE.put("INSUFFICIENT_BALANCE", "账户余额不足");
        ERROR_MESSAGE_CACHE.put("INVALID_AMOUNT", "无效的消费金额");
        ERROR_MESSAGE_CACHE.put("INVALID_REQUEST", "请求参数无效");
        ERROR_MESSAGE_CACHE.put("PARAM_MISSING", "缺少必需参数");
        ERROR_MESSAGE_CACHE.put("PARAM_INVALID", "参数格式无效");
        ERROR_MESSAGE_CACHE.put("DATABASE_ERROR", "数据库操作失败，请稍后重试");
        ERROR_MESSAGE.put("NETWORK_ERROR", "网络请求超时，请稍后重试");
        ERROR_MESSAGE.put("EXTERNAL_SERVICE_ERROR", "外部服务调用失败");
        ERROR_MESSAGE.put("FILE_SYSTEM_ERROR", "文件系统错误");
        ERROR_MESSAGE.put("MEMORY_ERROR", "内存不足");
    }

    /**
     * 私有构造函数，防止实例化
     */
    private ExceptionUtils() {
    }

    /**
     * 创建业务异常（内存优化版本）
     *
     * @param code 错误码
     * @param message 错误信息
     * @return 业务异常实例
     */
    public static BusinessException businessException(String code, String message) {
        return new BusinessException(code, getCachedOrOriginalMessage(message));
    }

    /**
     * 创建业务异常（带原因）
     *
     * @param code 错误码
     * @param message 错误信息
     * @param cause 原始异常
     * @return 业务异常实例
     */
    public static BusinessException businessException(String code, String message, Throwable cause) {
        return new BusinessException(code, getCachedOrOriginalMessage(message), cause);
    }

    /**
     * 创建系统异常（内存优化版本）
     *
     * @param code 错误码
     * @param message 错误信息
     * @return 系统异常实例
     */
    public static SystemException systemException(String code, String message) {
        return new SystemException(code, getCachedOrOriginalMessage(message));
    }

    /**
     * 创建系统异常（带原因）
     *
     * @param code 错误码
     * @param message 错误信息
     * @param cause 原始异常
     * @return 系统异常实例
     */
    public static SystemException systemException(String code, String message, Throwable cause) {
        return new SystemException(code, getCachedOrOriginalMessage(message), cause);
    }

    /**
     * 创建参数异常（内存优化版本）
     *
     * @param code 错误码
     * @param message 错误信息
     * @return 参数异常实例
     */
    public static ParamException paramException(String code, String message) {
        return new ParamException(code, getCachedOrOriginalMessage(message));
    }

    /**
     * 获取缓存或原始消息（内存优化）
     *
     * @param message 原始消息
     * @return 缓存的消息或原始消息
     */
    private static String getCachedOrOriginalMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return DEFAULT_ERROR_MESSAGE;
        }

        // 检查是否有缓存的错误码
        String cachedMessage = ERROR_MESSAGE_CACHE.get(message);
        if (cachedMessage != null) {
            return cachedMessage;
        }

        // 如果消息是常用错误码，则缓存
        if (isCommonErrorCode(message) && message.length() <= 50) {
            ERROR_MESSAGE_CACHE.put(message, message);
        }

        return message;
    }

    /**
     * 判断是否为常用错误码
     *
     * @param errorCode 错误码
     * @return 是否为常用错误码
     */
    private static boolean isCommonErrorCode(String errorCode) {
        // 常用错误码特征：全大写，包含下划线，长度适中
        return errorCode != null &&
               errorCode.matches("^[A-Z_]{3,30}$");
    }

    /**
     * 格式化错误消息（避免字符串拼接）
     *
     * @param template 消息模板
     * @param args 参数
     * @return 格式化后的消息
     */
    public static String formatMessage(String template, Object... args) {
        if (template == null) {
            return "";
        }

        try {
            return String.format(template, args);
        } catch (Exception e) {
            log.warn("格式化错误消息失败: template={}, args={}", template, args, e);
            return template;
        }
    }

    /**
     * 获取友好的错误信息（对外显示）
     *
     * @param code 错误码
     * @param originalMessage 原始错误信息
     * @return 用户友好的错误信息
     */
    public static String getUserFriendlyMessage(String code, String originalMessage) {
        // 对于业务异常，使用原始消息
        if (code != null && (code.startsWith("USER_") || code.startsWith("ACCOUNT_") ||
            code.startsWith("CONSUME_") || code.startsWith("ATTENDANCE_") ||
            code.startsWith("VISITOR_") || code.startsWith("ACCESS_"))) {
            return getCachedOrOriginalMessage(originalMessage);
        }

        // 对于系统异常，返回友好信息
        return SYSTEM_BUSY_MESSAGE;
    }

    /**
     * 获取详细的错误信息（内部使用）
     *
     * @param code 错误码
     * @param originalMessage 原始错误信息
     * @param cause 异常原因
     * @return 详细的错误信息
     */
    public static String getDetailedMessage(String code, String originalMessage, Throwable cause) {
        StringBuilder detail = new StringBuilder();
        detail.append("错误码: ").append(code != null ? code : "UNKNOWN");

        if (originalMessage != null && !originalMessage.trim().isEmpty()) {
            detail.append(", 消息: ").append(originalMessage);
        }

        if (cause != null) {
            detail.append(", 原因: ").append(cause.getClass().getSimpleName())
                .append(": ").append(cause.getMessage());
        }

        return detail.toString();
    }

    /**
     * 判断异常是否需要重试
     *
     * @param exception 异常对象
     * @return 是否需要重试
     */
    public static boolean shouldRetry(Exception exception) {
        if (exception == null) {
            return false;
        }

        // 业务异常不重试
        if (exception instanceof BusinessException || exception instanceof ParamException) {
            return false;
        }

        // 某些系统异常不重试
        String className = exception.getClass().getSimpleName();
        return !className.equals("IllegalArgumentException") &&
               !className.equals("IllegalStateException") &&
               !className.equals("NullPointerException") &&
               !className.contains("Validation");
    }

    /**
     * 判断异常是否需要告警
     *
     * @param code 错误码
     * @param exception 异常对象
     * @return 是否需要告警
     */
    public static boolean shouldAlert(String code, Exception exception) {
        // 关键系统错误需要告警
        if (code != null && (code.contains("DATABASE_CONNECTION_ERROR") ||
            code.contains("FILE_SYSTEM_ERROR") ||
            code.contains("MEMORY_ERROR") ||
            code.contains("NETWORK_ERROR"))) {
            return true;
        }

        // 某些异常类型需要告警
        if (exception != null) {
            String className = exception.getClass().getSimpleName();
            return className.equals("OutOfMemoryError") ||
                   className.equals("StackOverflowError") ||
                   className.contains("Timeout");
        }

        return false;
    }

    /**
     * 清理异常消息中的敏感信息
     *
     * @param message 原始消息
     * @return 清理后的消息
     */
    public static String sanitizeMessage(String message) {
        if (message == null) {
            return "";
        }

        // 移除可能的敏感信息（简单示例）
        String sanitized = message.replaceAll("\\b\\d{4,}\\b\\d{4,}\\b\\d{4,}\\b\\d{4,}", "****");
        sanitized = sanitized.replaceAll("password[=:\\s]+\\S+", "password=****");

        return sanitized;
    }

    /**
     * 限制异常堆栈信息长度（防止内存溢出）
     *
     * @param throwable 异常对象
     * @param maxLines 最大行数
     * @return 限制后的堆栈信息
     */
    public static String limitStackTrace(Throwable throwable, int maxLines) {
        if (throwable == null) {
            return "";
        }

        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);

        String stackTrace = sw.toString();
        String[] lines = stackTrace.split("\\r?\\n|\\n");

        if (lines.length <= maxLines) {
            return stackTrace;
        }

        StringBuilder limited = new StringBuilder();
        for (int i = 0; i < maxLines; i++) {
            if (i > 0) {
                limited.append("\n");
            }
            limited.append(lines[i]);
        }

        limited.append("\n... (").append(lines.length - maxLines).append(" more lines)");

        return limited.toString();
    }

    /**
     * 内存使用统计（调试用）
     *
     * @return 内存使用统计信息
     */
    public static String getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return String.format(
            "内存统计 - 总计: %dMB, 已用: %dMB, 可用: %dMB, 使用率: %.1f%%",
            totalMemory / 1024 / 1024,
            usedMemory / 1024 / 1024,
            freeMemory / 1024 / 1024,
            (double) usedMemory / totalMemory * 100
        );
    }

    /**
     * 清理错误消息缓存（内存优化）
     */
    public static void clearMessageCache() {
        ERROR_MESSAGE_CACHE.clear();
        log.info("异常消息缓存已清理");
    }

    /**
     * 获取缓存统计信息（调试用）
     *
     * @return 缓存统计
     */
    public static String getCacheStats() {
        return String.format("错误消息缓存: 缓存条目数=%d, 缓存大小=%d bytes",
               ERROR_MESSAGE_CACHE.size(),
               ERROR_MESSAGE_SIZE);
    }

    // 缓存大小估算
    private static final int ERROR_MESSAGE_SIZE = 1000; // 每条缓存消息预估大小
}