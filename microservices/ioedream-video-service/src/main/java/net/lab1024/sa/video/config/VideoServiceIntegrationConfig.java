package net.lab1024.sa.video.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 视频服务系统集成配置
 * <p>
 * 提供视频微服务的核心集成功能：
 * 1. 异步任务处理配置
 * 2. 定时任务调度配置
 * 3. 线程池管理
 * 4. 系统集成组件
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
public class VideoServiceIntegrationConfig {

    /**
     * 视频处理异步任务执行器
     * <p>
     * 专门用于处理视频相关的异步任务，如：
     * - 视频流处理
     * - AI分析任务
     * - 录像文件处理
     * - 批量设备操作
     * </p>
     *
     * @return 异步任务执行器
     */
    @Bean("videoTaskExecutor")
    @ConditionalOnMissingBean(name = "videoTaskExecutor")
    public Executor videoTaskExecutor() {
        log.info("[VideoTaskExecutor] 初始化视频处理异步任务执行器");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：CPU核心数
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());

        // 最大线程数：CPU核心数 * 2（视频处理需要较高并发）
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);

        // 队列容量：1000（避免任务堆积）
        executor.setQueueCapacity(1000);

        // 线程名前缀
        executor.setThreadNamePrefix("video-task-");

        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 线程空闲时间：60秒
        executor.setKeepAliveSeconds(60);

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间：30秒
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    /**
     * AI分析异步任务执行器
     * <p>
     * 专门用于处理AI智能分析相关的异步任务，如：
     * - 人脸识别分析
     * - 行为检测分析
     * - 目标检测分析
     * - 异常事件处理
     * </p>
     *
     * @return AI分析异步任务执行器
     */
    @Bean("aiAnalysisExecutor")
    @ConditionalOnMissingBean(name = "aiAnalysisExecutor")
    public Executor aiAnalysisExecutor() {
        log.info("[AIAnalysisExecutor] 初始化AI分析异步任务执行器");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // AI分析任务CPU密集，核心线程数适中
        executor.setCorePoolSize(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

        // 最大线程数：CPU核心数
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors());

        // 队列容量：500（AI分析任务相对较少）
        executor.setQueueCapacity(500);

        // 线程名前缀
        executor.setThreadNamePrefix("ai-analysis-");

        // 拒绝策略：记录日志并丢弃（AI分析失败不影响主业务）
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("[AI分析任务] 任务队列已满，丢弃任务: {}", r.toString());
        });

        // 线程空闲时间：120秒
        executor.setKeepAliveSeconds(120);

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();
        return executor;
    }

    /**
     * 视频流处理异步任务执行器
     * <p>
     * 专门用于处理实时视频流相关的异步任务，如：
     * - RTMP流转发
     * - HLS切片生成
     * - WebRTC信令处理
     * - 实时分析处理
     * </p>
     *
     * @return 视频流处理异步任务执行器
     */
    @Bean("videoStreamExecutor")
    @ConditionalOnMissingBean(name = "videoStreamExecutor")
    public Executor videoStreamExecutor() {
        log.info("[VideoStreamExecutor] 初始化视频流处理异步任务执行器");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 视频流处理需要较多线程
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);

        // 最大线程数：CPU核心数 * 4
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 4);

        // 队列容量：2000（视频流任务较多）
        executor.setQueueCapacity(2000);

        // 线程名前缀
        executor.setThreadNamePrefix("video-stream-");

        // 拒绝策略：调用线程执行（视频流不能丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 线程空闲时间：30秒
        executor.setKeepAliveSeconds(30);

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();
        return executor;
    }

    /**
     * 设备通讯异步任务执行器
     * <p>
     * 专门用于处理设备通讯相关的异步任务，如：
     * - 设备状态检测
     * - 设备控制指令
     * - 设备数据采集
     * - 设备告警处理
     * </p>
     *
     * @return 设备通讯异步任务执行器
     */
    @Bean("deviceCommExecutor")
    @ConditionalOnMissingBean(name = "deviceCommExecutor")
    public Executor deviceCommExecutor() {
        log.info("[DeviceCommExecutor] 初始化设备通讯异步任务执行器");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 设备通讯不需要太多线程
        executor.setCorePoolSize(Math.max(2, Runtime.getRuntime().availableProcessors() / 4));

        // 最大线程数：CPU核心数 / 2
        executor.setMaxPoolSize(Math.max(4, Runtime.getRuntime().availableProcessors() / 2));

        // 队列容量：300
        executor.setQueueCapacity(300);

        // 线程名前缀
        executor.setThreadNamePrefix("device-comm-");

        // 拒绝策略：记录日志并执行
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("[设备通讯任务] 任务队列已满，由调用线程执行: {}", r.toString());
            r.run();
        });

        // 线程空闲时间：180秒
        executor.setKeepAliveSeconds(180);

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();
        return executor;
    }
}