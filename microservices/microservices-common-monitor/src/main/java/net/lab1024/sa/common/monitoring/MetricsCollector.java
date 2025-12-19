package net.lab1024.sa.common.monitoring;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 指标收集器
 * <p>
 * 收集系统运行时的各项指标数据
 * 支持HTTP请求统计、错误统计、业务操作统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Slf4j
public class MetricsCollector {

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong errorRequests = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicLong businessOperations = new AtomicLong(0);
    private final AtomicLong businessErrorCount = new AtomicLong(0);

    /**
     * 记录请求
     */
    public void recordRequest() {
        totalRequests.incrementAndGet();
    }

    /**
     * 记录错误请求
     */
    public void recordErrorRequest() {
        errorRequests.incrementAndGet();
        errorCount.incrementAndGet();
    }

    /**
     * 记录业务操作
     */
    public void recordBusinessOperation() {
        businessOperations.incrementAndGet();
    }

    /**
     * 记录业务错误
     */
    public void recordBusinessError() {
        businessErrorCount.incrementAndGet();
    }

    /**
     * 获取指标概览
     *
     * @return 指标概览对象
     */
    public MetricOverview getMetricOverview() {
        MetricOverview overview = new MetricOverview();
        overview.setTotalRequests(totalRequests.get());
        overview.setErrorRequests(errorRequests.get());
        overview.setErrorCount(errorCount.get());
        overview.setBusinessOperations(businessOperations.get());
        overview.setBusinessErrorCount(businessErrorCount.get());
        return overview;
    }

    /**
     * 重置所有计数器
     */
    public void reset() {
        totalRequests.set(0);
        errorRequests.set(0);
        errorCount.set(0);
        businessOperations.set(0);
        businessErrorCount.set(0);
    }

    /**
     * 指标概览数据类
     */
    @Data
    public static class MetricOverview {
        /**
         * 总请求数
         */
        private long totalRequests;

        /**
         * 错误请求数
         */
        private long errorRequests;

        /**
         * 错误计数
         */
        private long errorCount;

        /**
         * 业务操作数
         */
        private long businessOperations;

        /**
         * 业务错误数
         */
        private long businessErrorCount;

        /**
         * 平均响应时间（毫秒）
         */
        private double averageResponseTime;

        /**
         * 缓存命中率
         */
        private double cacheHitRate;
    }
}
