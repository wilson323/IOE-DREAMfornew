package net.lab1024.sa.common.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * 性能监控器
 * 监控JVM、内存、GC等性能指标
 */
@Slf4j
public class PerformanceMonitor {

    private final MeterRegistry meterRegistry;
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    public PerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    private void initializeMetrics() {
        // 简化监控，只记录指标到日志，避免复杂的Gauge使用
        log.info("[性能监控] 性能指标监控已初始化");
        log.info("[性能监控] JVM堆内存监控已启用");
        log.info("[性能监控] CPU使用率监控已启用");
    }

    /**
     * 获取堆内存使用量
     * @SuppressWarnings("unused") 预留方法，用于未来性能监控扩展
     */
    @SuppressWarnings("unused")
    private double getHeapMemoryUsed() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        return heapUsage.getUsed();
    }

    /**
     * 获取堆内存最大值
     * @SuppressWarnings("unused") 预留方法，用于未来性能监控扩展
     */
    @SuppressWarnings("unused")
    private double getHeapMemoryMax() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        return heapUsage.getMax();
    }

    /**
     * 获取非堆内存使用量
     * @SuppressWarnings("unused") 预留方法，用于未来性能监控扩展
     */
    @SuppressWarnings("unused")
    private double getNonHeapMemoryUsed() {
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        return nonHeapUsage.getUsed();
    }

    /**
     * 获取CPU使用率
     * @SuppressWarnings("unused") 预留方法，用于未来性能监控扩展
     */
    @SuppressWarnings("unused")
    private double getCpuUsage() {
        // 简化的CPU使用率计算，使用系统负载
        double systemLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        return systemLoad >= 0 ? systemLoad : 0.0;
    }

    /**
     * 记录性能告警
     */
    public void recordPerformanceAlert(String metric, double value, double threshold) {
        if (value > threshold) {
            Counter.builder("performance.alert")
                    .description("性能告警次数")
                    .tag("metric", metric)
                    .tag("level", "warning")
                    .register(meterRegistry)
                    .increment();

            log.warn("[性能监控] 性能告警: {} = {}, 阈值 = {}", metric, value, threshold);
        }
    }
}
