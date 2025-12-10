package net.lab1024.sa.oa.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.oa.workflow.service.ApprovalService;
import net.lab1024.sa.common.workflow.domain.form.ApprovalTaskQueryForm;
import net.lab1024.sa.common.workflow.domain.form.ApprovalActionForm;
import net.lab1024.sa.common.workflow.domain.vo.ApprovalTaskVO;
import net.lab1024.sa.common.workflow.domain.vo.ApprovalInstanceVO;
import net.lab1024.sa.common.workflow.domain.vo.ApprovalStatisticsVO;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 企业级审批流控制器
 * <p>
 * 提供完整的审批管理功能，包括：
 * 1. 待办任务查询和处理
 * 2. 已办任务查询
 * 3. 审批历史记录
 * 4. 审批统计报表
 * 5. 审批流程管理
 * 6. 多种业务类型支持（请假、报销、出差等）
 * </p>
 * <p>
 * 严格遵循CLAUDE.md全局架构规范：
 * - 使用@Resource依赖注入
 * - 统一ResponseDTO响应格式
 * - 完善的参数验证和异常处理
 * - 详细的日志记录
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/approval")
@Tag(name = "审批管理", description = "企业级审批流管理相关接口")
public class ApprovalController {

    @Resource
    private ApprovalService approvalService;

    @Operation(summary = "获取待办任务列表")
    @GetMapping("/tasks/todo")
    public ResponseDTO<PageResult<ApprovalTaskVO>> getTodoTasks(
            @Parameter(description = "查询条件") @ModelAttribute ApprovalTaskQueryForm queryForm) {
        log.info("[审批管理] 查询待办任务: userId={}, status={}, pageNum={}, pageSize={}",
                queryForm.getUserId(), queryForm.getStatus(), queryForm.getPageNum(), queryForm.getPageSize());

        try {
            PageResult<ApprovalTaskVO> result = approvalService.getTodoTasks(queryForm);

            log.info("[审批管理] 待办任务查询完成: total={}, count={}",
                    result.getTotal(), result.getList().size());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[审批管理] 待办任务查询异常: userId={}, error={}",
                    queryForm.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "查询待办任务失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取已办任务列表")
    @GetMapping("/tasks/completed")
    public ResponseDTO<PageResult<ApprovalTaskVO>> getCompletedTasks(
            @Parameter(description = "查询条件") @ModelAttribute ApprovalTaskQueryForm queryForm) {
        log.info("[审批管理] 查询已办任务: userId={}, pageNum={}, pageSize={}",
                queryForm.getUserId(), queryForm.getPageNum(), queryForm.getPageSize());

        try {
            PageResult<ApprovalTaskVO> result = approvalService.getCompletedTasks(queryForm);

            log.info("[审批管理] 已办任务查询完成: total={}, count={}",
                    result.getTotal(), result.getList().size());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[审批管理] 已办任务查询异常: userId={}, error={}",
                    queryForm.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "查询已办任务失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取我的申请任务")
    @GetMapping("/tasks/my-applications")
    public ResponseDTO<PageResult<ApprovalTaskVO>> getMyApplications(
            @Parameter(description = "查询条件") @ModelAttribute ApprovalTaskQueryForm queryForm) {
        log.info("[审批管理] 查询我的申请: applicantId={}, pageNum={}, pageSize={}",
                queryForm.getApplicantId(), queryForm.getPageNum(), queryForm.getPageSize());

        try {
            PageResult<ApprovalTaskVO> result = approvalService.getMyApplications(queryForm);

            log.info("[审批管理] 我的申请查询完成: total={}, count={}",
                    result.getTotal(), result.getList().size());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[审批管理] 我的申请查询异常: applicantId={}, error={}",
                    queryForm.getApplicantId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "查询我的申请失败：" + e.getMessage());
        }
    }

    @Operation(summary = "审批同意")
    @PostMapping("/tasks/approve")
    public ResponseDTO<String> approveTask(
            @Parameter(description = "审批操作表单") @Valid @RequestBody ApprovalActionForm actionForm) {
        log.info("[审批管理] 审批同意: taskId={}, userId={}, comment={}",
                actionForm.getTaskId(), actionForm.getUserId(), actionForm.getComment());

        try {
            // 参数验证
            if (actionForm.getTaskId() == null) {
                return ResponseDTO.error("PARAM_ERROR", "任务ID不能为空");
            }
            if (actionForm.getUserId() == null) {
                return ResponseDTO.error("PARAM_ERROR", "审批人ID不能为空");
            }

            // 执行审批同意
            String result = approvalService.approveTask(actionForm);

            log.info("[审批管理] 审批同意成功: taskId={}, result={}",
                    actionForm.getTaskId(), result);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[审批管理] 审批同意异常: taskId={}, userId={}, error={}",
                    actionForm.getTaskId(), actionForm.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "审批同意失败：" + e.getMessage());
        }
    }

