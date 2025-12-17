package net.lab1024.sa.device.comm.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.dao.DeviceCommLogDao;
import net.lab1024.sa.device.comm.protocol.rs485.RS485ProtocolAdapter;
import net.lab1024.sa.device.comm.protocol.rs485.RS485ProtocolManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * RS485协议配置类
 * <p>
 * RS485工业设备协议相关的Spring配置
 * </p>
 * <p>
 * ⚠️ 线程池已废弃：rs485TaskExecutor、rs485MonitorExecutor已废弃，
 * 请使用 UnifiedThreadPoolConfiguration 中的统一线程池：
 * - ioExecutor: IO密集型任务（设备通讯）
 * - scheduledExecutor: 定时监控任务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
@Schema(description = "RS485协议配置")
public class RS485ProtocolConfiguration {

    @Resource
    private DeviceCommLogDao deviceCommLogDao;

    /**
     * 注册RS485协议适配器
     *
     * @return RS485ProtocolAdapter实例
     */
    @Bean(name = "rs485ProtocolAdapter")
    public RS485ProtocolAdapter rs485ProtocolAdapter() {
        RS485ProtocolAdapter adapter = new RS485ProtocolAdapter();
        log.info("[RS485配置] 注册RS485协议适配器成功");
        return adapter;
    }

    /**
     * 注册RS485协议管理器
     * 通过构造函数注入依赖，将纯Java类注册为Spring Bean
     *
     * @param rs485ProtocolAdapter RS485协议适配器
     * @return RS485ProtocolManager实例
     */
    @Bean(name = "rs485ProtocolManager")
    public RS485ProtocolManager rs485ProtocolManager(RS485ProtocolAdapter rs485ProtocolAdapter) {
        RS485ProtocolManager manager = new RS485ProtocolManager(rs485ProtocolAdapter, deviceCommLogDao);
        log.info("[RS485配置] 注册RS485协议管理器成功");
        return manager;
    }

    /**
     * 配置RS485异步任务执行器
     * 用于处理RS485设备通讯的异步操作
     *
     * @return Executor实例
     * @deprecated 请使用 UnifiedThreadPoolConfiguration 中的 ioExecutor
     */
    @Deprecated
    @Bean(name = "rs485TaskExecutor")
    public Executor rs485TaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(10);
        // 最大线程数
        executor.setMaxPoolSize(50);
        // 队列容量
        executor.setQueueCapacity(1000);
        // 线程名前缀
        executor.setThreadNamePrefix("rs485-async-");
        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        // 初始化
        executor.initialize();

        log.info("[RS485配置] RS485异步任务执行器配置完成, corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * 配置RS485监控任务执行器
     * 用于处理设备状态监控和性能统计的定时任务
     *
     * @return Executor实例
     * @deprecated 请使用 UnifiedThreadPoolConfiguration 中的 scheduledExecutor
     */
    @Deprecated
    @Bean(name = "rs485MonitorExecutor")
    public Executor rs485MonitorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 监控任务线程池较小
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("rs485-monitor-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();

        log.info("[RS485配置] RS485监控任务执行器配置完成, corePoolSize={}, maxPoolSize={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }
}