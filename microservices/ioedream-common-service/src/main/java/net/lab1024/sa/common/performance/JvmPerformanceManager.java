package net.lab1024.sa.common.performance;

import lombok.extern.slf4j.Slf4j;


/**
 * JVM性能管理器
 * <p>
 * 职责：
 * - JVM性能监控
 * - GC性能分析
 * - 内存使用分析
 * - 线程状态监控
 * </p>
 * <p>
 * 注意：Manager类是纯Java类，不使用Spring注解，通过构造函数注入依赖。
 * 在微服务中通过配置类将Manager注册为Spring Bean。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Slf4j
public class JvmPerformanceManager {


    private final JvmPerformanceConfig config;

    public JvmPerformanceManager(JvmPerformanceConfig config) {
        this.config = config;
        log.info("[JvmPerformanceManager] 初始化JVM性能管理器");
    }

    /**
     * JVM性能配置
     */
    public static class JvmPerformanceConfig {
        private Boolean monitoringEnabled;
        private Integer collectionIntervalSeconds;
        private Integer reportIntervalSeconds;
        private Double memoryUsageThreshold;
        private Long gcPauseTimeThreshold;
        private Integer threadBlockedCountThreshold;

        public Boolean getMonitoringEnabled() {
            return monitoringEnabled;
        }

        public void setMonitoringEnabled(Boolean monitoringEnabled) {
            this.monitoringEnabled = monitoringEnabled;
        }

        public Integer getCollectionIntervalSeconds() {
            return collectionIntervalSeconds;
        }

        public void setCollectionIntervalSeconds(Integer collectionIntervalSeconds) {
            this.collectionIntervalSeconds = collectionIntervalSeconds;
        }

        public Integer getReportIntervalSeconds() {
            return reportIntervalSeconds;
        }

        public void setReportIntervalSeconds(Integer reportIntervalSeconds) {
            this.reportIntervalSeconds = reportIntervalSeconds;
        }

        public Double getMemoryUsageThreshold() {
            return memoryUsageThreshold;
        }

        public void setMemoryUsageThreshold(Double memoryUsageThreshold) {
            this.memoryUsageThreshold = memoryUsageThreshold;
        }

        public Long getGcPauseTimeThreshold() {
            return gcPauseTimeThreshold;
        }

        public void setGcPauseTimeThreshold(Long gcPauseTimeThreshold) {
            this.gcPauseTimeThreshold = gcPauseTimeThreshold;
        }

        public Integer getThreadBlockedCountThreshold() {
            return threadBlockedCountThreshold;
        }

        public void setThreadBlockedCountThreshold(Integer threadBlockedCountThreshold) {
            this.threadBlockedCountThreshold = threadBlockedCountThreshold;
        }
    }

    public JvmPerformanceConfig getConfig() {
        return config;
    }
}
