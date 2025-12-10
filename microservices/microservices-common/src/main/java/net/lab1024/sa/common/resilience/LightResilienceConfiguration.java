package net.lab1024.sa.common.resilience;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轻量级容错配置
 * <p>
 * 避免过度复杂的容错机制，只提供核心功能：
 * - 简单的熔断器
 * - 基础的重试机制
 * - 降级处理
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "resilience.enabled", havingValue = "true", matchIfMissing = true)
public class LightResilienceConfiguration {

    /**
     * 轻量级熔断管理器
     */
    @Bean
    public LightCircuitBreakerManager circuitBreakerManager() {
        return new LightCircuitBreakerManager();
    }

    /**
     * 轻量级重试管理器
     */
    @Bean
    public LightRetryManager retryManager() {
        return new LightRetryManager();
    }

    /**
     * 轻量级熔断器管理器
     */
    public static class LightCircuitBreakerManager {
        private final ConcurrentHashMap<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();

        /**
         * 执行带熔断保护的方法
         */
        public <T> T execute(String name, CircuitBreakerSupplier<T> supplier, CircuitBreakerSupplier<T> fallback) {
            CircuitBreakerState state = circuitBreakers.get(name);
            if (state == null) {
                state = new CircuitBreakerState();
                circuitBreakers.put(name, state);
            }

            if (state.isOpen()) {
                log.warn("[熔断器] {} 处于开启状态，执行降级逻辑", name);
                try {
                    return fallback.get();
                } catch (Exception fallbackException) {
                    log.error("[熔断器] {} 降级逻辑也失败", name, fallbackException);
                    throw new RuntimeException("熔断器开启时降级逻辑失败", fallbackException);
                }
            }

            try {
                T result = supplier.get();
                state.recordSuccess();
                return result;
            } catch (Exception e) {
                state.recordFailure();
                log.warn("[熔断器] {} 执行失败，错误次数: {}/{}", name, state.failureCount.get(), state.failureThreshold);

                if (state.shouldOpen()) {
                    state.open();
                    log.warn("[熔断器] {} 已开启", name);
                }

                try {
                    return fallback.get();
                } catch (Exception fallbackException) {
                    log.error("[熔断器] {} 降级逻辑也失败", name, fallbackException);
                    throw new RuntimeException(fallbackException);
                }
            }
        }

        /**
         * 熔断器状态
         */
        private static class CircuitBreakerState {
            private static final int FAILURE_THRESHOLD = 5; // 失败阈值
            private static final long TIMEOUT_MS = 60000;   // 超时时间60秒

            private final AtomicInteger failureCount = new AtomicInteger(0);
            private final AtomicInteger successCount = new AtomicInteger(0);
            private final int failureThreshold = FAILURE_THRESHOLD;
            private volatile boolean isOpen = false;
            private volatile long lastFailureTime = 0;

            void recordSuccess() {
                successCount.incrementAndGet();
                if (successCount.get() >= 3) {
                    reset(); // 连续成功3次后重置
                }
            }

            void recordFailure() {
                failureCount.incrementAndGet();
                lastFailureTime = System.currentTimeMillis();
            }

            boolean shouldOpen() {
                return failureCount.get() >= FAILURE_THRESHOLD;
            }

            void open() {
                isOpen = true;
            }

            void close() {
                isOpen = false;
                reset();
            }

            void reset() {
                failureCount.set(0);
                successCount.set(0);
                lastFailureTime = 0;
            }

            boolean isOpen() {
                if (isOpen && System.currentTimeMillis() - lastFailureTime > TIMEOUT_MS) {
                    close(); // 超时后尝试关闭
                }
                return isOpen;
            }
        }
    }

    /**
     * 轻量级重试管理器
     */
    public static class LightRetryManager {

        /**
         * 执行带重试的方法
         */
        public <T> T executeWithRetry(RetrySupplier<T> supplier, int maxRetries) {
            Exception lastException = null;

            for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
                try {
                    T result = supplier.get();
                    if (attempt > 1) {
                        log.info("[重试] 第{}次尝试成功", attempt);
                    }
                    return result;
                } catch (Exception e) {
                    lastException = e;
                    if (attempt <= maxRetries) {
                        log.warn("[重试] 第{}次尝试失败: {}, 准备重试", attempt, e.getMessage());
                        try {
                            Thread.sleep(1000 * attempt); // 简单的退避策略
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("重试被中断", ie);
                        }
                    }
                }
            }

            log.error("[重试] 所有{}次尝试都失败了", maxRetries + 1);
            throw new RuntimeException("重试失败", lastException);
        }
    }

    @FunctionalInterface
    public interface CircuitBreakerSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface RetrySupplier<T> {
        T get() throws Exception;
    }
}