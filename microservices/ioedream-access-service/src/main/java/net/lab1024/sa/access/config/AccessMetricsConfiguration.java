package net.lab1024.sa.access.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * 门禁微服务性能监控配置
 * <p>
 * 提供完整的Micrometer指标监控配置，包括：
 * - JVM性能指标监控
 * - 业务自定义指标监控
 * - 接口调用统计监控
 * - 数据库连接池监控
 * - 缓存性能监控
 * - 蓝牙设备连接监控
 * - AI分析性能监控
 * - 视频处理性能监控
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
@Slf4j
public class AccessMetricsConfiguration {

    @Resource
    private AccessServiceProperties accessServiceProperties;

    /**
     * 自定义业务指标注册器
     */
    @Bean
    @Primary
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // 添加业务指标
        registerBusinessMetrics(registry);

        log.info("[性能监控] Prometheus指标注册器初始化完成");
        return registry;
    }

    /**
     * JVM指标绑定器
     */
    @Bean
    public List<MeterBinder> jvmMeterBinders() {
        return Arrays.asList(
            new JvmMemoryMetrics(),
            new JvmGcMetrics(),
            new JvmThreadMetrics(),
            new ProcessorMetrics()
        );
    }

    /**
     * 业务自定义指标配置
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags(
                "application", "ioedream-access-service",
                "version", "1.0.0",
                "environment", accessServiceProperties.getEnvironment(),
                "region", accessServiceProperties.getRegion(),
                "zone", accessServiceProperties.getZone()
            )
            .meterFilter(MeterFilter.deny(
                id -> {
                    String name = id.getName();
                    // 过滤掉不需要的指标
                    return name.startsWith("jvm.buffer") ||
                           name.startsWith("system.cpu.count") ||
                           name.startsWith("process.files.max");
                }
            ));
    }

    /**
     * 注册业务自定义指标
     */
    private void registerBusinessMetrics(PrometheusMeterRegistry registry) {
        // 1. 蓝牙设备连接指标
        registry.gauge("bluetooth.devices.connected", "connected bluetooth devices count");
        registry.gauge("bluetooth.devices.disconnected", "disconnected bluetooth devices count");
        registry.gauge("bluetooth.devices.health.score", "average bluetooth devices health score");

        // 2. 访问控制指标
        registry.counter("access.requests.total", "total access requests");
        registry.counter("access.requests.success", "successful access requests");
        registry.counter("access.requests.failed", "failed access requests");
        registry.gauge("access.response.time.avg", "average access response time");

        // 3. AI分析指标
        registry.counter("ai.analysis.requests.total", "total AI analysis requests");
        registry.counter("ai.analysis.behavior.anomalies", "behavior anomalies detected");
        registry.gauge("ai.model.accuracy", "AI model accuracy score");
        registry.gauge("ai.analysis.duration.avg", "average AI analysis duration");

        // 4. 视频监控指标
        registry.gauge("video.streams.active", "active video streams count");
        registry.counter("video.face.recognition.total", "total face recognition requests");
        registry.counter("video.abnormal.behavior.detected", "abnormal behavior detected");
        registry.gauge("video.recording.duration.total", "total video recording duration");

        // 5. 监控告警指标
        registry.counter("alerts.generated.total", "total alerts generated");
        registry.counter("alerts.processed.total", "total alerts processed");
        registry.gauge("alerts.response.time.avg", "average alert response time");
        registry.gauge("alerts.self.healing.success.rate", "self healing success rate");

        // 6. 离线模式指标
        registry.counter("offline.sync.requests.total", "total offline sync requests");
        registry.counter("offline.sync.records.success", "successfully synced records");
        registry.gauge("offline.data.size.total", "total offline data size");
        registry.gauge("offline.sync.duration.avg", "average offline sync duration");

        // 7. 数据库性能指标
        registry.gauge("database.connections.active", "active database connections");
        registry.gauge("database.connections.idle", "idle database connections");
        registry.gauge("database.query.duration.avg", "average database query duration");
        registry.gauge("database.cache.hit.rate", "database cache hit rate");

        // 8. 系统健康指标
        registry.gauge("system.health.score", "overall system health score");
        registry.gauge("system.cpu.usage", "system CPU usage");
        registry.gauge("system.memory.usage", "system memory usage");
        registry.gauge("system.disk.usage", "system disk usage");

        log.info("[性能监控] 业务指标注册完成 - 共注册{}个指标", registry.getMeters().size());
    }

    /**
     * 自定义业务指标收集器
     */
    @Bean
    public AccessBusinessMetricsCollector businessMetricsCollector(MeterRegistry meterRegistry) {
        return new AccessBusinessMetricsCollector(meterRegistry);
    }

    /**
     * 业务指标收集器
     */
    public static class AccessBusinessMetricsCollector {

        private final MeterRegistry meterRegistry;

        public AccessBusinessMetricsCollector(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        /**
         * 记录蓝牙设备连接事件
         */
        public void recordBluetoothDeviceConnection(String deviceId, boolean success) {
            meterRegistry.counter("bluetooth.device.connection.attempts",
                "device_id", deviceId, "success", String.valueOf(success)).increment();

            if (success) {
                meterRegistry.gauge("bluetooth.devices.connected").increment();
            }
        }

        /**
         * 记录访问请求
         */
        public void recordAccessRequest(String userId, String deviceId, boolean success, long duration) {
            String tags = "user_id=" + userId + ",device_id=" + deviceId + ",success=" + success;

            meterRegistry.counter("access.requests.total", "success", String.valueOf(success)).increment();

            Timer.Sample sample = Timer.start(meterRegistry);
            sample.stop(Timer.builder("access.request.duration")
                .description("Access request duration")
                .tag("success", String.valueOf(success))
                .register(meterRegistry));
        }

        /**
         * 记录AI分析请求
         */
        public void recordAIAnalysis(String analysisType, long duration, int anomalyCount) {
            meterRegistry.counter("ai.analysis.requests", "type", analysisType).increment();

            Timer.builder("ai.analysis.duration")
                .description("AI analysis duration")
                .tag("type", analysisType)
                .register(meterRegistry)
                .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);

            if (anomalyCount > 0) {
                meterRegistry.counter("ai.analysis.anomalies", "type", analysisType).increment(anomalyCount);
            }
        }

        /**
         * 记录视频监控事件
         */
        public void recordVideoMonitoring(String eventType, String cameraId, long duration) {
            meterRegistry.counter("video.monitoring.events",
                "event_type", eventType, "camera_id", cameraId).increment();

            if (duration > 0) {
                Timer.builder("video.monitoring.duration")
                    .description("Video monitoring duration")
                    .tag("event_type", eventType)
                    .register(meterRegistry)
                    .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        }

        /**
         * 记录告警处理事件
         */
        public void recordAlertProcessing(String alertType, String processResult, long duration) {
            meterRegistry.counter("alerts.processed",
                "alert_type", alertType, "result", processResult).increment();

            Timer.builder("alerts.processing.duration")
                .description("Alert processing duration")
                .tag("alert_type", alertType)
                .tag("result", processResult)
                .register(meterRegistry)
                .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        /**
         * 记录离线数据同步事件
         */
        public void recordOfflineSync(String syncType, int recordCount, boolean success) {
            meterRegistry.counter("offline.sync.requests",
                "sync_type", syncType, "success", String.valueOf(success)).increment();

            if (success) {
                meterRegistry.counter("offline.sync.records", "sync_type", syncType).increment(recordCount);
            }
        }

        /**
         * 更新设备健康评分
         */
        public void updateDeviceHealthScore(String deviceType, double healthScore) {
            meterRegistry.gauge("device.health.score",
                "device_type", deviceType, healthScore);
        }

        /**
         * 更新系统资源使用率
         */
        public void updateSystemResourceUsage(String resourceType, double usage) {
            meterRegistry.gauge("system.resource.usage",
                "resource_type", resourceType, usage);
        }

        /**
         * 记录缓存性能
         */
        public void recordCachePerformance(String cacheName, long hits, long misses) {
            meterRegistry.counter("cache.requests",
                "cache_name", cacheName, "result", "hit").increment(hits);
            meterRegistry.counter("cache.requests",
                "cache_name", cacheName, "result", "miss").increment(misses);

            double hitRate = (double) hits / (hits + misses);
            meterRegistry.gauge("cache.hit.rate",
                "cache_name", cacheName, hitRate);
        }

        /**
         * 记录数据库性能
         */
        public void recordDatabasePerformance(String operation, long duration, boolean success) {
            meterRegistry.counter("database.operations",
                "operation", operation, "success", String.valueOf(success)).increment();

            Timer.builder("database.operation.duration")
                .description("Database operation duration")
                .tag("operation", operation)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        /**
         * 记录自定义业务指标
         */
        public void recordCustomMetric(String metricName, double value, String... tags) {
            if (tags.length % 2 != 0) {
                log.warn("[性能监控] 自定义指标标签数量不匹配: {}", metricName);
                return;
            }

            meterRegistry.gauge(metricName, tags, value);
        }
    }

    /**
     * 应用服务属性配置类
     */
    public static class AccessServiceProperties {
        private String environment = "dev";
        private String region = "default";
        private String zone = "default";

        // Getters and Setters
        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getZone() {
            return zone;
        }

        public void setZone(String zone) {
            this.zone = zone;
        }
    }
}