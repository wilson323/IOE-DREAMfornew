package net.lab1024.sa.oa.workflow.controller;

import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.common.util.SmartRequestUtil;
import net.lab1024.sa.oa.workflow.entity.WorkflowDefinitionEntity;
import net.lab1024.sa.oa.workflow.entity.WorkflowInstanceEntity;
import net.lab1024.sa.oa.workflow.entity.WorkflowTaskEntity;
import net.lab1024.sa.oa.workflow.service.WorkflowEngineService;

/**
 * 工作流引擎控制器
 * 提供工作流流程定义、实例、任务管理的完整API接口
 * 严格遵循CLAUDE.md架构规范
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 * @version 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow/engine")
@Tag(name = "工作流引擎", description = "工作流流程定义、实例、任务管理API")
@PermissionCheck(value = "OA_WORKFLOW", description = "工作流引擎管理模块权限")
@Validated
public class WorkflowEngineController {

    @Resource
    private WorkflowEngineService workflowEngineService;

    // ==================== 流程定义管理 ====================

    @Observed(name = "workflow.deployProcess", contextualName = "workflow-deploy-process")
    @Operation(summary = "部署流程定义", description = "部署新的工作流流程定义")
    @PostMapping("/definition/deploy")
    @PermissionCheck(value = "OA_WORKFLOW_DEPLOY", description = "部署流程定义")
    public ResponseDTO<String> deployProcess(
            @Parameter(description = "BPMN XML", required = true) @RequestParam String bpmnXml,
            @Parameter(description = "流程名称", required = true) @RequestParam String processName,
            @Parameter(description = "流程Key", required = true) @RequestParam String processKey,
            @Parameter(description = "流程描述") @RequestParam(required = false) String description,
            @Parameter(description = "流程分类") @RequestParam(required = false) String category) {
        return workflowEngineService.deployProcess(bpmnXml, processName, processKey, description, category);
    }

    @Observed(name = "workflow.pageDefinitions", contextualName = "workflow-page-definitions")
    @Operation(summary = "分页查询流程定义", description = "分页查询工作流流程定义列表")
    @GetMapping("/definition/page")
    @PermissionCheck(value = "OA_WORKFLOW_VIEW", description = "查看流程定义")
    public ResponseDTO<PageResult<WorkflowDefinitionEntity>> pageDefinitions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Long pageSize,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(pageNum != null ? pageNum.intValue() : 1);
        pageParam.setPageSize(pageSize != null ? pageSize.intValue() : 20);
        return workflowEngineService.pageDefinitions(pageParam, category, status, keyword);
    }

    @Observed(name = "workflow.getDefinition", contextualName = "workflow-get-definition")
    @Operation(summary = "获取流程定义详情", description = "根据ID获取流程定义详细信息")
    @GetMapping("/definition/{definitionId}")
    @PermissionCheck(value = "OA_WORKFLOW_VIEW", description = "查看流程定义详情")
    public ResponseDTO<WorkflowDefinitionEntity> getDefinition(
            @Parameter(description = "定义ID", required = true) @PathVariable Long definitionId) {
        return workflowEngineService.getDefinition(definitionId);
    }

    @Observed(name = "workflow.activateDefinition", contextualName = "workflow-activate-definition")
    @Operation(summary = "激活流程定义", description = "激活指定的流程定义")
    @PutMapping("/definition/{definitionId}/activate")
    public ResponseDTO<String> activateDefinition(
            @Parameter(description = "定义ID", required = true) @PathVariable Long definitionId) {
        return workflowEngineService.activateDefinition(definitionId);
    }

    @Observed(name = "workflow.disableDefinition", contextualName = "workflow-disable-definition")
    @Operation(summary = "禁用流程定义", description = "禁用指定的流程定义")
    @PutMapping("/definition/{definitionId}/disable")
    public ResponseDTO<String> disableDefinition(
            @Parameter(description = "定义ID", required = true) @PathVariable Long definitionId) {
        return workflowEngineService.disableDefinition(definitionId);
    }

    @Observed(name = "workflow.deleteDefinition", contextualName = "workflow-delete-definition")
    @Operation(summary = "删除流程定义", description = "删除指定的流程定义")
    @PostMapping("/definition/{definitionId}/delete")
    public ResponseDTO<String> deleteDefinition(
            @Parameter(description = "定义ID", required = true) @PathVariable Long definitionId,
            @Parameter(description = "是否级联删除") @RequestParam(defaultValue = "false") Boolean cascade) {
        return workflowEngineService.deleteDefinition(definitionId, cascade);
    }

    // ==================== 流程实例管理 ====================

    @Observed(name = "workflow.startProcess", contextualName = "workflow-start-process")
    @Operation(summary = "启动流程实例", description = "启动新的工作流流程实例")
    @PostMapping("/instance/start")
    @PermissionCheck(value = "OA_WORKFLOW_START", description = "启动流程实例")
    public ResponseDTO<Long> startProcess(@RequestBody(required = false) Map<String, Object> request) {
        if (request == null) {
            return ResponseDTO.error("PARAM_ERROR", "请求体不能为空");
        }

        Long definitionId = toLong(request.get("definitionId"));
        String businessKey = toStringValue(request.get("businessKey"));
        String instanceName = toStringValue(request.get("instanceName"));
        Map<String, Object> variables = toMap(request.get("variables"));
        Map<String, Object> formData = toMap(request.get("formData"));

        if (definitionId == null) {
            return ResponseDTO.error("PARAM_ERROR", "definitionId不能为空");
        }

        return workflowEngineService.startProcess(definitionId, businessKey, instanceName, variables, formData);
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private static String toStringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toMap(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    @Observed(name = "workflow.pageInstances", contextualName = "workflow-page-instances")
    @Operation(summary = "分页查询流程实例", description = "分页查询工作流流程实例列表")
    @GetMapping("/instance/page")
    public ResponseDTO<PageResult<WorkflowInstanceEntity>> pageInstances(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Long pageSize,
            @Parameter(description = "定义ID") @RequestParam(required = false) Long definitionId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "发起人ID") @RequestParam(required = false) Long startUserId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(pageNum != null ? pageNum.intValue() : 1);
        pageParam.setPageSize(pageSize != null ? pageSize.intValue() : 20);
        return workflowEngineService.pageInstances(pageParam, definitionId, status, startUserId, startDate, endDate);
    }

    @Observed(name = "workflow.getInstance", contextualName = "workflow-get-instance")
    @Operation(summary = "获取流程实例详情", description = "根据ID获取流程实例详细信息")
    @GetMapping("/instance/{instanceId}")
    public ResponseDTO<WorkflowInstanceEntity> getInstance(
            @Parameter(description = "实例ID", required = true) @PathVariable Long instanceId) {
        return workflowEngineService.getInstance(instanceId);
    }

    @Observed(name = "workflow.suspendInstance", contextualName = "workflow-suspend-instance")
    @Operation(summary = "挂起流程实例", description = "挂起指定的流程实例")
    @PutMapping("/instance/{instanceId}/suspend")
    public ResponseDTO<String> suspendInstance(
            @Parameter(description = "实例ID", required = true) @PathVariable Long instanceId,
            @Parameter(description = "原因") @RequestParam(required = false) String reason) {
        return workflowEngineService.suspendInstance(instanceId, reason);
    }

    @Observed(name = "workflow.activateInstance", contextualName = "workflow-activate-instance")
    @Operation(summary = "激活流程实例", description = "激活指定的流程实例")
    @PutMapping("/instance/{instanceId}/activate")
    public ResponseDTO<String> activateInstance(
            @Parameter(description = "实例ID", required = true) @PathVariable Long instanceId) {
        return workflowEngineService.activateInstance(instanceId);
    }

    @Observed(name = "workflow.terminateInstance", contextualName = "workflow-terminate-instance")
    @Operation(summary = "终止流程实例", description = "终止指定的流程实例")
    @PutMapping("/instance/{instanceId}/terminate")
    public ResponseDTO<String> terminateInstance(
            @Parameter(description = "实例ID", required = true) @PathVariable Long instanceId,
            @Parameter(description = "原因") @RequestParam(required = false) String reason) {
        return workflowEngineService.terminateInstance(instanceId, reason);
    }

    @Observed(name = "workflow.revokeInstance", contextualName = "workflow-revoke-instance")
    @Operation(summary = "撤销流程实例", description = "撤销指定的流程实例")
    @PutMapping("/instance/{instanceId}/revoke")
    public ResponseDTO<String> revokeInstance(
            @Parameter(description = "实例ID", required = true) @PathVariable Long instanceId,
            @Parameter(description = "原因") @RequestParam(required = false) String reason) {
        return workflowEngineService.revokeInstance(instanceId, reason);
    }

    // ==================== 任务管理 ====================

    @Observed(name = "workflow.pageMyTasks", contextualName = "workflow-page-my-tasks")
    @Operation(summary = "分页查询我的待办任务", description = "分页查询当前用户的待办任务列表")
    @GetMapping("/task/my/pending")
    public ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyTasks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Long pageSize,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "优先级") @RequestParam(required = false) Integer priority,
            @Parameter(description = "到期状态") @RequestParam(required = false) String dueStatus) {
        // 从请求中获取当前用户ID
        Long userId = SmartRequestUtil.getUserId();
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(pageNum != null ? pageNum.intValue() : 1);
        pageParam.setPageSize(pageSize != null ? pageSize.intValue() : 20);
        return workflowEngineService.pageMyTasks(pageParam, userId, category, priority, dueStatus);
    }

    @Observed(name = "workflow.pageMyCompletedTasks", contextualName = "workflow-page-my-completed-tasks")
    @Operation(summary = "分页查询我的已办任务", description = "分页查询当前用户的已办任务列表")
    @GetMapping("/task/my/completed")
    public ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyCompletedTasks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Long pageSize,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "结果") @RequestParam(required = false) String outcome,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        // 从请求中获取当前用户ID
        Long userId = SmartRequestUtil.getUserId();
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(pageNum != null ? pageNum.intValue() : 1);
        pageParam.setPageSize(pageSize != null ? pageSize.intValue() : 20);
        return workflowEngineService.pageMyCompletedTasks(pageParam, userId, category, outcome, startDate, endDate);
    }

    @Observed(name = "workflow.pageMyProcesses", contextualName = "workflow-page-my-processes")
    @Operation(summary = "分页查询我发起的流程", description = "分页查询当前用户发起的流程列表")
    @GetMapping("/instance/my")
    public ResponseDTO<PageResult<WorkflowInstanceEntity>> pageMyProcesses(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Long pageSize,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        // 从请求中获取当前用户ID
        Long userId = SmartRequestUtil.getUserId();
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(pageNum != null ? pageNum.intValue() : 1);
        pageParam.setPageSize(pageSize != null ? pageSize.intValue() : 20);
        return workflowEngineService.pageMyProcesses(pageParam, userId, category, status);
    }

    @Observed(name = "workflow.getTask", contextualName = "workflow-get-task")
    @Operation(summary = "获取任务详情", description = "根据ID获取任务详细信息")
    @GetMapping("/task/{taskId}")
    public ResponseDTO<WorkflowTaskEntity> getTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        return workflowEngineService.getTask(taskId);
    }

    @Observed(name = "workflow.claimTask", contextualName = "workflow-claim-task")
    @Operation(summary = "受理任务", description = "受理指定的待办任务")
    @PutMapping("/task/{taskId}/claim")
    public ResponseDTO<String> claimTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        // 从请求中获取当前用户ID
        Long userId = SmartRequestUtil.getUserId();
        return workflowEngineService.claimTask(taskId, userId);
    }

    @Observed(name = "workflow.unclaimTask", contextualName = "workflow-unclaim-task")
    @Operation(summary = "取消受理任务", description = "取消受理指定的任务")
    @PutMapping("/task/{taskId}/unclaim")
    public ResponseDTO<String> unclaimTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        return workflowEngineService.unclaimTask(taskId);
    }

    @Observed(name = "workflow.delegateTask", contextualName = "workflow-delegate-task")
    @Operation(summary = "委派任务", description = "将任务委派给其他用户")
    @PutMapping("/task/{taskId}/delegate")
    public ResponseDTO<String> delegateTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId,
            @Parameter(description = "目标用户ID", required = true) @RequestParam Long targetUserId) {
        return workflowEngineService.delegateTask(taskId, targetUserId);
    }

    @Observed(name = "workflow.transferTask", contextualName = "workflow-transfer-task")
    @Operation(summary = "转交任务", description = "将任务转交给其他用户")
    @PutMapping("/task/{taskId}/transfer")
    public ResponseDTO<String> transferTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId,
            @Parameter(description = "目标用户ID", required = true) @RequestParam Long targetUserId) {
        return workflowEngineService.transferTask(taskId, targetUserId);
    }

    @Observed(name = "workflow.completeTask", contextualName = "workflow-complete-task")
    @Operation(summary = "完成任务", description = "完成指定的任务")
    @PostMapping("/task/{taskId}/complete")
    public ResponseDTO<String> completeTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId,
            @Parameter(description = "结果") @RequestParam(required = false, defaultValue = "同意") String outcome,
            @Parameter(description = "意见") @RequestParam(required = false) String comment,
            @Parameter(description = "流程变量") @RequestBody(required = false) Map<String, Object> variables,
            @Parameter(description = "表单数据") @RequestBody(required = false) Map<String, Object> formData) {
        return workflowEngineService.completeTask(taskId, outcome, comment, variables, formData);
    }

    @Observed(name = "workflow.rejectTask", contextualName = "workflow-reject-task")
    @Operation(summary = "驳回任务", description = "驳回指定的任务")
    @PostMapping("/task/{taskId}/reject")
    public ResponseDTO<String> rejectTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId,
            @Parameter(description = "意见", required = true) @RequestParam String comment,
            @Parameter(description = "流程变量") @RequestBody(required = false) Map<String, Object> variables) {
        return workflowEngineService.rejectTask(taskId, comment, variables);
    }

    // ==================== 流程监控 ====================

    @Observed(name = "workflow.getProcessDiagram", contextualName = "workflow-get-process-diagram")
    @Operation(summary = "获取流程实例图与当前位置", description = "获取流程实例的可视化流程图和当前节点位置")
    @GetMapping("/instance/{instanceId}/diagram")
    public ResponseDTO<Map<String, Object>> getProcessDiagram(
            @Parameter(description = "实例ID", required = true) @PathVariable Long instanceId) {
        return workflowEngineService.getProcessDiagram(instanceId);
    }

    @Observed(name = "workflow.getProcessHistory", contextualName = "workflow-get-process-history")
    @Operation(summary = "获取流程历史记录", description = "获取流程实例的完整历史记录")
    @GetMapping("/instance/{instanceId}/history")
    public ResponseDTO<List<Map<String, Object>>> getProcessHistory(
            @Parameter(description = "实例ID", required = true) @PathVariable Long instanceId) {
        return workflowEngineService.getProcessHistory(instanceId);
    }

    @Observed(name = "workflow.getProcessStatistics", contextualName = "workflow-get-process-statistics")
    @Operation(summary = "获取流程统计信息", description = "获取工作流的统计信息")
    @GetMapping("/statistics")
    public ResponseDTO<Map<String, Object>> getProcessStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return workflowEngineService.getProcessStatistics(startDate, endDate);
    }

    @Observed(name = "workflow.getUserWorkloadStatistics", contextualName = "workflow-get-user-workload-statistics")
    @Operation(summary = "获取用户工作量统计", description = "获取指定用户的工作量统计信息")
    @GetMapping("/statistics/user/{userId}")
    public ResponseDTO<Map<String, Object>> getUserWorkloadStatistics(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return workflowEngineService.getUserWorkloadStatistics(userId, startDate, endDate);
    }
}




