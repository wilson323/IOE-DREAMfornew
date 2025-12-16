package net.lab1024.sa.consume.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.RefundQueryForm;
import net.lab1024.sa.consume.domain.form.RefundRequestForm;
import net.lab1024.sa.consume.domain.vo.RefundRecordVO;
import net.lab1024.sa.consume.service.ConsumeRefundService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 消费退款控制器
 * <p>
 * 提供完整的退款功能，确保与Smart-Admin前端100%兼容
 * 包括退款申请、审批、处理、查询等核心功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/consume/refund")
@Tag(name = "消费退款管理", description = "消费退款相关接口")
@Validated
public class ConsumeRefundController {

    @Resource
    private ConsumeRefundService refundService;

    /**
     * 申请退款
     */
    @PostMapping("/apply")
    @Observed(name = "consumeRefund.applyRefund", contextualName = "consume-refund-apply-refund")
    @Operation(summary = "申请退款", description = "为消费记录申请退款")
    public ResponseDTO<Long> applyRefund(@Valid @RequestBody RefundRequestForm refundRequest) {
        Long refundId = refundService.applyRefund(refundRequest);
        return ResponseDTO.ok(refundId);
    }

    /**
     * 根据交易号申请退款
     */
    @PostMapping("/transaction/{transactionNo}")
    @Observed(name = "consumeRefund.refundByTransactionNo", contextualName = "consume-refund-refund-by-transaction-no")
    @Operation(summary = "根据交易号申请退款", description = "根据消费交易号快速申请退款")
    public ResponseDTO<Long> refundByTransactionNo(
            @Parameter(description = "交易号", example = "TX202512090001") @PathVariable @NotBlank String transactionNo,
            @Parameter(description = "退款金额", example = "50.00") @RequestParam @NotNull BigDecimal amount,
            @Parameter(description = "退款原因", example = "误操作") @RequestParam @NotBlank String reason,
            @Parameter(description = "退款说明") @RequestParam(defaultValue = "") String description) {

        RefundRequestForm refundRequest = new RefundRequestForm();
        refundRequest.setTransactionNo(transactionNo);
        refundRequest.setRefundAmount(amount);
        refundRequest.setRefundReason(reason);
        refundRequest.setDescription(description);

        Long refundId = refundService.applyRefund(refundRequest);
        return ResponseDTO.ok(refundId);
    }

    /**
     * 获取退款记录详情
     */
    @GetMapping("/{refundId}/detail")
    @Observed(name = "consumeRefund.getRefundDetail", contextualName = "consume-refund-get-refund-detail")
    @Operation(summary = "获取退款记录详情", description = "根据退款ID获取详细信息")
    public ResponseDTO<RefundRecordVO> getRefundDetail(
            @Parameter(description = "退款ID", example = "1") @PathVariable Long refundId) {
        RefundRecordVO refundRecord = refundService.getRefundDetail(refundId);
        return ResponseDTO.ok(refundRecord);
    }

