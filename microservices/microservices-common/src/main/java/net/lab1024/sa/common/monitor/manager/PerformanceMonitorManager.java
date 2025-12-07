package net.lab1024.sa.common.monitor.manager;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.domain.entity.SystemMonitorEntity;
import net.lab1024.sa.common.monitor.dao.SystemMonitorDao;

/**
 * 性能监控管理器
 * <p>
 * 负责JVM性能监控、GC监控、线程监控等性能相关功能
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖（DAO等）
 * - 在微服务中通过配置类注册为Spring Bean
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class PerformanceMonitorManager {

    private final SystemMonitorDao systemMonitorDao;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param systemMonitorDao 系统监控DAO
     */
    public PerformanceMonitorManager(SystemMonitorDao systemMonitorDao) {
        this.systemMonitorDao = systemMonitorDao;
    }

    // JVM相关Bean
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private final List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

    /**
     * 获取JVM性能指标
     *
     * @return JVM性能指标
     */
    public Map<String, Object> getJvmPerformanceMetrics() {
        log.debug("获取JVM性能指标");

        Map<String, Object> metrics = new HashMap<>();

        try {
            // 内存信息
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

            Map<String, Object> memoryMetrics = new HashMap<>();
            memoryMetrics.put("heapUsed", heapMemoryUsage.getUsed() / 1024 / 1024);
            memoryMetrics.put("heapMax", heapMemoryUsage.getMax() / 1024 / 1024);
            memoryMetrics.put("heapCommitted", heapMemoryUsage.getCommitted() / 1024 / 1024);
            memoryMetrics.put("nonHeapUsed", nonHeapMemoryUsage.getUsed() / 1024 / 1024);
            memoryMetrics.put("nonHeapMax", nonHeapMemoryUsage.getMax() / 1024 / 1024);
            metrics.put("memory", memoryMetrics);

            // 线程信息
            Map<String, Object> threadMetrics = new HashMap<>();
            threadMetrics.put("threadCount", threadMXBean.getThreadCount());
            threadMetrics.put("peakThreadCount", threadMXBean.getPeakThreadCount());
            threadMetrics.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
            threadMetrics.put("totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount());
            metrics.put("thread", threadMetrics);

            // GC信息
            List<Map<String, Object>> gcMetrics = new ArrayList<>();
            for (GarbageCollectorMXBean gcBean : gcMXBeans) {
                Map<String, Object> gc = new HashMap<>();
                gc.put("name", gcBean.getName());
                gc.put("collectionCount", gcBean.getCollectionCount());
                gc.put("collectionTime", gcBean.getCollectionTime());
                gcMetrics.add(gc);
            }
            metrics.put("gc", gcMetrics);

            // 运行时信息
            Map<String, Object> runtimeMetrics = new HashMap<>();
            runtimeMetrics.put("uptime", runtimeMXBean.getUptime());
            runtimeMetrics.put("startTime", runtimeMXBean.getStartTime());
            metrics.put("runtime", runtimeMetrics);

            metrics.put("collectTime", LocalDateTime.now());

            log.debug("JVM性能指标获取完成");

        } catch (Exception e) {
            log.error("获取JVM性能指标失败", e);
        }

        return metrics;
    }

    /**
     * 获取健康趋势
     *
     * @param hours 时间范围（小时）
     * @return 健康趋势数据
     */
    public List<Map<String, Object>> getHealthTrends(Integer hours) {
        log.debug("获取健康趋势，时间范围：{}小时", hours);

        try {
            LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
            LocalDateTime endTime = LocalDateTime.now();

            List<SystemMonitorEntity> monitorData = systemMonitorDao.selectByMonitorType("HEALTH", startTime, endTime);

            return monitorData.stream()
                    .map(entity -> {
                        Map<String, Object> trend = new HashMap<>();
                        trend.put("time", entity.getMonitorTime());
                        trend.put("status", entity.getStatus());
                        trend.put("metricValue", entity.getMetricValue());
                        return trend;
                    })
                    .collect(java.util.stream.Collectors.toList());

        } catch (Exception e) {
            log.error("获取健康趋势失败", e);
            return new ArrayList<>();
        }
    }

}

