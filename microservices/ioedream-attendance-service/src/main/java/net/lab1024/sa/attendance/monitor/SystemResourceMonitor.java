package net.lab1024.sa.attendance.monitor;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 系统资源监控服务
 * <p>
 * 监控JVM和系统资源使用情况
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
public class SystemResourceMonitor {

    /**
     * 获取JVM内存信息
     *
     * @return 内存信息
     */
    public Map<String, Object> getMemoryInfo() {
        try {
            Runtime runtime = Runtime.getRuntime();

            Map<String, Object> memoryInfo = new HashMap<>();
            memoryInfo.put("timestamp", LocalDateTime.now().toString());
            memoryInfo.put("maxMemory", bytesToMB(runtime.maxMemory())); // JVM最大内存
            memoryInfo.put("totalMemory", bytesToMB(runtime.totalMemory())); // JVM已分配内存
            memoryInfo.put("freeMemory", bytesToMB(runtime.freeMemory())); // JVM空闲内存
            memoryInfo.put("usedMemory", bytesToMB(runtime.totalMemory() - runtime.freeMemory())); // JVM已使用内存

            // 内存使用率
            double memoryUsageRate = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory() * 100;
            memoryInfo.put("memoryUsageRate", String.format("%.2f%%", memoryUsageRate));

            return memoryInfo;

        } catch (Exception e) {
            log.error("[系统资源监控] 获取内存信息失败: error={}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取线程信息
     *
     * @return 线程信息
     */
    public Map<String, Object> getThreadInfo() {
        try {
            Map<String, Object> threadInfo = new HashMap<>();
            threadInfo.put("timestamp", LocalDateTime.now().toString());
            threadInfo.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
            threadInfo.put("peakThreadCount", ManagementFactory.getThreadMXBean().getPeakThreadCount());
            threadInfo.put("daemonThreadCount", ManagementFactory.getThreadMXBean().getDaemonThreadCount());
            threadInfo.put("totalStartedThreadCount", ManagementFactory.getThreadMXBean().getTotalStartedThreadCount());

            return threadInfo;

        } catch (Exception e) {
            log.error("[系统资源监控] 获取线程信息失败: error={}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取JVM信息
     *
     * @return JVM信息
     */
    public Map<String, Object> getJvmInfo() {
        try {
            Map<String, Object> jvmInfo = new HashMap<>();
            jvmInfo.put("timestamp", LocalDateTime.now().toString());
            jvmInfo.put("javaVersion", System.getProperty("java.version"));
            jvmInfo.put("javaVendor", System.getProperty("java.vendor"));
            jvmInfo.put("javaHome", System.getProperty("java.home"));
            jvmInfo.put("osName", System.getProperty("os.name"));
            jvmInfo.put("osVersion", System.getProperty("os.version"));
            jvmInfo.put("osArch", System.getProperty("os.arch"));
            jvmInfo.put("processors", Runtime.getRuntime().availableProcessors());

            return jvmInfo;

        } catch (Exception e) {
            log.error("[系统资源监控] 获取JVM信息失败: error={}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取性能统计摘要
     *
     * @return 性能统计摘要
     */
    public Map<String, Object> getPerformanceSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("timestamp", LocalDateTime.now().toString());
        summary.put("memoryInfo", getMemoryInfo());
        summary.put("threadInfo", getThreadInfo());
        summary.put("jvmInfo", getJvmInfo());
        return summary;
    }

    /**
     * 字节转MB
     */
    private double bytesToMB(long bytes) {
        return bytes / 1024.0 / 1024.0;
    }
}
