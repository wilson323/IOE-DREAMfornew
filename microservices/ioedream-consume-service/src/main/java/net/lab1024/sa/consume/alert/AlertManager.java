package net.lab1024.sa.consume.alert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 告警管理器
 *
 * 职责：监控系统指标并触发告警
 *
 * 告警类型：
 * 1. 性能告警：TPS下降、响应时间超时
 * 2. 错误告警：错误率过高、系统异常
 * 3. 业务告警：余额不足、消费异常
 * 4. 资源告警：CPU、内存、连接池
 *
 * 告警级别：
 * - INFO：信息
 * - WARN：警告
 * - ERROR：错误
 * - CRITICAL：严重
 *
 * 告警规则：
 * - TPS < 100 → WARN
 * - TPS < 50 → ERROR
 * - 响应时间 > 100ms → WARN
 * - 响应时间 > 500ms → ERROR
 * - 错误率 > 1% → WARN
 * - 错误率 > 5% → ERROR
 * - 缓存命中率 < 80% → WARN
 * - 缓存命中率 < 70% → ERROR
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
@Component
public class AlertManager {

    /**
     * 告警记录
     */
    private final Map<String, List<Alert>> alertHistory = new ConcurrentHashMap<>();

    /**
     * 告警计数器
     */
    private final Map<String, AtomicLong> alertCounters = new ConcurrentHashMap<>();

    /**
     * 性能阈值配置
     */
    private final PerformanceThresholds thresholds = new PerformanceThresholds();

    /**
     * 检查性能指标并触发告警
     *
     * @param metrics 性能指标
     * @return 触发的告警列表
     */
    public List<Alert> checkAndAlert(PerformanceMetrics metrics) {
        List<Alert> alerts = new ArrayList<>();

        // 1. 检查TPS
        if (metrics.getTps() < thresholds.getTpsErrorThreshold()) {
            alerts.add(createAlert(AlertLevel.ERROR, "TPS过低",
                    String.format("当前TPS: %.2f, 最低要求: %.2f",
                            metrics.getTps(), thresholds.getTpsErrorThreshold())));
        } else if (metrics.getTps() < thresholds.getTpsWarnThreshold()) {
            alerts.add(createAlert(AlertLevel.WARN, "TPS偏低",
                    String.format("当前TPS: %.2f, 建议值: %.2f",
                            metrics.getTps(), thresholds.getTpsWarnThreshold())));
        }

        // 2. 检查响应时间
        if (metrics.getAverageResponseTime() > thresholds.getResponseTimeErrorThreshold()) {
            alerts.add(createAlert(AlertLevel.ERROR, "响应时间过长",
                    String.format("当前响应时间: %.2fms, 最高允许: %.2fms",
                            metrics.getAverageResponseTime(), thresholds.getResponseTimeErrorThreshold())));
        } else if (metrics.getAverageResponseTime() > thresholds.getResponseTimeWarnThreshold()) {
            alerts.add(createAlert(AlertLevel.WARN, "响应时间偏高",
                    String.format("当前响应时间: %.2fms, 建议值: %.2fms",
                            metrics.getAverageResponseTime(), thresholds.getResponseTimeWarnThreshold())));
        }

        // 3. 检查错误率
        if (metrics.getErrorRate() > thresholds.getErrorRateErrorThreshold()) {
            alerts.add(createAlert(AlertLevel.ERROR, "错误率过高",
                    String.format("当前错误率: %.2f%%, 最高允许: %.2f%%",
                            metrics.getErrorRate() * 100, thresholds.getErrorRateErrorThreshold() * 100)));
        } else if (metrics.getErrorRate() > thresholds.getErrorRateWarnThreshold()) {
            alerts.add(createAlert(AlertLevel.WARN, "错误率偏高",
                    String.format("当前错误率: %.2f%%, 建议值: %.2f%%",
                            metrics.getErrorRate() * 100, thresholds.getErrorRateWarnThreshold() * 100)));
        }

        // 4. 检查缓存命中率
        if (metrics.getCacheHitRate() < thresholds.getCacheHitRateErrorThreshold()) {
            alerts.add(createAlert(AlertLevel.ERROR, "缓存命中率过低",
                    String.format("当前缓存命中率: %.2f%%, 最低要求: %.2f%%",
                            metrics.getCacheHitRate() * 100, thresholds.getCacheHitRateErrorThreshold() * 100)));
        } else if (metrics.getCacheHitRate() < thresholds.getCacheHitRateWarnThreshold()) {
            alerts.add(createAlert(AlertLevel.WARN, "缓存命中率偏低",
                    String.format("当前缓存命中率: %.2f%%, 建议值: %.2f%%",
                            metrics.getCacheHitRate() * 100, thresholds.getCacheHitRateWarnThreshold() * 100)));
        }

        // 记录告警
        for (Alert alert : alerts) {
            recordAlert(alert);
        }

        return alerts;
    }

    /**
     * 创建告警
     *
     * @param level 告警级别
     * @param title 告警标题
     * @param message 告警消息
     * @return 告警对象
     */
    public Alert createAlert(AlertLevel level, String title, String message) {
        Alert alert = new Alert();
        alert.setLevel(level);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setTimestamp(LocalDateTime.now());
        alert.setId(generateAlertId());

        return alert;
    }

