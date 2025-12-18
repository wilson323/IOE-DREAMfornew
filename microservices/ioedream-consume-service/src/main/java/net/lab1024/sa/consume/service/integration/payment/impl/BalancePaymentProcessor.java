package net.lab1024.sa.consume.service.integration.payment.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.service.integration.payment.*;
import net.lab1024.sa.consume.service.ConsumeAccountService;
import net.lab1024.sa.consume.service.TransactionRecordService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 浣欓鏀粯澶勭悊鍣?
 * 瀹炵幇P0绾т綑棰濇敮浠樺姛鑳?
 *
 * @author IOE-DREAM鍥㈤槦
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class BalancePaymentProcessor implements PaymentProcessor {

    private final ConsumeAccountService accountService;
    private final TransactionRecordService transactionService;

    @Resource
    public BalancePaymentProcessor(ConsumeAccountService accountService,
                                   TransactionRecordService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Override
    public PaymentMethod getSupportedPaymentMethod() {
        return PaymentMethod.BALANCE;
    }

    @Override
    public PaymentResult processPayment(PaymentContext context) {
        log.info("[浣欓鏀粯] 寮€濮嬪鐞嗕綑棰濇敮浠? userId={}, amount={}",
                context.getUserId(), context.getAmount());

        try {
            // 1. 楠岃瘉璐︽埛鐘舵€?
            AccountInfo accountInfo = accountService.getAccountInfo(context.getUserId());
            if (accountInfo == null || !accountInfo.isActive()) {
                return PaymentResult.failure("璐︽埛涓嶅瓨鍦ㄦ垨宸插喕缁?);
            }

            // 2. 妫€鏌ヤ綑棰?
            BigDecimal availableBalance = accountInfo.getAvailableBalance();
            if (availableBalance.compareTo(context.getAmount()) < 0) {
                return PaymentResult.failure("浣欓涓嶈冻");
            }

            // 3. 鍒涘缓浜ゆ槗璁板綍
            String transactionId = generateTransactionId();
            TransactionRecord record = createTransactionRecord(context, transactionId);

            // 4. 鎵ｅ噺浣欓
            BalanceUpdateResult updateResult = accountService.updateBalance(
                    context.getUserId(),
                    context.getAmount(),
                    BalanceOperation.DEDUCT);

            if (!updateResult.isSuccess()) {
                return PaymentResult.failure(updateResult.getFailureReason());
            }

            // 5. 淇濆瓨浜ゆ槗璁板綍
            record.setStatus(PaymentStatus.SUCCESS);
            record.setProcessTime(LocalDateTime.now());
            transactionService.saveTransaction(record);

            log.info("[浣欓鏀粯] 鏀粯鎴愬姛: transactionId={}, amount={}", transactionId, context.getAmount());

            // 6. 杩斿洖鏀粯缁撴灉
            return PaymentResult.success(transactionId)
                    .paymentMethod(PaymentMethod.BALANCE)
                    .amount(context.getAmount())
                    .beforeBalance(availableBalance)
                    .afterBalance(availableBalance.subtract(context.getAmount()))
                    .transactionTime(LocalDateTime.now());

        } catch (Exception e) {
            log.error("[浣欓鏀粯] 澶勭悊寮傚父", e);
            return PaymentResult.failure("浣欓鏀粯澶勭悊寮傚父: " + e.getMessage());
        }
    }

    @Override
    public RefundResult processRefund(RefundContext context) {
        log.info("[浣欓鏀粯] 寮€濮嬪鐞嗛€€娆? originalTransactionId={}, amount={}",
                context.getOriginalTransactionId(), context.getRefundAmount());

        try {
            // 1. 楠岃瘉鍘熶氦鏄?
            TransactionRecord originalTransaction = transactionService.getTransaction(context.getOriginalTransactionId());
            if (originalTransaction == null) {
                return RefundResult.failure("鍘熶氦鏄撲笉瀛樺湪");
            }

            if (originalTransaction.getStatus() != PaymentStatus.SUCCESS) {
                return RefundResult.failure("鍘熶氦鏄撶姸鎬佸紓甯革紝鏃犳硶閫€娆?);
            }

            // 2. 妫€鏌ラ€€娆鹃噾棰?
            if (context.getRefundAmount().compareTo(originalTransaction.getAmount()) > 0) {
                return RefundResult.failure("閫€娆鹃噾棰濅笉鑳藉ぇ浜庡師浜ゆ槗閲戦");
            }

            // 3. 鍒涘缓閫€娆句氦鏄撹褰?
            String refundId = generateTransactionId();
            TransactionRecord refundRecord = createRefundTransactionRecord(context, refundId);

            // 4. 澧炲姞浣欓
            BalanceUpdateResult updateResult = accountService.updateBalance(
                    originalTransaction.getUserId(),
                    context.getRefundAmount(),
                    BalanceOperation.REFUND);

            if (!updateResult.isSuccess()) {
                return RefundResult.failure(updateResult.getFailureReason());
            }

            // 5. 淇濆瓨閫€娆捐褰?
            refundRecord.setStatus(RefundStatus.SUCCESS);
            refundRecord.setProcessTime(LocalDateTime.now());
            transactionService.saveTransaction(refundRecord);

            // 6. 鏇存柊鍘熶氦鏄撶姸鎬?
            originalTransaction.setRefundTransactionId(refundId);
            originalTransaction.setRefundAmount(context.getRefundAmount());
            transactionService.updateTransaction(originalTransaction);

            log.info("[浣欓鏀粯] 閫€娆炬垚鍔? refundId={}, amount={}", refundId, context.getRefundAmount());

            return RefundResult.success(refundId)
                    .originalTransactionId(context.getOriginalTransactionId())
                    .refundAmount(context.getRefundAmount())
                    .refundStatus(RefundStatus.SUCCESS)
                    .refundTime(LocalDateTime.now())
                    .refundReason(context.getRefundReason());

        } catch (Exception e) {
            log.error("[浣欓鏀粯] 閫€娆惧鐞嗗紓甯?, e);
            return RefundResult.failure("閫€娆惧鐞嗗紓甯? " + e.getMessage());
        }
    }

    @Override
    public PaymentStatus queryPaymentStatus(String transactionId) {
        try {
            TransactionRecord record = transactionService.getTransaction(transactionId);
            if (record == null) {
                return PaymentStatus.NOT_FOUND;
            }
            return record.getStatus();
        } catch (Exception e) {
            log.error("[浣欓鏀粯] 鏌ヨ鏀粯鐘舵€佸紓甯?, e);
            return PaymentStatus.UNKNOWN;
        }
    }

    @Override
    public boolean verifyPayment(String transactionId, BigDecimal amount) {
        try {
            TransactionRecord record = transactionService.getTransaction(transactionId);
            if (record == null) {
                return false;
            }

            return record.getStatus() == PaymentStatus.SUCCESS &&
                   record.getAmount().compareTo(amount) == 0;
        } catch (Exception e) {
            log.error("[浣欓鏀粯] 楠岃瘉鏀粯寮傚父", e);
            return false;
        }
    }

    @Override
    public PaymentDetails getPaymentDetails(String transactionId) {
        try {
            TransactionRecord record = transactionService.getTransaction(transactionId);
            if (record == null) {
                return null;
            }

            PaymentDetails details = new PaymentDetails();
            details.setTransactionId(record.getTransactionId());
            details.setPaymentMethod(record.getPaymentMethod());
            details.setAmount(record.getAmount());
            details.setStatus(record.getStatus());
            details.setCreateTime(record.getCreateTime());
            details.setProcessTime(record.getProcessTime());
            details.setUserId(record.getUserId());
            details.setMerchantId(record.getMerchantId());
            details.setMerchantName(record.getMerchantName());
            details.setDeviceId(record.getDeviceId());
            details.setLocationId(record.getLocationId());
            details.setLocationName(record.getLocationName());

            return details;

        } catch (Exception e) {
            log.error("[浣欓鏀粯] 鑾峰彇鏀粯璇︽儏寮傚父", e);
            return null;
        }
    }

    // ==================== 绉佹湁鏂规硶 ====================

    private TransactionRecord createTransactionRecord(PaymentContext context, String transactionId) {
        TransactionRecord record = new TransactionRecord();
        record.setTransactionId(transactionId);
        record.setUserId(context.getUserId());
        record.setAmount(context.getAmount());
        record.setPaymentMethod(PaymentMethod.BALANCE);
        record.setMerchantId(context.getMerchantId());
        record.setMerchantName(context.getMerchantName());
        record.setDeviceId(context.getDeviceId());
        record.setLocationId(context.getLocationId());
        record.setLocationName(context.getLocationName());
        record.setTransactionType(TransactionType.CONSUME);
        record.setStatus(PaymentStatus.PROCESSING);
        record.setCreateTime(LocalDateTime.now());
        record.setRiskLevel(context.getRiskAssessment().getRiskLevel());
        record.setRiskScore(context.getRiskAssessment().getRiskScore());
        return record;
    }

    private TransactionRecord createRefundTransactionRecord(RefundContext context, String refundId) {
        TransactionRecord record = new TransactionRecord();
        record.setTransactionId(refundId);
        record.setOriginalTransactionId(context.getOriginalTransactionId());
        record.setAmount(context.getRefundAmount());
        record.setPaymentMethod(PaymentMethod.BALANCE);
        record.setTransactionType(TransactionType.REFUND);
        record.setStatus(PaymentStatus.PROCESSING);
        record.setCreateTime(LocalDateTime.now());
        record.setRefundReason(context.getRefundReason());
        return record;
    }

    private String generateTransactionId() {
        return "BAL" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
