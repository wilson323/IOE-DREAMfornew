package net.lab1024.sa.gateway.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.dao.SystemLogDao;
import net.lab1024.sa.common.monitor.dao.SystemMonitorDao;
import net.lab1024.sa.common.monitor.manager.DruidConnectionPoolMonitor;
import net.lab1024.sa.common.monitor.manager.LogManagementManager;
import net.lab1024.sa.common.monitor.manager.PerformanceMonitorManager;
import net.lab1024.sa.common.monitor.manager.SystemMonitorManager;

/**
 * 监控Manager统一配置类
 * <p>
 * 符合CLAUDE.md规范 - Manager类通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 注册所有监控相关Manager为Spring Bean
 * - 统一管理监控模块的依赖注入
 * </p>
 * <p>
 * 包含的Manager：
 * - SystemMonitorManager: 系统资源监控（CPU、内存、磁盘）
 * - PerformanceMonitorManager: JVM性能监控（GC、线程、堆内存）
 * - LogManagementManager: 系统日志管理
 * - DruidConnectionPoolMonitor: 数据库连接池监控
 * </p>
 * <p>
 * 企业级特性：
 * - 完整的系统资源监控
 * - JVM性能指标采集
 * - 异步日志收集
 * - 连接池健康监控
 * - Prometheus指标导出
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Configuration
public class MonitorManagersConfig {

    @Resource
    private SystemMonitorDao systemMonitorDao;

    @Resource
    private SystemLogDao systemLogDao;

    @Resource
    private DataSource dataSource;

    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 注册企业级SystemMonitorManager
     * <p>
     * 功能：
     * - 系统资源使用情况监控（CPU、内存、磁盘）
     * - JVM堆内存监控
     * - 线程池状态监控
     * - 运行时信息收集
     * </p>
     *
     * @return SystemMonitorManager实例
     */
    @Bean
    public SystemMonitorManager systemMonitorManager() {
        log.info("[SystemMonitorManager] 初始化企业级系统监控管理器");
        log.info("[SystemMonitorManager] SystemMonitorDao: {}", systemMonitorDao != null ? "已注入" : "未注入");

        SystemMonitorManager manager = new SystemMonitorManager(systemMonitorDao);

        log.info("[SystemMonitorManager] 企业级系统监控管理器初始化完成");
        log.info("[SystemMonitorManager] 监控项：CPU使用率, 内存使用率, 磁盘空间, JVM堆内存");
        
        return manager;
    }

    /**
     * 注册企业级PerformanceMonitorManager
     * <p>
     * 功能：
     * - JVM性能指标监控
     * - GC垃圾回收监控
     * - 线程状态监控
     * - 性能数据存储
     * </p>
     *
     * @return PerformanceMonitorManager实例
     */
    @Bean
    public PerformanceMonitorManager performanceMonitorManager() {
        log.info("[PerformanceMonitorManager] 初始化企业级性能监控管理器");
        log.info("[PerformanceMonitorManager] SystemMonitorDao: {}", systemMonitorDao != null ? "已注入" : "未注入");

        PerformanceMonitorManager manager = new PerformanceMonitorManager(systemMonitorDao);

        log.info("[PerformanceMonitorManager] 企业级性能监控管理器初始化完成");
        log.info("[PerformanceMonitorManager] 监控项：JVM堆内存, GC次数, 线程数, 运行时间");
        
        return manager;
    }

    /**
     * 注册企业级LogManagementManager
     * <p>
     * 功能：
     * - 系统日志收集
     * - 日志异步存储
     * - 日志查询和分析
     * - 日志统计
     * </p>
     *
     * @return LogManagementManager实例
     */
    @Bean
    public LogManagementManager logManagementManager() {
        log.info("[LogManagementManager] 初始化企业级日志管理器");
        log.info("[LogManagementManager] SystemLogDao: {}", systemLogDao != null ? "已注入" : "未注入");

        LogManagementManager manager = new LogManagementManager(systemLogDao);

        log.info("[LogManagementManager] 企业级日志管理器初始化完成");
        log.info("[LogManagementManager] 异步线程池：10线程");
        log.info("[LogManagementManager] 支持功能：日志收集, 查询, 统计, 分析");
        
        return manager;
    }

    /**
     * 注册企业级DruidConnectionPoolMonitor
     * <p>
     * 功能：
     * - Druid连接池状态监控
     * - 连接池利用率统计
     * - 连接泄漏检测
     * - Prometheus指标导出
     * </p>
     *
     * @return DruidConnectionPoolMonitor实例
     */
    @Bean
    public DruidConnectionPoolMonitor druidConnectionPoolMonitor() {
        log.info("[DruidConnectionPoolMonitor] 初始化企业级连接池监控管理器");
        log.info("[DruidConnectionPoolMonitor] DataSource: {}", dataSource != null ? "已注入" : "未注入");
        log.info("[DruidConnectionPoolMonitor] MeterRegistry: {}", meterRegistry != null ? "已注入" : "未注入");

        DruidConnectionPoolMonitor monitor = new DruidConnectionPoolMonitor(dataSource, meterRegistry);

        log.info("[DruidConnectionPoolMonitor] 企业级连接池监控管理器初始化完成");
        log.info("[DruidConnectionPoolMonitor] 监控项：活跃连接, 空闲连接, 等待连接, 连接利用率");
        
        return monitor;
    }
}
