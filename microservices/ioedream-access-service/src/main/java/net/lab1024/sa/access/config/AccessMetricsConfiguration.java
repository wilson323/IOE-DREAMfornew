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
 * 闂ㄧ寰湇鍔℃€ц兘鐩戞帶閰嶇疆
 * <p>
 * 鎻愪緵瀹屾暣鐨凪icrometer鎸囨爣鐩戞帶閰嶇疆锛屽寘鎷細
 * - JVM鎬ц兘鎸囨爣鐩戞帶
 * - 涓氬姟鑷畾涔夋寚鏍囩洃鎺?
 * - 鎺ュ彛璋冪敤缁熻鐩戞帶
 * - 鏁版嵁搴撹繛鎺ユ睜鐩戞帶
 * - 缂撳瓨鎬ц兘鐩戞帶
 * - 钃濈墮璁惧杩炴帴鐩戞帶
 * - AI鍒嗘瀽鎬ц兘鐩戞帶
 * - 瑙嗛澶勭悊鎬ц兘鐩戞帶
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
     * 鑷畾涔変笟鍔℃寚鏍囨敞鍐屽櫒
     */
    @Bean
    @Primary
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // 娣诲姞涓氬姟鎸囨爣
        registerBusinessMetrics(registry);

        log.info("[鎬ц兘鐩戞帶] Prometheus鎸囨爣娉ㄥ唽鍣ㄥ垵濮嬪寲瀹屾垚");
        return registry;
    }

    /**
     * JVM鎸囨爣缁戝畾鍣?
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
     * 涓氬姟鑷畾涔夋寚鏍囬厤缃?
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
                    // 杩囨护鎺変笉闇€瑕佺殑鎸囨爣
                    return name.startsWith("jvm.buffer") ||
                           name.startsWith("system.cpu.count") ||
                           name.startsWith("process.files.max");
                }
            ));
    }

    /**
     * 娉ㄥ唽涓氬姟鑷畾涔夋寚鏍?
     */
    private void registerBusinessMetrics(PrometheusMeterRegistry registry) {
        // 1. 钃濈墮璁惧杩炴帴鎸囨爣
        registry.gauge("bluetooth.devices.connected", "connected bluetooth devices count");
        registry.gauge("bluetooth.devices.disconnected", "disconnected bluetooth devices count");
        registry.gauge("bluetooth.devices.health.score", "average bluetooth devices health score");

        // 2. 璁块棶鎺у埗鎸囨爣
        registry.counter("access.requests.total", "total access requests");
        registry.counter("access.requests.success", "successful access requests");
        registry.counter("access.requests.failed", "failed access requests");
        registry.gauge("access.response.time.avg", "average access response time");

        // 3. AI鍒嗘瀽鎸囨爣
        registry.counter("ai.analysis.requests.total", "total AI analysis requests");
        registry.counter("ai.analysis.behavior.anomalies", "behavior anomalies detected");
        registry.gauge("ai.model.accuracy", "AI model accuracy score");
        registry.gauge("ai.analysis.duration.avg", "average AI analysis duration");

        // 4. 瑙嗛鐩戞帶鎸囨爣
        registry.gauge("video.streams.active", "active video streams count");
        registry.counter("video.face.recognition.total", "total face recognition requests");
        registry.counter("video.abnormal.behavior.detected", "abnormal behavior detected");
        registry.gauge("video.recording.duration.total", "total video recording duration");

        // 5. 鐩戞帶鍛婅鎸囨爣
        registry.counter("alerts.generated.total", "total alerts generated");
        registry.counter("alerts.processed.total", "total alerts processed");
        registry.gauge("alerts.response.time.avg", "average alert response time");
        registry.gauge("alerts.self.healing.success.rate", "self healing success rate");

        // 6. 绂荤嚎妯″紡鎸囨爣
        registry.counter("offline.sync.requests.total", "total offline sync requests");
        registry.counter("offline.sync.records.success", "successfully synced records");
        registry.gauge("offline.data.size.total", "total offline data size");
        registry.gauge("offline.sync.duration.avg", "average offline sync duration");

        // 7. 鏁版嵁搴撴€ц兘鎸囨爣
        registry.gauge("database.connections.active", "active database connections");
        registry.gauge("database.connections.idle", "idle database connections");
        registry.gauge("database.query.duration.avg", "average database query duration");
        registry.gauge("database.cache.hit.rate", "database cache hit rate");

        // 8. 绯荤粺鍋ュ悍鎸囨爣
        registry.gauge("system.health.score", "overall system health score");
        registry.gauge("system.cpu.usage", "system CPU usage");
        registry.gauge("system.memory.usage", "system memory usage");
        registry.gauge("system.disk.usage", "system disk usage");

        log.info("[鎬ц兘鐩戞帶] 涓氬姟鎸囨爣娉ㄥ唽瀹屾垚 - 鍏辨敞鍐寋}涓寚鏍?, registry.getMeters().size());
    }

    /**
     * 鑷畾涔変笟鍔℃寚鏍囨敹闆嗗櫒
     */
    @Bean
    public AccessBusinessMetricsCollector businessMetricsCollector(MeterRegistry meterRegistry) {
        return new AccessBusinessMetricsCollector(meterRegistry);
    }

    /**
     * 涓氬姟鎸囨爣鏀堕泦鍣?
     */
    public static class AccessBusinessMetricsCollector {

        private final MeterRegistry meterRegistry;

        public AccessBusinessMetricsCollector(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        /**
         * 璁板綍钃濈墮璁惧杩炴帴浜嬩欢
         */
        public void recordBluetoothDeviceConnection(String deviceId, boolean success) {
            meterRegistry.counter("bluetooth.device.connection.attempts",
                "device_id", deviceId, "success", String.valueOf(success)).increment();

            if (success) {
                meterRegistry.gauge("bluetooth.devices.connected").increment();
            }
        }

        /**
         * 璁板綍璁块棶璇锋眰
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
         * 璁板綍AI鍒嗘瀽璇锋眰
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
         * 璁板綍瑙嗛鐩戞帶浜嬩欢
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
         * 璁板綍鍛婅澶勭悊浜嬩欢
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
         * 璁板綍绂荤嚎鏁版嵁鍚屾浜嬩欢
         */
        public void recordOfflineSync(String syncType, int recordCount, boolean success) {
            meterRegistry.counter("offline.sync.requests",
                "sync_type", syncType, "success", String.valueOf(success)).increment();

            if (success) {
                meterRegistry.counter("offline.sync.records", "sync_type", syncType).increment(recordCount);
            }
        }

        /**
         * 鏇存柊璁惧鍋ュ悍璇勫垎
         */
        public void updateDeviceHealthScore(String deviceType, double healthScore) {
            meterRegistry.gauge("device.health.score",
                "device_type", deviceType, healthScore);
        }

        /**
         * 鏇存柊绯荤粺璧勬簮浣跨敤鐜?
         */
        public void updateSystemResourceUsage(String resourceType, double usage) {
            meterRegistry.gauge("system.resource.usage",
                "resource_type", resourceType, usage);
        }

        /**
         * 璁板綍缂撳瓨鎬ц兘
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
         * 璁板綍鏁版嵁搴撴€ц兘
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
         * 璁板綍鑷畾涔変笟鍔℃寚鏍?
         */
        public void recordCustomMetric(String metricName, double value, String... tags) {
            if (tags.length % 2 != 0) {
                log.warn("[鎬ц兘鐩戞帶] 鑷畾涔夋寚鏍囨爣绛炬暟閲忎笉鍖归厤: {}", metricName);
                return;
            }

            meterRegistry.gauge(metricName, tags, value);
        }
    }

    /**
     * 搴旂敤鏈嶅姟灞炴€ч厤缃被
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