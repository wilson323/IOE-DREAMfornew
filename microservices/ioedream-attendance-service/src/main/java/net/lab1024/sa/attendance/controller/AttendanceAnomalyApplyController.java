package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.attendance.AttendanceAnomalyApplyEntity;
import net.lab1024.sa.attendance.service.AttendanceAnomalyApplyService;
import net.lab1024.sa.attendance.service.AttendanceAnomalyApprovalService;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤异常申请Controller
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/anomaly-apply")
@Tag(name = "考勤异常申请管理")
public class AttendanceAnomalyApplyController {

    private final AttendanceAnomalyApplyService applyService;
    private final AttendanceAnomalyApprovalService approvalService;

    public AttendanceAnomalyApplyController(AttendanceAnomalyApplyService applyService,
                                             AttendanceAnomalyApprovalService approvalService) {
        this.applyService = applyService;
        this.approvalService = approvalService;
    }

    /**
     * 提交补卡申请
     */
    @PostMapping("/supplement-card")
    @Operation(summary = "提交补卡申请")
    public ResponseDTO<Long> submitSupplementCardApply(@RequestBody AttendanceAnomalyApplyEntity apply) {
        log.info("[异常申请] 提交补卡申请: userId={}, date={}, type={}",
                apply.getApplicantId(), apply.getAttendanceDate(), apply.getPunchType());

        Long applyId = applyService.createSupplementCardApply(apply);
        return ResponseDTO.ok(applyId);
    }

    /**
     * 提交迟到说明申请
     */
    @PostMapping("/late-explanation")
    @Operation(summary = "提交迟到说明申请")
    public ResponseDTO<Long> submitLateExplanationApply(@RequestBody AttendanceAnomalyApplyEntity apply) {
        log.info("[异常申请] 提交迟到说明: userId={}, date={}", apply.getApplicantId(), apply.getAttendanceDate());

        Long applyId = applyService.createLateExplanationApply(apply);
        return ResponseDTO.ok(applyId);
    }

    /**
     * 提交早退说明申请
     */
    @PostMapping("/early-explanation")
    @Operation(summary = "提交早退说明申请")
    public ResponseDTO<Long> submitEarlyExplanationApply(@RequestBody AttendanceAnomalyApplyEntity apply) {
        log.info("[异常申请] 提交早退说明: userId={}, date={}", apply.getApplicantId(), apply.getAttendanceDate());

        Long applyId = applyService.createEarlyExplanationApply(apply);
        return ResponseDTO.ok(applyId);
    }

    /**
     * 提交旷工申诉申请
     */
    @PostMapping("/absent-appeal")
    @Operation(summary = "提交旷工申诉申请")
    public ResponseDTO<Long> submitAbsentAppealApply(@RequestBody AttendanceAnomalyApplyEntity apply) {
        log.info("[异常申请] 提交旷工申诉: userId={}, date={}", apply.getApplicantId(), apply.getAttendanceDate());

        Long applyId = applyService.createAbsentAppealApply(apply);
        return ResponseDTO.ok(applyId);
    }

    /**
     * 撤销申请
     */
    @PutMapping("/{applyId}/cancel")
    @Operation(summary = "撤销申请")
    public ResponseDTO<Void> cancelApply(@PathVariable Long applyId,
                                         @RequestParam Long userId) {
        log.info("[异常申请] 撤销申请: applyId={}, userId={}", applyId, userId);

        Boolean result = applyService.cancelApply(applyId, userId);
        return ResponseDTO.ok();
    }

    /**
     * 查询申请详情
     */
    @GetMapping("/{applyId}")
    @Operation(summary = "查询申请详情")
    public ResponseDTO<AttendanceAnomalyApplyEntity> getApplyDetail(@PathVariable Long applyId) {
        log.info("[异常申请] 查询申请详情: applyId={}", applyId);

        AttendanceAnomalyApplyEntity apply = applyService.getApplyById(applyId);
        return ResponseDTO.ok(apply);
    }

