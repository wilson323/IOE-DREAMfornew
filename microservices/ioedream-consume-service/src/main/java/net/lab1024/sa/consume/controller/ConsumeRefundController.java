package net.lab1024.sa.consume.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.consume.domain.form.ConsumeRefundAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeRefundQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeRefundRecordVO;
import net.lab1024.sa.consume.service.ConsumeRefundService;
import lombok.extern.slf4j.Slf4j;

/**
 * 退款管理控制器
 * <p>
 * 提供退款记录的管理功能，包括：
 * 1. 退款申请管理
 * 2. 退款审批流程
 * 3. 退款统计分析
 * 4. 退款记录查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@RestController
@PermissionCheck(value = "CONSUME_REFUND_MANAGE", description = "退款管理权限")
@RequestMapping("/api/v1/consume/refunds")
@Tag(name = "退款管理", description = "退款申请、审批、记录查询等功能")
public class ConsumeRefundController {

    @Resource
    private ConsumeRefundService consumeRefundService;

    /**
     * 分页查询退款记录
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @GetMapping("/query")
    @Operation(summary = "分页查询退款记录", description = "根据条件分页查询退款记录")
    public ResponseDTO<PageResult<ConsumeRefundRecordVO>> queryRefundRecords(ConsumeRefundQueryForm queryForm) {
        PageResult<ConsumeRefundRecordVO> result = consumeRefundService.queryRefundRecords(queryForm);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取退款记录详情
     *
     * @param refundId 退款ID
     * @return 记录详情
     */
    @GetMapping("/{refundId}")
    @Operation(summary = "获取退款记录详情", description = "根据退款ID获取详细的退款信息")
    public ResponseDTO<ConsumeRefundRecordVO> getRefundRecordDetail(
            @Parameter(description = "退款ID", required = true) @PathVariable Long refundId) {
        ConsumeRefundRecordVO record = consumeRefundService.getRefundRecordDetail(refundId);
        return ResponseDTO.ok(record);
    }

    /**
     * 创建退款申请
     *
     * @param addForm 退款申请表单
     * @return 申请结果
     */
    @PostMapping("/apply")
    @Operation(summary = "创建退款申请", description = "创建新的退款申请")
    public ResponseDTO<Long> applyRefund(@Valid @RequestBody ConsumeRefundAddForm addForm) {
        Long refundId = consumeRefundService.applyRefund(addForm);
        return ResponseDTO.ok(refundId);
    }

    /**
     * 审批退款申请
     *
     * @param refundId      退款ID
     * @param approved      是否批准
     * @param approveReason 审批意见
     * @return 审批结果
     */
    @PostMapping("/{refundId}/approve")
    @Operation(summary = "审批退款申请", description = "管理员审批退款申请")
    public ResponseDTO<Void> approveRefund(
            @Parameter(description = "退款ID", required = true) @PathVariable Long refundId,
            @Parameter(description = "是否批准", required = true) @RequestParam Boolean approved,
            @Parameter(description = "审批意见", required = true) @RequestParam String approveReason) {
        consumeRefundService.approveRefund(refundId, approved, approveReason);
        return ResponseDTO.ok();
    }

    /**
     * 批量审批退款申请
     *
     * @param refundIds     退款ID列表
     * @param approved      是否批准
     * @param approveReason 审批意见
     * @return 批量审批结果
     */
    @PostMapping("/batch/approve")
    @Operation(summary = "批量审批退款", description = "批量审批多个退款申请")
    public ResponseDTO<java.util.Map<String, Object>> batchApproveRefunds(
            @Parameter(description = "退款ID列表", required = true) @RequestParam List<Long> refundIds,
            @Parameter(description = "是否批准", required = true) @RequestParam Boolean approved,
            @Parameter(description = "审批意见", required = true) @RequestParam String approveReason) {
        java.util.Map<String, Object> result = consumeRefundService.batchApproveRefunds(refundIds, approved,
                approveReason);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取用户的退款记录
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 退款记录列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户退款记录", description = "获取指定用户的退款记录")
    public ResponseDTO<PageResult<ConsumeRefundRecordVO>> getUserRefundRecords(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        ConsumeRefundQueryForm queryForm = new ConsumeRefundQueryForm();
        queryForm.setUserId(userId);
        queryForm.setPageNum(pageNum);
        queryForm.setPageSize(pageSize);

        PageResult<ConsumeRefundRecordVO> result = consumeRefundService.queryRefundRecords(queryForm);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取待审批的退款申请
     *
     * @return 待审批申请列表
     */
    @GetMapping("/pending")
    @Operation(summary = "获取待审批申请", description = "获取所有待审批的退款申请")
    public ResponseDTO<List<ConsumeRefundRecordVO>> getPendingRefunds() {
        List<ConsumeRefundRecordVO> pendingRefunds = consumeRefundService.getPendingRefunds();
        return ResponseDTO.ok(pendingRefunds);
    }

    /**
     * 获取退款统计信息
     *
     * @param userId    用户ID（可选）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取退款统计", description = "获取指定时间段的退款统计信息")
    public ResponseDTO<java.util.Map<String, Object>> getRefundStatistics(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        java.util.Map<String, Object> statistics = consumeRefundService.getRefundStatistics(userId, startDate, endDate);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 退款记录执行
     *
     * @param refundId 退款ID
     * @return 执行结果
     */
    @PostMapping("/{refundId}/execute")
    @Operation(summary = "执行退款", description = "执行已批准的退款")
    public ResponseDTO<Boolean> executeRefund(
            @Parameter(description = "退款ID", required = true) @PathVariable Long refundId) {
        boolean result = consumeRefundService.executeRefund(refundId);
        return ResponseDTO.ok(result);
    }

    /**
     * 取消退款申请
     *
     * @param refundId     退款ID
     * @param cancelReason 取消原因
     * @return 取消结果
     */
    @PostMapping("/{refundId}/cancel")
    @Operation(summary = "取消退款申请", description = "取消用户的退款申请")
    public ResponseDTO<Void> cancelRefund(
            @Parameter(description = "退款ID", required = true) @PathVariable Long refundId,
            @Parameter(description = "取消原因", required = true) @RequestParam String cancelReason) {
        consumeRefundService.cancelRefund(refundId, cancelReason);
        return ResponseDTO.ok();
    }

    /**
     * 重新提交退款申请
     *
     * @param refundId 退款ID
     * @return 重新提交结果
     */
    @PostMapping("/{refundId}/resubmit")
    @Operation(summary = "重新提交申请", description = "重新提交被拒绝的退款申请")
    public ResponseDTO<Void> resubmitRefund(
            @Parameter(description = "退款ID", required = true) @PathVariable Long refundId) {
        consumeRefundService.resubmitRefund(refundId);
        return ResponseDTO.ok();
    }

    /**
     * 导出退款记录
     *
     * @param queryForm 查询条件
     * @return 导出结果
     */
    @PostMapping("/export")
    @Operation(summary = "导出退款记录", description = "导出退款记录到Excel文件")
    public ResponseDTO<String> exportRefundRecords(@RequestBody ConsumeRefundQueryForm queryForm) {
        String downloadUrl = consumeRefundService.exportRefundRecords(queryForm);
        return ResponseDTO.ok(downloadUrl);
    }

    /**
     * 获取退款趋势数据
     *
     * @param days 天数（默认30天）
     * @return 趋势数据
     */
    @GetMapping("/trend")
    @Operation(summary = "获取退款趋势", description = "获取退款金额和数量趋势数据")
    public ResponseDTO<java.util.Map<String, Object>> getRefundTrend(
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        java.util.Map<String, Object> trend = consumeRefundService.getRefundTrend(days);
        return ResponseDTO.ok(trend);
    }

    /**
     * 退款记录查询
     *
     * @param recordId 原消费记录ID
     * @return 退款记录列表
     */
    @GetMapping("/by-record/{recordId}")
    @Operation(summary = "根据原记录查询", description = "根据原消费记录ID查询相关退款记录")
    public ResponseDTO<List<ConsumeRefundRecordVO>> getRefundsByRecordId(
            @Parameter(description = "原消费记录ID", required = true) @PathVariable Long recordId) {
        List<ConsumeRefundRecordVO> refunds = consumeRefundService.getRefundsByRecordId(recordId);
        return ResponseDTO.ok(refunds);
    }
}
