package net.lab1024.sa.common.notification.manager;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

/**
 * 通知系统监控指标收集器
 * <p>
 * 统一收集通知系统的监控指标
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 企业级特性：
 * - 集成Micrometer进行指标收集
 * - 支持Prometheus格式导出
 * - 实时指标统计和查询
 * - 支持多维度标签（渠道、状态、错误类型等）
 * </p>
 * <p>
 * 监控指标：
 * - 发送总数（按渠道、状态）
 * - 发送成功率（按渠道）
 * - 发送耗时（P50、P90、P95、P99）
 * - 限流触发次数（按渠道）
 * - 重试次数（按渠道）
 * - 错误统计（按错误类型）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class NotificationMetricsCollector {

    private final MeterRegistry meterRegistry;

    /**
     * 指标名称常量
     */
    private static final String METRIC_NOTIFICATION_SEND_TOTAL = "notification.send.total";
    private static final String METRIC_NOTIFICATION_SEND_DURATION = "notification.send.duration";
    private static final String METRIC_NOTIFICATION_RATE_LIMIT = "notification.rate_limit.total";
    private static final String METRIC_NOTIFICATION_RETRY = "notification.retry.total";
    private static final String METRIC_NOTIFICATION_ERROR = "notification.error.total";

    /**
     * 标签键常量
     */
    private static final String TAG_CHANNEL = "channel";
    private static final String TAG_STATUS = "status";
    private static final String TAG_ERROR_TYPE = "error_type";

    /**
     * 状态标签值
     */
    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_FAILURE = "failure";

    /**
     * 构造函数
     *
     * @param meterRegistry Micrometer指标注册表
     */
    public NotificationMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 记录通知发送成功
     * <p>
     * 记录发送成功次数和耗时
     * </p>
     *
     * @param channel 通知渠道
     * @param durationMs 发送耗时（毫秒）
     */
    public void recordSuccess(String channel, long durationMs) {
        try {
            // 记录发送总数（成功）
            Counter.builder(METRIC_NOTIFICATION_SEND_TOTAL)
                    .tag(TAG_CHANNEL, channel)
                    .tag(TAG_STATUS, STATUS_SUCCESS)
                    .register(meterRegistry)
                    .increment();

            // 记录发送耗时
            Timer.builder(METRIC_NOTIFICATION_SEND_DURATION)
                    .tag(TAG_CHANNEL, channel)
                    .tag(TAG_STATUS, STATUS_SUCCESS)
                    .register(meterRegistry)
                    .record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

            log.debug("[通知监控] 记录发送成功，渠道：{}，耗时：{}ms", channel, durationMs);

        } catch (Exception e) {
            log.error("[通知监控] 记录发送成功指标失败，渠道：{}", channel, e);
        }
    }

    /**
     * 记录通知发送失败
     * <p>
     * 记录发送失败次数、耗时和错误类型
     * </p>
     *
     * @param channel 通知渠道
     * @param durationMs 发送耗时（毫秒）
     * @param errorType 错误类型
     */
    public void recordFailure(String channel, long durationMs, String errorType) {
        try {
            // 记录发送总数（失败）
            Counter.builder(METRIC_NOTIFICATION_SEND_TOTAL)
                    .tag(TAG_CHANNEL, channel)
                    .tag(TAG_STATUS, STATUS_FAILURE)
                    .tag(TAG_ERROR_TYPE, errorType != null ? errorType : "unknown")
                    .register(meterRegistry)
                    .increment();

            // 记录发送耗时
            Timer.builder(METRIC_NOTIFICATION_SEND_DURATION)
                    .tag(TAG_CHANNEL, channel)
                    .tag(TAG_STATUS, STATUS_FAILURE)
                    .register(meterRegistry)
                    .record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

            // 记录错误统计
            Counter.builder(METRIC_NOTIFICATION_ERROR)
                    .tag(TAG_CHANNEL, channel)
                    .tag(TAG_ERROR_TYPE, errorType != null ? errorType : "unknown")
                    .register(meterRegistry)
                    .increment();

            log.debug("[通知监控] 记录发送失败，渠道：{}，耗时：{}ms，错误类型：{}",
                    channel, durationMs, errorType);

        } catch (Exception e) {
            log.error("[通知监控] 记录发送失败指标失败，渠道：{}", channel, e);
        }
    }

    /**
     * 记录限流触发
     * <p>
     * 记录限流触发次数
     * </p>
     *
     * @param channel 通知渠道
     */
    public void recordRateLimit(String channel) {
        try {
            Counter.builder(METRIC_NOTIFICATION_RATE_LIMIT)
                    .tag(TAG_CHANNEL, channel)
                    .register(meterRegistry)
                    .increment();

            log.debug("[通知监控] 记录限流触发，渠道：{}", channel);

        } catch (Exception e) {
            log.error("[通知监控] 记录限流触发指标失败，渠道：{}", channel, e);
        }
    }

    /**
     * 记录重试次数
     * <p>
     * 记录重试次数（按渠道）
     * </p>
     *
     * @param channel 通知渠道
     * @param retryCount 重试次数
     */
    public void recordRetry(String channel, int retryCount) {
        try {
            Counter.builder(METRIC_NOTIFICATION_RETRY)
                    .tag(TAG_CHANNEL, channel)
                    .register(meterRegistry)
                    .increment(retryCount);

            log.debug("[通知监控] 记录重试次数，渠道：{}，重试次数：{}", channel, retryCount);

        } catch (Exception e) {
            log.error("[通知监控] 记录重试次数指标失败，渠道：{}", channel, e);
        }
    }

    /**
     * 获取发送成功率
     * <p>
     * 计算指定渠道的发送成功率
     * </p>
     *
     * @param channel 通知渠道
     * @return 发送成功率（0.0-1.0）
     */
    public double getSuccessRate(String channel) {
        try {
            double successCount = meterRegistry.counter(METRIC_NOTIFICATION_SEND_TOTAL,
                    TAG_CHANNEL, channel, TAG_STATUS, STATUS_SUCCESS).count();
            double failureCount = meterRegistry.counter(METRIC_NOTIFICATION_SEND_TOTAL,
                    TAG_CHANNEL, channel, TAG_STATUS, STATUS_FAILURE).count();

            double totalCount = successCount + failureCount;
            if (totalCount == 0) {
                return 0.0;
            }

            return successCount / totalCount;

        } catch (Exception e) {
            log.error("[通知监控] 获取发送成功率失败，渠道：{}", channel, e);
            return 0.0;
        }
    }

    /**
     * 获取平均发送耗时
     * <p>
     * 计算指定渠道的平均发送耗时（毫秒）
     * </p>
     *
     * @param channel 通知渠道
     * @return 平均发送耗时（毫秒）
     */
    public double getAvgDuration(String channel) {
        try {
            Timer timer = meterRegistry.find(METRIC_NOTIFICATION_SEND_DURATION)
                    .tag(TAG_CHANNEL, channel)
                    .tag(TAG_STATUS, STATUS_SUCCESS)
                    .timer();

            if (timer == null || timer.count() == 0) {
                return 0.0;
            }

            return timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            log.error("[通知监控] 获取平均发送耗时失败，渠道：{}", channel, e);
            return 0.0;
        }
    }

    /**
     * 获取P99发送耗时
     * <p>
     * 计算指定渠道的P99发送耗时（毫秒）
     * 注意：使用max()方法作为P99的近似值（percentile()方法已废弃）
     * 如果需要精确的P99值，需要配置Micrometer的percentile histogram
     * </p>
     *
     * @param channel 通知渠道
     * @return P99发送耗时（毫秒）
     */
    public double getP99Duration(String channel) {
        try {
            Timer timer = meterRegistry.find(METRIC_NOTIFICATION_SEND_DURATION)
                    .tag(TAG_CHANNEL, channel)
                    .tag(TAG_STATUS, STATUS_SUCCESS)
                    .timer();

            if (timer == null || timer.count() == 0) {
                return 0.0;
            }

            // 使用max()方法作为P99的近似值（percentile()方法已废弃）
            // 如果需要精确的P99值，需要配置Timer.Builder.publishPercentiles(0.99)
            // 然后通过percentileValues()方法获取
            return timer.max(java.util.concurrent.TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            log.error("[通知监控] 获取P99发送耗时失败，渠道：{}", channel, e);
            return 0.0;
        }
    }

    /**
     * 获取发送总数
     * <p>
     * 获取指定渠道的发送总数
     * </p>
     *
     * @param channel 通知渠道
     * @return 发送总数
     */
    public double getTotalCount(String channel) {
        try {
            double successCount = meterRegistry.counter(METRIC_NOTIFICATION_SEND_TOTAL,
                    TAG_CHANNEL, channel, TAG_STATUS, STATUS_SUCCESS).count();
            double failureCount = meterRegistry.counter(METRIC_NOTIFICATION_SEND_TOTAL,
                    TAG_CHANNEL, channel, TAG_STATUS, STATUS_FAILURE).count();

            return successCount + failureCount;

        } catch (Exception e) {
            log.error("[通知监控] 获取发送总数失败，渠道：{}", channel, e);
            return 0.0;
        }
    }

    /**
     * 获取限流触发次数
     * <p>
     * 获取指定渠道的限流触发次数
     * </p>
     *
     * @param channel 通知渠道
     * @return 限流触发次数
     */
    public double getRateLimitCount(String channel) {
        try {
            return meterRegistry.counter(METRIC_NOTIFICATION_RATE_LIMIT,
                    TAG_CHANNEL, channel).count();

        } catch (Exception e) {
            log.error("[通知监控] 获取限流触发次数失败，渠道：{}", channel, e);
            return 0.0;
        }
    }

    /**
     * 获取重试总次数
     * <p>
     * 获取指定渠道的重试总次数
     * </p>
     *
     * @param channel 通知渠道
     * @return 重试总次数
     */
    public double getRetryCount(String channel) {
        try {
            return meterRegistry.counter(METRIC_NOTIFICATION_RETRY,
                    TAG_CHANNEL, channel).count();

        } catch (Exception e) {
            log.error("[通知监控] 获取重试总次数失败，渠道：{}", channel, e);
            return 0.0;
        }
    }
}