    @Operation(summary = "审批驳回")
    @PostMapping("/tasks/reject")
    public ResponseDTO<String> rejectTask(
            @Parameter(description = "审批操作表单") @Valid @RequestBody ApprovalActionForm actionForm) {
        log.info("[审批管理] 审批驳回: taskId={}, userId={}, comment={}",
                actionForm.getTaskId(), actionForm.getUserId(), actionForm.getComment());

        try {
            // 参数验证
            if (actionForm.getTaskId() == null) {
                return ResponseDTO.error("PARAM_ERROR", "任务ID不能为空");
            }
            if (actionForm.getUserId() == null) {
                return ResponseDTO.error("PARAM_ERROR", "审批人ID不能为空");
            }
            if (actionForm.getComment() == null || actionForm.getComment().trim().isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "驳回理由不能为空");
            }

            // 执行审批驳回
            String result = approvalService.rejectTask(actionForm);

            log.info("[审批管理] 审批驳回成功: taskId={}, result={}",
                    actionForm.getTaskId(), result);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[审批管理] 审批驳回异常: taskId={}, userId={}, error={}",
                    actionForm.getTaskId(), actionForm.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "审批驳回失败：" + e.getMessage());
        }
    }

    @Operation(summary = "审批转办")
    @PostMapping("/tasks/transfer")
    public ResponseDTO<String> transferTask(
            @Parameter(description = "审批操作表单") @Valid @RequestBody ApprovalActionForm actionForm) {
        log.info("[审批管理] 审批转办: taskId={}, userId={}, targetUserId={}",
                actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId());

        try {
            // 参数验证
            if (actionForm.getTaskId() == null) {
                return ResponseDTO.error("PARAM_ERROR", "任务ID不能为空");
            }
            if (actionForm.getUserId() == null) {
                return ResponseDTO.error("PARAM_ERROR", "当前审批人ID不能为空");
            }
            if (actionForm.getTargetUserId() == null) {
                return ResponseDTO.error("PARAM_ERROR", "目标审批人ID不能为空");
            }

            // 执行审批转办
            String result = approvalService.transferTask(actionForm);

            log.info("[审批管理] 审批转办成功: taskId={}, result={}",
                    actionForm.getTaskId(), result);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[审批管理] 审批转办异常: taskId={}, userId={}, targetUserId={}, error={}",
                    actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "审批转办失败：" + e.getMessage());
        }
    }

    @Operation(summary = "审批委派")
    @PostMapping("/tasks/delegate")
    public ResponseDTO<String> delegateTask(
            @Parameter(description = "审批操作表单") @Valid @RequestBody ApprovalActionForm actionForm) {
        log.info("[审批管理] 审批委派: taskId={}, userId={}, targetUserId={}",
                actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId());

        try {
            // 参数验证
            if (actionForm.getTaskId() == null) {
                return ResponseDTO.error("PARAM_ERROR", "任务ID不能为空");
            }
            if (actionForm.getUserId() == null) {
                return ResponseDTO.error("PARAM_ERROR", "当前审批人ID不能为空");
            }
            if (actionForm.getTargetUserId() == null) {
                return ResponseDTO.error("PARAM_ERROR", "委派人ID不能为空");
            }

            // 执行审批委派
            String result = approvalService.delegateTask(actionForm);

            log.info("[审批管理] 审批委派成功: taskId={}, result={}",
                    actionForm.getTaskId(), result);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[审批管理] 审批委派异常: taskId={}, userId={}, targetUserId={}, error={}",
                    actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "审批委派失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取审批任务详情")
    @GetMapping("/tasks/{taskId}")
    public ResponseDTO<ApprovalTaskVO> getTaskDetail(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        log.info("[审批管理] 获取审批任务详情: taskId={}", taskId);

        try {
            if (taskId == null) {
                return ResponseDTO.error("PARAM_ERROR", "任务ID不能为空");
            }

            ApprovalTaskVO taskDetail = approvalService.getTaskDetail(taskId);

            if (taskDetail == null) {
                return ResponseDTO.error("TASK_NOT_FOUND", "审批任务不存在");
            }

            log.info("[审批管理] 审批任务详情获取成功: taskId={}, taskName={}",
                    taskId, taskDetail.getTaskName());

            return ResponseDTO.ok(taskDetail);

        } catch (Exception e) {
            log.error("[审批管理] 获取审批任务详情异常: taskId={}, error={}", taskId, e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取审批任务详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取审批流程实例详情")
    @GetMapping("/instances/{instanceId}")
    public ResponseDTO<ApprovalInstanceVO> getInstanceDetail(
            @Parameter(description = "流程实例ID") @PathVariable Long instanceId) {
        log.info("[审批管理] 获取审批流程详情: instanceId={}", instanceId);

        try {
            if (instanceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "流程实例ID不能为空");
            }

            ApprovalInstanceVO instanceDetail = approvalService.getInstanceDetail(instanceId);

            if (instanceDetail == null) {
                return ResponseDTO.error("INSTANCE_NOT_FOUND", "审批流程实例不存在");
            }

            log.info("[审批管理] 审批流程详情获取成功: instanceId={}, processName={}",
                    instanceId, instanceDetail.getProcessName());

            return ResponseDTO.ok(instanceDetail);

        } catch (Exception e) {
            log.error("[审批管理] 获取审批流程详情异常: instanceId={}, error={}", instanceId, e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取审批流程详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取审批统计信息")
    @GetMapping("/statistics")
    public ResponseDTO<ApprovalStatisticsVO> getApprovalStatistics(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "统计类型：day/week/month") @RequestParam(defaultValue = "week") String statisticsType) {
        log.info("[审批管理] 获取审批统计: userId={}, departmentId={}, type={}",
                userId, departmentId, statisticsType);

        try {
            ApprovalStatisticsVO statistics = approvalService.getApprovalStatistics(userId, departmentId, statisticsType);

            log.info("[审批管理] 审批统计获取成功: userId={}, departmentId={}, type={}",
                    userId, departmentId, statisticsType);

            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[审批管理] 获取审批统计异常: userId={}, departmentId={}, type={}, error={}",
                    userId, departmentId, statisticsType, e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取审批统计失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取业务类型列表")
    @GetMapping("/business-types")
    public ResponseDTO<List<Map<String, Object>>> getBusinessTypes() {
        log.info("[审批管理] 获取业务类型列表");

        try {
            List<Map<String, Object>> businessTypes = approvalService.getBusinessTypes();

            log.info("[审批管理] 业务类型列表获取成功: count={}", businessTypes.size());

            return ResponseDTO.ok(businessTypes);

        } catch (Exception e) {
            log.error("[审批管理] 获取业务类型列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取业务类型列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取审批优先级列表")
    @GetMapping("/priorities")
    public ResponseDTO<List<Map<String, Object>>> getPriorities() {
        log.info("[审批管理] 获取审批优先级列表");

        try {
            List<Map<String, Object>> priorities = approvalService.getPriorities();

            log.info("[审批管理] 审批优先级列表获取成功: count={}", priorities.size());

            return ResponseDTO.ok(priorities);

        } catch (Exception e) {
            log.error("[审批管理] 获取审批优先级列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取审批优先级列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "批量审批处理")
    @PostMapping("/tasks/batch-action")
    public ResponseDTO<Map<String, Object>> batchProcessTasks(
            @Parameter(description = "批量操作参数") @RequestBody Map<String, Object> batchParams) {
        log.info("[审批管理] 批量审批处理: params={}", batchParams);

        try {
            // 参数验证
            @SuppressWarnings("unchecked")
            List<Long> taskIds = (List<Long>) batchParams.get("taskIds");
            String action = (String) batchParams.get("action");
            Long userId = (Long) batchParams.get("userId");
            String comment = (String) batchParams.get("comment");

            if (taskIds == null || taskIds.isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "任务ID列表不能为空");
            }
            if (action == null || action.trim().isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "操作类型不能为空");
            }
            if (userId == null) {
                return ResponseDTO.error("PARAM_ERROR", "操作人ID不能为空");
            }

            // 执行批量操作
            Map<String, Object> result = approvalService.batchProcessTasks(taskIds, action, userId, comment);

            log.info("[审批管理] 批量审批处理完成: action={}, taskCount={}, successCount={}, failedCount={}",
                    action, taskIds.size(),
                    result.get("successCount"), result.get("failedCount"));

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[审批管理] 批量审批处理异常: params={}, error={}", batchParams, e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "批量审批处理失败：" + e.getMessage());
        }
    }

    @Operation(summary = "撤回审批申请")
    @PostMapping("/instances/{instanceId}/withdraw")
    public ResponseDTO<String> withdrawApplication(
            @Parameter(description = "流程实例ID") @PathVariable Long instanceId,
            @Parameter(description = "撤回理由") @RequestParam String reason,
            @Parameter(description = "申请人ID") @RequestParam Long applicantId) {
        log.info("[审批管理] 撤回审批申请: instanceId={}, applicantId={}, reason={}",
                instanceId, applicantId, reason);

        try {
            if (instanceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "流程实例ID不能为空");
            }
            if (applicantId == null) {
                return ResponseDTO.error("PARAM_ERROR", "申请人ID不能为空");
            }
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "撤回理由不能为空");
            }

            // 执行撤回操作
            String result = approvalService.withdrawApplication(instanceId, applicantId, reason);

            log.info("[审批管理] 审批申请撤回成功: instanceId={}, result={}", instanceId, result);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[审批管理] 撤回审批申请异常: instanceId={}, applicantId={}, error={}",
                    instanceId, applicantId, e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "撤回审批申请失败：" + e.getMessage());
        }
    }
}
