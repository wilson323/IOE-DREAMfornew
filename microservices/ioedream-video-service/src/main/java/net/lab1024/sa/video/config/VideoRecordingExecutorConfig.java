package net.lab1024.sa.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 视频录像异步执行器配置
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Configuration
@EnableAsync
public class VideoRecordingExecutorConfig {

    /**
     * 视频录像异步执行器
     * 用于异步启动录像任务
     */
    @Bean("videoRecordingExecutor")
    public Executor videoRecordingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：同时能执行的最大录像任务数
        executor.setCorePoolSize(5);

        // 最大线程数：高峰期能支持的最大录像任务数
        executor.setMaxPoolSize(20);

        // 队列容量：等待执行的任务队列大小
        executor.setQueueCapacity(100);

        // 线程名称前缀
        executor.setThreadNamePrefix("video-recording-");

        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：队列满时，由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        return executor;
    }

    /**
     * 视频处理异步执行器
     * 用于视频文件处理、转码等耗时操作
     */
    @Bean("videoProcessingExecutor")
    public Executor videoProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("video-processing-");
        executor.setKeepAliveSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);

        executor.initialize();

        return executor;
    }
}