    /**
     * 查询我的申请列表
     */
    @GetMapping("/my-applies")
    @Operation(summary = "查询我的申请列表")
    public ResponseDTO<List<AttendanceAnomalyApplyEntity>> getMyApplies(@RequestParam Long userId) {
        log.info("[异常申请] 查询我的申请: userId={}", userId);

        List<AttendanceAnomalyApplyEntity> applies = applyService.getAppliesByUserId(userId);
        return ResponseDTO.ok(applies);
    }

    /**
     * 查询待审批的申请列表（管理员）
     */
    @GetMapping("/pending")
    @Operation(summary = "查询待审批的申请列表")
    public ResponseDTO<List<AttendanceAnomalyApplyEntity>> getPendingApplies() {
        log.info("[异常申请] 查询待审批申请列表");

        List<AttendanceAnomalyApplyEntity> applies = applyService.getPendingApplies();
        return ResponseDTO.ok(applies);
    }

    /**
     * 批准申请
     */
    @PutMapping("/{applyId}/approve")
    @Operation(summary = "批准申请")
    public ResponseDTO<Void> approveApply(@PathVariable Long applyId,
                                          @RequestParam Long approverId,
                                          @RequestParam String approverName,
                                          @RequestParam(required = false) String comment) {
        log.info("[异常审批] 批准申请: applyId={}, approverId={}, approverName={}",
                applyId, approverId, approverName);

        Boolean result = approvalService.approveApply(applyId, approverId, approverName, comment);
        return ResponseDTO.ok();
    }

    /**
     * 驳回申请
     */
    @PutMapping("/{applyId}/reject")
    @Operation(summary = "驳回申请")
    public ResponseDTO<Void> rejectApply(@PathVariable Long applyId,
                                         @RequestParam Long approverId,
                                         @RequestParam String approverName,
                                         @RequestParam String comment) {
        log.info("[异常审批] 驳回申请: applyId={}, approverId={}, reason={}",
                applyId, approverId, comment);

        Boolean result = approvalService.rejectApply(applyId, approverId, approverName, comment);
        return ResponseDTO.ok();
    }

    /**
     * 批量审批
     */
    @PutMapping("/batch-approve")
    @Operation(summary = "批量审批")
    public ResponseDTO<Integer> batchApprove(@RequestBody BatchApproveRequest request) {
        log.info("[异常审批] 批量审批: count={}, approverId={}, approve={}",
                request.getApplyIds().length, request.getApproverId(), request.getApprove());

        Integer count = approvalService.batchApprove(
                request.getApplyIds(),
                request.getApproverId(),
                request.getApproverName(),
                request.getComment(),
                request.getApprove()
        );

        return ResponseDTO.ok(count);
    }

    /**
     * 检查是否允许补卡
     */
    @GetMapping("/check-supplement-allowed")
    @Operation(summary = "检查是否允许补卡")
    public ResponseDTO<Boolean> checkSupplementAllowed(@RequestParam Long userId,
                                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("[异常申请] 检查补卡权限: userId={}, date={}", userId, date);

        Boolean allowed = applyService.checkSupplementCardAllowed(userId, date);
        return ResponseDTO.ok(allowed);
    }

    /**
     * 批量审批请求体
     */
    public static class BatchApproveRequest {
        private Long[] applyIds;
        private Long approverId;
        private String approverName;
        private String comment;
        private Boolean approve;

        public Long[] getApplyIds() {
            return applyIds;
        }

        public void setApplyIds(Long[] applyIds) {
            this.applyIds = applyIds;
        }

        public Long getApproverId() {
            return approverId;
        }

        public void setApproverId(Long approverId) {
            this.approverId = approverId;
        }

        public String getApproverName() {
            return approverName;
        }

        public void setApproverName(String approverName) {
            this.approverName = approverName;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Boolean getApprove() {
            return approve;
        }

        public void setApprove(Boolean approve) {
            this.approve = approve;
        }
    }
}
