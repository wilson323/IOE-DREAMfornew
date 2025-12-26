package net.lab1024.sa.attendance.monitor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

/**
 * API性能监控组件
 * <p>
 * 监控API接口性能指标
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class ApiPerformanceMonitor {

    private final MeterRegistry meterRegistry;

    // 性能计数器
    private final Counter requestCounter;
    private final Counter errorCounter;
    private final Counter slowRequestCounter;

    // 性能计时器
    private final Timer responseTimer;

    // 慢请求阈值（毫秒）
    private static final long SLOW_REQUEST_THRESHOLD = 3000;

    // 请求统计（按URI）
    private final ConcurrentHashMap<String, ApiStatistics> apiStatisticsMap = new ConcurrentHashMap<>();

    public ApiPerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 初始化计数器
        this.requestCounter = Counter.builder("attendance.api.requests")
                .description("API请求总数")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("attendance.api.errors")
                .description("API错误总数")
                .register(meterRegistry);

        this.slowRequestCounter = Counter.builder("attendance.api.slow_requests")
                .description("慢请求总数（>3秒）")
                .register(meterRegistry);

        // 初始化计时器
        this.responseTimer = Timer.builder("attendance.api.response.time")
                .description("API响应时间")
                .register(meterRegistry);
    }

    /**
     * 记录API请求
     *
     * @param uri 请求URI
     * @param method 请求方法
     * @param responseStatus 响应状态码
     * @param duration 响应时长（毫秒）
     */
    public void recordRequest(String uri, String method, int responseStatus, long duration) {
        log.debug("[性能监控] 记录API请求: uri={}, method={}, status={}, duration={}ms",
                uri, method, responseStatus, duration);

        // 记录请求总数
        requestCounter.increment();

        // 记录响应时间
        responseTimer.record(Duration.ofMillis(duration));

        // 记录错误
        if (responseStatus >= 400) {
            errorCounter.increment();
        }

        // 记录慢请求
        if (duration > SLOW_REQUEST_THRESHOLD) {
            slowRequestCounter.increment();
            log.warn("[性能监控] 慢请求告警: uri={}, duration={}ms", uri, duration);
        }

        // 更新API统计
        updateApiStatistics(uri, method, responseStatus, duration);
    }

    /**
     * 更新API统计
     */
    private void updateApiStatistics(String uri, String method, int responseStatus, long duration) {
        String key = method + ":" + uri;

        ApiStatistics statistics = apiStatisticsMap.computeIfAbsent(key, k -> new ApiStatistics());
        statistics.recordRequest(responseStatus, duration);
    }

    /**
     * 获取API统计
     *
     * @param uri 请求URI
     * @param method 请求方法
     * @return API统计数据
     */
    public ApiStatistics getApiStatistics(String uri, String method) {
        String key = method + ":" + uri;
        return apiStatisticsMap.get(key);
    }

    /**
     * 获取所有API统计
     *
     * @return 所有API统计数据
     */
    public ConcurrentHashMap<String, ApiStatistics> getAllApiStatistics() {
        return apiStatisticsMap;
    }

    /**
     * API统计数据
     */
    public static class ApiStatistics {
        private long totalRequests = 0;
        private long successRequests = 0;
        private long errorRequests = 0;
        private long slowRequests = 0;
        private long totalDuration = 0;
        private long maxDuration = 0;
        private long minDuration = Long.MAX_VALUE;
        private LocalDateTime lastRequestTime;

        public synchronized void recordRequest(int responseStatus, long duration) {
            totalRequests++;
            totalDuration += duration;

            if (responseStatus < 400) {
                successRequests++;
            } else {
                errorRequests++;
            }

            if (duration > 3000) {
                slowRequests++;
            }

            if (duration > maxDuration) {
                maxDuration = duration;
            }

            if (duration < minDuration) {
                minDuration = duration;
            }

            lastRequestTime = LocalDateTime.now();
        }

        public double getAverageDuration() {
            return totalRequests > 0 ? (double) totalDuration / totalRequests : 0;
        }

        public double getSuccessRate() {
            return totalRequests > 0 ? (double) successRequests / totalRequests * 100 : 0;
        }

        // Getters
        public long getTotalRequests() { return totalRequests; }
        public long getSuccessRequests() { return successRequests; }
        public long getErrorRequests() { return errorRequests; }
        public long getSlowRequests() { return slowRequests; }
        public long getMaxDuration() { return maxDuration; }
        public long getMinDuration() { return minDuration == Long.MAX_VALUE ? 0 : minDuration; }
        public LocalDateTime getLastRequestTime() { return lastRequestTime; }
    }
}
