package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.lab1024.sa.common.performance.JvmPerformanceManager;

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
@Configuration
@ConditionalOnProperty(name = "performance.jvm.monitoring.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({ JvmPerformanceProperties.class })
@Slf4j
public class JvmPerformanceAutoConfiguration {


    /**
     * 配置JVM性能管理器
     */
    @Bean
    public JvmPerformanceManager jvmPerformanceManager(JvmPerformanceProperties properties) {
        JvmPerformanceManager.JvmPerformanceConfig config = new JvmPerformanceManager.JvmPerformanceConfig();

        // 从配置属性转换到JVM性能配置
        // 确保monitoring和alerts不为null（JvmPerformanceProperties已初始化默认值）
        JvmPerformanceProperties.Monitoring monitoring = properties.getMonitoring() != null
                ? properties.getMonitoring()
                : new JvmPerformanceProperties.Monitoring();
        JvmPerformanceProperties.Alerts alerts = properties.getAlerts() != null
                ? properties.getAlerts()
                : new JvmPerformanceProperties.Alerts();

        config.setMonitoringEnabled(monitoring.getEnabled() != null ? monitoring.getEnabled() : true);
        config.setCollectionIntervalSeconds(monitoring.getCollectionIntervalSeconds() != null
                ? monitoring.getCollectionIntervalSeconds()
                : 60);
        config.setReportIntervalSeconds(monitoring.getReportIntervalSeconds() != null
                ? monitoring.getReportIntervalSeconds()
                : 300);

        JvmPerformanceProperties.Memory memory = alerts.getMemory() != null
                ? alerts.getMemory()
                : new JvmPerformanceProperties.Memory();
        JvmPerformanceProperties.Gc gc = alerts.getGc() != null
                ? alerts.getGc()
                : new JvmPerformanceProperties.Gc();
        JvmPerformanceProperties.Thread thread = alerts.getThread() != null
                ? alerts.getThread()
                : new JvmPerformanceProperties.Thread();

        config.setMemoryUsageThreshold(memory.getUsageThreshold() != null
                ? memory.getUsageThreshold()
                : 80.0);
        config.setGcPauseTimeThreshold(gc.getPauseTimeThreshold() != null
                ? gc.getPauseTimeThreshold()
                : 1000L);
        config.setThreadBlockedCountThreshold(thread.getBlockedCountThreshold() != null
                ? thread.getBlockedCountThreshold()
                : 10);

        JvmPerformanceManager manager = new JvmPerformanceManager(config);

        log.info("[JVM性能配置] JVM性能管理器已配置并启用");
        log.info("[JVM性能配置] 监控间隔: {}秒, 报告间隔: {}秒",
                config.getCollectionIntervalSeconds(), config.getReportIntervalSeconds());
        log.info("[JVM性能配置] 内存告警阈值: {}%, GC告警阈值: {}ms",
                config.getMemoryUsageThreshold(), config.getGcPauseTimeThreshold());

        return manager;
    }
}
