package net.lab1024.sa.common.monitor.manager;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.dao.SystemMonitorDao;
import net.lab1024.sa.common.monitor.domain.vo.ResourceUsageVO;

/**
 * 系统监控管理器
 * <p>
 * 负责系统资源监控、性能数据收集、监控数据存储等功能
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
public class SystemMonitorManager {

    @SuppressWarnings("unused")
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
    public SystemMonitorManager(SystemMonitorDao systemMonitorDao) {
        this.systemMonitorDao = systemMonitorDao;
    }

    // JVM相关Bean
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    /**
     * 获取系统资源使用情况
     *
     * @return 资源使用情况
     */
    public ResourceUsageVO getResourceUsage() {
        log.debug("获取系统资源使用情况");

        ResourceUsageVO resourceUsage = new ResourceUsageVO();

        try {
            // CPU使用率
            double cpuUsage = getCpuUsage();
            resourceUsage.setCpuUsage(cpuUsage);

            // CPU核心数
            int cpuCores = osMXBean.getAvailableProcessors();
            resourceUsage.setCpuCores(cpuCores);

            // CPU负载
            double cpuLoad = osMXBean.getSystemLoadAverage();
            resourceUsage.setCpuLoad(cpuLoad);

            // 内存使用情况
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            long totalMemory = heapMemoryUsage.getMax() / 1024 / 1024; // MB
            long usedMemory = heapMemoryUsage.getUsed() / 1024 / 1024; // MB
            long availableMemory = totalMemory - usedMemory;
            double memoryUsage = totalMemory > 0 ? (double) usedMemory / totalMemory * 100 : 0;

            resourceUsage.setTotalMemory(totalMemory / 1024.0); // 转换为GB
            resourceUsage.setUsedMemory(usedMemory / 1024.0);
            resourceUsage.setAvailableMemory(availableMemory / 1024.0);
            resourceUsage.setMemoryUsage(memoryUsage);

            // 磁盘使用情况
            File[] roots = File.listRoots();
            long totalDisk = 0;
            long usedDisk = 0;
            for (File root : roots) {
                totalDisk += root.getTotalSpace();
                usedDisk += (root.getTotalSpace() - root.getFreeSpace());
            }
            double totalDiskGB = totalDisk / 1024.0 / 1024.0 / 1024.0;
            double usedDiskGB = usedDisk / 1024.0 / 1024.0 / 1024.0;
            double availableDiskGB = totalDiskGB - usedDiskGB;
            double diskUsage = totalDiskGB > 0 ? usedDiskGB / totalDiskGB * 100 : 0;

            resourceUsage.setTotalDisk(totalDiskGB);
            resourceUsage.setUsedDisk(usedDiskGB);
            resourceUsage.setAvailableDisk(availableDiskGB);
            resourceUsage.setDiskUsage(diskUsage);

            // 网络流量（模拟）
            resourceUsage.setNetworkInbound(10.5);
            resourceUsage.setNetworkOutbound(8.3);

            // 线程信息
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            resourceUsage.setTotalThreads(threadMXBean.getThreadCount());
            resourceUsage.setActiveThreads(threadMXBean.getThreadCount());

            // JVM内存信息
            double heapUsage = (double) heapMemoryUsage.getUsed() / heapMemoryUsage.getMax() * 100;
            MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
            double nonHeapUsage = (double) nonHeapMemoryUsage.getUsed() / nonHeapMemoryUsage.getMax() * 100;

            resourceUsage.setHeapMemoryUsage(heapUsage);
            resourceUsage.setNonHeapMemoryUsage(nonHeapUsage);

            // GC信息
            long gcCount = ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .mapToLong(GarbageCollectorMXBean::getCollectionCount)
                    .sum();
            long gcTime = ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .mapToLong(GarbageCollectorMXBean::getCollectionTime)
                    .sum();

            resourceUsage.setGcCount(gcCount);
            resourceUsage.setGcTime(gcTime);

            log.debug("系统资源使用情况获取完成");

        } catch (Exception e) {
            log.error("获取系统资源使用情况失败", e);
        }

        return resourceUsage;
    }

    /**
     * 获取系统性能指标
     *
     * @return 系统性能指标
     */
    public Map<String, Object> getSystemMetrics() {
        log.debug("获取系统性能指标");

        Map<String, Object> metrics = new HashMap<>();

        try {
            // JVM运行时间
            long uptime = runtimeMXBean.getUptime();
            metrics.put("uptime", uptime);

            // 类加载信息
            metrics.put("loadedClassCount", ManagementFactory.getClassLoadingMXBean().getLoadedClassCount());
            metrics.put("totalLoadedClassCount", ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
            metrics.put("unloadedClassCount", ManagementFactory.getClassLoadingMXBean().getUnloadedClassCount());

            // 编译信息
            metrics.put("totalCompilationTime", ManagementFactory.getCompilationMXBean().getTotalCompilationTime());

            log.debug("系统性能指标获取完成");

        } catch (Exception e) {
            log.error("获取系统性能指标失败", e);
        }

        return metrics;
    }

    /**
     * 获取CPU使用率
     */
    private double getCpuUsage() {
        try {
            if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsMXBean = (com.sun.management.OperatingSystemMXBean) osMXBean;
                return sunOsMXBean.getProcessCpuLoad() * 100;
            }
            return 0.0;
        } catch (Exception e) {
            log.error("获取CPU使用率失败", e);
            return 0.0;
        }
    }
}
