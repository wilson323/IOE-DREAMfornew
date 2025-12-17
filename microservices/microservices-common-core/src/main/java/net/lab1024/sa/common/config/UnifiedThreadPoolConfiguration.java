package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 统一线程池配置
 * 
 * <p>线程池设计原则:</p>
 * <ul>
 *   <li>1. CPU密集型: 核心线程数 = CPU核心数+1</li>
 *   <li>2. IO密集型: 核心线程数 = CPU核心数×2</li>
 *   <li>3. 混合型: 核心线程数 = CPU核心数×1.5</li>
 *   <li>4. 队列容量: 核心线程数×2~5倍</li>
 *   <li>5. 拒绝策略: CallerRunsPolicy（降级由调用线程执行）</li>
 * </ul>
 * 
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Slf4j
@Configuration
public class UnifiedThreadPoolConfiguration {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 业务处理线程池（CPU密集型）
     * <p>核心线程数: CPU核心数+1</p>
     * <p>最大线程数: CPU核心数×2</p>
     * <p>队列容量: 200</p>
     */
    @Bean(name = "businessExecutor")
    public ThreadPoolTaskExecutor businessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_COUNT + 1);
        executor.setMaxPoolSize(CPU_COUNT * 2);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("business-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("[线程池配置] 业务处理线程池初始化 - 核心:{}, 最大:{}, 队列:{}",
                CPU_COUNT + 1, CPU_COUNT * 2, 200);
        return executor;
    }

    /**
     * 异步任务线程池（IO密集型）
     * <p>核心线程数: CPU核心数×2</p>
     * <p>最大线程数: CPU核心数×4</p>
     * <p>队列容量: 500</p>
     */
    @Bean(name = "asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_COUNT * 2);
        executor.setMaxPoolSize(CPU_COUNT * 4);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("[线程池配置] 异步任务线程池初始化 - 核心:{}, 最大:{}, 队列:{}",
                CPU_COUNT * 2, CPU_COUNT * 4, 500);
        return executor;
    }

    /**
     * 定时任务线程池
     * <p>核心线程数: 10</p>
     * <p>最大线程数: 50</p>
     * <p>队列容量: 100</p>
     */
    @Bean(name = "scheduledExecutor")
    public ThreadPoolTaskExecutor scheduledExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("scheduled-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("[线程池配置] 定时任务线程池初始化 - 核心:10, 最大:50, 队列:100");
        return executor;
    }
}
