package net.lab1024.sa.common.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 监控指标收集器
 * <p>
 * 统一收集应用程序的监控指标
 * 支持业务指标、性能指标、错误指标收集
 * 提供指标注册、统计和查询功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class MetricsCollector {

    private final MeterRegistry meterRegistry;

    // 指标缓存
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Gauge> gauges = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> countersWithReset = new ConcurrentHashMap<>();

    // 构造函数注入依赖
    public MetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeBuiltinMetrics();
    }

    /**
     * 初始化内置指标
     */
    private void initializeBuiltinMetrics() {
        // 应用级别指标
        registerCounter("application.startups", "应用启动次数");
        registerGauge("application.uptime", "应用运行时间", () -> getApplicationUptime());
        registerCounter("http.requests.total", "HTTP请求总数");
        registerCounter("http.requests.success", "HTTP成功请求数");
        registerCounter("http.requests.error", "HTTP错误请求数");

        // 业务指标
        registerCounter("business.user.login.total", "用户登录总数");
        registerCounter("business.device.access.total", "门禁访问总数");
        registerCounter("business.attendance.check.total", "考勤打卡总数");
        registerCounter("business.consume.payment.total", "消费支付总数");
        registerCounter("business.visitor.register.total", "访客注册总数");

        // 性能指标
        registerTimer("http.request.duration", "HTTP请求处理时间");
        registerTimer("database.query.duration", "数据库查询时间");
        registerTimer("cache.get.duration", "缓存获取时间");

        // 错误指标
        registerCounter("error.system.total", "系统错误总数");
        registerCounter("error.business.total", "业务错误总数");
        registerCounter("error.database.total", "数据库错误总数");
        registerCounter("error.cache.total", "缓存错误总数");
    }

    /**
     * 注册计数器
     *
     * @param name 指标名称
     * @param description 指标描述
     * @return 计数器实例
     */
    public Counter registerCounter(String name, String description) {
        Counter counter = Counter.builder(name)
                .description(description)
                .register(meterRegistry);
        counters.put(name, counter);
        return counter;
    }

    /**
     * 注册仪表盘
     *
     * @param name 指标名称
     * @param description 指标描述
     * @param valueSupplier 值提供者
     * @return 仪表盘实例
     */
    public Gauge registerGauge(String name, String description, Supplier<Number> valueSupplier) {
        Gauge gauge = Gauge.builder(name, valueSupplier)
                .description(description)
                .register(meterRegistry);
        gauges.put(name, gauge);
        return gauge;
    }

    /**
     * 注册计时器
     *
     * @param name 指标名称
     * @param description 指标描述
     * @return 计时器实例
     */
    public Timer registerTimer(String name, String description) {
        Timer timer = Timer.builder(name)
                .description(description)
                .register(meterRegistry);
        timers.put(name, timer);
        return timer;
    }

    /**
     * 记录计数器
     *
     * @param name 指标名称
     * @param delta 增量值
     */
    public void incrementCounter(String name, double delta) {
        Counter counter = counters.get(name);
        if (counter != null) {
            counter.increment(delta);
        }

        // 记录自定义计数器
        AtomicLong customCounter = countersWithReset.get(name);
        if (customCounter != null) {
            customCounter.addAndGet((long) delta);
        }
    }

    /**
     * 记录计数器
     *
     * @param name 指标名称
     */
    public void incrementCounter(String name) {
        incrementCounter(name, 1.0);
    }

    /**
     * 记录计时器执行时间
     *
     * @param name 指标名称
     * @param duration 执行时间
     */
    public void recordTimer(String name, Duration duration) {
        Timer timer = timers.get(name);
        if (timer != null) {
            timer.record(duration);
        }
    }

    /**
     * 记录计时器执行时间（毫秒）
     *
     * @param name 指标名称
     * @param durationMs 执行时间（毫秒）
     */
    public void recordTimer(String name, long durationMs) {
        recordTimer(name, Duration.ofMillis(durationMs));
    }

    /**
     * 使用计时器执行操作
     *
     * @param name 指标名称
     * @param operation 操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T recordTimer(String name, Supplier<T> operation) {
        Timer timer = timers.get(name);
        if (timer != null) {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                T result = operation.get();
                sample.stop(timer);
                return result;
            } catch (Exception e) {
                sample.stop(timer);
                throw e;
            }
        } else {
            return operation.get();
        }
    }

    /**
     * 注册自定义计数器（可重置）
     *
     * @param name 指标名称
     * @param description 指标描述
     */
    public void registerResettableCounter(String name, String description) {
        countersWithReset.put(name, new AtomicLong(0));
        log.info("[监控指标] 注册可重置计数器, name={}, description={}", name, description);
    }

    /**
     * 重置自定义计数器
     *
     * @param name 指标名称
     * @return 重置前的值
     */
    public long resetCounter(String name) {
        AtomicLong counter = countersWithReset.get(name);
        if (counter != null) {
            return counter.getAndSet(0);
        }
        return 0;
    }

    /**
     * 获取计数器值
     *
     * @param name 指标名称
     * @return 计数器值
     */
    public double getCounterValue(String name) {
        Counter counter = counters.get(name);
        if (counter != null) {
            return counter.count();
        }

        AtomicLong customCounter = countersWithReset.get(name);
        if (customCounter != null) {
            return customCounter.get();
        }

        return 0.0;
    }

    /**
     * 获取仪表盘值
     *
     * @param name 指标名称
     * @return 仪表盘值
     */
    public double getGaugeValue(String name) {
        Gauge gauge = gauges.get(name);
        if (gauge != null) {
            return gauge.value();
        }
        return 0.0;
    }

    /**
     * 记录业务指标
     *
     * @param category 业务分类
     * @param action 业务操作
     * @param count 数量
     * @param attributes 属性
     */
    public void recordBusinessMetric(String category, String action, double count, Map<String, Object> attributes) {
        String metricName = "business." + category + "." + action;

        // 注册业务指标（如果不存在）
        if (!counters.containsKey(metricName)) {
            registerCounter(metricName, String.format("业务指标 - %s.%s", category, action));
        }

        // 记录计数
        incrementCounter(metricName, count);

        // 记录属性（可选）
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                String attrName = metricName + "." + entry.getKey();
                if (entry.getValue() instanceof Number) {
                    registerGauge(attrName, String.format("业务属性 - %s", attrName),
                            () -> ((Number) entry.getValue()).doubleValue());
                }
            }
        }

        log.debug("[监控指标] 记录业务指标, category={}, action={}, count={}, attributes={}",
                category, action, count, attributes);
    }

    /**
     * 记录HTTP请求指标
     *
     * @param method HTTP方法
     * @param uri 请求URI
     * @param statusCode 状态码
     * @param duration 响应时间
     */
    public void recordHttpRequest(String method, String uri, int statusCode, Duration duration) {
        // 记录总请求数
        incrementCounter("http.requests.total");

        // 记录成功/失败请求数
        if (statusCode >= 200 && statusCode < 300) {
            incrementCounter("http.requests.success");
        } else {
            incrementCounter("http.requests.error");
        }

        // 记录响应时间
        recordTimer("http.request.duration", duration);

        // 记录状态码
        String statusMetricName = "http.status." + statusCode;
        if (!counters.containsKey(statusMetricName)) {
            registerCounter(statusMetricName, "HTTP状态码统计");
        }
        incrementCounter(statusMetricName);

        // 记录端点指标
        String endpointMetricName = "http.endpoint." + method + "." + uri;
        if (!counters.containsKey(endpointMetricName)) {
            registerCounter(endpointMetricName, "HTTP端点统计");
        }
        incrementCounter(endpointMetricName);

        log.debug("[监控指标] 记录HTTP请求, method={}, uri={}, statusCode={}, duration={}ms",
                method, uri, statusCode, duration.toMillis());
    }

    /**
     * 记录数据库操作指标
     *
     * @param operation 操作类型
     * @param tableName 表名
     * @param duration 执行时间
     * @param success 是否成功
     */
    public void recordDatabaseOperation(String operation, String tableName, Duration duration, boolean success) {
        // 记录数据库操作次数
        String operationMetricName = "database.operation." + operation;
        if (!counters.containsKey(operationMetricName)) {
            registerCounter(operationMetricName, String.format("数据库操作 - %s", operation));
        }
        incrementCounter(operationMetricName);

        // 记录表名操作
        String tableMetricName = "database.table." + tableName;
        if (!counters.containsKey(tableMetricName)) {
            registerCounter(tableMetricName, String.format("数据库表操作 - %s", tableName));
        }
        incrementCounter(tableMetricName);

        // 记录成功/失败操作
        String resultMetricName = success ? "database.success" : "database.error";
        if (!counters.containsKey(resultMetricName)) {
            registerCounter(resultMetricName, "数据库操作结果");
        }
        incrementCounter(resultMetricName);

        // 记录执行时间
        recordTimer("database.query.duration", duration);

        log.debug("[监控指标] 记录数据库操作, operation={}, table={}, duration={}ms, success={}",
                operation, tableName, duration.toMillis(), success);
    }

    /**
     * 记录缓存操作指标
     *
     * @param operation 操作类型
     * @param cacheType 缓存类型
     * @param hit 是否命中
     * @param duration 执行时间
     */
    public void recordCacheOperation(String operation, String cacheType, boolean hit, Duration duration) {
        // 记录缓存操作次数
        String cacheMetricName = "cache.operation." + cacheType + "." + operation;
        if (!counters.containsKey(cacheMetricName)) {
            registerCounter(cacheMetricName, String.format("缓存操作 - %s.%s", cacheType, operation));
        }
        incrementCounter(cacheMetricName);

        // 记录命中/未命中
        String resultMetricName = hit ? "cache.hit" : "cache.miss";
        if (!counters.containsKey(resultMetricName)) {
            registerCounter(resultMetricName, "缓存命中结果");
        }
        incrementCounter(resultMetricName);

        // 记录执行时间
        recordTimer("cache.get.duration", duration);

        log.debug("[监控指标] 记录缓存操作, operation={}, type={}, hit={}, duration={}ms",
                operation, cacheType, hit, duration.toMillis());
    }

    /**
     * 记录系统错误
     *
     * @param errorType 错误类型
     * @param errorMessage 错误信息
     * @param cause 异常原因
     */
    public void recordSystemError(String errorType, String errorMessage, Throwable cause) {
        // 记录系统错误总数
        incrementCounter("error.system.total");

        // 记录具体错误类型
        String errorMetricName = "error.system." + errorType;
        if (!counters.containsKey(errorMetricName)) {
            registerCounter(errorMetricName, String.format("系统错误 - %s", errorType));
        }
        incrementCounter(errorMetricName);

        log.error("[监控指标] 记录系统错误, type={}, message={}", errorType, errorMessage, cause);
    }

    /**
     * 记录业务错误
     *
     * @param errorType 错误类型
     * @param errorMessage 错误信息
     * @param module 模块名称
     */
    public void recordBusinessError(String errorType, String errorMessage, String module) {
        // 记录业务错误总数
        incrementCounter("error.business.total");

        // 记录模块错误
        String moduleMetricName = "error.business." + module;
        if (!counters.containsKey(moduleMetricName)) {
            registerCounter(moduleMetricName, String.format("业务错误 - %s", module));
        }
        incrementCounter(moduleMetricName);

        // 记录具体错误类型
        String errorMetricName = "error.business." + errorType;
        if (!counters.containsKey(errorMetricName)) {
            registerCounter(errorMetricName, String.format("业务错误 - %s", errorType));
        }
        incrementCounter(errorMetricName);

        log.error("[监控指标] 记录业务错误, type={}, module={}, message={}", errorType, module, errorMessage);
    }

    /**
     * 应用启动时间计算
     */
    private static long applicationStartTime = System.currentTimeMillis();

    private double getApplicationUptime() {
        return (System.currentTimeMillis() - applicationStartTime) / 1000.0;
    }

    /**
     * 获取所有指标概览
     *
     * @return 指标概览
     */
    public MetricOverview getMetricOverview() {
        MetricOverview overview = new MetricOverview();

        // 基础统计
        overview.setTotalRequests((long) getCounterValue("http.requests.total"));
        overview.setSuccessRequests((long) getCounterValue("http.requests.success"));
        overview.setErrorRequests((long) getCounterValue("http.requests.error"));
        overview.setBusinessOperations(counters.size() + countersWithReset.size());

        // 性能统计
        overview.setAverageResponseTime(getAverageResponseTime());
        overview.setCacheHitRate(getCacheHitRate());

        // 系统健康状态
        overview.setErrorCount((long) getCounterValue("error.system.total"));
        overview.setBusinessErrorCount((long) getCounterValue("error.business.total"));
        overview.setUptime(getApplicationUptime());

        // 时间戳
        overview.setLastUpdateTime(LocalDateTime.now());

        return overview;
    }

    /**
     * 获取平均响应时间
     */
    private double getAverageResponseTime() {
        Timer timer = timers.get("http.request.duration");
        if (timer != null) {
            return timer.mean(TimeUnit.MILLISECONDS);
        }
        return 0.0;
    }

    /**
     * 获取缓存命中率
     */
    private double getCacheHitRate() {
        long hits = (long) getCounterValue("cache.hit");
        long misses = (long) getCounterValue("cache.miss");
        long total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }

    /**
     * 指标概览数据结构
     */
    public static class MetricOverview {
        private long totalRequests;
        private long successRequests;
        private long errorRequests;
        private int businessOperations;
        private double averageResponseTime;
        private double cacheHitRate;
        private long errorCount;
        private long businessErrorCount;
        private double uptime;
        private LocalDateTime lastUpdateTime;

        // getters and setters
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        public long getSuccessRequests() { return successRequests; }
        public void setSuccessRequests(long successRequests) { this.successRequests = successRequests; }
        public long getErrorRequests() { return errorRequests; }
        public void setErrorRequests(long errorRequests) { this.errorRequests = errorRequests; }
        public int getBusinessOperations() { return businessOperations; }
        public void setBusinessOperations(int businessOperations) { this.businessOperations = businessOperations; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        public double getCacheHitRate() { return cacheHitRate; }
        public void setCacheHitRate(double cacheHitRate) { this.cacheHitRate = cacheHitRate; }
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
        public long getBusinessErrorCount() { return businessErrorCount; }
        public void setBusinessErrorCount(long businessErrorCount) { this.businessErrorCount = businessErrorCount; }
        public double getUptime() { return uptime; }
        public void setUptime(double uptime) { this.uptime = uptime; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    }
}