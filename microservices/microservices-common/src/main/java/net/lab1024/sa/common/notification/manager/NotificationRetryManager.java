package net.lab1024.sa.common.notification.manager;

import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

/**
 * 通知重试管理器
 * <p>
 * 统一管理所有通知渠道的重试策略
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 企业级特性：
 * - 指数退避重试策略
 * - 支持自定义重试次数和延迟
 * - 支持特定错误码跳过重试
 * - 线程安全实现
 * </p>
 * <p>
 * 重试策略：
 * - 默认最大重试次数：3次
 * - 基础延迟：1秒
 * - 指数退避：1s, 2s, 4s
 * - 可配置跳过重试的错误码
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class NotificationRetryManager {

    /**
     * 默认最大重试次数
     */
    private static final int DEFAULT_MAX_RETRIES = 3;

    /**
     * 默认基础延迟（毫秒）
     */
    private static final long DEFAULT_BASE_DELAY_MS = 1000L;

    /**
     * 最大重试次数
     */
    private final int maxRetries;

    /**
     * 基础延迟（毫秒）
     */
    private final long baseDelayMs;

    /**
     * 构造函数（使用默认配置）
     */
    public NotificationRetryManager() {
        this.maxRetries = DEFAULT_MAX_RETRIES;
        this.baseDelayMs = DEFAULT_BASE_DELAY_MS;
    }

    /**
     * 构造函数（支持自定义配置）
     *
     * @param maxRetries 最大重试次数
     * @param baseDelayMs 基础延迟（毫秒）
     */
    public NotificationRetryManager(int maxRetries, long baseDelayMs) {
        this.maxRetries = maxRetries > 0 ? maxRetries : DEFAULT_MAX_RETRIES;
        this.baseDelayMs = baseDelayMs > 0 ? baseDelayMs : DEFAULT_BASE_DELAY_MS;
    }

    /**
     * 执行带重试的操作
     * <p>
     * 使用指数退避策略进行重试
     * </p>
     *
     * @param operation 操作函数（返回操作结果）
     * @param operationName 操作名称（用于日志）
     * @return 操作结果
     * @throws Exception 重试失败后抛出异常
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) throws Exception {
        return executeWithRetry(operation, operationName, null);
    }

    /**
     * 执行带重试的操作（支持错误码检查）
     * <p>
     * 使用指数退避策略进行重试
     * 如果操作返回特定错误码，跳过重试
     * </p>
     *
     * @param operation 操作函数（返回操作结果）
     * @param operationName 操作名称（用于日志）
     * @param errorCodeChecker 错误码检查器（返回true表示跳过重试）
     * @return 操作结果
     * @throws Exception 重试失败后抛出异常
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName,
                                  java.util.function.Function<T, Boolean> errorCodeChecker) throws Exception {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount <= maxRetries) {
            try {
                T result = operation.get();

                // 检查错误码（如果提供了错误码检查器）
                if (errorCodeChecker != null && result != null) {
                    Boolean shouldSkipRetry = errorCodeChecker.apply(result);
                    if (Boolean.TRUE.equals(shouldSkipRetry)) {
                        log.warn("[通知重试] 错误码检查失败，跳过重试，操作：{}", operationName);
                        return result;
                    }
                }

                // 操作成功
                if (retryCount > 0) {
                    log.info("[通知重试] 操作成功（重试{}次后），操作：{}", retryCount, operationName);
                }
                return result;

            } catch (Exception e) {
                lastException = e;
                log.warn("[通知重试] 操作失败，重试次数：{}/{}，操作：{}，错误：{}",
                        retryCount, maxRetries, operationName, e.getMessage());

                // 检查是否应该跳过重试（特定异常类型）
                if (shouldSkipRetry(e)) {
                    log.warn("[通知重试] 异常类型不允许重试，操作：{}，异常类型：{}",
                            operationName, e.getClass().getSimpleName());
                    throw e;
                }

                // 重试逻辑
                if (retryCount < maxRetries) {
                    long delay = calculateDelay(retryCount);
                    log.info("[通知重试] 等待{}ms后重试，操作：{}", delay, operationName);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[通知重试] 重试被中断，操作：{}", operationName);
                        throw new RuntimeException("重试被中断", ie);
                    }
                    retryCount++;
                } else {
                    log.error("[通知重试] 重试次数已达上限，操作失败，操作：{}", operationName, e);
                    throw new RuntimeException("操作失败，已重试" + maxRetries + "次", e);
                }
            }
        }

        // 理论上不会执行到这里
        if (lastException != null) {
            throw lastException;
        }
        throw new RuntimeException("操作失败，未知错误");
    }

    /**
     * 执行带重试的布尔操作
     * <p>
     * 用于返回boolean类型的操作
     * </p>
     *
     * @param operation 操作函数（返回操作是否成功）
     * @param operationName 操作名称（用于日志）
     * @return 操作是否成功
     */
    public boolean executeWithRetryBoolean(java.util.function.Supplier<Boolean> operation, String operationName) {
        try {
            return executeWithRetry(operation, operationName);
        } catch (Exception e) {
            log.error("[通知重试] 布尔操作失败，操作：{}", operationName, e);
            return false;
        }
    }

    /**
     * 计算重试延迟（指数退避）
     * <p>
     * 延迟公式：baseDelay * 2^retryCount
     * 示例：1s, 2s, 4s, 8s...
     * </p>
     *
     * @param retryCount 当前重试次数
     * @return 延迟时间（毫秒）
     */
    private long calculateDelay(int retryCount) {
        return baseDelayMs * (1L << retryCount); // 指数退避：1s, 2s, 4s, 8s...
    }

    /**
     * 检查是否应该跳过重试
     * <p>
     * 某些异常类型不应该触发重试（如参数错误、认证失败等）
     * </p>
     *
     * @param exception 异常对象
     * @return 是否跳过重试
     */
    private boolean shouldSkipRetry(Exception exception) {
        // 参数错误、认证失败等不应该重试
        String exceptionMessage = exception.getMessage();
        if (exceptionMessage != null) {
            String messageLower = exceptionMessage.toLowerCase();
            if (messageLower.contains("参数错误") ||
                    messageLower.contains("认证失败") ||
                    messageLower.contains("权限不足") ||
                    messageLower.contains("invalid parameter") ||
                    messageLower.contains("unauthorized") ||
                    messageLower.contains("forbidden")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * 获取基础延迟
     *
     * @return 基础延迟（毫秒）
     */
    public long getBaseDelayMs() {
        return baseDelayMs;
    }
}
