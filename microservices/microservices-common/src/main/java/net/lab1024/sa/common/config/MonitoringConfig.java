package net.lab1024.sa.common.config;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 微服务监控配置 - 最新版Micrometer API
 * 提供业务指标监控和健康检查功能
 *
 * @author 老王重写 - 2025年
 */
@Configuration
public class MonitoringConfig {

    private final ConcurrentHashMap<String, AtomicLong> businessCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer.Sample> activeSamples = new ConcurrentHashMap<>();

    /**
     * 业务指标收集器
     */
    @Bean
    public MeterBinder businessMetrics() {
        return registry -> {
            // 业务交易计数器
            Counter.builder("business.transactions.count")
                    .description("业务交易总数")
                    .tags("type", "business")
                    .register(registry);

            // 成功交易计数器
            Counter.builder("business.transactions.success")
                    .description("成功业务交易数")
                    .tags("type", "business", "status", "success")
                    .register(registry);

            // 失败交易计数器
            Counter.builder("business.transactions.failure")
                    .description("失败业务交易数")
                    .tags("type", "business", "status", "failure")
                    .register(registry);

            // 数据库连接池使用率
            registry.gauge("database.connections.active", getActiveDatabaseConnections());

            // 缓存命中率
            registry.gauge("cache.hit.ratio", getCacheHitRatio());
        };
    }

    /**
     * 开始业务事务计时
     */
    public void startBusinessTimer(String transactionId, MeterRegistry registry) {
        Timer.Sample sample = Timer.start(registry);
        activeSamples.put(transactionId, sample);
    }

    /**
     * 结束业务事务计时
     */
    public void endBusinessTimer(String transactionId, String status, MeterRegistry registry) {
        Timer.Sample sample = activeSamples.remove(transactionId);
        if (sample != null) {
            sample.stop(Timer.builder("business.transaction.duration")
                    .description("业务事务执行时间")
                    .tags("status", status)
                    .register(registry));
        }
    }

    /**
     * 记录业务计数
     */
    public void recordBusinessCount(String metricName, long delta, MeterRegistry registry) {
        businessCounters.computeIfAbsent(metricName, k -> new AtomicLong(0)).addAndGet(delta);

        Counter.builder("business.custom.count")
                .description("自定义业务计数")
                .tags("metric", metricName)
                .register(registry)
                .increment(delta);
    }

    /**
     * 系统健康指示器
     */
    @Bean
    public HealthIndicator systemHealthIndicator() {
        return () -> {
            try {
                // 检查内存使用率
                double memoryUsage = getMemoryUsagePercentage();
                if (memoryUsage > 90) {
                    return Health.down()
                            .withDetail("memory_usage", memoryUsage + "%")
                            .withDetail("error", "内存使用率过高")
                            .build();
                }

                // 检查磁盘空间（简化版）
                double diskUsage = getDiskUsagePercentage();
                if (diskUsage > 85) {
                    return Health.down()
                            .withDetail("disk_usage", diskUsage + "%")
                            .withDetail("error", "磁盘使用率过高")
                            .build();
                }

                // 检查活跃连接数
                int activeConnections = getActiveDatabaseConnections();
                if (activeConnections > 80) {
                    return Health.down()
                            .withDetail("active_connections", activeConnections)
                            .withDetail("error", "活跃数据库连接数过多")
                            .build();
                }

                return Health.up()
                        .withDetail("memory_usage", memoryUsage + "%")
                        .withDetail("disk_usage", diskUsage + "%")
                        .withDetail("active_connections", activeConnections)
                        .withDetail("status", "系统运行正常")
                        .build();

            } catch (Exception e) {
                return Health.down()
                        .withDetail("error", "健康检查异常: " + e.getMessage())
                        .build();
            }
        };
    }

    /**
     * 数据库健康指示器
     */
    @Bean
    public HealthIndicator databaseHealthIndicator() {
        return () -> {
            try {
                int activeConnections = getActiveDatabaseConnections();
                int totalConnections = getTotalDatabaseConnections();

                if (activeConnections == 0) {
                    return Health.down()
                            .withDetail("error", "数据库无活跃连接")
                            .build();
                }

                double connectionUsageRatio = (double) activeConnections / totalConnections;
                if (connectionUsageRatio > 0.9) {
                    return Health.down()
                            .withDetail("active_connections", activeConnections)
                            .withDetail("total_connections", totalConnections)
                            .withDetail("usage_ratio", connectionUsageRatio)
                            .withDetail("error", "数据库连接使用率过高")
                            .build();
                }

                return Health.up()
                        .withDetail("active_connections", activeConnections)
                        .withDetail("total_connections", totalConnections)
                        .withDetail("usage_ratio", connectionUsageRatio)
                        .build();

            } catch (Exception e) {
                return Health.down()
                        .withDetail("error", "数据库健康检查异常: " + e.getMessage())
                        .build();
            }
        };
    }

    /**
     * 缓存健康指示器
     */
    @Bean
    public HealthIndicator cacheHealthIndicator() {
        return () -> {
            try {
                double hitRatio = getCacheHitRatio();

                if (hitRatio < 0.3) {
                    return Health.down()
                            .withDetail("hit_ratio", hitRatio)
                            .withDetail("error", "缓存命中率过低")
                            .build();
                }

                return Health.up()
                        .withDetail("hit_ratio", hitRatio)
                        .withDetail("status", "缓存运行正常")
                        .build();

            } catch (Exception e) {
                return Health.down()
                        .withDetail("error", "缓存健康检查异常: " + e.getMessage())
                        .build();
            }
        };
    }

    // 辅助方法实现

    private double getMemoryUsagePercentage() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            return ((double) (totalMemory - freeMemory) / totalMemory) * 100;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double getDiskUsagePercentage() {
        // 简化实现，实际应该检查具体磁盘使用情况
        try {
            java.io.File disk = new java.io.File("/");
            long totalSpace = disk.getTotalSpace();
            long freeSpace = disk.getFreeSpace();
            return ((double) (totalSpace - freeSpace) / totalSpace) * 100;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private int getActiveDatabaseConnections() {
        // 简化实现，实际应该从数据源获取
        return 10; // 模拟值
    }

    private int getTotalDatabaseConnections() {
        // 简化实现，实际应该从数据源配置获取
        return 50; // 模拟值
    }

    private double getCacheHitRatio() {
        // 简化实现，实际应该从缓存统计获取
        return 0.85; // 模拟值
    }
}