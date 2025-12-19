package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 统一配置异步任务执行器，支持线程池优化和任务编排
 * - 独立线程池配置（taskExecutor、ioTaskExecutor、cpuTaskExecutor）
 * - 合理的线程池参数设置（根据任务类型优化）
 * - 完善的异常处理和超时控制
 * </p>
 * <p>
 * 迁移说明：从microservices-common-core迁移到microservices-common
 * 原因：common-core应保持最小稳定内核，不包含Spring配置类
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncTaskConfiguration {

    /**
     * 默认异步任务执行器
     * <p>
     * 用于通用的异步任务处理
     * </p>
     *
     * @return 线程池执行器
     */
    @Bean("taskExecutor")
    @ConditionalOnMissingBean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数 = CPU核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);

        // 最大线程数 = CPU核心数 * 2
        executor.setMaxPoolSize(corePoolSize * 2);

        // 队列容量
        executor.setQueueCapacity(200);

        // 线程名前缀
        executor.setThreadNamePrefix("async-task-");

        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(60);

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        log.info("[异步任务配置] 默认异步任务执行器初始化完成, corePoolSize={}, maxPoolSize={}",
                corePoolSize, corePoolSize * 2);
        return executor;
    }

    /**
     * IO密集型任务执行器
     * <p>
     * 用于数据库操作、网络请求等IO密集型任务
     * </p>
     *
     * @return 线程池执行器
     */
    @Bean("ioTaskExecutor")
    @ConditionalOnMissingBean(name = "ioTaskExecutor")
    public Executor ioTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // IO密集型任务：线程数 = CPU核心数 * 2
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);

        // 队列容量较大
        executor.setQueueCapacity(500);

        executor.setThreadNamePrefix("io-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        log.info("[异步任务配置] IO密集型任务执行器初始化完成, corePoolSize={}, maxPoolSize={}",
                corePoolSize, corePoolSize * 2);
        return executor;
    }

    /**
     * CPU密集型任务执行器
     * <p>
     * 用于计算、数据处理等CPU密集型任务
     * </p>
     *
     * @return 线程池执行器
     */
    @Bean("cpuTaskExecutor")
    @ConditionalOnMissingBean(name = "cpuTaskExecutor")
    public Executor cpuTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // CPU密集型任务：线程数 = CPU核心数 + 1
        int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize);

        // 队列容量较小
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("cpu-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setAllowCoreThreadTimeOut(false); // CPU密集型任务不允许核心线程超时
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        log.info("[异步任务配置] CPU密集型任务执行器初始化完成, corePoolSize={}, maxPoolSize={}",
                corePoolSize, corePoolSize);
        return executor;
    }
}

