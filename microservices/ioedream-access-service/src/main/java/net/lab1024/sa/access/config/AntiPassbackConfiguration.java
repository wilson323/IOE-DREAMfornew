package net.lab1024.sa.access.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

/**
 * 门禁反潜回配置
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Configuration注解标识配置类
 * - 配置Resilience4j容错机制
 * - 配置Redis连接和序列化
 * - 设置合理的超时和重试参数
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
public class AntiPassbackConfiguration {

    /**
     * 配置Redis模板
     */
    @Bean
    public RedisTemplate<String, Object> antiPassbackRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用String序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 使用JSON序列化器
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置反潜回服务熔断器
     */
    @Bean("antiPassbackService")
    public CircuitBreaker antiPassbackCircuitBreaker() {
        return CircuitBreaker.of("antiPassbackService", CircuitBreakerConfig.custom()
                .failureRateThreshold(50)           // 失败率阈值50%
                .waitDurationInOpenState(Duration.ofSeconds(30))  // 熔断开启时间30秒
                .slidingWindowSize(20)              // 滑动窗口大小20
                .minimumNumberOfCalls(10)           // 最少调用次数10
                .slowCallDurationThreshold(Duration.ofSeconds(3))  // 慢调用阈值3秒
                .slowCallRateThreshold(50)          // 慢调用率阈值50%
                .build());
    }

    /**
     * 配置反潜回服务限流器
     */
    @Bean("antiPassbackService")
    public RateLimiter antiPassbackRateLimiter() {
        return RateLimiter.of("antiPassbackService", RateLimiterConfig.custom()
                .limitForPeriod(100)               // 每个周期允许100个请求
                .limitRefreshPeriod(Duration.ofSeconds(1))  // 刷新周期1秒
                .timeoutDuration(Duration.ofSeconds(5))      // 等待超时5秒
                .build());
    }

    /**
     * 配置反潜回服务重试器
     */
    @Bean("antiPassbackService")
    public Retry antiPassbackRetry() {
        return Retry.of("antiPassbackService", RetryConfig.custom()
                .maxAttempts(3)                     // 最大重试次数3
                .waitDuration(Duration.ofMillis(500)) // 重试间隔500毫秒
                .retryExceptions(Exception.class)   // 重试异常类型
                .build());
    }

    /**
     * 配置反潜回服务时间限制器
     */
    @Bean("antiPassbackService")
    public TimeLimiter antiPassbackTimeLimiter() {
        return TimeLimiter.of("antiPassbackService", TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))  // 超时时间10秒
                .cancelRunningFuture(true)         // 取消运行中的Future
                .build());
    }
}