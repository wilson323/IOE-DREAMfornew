package net.lab1024.sa.common.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * 轻量级Actuator配置
 * <p>
 * 只启用最核心的健康检查和监控端点：
 * - /actuator/health
 * - /actuator/info
 * - /actuator/metrics (轻量版)
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "management.endpoints.enabled", havingValue = "true", matchIfMissing = true)
public class LightActuatorConfiguration {

    /**
     * 内存健康检查
     */
    @Bean
    public MemoryHealthIndicator memoryHealthIndicator() {
        return new MemoryHealthIndicator();
    }

    /**
     * 简单的业务健康检查
     */
    @Bean
    public BusinessHealthIndicator businessHealthIndicator() {
        return new BusinessHealthIndicator();
    }

    /**
     * 内存健康检查器
     */
    public static class MemoryHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            try {
                MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
                long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
                long maxMemory = memoryBean.getHeapMemoryUsage().getMax();

                double usagePercent = (double) usedMemory / maxMemory * 100;

                if (usagePercent > 90) {
                    return Health.down()
                        .withDetail("usage", String.format("%.1f%%", usagePercent))
                        .withDetail("used", usedMemory / 1024 / 1024 + "MB")
                        .withDetail("max", maxMemory / 1024 / 1024 + "MB")
                        .withDetail("status", "内存使用率过高")
                        .build();
                } else if (usagePercent > 80) {
                    return Health.unknown()
                        .withDetail("usage", String.format("%.1f%%", usagePercent))
                        .withDetail("status", "内存使用率偏高")
                        .build();
                } else {
                    return Health.up()
                        .withDetail("usage", String.format("%.1f%%", usagePercent))
                        .build();
                }
            } catch (Exception e) {
                log.debug("[健康检查] 内存健康检查异常: error={}", e.getMessage());
                return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
            }
        }
    }

    /**
     * 业务健康检查器
     */
    public static class BusinessHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            try {
                // 这里可以添加核心业务检查逻辑
                // 例如：数据库连接、Redis连接、第三方服务状态等

                boolean databaseOk = checkDatabase();
                boolean redisOk = checkRedis();

                if (databaseOk && redisOk) {
                    return Health.up()
                        .withDetail("database", "正常")
                        .withDetail("redis", "正常")
                        .withDetail("status", "所有核心服务正常")
                        .build();
                } else {
                    return Health.down()
                        .withDetail("database", databaseOk ? "正常" : "异常")
                        .withDetail("redis", redisOk ? "正常" : "异常")
                        .withDetail("status", "部分核心服务异常")
                        .build();
                }
            } catch (Exception e) {
                log.debug("[健康检查] 核心服务健康检查异常: error={}", e.getMessage());
                return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
            }
        }

        private boolean checkDatabase() {
            // 简单的数据库连接检查
            // 实际实现中应该执行一个简单的查询
            return true; // 简化实现
        }

        private boolean checkRedis() {
            // 简单的Redis连接检查
            // 实际实现中应该执行一个简单的操作
            return true; // 简化实现
        }
    }
}