    /**
     * 分页查询退款记录
     */
    @GetMapping("/list")
    @Observed(name = "consumeRefund.getRefundList", contextualName = "consume-refund-get-refund-list")
    @Operation(summary = "分页查询退款记录", description = "支持多条件查询")
    public ResponseDTO<PageResult<RefundRecordVO>> getRefundList(RefundQueryForm queryForm) {
        PageResult<RefundRecordVO> pageResult = refundService.getRefundPage(queryForm);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 获取用户的退款记录
     */
    @GetMapping("/user/{userId}")
    @Observed(name = "consumeRefund.getUserRefundRecords", contextualName = "consume-refund-get-user-refund-records")
    @Operation(summary = "获取用户的退款记录", description = "分页查询指定用户的退款记录")
    public ResponseDTO<PageResult<RefundRecordVO>> getUserRefundRecords(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {

        RefundQueryForm queryForm = new RefundQueryForm();
        queryForm.setUserId(userId);
        queryForm.setPageNum(pageNum);
        queryForm.setPageSize(pageSize);

        PageResult<RefundRecordVO> pageResult = refundService.getRefundPage(queryForm);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 审批退款申请
     */
    @PostMapping("/batch/apply")
    @Observed(name = "consumeRefund.batchApplyRefund", contextualName = "consume-refund-batch-apply-refund")
    @Operation(summary = "批量申请退款", description = "批量申请多个消费记录的退款")
    public ResponseDTO<Integer> batchApplyRefund(
            @Parameter(description = "交易号列表") @RequestParam @NotNull List<String> transactionNos,
            @Parameter(description = "退款原因", example = "批量误操作") @RequestParam @NotBlank String reason) {
        int successCount = refundService.batchApplyRefund(transactionNos, reason);
        return ResponseDTO.ok(successCount);
    }

    /**
     * 审批退款申请（带金额）
     */
    @PostMapping("/batch/apply/amount")
    @Observed(name = "consumeRefund.batchApplyRefundWithAmount", contextualName = "consume-refund-batch-apply-refund-with-amount")
    @Operation(summary = "批量申请退款（带金额）", description = "批量申请退款，可指定每个交易的退款金额")
    public ResponseDTO<Integer> batchApplyRefundWithAmount(
            @Parameter(description = "退款申请信息") @RequestBody @NotNull List<Map<String, Object>> refundRequests) {
        int successCount = refundService.batchApplyRefundWithAmount(refundRequests);
        return ResponseDTO.ok(successCount);
    }

    /**
     * 审批退款申请
     */
    @PostMapping("/{refundId}/approve")
    @Observed(name = "consumeRefund.approveRefund", contextualName = "consume-refund-approve-refund")
    @Operation(summary = "审批退款申请", description = "审批退款申请（通过/拒绝）")
    public ResponseDTO<String> approveRefund(
            @Parameter(description = "退款ID", example = "1") @PathVariable Long refundId,
            @Parameter(description = "审批结果：true-通过，false-拒绝") @RequestParam @NotNull Boolean approved,
            @Parameter(description = "审批意见", example = "同意退款") @RequestParam(defaultValue = "") String comment) {
        boolean result = refundService.approveRefund(refundId, approved, comment);
        if (result) {
            return ResponseDTO.ok(approved ? "退款审批通过" : "退款审批拒绝");
        } else {
            return ResponseDTO.error(500, "退款审批失败");
        }
    }

    /**
     * 批量审批退款申请
     */
    @PostMapping("/batch/approve")
    @Observed(name = "consumeRefund.batchApproveRefund", contextualName = "consume-refund-batch-approve-refund")
    @Operation(summary = "批量审批退款申请", description = "批量审批多个退款申请")
    public ResponseDTO<String> batchApproveRefund(
            @Parameter(description = "退款ID列表") @RequestParam @NotNull List<Long> refundIds,
            @Parameter(description = "审批结果：true-通过，false-拒绝") @RequestParam @NotNull Boolean approved,
            @Parameter(description = "审批意见") @RequestParam(defaultValue = "") String comment) {
        int successCount = refundService.batchApproveRefund(refundIds, approved, comment);
        return ResponseDTO.ok(String.format("批量审批完成，成功处理 %d 个退款申请", successCount));
    }

    /**
     * 取消退款申请
     */
    @PostMapping("/{refundId}/cancel")
    @Observed(name = "consumeRefund.cancelRefund", contextualName = "consume-refund-cancel-refund")
    @Operation(summary = "取消退款申请", description = "取消待审批的退款申请")
    public ResponseDTO<String> cancelRefund(
            @Parameter(description = "退款ID", example = "1") @PathVariable Long refundId,
            @Parameter(description = "取消原因", example = "不需要退款了") @RequestParam @NotBlank String reason) {
        boolean result = refundService.cancelRefund(refundId, reason);
        if (result) {
            return ResponseDTO.ok("退款申请已取消");
        } else {
            return ResponseDTO.error(500, "取消退款申请失败");
        }
    }

    /**
     * 处理退款（执行退款）
     */
    @PostMapping("/{refundId}/process")
    @Observed(name = "consumeRefund.processRefund", contextualName = "consume-refund-process-refund")
    @Operation(summary = "处理退款", description = "执行退款操作，将款项退还到账户")
    public ResponseDTO<String> processRefund(
            @Parameter(description = "退款ID", example = "1") @PathVariable Long refundId) {
        boolean result = refundService.processRefund(refundId);
        if (result) {
            return ResponseDTO.ok("退款处理成功");
        } else {
            return ResponseDTO.error(500, "退款处理失败");
        }
    }

    /**
     * 获取退款统计信息
     */
    @GetMapping("/statistics")
    @Observed(name = "consumeRefund.getRefundStatistics", contextualName = "consume-refund-get-refund-statistics")
    @Operation(summary = "获取退款统计信息", description = "获取退款总数、金额统计等")
    public ResponseDTO<Map<String, Object>> getRefundStatistics() {
        Map<String, Object> statistics = refundService.getRefundStatistics();
        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取用户退款统计
     */
    @GetMapping("/statistics/user/{userId}")
    @Observed(name = "consumeRefund.getUserRefundStatistics", contextualName = "consume-refund-get-user-refund-statistics")
    @Operation(summary = "获取用户退款统计", description = "获取指定用户的退款统计信息")
    public ResponseDTO<Map<String, Object>> getUserRefundStatistics(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        Map<String, Object> statistics = refundService.getUserRefundStatistics(userId);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取退款趋势数据
     */
    @GetMapping("/trend")
    @Observed(name = "consumeRefund.getRefundTrend", contextualName = "consume-refund-get-refund-trend")
    @Operation(summary = "获取退款趋势数据", description = "获取指定时间段的退款趋势")
    public ResponseDTO<Map<String, Object>> getRefundTrend(
            @Parameter(description = "开始时间", example = "2025-12-01") @RequestParam @NotBlank String startDate,
            @Parameter(description = "结束时间", example = "2025-12-31") @RequestParam @NotBlank String endDate,
            @Parameter(description = "统计类型：day-按天，week-按周，month-按月", example = "day") @RequestParam(defaultValue = "day") String statisticsType) {
        Map<String, Object> trend = refundService.getRefundTrend(startDate, endDate, statisticsType);
        return ResponseDTO.ok(trend);
    }

    /**
     * 导出退款数据
     */
    @GetMapping("/export")
    @Observed(name = "consumeRefund.exportRefundData", contextualName = "consume-refund-export-refund-data")
    @Operation(summary = "导出退款数据", description = "导出退款数据到Excel文件")
    public void exportRefundData(RefundQueryForm queryForm, HttpServletResponse response) {
        refundService.exportRefundData(queryForm, response);
    }

    /**
     * 检查退款状态
     */
    @GetMapping("/{refundId}/status")
    @Observed(name = "consumeRefund.checkRefundStatus", contextualName = "consume-refund-check-refund-status")
    @Operation(summary = "检查退款状态", description = "检查退款申请的处理状态")
    public ResponseDTO<Map<String, Object>> checkRefundStatus(
            @Parameter(description = "退款ID", example = "1") @PathVariable Long refundId) {
        Map<String, Object> statusInfo = refundService.checkRefundStatus(refundId);
        return ResponseDTO.ok(statusInfo);
    }

    /**
     * 获取可退款金额
     */
    @GetMapping("/transaction/{transactionNo}/available")
    @Observed(name = "consumeRefund.getAvailableRefundAmount", contextualName = "consume-refund-get-available-refund-amount")
    @Operation(summary = "获取可退款金额", description = "检查交易记录的可退款金额")
    public ResponseDTO<Map<String, Object>> getAvailableRefundAmount(
            @Parameter(description = "交易号", example = "TX202512090001") @PathVariable @NotBlank String transactionNo) {
        Map<String, Object> amountInfo = refundService.getAvailableRefundAmount(transactionNo);
        return ResponseDTO.ok(amountInfo);
    }

    /**
     * 退款记录查询（交易号）
     */
    @GetMapping("/transaction/{transactionNo}")
    @Observed(name = "consumeRefund.getRefundByTransactionNo", contextualName = "consume-refund-get-refund-by-transaction-no")
    @Operation(summary = "查询交易退款记录", description = "根据交易号查询相关的退款记录")
    public ResponseDTO<List<RefundRecordVO>> getRefundByTransactionNo(
            @Parameter(description = "交易号", example = "TX202512090001") @PathVariable @NotBlank String transactionNo) {
        List<RefundRecordVO> refunds = refundService.getRefundByTransactionNo(transactionNo);
        return ResponseDTO.ok(refunds);
    }

    /**
     * 验证退款申请
     */
    @PostMapping("/validate")
    @Observed(name = "consumeRefund.validateRefundRequest", contextualName = "consume-refund-validate-refund-request")
    @Operation(summary = "验证退款申请", description = "验证退款申请的合法性和可行性")
    public ResponseDTO<Map<String, Object>> validateRefundRequest(@Valid @RequestBody RefundRequestForm refundRequest) {
        Map<String, Object> validationResult = refundService.validateRefundRequest(refundRequest);
        return ResponseDTO.ok(validationResult);
    }

    /**
     * 获取退款政策信息
     */
    @GetMapping("/policy")
    @Observed(name = "consumeRefund.getRefundPolicy", contextualName = "consume-refund-get-refund-policy")
    @Operation(summary = "获取退款政策信息", description = "获取退款政策、时限、规则等信息")
    public ResponseDTO<Map<String, Object>> getRefundPolicy() {
        Map<String, Object> policyInfo = refundService.getRefundPolicy();
        return ResponseDTO.ok(policyInfo);
    }
}



