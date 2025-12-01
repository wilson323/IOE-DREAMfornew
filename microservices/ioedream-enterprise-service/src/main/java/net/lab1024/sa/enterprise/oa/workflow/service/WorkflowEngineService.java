package net.lab1024.sa.enterprise.oa.workflow.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.enterprise.oa.workflow.domain.entity.WorkflowDefinitionEntity;
import net.lab1024.sa.enterprise.oa.workflow.domain.entity.WorkflowInstanceEntity;
import net.lab1024.sa.enterprise.oa.workflow.domain.entity.WorkflowTaskEntity;

/**
 * 工作流引擎服务接口
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface WorkflowEngineService {

    // ==================== 流程定义管理 ====================

    /**
     * 部署流程定义
     */
    ResponseDTO<String> deployProcess(String bpmnXml, String processName, String processKey,
            String description, String category);

    /**
     * 分页查询流程定义
     */
    ResponseDTO<PageResult<WorkflowDefinitionEntity>> pageDefinitions(PageParam pageParam, String category,
            String status, String keyword);

    /**
     * 获取流程定义详情
     */
    ResponseDTO<WorkflowDefinitionEntity> getDefinition(Long definitionId);

    /**
     * 激活流程定义
     */
    ResponseDTO<String> activateDefinition(Long definitionId);

    /**
     * 禁用流程定义
     */
    ResponseDTO<String> disableDefinition(Long definitionId);

    /**
     * 删除流程定义
     */
    ResponseDTO<String> deleteDefinition(Long definitionId, Boolean cascade);

    // ==================== 流程实例管理 ====================

    /**
     * 启动流程实例
     */
    ResponseDTO<Long> startProcess(Long definitionId, String businessKey, String instanceName,
            Map<String, Object> variables, Map<String, Object> formData);

    /**
     * 分页查询流程实例
     */
    ResponseDTO<PageResult<WorkflowInstanceEntity>> pageInstances(PageParam pageParam, Long definitionId,
            String status, Long startUserId,
            String startDate, String endDate);

    /**
     * 获取流程实例详情
     */
    ResponseDTO<WorkflowInstanceEntity> getInstance(Long instanceId);

    /**
     * 挂起流程实例
     */
    ResponseDTO<String> suspendInstance(Long instanceId, String reason);

    /**
     * 激活流程实例
     */
    ResponseDTO<String> activateInstance(Long instanceId);

    /**
     * 终止流程实例
     */
    ResponseDTO<String> terminateInstance(Long instanceId, String reason);

    /**
     * 撤销流程实例
     */
    ResponseDTO<String> revokeInstance(Long instanceId, String reason);

    // ==================== 任务管理 ====================

    /**
     * 分页查询我的待办任务
     */
    ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyTasks(PageParam pageParam, Long userId,
            String category, Integer priority,
            String dueStatus);

    /**
     * 分页查询我的已办任务
     */
    ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyCompletedTasks(PageParam pageParam, Long userId,
            String category, String outcome,
            String startDate, String endDate);

    /**
     * 分页查询我发起的流程
     */
    ResponseDTO<PageResult<WorkflowInstanceEntity>> pageMyProcesses(PageParam pageParam, Long userId,
            String category, String status);

    /**
     * 获取任务详情
     */
    ResponseDTO<WorkflowTaskEntity> getTask(Long taskId);

    /**
     * 受理任务
     */
    ResponseDTO<String> claimTask(Long taskId, Long userId);

    /**
     * 取消受理任务
     */
    ResponseDTO<String> unclaimTask(Long taskId);

    /**
     * 委派任务
     */
    ResponseDTO<String> delegateTask(Long taskId, Long targetUserId);

    /**
     * 转交任务
     */
    ResponseDTO<String> transferTask(Long taskId, Long targetUserId);

    /**
     * 完成任务
     */
    ResponseDTO<String> completeTask(Long taskId, String outcome, String comment,
            Map<String, Object> variables, Map<String, Object> formData);

    /**
     * 驳回任务
     */
    ResponseDTO<String> rejectTask(Long taskId, String comment, Map<String, Object> variables);

    // ==================== 流程监控 ====================

    /**
     * 获取流程实例图与当前位置
     */
    ResponseDTO<Map<String, Object>> getProcessDiagram(Long instanceId);

    /**
     * 获取流程历史记录
     */
    ResponseDTO<List<Map<String, Object>>> getProcessHistory(Long instanceId);

    /**
     * 获取流程统计信息
     */
    ResponseDTO<Map<String, Object>> getProcessStatistics(String startDate, String endDate);

    /**
     * 获取用户工作量统计
     */
    ResponseDTO<Map<String, Object>> getUserWorkloadStatistics(Long userId, String startDate, String endDate);
}
