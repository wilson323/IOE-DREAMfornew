package net.lab1024.sa.common.monitor.manager;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.domain.vo.ServiceMetricsVO;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 指标采集Manager
 * 整合自ioedream-monitor-service
 *
 * 符合CLAUDE.md规范 - Manager层
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 *
 * 职责：
 * - 服务性能指标采集（QPS、TPS、响应时间、错误率）
 * - 业务指标采集
 * - 指标数据聚合
 * - 指标数据存储（Redis）
 *
 * 企业级特性：
 * - 集成Micrometer进行指标采集
 * - 支持Prometheus格式导出
 * - Redis存储指标数据（可选时序数据库）
 * - 实时指标查询和统计
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自monitor-service）
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
@SuppressWarnings("null")
public class MetricsCollectorManager {

    private final MeterRegistry meterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param meterRegistry 指标注册表
     * @param redisTemplate Redis模板
     */
    public MetricsCollectorManager(MeterRegistry meterRegistry, RedisTemplate<String, Object> redisTemplate) {
        this.meterRegistry = meterRegistry;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 指标存储前缀
     */
    private static final String METRIC_PREFIX = "metrics:";
    private static final String METRIC_SERVICE_PREFIX = "metrics:service:";
    // 注意：METRIC_BUSINESS_PREFIX 已移除，当前使用 METRIC_PREFIX 统一管理所有指标
    // 如需区分业务指标，可在 recordMetric 方法中添加指标类型参数

    /**
     * 检查RedisTemplate是否可用
     *
     * @return Redis是否可用
     */
    private boolean isRedisAvailable() {
        try {
            return redisTemplate != null && redisTemplate.getConnectionFactory() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 指标过期时间（7天）
     */
    private static final long METRIC_EXPIRE_SECONDS = 7 * 24 * 60 * 60;

    /**
     * 采集服务指标
     * <p>
     * 企业级实现：
     * - 从MeterRegistry查询HTTP请求指标
     * - 计算QPS（每秒请求数）
     * - 计算TPS（每秒事务数）
     * - 计算平均响应时间
     * - 计算错误率
     * - 获取活跃连接数
     * </p>
     *
     * @param serviceName 服务名称
     * @return 服务指标VO
     */
    public ServiceMetricsVO collectServiceMetrics(String serviceName) {
        log.debug("开始采集服务指标，服务名称：{}", serviceName);

        ServiceMetricsVO metrics = new ServiceMetricsVO();
        metrics.setServiceName(serviceName);

        try {
            // 1. 计算QPS（每秒请求数）
            Long qps = calculateQPS(serviceName);
            metrics.setQps(qps);

            // 2. 计算TPS（每秒事务数，这里使用QPS作为近似值）
            metrics.setTps(qps);

            // 3. 计算平均响应时间（毫秒）
            Double avgResponseTime = calculateAvgResponseTime(serviceName);
            metrics.setAvgResponseTime(avgResponseTime);

            // 4. 计算错误率
            Double errorRate = calculateErrorRate(serviceName);
            metrics.setErrorRate(errorRate);

            // 5. 获取活跃连接数
            Integer activeConnections = getActiveConnections(serviceName);
            metrics.setActiveConnections(activeConnections);

            // 6. 存储指标到Redis（可选）
            if (isRedisAvailable()) {
                storeServiceMetrics(serviceName, metrics);
            }

            log.debug("服务指标采集完成，服务：{}，QPS：{}，平均响应时间：{}ms，错误率：{}",
                    serviceName, qps, avgResponseTime, errorRate);

        } catch (Exception e) {
            log.error("采集服务指标失败，服务名称：{}", serviceName, e);
            // 异常情况下返回默认值
            metrics.setQps(0L);
            metrics.setTps(0L);
            metrics.setAvgResponseTime(0.0);
            metrics.setErrorRate(0.0);
            metrics.setActiveConnections(0);
        }

        return metrics;
    }

    /**
     * 计算QPS（每秒请求数）
     *
     * @param serviceName 服务名称
     * @return QPS值
     */
    private Long calculateQPS(String serviceName) {
        try {
            // 查询HTTP请求计数器
            Counter counter = meterRegistry.find("http.server.requests")
                    .tag("service", serviceName)
                    .counter();

            if (counter != null && counter.count() > 0) {
                // 获取最近1分钟的请求数，计算QPS
                // 注意：这里简化处理，实际应该查询时间窗口内的请求数
                double count = counter.count();
                // 假设统计周期为60秒，计算QPS
                return Math.round(count / 60.0);
            }

            // 如果没有找到计数器，尝试查询通用HTTP请求指标
            Counter httpCounter = meterRegistry.find("http.server.requests").counter();
            if (httpCounter != null && httpCounter.count() > 0) {
                double count = httpCounter.count();
                return Math.round(count / 60.0);
            }

        } catch (Exception e) {
            log.warn("计算QPS失败，服务：{}", serviceName, e);
        }

        return 0L;
    }

    /**
     * 计算平均响应时间
     *
     * @param serviceName 服务名称
     * @return 平均响应时间（毫秒）
     */
    private Double calculateAvgResponseTime(String serviceName) {
        try {
            // 查询HTTP请求计时器
            Timer timer = meterRegistry.find("http.server.requests")
                    .tag("service", serviceName)
                    .timer();

            if (timer != null && timer.count() > 0) {
                // 获取平均响应时间（秒转毫秒）
                return timer.mean(TimeUnit.MILLISECONDS);
            }

            // 如果没有找到计时器，尝试查询通用HTTP请求指标
            Timer httpTimer = meterRegistry.find("http.server.requests").timer();
            if (httpTimer != null && httpTimer.count() > 0) {
                return httpTimer.mean(TimeUnit.MILLISECONDS);
            }

        } catch (Exception e) {
            log.warn("计算平均响应时间失败，服务：{}", serviceName, e);
        }

        return 0.0;
    }

    /**
     * 计算错误率
     *
     * @param serviceName 服务名称
     * @return 错误率（0-1之间的小数）
     */
    private Double calculateErrorRate(String serviceName) {
        try {
            // 查询总请求数
            Counter totalCounter = meterRegistry.find("http.server.requests")
                    .tag("service", serviceName)
                    .counter();

            if (totalCounter == null || totalCounter.count() == 0) {
                return 0.0;
            }

            double totalCount = totalCounter.count();
            double[] errorCount = {0.0}; // 使用数组来在lambda中修改值

            // 查询4xx错误请求数
            Counter error4xxCounter = meterRegistry.find("http.server.requests")
                    .tag("service", serviceName)
                    .tag("status", "4xx")
                    .counter();
            if (error4xxCounter != null) {
                errorCount[0] += error4xxCounter.count();
            }

            // 查询5xx错误请求数
            Counter error5xxCounter = meterRegistry.find("http.server.requests")
                    .tag("service", serviceName)
                    .tag("status", "5xx")
                    .counter();
            if (error5xxCounter != null) {
                errorCount[0] += error5xxCounter.count();
            }

            // 如果没有找到错误计数器，尝试查询所有状态码>=400的请求
            if (errorCount[0] == 0) {
                // 遍历所有HTTP请求指标，查找错误状态码
                meterRegistry.find("http.server.requests")
                        .tag("service", serviceName)
                        .counters()
                        .forEach(counter -> {
                            String status = counter.getId().getTag("status");
                            if (status != null && (status.startsWith("4") || status.startsWith("5"))) {
                                errorCount[0] += counter.count();
                            }
                        });
            }

            return errorCount[0] / totalCount;

        } catch (Exception e) {
            log.warn("计算错误率失败，服务：{}", serviceName, e);
        }

        return 0.0;
    }

    /**
     * 获取活跃连接数
     *
     * @param serviceName 服务名称
     * @return 活跃连接数
     */
    private Integer getActiveConnections(String serviceName) {
        try {
            // 查询活跃连接数指标
            Gauge gauge = meterRegistry.find("http.server.connections.active")
                    .tag("service", serviceName)
                    .gauge();

            if (gauge != null) {
                return (int) gauge.value();
            }

            // 如果没有找到，尝试查询通用连接数指标
            Gauge connectionsGauge = meterRegistry.find("http.server.connections.active").gauge();
            if (connectionsGauge != null) {
                return (int) connectionsGauge.value();
            }

        } catch (Exception e) {
            log.warn("获取活跃连接数失败，服务：{}", serviceName, e);
        }

        return 0;
    }

    /**
     * 存储服务指标到Redis
     *
     * @param serviceName 服务名称
     * @param metrics     指标数据
     */
    private void storeServiceMetrics(String serviceName, ServiceMetricsVO metrics) {
        try {
            String key = METRIC_SERVICE_PREFIX + serviceName + ":" + System.currentTimeMillis();
            Map<String, Object> data = new HashMap<>();
            data.put("serviceName", metrics.getServiceName());
            data.put("qps", metrics.getQps());
            data.put("tps", metrics.getTps());
            data.put("avgResponseTime", metrics.getAvgResponseTime());
            data.put("errorRate", metrics.getErrorRate());
            data.put("activeConnections", metrics.getActiveConnections());
            data.put("timestamp", LocalDateTime.now().toString());

            redisTemplate.opsForValue().set(key, data, METRIC_EXPIRE_SECONDS, TimeUnit.SECONDS);
            log.debug("服务指标已存储到Redis，服务：{}", serviceName);

        } catch (Exception e) {
            log.warn("存储服务指标到Redis失败，服务：{}", serviceName, e);
        }
    }

    /**
     * 采集业务指标
     * <p>
     * 企业级实现：
     * - 从MeterRegistry查询业务指标
     * - 支持自定义业务指标名称
     * - 返回指标值和时间戳
     * </p>
     *
     * @param metricName 指标名称
     * @return 业务指标数据
     */
    public Map<String, Object> collectBusinessMetrics(String metricName) {
        log.debug("开始采集业务指标，指标名称：{}", metricName);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("metricName", metricName);
        metrics.put("timestamp", System.currentTimeMillis());

        try {
            // 1. 尝试查询Gauge类型指标
            Gauge gauge = meterRegistry.find(metricName).gauge();
            if (gauge != null) {
                metrics.put("value", gauge.value());
                metrics.put("type", "gauge");
                log.debug("采集业务指标完成（Gauge），指标：{}，值：{}", metricName, gauge.value());
                return metrics;
            }

            // 2. 尝试查询Counter类型指标
            Counter counter = meterRegistry.find(metricName).counter();
            if (counter != null) {
                metrics.put("value", counter.count());
                metrics.put("type", "counter");
                log.debug("采集业务指标完成（Counter），指标：{}，值：{}", metricName, counter.count());
                return metrics;
            }

            // 3. 尝试查询Timer类型指标
            Timer timer = meterRegistry.find(metricName).timer();
            if (timer != null) {
                metrics.put("value", timer.mean(TimeUnit.MILLISECONDS));
                metrics.put("count", timer.count());
                metrics.put("type", "timer");
                log.debug("采集业务指标完成（Timer），指标：{}，平均值：{}ms", metricName, timer.mean(TimeUnit.MILLISECONDS));
                return metrics;
            }

            // 4. 如果找不到指标，返回默认值
            log.warn("未找到业务指标：{}，返回默认值", metricName);
            metrics.put("value", 0);
            metrics.put("type", "unknown");

        } catch (Exception e) {
            log.error("采集业务指标失败，指标名称：{}", metricName, e);
            metrics.put("value", 0);
            metrics.put("type", "error");
        }

        return metrics;
    }

    /**
     * 记录指标
     * <p>
     * 企业级实现：
     * - 存储指标到Redis（时序数据）
     * - 支持标签（tags）分类
     * - 设置过期时间
     * - 支持指标聚合查询
     * </p>
     *
     * @param metricName 指标名称
     * @param value      指标值
     * @param tags       标签（用于分类和查询）
     */
    public void recordMetric(String metricName, Double value, Map<String, String> tags) {
        log.debug("记录指标，名称：{}，值：{}，标签：{}", metricName, value, tags);

        try {
            if (!isRedisAvailable()) {
                log.warn("RedisTemplate未配置或不可用，无法存储指标");
                return;
            }

            // 构建Redis Key
            StringBuilder keyBuilder = new StringBuilder(METRIC_PREFIX);
            keyBuilder.append(metricName);

            if (tags != null && !tags.isEmpty()) {
                // 将标签添加到Key中
                tags.forEach((k, v) -> keyBuilder.append(":").append(k).append("=").append(v));
            }

            keyBuilder.append(":").append(System.currentTimeMillis());

            String key = keyBuilder.toString();

            // 构建指标数据
            Map<String, Object> metricData = new HashMap<>();
            metricData.put("metricName", metricName);
            metricData.put("value", value);
            metricData.put("timestamp", LocalDateTime.now().toString());
            metricData.put("tags", tags);

            // 存储到Redis，设置过期时间
            redisTemplate.opsForValue().set(key, metricData, METRIC_EXPIRE_SECONDS, TimeUnit.SECONDS);

            // 同时更新最新值（用于快速查询）
            StringBuilder latestKeyBuilder = new StringBuilder(METRIC_PREFIX + "latest:" + metricName);
            if (tags != null && !tags.isEmpty()) {
                tags.forEach((k, v) -> latestKeyBuilder.append(":").append(k).append("=").append(v));
            }
            String latestKey = latestKeyBuilder.toString();
            redisTemplate.opsForValue().set(latestKey, metricData, METRIC_EXPIRE_SECONDS, TimeUnit.SECONDS);

            log.debug("指标已记录到Redis，Key：{}", key);

        } catch (Exception e) {
            log.error("记录指标失败，指标名称：{}，值：{}", metricName, value, e);
        }
    }
}

