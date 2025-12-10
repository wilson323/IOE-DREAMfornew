package net.lab1024.sa.common.monitoring;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

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
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }

    /**
     * Timed切面
     * 自动为带@Timed注解的方法生成计时器指标
     */
    @Bean
    @ConditionalOnClass(TimedAspect.class)
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * 业务指标收集器
     * 统一的业务指标收集和上报
     */
    @Bean
    public BusinessMetricsCollector businessMetricsCollector(MeterRegistry registry) {
        return new BusinessMetricsCollector(registry);
    }

    /**
     * 性能监控器
     * JVM、内存、GC等性能指标监控
     */
    @Bean
    public PerformanceMonitor performanceMonitor(MeterRegistry registry) {
        return new PerformanceMonitor(registry);
    }

    /**
     * 健康检查监控器
     * 服务健康状态和依赖检查
     */
    @Bean
    public HealthCheckMonitor healthCheckMonitor(MeterRegistry registry) {
        return new HealthCheckMonitor(registry);
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

    /**
     * 业务指标收集器
     */
    public static class BusinessMetricsCollector {
        @SuppressWarnings("unused") // 预留字段，用于未来指标注册扩展
        private final MeterRegistry meterRegistry;

        // 生物识别相关指标
        private final Timer biometricVerifyTimer;
        private final Timer biometricRegisterTimer;
        private final io.micrometer.core.instrument.Counter biometricSuccessCounter;
        private final io.micrometer.core.instrument.Counter biometricFailureCounter;

        // 设备通讯相关指标
        private final Timer deviceCommunicationTimer;
        private final io.micrometer.core.instrument.Counter deviceMessageCounter;
        private final io.micrometer.core.instrument.Counter deviceErrorCounter;

        // 门禁相关指标
        private final Timer accessControlTimer;
        private final io.micrometer.core.instrument.Counter accessSuccessCounter;
        private final io.micrometer.core.instrument.Counter accessFailureCounter;

        public BusinessMetricsCollector(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;

            // 初始化指标
            this.biometricVerifyTimer = Timer.builder("biometric.verify")
                    .description("生物识别验证耗时")
                    .register(meterRegistry);

            this.biometricRegisterTimer = Timer.builder("biometric.register")
                    .description("生物识别注册耗时")
                    .register(meterRegistry);

            this.biometricSuccessCounter = io.micrometer.core.instrument.Counter.builder("biometric.success")
                    .description("生物识别成功次数")
                    .register(meterRegistry);

            this.biometricFailureCounter = io.micrometer.core.instrument.Counter.builder("biometric.failure")
                    .description("生物识别失败次数")
                    .register(meterRegistry);

            this.deviceCommunicationTimer = Timer.builder("device.communication")
                    .description("设备通讯耗时")
                    .register(meterRegistry);

            this.deviceMessageCounter = io.micrometer.core.instrument.Counter.builder("device.message")
                    .description("设备消息处理次数")
                    .register(meterRegistry);

            this.deviceErrorCounter = io.micrometer.core.instrument.Counter.builder("device.error")
                    .description("设备通讯错误次数")
                    .register(meterRegistry);

            this.accessControlTimer = Timer.builder("access.control")
                    .description("门禁控制耗时")
                    .register(meterRegistry);

            this.accessSuccessCounter = io.micrometer.core.instrument.Counter.builder("access.success")
                    .description("门禁成功次数")
                    .register(meterRegistry);

            this.accessFailureCounter = io.micrometer.core.instrument.Counter.builder("access.failure")
                    .description("门禁失败次数")
                    .register(meterRegistry);
        }

        // 记录生物识别验证耗时
        public void recordBiometricVerify(String type, long duration, boolean success) {
            biometricVerifyTimer.record(duration, TimeUnit.MILLISECONDS);
            if (success) {
                biometricSuccessCounter.increment();
            } else {
                biometricFailureCounter.increment();
            }
        }

        // 记录生物识别注册耗时
        public void recordBiometricRegister(String type, long duration, boolean success) {
            biometricRegisterTimer.record(duration, TimeUnit.MILLISECONDS);
            if (success) {
                biometricSuccessCounter.increment();
            } else {
                biometricFailureCounter.increment();
            }
        }

        // 记录设备通讯耗时
        public void recordDeviceCommunication(String deviceType, long duration) {
            deviceCommunicationTimer.record(duration, TimeUnit.MILLISECONDS);
            deviceMessageCounter.increment();
        }

        // 记录设备错误
        public void recordDeviceError(String deviceType, String errorType) {
            deviceErrorCounter.increment();
        }

        // 记录门禁控制耗时
        public void recordAccessControl(String action, long duration, boolean success) {
            accessControlTimer.record(duration, TimeUnit.MILLISECONDS);
            if (success) {
                accessSuccessCounter.increment();
            } else {
                accessFailureCounter.increment();
            }
        }
    }
}
