package net.lab1024.sa.attendance.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * Resilience4j容错配置
 * <p>
 * 配置API限流、重试、熔断等容错机制，提升系统稳定性
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Configuration
public class Resilience4jConfiguration {

    /**
     * 重试配置
     * <p>
     * 用于API调用失败后的重试策略
     * - 最大重试次数: 3次
     * - 重试间隔: 指数退避（100ms, 200ms, 400ms）
     * - 重试异常: Exception
     * </p>
     */
    @Bean
    public RetryRegistry retryRegistry() {
        log.info("[容错配置] 初始化重试注册器");

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3) // 最大重试次数
                .waitDuration(Duration.ofMillis(100)) // 初始等待时间
                .intervalFunction(IntervalFunction.ofExponentialBackoff(100, 2)) // 指数退避
                .retryExceptions(Exception.class) // 重试异常类型
                .ignoreExceptions(IllegalArgumentException.class) // 忽略异常类型
                .build();

        return RetryRegistry.of(config);
    }

    /**
     * Dashboard API重试配置
     */
    @Bean
    public Retry dashboardRetry(RetryRegistry registry) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(2) // Dashboard API重试2次
                .waitDuration(Duration.ofMillis(50))
                .intervalFunction(IntervalFunction.ofExponentialBackoff(50, 2))
                .retryExceptions(Exception.class)
                .build();

        return registry.retry("dashboardRetry", config);
    }

    /**
     * 时间限制器配置
     * <p>
     * 用于防止API调用时间过长
     * - 超时时间: 5秒
     * - 取消运行时: true
     * </p>
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        log.info("[容错配置] 初始化时间限制器注册器");

        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5)) // 超时时间5秒
                .cancelRunningFuture(true) // 取消运行中的Future
                .build();

        return TimeLimiterRegistry.of(config);
    }

    /**
     * Dashboard API时间限制器
     */
    @Bean
    public TimeLimiter dashboardTimeLimiter(TimeLimiterRegistry registry) {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3)) // Dashboard API超时3秒
                .cancelRunningFuture(true)
                .build();

        return registry.timeLimiter("dashboardTimeLimiter", config);
    }
}
