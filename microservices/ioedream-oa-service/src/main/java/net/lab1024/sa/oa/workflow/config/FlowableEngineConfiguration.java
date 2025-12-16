package net.lab1024.sa.oa.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.IdentityService;
import org.flowable.app.api.AppRepositoryService;
import org.flowable.app.api.AppRuntimeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Flowable工作流引擎配置
 * <p>
 * 提供企业级工作流引擎的核心配置
 * 支持异步处理、事件监听、集群部署等高级功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Configuration
@EnableAsync
public class FlowableEngineConfiguration {

    /**
     * 流程定义服务Bean
     * 提供流程部署、定义管理等功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableRepositoryService flowableRepositoryService(RepositoryService repositoryService) {
        log.info("[Flowable配置] 初始化RepositoryService - 流程定义服务");
        return new FlowableRepositoryService(repositoryService);
    }

    /**
     * 流程运行时服务Bean
     * 提供流程启动、执行、信号等功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableRuntimeService flowableRuntimeService(RuntimeService runtimeService) {
        log.info("[Flowable配置] 初始化RuntimeService - 流程运行时服务");
        return new FlowableRuntimeService(runtimeService);
    }

    /**
     * 任务服务Bean
     * 提供任务查询、处理、委派等功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableTaskService flowableTaskService(TaskService taskService) {
        log.info("[Flowable配置] 初始化TaskService - 任务服务");
        return new FlowableTaskService(taskService);
    }

    /**
     * 历史服务Bean
     * 提供历史数据查询、统计等功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableHistoryService flowableHistoryService(HistoryService historyService) {
        log.info("[Flowable配置] 初始化HistoryService - 历史服务");
        return new FlowableHistoryService(historyService);
    }

    /**
     * 管理服务Bean
     * 提供引擎管理、性能监控等功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableManagementService flowableManagementService(ManagementService managementService) {
        log.info("[Flowable配置] 初始化ManagementService - 管理服务");
        return new FlowableManagementService(managementService);
    }

    /**
     * 身份服务Bean
     * 提供用户、组管理等功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableIdentityService flowableIdentityService(IdentityService identityService) {
        log.info("[Flowable配置] 初始化IdentityService - 身份服务");
        return new FlowableIdentityService(identityService);
    }

    /**
     * 应用存储库服务Bean
     * 提供应用定义管理功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableAppRepositoryService flowableAppRepositoryService(AppRepositoryService appRepositoryService) {
        log.info("[Flowable配置] 初始化AppRepositoryService - 应用存储库服务");
        return new FlowableAppRepositoryService(appRepositoryService);
    }

    /**
     * 应用运行时服务Bean
     * 提供应用实例管理功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableAppRuntimeService flowableAppRuntimeService(AppRuntimeService appRuntimeService) {
        log.info("[Flowable配置] 初始化AppRuntimeService - 应用运行时服务");
        return new FlowableAppRuntimeService(appRuntimeService);
    }

    /**
     * 异步任务执行器
     * 用于异步处理工作流任务，提高系统性能
     */
    @Bean(name = "flowableTaskExecutor")
    public Executor flowableTaskExecutor() {
        log.info("[Flowable配置] 初始化异步任务执行器");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(10);
        // 最大线程数
        executor.setMaxPoolSize(50);
        // 队列容量
        executor.setQueueCapacity(200);
        // 线程名前缀
        executor.setThreadNamePrefix("flowable-async-");
        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);
        // 线程空闲时间
        executor.setKeepAliveSeconds(300);

        executor.initialize();
        return executor;
    }

    /**
     * 流程事件监听器
     * 监听流程执行过程中的各种事件
     */
    @Bean
    public FlowableEventLogger flowableEventLogger() {
        log.info("[Flowable配置] 初始化事件监听器");
        return new FlowableEventLogger();
    }

    /**
     * 异步执行器Bean
     * 为Flowable提供异步处理能力
     */
    @Bean
    public AsyncTaskExecutor flowableAsyncTaskExecutor() {
        log.info("[Flowable配置] 初始化异步执行器");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("flowable-async-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Flowable配置完成后处理器
     * 在引擎初始化完成后执行自定义初始化逻辑
     */
    @Bean
    public FlowableInitializationProcessor flowableInitializationProcessor() {
        log.info("[Flowable配置] 初始化配置后处理器");
        return new FlowableInitializationProcessor();
    }
}