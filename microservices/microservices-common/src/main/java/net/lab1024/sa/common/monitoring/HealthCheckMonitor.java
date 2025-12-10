package net.lab1024.sa.common.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 健康检查监控器
 * 监控服务健康状态和依赖检查
 */
@Slf4j
public class HealthCheckMonitor {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger healthCheckCount = new AtomicInteger(0);
    private final AtomicInteger healthCheckFailures = new AtomicInteger(0);
    private final AtomicBoolean databaseHealthy = new AtomicBoolean(true);

    public HealthCheckMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    private void initializeMetrics() {
        // 使用Counter代替Gauge来计数
        Counter.builder("health.check.count")
                .description("健康检查执行次数")
                .register(meterRegistry);

        Counter.builder("health.check.failures")
                .description("健康检查失败次数")
                .register(meterRegistry);

        log.info("[健康监控] 健康检查监控已初始化");
    }

    /**
     * 检查数据库连接健康状态
     */
    public boolean checkDatabaseHealth(DataSource dataSource) {
        healthCheckCount.incrementAndGet();

        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                databaseHealthy.set(true);
                return true;
            }
        } catch (SQLException e) {
            log.error("[健康监控] 数据库健康检查失败", e);
            databaseHealthy.set(false);
            healthCheckFailures.incrementAndGet();

            // 记录健康检查失败指标
            Counter.builder("health.check.database.failure")
                    .description("数据库健康检查失败次数")
                    .register(meterRegistry)
                    .increment();
        }

        return false;
    }

    /**
     * 检查外部服务健康状态
     */
    public boolean checkExternalServiceHealth(String serviceName, String healthCheckUrl) {
        healthCheckCount.incrementAndGet();

        try {
            // 简化的HTTP健康检查
            // 实际实现应该使用HTTP客户端调用健康检查端点
            boolean healthy = simulateHealthCheck(serviceName);

            if (!healthy) {
                healthCheckFailures.incrementAndGet();
                Counter.builder("health.check.service.failure")
                        .description("外部服务健康检查失败次数")
                        .tag("service", serviceName)
                        .register(meterRegistry)
                        .increment();
            }

            return healthy;
        } catch (Exception e) {
            log.error("[健康监控] 外部服务健康检查失败: {}", serviceName, e);
            healthCheckFailures.incrementAndGet();
            return false;
        }
    }

    /**
     * 模拟健康检查（实际应该实现真实的检查逻辑）
     */
    private boolean simulateHealthCheck(String serviceName) {
        // 简化实现，实际应该调用真实的健康检查端点
        return true;
    }

    /**
     * 记录自定义健康指标
     */
    public void recordCustomHealthMetric(String metricName, double value, boolean healthy) {
        // 简化实现，避免复杂的Gauge使用
        log.info("[健康监控] 自定义健康指标: {}={}, 健康状态={}", metricName, value, healthy);

        if (!healthy) {
            Counter.builder("health.custom.failure")
                    .description("自定义健康指标失败次数")
                    .tag("metric", metricName)
                    .register(meterRegistry)
                    .increment();
        }
    }

    // Gauge方法
    public double getHealthCheckCount() {
        return healthCheckCount.get();
    }

    public double getHealthCheckFailures() {
        return healthCheckFailures.get();
    }

    public double getDatabaseHealthStatus() {
        return databaseHealthy.get() ? 1 : 0;
    }

    /**
     * 获取健康统计信息
     */
    public HealthStatus getHealthStatus() {
        int totalChecks = healthCheckCount.get();
        int failures = healthCheckFailures.get();
        double successRate = totalChecks > 0 ? (double) (totalChecks - failures) / totalChecks : 1.0;

        return new HealthStatus(
            totalChecks,
            failures,
            successRate,
            databaseHealthy.get()
        );
    }

    /**
     * 健康状态信息
     */
    public static class HealthStatus {
        private final int totalChecks;
        private final int failures;
        private final double successRate;
        private final boolean databaseHealthy;

        public HealthStatus(int totalChecks, int failures, double successRate, boolean databaseHealthy) {
            this.totalChecks = totalChecks;
            this.failures = failures;
            this.successRate = successRate;
            this.databaseHealthy = databaseHealthy;
        }

        // Getters
        public int getTotalChecks() { return totalChecks; }
        public int getFailures() { return failures; }
        public double getSuccessRate() { return successRate; }
        public boolean isDatabaseHealthy() { return databaseHealthy; }
    }
}
