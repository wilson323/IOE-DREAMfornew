package net.lab1024.sa.oa.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableEngineEntityEvent;
import org.flowable.engine.delegate.event.FlowableEngineEventType;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import net.lab1024.sa.oa.workflow.service.WorkflowEngineService;
import net.lab1024.sa.oa.workflow.service.ApprovalService;
import net.lab1024.sa.oa.workflow.websocket.WorkflowWebSocketController;
import net.lab1024.sa.oa.workflow.dao.WorkflowInstanceDao;
import net.lab1024.sa.oa.workflow.entity.WorkflowInstanceEntity;
import net.lab1024.sa.oa.workflow.notification.WorkflowNotificationService;

import java.util.Map;
import java.util.HashMap;

/**
 * Flowable流程事件监听器
 * <p>
 * 处理流程实例的生命周期事件，包括流程启动、完成、挂起、激活等
 * 提供实时流程状态监控和业务集成
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Component
public class FlowableProcessEventListener extends AbstractFlowableEngineEventListener {

    @Resource
    private WorkflowEngineService workflowEngineService;

    @Resource
    private ApprovalService approvalService;

    @Resource
    private WorkflowWebSocketController webSocketController;

    @Resource
    private WorkflowInstanceDao workflowInstanceDao;

    @Resource
    private WorkflowNotificationService workflowNotificationService;

    /**
     * 处理流程事件
     */
    @Override
    protected void entityEvent(FlowableEngineEntityEvent event) {
        try {
            FlowableEngineEventType eventType = event.getType();
            String eventTypeStr = eventType.name();

            log.debug("[Flowable事件] 收到流程事件: {}, 流程实例ID: {}",
                eventTypeStr, event.getProcessInstanceId());

            switch (eventType) {
                case PROCESS_STARTED:
                    handleProcessStarted(event);
                    break;
                case PROCESS_COMPLETED:
                    handleProcessCompleted(event);
                    break;
                case PROCESS_CANCELLED:
                    handleProcessCancelled(event);
                    break;
                case PROCESS_SUSPENDED:
                    handleProcessSuspended(event);
                    break;
                case PROCESS_RESUMED:
                    handleProcessResumed(event);
                    break;
                default:
                    // 其他事件暂不处理
                    break;
            }
        } catch (Exception e) {
            log.error("[Flowable事件] 处理流程事件失败: event={}, error={}",
                event.getType(), e.getMessage(), e);
        }
    }

    /**
     * 处理流程启动事件
     */
    private void handleProcessStarted(FlowableEngineEntityEvent event) {
        String processInstanceId = event.getProcessInstanceId();
        String processDefinitionId = event.getProcessDefinitionId();

        log.info("[Flowable事件] 流程启动: 流程实例ID={}, 流程定义ID={}",
            processInstanceId, processDefinitionId);

        try {
            // 更新本地流程实例状态
            updateLocalInstanceStatus(processInstanceId, 1, "RUNNING");

            // 发送流程启动通知
            Map<String, Object> processStartData = createProcessNotificationData(
                processInstanceId, processDefinitionId, "PROCESS_STARTED", "RUNNING"
            );
            workflowNotificationService.sendProcessStatusChangeNotification(processStartData);

            log.info("[Flowable事件] 流程启动事件处理完成: {}", processInstanceId);

        } catch (Exception e) {
            log.error("[Flowable事件] 处理流程启动事件失败: processInstanceId={}, error={}",
                processInstanceId, e.getMessage(), e);
        }
    }

    /**
     * 处理流程完成事件
     */
    private void handleProcessCompleted(FlowableEngineEntityEvent event) {
        String processInstanceId = event.getProcessInstanceId();
        String processDefinitionId = event.getProcessDefinitionId();

        log.info("[Flowable事件] 流程完成: 流程实例ID={}, 流程定义ID={}",
            processInstanceId, processDefinitionId);

        try {
            // 更新本地流程实例状态
            updateLocalInstanceStatus(processInstanceId, 2, "COMPLETED");

            // 处理流程完成的业务逻辑
            approvalService.handleProcessCompleted(processInstanceId, processDefinitionId);

            // 发送流程完成通知
            Map<String, Object> processCompleteData = createProcessNotificationData(
                processInstanceId, processDefinitionId, "PROCESS_COMPLETED", "COMPLETED"
            );
            workflowNotificationService.sendProcessStatusChangeNotification(processCompleteData);

            log.info("[Flowable事件] 流程完成事件处理完成: {}", processInstanceId);

        } catch (Exception e) {
            log.error("[Flowable事件] 处理流程完成事件失败: processInstanceId={}, error={}",
                processInstanceId, e.getMessage(), e);
        }
    }

    /**
     * 处理流程取消事件
     */
    private void handleProcessCancelled(FlowableEngineEntityEvent event) {
        String processInstanceId = event.getProcessInstanceId();
        String processDefinitionId = event.getProcessDefinitionId();

        log.info("[Flowable事件] 流程取消: 流程实例ID={}, 流程定义ID={}",
            processInstanceId, processDefinitionId);

        try {
            // 更新本地流程实例状态
            updateLocalInstanceStatus(processInstanceId, 3, "CANCELLED");

            // 发送流程取消通知
            Map<String, Object> processCancelData = createProcessNotificationData(
                processInstanceId, processDefinitionId, "PROCESS_CANCELLED", "CANCELLED"
            );
            workflowNotificationService.sendProcessStatusChangeNotification(processCancelData);

            log.info("[Flowable事件] 流程取消事件处理完成: {}", processInstanceId);

        } catch (Exception e) {
            log.error("[Flowable事件] 处理流程取消事件失败: processInstanceId={}, error={}",
                processInstanceId, e.getMessage(), e);
        }
    }

    /**
     * 处理流程挂起事件
     */
    private void handleProcessSuspended(FlowableEngineEntityEvent event) {
        String processInstanceId = event.getProcessInstanceId();
        String processDefinitionId = event.getProcessDefinitionId();

        log.info("[Flowable事件] 流程挂起: 流程实例ID={}, 流程定义ID={}",
            processInstanceId, processDefinitionId);

        try {
            // 更新本地流程实例状态
            updateLocalInstanceStatus(processInstanceId, 4, "SUSPENDED");

            // 发送流程挂起通知
            Map<String, Object> processSuspendData = createProcessNotificationData(
                processInstanceId, processDefinitionId, "PROCESS_SUSPENDED", "SUSPENDED"
            );
            workflowNotificationService.sendProcessStatusChangeNotification(processSuspendData);

            log.info("[Flowable事件] 流程挂起事件处理完成: {}", processInstanceId);

        } catch (Exception e) {
            log.error("[Flowable事件] 处理流程挂起事件失败: processInstanceId={}, error={}",
                processInstanceId, e.getMessage(), e);
        }
    }

    /**
     * 处理流程恢复事件
     */
    private void handleProcessResumed(FlowableEngineEntityEvent event) {
        String processInstanceId = event.getProcessInstanceId();
        String processDefinitionId = event.getProcessDefinitionId();

        log.info("[Flowable事件] 流程恢复: 流程实例ID={}, 流程定义ID={}",
            processInstanceId, processDefinitionId);

        try {
            // 更新本地流程实例状态
            updateLocalInstanceStatus(processInstanceId, 1, "RUNNING");

            // 发送流程恢复通知
            Map<String, Object> processResumeData = createProcessNotificationData(
                processInstanceId, processDefinitionId, "PROCESS_RESUMED", "RUNNING"
            );
            workflowNotificationService.sendProcessStatusChangeNotification(processResumeData);

            log.info("[Flowable事件] 流程恢复事件处理完成: {}", processInstanceId);

        } catch (Exception e) {
            log.error("[Flowable事件] 处理流程恢复事件失败: processInstanceId={}, error={}",
                processInstanceId, e.getMessage(), e);
        }
    }

    /**
     * 更新本地流程实例状态
     *
     * @param flowableProcessInstanceId Flowable流程实例ID
     * @param status 状态
     * @param statusName 状态名称
     */
    private void updateLocalInstanceStatus(String flowableProcessInstanceId, Integer status, String statusName) {
        try {
            // 通过Flowable实例ID查找本地流程实例
            WorkflowInstanceEntity instance = workflowInstanceDao.selectByFlowableInstanceId(flowableProcessInstanceId);
            if (instance != null) {
                instance.setStatus(status);
                // 可以在这里更新其他状态相关信息
                // instance.setStatusName(statusName);
                workflowInstanceDao.updateById(instance);
                log.debug("[Flowable事件] 本地流程实例状态已更新: ID={}, 状态={}",
                    instance.getId(), statusName);
            } else {
                log.warn("[Flowable事件] 未找到对应的本地流程实例: Flowable实例ID={}", flowableProcessInstanceId);
            }
        } catch (Exception e) {
            log.error("[Flowable事件] 更新本地流程实例状态失败: flowableProcessInstanceId={}, status={}, error={}",
                flowableProcessInstanceId, status, e.getMessage(), e);
        }
    }

    /**
     * 判断是否支持指定的事件类型
     */
    @Override
    public boolean isFailOnException() {
        return false; // 事件处理失败不影响主流程
    }

    @Override
    protected boolean isValidEvent(FlowableEngineEntityEvent event) {
        // 只处理流程相关事件
        FlowableEngineEventType eventType = event.getType();
        return eventType == FlowableEngineEventType.PROCESS_STARTED ||
               eventType == FlowableEngineEventType.PROCESS_COMPLETED ||
               eventType == FlowableEngineEventType.PROCESS_CANCELLED ||
               eventType == FlowableEngineEventType.PROCESS_SUSPENDED ||
               eventType == FlowableEngineEventType.PROCESS_RESUMED;
    }

    /**
     * 创建流程通知数据
     *
     * @param processInstanceId 流程实例ID
     * @param processDefinitionId 流程定义ID
     * @param eventType 事件类型
     * @param status 状态
     * @return 通知数据
     */
    private Map<String, Object> createProcessNotificationData(String processInstanceId,
                                                               String processDefinitionId,
                                                               String eventType,
                                                               String status) {
        Map<String, Object> processData = new HashMap<>();
        processData.put("eventType", eventType);
        processData.put("processInstanceId", processInstanceId);
        processData.put("processDefinitionId", processDefinitionId);
        processData.put("status", status);
        processData.put("timestamp", System.currentTimeMillis());

        // 添加状态描述
        processData.put("statusDescription", getProcessStatusDescription(status));

        // 尝试获取更多流程信息
        try {
            WorkflowInstanceEntity instance = workflowInstanceDao.selectByFlowableInstanceId(processInstanceId);
            if (instance != null) {
                processData.put("instanceId", instance.getId());
                processData.put("processName", instance.getProcessName());
                processData.put("businessKey", instance.getBusinessKey());
                processData.put("startUserId", instance.getStartUserId());
                processData.put("startTime", instance.getStartTime());
            }
        } catch (Exception e) {
            log.debug("[Flowable事件] 获取流程实例信息失败: {}", e.getMessage());
        }

        return processData;
    }

    /**
     * 获取流程状态描述
     */
    private String getProcessStatusDescription(String status) {
        switch (status) {
            case "RUNNING": return "运行中";
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            case "SUSPENDED": return "已挂起";
            default: return "未知状态";
        }
    }
}