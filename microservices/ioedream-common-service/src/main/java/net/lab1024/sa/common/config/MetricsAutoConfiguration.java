package net.lab1024.sa.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitoring.MetricsCollector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 指标自动配置类
 * <p>
 * 配置MetricsCollector和相关监控组件
 * 提供企业级指标收集和监控功能
 * 支持HTTP、数据库、缓存、业务等全方位监控
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "monitoring.performance.enabled", havingValue = "true", matchIfMissing = true)
public class MetricsAutoConfiguration {

    /**
     * 配置MetricsCollector
     */
    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    public MetricsCollector metricsCollector(MeterRegistry meterRegistry) {
        MetricsCollector collector = new MetricsCollector(meterRegistry);

        log.info("[指标配置] MetricsCollector 已配置并启用");
        log.info("[指标配置] 已注册内置业务指标和性能指标");

        return collector;
    }
}