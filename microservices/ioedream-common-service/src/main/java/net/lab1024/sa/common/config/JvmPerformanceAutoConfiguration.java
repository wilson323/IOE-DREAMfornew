package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.performance.JvmPerformanceManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JVM性能管理自动配置类
 * <p>
 * 配置JVM性能监控和分析组件
 * 提供性能调优建议和告警功能
 * 支持不同环境的性能配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "performance.jvm.monitoring.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({JvmPerformanceProperties.class})
public class JvmPerformanceAutoConfiguration {

    /**
     * 配置JVM性能管理器
     */
    @Bean
    public JvmPerformanceManager jvmPerformanceManager(JvmPerformanceProperties properties) {
        JvmPerformanceManager.JvmPerformanceConfig config = new JvmPerformanceManager.JvmPerformanceConfig();

        // 从配置属性转换到JVM性能配置
        config.setMonitoringEnabled(properties.getMonitoring().isEnabled());
        config.setCollectionIntervalSeconds(properties.getMonitoring().getCollectionIntervalSeconds());
        config.setReportIntervalSeconds(properties.getMonitoring().getReportIntervalSeconds());
        config.setMemoryUsageThreshold(properties.getAlerts().getMemory().getUsageThreshold());
        config.setGcPauseTimeThreshold(properties.getAlerts().getGc().getPauseTimeThreshold());
        config.setThreadBlockedCountThreshold(properties.getAlerts().getThread().getBlockedCountThreshold());

        JvmPerformanceManager manager = new JvmPerformanceManager(config);

        log.info("[JVM性能配置] JVM性能管理器已配置并启用");
        log.info("[JVM性能配置] 监控间隔: {}秒, 报告间隔: {}秒",
                config.getCollectionIntervalSeconds(), config.getReportIntervalSeconds());
        log.info("[JVM性能配置] 内存告警阈值: {}%, GC告警阈值: {}ms",
                config.getMemoryUsageThreshold(), config.getGcPauseTimeThreshold());

        return manager;
    }
}