package net.lab1024.sa.common.tracing;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式追踪配置
 * <p>
 * 企业级分布式追踪体系基础配置：
 * - 使用Micrometer Tracing + Brave + Zipkin
 * - 自动集成Spring Boot 3.x的追踪机制
 * - 可配置启用/禁用
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0 - 完整实现版
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "true", matchIfMissing = true)
public class TracingConfiguration {

    /**
     * 初始化分布式追踪配置
     */
    public TracingConfiguration() {
        log.info("[追踪配置] Micrometer Tracing分布式追踪配置已启用");
        log.info("[追踪配置] 使用Brave作为追踪实现，Zipkin作为追踪后端");
    }

    /**
     * 追踪器配置
     * Micrometer Tracing会自动配置，这里仅用于日志输出
     */
    @Bean
    public TracingInitializer tracingInitializer(Tracer tracer) {
        log.info("[追踪配置] Tracer已初始化: {}", tracer.getClass().getName());
        return new TracingInitializer(tracer);
    }

    /**
     * 追踪初始化器（内部类）
     */
    public static class TracingInitializer {
        private final Tracer tracer;

        public TracingInitializer(Tracer tracer) {
            this.tracer = tracer;
        }

        public Tracer getTracer() {
            return tracer;
        }
    }
}
