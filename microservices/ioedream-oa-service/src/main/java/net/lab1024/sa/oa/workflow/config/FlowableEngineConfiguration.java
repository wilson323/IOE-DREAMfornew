package net.lab1024.sa.oa.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.IdentityService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.api.DmnDecisionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableCmmnRuntimeService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableCmmnRepositoryService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableDmnRepositoryService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableDmnRuleService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableRepositoryService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableRuntimeService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableTaskService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableHistoryService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableManagementService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableIdentityService;

import java.util.concurrent.Executor;

/**
 * Flowable 7.2.0 工作流引擎配置
 * <p>
 * 提供企业级工作流引擎的核心配置
 * 支持BPMN流程引擎、CMMN案例管理、DMN决策表
 * 注意：Flowable 7.x已移除App模块和异步历史功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 - Flowable 7.2.0升级版
 * @since 2025-01-16
 */
@Slf4j
@Configuration
@EnableAsync
public class FlowableEngineConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FlowableEngineConfiguration.class);

    // ==================== BPMN 流程引擎服务 ====================

    /**
     * 流程定义服务Bean
     * 提供流程部署、定义管理等功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableRepositoryService flowableRepositoryService(RepositoryService repositoryService) {
        log.info("[Flowable 7.2配置] 初始化RepositoryService - 流程定义服务");
        return new FlowableRepositoryService(repositoryService);
    }

    /**
     * 流程运行时服务Bean
     * 提供流程启动、执行、信号等功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableRuntimeService flowableRuntimeService(RuntimeService runtimeService) {
        log.info("[Flowable 7.2配置] 初始化RuntimeService - 流程运行时服务");
        return new FlowableRuntimeService(runtimeService);
    }

    /**
     * 任务服务Bean
     * 提供任务查询、处理、委派等功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableTaskService flowableTaskService(TaskService taskService) {
        log.info("[Flowable 7.2配置] 初始化TaskService - 任务服务");
        return new FlowableTaskService(taskService);
    }

    /**
     * 历史服务Bean
     * 提供历史数据查询、统计等功能
     * 注意：Flowable 7.x改为同步历史处理，不再支持异步历史
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableHistoryService flowableHistoryService(HistoryService historyService) {
        log.info("[Flowable 7.2配置] 初始化HistoryService - 历史服务(同步模式)");
        return new FlowableHistoryService(historyService);
    }

    /**
     * 管理服务Bean
     * 提供引擎管理、性能监控等功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableManagementService flowableManagementService(ManagementService managementService) {
        log.info("[Flowable 7.2配置] 初始化ManagementService - 管理服务");
        return new FlowableManagementService(managementService);
    }

    /**
     * 身份服务Bean
     * 提供用户、组管理等功能
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowableIdentityService flowableIdentityService(IdentityService identityService) {
        log.info("[Flowable 7.2配置] 初始化IdentityService - 身份服务");
        return new FlowableIdentityService(identityService);
    }

    // ==================== CMMN 案例管理服务 ====================

    /**
     * CMMN案例运行时服务
     * 提供案例实例管理功能
     */
    @Bean
    @ConditionalOnBean(CmmnRuntimeService.class)
    public FlowableCmmnRuntimeService flowableCmmnRuntimeService(CmmnRuntimeService cmmnRuntimeService) {
        log.info("[Flowable 7.2配置] 初始化CmmnRuntimeService - CMMN运行时服务");
        return new FlowableCmmnRuntimeService(cmmnRuntimeService);
    }

    /**
     * CMMN案例定义服务
     * 提供案例定义部署和管理功能
     */
    @Bean
    @ConditionalOnBean(CmmnRepositoryService.class)
    public FlowableCmmnRepositoryService flowableCmmnRepositoryService(CmmnRepositoryService cmmnRepositoryService) {
        log.info("[Flowable 7.2配置] 初始化CmmnRepositoryService - CMMN定义服务");
        return new FlowableCmmnRepositoryService(cmmnRepositoryService);
    }

    // ==================== DMN 决策表服务 ====================

    /**
     * DMN决策表定义服务
     * 提供决策表部署和管理功能
     */
    @Bean
    @ConditionalOnBean(DmnRepositoryService.class)
    public FlowableDmnRepositoryService flowableDmnRepositoryService(DmnRepositoryService dmnRepositoryService) {
        log.info("[Flowable 7.2配置] 初始化DmnRepositoryService - DMN定义服务");
        return new FlowableDmnRepositoryService(dmnRepositoryService);
    }

    /**
     * DMN规则执行服务
     * 提供决策表执行功能
     */
    @Bean
    @ConditionalOnBean(DmnDecisionService.class)
    public FlowableDmnRuleService flowableDmnRuleService(DmnDecisionService dmnDecisionService) {
        log.info("[Flowable 7.2配置] 初始化DmnDecisionService - DMN决策服务");
        return new FlowableDmnRuleService(dmnDecisionService);
    }

    // ==================== 异步执行器配置 ====================

    /**
     * 异步任务执行器
     * 用于异步处理工作流任务，提高系统性能
     */
    @Bean(name = "flowableTaskExecutor")
    public Executor flowableTaskExecutor() {
        log.info("[Flowable 7.2配置] 初始化异步任务执行器");
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
        log.info("[Flowable 7.2配置] 初始化事件监听器");
        return new FlowableEventLogger();
    }

    /**
     * 异步执行器Bean
     * 为Flowable提供异步处理能力
     */
    @Bean
    public AsyncTaskExecutor flowableAsyncTaskExecutor() {
        log.info("[Flowable 7.2配置] 初始化异步执行器");
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
        log.info("[Flowable 7.2配置] 初始化配置后处理器");
        return new FlowableInitializationProcessor();
    }
}
