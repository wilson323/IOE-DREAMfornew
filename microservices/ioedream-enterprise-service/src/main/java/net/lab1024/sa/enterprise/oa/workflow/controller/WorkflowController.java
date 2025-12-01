package net.lab1024.sa.enterprise.oa.workflow.controller;

import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.enterprise.oa.workflow.domain.entity.WorkflowDefinitionEntity;
import net.lab1024.sa.enterprise.oa.workflow.domain.entity.WorkflowInstanceEntity;
import net.lab1024.sa.enterprise.oa.workflow.domain.entity.WorkflowTaskEntity;
import net.lab1024.sa.enterprise.oa.workflow.service.WorkflowEngineService;

/**
 * 工作流引擎控制器
 *
 * 提供工作流相关的API接口，包含： - 流程定义管理 - 流程实例管理 - 任务管理 - 流程监控
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@RestController
@RequestMapping("/api/workflow")
@SaCheckPermission("workflow:manage")
@Tag(name = "工作流引擎", description = "基于 BPMN 2.0 的工作流引擎")
@Validated
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowEngineService workflowEngineService;

    // ==================== 流程定义管理 ====================

    @PostMapping("/definition/deploy")
    @Operation(summary = "部署流程定义", description = "部署 BPMN 流程定义")
    public ResponseDTO<String> deployProcess(@RequestParam @NotNull String bpmnXml,
            @RequestParam @NotNull String processName, @RequestParam @NotNull String processKey,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category) {
        return workflowEngineService.deployProcess(bpmnXml, processName, processKey, description,
                category);
    }

    @GetMapping("/definition/page")
    @Operation(summary = "分页查询流程定义")
    public ResponseDTO<PageResult<WorkflowDefinitionEntity>> pageDefinitions(
            @Valid PageParam pageParam, @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return workflowEngineService.pageDefinitions(pageParam, category, status, keyword);
    }

    @GetMapping("/definition/{definitionId}")
    @Operation(summary = "获取流程定义详情")
    public ResponseDTO<WorkflowDefinitionEntity> getDefinition(
            @PathVariable @NotNull Long definitionId) {
        return workflowEngineService.getDefinition(definitionId);
    }

    @PostMapping("/definition/{definitionId}/activate")
    @Operation(summary = "激活流程定义")
    public ResponseDTO<String> activateDefinition(@PathVariable @NotNull Long definitionId) {
        return workflowEngineService.activateDefinition(definitionId);
    }

    @PostMapping("/definition/{definitionId}/disable")
    @Operation(summary = "禁用流程定义")
    public ResponseDTO<String> disableDefinition(@PathVariable @NotNull Long definitionId) {
        return workflowEngineService.disableDefinition(definitionId);
    }

    @DeleteMapping("/definition/{definitionId}")
    @Operation(summary = "删除流程定义")
    public ResponseDTO<String> deleteDefinition(@PathVariable @NotNull Long definitionId,
            @RequestParam(defaultValue = "false") Boolean cascade) {
        return workflowEngineService.deleteDefinition(definitionId, cascade);
    }

    // ==================== 流程实例管理 ====================

    @PostMapping("/instance/start")
    @Operation(summary = "启动流程实例")
    public ResponseDTO<Long> startProcess(@RequestParam @NotNull Long definitionId,
            @RequestParam(required = false) String businessKey,
            @RequestParam(required = false) String instanceName,
            @RequestBody(required = false) Map<String, Object> variables,
            @RequestBody(required = false) Map<String, Object> formData) {
        return workflowEngineService.startProcess(definitionId, businessKey, instanceName,
                variables, formData);
    }

    @GetMapping("/instance/page")
    @Operation(summary = "分页查询流程实例")
    public ResponseDTO<PageResult<WorkflowInstanceEntity>> pageInstances(
            @Valid PageParam pageParam,
            @RequestParam(required = false) Long definitionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long startUserId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return workflowEngineService.pageInstances(pageParam, definitionId, status, startUserId,
                startDate, endDate);
    }

    @GetMapping("/instance/{instanceId}")
    @Operation(summary = "获取流程实例详情")
    public ResponseDTO<WorkflowInstanceEntity> getInstance(
            @PathVariable @NotNull Long instanceId) {
        return workflowEngineService.getInstance(instanceId);
    }

    @PostMapping("/instance/{instanceId}/suspend")
    @Operation(summary = "挂起流程实例")
    public ResponseDTO<String> suspendInstance(@PathVariable @NotNull Long instanceId,
            @RequestParam(required = false) String reason) {
        return workflowEngineService.suspendInstance(instanceId, reason);
    }

    @PostMapping("/instance/{instanceId}/activate")
    @Operation(summary = "激活流程实例")
    public ResponseDTO<String> activateInstance(@PathVariable @NotNull Long instanceId) {
        return workflowEngineService.activateInstance(instanceId);
    }

    @PostMapping("/instance/{instanceId}/terminate")
    @Operation(summary = "终止流程实例")
    public ResponseDTO<String> terminateInstance(@PathVariable @NotNull Long instanceId,
            @RequestParam(required = false) String reason) {
        return workflowEngineService.terminateInstance(instanceId, reason);
    }

    @PostMapping("/instance/{instanceId}/revoke")
    @Operation(summary = "撤销流程实例")
    public ResponseDTO<String> revokeInstance(@PathVariable @NotNull Long instanceId,
            @RequestParam(required = false) String reason) {
        return workflowEngineService.revokeInstance(instanceId, reason);
    }

    // ==================== 任务管理 ====================

    @GetMapping("/task/my-tasks")
    @Operation(summary = "分页查询我的待办任务")
    public ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyTasks(
            @Valid PageParam pageParam,
            @RequestParam @NotNull Long userId, @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String dueStatus) {
        return workflowEngineService.pageMyTasks(pageParam, userId, category, priority, dueStatus);
    }

    @GetMapping("/task/my-completed")
    @Operation(summary = "分页查询我的已办任务")
    public ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyCompletedTasks(
            @Valid PageParam pageParam, @RequestParam @NotNull Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return workflowEngineService.pageMyCompletedTasks(pageParam, userId, category, outcome,
                startDate, endDate);
    }

    @GetMapping("/task/my-processes")
    @Operation(summary = "分页查询我发起的流程")
    public ResponseDTO<PageResult<WorkflowInstanceEntity>> pageMyProcesses(
            @Valid PageParam pageParam, @RequestParam @NotNull Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return workflowEngineService.pageMyProcesses(pageParam, userId, category, status);
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "获取任务详情")
    public ResponseDTO<WorkflowTaskEntity> getTask(
            @PathVariable @NotNull Long taskId) {
        return workflowEngineService.getTask(taskId);
    }

    @PostMapping("/task/{taskId}/claim")
    @Operation(summary = "受理任务")
    public ResponseDTO<String> claimTask(@PathVariable @NotNull Long taskId,
            @RequestParam @NotNull Long userId) {
        return workflowEngineService.claimTask(taskId, userId);
    }

    @PostMapping("/task/{taskId}/unclaim")
    @Operation(summary = "取消受理任务")
    public ResponseDTO<String> unclaimTask(@PathVariable @NotNull Long taskId) {
        return workflowEngineService.unclaimTask(taskId);
    }

    @PostMapping("/task/{taskId}/delegate")
    @Operation(summary = "委派任务")
    public ResponseDTO<String> delegateTask(@PathVariable @NotNull Long taskId,
            @RequestParam @NotNull Long targetUserId) {
        return workflowEngineService.delegateTask(taskId, targetUserId);
    }

    @PostMapping("/task/{taskId}/transfer")
    @Operation(summary = "转交任务")
    public ResponseDTO<String> transferTask(@PathVariable @NotNull Long taskId,
            @RequestParam @NotNull Long targetUserId) {
        return workflowEngineService.transferTask(taskId, targetUserId);
    }

    @PostMapping("/task/{taskId}/complete")
    @Operation(summary = "完成任务")
    public ResponseDTO<String> completeTask(@PathVariable @NotNull Long taskId,
            @RequestParam @NotNull String outcome, @RequestParam(required = false) String comment,
            @RequestBody(required = false) Map<String, Object> variables,
            @RequestBody(required = false) Map<String, Object> formData) {
        return workflowEngineService.completeTask(taskId, outcome, comment, variables, formData);
    }

    @PostMapping("/task/{taskId}/reject")
    @Operation(summary = "驳回任务")
    public ResponseDTO<String> rejectTask(@PathVariable @NotNull Long taskId,
            @RequestParam(required = false) String comment,
            @RequestBody(required = false) Map<String, Object> variables) {
        return workflowEngineService.rejectTask(taskId, comment, variables);
    }

    // ==================== 流程监控 ====================

    @GetMapping("/instance/{instanceId}/diagram")
    @Operation(summary = "获取流程实例图与当前位置")
    public ResponseDTO<Map<String, Object>> getProcessDiagram(
            @PathVariable @NotNull Long instanceId) {
        return workflowEngineService.getProcessDiagram(instanceId);
    }

    @GetMapping("/instance/{instanceId}/history")
    @Operation(summary = "获取流程历史记录")
    public ResponseDTO<java.util.List<Map<String, Object>>> getProcessHistory(
            @PathVariable @NotNull Long instanceId) {
        return workflowEngineService.getProcessHistory(instanceId);
    }

    @GetMapping("/statistics/process")
    @Operation(summary = "获取流程统计信息")
    public ResponseDTO<Map<String, Object>> getProcessStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return workflowEngineService.getProcessStatistics(startDate, endDate);
    }

    @GetMapping("/statistics/user-workload")
    @Operation(summary = "获取用户工作量统计")
    public ResponseDTO<Map<String, Object>> getUserWorkloadStatistics(
            @RequestParam @NotNull Long userId, @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return workflowEngineService.getUserWorkloadStatistics(userId, startDate, endDate);
    }
}
