package net.lab1024.sa.oa.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import net.lab1024.sa.oa.workflow.service.ApprovalService;
import net.lab1024.sa.oa.workflow.dao.WorkflowInstanceDao;
import net.lab1024.sa.oa.workflow.entity.WorkflowInstanceEntity;
import net.lab1024.sa.oa.workflow.notification.WorkflowNotificationService;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * Flowable执行监听器
 * <p>
 * 处理流程执行过程中的自定义逻辑，包括开始节点、结束节点、网关等
 * 提供业务规则验证和流程变量管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Component("flowableExecutionListener")
public class FlowableExecutionListener implements ExecutionListener {

    @Resource
    private ApprovalService approvalService;

    @Resource
    private WorkflowInstanceDao workflowInstanceDao;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private WorkflowNotificationService workflowNotificationService;

    /**
     * 流程开始执行
     */
    @Override
    public void notify(DelegateExecution execution) {
        String eventName = execution.getCurrentActivityId();
        String processInstanceId = execution.getProcessInstanceId();
        String processDefinitionKey = execution.getProcessDefinitionKey();

        log.info("[Flowable执行监听] 流程开始: 节点={}, 流程实例ID={}, 流程定义Key={}",
            eventName, processInstanceId, processDefinitionKey);

        try {
            // 处理开始事件
            if ("start".equals(eventName)) {
                handleStartEvent(execution);
            }
        } catch (Exception e) {
            log.error("[Flowable执行监听] 处理流程开始事件失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 处理开始事件
     */
    private void handleStartEvent(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = execution.getVariables();

        try {
            // 添加执行时间戳
            variables.put("executionStartTime", LocalDateTime.now());
            variables.put("startNodeId", execution.getCurrentActivityId());

            // 更新本地流程实例的执行信息
            updateLocalInstanceExecutionInfo(processInstanceId, variables);

            // 处理开始节点的业务逻辑
            approvalService.handleStartNode(processInstanceId, variables);

            log.debug("[Flowable执行监听] 开始事件处理完成: 流程实例ID={}", processInstanceId);

        } catch (Exception e) {
            log.error("[Flowable执行监听] 处理开始事件失败: processInstanceId={}, error={}",
                processInstanceId, e.getMessage(), e);
        }
    }

    /**
     * 更新本地流程实例的执行信息
     *
     * @param flowableProcessInstanceId Flowable流程实例ID
     * @param variables 流程变量
     */
    private void updateLocalInstanceExecutionInfo(String flowableProcessInstanceId, Map<String, Object> variables) {
        try {
            WorkflowInstanceEntity instance = workflowInstanceDao.selectByFlowableInstanceId(flowableProcessInstanceId);
            if (instance != null) {
                // 更新流程变量
                if (variables != null && !variables.isEmpty()) {
                    String variablesJson = objectMapper.writeValueAsString(variables);
                    instance.setVariables(variablesJson);
                }

                // 可以添加其他执行相关的字段
                // instance.setLastActivityId(execution.getCurrentActivityId());

                workflowInstanceDao.updateById(instance);
                log.debug("[Flowable执行监听] 本地流程实例执行信息已更新: ID={}", instance.getId());
            } else {
                log.warn("[Flowable执行监听] 未找到对应的本地流程实例: Flowable实例ID={}", flowableProcessInstanceId);
            }
        } catch (Exception e) {
            log.error("[Flowable执行监听] 更新本地流程实例执行信息失败: flowableProcessInstanceId={}, error={}",
                flowableProcessInstanceId, e.getMessage(), e);
        }
    }
}

/**
 * 流程执行委托类
 * <p>
 * 用于处理特定的流程执行节点业务逻辑
 * </p>
 */
@Slf4j
@Component("flowableExecutionDelegate")
public class FlowableExecutionDelegate implements JavaDelegate {

    @Resource
    private ApprovalService approvalService;

    /**
     * 执行委托任务
     */
    @Override
    public void execute(DelegateExecution execution) {
        String nodeName = execution.getCurrentActivityId();
        String processInstanceId = execution.getProcessInstanceId();

        log.info("[Flowable执行委托] 执行节点: {}, 流程实例ID={}", nodeName, processInstanceId);

        try {
            // 根据节点名称处理不同的业务逻辑
            switch (nodeName) {
                case "autoApproveNode":
                    handleAutoApprove(execution);
                    break;
                case "sendNotificationNode":
                    handleSendNotification(execution);
                    break;
                case "validateBusinessRuleNode":
                    handleValidateBusinessRule(execution);
                    break;
                case "updateProcessDataNode":
                    handleUpdateProcessData(execution);
                    break;
                default:
                    log.debug("[Flowable执行委托] 未知节点: {}", nodeName);
                    break;
            }

        } catch (Exception e) {
            log.error("[Flowable执行委托] 执行节点失败: 节点={}, 流程实例ID={}, error={}",
                nodeName, processInstanceId, e.getMessage(), e);
        }
    }

    /**
     * 处理自动审批节点
     */
    private void handleAutoApprove(DelegateExecution execution) {
        log.info("[Flowable执行委托] 处理自动审批节点");
        // 自动审批逻辑
        execution.setVariable("autoApproved", true);
        execution.setVariable("autoApproveTime", LocalDateTime.now());
    }

    /**
     * 处理发送通知节点
     */
    private void handleSendNotification(DelegateExecution execution) {
        log.info("[Flowable执行委托] 处理发送通知节点");
        // 发送通知逻辑
        execution.setVariable("notificationSent", true);
        execution.setVariable("notificationTime", LocalDateTime.now());
    }

    /**
     * 处理业务规则验证节点
     */
    private void handleValidateBusinessRule(DelegateExecution execution) {
        log.info("[Flowable执行委托] 处理业务规则验证节点");
        // 业务规则验证逻辑
        boolean isValid = approvalService.validateBusinessRule(execution.getVariables());
        execution.setVariable("businessRuleValid", isValid);

        if (!isValid) {
            execution.setVariable("businessRuleMessage", "业务规则验证失败");
        }
    }

    /**
     * 处理更新流程数据节点
     */
    private void handleUpdateProcessData(DelegateExecution execution) {
        log.info("[Flowable执行委托] 处理更新流程数据节点");
        // 更新流程数据逻辑
        execution.setVariable("processDataUpdated", true);
        execution.setVariable("updateTime", LocalDateTime.now());
    }
}