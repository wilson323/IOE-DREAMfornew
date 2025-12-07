package net.lab1024.sa.access.approval.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.approval.service.AccessApprovalService;
import net.lab1024.sa.access.approval.domain.form.ApprovalApplicationForm;
import net.lab1024.sa.access.approval.domain.form.VisitorReservationForm;
import net.lab1024.sa.access.approval.domain.vo.ApprovalStatusVO;
import net.lab1024.sa.access.approval.domain.vo.VisitorInfoVO;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门禁审批控制器
 * 严格遵循四层架构规范：
 * - Controller层只负责参数验证和调用Service
 * - 使用统一响应格式ResponseDTO
 * - 权限控制注解@SaCheckPermission
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/api/access/approval")
@Slf4j
public class AccessApprovalController {

    @Resource
    private AccessApprovalService accessApprovalService;

    /**
     * 提交权限申请
     *
     * @param form 权限申请表单
     * @return 申请结果
     */
    @PostMapping("/apply")
    @SaCheckPermission("access:approval:apply")
    public ResponseDTO<String> submitApplication(@Valid @RequestBody ApprovalApplicationForm form) {
        log.info("[AccessApprovalController] 提交权限申请: userId={}, accessType={}",
                form.getApplicantId(), form.getAccessType());

        String result = accessApprovalService.submitApplication(form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 提交临时权限申请（通过网关调用访客微服务）
     *
     * @param form 权限申请表单
     * @return 申请结果
     */
    @PostMapping("/temporary")
    @SaCheckPermission("access:approval:temporary")
    public ResponseDTO<String> submitTemporaryApplication(@Valid @RequestBody ApprovalApplicationForm form) {
        log.info("[AccessApprovalController] 提交临时权限申请: userId={}, accessType={}",
                form.getApplicantId(), form.getAccessType());

        String result = accessApprovalService.submitTemporaryApplication(form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 审批处理
     *
     * @param processId 流程ID
     * @param approved 是否批准
     * @param comment 审批意见
     * @return 审批结果
     */
    @PostMapping("/process/{processId}/approve")
    @SaCheckPermission("access:approval:process")
    public ResponseDTO<String> processApproval(@PathVariable Long processId,
                                              @RequestParam Boolean approved,
                                              @RequestParam(required = false) String comment) {
        log.info("[AccessApprovalController] 审批处理: processId={}, approved={}", processId, approved);

        String result = accessApprovalService.processApproval(processId, approved, comment);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取我的申请列表
     *
     * @param applicantId 申请人ID
     * @return 申请列表
     */
    @GetMapping("/my-applications")
    @SaCheckPermission("access:approval:list")
    public ResponseDTO<List<ApprovalStatusVO>> getMyApplications(@RequestParam Long applicantId) {
        log.debug("[AccessApprovalController] 获取我的申请列表: applicantId={}", applicantId);

        List<ApprovalStatusVO> applications = accessApprovalService.getMyApplications(applicantId);
        return ResponseDTO.userOk(applications);
    }

    /**
     * 获取待我审批列表
     *
     * @param approverId 审批人ID
     * @return 待审批列表
     */
    @GetMapping("/pending-approvals")
    @SaCheckPermission("access:approval:pending")
    public ResponseDTO<List<ApprovalStatusVO>> getPendingApprovals(@RequestParam Long approverId) {
        log.debug("[AccessApprovalController] 获取待我审批列表: approverId={}", approverId);

        List<ApprovalStatusVO> pendingApprovals = accessApprovalService.getPendingApprovals(approverId);
        return ResponseDTO.userOk(pendingApprovals);
    }

    /**
     * 获取临时权限申请列表
     *
     * @param applicantId 申请人ID
     * @return 临时权限列表
     */
    @GetMapping("/temporary/list")
    @SaCheckPermission("access:approval:temporary:list")
    public ResponseDTO<List<ApprovalStatusVO>> getTemporaryApplications(@RequestParam Long applicantId) {
        log.debug("[AccessApprovalController] 获取临时权限申请列表: applicantId={}", applicantId);

        List<ApprovalStatusVO> applications = accessApprovalService.getTemporaryApplications(applicantId);
        return ResponseDTO.userOk(applications);
    }

    /**
     * 紧急权限申请
     *
     * @param form 紧急权限申请表单
     * @return 申请结果
     */
    @PostMapping("/emergency")
    @SaCheckPermission("access:approval:emergency")
    public ResponseDTO<String> submitEmergencyApplication(@Valid @RequestBody ApprovalApplicationForm form) {
        log.warn("[AccessApprovalController] 紧急权限申请: userId={}, reason={}",
                form.getApplicantId(), form.getApplicationReason());

        String result = accessApprovalService.submitEmergencyApplication(form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 撤回申请
     *
     * @param processId 流程ID
     * @param userId 用户ID
     * @return 撤回结果
     */
    @PostMapping("/process/{processId}/withdraw")
    @SaCheckPermission("access:approval:withdraw")
    public ResponseDTO<String> withdrawApplication(@PathVariable Long processId,
                                                  @RequestParam Long userId) {
        log.info("[AccessApprovalController] 撤回申请: processId={}, userId={}", processId, userId);

        String result = accessApprovalService.withdrawApplication(processId, userId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取审批详情
     *
     * @param processId 流程ID
     * @return 审批详情
     */
    @GetMapping("/process/{processId}/detail")
    @SaCheckPermission("access:approval:detail")
    public ResponseDTO<ApprovalStatusVO> getApprovalDetail(@PathVariable Long processId) {
        log.debug("[AccessApprovalController] 获取审批详情: processId={}", processId);

        ApprovalStatusVO detail = accessApprovalService.getApprovalDetail(processId);
        return ResponseDTO.userOk(detail);
    }

    /**
     * 获取审批历史
     *
     * @param userId 用户ID
     * @return 审批历史
     */
    @GetMapping("/history")
    @SaCheckPermission("access:approval:history")
    public ResponseDTO<List<ApprovalStatusVO>> getApprovalHistory(@RequestParam Long userId) {
        log.debug("[AccessApprovalController] 获取审批历史: userId={}", userId);

        List<ApprovalStatusVO> history = accessApprovalService.getApprovalHistory(userId);
        return ResponseDTO.userOk(history);
    }
}