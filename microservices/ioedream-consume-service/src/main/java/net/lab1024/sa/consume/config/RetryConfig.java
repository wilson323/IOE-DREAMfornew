package net.lab1024.sa.consume.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 重试配置
 * <p>
 * 配置账户服务调用的重试策略
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-23
 */
@Configuration
@EnableRetry
public class RetryConfig {

    /**
     * 配置重试模板
     * <p>
     * 用于手动重试场景，如账户服务调用失败时的重试
     * </p>
     *
     * @return RetryTemplate 重试模板
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 固定间隔重试策略
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000L); // 1秒

        // 简单重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // 最多重试3次

        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
