package net.lab1024.sa.common.monitoring;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * 指标收集器
 * <p>
 * 职责：
 * - 收集应用运行时指标，如异常计数、方法执行时间等
 * - 提供统一的指标记录接口
 * </p>
 * <p>
 * 注意：这是一个纯Java类，通过构造函数注入MeterRegistry。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class MetricsCollector {

    private final MeterRegistry meterRegistry;

    public MetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 记录指标
     *
     * @param name 指标名称
     * @param value 指标值
     */
    public void record(String name, double value) {
        if (meterRegistry != null) {
            meterRegistry.counter(name).increment(value);
        }
    }

    /**
     * 记录业务指标
     *
     * @param metricName 指标名称
     * @param value      值
     * @param tags       标签
     */
    public void recordBusinessMetric(String metricName, double value, String... tags) {
        if (meterRegistry != null && tags.length >= 2) {
            // 将tags数组转换为Map或使用Micrometer的tag方法
            io.micrometer.core.instrument.Counter.Builder counterBuilder = io.micrometer.core.instrument.Counter.builder(metricName);
            for (int i = 0; i < tags.length - 1; i += 2) {
                counterBuilder.tag(tags[i], tags[i + 1]);
            }
            counterBuilder.register(meterRegistry).increment(value);
        }
    }

    /**
     * 记录异常指标
     *
     * @param exception 异常
     * @param duration 持续时间
     */
    public void recordException(Exception exception, long duration) {
        if (meterRegistry != null) {
            meterRegistry.counter("exception", "type", exception.getClass().getSimpleName()).increment();
            meterRegistry.timer("exception.duration").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }
}
