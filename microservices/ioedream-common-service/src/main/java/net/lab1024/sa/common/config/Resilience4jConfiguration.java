package net.lab1024.sa.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * Resilience4j 容错熔断配置类
 * <p>
 * 企业级容错机制配置，提供统一的容错熔断、重试、限流、隔离配置
 * 严格遵循CLAUDE.md规范：使用Resilience4j标准方案，禁止自定义容错实现
 * </p>
 * <p>
 * 配置内容：
 * - 数据库访问容错配置（熔断、重试、限流、隔离、时间限制）
 * - Redis访问容错配置（熔断、重试）
 * - 外部服务调用容错配置（熔断、重试）
 * - 微服务间调用容错配置（熔断）
 * - 健康检查指示器
 * - 自定义异常Predicate
 * </p>
 * <p>
 * 注意：KeyResolver配置应在Gateway服务中，不在common-service中
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
public class Resilience4jConfiguration {

    // ============================================================
    // 数据库访问容错配置
    // ============================================================

    @Bean("databaseCircuitBreaker")
    public CircuitBreaker databaseCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(40f)                           // 40%失败率熔断
                .waitDurationInOpenState(Duration.ofSeconds(30))       // 熔断30秒
                .minimumNumberOfCalls(10)                             // 最少10次调用
                .permittedNumberOfCallsInHalfOpenState(5)             // 半开状态5次调用
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(100)                              // 滑动窗口100次
                .slowCallDurationThreshold(Duration.ofSeconds(10))    // 慢调用10秒
                .slowCallRateThreshold(60f)                          // 60%慢调用率
                .recordExceptions(
                        java.sql.SQLException.class,
                        java.util.concurrent.TimeoutException.class,
                        org.springframework.dao.QueryTimeoutException.class
                )
                .ignoreExceptions(
                        java.lang.IllegalArgumentException.class,
                        java.sql.SQLSyntaxErrorException.class
                )
                .build();

        CircuitBreaker circuitBreaker = registry.circuitBreaker("database-circuitbreaker", config);

