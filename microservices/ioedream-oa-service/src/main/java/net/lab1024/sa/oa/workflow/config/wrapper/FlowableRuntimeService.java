package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Flowable流程运行时服务包装器
 * <p>
 * 提供流程实例管理的完整功能封装
 * 包括流程启动、执行、查询、统计等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Service
public class FlowableRuntimeService {

    private final RuntimeService runtimeService;

    public FlowableRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
        log.info("[Flowable] RuntimeService包装器初始化完成");
    }

    /**
     * 启动流程实例（通过流程定义键）
     *
     * @param processDefinitionKey 流程定义键
     * @param businessKey         业务键
     * @param variables          流程变量
     * @param starterUserId       发起人ID
     * @return 流程实例ID
     */
    public String startProcessInstanceByKey(String processDefinitionKey,
                                           String businessKey,
                                           Map<String, Object> variables,
                                           String starterUserId) {
        log.info("[Flowable] 启动流程实例: processDefinitionKey={}, businessKey={}, starterUserId={}",
                processDefinitionKey, businessKey, starterUserId);

        try {
            // 设置发起人
            runtimeService.setAuthenticatedUserId(starterUserId);

            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                    processDefinitionKey,
                    businessKey,
                    variables
            );

            log.info("[Flowable] 流程实例启动成功: processInstanceId={}, processDefinitionId={}",
                    processInstance.getId(), processInstance.getProcessDefinitionId());

            return processInstance.getId();

        } catch (Exception e) {
            log.error("[Flowable] 启动流程实例失败: processDefinitionKey={}, businessKey={}, error={}",
                    processDefinitionKey, businessKey, e.getMessage(), e);
            throw new RuntimeException("启动流程实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 启动流程实例（通过流程定义ID）
     *
     * @param processDefinitionId 流程定义ID
     * @param businessKey         业务键
     * @param variables          流程变量
     * @param starterUserId       发起人ID
     * @return 流程实例ID
     */
    public String startProcessInstanceById(String processDefinitionId,
                                          String businessKey,
                                          Map<String, Object> variables,
                                          String starterUserId) {
        log.info("[Flowable] 启动流程实例: processDefinitionId={}, businessKey={}, starterUserId={}",
                processDefinitionId, businessKey, starterUserId);

        try {
            // 设置发起人
            runtimeService.setAuthenticatedUserId(starterUserId);

            ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                    processDefinitionId,
                    businessKey,
                    variables
            );

            log.info("[Flowable] 流程实例启动成功: processInstanceId={}, processDefinitionId={}",
                    processInstance.getId(), processInstance.getProcessDefinitionId());

            return processInstance.getId();

        } catch (Exception e) {
            log.error("[Flowable] 启动流程实例失败: processDefinitionId={}, businessKey={}, error={}",
                    processDefinitionId, businessKey, e.getMessage(), e);
            throw new RuntimeException("启动流程实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取流程实例
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例
     */
    public ProcessInstance getProcessInstance(String processInstanceId) {
        log.debug("[Flowable] 获取流程实例: processInstanceId={}", processInstanceId);

        try {
            return runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

        } catch (Exception e) {
            log.error("[Flowable] 获取流程实例失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取所有运行中的流程实例
     *
     * @return 流程实例列表
     */
    public List<ProcessInstance> getRunningProcessInstances() {
        log.debug("[Flowable] 获取所有运行中的流程实例");

        try {
            return runtimeService.createProcessInstanceQuery()
                    .active()
                    .orderByStartTime()
                    .desc()
                    .list();

        } catch (Exception e) {
            log.error("[Flowable] 获取运行中流程实例失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 分页查询流程实例
     *
     * @param pageNum           页码
     * @param pageSize          页大小
     * @param processDefinitionKey 流程定义键（可选）
     * @param businessKey       业务键（可选）
     * @param starterUserId     发起人ID（可选）
     * @return 流程实例列表
     */
    public List<ProcessInstance> getProcessInstancesPage(int pageNum, int pageSize,
                                                         String processDefinitionKey,
                                                         String businessKey,
                                                         String starterUserId) {
        log.debug("[Flowable] 分页查询流程实例: pageNum={}, pageSize={}, processDefinitionKey={}, businessKey={}, starterUserId={}",
                pageNum, pageSize, processDefinitionKey, businessKey, starterUserId);

        try {
            ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
                    .active();

            if (processDefinitionKey != null && !processDefinitionKey.trim().isEmpty()) {
                query = query.processDefinitionKey(processDefinitionKey);
            }

            if (businessKey != null && !businessKey.trim().isEmpty()) {
                query = query.processInstanceBusinessKey(businessKey);
            }

            if (starterUserId != null && !starterUserId.trim().isEmpty()) {
                query = query.startedBy(starterUserId);
            }

            return query.orderByStartTime()
                    .desc()
                    .listPage(pageNum * pageSize, pageSize);

        } catch (Exception e) {
            log.error("[Flowable] 分页查询流程实例失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取流程实例总数
     *
     * @param processDefinitionKey 流程定义键（可选）
     * @param businessKey       业务键（可选）
     * @param starterUserId     发起人ID（可选）
     * @return 总数
     */
    public long getProcessInstanceCount(String processDefinitionKey, String businessKey, String starterUserId) {
        log.debug("[Flowable] 获取流程实例总数: processDefinitionKey={}, businessKey={}, starterUserId={}",
                processDefinitionKey, businessKey, starterUserId);

        try {
            ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
                    .active();

            if (processDefinitionKey != null && !processDefinitionKey.trim().isEmpty()) {
                query = query.processDefinitionKey(processDefinitionKey);
            }

            if (businessKey != null && !businessKey.trim().isEmpty()) {
                query = query.processInstanceBusinessKey(businessKey);
            }

            if (starterUserId != null && !starterUserId.trim().isEmpty()) {
                query = query.startedBy(starterUserId);
            }

            return query.count();

        } catch (Exception e) {
            log.error("[Flowable] 获取流程实例总数失败: error={}", e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 删除流程实例
     *
     * @param processInstanceId 流程实例ID
     * @param deleteReason       删除原因
     */
    public void deleteProcessInstance(String processInstanceId, String deleteReason) {
        log.info("[Flowable] 删除流程实例: processInstanceId={}, deleteReason={}",
                processInstanceId, deleteReason);

        try {
            runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
            log.info("[Flowable] 流程实例删除成功: processInstanceId={}", processInstanceId);

        } catch (Exception e) {
            log.error("[Flowable] 删除流程实例失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            throw new RuntimeException("删除流程实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 挂起流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    public void suspendProcessInstance(String processInstanceId) {
        log.info("[Flowable] 挂起流程实例: processInstanceId={}", processInstanceId);

        try {
            runtimeService.suspendProcessInstanceById(processInstanceId);
            log.info("[Flowable] 流程实例挂起成功: processInstanceId={}", processInstanceId);

        } catch (Exception e) {
            log.error("[Flowable] 挂起流程实例失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            throw new RuntimeException("挂起流程实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 激活流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    public void activateProcessInstance(String processInstanceId) {
        log.info("[Flowable] 激活流程实例: processInstanceId={}", processInstanceId);

        try {
            runtimeService.activateProcessInstanceById(processInstanceId);
            log.info("[Flowable] 流程实例激活成功: processInstanceId={}", processInstanceId);

        } catch (Exception e) {
            log.error("[Flowable] 激活流程实例失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            throw new RuntimeException("激活流程实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送信号
     *
     * @param signalName 信号名称
     * @param variables  变量
     */
    public void signalEventReceived(String signalName, Map<String, Object> variables) {
        log.info("[Flowable] 发送信号: signalName={}", signalName);

        try {
            runtimeService.signalEventReceived(signalName, variables);
            log.info("[Flowable] 信号发送成功: signalName={}", signalName);

        } catch (Exception e) {
            log.error("[Flowable] 发送信号失败: signalName={}, error={}",
                    signalName, e.getMessage(), e);
            throw new RuntimeException("发送信号失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送信号给特定流程实例
     *
     * @param signalName        信号名称
     * @param processInstanceId 流程实例ID
     * @param variables         变量
     */
    public void signalEventReceived(String signalName, String processInstanceId, Map<String, Object> variables) {
        log.info("[Flowable] 发送信号给流程实例: signalName={}, processInstanceId={}",
                signalName, processInstanceId);

        try {
            runtimeService.signalEventReceived(signalName, processInstanceId, variables);
            log.info("[Flowable] 信号发送成功: signalName={}, processInstanceId={}",
                    signalName, processInstanceId);

        } catch (Exception e) {
            log.error("[Flowable] 发送信号失败: signalName={}, processInstanceId={}, error={}",
                    signalName, processInstanceId, e.getMessage(), e);
            throw new RuntimeException("发送信号失败: " + e.getMessage(), e);
        }
    }

    /**
     * 设置流程变量
     *
     * @param processInstanceId 流程实例ID
     * @param variables         变量
     */
    public void setVariables(String processInstanceId, Map<String, Object> variables) {
        log.debug("[Flowable] 设置流程变量: processInstanceId={}, variables={}",
                processInstanceId, variables != null ? variables.size() : 0);

        try {
            runtimeService.setVariables(processInstanceId, variables);
            log.debug("[Flowable] 流程变量设置成功: processInstanceId={}", processInstanceId);

        } catch (Exception e) {
            log.error("[Flowable] 设置流程变量失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            throw new RuntimeException("设置流程变量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取流程变量
     *
     * @param processInstanceId 流程实例ID
     * @param variableNames      变量名称
     * @return 变量Map
     */
    public Map<String, Object> getVariables(String processInstanceId, List<String> variableNames) {
        log.debug("[Flowable] 获取流程变量: processInstanceId={}, variableNames={}",
                processInstanceId, variableNames);

        try {
            return runtimeService.getVariables(processInstanceId, variableNames);

        } catch (Exception e) {
            log.error("[Flowable] 获取流程变量失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            return Map.of();
        }
    }
}