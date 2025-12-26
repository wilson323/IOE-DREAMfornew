package net.lab1024.sa.common.monitoring.config;

import io.micrometer.core.instrument.MeterRegistry;
import net.lab1024.sa.common.monitoring.ExceptionMetricsCollector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控模块配置类
 * <p>
 * 注册监控相关的Bean，遵循CLAUDE.md架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 * @updated 从microservices-common迁移到细粒度模块
 */
@Configuration
public class MonitoringConfiguration {

    @Bean
    @ConditionalOnMissingBean(ExceptionMetricsCollector.class)
    public ExceptionMetricsCollector exceptionMetricsCollector(MeterRegistry meterRegistry) {
        return new ExceptionMetricsCollector(meterRegistry);
    }
}