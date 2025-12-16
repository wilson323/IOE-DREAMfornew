package net.lab1024.sa.oa.workflow.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.entity.WorkflowDefinitionEntity;
import net.lab1024.sa.oa.workflow.entity.WorkflowInstanceEntity;
import net.lab1024.sa.oa.workflow.entity.WorkflowTaskEntity;

/**
 * 工作流引擎服务接口
 * 提供工作流流程定义、实例、任务管理的完整业务逻辑
 * 严格遵循CLAUDE.md架构规范
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 * @version 3.0.0
 */
public interface WorkflowEngineService {

    // ==================== 流程定义管理 ====================

    /**
     * 部署流程定义
     *
     * @param bpmnXml BPMN XML定义
     * @param processName 流程名称
     * @param processKey 流程Key（唯一标识）
     * @param description 流程描述
     * @param category 流程分类
     * @return 部署结果
     */
    ResponseDTO<String> deployProcess(String bpmnXml, String processName, String processKey, String description, String category);

    /**
     * 分页查询流程定义
     *
     * @param pageParam 分页参数
     * @param category 分类
     * @param status 状态
     * @param keyword 关键词
     * @return 流程定义列表
     */
    ResponseDTO<PageResult<WorkflowDefinitionEntity>> pageDefinitions(PageParam pageParam, String category, String status, String keyword);

    /**
     * 获取流程定义详情
     *
     * @param definitionId 定义ID
     * @return 流程定义详情
     */
    ResponseDTO<WorkflowDefinitionEntity> getDefinition(Long definitionId);

    /**
     * 激活流程定义
     *
     * @param definitionId 定义ID
     * @return 操作结果
     */
    ResponseDTO<String> activateDefinition(Long definitionId);

    /**
     * 禁用流程定义
     *
     * @param definitionId 定义ID
     * @return 操作结果
     */
    ResponseDTO<String> disableDefinition(Long definitionId);

    /**
     * 删除流程定义
     *
     * @param definitionId 定义ID
     * @param cascade 是否级联删除
     * @return 操作结果
     */
    ResponseDTO<String> deleteDefinition(Long definitionId, Boolean cascade);

    // ==================== 流程实例管理 ====================

    /**
     * 启动流程实例
     *
     * @param definitionId 定义ID
     * @param businessKey 业务Key
     * @param instanceName 实例名称
     * @param variables 流程变量
     * @param formData 表单数据
     * @return 流程实例ID
     */
    ResponseDTO<Long> startProcess(Long definitionId, String businessKey, String instanceName, Map<String, Object> variables, Map<String, Object> formData);

    /**
     * 分页查询流程实例
     *
     * @param pageParam 分页参数
     * @param definitionId 定义ID
     * @param status 状态
     * @param startUserId 发起人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 流程实例列表
     */
    ResponseDTO<PageResult<WorkflowInstanceEntity>> pageInstances(PageParam pageParam, Long definitionId, String status, Long startUserId, String startDate, String endDate);

    /**
     * 获取流程实例详情
     *
     * @param instanceId 实例ID
     * @return 流程实例详情
     */
    ResponseDTO<WorkflowInstanceEntity> getInstance(Long instanceId);

    /**
     * 挂起流程实例
     *
     * @param instanceId 实例ID
     * @param reason 原因
     * @return 操作结果
     */
    ResponseDTO<String> suspendInstance(Long instanceId, String reason);

    /**
     * 激活流程实例
     *
     * @param instanceId 实例ID
     * @return 操作结果
     */
    ResponseDTO<String> activateInstance(Long instanceId);

    /**
     * 终止流程实例
     *
     * @param instanceId 实例ID
     * @param reason 原因
     * @return 操作结果
     */
    ResponseDTO<String> terminateInstance(Long instanceId, String reason);

    /**
     * 撤销流程实例
     *
     * @param instanceId 实例ID
     * @param reason 原因
     * @return 操作结果
     */
    ResponseDTO<String> revokeInstance(Long instanceId, String reason);

    // ==================== 任务管理 ====================

    /**
     * 分页查询我的待办任务
     *
     * @param pageParam 分页参数
     * @param userId 用户ID
     * @param category 分类
     * @param priority 优先级
     * @param dueStatus 到期状态
     * @return 任务列表
     */
    ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyTasks(PageParam pageParam, Long userId, String category, Integer priority, String dueStatus);

    /**
     * 分页查询我的已办任务
     *
     * @param pageParam 分页参数
     * @param userId 用户ID
     * @param category 分类
     * @param outcome 结果
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 任务列表
     */
    ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyCompletedTasks(PageParam pageParam, Long userId, String category, String outcome, String startDate, String endDate);

    /**
     * 分页查询我发起的流程
     *
     * @param pageParam 分页参数
     * @param userId 用户ID
     * @param category 分类
     * @param status 状态
     * @return 流程实例列表
     */
    ResponseDTO<PageResult<WorkflowInstanceEntity>> pageMyProcesses(PageParam pageParam, Long userId, String category, String status);

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    ResponseDTO<WorkflowTaskEntity> getTask(Long taskId);

    /**
     * 受理任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 操作结果
     */
    ResponseDTO<String> claimTask(Long taskId, Long userId);

    /**
     * 取消受理任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    ResponseDTO<String> unclaimTask(Long taskId);

    /**
     * 委派任务
     *
     * @param taskId 任务ID
     * @param targetUserId 目标用户ID
     * @return 操作结果
     */
    ResponseDTO<String> delegateTask(Long taskId, Long targetUserId);

    /**
     * 转交任务
     *
     * @param taskId 任务ID
     * @param targetUserId 目标用户ID
     * @return 操作结果
     */
    ResponseDTO<String> transferTask(Long taskId, Long targetUserId);

    /**
     * 完成任务
     *
     * @param taskId 任务ID
     * @param outcome 结果
     * @param comment 意见
     * @param variables 流程变量
     * @param formData 表单数据
     * @return 操作结果
     */
    ResponseDTO<String> completeTask(Long taskId, String outcome, String comment, Map<String, Object> variables, Map<String, Object> formData);

    /**
     * 驳回任务
     *
     * @param taskId 任务ID
     * @param comment 意见
     * @param variables 流程变量
     * @return 操作结果
     */
    ResponseDTO<String> rejectTask(Long taskId, String comment, Map<String, Object> variables);

    // ==================== 流程监控 ====================

    /**
     * 获取流程实例图与当前位置
     *
     * @param instanceId 实例ID
     * @return 流程图数据
     */
    ResponseDTO<Map<String, Object>> getProcessDiagram(Long instanceId);

    /**
     * 获取流程历史记录
     *
     * @param instanceId 实例ID
     * @return 历史记录列表
     */
    ResponseDTO<List<Map<String, Object>>> getProcessHistory(Long instanceId);

    /**
     * 获取流程统计信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    ResponseDTO<Map<String, Object>> getProcessStatistics(String startDate, String endDate);

    /**
     * 获取用户工作量统计
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作量统计
     */
    ResponseDTO<Map<String, Object>> getUserWorkloadStatistics(Long userId, String startDate, String endDate);
}





