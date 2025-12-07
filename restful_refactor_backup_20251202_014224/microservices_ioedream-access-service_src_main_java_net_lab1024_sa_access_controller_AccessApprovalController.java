package net.lab1024.sa.access.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.entity.ApprovalProcessEntity;
import net.lab1024.sa.access.domain.form.ApprovalApplicationForm;
import net.lab1024.sa.access.domain.form.ApprovalQueryForm;
import net.lab1024.sa.access.domain.vo.ApprovalProcessVO;
import net.lab1024.sa.access.domain.vo.ApprovalStatusVO;
import net.lab1024.sa.access.domain.vo.ApprovalStatisticsVO;
import net.lab1024.sa.access.service.AccessApprovalService;
import net.lab1024.sa.common.controller.SupportBaseController;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.annotation.RequireResource;
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartResponseUtil;

/**
 * 门禁审批流程管理控制器
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入（一级规范）
 * - 使用jakarta.*包名（一级规范）
 * - 完整的权限控制@SaCheckPermission
 * - RESTful API设计规范
 * - 统一响应格式ResponseDTO
 * - 完整的参数验证@Valid
 * - 详细的Swagger文档注解
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@Slf4j
@RestController
@RequestMapping("/api/access/approval")
@Tag(name = "门禁审批流程管理", description = "门禁审批流程的申请、审批、查询和管理")
@Validated
public class AccessApprovalController extends SupportBaseController {

    @Resource
    private AccessApprovalService approvalService;

    /**
     * 提交审批申请
     *
     * @param form 申请表单
     * @return 流程ID
     */
    @PostMapping("/apply")
    @Operation(summary = "提交审批申请", description = "用户提交门禁权限、访客预约等审批申请")
    @SaCheckPermission("access:approval:apply")
    public ResponseDTO<Long> submitApplication(@Valid @RequestBody ApprovalApplicationForm form) {
        log.info("开始处理审批申请，申请类型: {}, 申请人ID: {}", form.getProcessType(), form.getApplicantId());

        try {
            Long processId = approvalService.submitApplication(form);
            log.info("审批申请提交成功，流程ID: {}", processId);
            return SmartResponseUtil.ok(processId, "申请提交成功");
        } catch (Exception e) {
            log.error("审批申请提交失败，申请类型: {}, 申请人ID: {}", form.getProcessType(), form.getApplicantId(), e);
            return SmartResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 分页查询审批流程
     *
     * @param queryForm 查询表单
     * @return 流程分页结果
     */
    @GetMapping("/processes/page")
    @Operation(summary = "分页查询审批流程", description = "根据条件分页查询审批流程列表")
    @SaCheckPermission("access:approval:query")
    public ResponseDTO<PageResult<ApprovalProcessVO>> queryProcessPage(@Valid ApprovalQueryForm queryForm) {
        log.info("开始查询审批流程，查询条件: {}", queryForm);

        try {
            PageResult<ApprovalProcessVO> result = approvalService.queryProcessPage(queryForm);
            log.info("审批流程查询成功，总数: {}", result.getTotal());
            return SmartResponseUtil.ok(result);
        } catch (Exception e) {
            log.error("审批流程查询失败，查询条件: {}", queryForm, e);
            return SmartResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 查询审批流程详情
     *
     * @param processId 流程ID
     * @return 流程详情
     */
    @GetMapping("/processes/{processId}")
    @Operation(summary = "查询审批流程详情", description = "根据流程ID查询详细的审批流程信息")
    @SaCheckPermission("access:approval:query")
    @RequireResource("access:approval:process:{processId}")
    public ResponseDTO<ApprovalProcessVO> getProcessDetail(
            @Parameter(description = "流程ID", required = true)
            @PathVariable @NotNull Long processId) {
        log.info("开始查询审批流程详情，流程ID: {}", processId);

        try {
            ApprovalProcessVO processVO = approvalService.getProcessDetail(processId);
            log.info("审批流程详情查询成功，流程ID: {}", processId);
            return SmartResponseUtil.ok(processVO);
        } catch (Exception e) {
            log.error("审批流程详情查询失败，流程ID: {}", processId, e);
            return SmartResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 审批处理
     *
     * @param processId       流程ID
     * @param approvalComment 审批意见
     * @param approved        是否通过
     * @return 操作结果
     */
    @PostMapping("/processes/{processId}/approve")
    @Operation(summary = "审批处理", description = "审批人对流程进行通过或拒绝处理")
    @SaCheckPermission("access:approval:approve")
    @RequireResource("access:approval:process:{processId}")
    public ResponseDTO<String> approveProcess(
            @Parameter(description = "流程ID", required = true)
            @PathVariable @NotNull Long processId,
            @Parameter(description = "审批意见", required = true)
            @RequestParam @NotNull String approvalComment,
            @Parameter(description = "是否通过", required = true)
            @RequestParam @NotNull Boolean approved) {
        log.info("开始处理审批，流程ID: {}, 审批结果: {}", processId, approved ? "通过" : "拒绝");

        try {
            approvalService.approveProcess(processId, approvalComment, approved);
            String message = approved ? "审批通过" : "审批拒绝";
            log.info("审批处理成功，流程ID: {}, 结果: {}", processId, message);
            return SmartResponseUtil.ok(message);
        } catch (Exception e) {
            log.error("审批处理失败，流程ID: {}, 审批结果: {}", processId, approved, e);
            return SmartResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 查询待审批流程
     *
     * @param approverId 审批人ID（可选，为空时查询当前用户）
     * @return 待审批流程列表
     */
    @GetMapping("/processes/pending")
    @Operation(summary = "查询待审批流程", description = "查询指定审批人的待审批流程列表")
    @SaCheckPermission("access:approval:approve")
    public ResponseDTO<List<ApprovalProcessVO>> queryPendingProcesses(
            @Parameter(description = "审批人ID")
            @RequestParam(required = false) Long approverId) {
        log.info("开始查询待审批流程，审批人ID: {}", approverId);

        try {
            List<ApprovalProcessVO> processes = approvalService.queryPendingProcesses(approverId);
            log.info("待审批流程查询成功，数量: {}", processes.size());
            return SmartResponseUtil.ok(processes);
        } catch (Exception e) {
            log.error("待审批流程查询失败，审批人ID: {}", approverId, e);
            return SmartResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 查询我的申请
     *
     * @param applicantId 申请人ID（可选，为空时查询当前用户）
     * @param status       状态（可选）
     * @return 申请列表
     */
    @GetMapping("/processes/my-applications")
    @Operation(summary = "查询我的申请", description = "查询用户提交的审批申请列表")
    @SaCheckPermission("access:approval:query")
    public ResponseDTO<List<ApprovalProcessVO>> queryMyApplications(
            @Parameter(description = "申请人ID")
            @RequestParam(required = false) Long applicantId,
            @Parameter(description = "申请状态")
            @RequestParam(required = false) String status) {
        log.info("开始查询我的申请，申请人ID: {}, 状态: {}", applicantId, status);

        try {
            List<ApprovalProcessVO> applications = approvalService.queryMyApplications(applicantId, status);
            log.info("我的申请查询成功，数量: {}", applications.size());
            return SmartResponseUtil.ok(applications);
        } catch (Exception e) {
            log.error("我的申请查询失败，申请人ID: {}, 状态: {}", applicantId, status, e);
            return SmartResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 撤销申请
     *
     * @param processId 流程ID
     * @return 操作结果
     */
    @PostMapping("/processes/{processId}/withdraw")
    @Operation(summary = "撤销申请", description = "申请人撤销待审批的申请")
    @SaCheckPermission("access:approval:withdraw")
    @RequireResource("access:approval:process:{processId}")
    public ResponseDTO<String> withdrawApplication(
            @Parameter(description = "流程ID", required = true)
            @PathVariable @NotNull Long processId) {
        log.info("开始撤销申请，流程ID: {}", processId);

        try {
            approvalService.withdrawApplication(processId);
            log.info("申请撤销成功，流程ID: {}", processId);
            return SmartResponseUtil.ok("申请已撤销");
        } catch (Exception e) {
            log.error("申请撤销失败，流程ID: {}", processId, e);
            return SmartResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 获取审批统计信息
     *
     * @param applicantId 申请人ID（可选）
     * @param approverId  审批人ID（可选）
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取审批统计信息", description = "获取审批流程的统计数据")
    @SaCheckPermission("access:approval:statistics")
    public ResponseDTO<ApprovalStatisticsVO> getApprovalStatistics(
            @Parameter(description = "申请人ID")
            @RequestParam(required = false) Long applicantId,
            @Parameter(description = "审批人ID")
            @RequestParam(required = false) Long approverId) {
        log.info("开始获取审批统计，申请人ID: {}, 审批人ID: {}", applicantId, approverId);

        try {
            ApprovalStatisticsVO statistics = approvalService.getApprovalStatistics(applicantId, approverId);
            log.info("审批统计获取成功");
            return SmartResponseUtil.ok(statistics);
        } catch (Exception e) {
            log.error("审批统计获取失败，申请人ID: {}, 审批人ID: {}", applicantId, approverId, e);
            return SmartResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 获取审批状态选项
     *
     * @return 状态列表
     */
    @GetMapping("/status-options")
    @Operation(summary = "获取审批状态选项", description = "获取所有可用的审批状态选项")
    @SaCheckPermission("access:approval:query")
    public ResponseDTO<List<ApprovalStatusVO>> getStatusOptions() {
        log.info("开始获取审批状态选项");

        try {
            List<ApprovalStatusVO> statusOptions = approvalService.getStatusOptions();
            log.info("审批状态选项获取成功，数量: {}", statusOptions.size());
            return SmartResponseUtil.ok(statusOptions);
        } catch (Exception e) {
            log.error("审批状态选项获取失败", e);
            return SmartResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 删除审批流程
     *
     * @param processId 流程ID
     * @return 操作结果
     */
    @DeleteMapping("/processes/{processId}")
    @Operation(summary = "删除审批流程", description = "软删除审批流程（仅管理员可操作）")
    @SaCheckPermission("access:approval:delete")
    @RequireResource("access:approval:process:{processId}")
    public ResponseDTO<String> deleteProcess(
            @Parameter(description = "流程ID", required = true)
            @PathVariable @NotNull Long processId) {
        log.info("开始删除审批流程，流程ID: {}", processId);

        try {
            approvalService.deleteProcess(processId);
            log.info("审批流程删除成功，流程ID: {}", processId);
            return SmartResponseUtil.ok("流程已删除");
        } catch (Exception e) {
            log.error("审批流程删除失败，流程ID: {}", processId, e);
            return SmartResponseUtil.error(e.getMessage());
        }
    }
}