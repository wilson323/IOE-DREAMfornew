package net.lab1024.sa.common.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * 轻量级监控配置
 * <p>
 * 避免过度工程化，只保留核心监控功能：
 * - HTTP请求监控
 * - 业务方法计时
 * - 基础性能指标
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "management.metrics.enabled", havingValue = "true", matchIfMissing = true)
public class LightMonitoringConfiguration {

    /**
     * 轻量级业务指标收集器
     * 只包含核心业务指标，避免过度复杂
     */
    @Bean
    public LightMetricsCollector lightMetricsCollector(MeterRegistry registry) {
        return new LightMetricsCollector(registry);
    }

    /**
     * 初始化轻量级监控
     */
    @PostConstruct
    public void initLightMonitoring() {
        log.info("[轻量监控] 已启用 - 核心业务指标监控");
    }

    /**
     * 轻量级业务指标收集器
     */
    public static class LightMetricsCollector {
        @SuppressWarnings("unused") // 预留字段，用于未来指标注册扩展
        private final MeterRegistry meterRegistry;

        // 核心业务计时器
        private final Timer businessTimer;
        private final Timer httpTimer;

        // 核心业务计数器
        private final io.micrometer.core.instrument.Counter businessCounter;
        private final io.micrometer.core.instrument.Counter errorCounter;

        public LightMetricsCollector(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;

            // 只创建最核心的指标
            this.businessTimer = Timer.builder("business.execution")
                    .description("业务方法执行时间")
                    .register(meterRegistry);

            this.httpTimer = Timer.builder("http.request")
                    .description("HTTP请求处理时间")
                    .register(meterRegistry);

            this.businessCounter = io.micrometer.core.instrument.Counter.builder("business.count")
                    .description("业务操作次数")
                    .register(meterRegistry);

            this.errorCounter = io.micrometer.core.instrument.Counter.builder("business.error")
                    .description("业务错误次数")
                    .register(meterRegistry);
        }

        /**
         * 记录业务方法执行时间
         */
        public void recordBusiness(String operation, long duration, boolean success) {
            businessTimer.record(duration, TimeUnit.MILLISECONDS);
            businessCounter.increment();

            if (!success) {
                errorCounter.increment();
            }
        }

        /**
         * 记录HTTP请求时间
         */
        public void recordHttp(String method, String uri, long duration, int status) {
            httpTimer.record(duration, TimeUnit.MILLISECONDS);

            if (status >= 400) {
                errorCounter.increment();
            }
        }

        /**
         * 获取简单统计信息
         */
        public String getSimpleStats() {
            return String.format(
                "业务执行: avg=%.2fms, 成功率=%.1f%%, HTTP请求: avg=%.2fms",
                businessTimer.mean(TimeUnit.MILLISECONDS),
                calculateSuccessRate(),
                httpTimer.mean(TimeUnit.MILLISECONDS)
            );
        }

        private double calculateSuccessRate() {
            double totalCount = businessCounter.count();
            double errorCount = errorCounter.count();
            return totalCount > 0 ? ((totalCount - errorCount) / totalCount * 100) : 100;
        }
    }
}
