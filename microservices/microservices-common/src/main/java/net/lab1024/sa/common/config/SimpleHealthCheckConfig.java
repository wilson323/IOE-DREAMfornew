package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化的健康检查配置 - 企业级标准
 * 提供核心健康检查功能，避免复杂的API依赖问题
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-30
 */
@Slf4j
@Configuration
public class SimpleHealthCheckConfig {

    /**
     * 基础系统健康检查
     */
    @Bean
    public HealthIndicator systemHealth() {
        return () -> {
            try {
                Map<String, Object> details = new HashMap<>();

                // JVM信息
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;
                long maxMemory = runtime.maxMemory();

                double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

                details.put("totalMemoryMB", totalMemory / 1024 / 1024);
                details.put("usedMemoryMB", usedMemory / 1024 / 1024);
                details.put("freeMemoryMB", freeMemory / 1024 / 1024);
                details.put("maxMemoryMB", maxMemory / 1024 / 1024);
                details.put("memoryUsagePercent", String.format("%.2f%%", memoryUsagePercent));
                details.put("availableProcessors", runtime.availableProcessors());

                // 启动时间
                long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
                long uptime = System.currentTimeMillis() - startTime;
                details.put("uptimeSeconds", uptime / 1000);
                details.put("uptimeFormatted", formatUptime(uptime));

                // 根据内存使用率判断健康状态
                if (memoryUsagePercent <= 85) {
                    return Health.up()
                            .withDetails(details)
                            .build();
                } else if (memoryUsagePercent <= 95) {
                    return Health.down()
                            .withDetail("status", "WARNING")
                            .withDetails(details)
                            .build();
                } else {
                    return Health.down()
                            .withDetail("status", "CRITICAL")
                            .withDetails(details)
                            .build();
                }

            } catch (Exception e) {
                log.error("系统健康检查失败", e);
                return Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * 应用状态健康检查
     */
    @Bean
    public HealthIndicator applicationHealth() {
        return () -> {
            Map<String, Object> details = new HashMap<>();
            details.put("application", "IOE-DREAM Microservices");
            details.put("status", "RUNNING");
            details.put("timestamp", System.currentTimeMillis());
            details.put("version", "1.0.0");
            details.put("profile", System.getProperty("spring.profiles.active", "default"));

            return Health.up()
                    .withDetails(details)
                    .build();
        };
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime(long uptimeMs) {
        long days = uptimeMs / (24 * 60 * 60 * 1000);
        long hours = (uptimeMs % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (uptimeMs % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (uptimeMs % (60 * 1000)) / 1000;

        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append("天");
        }
        if (hours > 0) {
            result.append(hours).append("小时");
        }
        if (minutes > 0) {
            result.append(minutes).append("分钟");
        }
        if (seconds > 0 || result.length() == 0) {
            result.append(seconds).append("秒");
        }

        return result.toString();
    }
}