    /**
     * 记录告警
     *
     * @param alert 告警对象
     */
    public void recordAlert(Alert alert) {
        String level = alert.getLevel().name();

        alertHistory.computeIfAbsent(level, k -> new ArrayList<>()).add(alert);
        alertCounters.computeIfAbsent(level, k -> new AtomicLong(0)).incrementAndGet();

        // 记录日志
        switch (alert.getLevel()) {
            case CRITICAL:
            case ERROR:
                log.error("[告警] {}: {}", alert.getTitle(), alert.getMessage());
                break;
            case WARN:
                log.warn("[告警] {}: {}", alert.getTitle(), alert.getMessage());
                break;
            case INFO:
                log.info("[告警] {}: {}", alert.getTitle(), alert.getMessage());
                break;
        }

        // TODO: 发送告警通知（邮件、短信、钉钉等）
        sendAlertNotification(alert);
    }

    /**
     * 发送告警通知
     *
     * @param alert 告警对象
     */
    private void sendAlertNotification(Alert alert) {
        // TODO: 实现告警通知逻辑
        // 1. 邮件通知
        // 2. 短信通知
        // 3. 钉钉机器人
        // 4. 企业微信
        log.info("[告警通知] 发送通知: level={}, title={}", alert.getLevel(), alert.getTitle());
    }

    /**
     * 获取告警统计
     *
     * @return 告警统计
     */
    public AlertStatistics getAlertStatistics() {
        AlertStatistics statistics = new AlertStatistics();

        for (Map.Entry<String, AtomicLong> entry : alertCounters.entrySet()) {
            String level = entry.getKey();
            long count = entry.getValue().get();

            switch (level) {
                case "CRITICAL":
                    statistics.setCriticalCount(count);
                    break;
                case "ERROR":
                    statistics.setErrorCount(count);
                    break;
                case "WARN":
                    statistics.setWarnCount(count);
                    break;
                case "INFO":
                    statistics.setInfoCount(count);
                    break;
            }
        }

        return statistics;
    }

    /**
     * 生成告警ID
     */
    private String generateAlertId() {
        return "ALERT-" + System.currentTimeMillis();
    }

    /**
     * 性能阈值配置
     */
    public static class PerformanceThresholds {
        private double tpsWarnThreshold = 500.0;
        private double tpsErrorThreshold = 100.0;
        private double responseTimeWarnThreshold = 50.0;
        private double responseTimeErrorThreshold = 500.0;
        private double errorRateWarnThreshold = 0.01;  // 1%
        private double errorRateErrorThreshold = 0.05; // 5%
        private double cacheHitRateWarnThreshold = 0.80; // 80%
        private double cacheHitRateErrorThreshold = 0.70; // 70%

        // Getters
        public double getTpsWarnThreshold() { return tpsWarnThreshold; }
        public double getTpsErrorThreshold() { return tpsErrorThreshold; }
        public double getResponseTimeWarnThreshold() { return responseTimeWarnThreshold; }
        public double getResponseTimeErrorThreshold() { return responseTimeErrorThreshold; }
        public double getErrorRateWarnThreshold() { return errorRateWarnThreshold; }
        public double getErrorRateErrorThreshold() { return errorRateErrorThreshold; }
        public double getCacheHitRateWarnThreshold() { return cacheHitRateWarnThreshold; }
        public double getCacheHitRateErrorThreshold() { return cacheHitRateErrorThreshold; }
    }

    /**
     * 告警对象
     */
    public static class Alert {
        private String id;
        private AlertLevel level;
        private String title;
        private String message;
        private LocalDateTime timestamp;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public AlertLevel getLevel() { return level; }
        public void setLevel(AlertLevel level) { this.level = level; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 告警级别
     */
    public enum AlertLevel {
        INFO,     // 信息
        WARN,     // 警告
        ERROR,    // 错误
        CRITICAL  // 严重
    }

    /**
     * 告警统计
     */
    public static class AlertStatistics {
        private long infoCount = 0;
        private long warnCount = 0;
        private long errorCount = 0;
        private long criticalCount = 0;

        // Getters and Setters
        public long getInfoCount() { return infoCount; }
        public void setInfoCount(long infoCount) { this.infoCount = infoCount; }
        public long getWarnCount() { return warnCount; }
        public void setWarnCount(long warnCount) { this.warnCount = warnCount; }
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
        public long getCriticalCount() { return criticalCount; }
        public void setCriticalCount(long criticalCount) { this.criticalCount = criticalCount; }

        public long getTotalCount() {
            return infoCount + warnCount + errorCount + criticalCount;
        }
    }

    /**
     * 性能指标
     */
    public static class PerformanceMetrics {
        private double tps;
        private double averageResponseTime;
        private double errorRate;
        private double cacheHitRate;

        // Getters and Setters
        public double getTps() { return tps; }
        public void setTps(double tps) { this.tps = tps; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
        public double getCacheHitRate() { return cacheHitRate; }
        public void setCacheHitRate(double cacheHitRate) { this.cacheHitRate = cacheHitRate; }
    }
}
