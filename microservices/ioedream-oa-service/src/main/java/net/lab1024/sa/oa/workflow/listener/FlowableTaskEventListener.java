package net.lab1024.sa.oa.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableEngineEntityEvent;
import org.flowable.engine.delegate.event.FlowableEngineEventType;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import net.lab1024.sa.oa.workflow.dao.WorkflowTaskDao;
import net.lab1024.sa.oa.workflow.entity.WorkflowTaskEntity;
import net.lab1024.sa.oa.workflow.websocket.WorkflowWebSocketController;
import net.lab1024.sa.oa.workflow.service.ApprovalService;
import net.lab1024.sa.oa.workflow.notification.WorkflowNotificationService;
import org.flowable.task.api.Task;

import java.util.Map;
import java.util.HashMap;

/**
 * Flowable任务事件监听器
 * <p>
 * 处理任务的生命周期事件，包括任务创建、分配、完成、删除等
 * 提供实时任务状态监控和业务集成
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Component
public class FlowableTaskEventListener extends AbstractFlowableEngineEventListener {

    @Resource
    private WorkflowTaskDao workflowTaskDao;

    @Resource
    private WorkflowWebSocketController webSocketController;

    @Resource
    private ApprovalService approvalService;

    @Resource
    private WorkflowNotificationService workflowNotificationService;

    /**
     * 处理任务事件
     */
    @Override
    protected void entityEvent(FlowableEngineEntityEvent event) {
        try {
            FlowableEngineEventType eventType = event.getType();
            String eventTypeStr = eventType.name();

            log.debug("[Flowable任务事件] 收到任务事件: {}, 任务ID: {}, 流程实例ID: {}",
                eventTypeStr, event.getExecutionId(), event.getProcessInstanceId());

            switch (eventType) {
                case TASK_CREATED:
                    handleTaskCreated(event);
                    break;
                case TASK_ASSIGNED:
                    handleTaskAssigned(event);
                    break;
                case TASK_COMPLETED:
                    handleTaskCompleted(event);
                    break;
                case TASK_SUSPENDED:
                    handleTaskSuspended(event);
                    break;
                case TASK_RESUMED:
                    handleTaskResumed(event);
                    break;
                case TASK_DELETED:
                    handleTaskDeleted(event);
                    break;
                default:
                    // 其他任务事件暂不处理
                    break;
            }
        } catch (Exception e) {
            log.error("[Flowable任务事件] 处理任务事件失败: event={}, error={}",
                event.getType(), e.getMessage(), e);
        }
    }

    /**
     * 处理任务创建事件
     */
    private void handleTaskCreated(FlowableEngineEntityEvent event) {
        String taskId = event.getExecutionId();
        String processInstanceId = event.getProcessInstanceId();

        log.info("[Flowable任务事件] 任务创建: 任务ID={}, 流程实例ID={}",
            taskId, processInstanceId);

        try {
            // 创建本地任务记录
            WorkflowTaskEntity task = new WorkflowTaskEntity();
            task.setFlowableTaskId(taskId);
            task.setFlowableProcessInstanceId(processInstanceId);
            task.setTaskName(getTaskName(event));
            task.setTaskDefinitionKey(getTaskDefinitionKey(event));

            // 获取任务的其他属性
            if (event.getEntity() instanceof Task) {
                Task flowableTask = (Task) event.getEntity();
                task.setTaskName(flowableTask.getName());
                task.setTaskDefinitionKey(flowableTask.getTaskDefinitionKey());
                task.setAssigneeId(getAssigneeId(flowableTask));
                task.setCreateTime(flowableTask.getCreateTime());
                task.setPriority(flowableTask.getPriority());
            }

            task.setStatus(1); // 1-待处理
            task.setOutcome(0); // 0-未处理
            workflowTaskDao.insert(task);

            // 处理任务创建的业务逻辑
            approvalService.handleTaskCreated(taskId, processInstanceId);

            // 发送新任务通知
            if (task.getAssigneeId() != null) {
                workflowNotificationService.sendNewTaskNotification(
                    task.getAssigneeId(),
                    createTaskNotificationData(task, "TASK_CREATED")
                );
            }

            log.info("[Flowable任务事件] 任务创建事件处理完成: {}", taskId);

        } catch (Exception e) {
            log.error("[Flowable任务事件] 处理任务创建事件失败: taskId={}, error={}",
                taskId, e.getMessage(), e);
        }
    }

    /**
     * 处理任务分配事件
     */
    private void handleTaskAssigned(FlowableEngineEntityEvent event) {
        String taskId = event.getExecutionId();

        log.info("[Flowable任务事件] 任务分配: 任务ID={}", taskId);

        try {
            // 更新本地任务记录
            WorkflowTaskEntity task = workflowTaskDao.selectByFlowableTaskId(taskId);
            if (task != null) {
                if (event.getEntity() instanceof Task) {
                    Task flowableTask = (Task) event.getEntity();
                    task.setAssigneeId(getAssigneeId(flowableTask));
                    task.setAssignTime(flowableTask.getAssignee() != null ?
                        java.time.LocalDateTime.now() : null);
                }

                workflowTaskDao.updateById(task);

                // 处理任务分配的业务逻辑
                approvalService.handleTaskAssigned(taskId, task.getAssigneeId());

                // 发送任务分配通知
                if (task.getAssigneeId() != null) {
                    workflowNotificationService.sendTaskAssignmentNotification(
                        task.getAssigneeId(),
                        createTaskNotificationData(task, "TASK_ASSIGNED")
                    );
                }

                log.info("[Flowable任务事件] 任务分配事件处理完成: {}", taskId);
            }

        } catch (Exception e) {
            log.error("[Flowable任务事件] 处理任务分配事件失败: taskId={}, error={}",
                taskId, e.getMessage(), e);
        }
    }

    /**
     * 处理任务完成事件
     */
    private void handleTaskCompleted(FlowableEngineEntityEvent event) {
        String taskId = event.getExecutionId();

        log.info("[Flowable任务事件] 任务完成: 任务ID={}", taskId);

        try {
            // 更新本地任务记录
            WorkflowTaskEntity task = workflowTaskDao.selectByFlowableTaskId(taskId);
            if (task != null) {
                task.setStatus(2); // 2-已完成
                task.setCompleteTime(java.time.LocalDateTime.now());

                // 处理任务完成的业务逻辑
                approvalService.handleTaskCompleted(taskId, task.getOutcome(), task.getComment());

                // 发送任务完成通知
                workflowNotificationService.sendTaskCompletionNotification(
                    createTaskNotificationData(task, "TASK_COMPLETED")
                );

                log.info("[Flowable任务事件] 任务完成事件处理完成: {}", taskId);
            }

        } catch (Exception e) {
            log.error("[Flowable任务事件] 处理任务完成事件失败: taskId={}, error={}",
                taskId, e.getMessage(), e);
        }
    }

    /**
     * 处理任务挂起事件
     */
    private void handleTaskSuspended(FlowableEngineEntityEvent event) {
        String taskId = event.getExecutionId();

        log.info("[Flowable任务事件] 任务挂起: 任务ID={}", taskId);

        try {
            // 更新本地任务记录
            WorkflowTaskEntity task = workflowTaskDao.selectByFlowableTaskId(taskId);
            if (task != null) {
                task.setStatus(3); // 3-挂起

                // 发送任务状态变更通知（挂起）
                workflowNotificationService.sendTaskStatusChangeNotification(
                    createTaskNotificationData(task, "TASK_SUSPENDED")
                );

                log.info("[Flowable任务事件] 任务挂起事件处理完成: {}", taskId);
            }

        } catch (Exception e) {
            log.error("[Flowable任务事件] 处理任务挂起事件失败: taskId={}, error={}",
                taskId, e.getMessage(), e);
        }
    }

    /**
     * 处理任务恢复事件
     */
    private void handleTaskResumed(FlowableEngineEntityEvent event) {
        String taskId = event.getExecutionId();

        log.info("[Flowable任务事件] 任务恢复: 任务ID={}", taskId);

        try {
            // 更新本地任务记录
            WorkflowTaskEntity task = workflowTaskDao.selectByFlowableTaskId(taskId);
            if (task != null) {
                task.setStatus(1); // 1-待处理

                // 发送任务状态变更通知（恢复）
                workflowNotificationService.sendTaskStatusChangeNotification(
                    createTaskNotificationData(task, "TASK_RESUMED")
                );

                log.info("[Flowable任务事件] 任务恢复事件处理完成: {}", taskId);
            }

        } catch (Exception e) {
            log.error("[Flowable任务事件] 处理任务恢复事件失败: taskId={}, error={}",
                taskId, e.getMessage(), e);
        }
    }

    /**
     * 处理任务删除事件
     */
    private void handleTaskDeleted(FlowableEngineEntityEvent event) {
        String taskId = event.getExecutionId();

        log.info("[Flowable任务事件] 任务删除: 任务ID={}", taskId);

        try {
            // 更新本地任务记录
            WorkflowTaskEntity task = workflowTaskDao.selectByFlowableTaskId(taskId);
            if (task != null) {
                task.setStatus(4); // 4-已删除

                // 发送任务状态变更通知（删除）
                workflowNotificationService.sendTaskStatusChangeNotification(
                    createTaskNotificationData(task, "TASK_DELETED")
                );

                log.info("[Flowable任务事件] 任务删除事件处理完成: {}", taskId);
            }

        } catch (Exception e) {
            log.error("[Flowable任务事件] 处理任务删除事件失败: taskId={}, error={}",
                taskId, e.getMessage(), e);
        }
    }

    /**
     * 创建任务通知数据
     *
     * @param task 任务对象
     * @param eventType 事件类型
     * @return 通知数据
     */
    private Map<String, Object> createTaskNotificationData(WorkflowTaskEntity task, String eventType) {
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("eventType", eventType);
        taskData.put("taskId", task.getId());
        taskData.put("flowableTaskId", task.getFlowableTaskId());
        taskData.put("taskName", task.getTaskName());
        taskData.put("taskDefinitionKey", task.getTaskDefinitionKey());
        taskData.put("assigneeId", task.getAssigneeId());
        taskData.put("status", task.getStatus());
        taskData.put("outcome", task.getOutcome());
        taskData.put("priority", task.getPriority());
        taskData.put("dueDate", task.getDueDate());
        taskData.put("createTime", task.getCreateTime());
        taskData.put("assignTime", task.getAssignTime());
        taskData.put("completeTime", task.getCompleteTime());
        taskData.put("processInstanceId", task.getInstanceId());
        taskData.put("flowableProcessInstanceId", task.getFlowableProcessInstanceId());
        taskData.put("timestamp", System.currentTimeMillis());

        // 添加状态描述
        taskData.put("statusDescription", getStatusDescription(task.getStatus()));

        return taskData;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDescription(Integer status) {
        switch (status) {
            case 1: return "待处理";
            case 2: return "已完成";
            case 3: return "已挂起";
            case 4: return "已删除";
            default: return "未知状态";
        }
    }

    /**
     * 获取任务名称
     */
    private String getTaskName(FlowableEngineEntityEvent event) {
        if (event.getEntity() instanceof Task) {
            return ((Task) event.getEntity()).getName();
        }
        return null;
    }

    /**
     * 获取任务定义键
     */
    private String getTaskDefinitionKey(FlowableEngineEntityEvent event) {
        if (event.getEntity() instanceof Task) {
            return ((Task) event.getEntity()).getTaskDefinitionKey();
        }
        return null;
    }

    /**
     * 获取分配人ID
     */
    private Long getAssigneeId(Task task) {
        if (task.getAssignee() != null) {
            try {
                return Long.parseLong(task.getAssignee());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
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
        // 只处理任务相关事件
        FlowableEngineEventType eventType = event.getType();
        return eventType == FlowableEngineEventType.TASK_CREATED ||
               eventType == FlowableEngineEventType.TASK_ASSIGNED ||
               eventType == FlowableEngineEventType.TASK_COMPLETED ||
               eventType == FlowableEngineEventType.TASK_SUSPENDED ||
               eventType == FlowableEngineEventType.TASK_RESUMED ||
               eventType == FlowableEngineEventType.TASK_DELETED;
    }
}