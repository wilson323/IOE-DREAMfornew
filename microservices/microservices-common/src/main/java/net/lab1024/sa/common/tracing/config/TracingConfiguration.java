package net.lab1024.sa.common.tracing.config;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.tracing.TracingUtils;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式追踪配置类
 * <p>
 * 配置Micrometer Tracing、Zipkin集成和自定义追踪组件
 * 提供统一的分布式追踪基础设施
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({ObservationProperties.class, MetricsProperties.class})
@ConditionalOnEnabledTracing
public class TracingConfiguration {

    /**
     * 配置追踪工具类
     *
     * @param tracer Tracer实例
     * @param propagator 传播器
     * @return TracingUtils实例
     */
    @Bean
    @ConditionalOnMissingBean
    public TracingUtils tracingUtils(Tracer tracer, Propagator propagator) {
        log.info("[分布式追踪] 初始化TracingUtils");
        return new TracingUtils();
    }

    /**
     * 配置自定义Span过滤器
     *
     * @return SpanCustomizer实例
     */
    @Bean
    public SpanCustomizer spanCustomizer() {
        return new SpanCustomizer();
    }
}

/**
 * Span自定义器
 * <p>
 * 为所有Span添加标准标签和事件
 * </p>
 */
class SpanCustomizer {

    /**
     * 自定义Span
     *
     * @param span Span对象
     * @param operationName 操作名称
     */
    public void customize(io.micrometer.tracing.Span span, String operationName) {
        // 添加标准标签
        span.tag("application.name", getApplicationName());
        span.tag("operation.name", operationName);
        span.tag("span.type", "custom");

        // 添加开始事件
        span.event("span.started");
    }

    private String getApplicationName() {
        // 从系统属性或环境变量获取应用名称
        return System.getProperty("spring.application.name", "unknown-application");
    }
}