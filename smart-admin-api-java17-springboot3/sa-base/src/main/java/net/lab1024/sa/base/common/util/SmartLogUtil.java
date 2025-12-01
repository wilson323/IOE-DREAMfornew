package net.lab1024.sa.base.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 日志工具类 - 统一项目日志记录
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-14
 * @Copyright SmartAdmin v3
 */
@Slf4j
public final class SmartLogUtil {

    /**
     * 记录错误日志
     */
    public static void error(String message) {
        log.error(message);
    }

    /**
     * 记录错误日志
     */
    public static void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    /**
     * 记录警告日志
     */
    public static void warn(String message) {
        log.warn(message);
    }

    /**
     * 记录信息日志
     */
    public static void info(String message) {
        log.info(message);
    }

    /**
     * 记录调试日志
     */
    public static void debug(String message) {
        log.debug(message);
    }

    /**
     * 记录跟踪日志
     */
    public static void trace(String message) {
        log.trace(message);
    }
}
