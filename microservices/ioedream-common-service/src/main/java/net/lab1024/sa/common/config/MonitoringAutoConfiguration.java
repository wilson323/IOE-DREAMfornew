package net.lab1024.sa.common.config;

import io.micrometer.core.instrument.MeterRegistry;
// 暂时移除Prometheus支持，后续可单独配置
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 监控自动配置类
 * <p>
 * 统一配置监控组件和指标收集器
 * 支持Micrometer、Prometheus、自定义告警等监控功能
 * 根据配置自动启用或禁用监控功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Configuration
@ConditionalOnProperty(name = "monitoring.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({MonitoringProperties.class})
@Import({
    AlertAutoConfiguration.class
})
public class MonitoringAutoConfiguration {

    /**
     * 自定义MeterRegistry配置
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
                "application", "ioedream-common-service",
                "version", "1.0.0",
                "region", System.getProperty("user.region", "default"),
                "instance", getInstanceId()
        );
    }

    /**
     * 通用MeterRegistry配置
     * Prometheus支持暂时禁用，可后续单独配置
     */
    // @Bean
    // @ConditionalOnProperty(name = "management.prometheus.metrics.export.enabled", havingValue = "true")
    // public PrometheusMeterRegistry prometheusMeterRegistry() {
    //     // Prometheus配置暂时禁用
    // }

    /**
     * 获取实例ID
     */
    private String getInstanceId() {
        return System.getenv().getOrDefault("INSTANCE_ID",
                System.getProperty("user.name", "unknown") + "-" + System.currentTimeMillis());
    }
}
