package net.lab1024.sa.consume.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.consume.domain.form.RefundApplyForm;
import net.lab1024.sa.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.entity.PaymentRefundRecordEntity;
import net.lab1024.sa.consume.service.PaymentService;

/**
 * 支付管理控制器
 * <p>
 * 企业级支付管理REST API，提供完整的支付、退款、对账等功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@Tag(name = "支付管理", description = "支付管理相关接口")
@PermissionCheck(value = "CONSUME_PAYMENT", description = "消费支付管理模块权限")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Operation(summary = "处理支付")
    @Observed(name = "payment.processPayment", contextualName = "payment-process-payment")
    @PostMapping("/process")
    public ResponseDTO<Map<String, Object>> processPayment(@Valid @RequestBody PaymentProcessForm form) {
        log.info("[支付API] 处理支付请求: userId={}, amount={}, method={}",
                form.getUserId(), form.getPaymentAmount(), form.getPaymentMethod());

        try {
            Map<String, Object> result = paymentService.processPayment(form);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 支付处理参数错误: userId={}, amount={}, error={}",
                    form.getUserId(), form.getPaymentAmount(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 支付处理业务异常: userId={}, amount={}, code={}, message={}",
                    form.getUserId(), form.getPaymentAmount(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 支付处理系统异常: userId={}, amount={}, code={}, message={}",
                    form.getUserId(), form.getPaymentAmount(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("PAYMENT_PROCESS_SYSTEM_ERROR", "支付处理失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 支付处理未知异常: userId={}, amount={}, error={}",
                    form.getUserId(), form.getPaymentAmount(), e.getMessage(), e);
            return ResponseDTO.error("PAYMENT_PROCESS_FAILED", "支付处理失败: " + e.getMessage());
        }
    }

    @Operation(summary = "申请退款")
    @Observed(name = "payment.applyRefund", contextualName = "payment-apply-refund")
    @PostMapping("/refund/apply")
    public ResponseDTO<Map<String, Object>> applyRefund(@Valid @RequestBody RefundApplyForm form) {
        log.info("[支付API] 申请退款: paymentId={}, amount={}, reason={}",
                form.getPaymentId(), form.getRefundAmount(), form.getRefundReasonDesc());

        try {
            Map<String, Object> result = paymentService.applyRefund(form);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 退款申请参数错误: paymentId={}, amount={}, error={}",
                    form.getPaymentId(), form.getRefundAmount(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 退款申请业务异常: paymentId={}, amount={}, code={}, message={}",
                    form.getPaymentId(), form.getRefundAmount(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 退款申请系统异常: paymentId={}, amount={}, code={}, message={}",
                    form.getPaymentId(), form.getRefundAmount(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("REFUND_APPLY_SYSTEM_ERROR", "退款申请失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 退款申请未知异常: paymentId={}, amount={}, error={}",
                    form.getPaymentId(), form.getRefundAmount(), e.getMessage(), e);
            return ResponseDTO.error("REFUND_APPLY_FAILED", "退款申请失败: " + e.getMessage());
        }
    }

    @Operation(summary = "审核退款")
    @Observed(name = "payment.auditRefund", contextualName = "payment-audit-refund")
    @PutMapping("/refund/{refundId}/audit")
    public ResponseDTO<Map<String, Object>> auditRefund(
            @Parameter(description = "退款记录ID", required = true) @PathVariable String refundId,
            @Parameter(description = "审核状态", required = true) @RequestParam Integer auditStatus,
            @Parameter(description = "审核意见") @RequestParam(required = false) String auditComment) {

        log.info("[支付API] 审核退款: refundId={}, status={}", refundId, auditStatus);

        try {
            Map<String, Object> result = paymentService.auditRefund(refundId, auditStatus, auditComment);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 退款审核参数错误: refundId={}, status={}, error={}",
                    refundId, auditStatus, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 退款审核业务异常: refundId={}, status={}, code={}, message={}",
                    refundId, auditStatus, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 退款审核系统异常: refundId={}, status={}, code={}, message={}",
                    refundId, auditStatus, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("REFUND_AUDIT_SYSTEM_ERROR", "退款审核失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 退款审核未知异常: refundId={}, status={}, error={}",
                    refundId, auditStatus, e.getMessage(), e);
            return ResponseDTO.error("REFUND_AUDIT_FAILED", "退款审核失败: " + e.getMessage());
        }
    }

    @Operation(summary = "执行退款")
    @Observed(name = "payment.executeRefund", contextualName = "payment-execute-refund")
    @PostMapping("/refund/execute")
    public ResponseDTO<Map<String, Object>> executeRefund(
            @Parameter(description = "退款记录ID", required = true) @RequestParam String refundId) {

        log.info("[支付API] 执行退款: refundId={}", refundId);

        try {
            Map<String, Object> result = paymentService.executeRefund(refundId);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 退款执行参数错误: refundId={}, error={}", refundId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 退款执行业务异常: refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 退款执行系统异常: refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("REFUND_EXECUTE_SYSTEM_ERROR", "退款执行失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 退款执行未知异常: refundId={}, error={}", refundId, e.getMessage(), e);
            return ResponseDTO.error("REFUND_EXECUTE_FAILED", "退款执行失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询支付记录")
    @Observed(name = "payment.getPaymentRecord", contextualName = "payment-get-payment-record")
    @GetMapping("/record/{paymentId}")
    public ResponseDTO<PaymentRecordEntity> getPaymentRecord(
            @Parameter(description = "支付记录ID", required = true) @PathVariable String paymentId) {

        log.debug("[支付API] 查询支付记录: paymentId={}", paymentId);

        try {
            PaymentRecordEntity record = paymentService.getPaymentRecord(paymentId);
            return ResponseDTO.ok(record);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 支付记录查询参数错误: paymentId={}, error={}", paymentId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 支付记录查询业务异常: paymentId={}, code={}, message={}", paymentId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 支付记录查询系统异常: paymentId={}, code={}, message={}", paymentId, e.getCode(), e.getMessage(),
                    e);
            return ResponseDTO.error("PAYMENT_RECORD_QUERY_SYSTEM_ERROR", "支付记录查询失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 支付记录查询未知异常: paymentId={}, error={}", paymentId, e.getMessage(), e);
            return ResponseDTO.error("PAYMENT_RECORD_QUERY_FAILED", "支付记录查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询用户支付记录")
    @Observed(name = "payment.getUserPaymentRecords", contextualName = "payment-get-user-payment-records")
    @GetMapping("/records/user/{userId}")
    public ResponseDTO<Map<String, Object>> getUserPaymentRecords(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {

        log.debug("[支付API] 查询用户支付记录: userId={}, pageNum={}, pageSize={}",
                userId, pageNum, pageSize);

        try {
            Map<String, Object> result = paymentService.getUserPaymentRecords(userId, pageNum, pageSize);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 用户支付记录查询参数错误: userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 用户支付记录查询业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 用户支付记录查询系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("USER_PAYMENT_RECORDS_QUERY_SYSTEM_ERROR", "用户支付记录查询失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 用户支付记录查询未知异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("USER_PAYMENT_RECORDS_QUERY_FAILED", "用户支付记录查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询退款记录")
    @Observed(name = "payment.getRefundRecord", contextualName = "payment-get-refund-record")
    @GetMapping("/refund/{refundId}")
    public ResponseDTO<Map<String, Object>> getRefundRecord(
            @Parameter(description = "退款记录ID", required = true) @PathVariable String refundId) {

        log.debug("[支付API] 查询退款记录: refundId={}", refundId);

        try {
            Map<String, Object> result = paymentService.getRefundRecord(refundId);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 退款记录查询参数错误: refundId={}, error={}", refundId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 退款记录查询业务异常: refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 退款记录查询系统异常: refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("REFUND_RECORD_QUERY_SYSTEM_ERROR", "退款记录查询失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 退款记录查询未知异常: refundId={}, error={}", refundId, e.getMessage(), e);
            return ResponseDTO.error("REFUND_RECORD_QUERY_FAILED", "退款记录查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询用户退款记录")
    @Observed(name = "payment.getUserRefundRecords", contextualName = "payment-get-user-refund-records")
    @GetMapping("/refunds/user/{userId}")
    public ResponseDTO<Map<String, Object>> getUserRefundRecords(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {

        log.debug("[支付API] 查询用户退款记录: userId={}, pageNum={}, pageSize={}",
                userId, pageNum, pageSize);

        try {
            Map<String, Object> result = paymentService.getUserRefundRecords(userId, pageNum, pageSize);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 用户退款记录查询参数错误: userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 用户退款记录查询业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 用户退款记录查询系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("USER_REFUND_RECORDS_QUERY_SYSTEM_ERROR", "用户退款记录查询失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 用户退款记录查询未知异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("USER_REFUND_RECORDS_QUERY_FAILED", "用户退款记录查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户支付统计")
    @Observed(name = "payment.getUserPaymentStatistics", contextualName = "payment-get-user-payment-statistics")
    @GetMapping("/statistics/user/{userId}")
    public ResponseDTO<Map<String, Object>> getUserPaymentStatistics(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        log.debug("[支付API] 获取用户支付统计: userId={}, startTime={}, endTime={}",
                userId, startTime, endTime);

        try {
            // 默认查询最近30天
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(30);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }

            Map<String, Object> statistics = paymentService.getUserPaymentStatistics(userId, startTime, endTime);
            return ResponseDTO.ok(statistics);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 用户支付统计获取参数错误: userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 用户支付统计获取业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 用户支付统计获取系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("USER_PAYMENT_STATISTICS_SYSTEM_ERROR", "用户支付统计获取失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 用户支付统计获取未知异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("USER_PAYMENT_STATISTICS_FAILED", "用户支付统计获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户退款统计")
    @Observed(name = "payment.getUserRefundStatistics", contextualName = "payment-get-user-refund-statistics")
    @GetMapping("/statistics/refund/user/{userId}")
    public ResponseDTO<Map<String, Object>> getUserRefundStatistics(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        log.debug("[支付API] 获取用户退款统计: userId={}, startTime={}, endTime={}",
                userId, startTime, endTime);

        try {
            // 默认查询最近30天
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(30);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }

            Map<String, Object> statistics = paymentService.getUserRefundStatistics(userId, startTime, endTime);
            return ResponseDTO.ok(statistics);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 用户退款统计获取参数错误: userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 用户退款统计获取业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 用户退款统计获取系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("USER_REFUND_STATISTICS_SYSTEM_ERROR", "用户退款统计获取失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 用户退款统计获取未知异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("USER_REFUND_STATISTICS_FAILED", "用户退款统计获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "执行对账")
    @Observed(name = "payment.performReconciliation", contextualName = "payment-perform-reconciliation")
    @PostMapping("/reconciliation")
    public ResponseDTO<Map<String, Object>> performReconciliation(
            @Parameter(description = "开始时间", required = true) @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam LocalDateTime endTime,
            @Parameter(description = "商户ID") @RequestParam(required = false) Long merchantId) {

        log.info("[支付API] 执行对账: startTime={}, endTime={}, merchantId={}", startTime, endTime, merchantId);

        try {
            Map<String, Object> result = paymentService.performReconciliation(startTime, endTime, merchantId);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 对账执行参数错误: startTime={}, endTime={}, merchantId={}, error={}",
                    startTime, endTime, merchantId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 对账执行业务异常: startTime={}, endTime={}, merchantId={}, code={}, message={}",
                    startTime, endTime, merchantId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 对账执行系统异常: startTime={}, endTime={}, merchantId={}, code={}, message={}",
                    startTime, endTime, merchantId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("RECONCILIATION_SYSTEM_ERROR", "对账执行失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 对账执行未知异常: startTime={}, endTime={}, merchantId={}, error={}",
                    startTime, endTime, merchantId, e.getMessage(), e);
            return ResponseDTO.error("RECONCILIATION_FAILED", "对账执行失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取商户结算统计")
    @Observed(name = "payment.getMerchantSettlementStatistics", contextualName = "payment-get-merchant-settlement-statistics")
    @GetMapping("/settlement/statistics/merchant/{merchantId}")
    public ResponseDTO<Map<String, Object>> getMerchantSettlementStatistics(
            @Parameter(description = "商户ID", required = true) @PathVariable Long merchantId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        log.debug("[支付API] 获取商户结算统计: merchantId={}, startTime={}, endTime={}",
                merchantId, startTime, endTime);

        try {
            // 默认查询最近30天
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(30);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }

            Map<String, Object> statistics = paymentService.getMerchantSettlementStatistics(merchantId, startTime,
                    endTime);
            return ResponseDTO.ok(statistics);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 商户结算统计获取参数错误: merchantId={}, error={}", merchantId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 商户结算统计获取业务异常: merchantId={}, code={}, message={}", merchantId, e.getCode(),
                    e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 商户结算统计获取系统异常: merchantId={}, code={}, message={}", merchantId, e.getCode(),
                    e.getMessage(), e);
            return ResponseDTO.error("MERCHANT_SETTLEMENT_STATISTICS_SYSTEM_ERROR", "商户结算统计获取失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 商户结算统计获取未知异常: merchantId={}, error={}", merchantId, e.getMessage(), e);
            return ResponseDTO.error("MERCHANT_SETTLEMENT_STATISTICS_FAILED", "商户结算统计获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询待审核退款列表")
    @Observed(name = "payment.getPendingAuditRefunds", contextualName = "payment-get-pending-audit-refunds")
    @GetMapping("/refunds/pending-audit")
    public ResponseDTO<List<PaymentRefundRecordEntity>> getPendingAuditRefunds() {
        log.debug("[支付API] 查询待审核退款列表");

        try {
            Map<String, Object> result = paymentService.getPendingAuditRefunds();
            @SuppressWarnings("unchecked")
            List<PaymentRefundRecordEntity> refunds = (List<PaymentRefundRecordEntity>) result.get("list");
            return ResponseDTO.ok(refunds != null ? refunds : new ArrayList<>());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 待审核退款列表查询参数错误: error={}", e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 待审核退款列表查询业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 待审核退款列表查询系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("PENDING_AUDIT_REFUNDS_QUERY_SYSTEM_ERROR", "待审核退款列表查询失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 待审核退款列表查询未知异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("PENDING_AUDIT_REFUNDS_QUERY_FAILED", "待审核退款列表查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询待处理退款列表")
    @Observed(name = "payment.getPendingProcessRefunds", contextualName = "payment-get-pending-process-refunds")
    @GetMapping("/refunds/pending-process")
    public ResponseDTO<List<PaymentRefundRecordEntity>> getPendingProcessRefunds() {
        log.debug("[支付API] 查询待处理退款列表");

        try {
            Map<String, Object> result = paymentService.getPendingProcessRefunds();
            @SuppressWarnings("unchecked")
            List<PaymentRefundRecordEntity> refunds = (List<PaymentRefundRecordEntity>) result.get("list");
            return ResponseDTO.ok(refunds != null ? refunds : new ArrayList<>());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 待处理退款列表查询参数错误: error={}", e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 待处理退款列表查询业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 待处理退款列表查询系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("PENDING_PROCESS_REFUNDS_QUERY_SYSTEM_ERROR", "待处理退款列表查询失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 待处理退款列表查询未知异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("PENDING_PROCESS_REFUNDS_QUERY_FAILED", "待处理退款列表查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询高风险支付记录")
    @Observed(name = "payment.getHighRiskPayments", contextualName = "payment-get-high-risk-payments")
    @GetMapping("/records/high-risk")
    public ResponseDTO<List<PaymentRecordEntity>> getHighRiskPayments(
            @Parameter(description = "小时数", required = false) @RequestParam(defaultValue = "24") Integer hours) {

        log.debug("[支付API] 查询高风险支付记录: hours={}", hours);

        try {
            Map<String, Object> result = paymentService.getHighRiskPayments(hours);
            @SuppressWarnings("unchecked")
            List<PaymentRecordEntity> payments = (List<PaymentRecordEntity>) result.get("list");
            return ResponseDTO.ok(payments != null ? payments : new ArrayList<>());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 高风险支付记录查询参数错误: hours={}, error={}", hours, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 高风险支付记录查询业务异常: hours={}, code={}, message={}", hours, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 高风险支付记录查询系统异常: hours={}, code={}, message={}", hours, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("HIGH_RISK_PAYMENTS_QUERY_SYSTEM_ERROR", "高风险支付记录查询失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 高风险支付记录查询未知异常: hours={}, error={}", hours, e.getMessage(), e);
            return ResponseDTO.error("HIGH_RISK_PAYMENTS_QUERY_FAILED", "高风险支付记录查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询高风险退款记录")
    @Observed(name = "payment.getHighRiskRefunds", contextualName = "payment-get-high-risk-refunds")
    @GetMapping("/refunds/high-risk")
    public ResponseDTO<List<PaymentRefundRecordEntity>> getHighRiskRefunds(
            @Parameter(description = "小时数", required = false) @RequestParam(defaultValue = "24") Integer hours) {

        log.debug("[支付API] 查询高风险退款记录: hours={}", hours);

        try {
            Map<String, Object> result = paymentService.getHighRiskRefunds(hours);
            @SuppressWarnings("unchecked")
            List<PaymentRefundRecordEntity> refunds = (List<PaymentRefundRecordEntity>) result.get("list");
            return ResponseDTO.ok(refunds != null ? refunds : new ArrayList<>());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 高风险退款记录查询参数错误: hours={}, error={}", hours, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 高风险退款记录查询业务异常: hours={}, code={}, message={}", hours, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 高风险退款记录查询系统异常: hours={}, code={}, message={}", hours, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("HIGH_RISK_REFUNDS_QUERY_SYSTEM_ERROR", "高风险退款记录查询失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 高风险退款记录查询未知异常: hours={}, error={}", hours, e.getMessage(), e);
            return ResponseDTO.error("HIGH_RISK_REFUNDS_QUERY_FAILED", "高风险退款记录查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询异常支付记录")
    @Observed(name = "payment.getAbnormalPayments", contextualName = "payment-get-abnormal-payments")
    @GetMapping("/records/abnormal")
    public ResponseDTO<List<PaymentRecordEntity>> getAbnormalPayments(
            @Parameter(description = "小时数", required = false) @RequestParam(defaultValue = "24") Integer hours) {

        log.debug("[支付API] 查询异常支付记录: hours={}", hours);

        try {
            Map<String, Object> result = paymentService.getAbnormalPayments(hours);
            @SuppressWarnings("unchecked")
            List<PaymentRecordEntity> payments = (List<PaymentRecordEntity>) result.get("list");
            return ResponseDTO.ok(payments != null ? payments : new ArrayList<>());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付API] 异常支付记录查询参数错误: hours={}, error={}", hours, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付API] 异常支付记录查询业务异常: hours={}, code={}, message={}", hours, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付API] 异常支付记录查询系统异常: hours={}, code={}, message={}", hours, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("ABNORMAL_PAYMENTS_QUERY_SYSTEM_ERROR", "异常支付记录查询失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付API] 异常支付记录查询未知异常: hours={}, error={}", hours, e.getMessage(), e);
            return ResponseDTO.error("ABNORMAL_PAYMENTS_QUERY_FAILED", "异常支付记录查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建微信支付订单（保留原有功能）
     */
    @PostMapping("/wechat/createOrder")
    @Observed(name = "payment.createWechatPayOrder", contextualName = "payment-create-wechat-pay-order")
    @Operation(summary = "创建微信支付订单", description = "创建微信支付V3预支付订单，支持多种支付方式（JSAPI、APP、H5、Native）")
    @PermissionCheck(value = { "CONSUME_ACCOUNT_MANAGE", "CONSUME_ACCOUNT_USE" }, description = "账户管理和使用操作")
    public ResponseDTO<Map<String, Object>> createWechatPayOrder(
            @Parameter(description = "订单ID（业务订单号）", required = true) @RequestParam @NotBlank String orderId,
            @Parameter(description = "金额（单位：元）", required = true) @RequestParam @NotNull BigDecimal amount,
            @Parameter(description = "商品描述", required = true) @RequestParam @NotBlank String description,
            @Parameter(description = "用户OpenID（JSAPI支付时必需）") @RequestParam(required = false) String openId,
            @Parameter(description = "支付类型（JSAPI/APP/H5/Native）", required = true) @RequestParam @NotBlank String payType) {
        log.info("[支付管理] 创建微信支付订单，orderId={}, amount={}, payType={}", orderId, amount, payType);
        try {
            Map<String, Object> result = paymentService.createWechatPayOrder(
                    orderId, amount, description, openId, payType);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付管理] 创建微信支付订单参数错误，orderId={}, amount={}, error={}", orderId, amount, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付管理] 创建微信支付订单业务异常，orderId={}, amount={}, code={}, message={}", orderId, amount, e.getCode(),
                    e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付管理] 创建微信支付订单系统异常，orderId={}, amount={}, code={}, message={}", orderId, amount, e.getCode(),
                    e.getMessage(), e);
            return ResponseDTO.error("CREATE_WECHAT_ORDER_SYSTEM_ERROR", "创建微信支付订单失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付管理] 创建微信支付订单未知异常，orderId={}, amount={}", orderId, amount, e);
            return ResponseDTO.error("CREATE_WECHAT_ORDER_ERROR", "创建微信支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 处理微信支付回调通知
     * <p>
     * 接收并处理微信支付平台的回调通知，验证签名、更新订单状态、处理业务逻辑
     * 此接口不需要认证，由微信支付平台直接调用
     * </p>
     *
     * @param notifyData 通知数据（JSON格式，微信支付平台发送的加密数据）
     * @return 处理结果，必须返回SUCCESS或FAIL
     * @apiNote 此接口由微信支付平台调用，不需要认证
     */
    @PostMapping("/wechat/notify")
    @Observed(name = "payment.handleWechatPayNotify", contextualName = "payment-handle-wechat-pay-notify")
    @Operation(summary = "处理微信支付回调", description = "接收并处理微信支付平台的回调通知，验证签名、更新订单状态、处理业务逻辑。此接口不需要认证，由微信支付平台直接调用。", tags = {
            "支付管理PC端" })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "处理成功，返回SUCCESS", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Map.class)))
    public Map<String, Object> handleWechatPayNotify(
            @Parameter(description = "通知数据（JSON格式，微信支付平台发送的加密数据）", required = true) @RequestBody String notifyData) {
        log.info("[支付管理] 接收微信支付回调通知，notifyData长度={}",
                notifyData != null ? notifyData.length() : 0);
        try {
            Map<String, Object> result = paymentService.handleWechatPayNotify(notifyData);
            return result;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付管理] 处理微信支付回调参数错误: error={}", e.getMessage());
            return Map.of("code", "FAIL", "message", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付管理] 处理微信支付回调业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return Map.of("code", "FAIL", "message", e.getMessage());
        } catch (SystemException e) {
            log.error("[支付管理] 处理微信支付回调系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return Map.of("code", "FAIL", "message", "处理回调失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付管理] 处理微信支付回调未知异常", e);
            return Map.of("code", "FAIL", "message", "处理回调失败: " + e.getMessage());
        }
    }

    /**
     * 创建支付宝支付订单
     *
     * @param orderId 订单ID
     * @param amount  金额（元）
     * @param subject 商品标题
     * @param payType 支付类型（APP/Web/Wap）
     * @return 支付参数
     */
    @PostMapping("/alipay/createOrder")
    @Observed(name = "payment.createAlipayOrder", contextualName = "payment-create-alipay-order")
    @Operation(summary = "创建支付宝支付订单", description = "创建支付宝预支付订单，返回支付所需参数")
    @PermissionCheck(value = { "CONSUME_ACCOUNT_MANAGE", "CONSUME_ACCOUNT_USE" }, description = "账户管理和使用操作")
    public ResponseDTO<Map<String, Object>> createAlipayOrder(
            @RequestParam @NotBlank String orderId,
            @RequestParam @NotNull BigDecimal amount,
            @RequestParam @NotBlank String subject,
            @RequestParam @NotBlank String payType) {
        log.info("[支付管理] 创建支付宝支付订单，orderId={}, amount={}, payType={}", orderId, amount, payType);
        try {
            Map<String, Object> result = paymentService.createAlipayOrder(
                    orderId, amount, subject, payType);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付管理] 创建支付宝支付订单参数错误，orderId={}, amount={}, error={}", orderId, amount, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付管理] 创建支付宝支付订单业务异常，orderId={}, amount={}, code={}, message={}", orderId, amount, e.getCode(),
                    e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付管理] 创建支付宝支付订单系统异常，orderId={}, amount={}, code={}, message={}", orderId, amount, e.getCode(),
                    e.getMessage(), e);
            return ResponseDTO.error("CREATE_ALIPAY_ORDER_SYSTEM_ERROR", "创建支付宝支付订单失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付管理] 创建支付宝支付订单未知异常，orderId={}, amount={}", orderId, amount, e);
            return ResponseDTO.error("CREATE_ALIPAY_ORDER_ERROR", "创建支付宝支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 处理支付宝支付回调通知
     *
     * @param params 通知参数
     * @return 处理结果（"success"或"fail"）
     */
    @PostMapping("/alipay/notify")
    @Observed(name = "payment.handleAlipayNotify", contextualName = "payment-handle-alipay-notify")
    @Operation(summary = "处理支付宝支付回调", description = "接收并处理支付宝平台的回调通知")
    public String handleAlipayNotify(@RequestParam Map<String, String> params) {
        log.info("[支付管理] 接收支付宝支付回调通知，参数数量={}", params != null ? params.size() : 0);
        try {
            Map<String, Object> result = paymentService.handleAlipayNotify(params);
            // 支付宝回调需要返回字符串格式的响应
            String response = result != null && Boolean.TRUE.equals(result.get("success"))
                    ? "success"
                    : "fail";
            return response;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付管理] 处理支付宝支付回调参数错误: error={}", e.getMessage());
            return "fail";
        } catch (BusinessException e) {
            log.warn("[支付管理] 处理支付宝支付回调业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return "fail";
        } catch (SystemException e) {
            log.error("[支付管理] 处理支付宝支付回调系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return "fail";
        } catch (Exception e) {
            log.error("[支付管理] 处理支付宝支付回调未知异常", e);
            return "fail";
        }
    }

    /**
     * 微信支付退款
     *
     * @param orderId      订单ID
     * @param refundId     退款单ID
     * @param totalAmount  订单总金额（分）
     * @param refundAmount 退款金额（分）
     * @return 退款结果
     */
    @PostMapping("/wechat/refund")
    @Observed(name = "payment.wechatRefund", contextualName = "payment-wechat-refund")
    @Operation(summary = "微信支付退款", description = "发起微信支付订单的退款申请")
    @PermissionCheck(value = "CONSUME_ACCOUNT_MANAGE", description = "账户管理操作")
    public ResponseDTO<Map<String, Object>> wechatRefund(
            @RequestParam @NotBlank String orderId,
            @RequestParam @NotBlank String refundId,
            @RequestParam @NotNull Integer totalAmount,
            @RequestParam @NotNull Integer refundAmount) {
        log.info("[支付管理] 微信支付退款，orderId={}, refundId={}, totalAmount={}, refundAmount={}",
                orderId, refundId, totalAmount, refundAmount);
        try {
            Map<String, Object> result = paymentService.wechatRefund(orderId, refundId, totalAmount, refundAmount);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付管理] 微信支付退款参数错误，orderId={}, refundId={}, error={}", orderId, refundId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付管理] 微信支付退款业务异常，orderId={}, refundId={}, code={}, message={}", orderId, refundId, e.getCode(),
                    e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付管理] 微信支付退款系统异常，orderId={}, refundId={}, code={}, message={}", orderId, refundId, e.getCode(),
                    e.getMessage(), e);
            return ResponseDTO.error("WECHAT_REFUND_SYSTEM_ERROR", "微信支付退款失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付管理] 微信支付退款未知异常，orderId={}, refundId={}", orderId, refundId, e);
            return ResponseDTO.error("WECHAT_REFUND_ERROR", "微信支付退款失败: " + e.getMessage());
        }
    }

    /**
     * 支付宝退款
     *
     * @param orderId      订单ID
     * @param refundAmount 退款金额（元）
     * @param reason       退款原因（可选）
     * @return 退款结果
     */
    @PostMapping("/alipay/refund")
    @Observed(name = "payment.alipayRefund", contextualName = "payment-alipay-refund")
    @Operation(summary = "支付宝退款", description = "发起支付宝支付订单的退款申请")
    @PermissionCheck(value = "CONSUME_ACCOUNT_MANAGE", description = "账户管理操作")
    public ResponseDTO<Map<String, Object>> alipayRefund(
            @RequestParam @NotBlank String orderId,
            @RequestParam @NotNull BigDecimal refundAmount,
            @RequestParam(required = false) String reason) {
        log.info("[支付管理] 支付宝退款，orderId={}, refundAmount={}, reason={}",
                orderId, refundAmount, reason);
        try {
            Map<String, Object> result = paymentService.alipayRefund(orderId, refundAmount, reason);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付管理] 支付宝退款参数错误，orderId={}, refundAmount={}, error={}", orderId, refundAmount, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付管理] 支付宝退款业务异常，orderId={}, refundAmount={}, code={}, message={}", orderId, refundAmount,
                    e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付管理] 支付宝退款系统异常，orderId={}, refundAmount={}, code={}, message={}", orderId, refundAmount,
                    e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("ALIPAY_REFUND_SYSTEM_ERROR", "支付宝退款失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付管理] 支付宝退款未知异常，orderId={}, refundAmount={}", orderId, refundAmount, e);
            return ResponseDTO.error("ALIPAY_REFUND_ERROR", "支付宝退款失败: " + e.getMessage());
        }
    }

    /**
     * 创建银行支付订单
     *
     * @param accountId   账户ID
     * @param amount      金额（元）
     * @param orderId     订单ID
     * @param description 商品描述
     * @param bankCardNo  银行卡号（可选）
     * @return 支付结果
     */
    @PostMapping("/bank/createOrder")
    @Observed(name = "payment.createBankPaymentOrder", contextualName = "payment-create-bank-payment-order")
    @Operation(summary = "创建银行支付订单", description = "创建银行支付订单，调用银行支付网关API")
    @PermissionCheck(value = { "CONSUME_ACCOUNT_MANAGE", "CONSUME_ACCOUNT_USE" }, description = "账户管理和使用操作")
    public ResponseDTO<Map<String, Object>> createBankPaymentOrder(
            @RequestParam @NotNull Long accountId,
            @RequestParam @NotNull BigDecimal amount,
            @RequestParam @NotBlank String orderId,
            @RequestParam @NotBlank String description,
            @RequestParam(required = false) String bankCardNo) {
        log.info("[支付管理] 创建银行支付订单，accountId={}, amount={}, orderId={}", accountId, amount, orderId);
        try {
            Map<String, Object> result = paymentService.createBankPaymentOrder(
                    accountId, amount, orderId, description, bankCardNo);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付管理] 创建银行支付订单参数错误，accountId={}, orderId={}, error={}", accountId, orderId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付管理] 创建银行支付订单业务异常，accountId={}, orderId={}, code={}, message={}", accountId, orderId,
                    e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付管理] 创建银行支付订单系统异常，accountId={}, orderId={}, code={}, message={}", accountId, orderId,
                    e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("CREATE_BANK_ORDER_SYSTEM_ERROR", "创建银行支付订单失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付管理] 创建银行支付订单未知异常，accountId={}, orderId={}", accountId, orderId, e);
            return ResponseDTO.error("CREATE_BANK_ORDER_ERROR", "创建银行支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 处理信用额度支付
     *
     * @param accountId 账户ID
     * @param amount    金额（元）
     * @param orderId   订单ID
     * @param reason    扣除原因（可选）
     * @return 支付结果
     */
    @PostMapping("/credit/processPayment")
    @Observed(name = "payment.processCreditLimitPayment", contextualName = "payment-process-credit-limit-payment")
    @Operation(summary = "处理信用额度支付", description = "使用信用额度进行支付，扣除信用额度")
    @PermissionCheck(value = { "CONSUME_ACCOUNT_MANAGE", "CONSUME_ACCOUNT_USE" }, description = "账户管理和使用操作")
    public ResponseDTO<Map<String, Object>> processCreditLimitPayment(
            @RequestParam @NotNull Long accountId,
            @RequestParam @NotNull BigDecimal amount,
            @RequestParam @NotBlank String orderId,
            @RequestParam(required = false) String reason) {
        log.info("[支付管理] 处理信用额度支付，accountId={}, amount={}, orderId={}", accountId, amount, orderId);
        try {
            Map<String, Object> result = paymentService.processCreditLimitPayment(
                    accountId, amount, orderId, reason);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付管理] 处理信用额度支付参数错误，accountId={}, orderId={}, error={}", accountId, orderId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付管理] 处理信用额度支付业务异常，accountId={}, orderId={}, code={}, message={}", accountId, orderId,
                    e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付管理] 处理信用额度支付系统异常，accountId={}, orderId={}, code={}, message={}", accountId, orderId,
                    e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("CREDIT_PAYMENT_SYSTEM_ERROR", "处理信用额度支付失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[支付管理] 处理信用额度支付未知异常，accountId={}, orderId={}", accountId, orderId, e);
            return ResponseDTO.error("CREDIT_PAYMENT_ERROR", "处理信用额度支付失败: " + e.getMessage());
        }
    }
}
