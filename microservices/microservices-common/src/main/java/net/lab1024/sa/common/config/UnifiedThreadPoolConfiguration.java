package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 全局统一线程池配置
 *
 * <p>设计原则:</p>
 * <ul>
 *   <li>1. coreExecutor: CPU密集型，核心线程数 = CPU+1</li>
 *   <li>2. ioExecutor: IO密集型，核心线程数 = CPU×2</li>
 *   <li>3. scheduledExecutor: 定时任务，固定小池</li>
 *   <li>4. 拒绝策略: CallerRunsPolicy（降级由调用线程执行）</li>
 *   <li>5. allowCoreThreadTimeOut: true，允许核心线程超时回收</li>
 * </ul>
 *
 * <p>预期效果: 线程数从~300减少到~80，内存节省200-500MB</p>
 * <p>
 * 迁移说明：从microservices-common-core迁移到microservices-common
 * 原因：common-core应保持最小稳定内核，不包含Spring配置类
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Slf4j
@Configuration
public class UnifiedThreadPoolConfiguration {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 核心业务线程池（CPU密集型）- 主线程池
     * <p>适用: API请求、业务计算、数据处理</p>
     * <p>核心线程数: CPU核心数+1</p>
     * <p>最大线程数: CPU核心数×2</p>
     * <p>队列容量: 500</p>
     */
    @Bean(name = {"coreExecutor", "businessExecutor"})
    @Primary
    public ThreadPoolTaskExecutor coreExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_COUNT + 1);
        executor.setMaxPoolSize(CPU_COUNT * 2);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setThreadNamePrefix("core-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("[线程池] coreExecutor初始化 - 核心:{}, 最大:{}, 队列:{}",
                CPU_COUNT + 1, CPU_COUNT * 2, 500);
        return executor;
    }

    /**
     * IO密集型线程池
     * <p>适用: 数据库查询、外部API调用、文件IO</p>
     * <p>核心线程数: CPU核心数×2</p>
     * <p>最大线程数: CPU核心数×4</p>
     * <p>队列容量: 1000</p>
     */
    @Bean(name = {"ioExecutor", "asyncExecutor"})
    public ThreadPoolTaskExecutor ioExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_COUNT * 2);
        executor.setMaxPoolSize(CPU_COUNT * 4);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(120);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setThreadNamePrefix("io-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("[线程池] ioExecutor初始化 - 核心:{}, 最大:{}, 队列:{}",
                CPU_COUNT * 2, CPU_COUNT * 4, 1000);
        return executor;
    }

    /**
     * 定时任务线程池（固定小池）
     * <p>适用: 定时任务、调度任务</p>
     * <p>核心线程数: 5</p>
     * <p>最大线程数: 20</p>
     * <p>队列容量: 100</p>
     */
    @Bean(name = "scheduledExecutor")
    public ThreadPoolTaskExecutor scheduledExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setThreadNamePrefix("sched-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("[线程池] scheduledExecutor初始化 - 核心:5, 最大:20, 队列:100");
        return executor;
    }
}

