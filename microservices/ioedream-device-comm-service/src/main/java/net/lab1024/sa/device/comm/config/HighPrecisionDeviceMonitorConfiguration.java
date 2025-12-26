package net.lab1024.sa.device.comm.config;

import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.device.comm.monitor.HighPrecisionDeviceMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 高精度设备监控配置类
 * <p>
 * 高精度设备监控相关的Spring配置
 * </p>
 * <p>
 * ⚠️ 线程池已废弃：所有线程池Bean已废弃，请使用 UnifiedThreadPoolConfiguration 中的统一线程池：
 * - coreExecutor: CPU密集型任务
 * - ioExecutor: IO密集型任务
 * - scheduledExecutor: 定时任务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 * @deprecated 线程池配置请使用 UnifiedThreadPoolConfiguration
 */
@Deprecated
@Configuration
@EnableAsync
@EnableScheduling
@Schema(description = "高精度设备监控配置")
@Slf4j
public class HighPrecisionDeviceMonitorConfiguration {


    @Resource
    private DeviceDao deviceDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 注册高精度设备监控器
     *
     * @return HighPrecisionDeviceMonitor实例
     */
    @Bean(name = "highPrecisionDeviceMonitor")
    public HighPrecisionDeviceMonitor highPrecisionDeviceMonitor() {
        HighPrecisionDeviceMonitor monitor = new HighPrecisionDeviceMonitor();
        log.info("[高精度监控配置] 注册高精度设备监控器成功");
        return monitor;
    }

    /**
     * 配置高精度监控异步任务执行器
     * 用于处理高精度监控的异步操作
     *
     * @return Executor实例
     */
    @Bean(name = "highPrecisionMonitorTaskExecutor")
    public Executor highPrecisionMonitorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数（支持高并发监控）
        executor.setCorePoolSize(20);
        // 最大线程数（应对监控峰值）
        executor.setMaxPoolSize(100);
        // 队列容量
        executor.setQueueCapacity(2000);
        // 线程名前缀
        executor.setThreadNamePrefix("high-precision-monitor-async-");
        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        // 线程空闲时间
        executor.setKeepAliveSeconds(30);
        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        // 初始化
        executor.initialize();

        log.info("[高精度监控配置] 高精度监控异步任务执行器配置完成, corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * 配置高精度监控数据收集执行器
     * 用于处理设备数据收集和指标计算
     *
     * @return Executor实例
     */
    @Bean(name = "highPrecisionDataCollectorExecutor")
    public Executor highPrecisionDataCollectorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 数据收集线程池配置
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("high-precision-data-collector-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();

        log.info("[高精度监控配置] 高精度监控数据收集执行器配置完成, corePoolSize={}, maxPoolSize={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    /**
     * 配置高精度监控批量处理执行器
     * 用于处理批量设备监控任务
     *
     * @return Executor实例
     */
    @Bean(name = "highPrecisionBatchProcessorExecutor")
    public Executor highPrecisionBatchProcessorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 批量处理线程池配置
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("high-precision-batch-processor-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(120);
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();

        log.info("[高精度监控配置] 高精度监控批量处理执行器配置完成, corePoolSize={}, maxPoolSize={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    /**
     * 配置高精度监控缓存清理执行器
     * 用于处理缓存清理和数据过期任务
     *
     * @return Executor实例
     */
    @Bean(name = "highPrecisionCacheCleanupExecutor")
    public Executor highPrecisionCacheCleanupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 缓存清理线程池配置（较小规模）
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("high-precision-cache-cleanup-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();

        log.info("[高精度监控配置] 高精度监控缓存清理执行器配置完成, corePoolSize={}, maxPoolSize={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }
}
