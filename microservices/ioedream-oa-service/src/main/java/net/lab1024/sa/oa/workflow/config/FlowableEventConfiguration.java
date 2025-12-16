package net.lab1024.sa.oa.workflow.config;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.FlowableEngineEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import jakarta.annotation.Resource;
import net.lab1024.sa.oa.workflow.listener.FlowableExecutionListener;
import net.lab1024.sa.oa.workflow.listener.FlowableProcessEventListener;
import net.lab1024.sa.oa.workflow.listener.FlowableTaskEventListener;

/**
 * Flowable事件监听器配置
 * <p>
 * 配置Flowable工作流引擎的事件监听器，用于处理流程生命周期事件
 * 实现Flowable事件与本地数据库的实时同步
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Configuration
@Order(1) // 优先级高于其他配置
public class FlowableEventConfiguration {

    @Resource
    private FlowableProcessEventListener flowableProcessEventListener;

    @Resource
    private FlowableTaskEventListener flowableTaskEventListener;

    @Resource
    private FlowableExecutionListener flowableExecutionListener;

    /**
     * 配置流程事件监听器
     * 监听流程的启动、完成、取消、挂起、恢复等事件
     */
    @Bean("flowableProcessEventListener")
    public FlowableEngineEventListener processEventListener() {
        return flowableProcessEventListener;
    }

    /**
     * 配置任务事件监听器
     * 监听任务的创建、分配、完成、挂起、恢复、删除等事件
     */
    @Bean("flowableTaskEventListener")
    public FlowableEngineEventListener taskEventListener() {
        return flowableTaskEventListener;
    }

    /**
     * 配置执行事件监听器
     * 监听流程执行过程中的节点事件
     */
    @Bean("flowableExecutionListener")
    public FlowableExecutionListener executionListener() {
        return flowableExecutionListener;
    }

    /**
     * 获取需要监听的流程事件类型
     */
    public static FlowableEngineEventType[] getProcessEventTypes() {
        return new FlowableEngineEventType[]{
            // 流程实例事件
            FlowableEngineEventType.PROCESS_STARTED,
            FlowableEngineEventType.PROCESS_COMPLETED,
            FlowableEngineEventType.PROCESS_CANCELLED,
            FlowableEngineEventType.PROCESS_SUSPENDED,
            FlowableEngineEventType.PROCESS_RESUMED
        };
    }

    /**
     * 获取需要监听的任务事件类型
     */
    public static FlowableEngineEventType[] getTaskEventTypes() {
        return new FlowableEngineEventType[]{
            // 任务事件
            FlowableEngineEventType.TASK_CREATED,
            FlowableEngineEventType.TASK_ASSIGNED,
            FlowableEngineEventType.TASK_COMPLETED,
            FlowableEngineEventType.TASK_SUSPENDED,
            FlowableEngineEventType.TASK_RESUMED,
            FlowableEngineEventType.TASK_DELETED
        };
    }

    /**
     * 获取需要监听的执行事件类型
     */
    public static FlowableEngineEventType[] getExecutionEventTypes() {
        return new FlowableEngineEventType[]{
            // 执行事件
            FlowableEngineEventType.ACTIVITY_STARTED,
            FlowableEngineEventType.ACTIVITY_COMPLETED,
            FlowableEngineEventType.ACTIVITY_CANCELLED,
            FlowableEngineEventType.GATEWAY_ACTIVATED,
            FlowableEngineEventType.SEQUENCEFLOW_TAKEN
        };
    }
}