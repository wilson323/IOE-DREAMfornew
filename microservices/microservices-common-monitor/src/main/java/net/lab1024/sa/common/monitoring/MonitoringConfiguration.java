package net.lab1024.sa.common.monitoring;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import jakarta.annotation.PostConstruct;

/**
 * 企业级监控配置
 * <p>
 * 基于Micrometer + Prometheus的监控体系：
 * - 业务指标自动采集
 * - 性能指标监控
 * - 自定义指标定义
 * - 告警阈值配置
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@EnableAspectJAutoProxy
@ConditionalOnProperty(name = "management.metrics.enabled", havingValue = "true", matchIfMissing = true)
public class MonitoringConfiguration {

    /**
     * Micrometer监控注册器
     * 企业级监控组件
     */

    /**
     * Counted切面
     * 自动为带@Counted注解的方法生成计数器指标
     */
    @Bean
    @ConditionalOnClass(CountedAspect.class)
    @ConditionalOnMissingBean(name = "performanceMonitor")
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }

    /**
     * Timed切面
     * 自动为带@Timed注解的方法生成计时器指标
     */
    @Bean
    @ConditionalOnClass(TimedAspect.class)
    @ConditionalOnMissingBean(name = "performanceMonitor")
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * 注意：PerformanceMonitor和HealthCheckMonitor已移除
     * 原因：
     * - PerformanceMonitor：项目已有PerformanceMonitorManager提供更完善的性能监控
     * - HealthCheckMonitor：项目已有HealthCheckManager和Spring Boot Actuator提供健康检查
     * - Micrometer + Prometheus：标准监控体系已完整，无需重复实现
     */

    /**
     * 性能监控器占位Bean
     * <p>
     * 此方法用于防止与LightPerformanceConfiguration中的bean冲突
     * 使用@ConditionalOnMissingBean确保只有在没有其他performanceMonitor bean时才创建
     * 返回一个简单的占位对象，实际监控功能由Micrometer的CountedAspect和TimedAspect提供
     * </p>
     */
    @Bean(name = "performanceMonitor")
    @ConditionalOnMissingBean(name = "performanceMonitor")
    public String performanceMonitor() {
        // 返回一个占位字符串，表示监控功能由Micrometer提供
        // 这样可以防止Spring从其他配置类推断出同名的bean
        log.info("[监控配置] performanceMonitor占位bean已创建，实际监控由Micrometer提供");
        return "Micrometer-based monitoring";
    }

    /**
     * 自定义指标注册器
     * 注册业务特定的自定义指标
     */
    @PostConstruct
    public void registerCustomMetrics() {
        log.info("[监控配置] 开始注册自定义指标");

        // 这里可以注册项目特定的自定义指标
        // 例如：业务成功率、响应时间分布、错误率等

        log.info("[监控配置] 自定义指标注册完成");
    }
}
