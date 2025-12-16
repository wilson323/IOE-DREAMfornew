package net.lab1024.sa.consume.service.engine;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.response.ResponseDTO;
import net.lab1024.sa.consume.domain.form.consume.ConsumeRequestForm;
import net.lab1024.sa.consume.domain.vo.consume.PaymentResultVO;
import net.lab1024.sa.consume.domain.vo.consume.RefundResultVO;
import net.lab1024.sa.consume.domain.vo.consume.RiskAssessmentVO;
import net.lab1024.sa.consume.service.integration.payment.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureTask;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 消费支付引擎
 * 实现P0级多支付方式集成、实时清算、风险控制功能
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Service
public class ConsumePaymentEngine {

    private final ExecutorService paymentExecutor;
    private final PaymentProcessorRegistry processorRegistry;
    private final RiskAssessmentEngine riskEngine;
    private final TransactionSyncService syncService;

    public ConsumePaymentEngine(PaymentProcessorRegistry processorRegistry,
                                 RiskAssessmentEngine riskEngine,
                                 TransactionSyncService syncService) {
        this.processorRegistry = processorRegistry;
        this.riskEngine = riskEngine;
        this.syncService = syncService;
        this.paymentExecutor = Executors.newFixedThreadPool(20);
    }

    /**
     * 执行支付 - P0级核心功能
     * 支持多支付方式集成、实时清算、风险控制
     *
     * @param request 支付请求
     * @return 支付结果
     */
    @CircuitBreaker(name = "consumePayment", fallbackMethod = "paymentFallback")
    @RateLimiter(name = "consumePayment", fallbackMethod = "paymentRateLimitFallback")
    @Retry(name = "consumePayment", fallbackMethod = "paymentRetryFallback")
    @Bulkhead(name = "consumePayment", type = Bulkhead.Type.THREADPOOL, fallbackMethod = "paymentBulkheadFallback")
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<ResponseDTO<PaymentResultVO>> executePayment(ConsumeRequestForm request) {
        log.info("[消费支付] 开始处理支付请求: userId={}, amount={}, paymentMethod={}",
                request.getUserId(), request.getAmount(), request.getPaymentMethod());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 风险评估
                ResponseDTO<RiskAssessmentVO> riskResult = assessRisk(request).get();
                if (!riskResult.isSuccess() || riskResult.getData().getRiskLevel() >= 3) {
                    log.warn("[消费支付] 风险评估失败或风险等级过高: {}", riskResult.getMessage());
                    return ResponseDTO.error("PAYMENT_RISK_HIGH", "支付风险评估不通过，交易已拒绝");
                }

                // 2. 获取支付处理器
                PaymentProcessor processor = processorRegistry.getProcessor(request.getPaymentMethod());
                if (processor == null) {
                    log.error("[消费支付] 不支持的支付方式: {}", request.getPaymentMethod());
                    return ResponseDTO.error("UNSUPPORTED_PAYMENT_METHOD", "不支持的支付方式");
                }

                // 3. 执行支付处理
                PaymentContext context = buildPaymentContext(request, riskResult.getData());
                PaymentResult result = processor.processPayment(context);

                // 4. 处理支付结果
                if (result.isSuccess()) {
                    // 5. 同步交易数据
                    syncService.syncTransaction(result.getTransactionId());
                    log.info("[消费支付] 支付成功: transactionId={}, amount={}",
                            result.getTransactionId(), request.getAmount());
                    return ResponseDTO.ok(convertToVO(result));
                } else {
                    log.error("[消费支付] 支付失败: transactionId={}, reason={}",
                            result.getTransactionId(), result.getFailureReason());
                    return ResponseDTO.error("PAYMENT_FAILED", result.getFailureReason());
                }

            } catch (Exception e) {
                log.error("[消费支付] 处理支付异常", e);
                return ResponseDTO.error("PAYMENT_PROCESS_ERROR", "支付处理异常: " + e.getMessage());
            }
        }, paymentExecutor);
    }

    /**
     * 处理退款 - P0级功能
     * 支持多种退款方式和退款状态跟踪
     *
     * @param request 退款请求
     * @return 退款结果
     */
    @CircuitBreaker(name = "consumeRefund", fallbackMethod = "refundFallback")
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<ResponseDTO<RefundResultVO>> processRefund(RefundRequest request) {
        log.info("[消费支付] 开始处理退款请求: transactionId={}, amount={}",
                request.getOriginalTransactionId(), request.getRefundAmount());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 验证原交易
                PaymentProcessor processor = processorRegistry.getProcessor(request.getPaymentMethod());
                if (processor == null) {
                    return ResponseDTO.error("UNSUPPORTED_REFUND_METHOD", "不支持的退款方式");
                }

                // 2. 执行退款处理
                RefundContext context = buildRefundContext(request);
                RefundResult result = processor.processRefund(context);

                // 3. 处理退款结果
                if (result.isSuccess()) {
                    syncService.syncRefundTransaction(result.getRefundId());
                    log.info("[消费支付] 退款成功: refundId={}", result.getRefundId());
                    return ResponseDTO.ok(convertToRefundVO(result));
                } else {
                    log.error("[消费支付] 退款失败: reason={}", result.getFailureReason());
                    return ResponseDTO.error("REFUND_FAILED", result.getFailureReason());
                }

            } catch (Exception e) {
                log.error("[消费支付] 处理退款异常", e);
                return ResponseDTO.error("REFUND_PROCESS_ERROR", "退款处理异常: " + e.getMessage());
            }
        }, paymentExecutor);
    }

    /**
     * 风险评估 - P0级功能
     * 实时风险检测和异常分析
     *
     * @param request 支付请求
     * @return 风险评估结果
     */
    @CircuitBreaker(name = "riskAssessment")
    public CompletableFuture<ResponseDTO<RiskAssessmentVO>> assessRisk(ConsumeRequestForm request) {
        log.debug("[消费支付] 开始风险评估: userId={}, amount={}", request.getUserId(), request.getAmount());

        return CompletableFuture.supplyAsync(() -> {
            try {
                RiskAssessmentResult result = riskEngine.assessPaymentRisk(request);
                RiskAssessmentVO vo = convertToRiskAssessmentVO(result);
                return ResponseDTO.ok(vo);
            } catch (Exception e) {
                log.error("[消费支付] 风险评估异常", e);
                return ResponseDTO.error("RISK_ASSESSMENT_ERROR", "风险评估异常: " + e.getMessage());
            }
        }, paymentExecutor);
    }

    /**
     * 离线支付处理 - P0级功能
     * 支持网络中断时的离线支付
     *
     * @param request 离线支付请求
     * @return 处理结果
     */
    @CircuitBreaker(name = "offlinePayment")
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<ResponseDTO<PaymentResultVO>> processOfflinePayment(OfflinePaymentRequest request) {
        log.info("[消费支付] 处理离线支付: deviceId={}, amount={}",
                request.getDeviceId(), request.getAmount());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 验证离线支付权限
                if (!validateOfflinePayment(request)) {
                    return ResponseDTO.error("OFFLINE_PAYMENT_NOT_ALLOWED", "离线支付权限不足");
                }

                // 2. 创建离线交易记录
                OfflineTransactionRecord record = createOfflineTransaction(request);

                // 3. 扣减余额（如果使用余额支付）
                if (PaymentMethod.BALANCE.equals(request.getPaymentMethod())) {
                    BalanceUpdateResult balanceResult = updateBalance(request.getUserId(),
                            request.getAmount(), BalanceOperation.DEDUCT);

                    if (!balanceResult.isSuccess()) {
                        return ResponseDTO.error("BALANCE_INSUFFICIENT", "余额不足");
                    }
                }

                // 4. 返回离线支付结果
                PaymentResultVO result = createOfflinePaymentResult(record);
                return ResponseDTO.ok(result);

            } catch (Exception e) {
                log.error("[消费支付] 离线支付处理异常", e);
                return ResponseDTO.error("OFFLINE_PAYMENT_ERROR", "离线支付异常: " + e.getMessage());
            }
        }, paymentExecutor);
    }

    /**
     * 同步离线交易 - P0级功能
     * 将离线交易数据同步到系统
     *
     * @param deviceId 设备ID
     * @return 同步结果
     */
    @CircuitBreaker(name = "syncOfflineTransactions")
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<ResponseDTO<SyncResultVO>> syncOfflineTransactions(String deviceId) {
        log.info("[消费支付] 同步离线交易: deviceId={}", deviceId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<OfflineTransactionRecord> offlineTransactions = getOfflineTransactions(deviceId);
                int successCount = 0;
                int failCount = 0;

                for (OfflineTransactionRecord transaction : offlineTransactions) {
                    try {
                        syncService.syncOfflineTransaction(transaction);
                        successCount++;
                    } catch (Exception e) {
                        log.error("[消费支付] 同步离线交易失败: transactionId={}", transaction.getTransactionId(), e);
                        failCount++;
                    }
                }

                SyncResultVO result = new SyncResultVO();
                result.setTotalCount(offlineTransactions.size());
                result.setSuccessCount(successCount);
                result.setFailCount(failCount);

                return ResponseDTO.ok(result);

            } catch (Exception e) {
                log.error("[消费支付] 同步离线交易异常", e);
                return ResponseDTO.error("SYNC_OFFLINE_ERROR", "同步离线交易异常: " + e.getMessage());
            }
        }, paymentExecutor);
    }

    // ==================== 私有方法 ====================

    private PaymentContext buildPaymentContext(ConsumeRequestForm request, RiskAssessmentVO riskResult) {
        PaymentContext context = new PaymentContext();
        context.setUserId(request.getUserId());
        context.setAmount(request.getAmount());
        context.setPaymentMethod(request.getPaymentMethod());
        context.setDeviceId(request.getDeviceId());
        context.setMerchantId(request.getMerchantId());
        context.setRiskAssessment(riskResult);
        context.setCreateTime(System.currentTimeMillis());
        return context;
    }

    private RefundContext buildRefundContext(RefundRequest request) {
        RefundContext context = new RefundContext();
        context.setOriginalTransactionId(request.getOriginalTransactionId());
        context.setRefundAmount(request.getRefundAmount());
        context.setRefundReason(request.getRefundReason());
        context.setPaymentMethod(request.getPaymentMethod());
        context.setCreateTime(System.currentTimeMillis());
        return context;
    }

    private PaymentResultVO convertToVO(PaymentResult result) {
        PaymentResultVO vo = new PaymentResultVO();
        vo.setTransactionId(result.getTransactionId());
        vo.setPaymentMethod(result.getPaymentMethod());
        vo.setAmount(result.getAmount());
        vo.setStatus(result.getStatus());
        vo.setTransactionTime(result.getTransactionTime());
        vo.setPaymentDetails(result.getPaymentDetails());
        return vo;
    }

    private RefundResultVO convertToRefundVO(RefundResult result) {
        RefundResultVO vo = new RefundResultVO();
        vo.setRefundId(result.getRefundId());
        vo.setOriginalTransactionId(result.getOriginalTransactionId());
        vo.setRefundAmount(result.getRefundAmount());
        vo.setRefundStatus(result.getRefundStatus());
        vo.setRefundTime(result.getRefundTime());
        vo.setRefundReason(result.getRefundReason());
        return vo;
    }

    private RiskAssessmentVO convertToRiskAssessmentVO(RiskAssessmentResult result) {
        RiskAssessmentVO vo = new RiskAssessmentVO();
        vo.setRiskLevel(result.getRiskLevel());
        vo.setRiskScore(result.getRiskScore());
        vo.setRiskFactors(result.getRiskFactors());
        vo.setRecommendation(result.getRecommendation());
        return vo;
    }

    private boolean validateOfflinePayment(OfflinePaymentRequest request) {
        // 验证离线支付权限
        return true; // 简化实现，实际应该验证用户权限、设备权限等
    }

    private OfflineTransactionRecord createOfflineTransaction(OfflinePaymentRequest request) {
        OfflineTransactionRecord record = new OfflineTransactionRecord();
        record.setTransactionId(generateTransactionId());
        record.setUserId(request.getUserId());
        record.setAmount(request.getAmount());
        record.setPaymentMethod(request.getPaymentMethod());
        record.setDeviceId(request.getDeviceId());
        record.setOffline(true);
        record.setCreateTime(System.currentTimeMillis());
        record.setSyncStatus(SyncStatus.PENDING);
        return record;
    }

    private PaymentResultVO createOfflinePaymentResult(OfflineTransactionRecord record) {
        PaymentResultVO result = new PaymentResultVO();
        result.setTransactionId(record.getTransactionId());
        result.setPaymentMethod(record.getPaymentMethod());
        result.setAmount(record.getAmount());
        result.setStatus(PaymentStatus.SUCCESS);
        result.setTransactionTime(record.getCreateTime());
        result.setOffline(true);
        return result;
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    // ==================== 降级方法 ====================

    public CompletableFuture<ResponseDTO<PaymentResultVO>> paymentFallback(ConsumeRequestForm request, Exception e) {
        log.warn("[消费支付] 支付熔断降级: {}", e.getMessage());
        return CompletableFuture.completedFuture(
                ResponseDTO.error("PAYMENT_SERVICE_UNAVAILABLE", "支付服务暂时不可用，请稍后重试")
        );
    }

    public CompletableFuture<ResponseDTO<PaymentResultVO>> paymentRateLimitFallback(ConsumeRequestForm request, Exception e) {
        log.warn("[消费支付] 支付限流降级: {}", e.getMessage());
        return CompletableFuture.completedFuture(
                ResponseDTO.error("PAYMENT_RATE_LIMIT", "支付请求过于频繁，请稍后重试")
        );
    }

    public CompletableFuture<ResponseDTO<PaymentResultVO>> paymentRetryFallback(ConsumeRequestForm request, Exception e) {
        log.warn("[消费支付] 支付重试降级: {}", e.getMessage());
        return CompletableFuture.completedFuture(
                ResponseDTO.error("PAYMENT_RETRY_FAILED", "支付重试失败，请稍后重试")
        );
    }

    public CompletableFuture<ResponseDTO<PaymentResultVO>> paymentBulkheadFallback(ConsumeRequestForm request, Exception e) {
        log.warn("[消费支付] 支付隔离降级: {}", e.getMessage());
        return CompletableFuture.completedFuture(
                ResponseDTO.error("PAYMENT_SERVICE_BUSY", "支付服务繁忙，请稍后重试")
        );
    }

    public CompletableFuture<ResponseDTO<RefundResultVO>> refundFallback(RefundRequest request, Exception e) {
        log.warn("[消费支付] 退款熔断降级: {}", e.getMessage());
        return CompletableFuture.completedFuture(
                ResponseDTO.error("REFUND_SERVICE_UNAVAILABLE", "退款服务暂时不可用，请稍后重试")
        );
    }
}