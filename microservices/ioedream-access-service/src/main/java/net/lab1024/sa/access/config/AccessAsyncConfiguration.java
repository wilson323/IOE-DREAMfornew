package net.lab1024.sa.access.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 门禁服务异步任务配置
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 配置异步线程池
 * - 合理的线程池参数
 * - 完整的异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@EnableAsync
public class AccessAsyncConfiguration {

    /**
     * 权限同步线程池
     * <p>
     * 用于权限变更时的权限同步任务
     * </p>
     */
    @Bean("permissionSyncExecutor")
    public Executor permissionSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("access-permission-sync-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("[异步配置] 权限同步线程池初始化完成, corePoolSize=5, maxPoolSize=20");
        return executor;
    }
}