        // 添加事件监听器
        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    log.info("[数据库熔断器] 状态转换: {} -> {}", event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState()))
                .onError(event ->
                    log.error("[数据库熔断器] 错误事件: duration={}, error={}",
                            event.getElapsedDuration(), event.getThrowable().getMessage()))
                .onSuccess(event ->
                    log.debug("[数据库熔断器] 成功事件: duration={}", event.getElapsedDuration()))
                .onIgnoredError(event ->
                    log.warn("[数据库熔断器] 忽略错误: {}", event.getThrowable().getMessage()));

        return circuitBreaker;
    }

    @Bean("databaseRetry")
    public Retry databaseRetry(RetryRegistry registry) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(2)                                          // 最多重试2次
                .waitDuration(Duration.ofMillis(500))                   // 等待500ms
                .retryExceptions(
                        java.sql.SQLException.class,
                        java.util.concurrent.TimeoutException.class,
                        org.springframework.dao.QueryTimeoutException.class
                )
                .ignoreExceptions(
                        java.sql.SQLSyntaxErrorException.class,
                        java.lang.IllegalArgumentException.class
                )
                .build();

        Retry retry = registry.retry("database-retry", config);

        // 添加事件监听器
        retry.getEventPublisher()
                .onRetry(event ->
                        log.warn("[数据库重试] 第{}次重试, error={}",
                                event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()))
                .onSuccess(event ->
                        log.info("[数据库重试] 重试成功, attempts={}", event.getNumberOfRetryAttempts()));

        return retry;
    }

    @Bean("databaseRateLimiter")
    public RateLimiter databaseRateLimiter(RateLimiterRegistry registry) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(200)                                     // 每秒200次请求
                .limitRefreshPeriod(Duration.ofSeconds(1))               // 1秒刷新
                .timeoutDuration(Duration.ofMillis(100))                 // 等待100ms
                .build();

        RateLimiter rateLimiter = registry.rateLimiter("database-ratelimiter", config);

        // 添加事件监听器
        rateLimiter.getEventPublisher()
                .onFailure(event ->
                        log.warn("[数据库限流] 限流拒绝"))
                .onSuccess(event ->
                        log.debug("[数据库限流] 限流通过"));

        return rateLimiter;
    }

    @Bean("databaseBulkhead")
    public Bulkhead databaseBulkhead(BulkheadRegistry registry) {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(50)                                // 最大50并发
                .maxWaitDuration(Duration.ofSeconds(1))                 // 等待1秒
                .build();

        Bulkhead bulkhead = registry.bulkhead("database-bulkhead", config);

        // 添加事件监听器
        bulkhead.getEventPublisher()
                .onCallPermitted(event ->
                        log.debug("[数据库舱壁] 调用允许, remaining={}", event.getBulkheadName()))
                .onCallRejected(event ->
                        log.warn("[数据库舱壁] 调用拒绝, remaining={}", event.getBulkheadName()));

        return bulkhead;
    }

    // ============================================================
    // Redis访问容错配置
    // ============================================================

    @Bean("redisCircuitBreaker")
    public CircuitBreaker redisCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(60f)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .minimumNumberOfCalls(15)
                .permittedNumberOfCallsInHalfOpenState(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(50)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .recordExceptions(
                        java.net.SocketException.class,
                        java.io.IOException.class,
                        org.springframework.dao.DataAccessException.class
                )
                .build();

        CircuitBreaker circuitBreaker = registry.circuitBreaker("redis-circuitbreaker", config);

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    log.info("[Redis熔断器] 状态转换: {} -> {}",
                            event.getStateTransition().getFromState(), event.getStateTransition().getToState()));

        return circuitBreaker;
    }

    @Bean("redisRetry")
    public Retry redisRetry(RetryRegistry registry) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofMillis(100))
                .retryExceptions(
                        java.net.SocketException.class,
                        java.io.IOException.class,
                        org.springframework.dao.DataAccessException.class
                )
                .build();

        return registry.retry("redis-retry", config);
    }

    // ============================================================
    // 外部服务调用容错配置
    // ============================================================

    @Bean("externalServiceCircuitBreaker")
    public CircuitBreaker externalServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(55f)
                .waitDurationInOpenState(Duration.ofSeconds(90))
                .minimumNumberOfCalls(15)
                .permittedNumberOfCallsInHalfOpenState(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(100)
                .slowCallDurationThreshold(Duration.ofSeconds(15))
                .recordExceptions(
                        org.springframework.web.client.ResourceAccessException.class,
                        java.net.SocketTimeoutException.class,
                        java.io.IOException.class
                )
                .build();

        CircuitBreaker circuitBreaker = registry.circuitBreaker("external-service-circuitbreaker", config);

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    log.info("[外部服务熔断器] 状态转换: {} -> {}",
                            event.getStateTransition().getFromState(), event.getStateTransition().getToState()));

        return circuitBreaker;
    }

    @Bean("externalServiceRetry")
    public Retry externalServiceRetry(RetryRegistry registry) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .retryExceptions(
                        org.springframework.web.client.ResourceAccessException.class,
                        java.net.SocketTimeoutException.class,
                        java.io.IOException.class
                )
                .build();

        return registry.retry("external-service-retry", config);
    }

    // ============================================================
    // 微服务间调用容错配置
    // ============================================================

    @Bean("microserviceCircuitBreaker")
    public CircuitBreaker microserviceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(45f)
                .waitDurationInOpenState(Duration.ofSeconds(45))
                .minimumNumberOfCalls(20)
                .permittedNumberOfCallsInHalfOpenState(15)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(200)
                .slowCallDurationThreshold(Duration.ofSeconds(8))
                .recordExceptions(
                        org.springframework.web.client.ResourceAccessException.class,
                        java.net.SocketTimeoutException.class,
                        java.io.IOException.class
                )
                .build();

        CircuitBreaker circuitBreaker = registry.circuitBreaker("microservice-circuitbreaker", config);

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    log.info("[微服务熔断器] 状态转换: {} -> {}",
                            event.getStateTransition().getFromState(), event.getStateTransition().getToState()));

        return circuitBreaker;
    }

    // ============================================================
    // 时间限制器配置
    // ============================================================

    @Bean("databaseTimeLimiter")
    public TimeLimiter databaseTimeLimiter(TimeLimiterRegistry registry) {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .cancelRunningFuture(true)
                .build();

        TimeLimiter timeLimiter = registry.timeLimiter("database-timelimiter", config);

        timeLimiter.getEventPublisher()
                .onTimeout(event ->
                        log.warn("[数据库时间限制器] 超时"))
                .onSuccess(event ->
                        log.debug("[数据库时间限制器] 完成"));

        return timeLimiter;
    }

    @Bean("externalServiceTimeLimiter")
    public TimeLimiter externalServiceTimeLimiter(TimeLimiterRegistry registry) {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(15))
                .cancelRunningFuture(true)
                .build();

        return registry.timeLimiter("external-service-timelimiter", config);
    }

    // ============================================================
    // 健康检查指示器
    // ============================================================

    @Bean
    public HealthIndicator resilience4jHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry,
                                                     RateLimiterRegistry rateLimiterRegistry,
                                                     BulkheadRegistry bulkheadRegistry) {
        return () -> {
            org.springframework.boot.actuate.health.Health.Builder builder = new org.springframework.boot.actuate.health.Health.Builder();

            // 检查熔断器状态
            circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
                CircuitBreaker.State state = circuitBreaker.getState();
                if (state == CircuitBreaker.State.OPEN) {
                    builder.down();
                    builder.withDetail(circuitBreaker.getName() + "_circuitbreaker", "OPEN");
                } else if (state == CircuitBreaker.State.HALF_OPEN) {
                    builder.unknown();
                    builder.withDetail(circuitBreaker.getName() + "_circuitbreaker", "HALF_OPEN");
                } else {
                    builder.up();
                    builder.withDetail(circuitBreaker.getName() + "_circuitbreaker", "CLOSED");
                }
            });

            // 检查限流器状态
            rateLimiterRegistry.getAllRateLimiters().forEach(rateLimiter -> {
                builder.withDetail(rateLimiter.getName() + "_ratelimiter",
                        "available=" + rateLimiter.getMetrics().getAvailablePermissions());
            });

            // 检查舱壁状态
            bulkheadRegistry.getAllBulkheads().forEach(bulkhead -> {
                builder.withDetail(bulkhead.getName() + "_bulkhead",
                        "available=" + bulkhead.getMetrics().getAvailableConcurrentCalls());
            });

            return builder.build();
        };
    }

    // ============================================================
    // 自定义异常 Predicate
    // ============================================================

    @Bean
    public Predicate<Throwable> retryableExceptionPredicate() {
        return throwable -> {
            // 可重试的异常类型
            return throwable instanceof java.net.SocketTimeoutException ||
                   throwable instanceof java.io.IOException ||
                   throwable instanceof org.springframework.web.client.ResourceAccessException ||
                   (throwable instanceof org.springframework.dao.DataAccessException &&
                    !(throwable instanceof org.springframework.dao.DataIntegrityViolationException));
        };
    }

    @Bean
    public Predicate<Throwable> nonRetryableExceptionPredicate() {
        return throwable -> {
            // 不可重试的异常类型
            return throwable instanceof java.lang.IllegalArgumentException ||
                   throwable instanceof org.springframework.security.authentication.BadCredentialsException ||
                   throwable instanceof org.springframework.dao.DataIntegrityViolationException;
        };
    }
}

