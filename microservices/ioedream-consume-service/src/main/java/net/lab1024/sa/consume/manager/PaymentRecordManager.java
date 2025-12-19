package net.lab1024.sa.consume.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.entity.PaymentRefundRecordEntity;
import net.lab1024.sa.consume.dao.PaymentRecordDao;
import net.lab1024.sa.consume.dao.PaymentRefundRecordDao;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * 支付记录管理器
 * <p>
 * 企业级支付记录业务管理器，负责支付记录的创建、查询、对账、退款等核心业务逻辑
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class PaymentRecordManager {

    private final PaymentRecordDao paymentRecordDao;
    private final PaymentRefundRecordDao paymentRefundRecordDao;
    private final GatewayServiceClient gatewayServiceClient;
    @SuppressWarnings("unused") // 预留字段，用于未来JSON序列化需求
    private final ObjectMapper objectMapper;

    // 构造函数注入依赖
    public PaymentRecordManager(PaymentRecordDao paymentRecordDao,
                              PaymentRefundRecordDao paymentRefundRecordDao,
                              GatewayServiceClient gatewayServiceClient,
                              ObjectMapper objectMapper) {
        this.paymentRecordDao = paymentRecordDao;
        this.paymentRefundRecordDao = paymentRefundRecordDao;
        this.gatewayServiceClient = gatewayServiceClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建支付记录
     *
     * @param paymentRecord 支付记录实体
     * @return 创建的支付记录
     */
    public PaymentRecordEntity createPaymentRecord(PaymentRecordEntity paymentRecord) {
        log.info("[支付记录管理] 创建支付记录: userId={}, amount={}, method={}",
                paymentRecord.getUserId(), paymentRecord.getPaymentAmount(), paymentRecord.getPaymentMethod());

        try {
            // 1. 设置基本信息
            paymentRecord.setPaymentId("PAY-" + UUID.randomUUID().toString().replace("-", ""));

            // 2. 生成订单号和交易流水号
            if (paymentRecord.getOrderNo() == null || paymentRecord.getOrderNo().trim().isEmpty()) {
                paymentRecord.setOrderNo(generateOrderNo());
            }
            if (paymentRecord.getTransactionNo() == null || paymentRecord.getTransactionNo().trim().isEmpty()) {
                paymentRecord.setTransactionNo(generateTransactionNo());
            }

            // 3. 设置默认值
            paymentRecord.setPaymentStatus(1); // 待支付
            paymentRecord.setSettlementStatus(1); // 未结算
            paymentRecord.setAuditStatus(0); // 无需审核

            // 4. 计算实付金额
            if (paymentRecord.getActualAmount() == null) {
                paymentRecord.setActualAmount(paymentRecord.getPaymentAmount());
            }

            // 5. 保存到数据库
            int result = paymentRecordDao.insert(paymentRecord);
            if (result <= 0) {
                log.error("[支付记录管理] 支付记录创建失败: 数据库插入返回0");
                throw new BusinessException("PAYMENT_RECORD_CREATE_FAILED", "支付记录创建失败");
            }

            log.info("[支付记录管理] 支付记录创建成功: paymentId={}, orderNo={}, amount={}",
                    paymentRecord.getPaymentId(), paymentRecord.getOrderNo(), paymentRecord.getPaymentAmount());

            return paymentRecord;

        } catch (Exception e) {
            log.error("[支付记录管理] 支付记录创建失败: userId={}, amount={}, error={}",
                    paymentRecord.getUserId(), paymentRecord.getPaymentAmount(), e.getMessage(), e);
            throw new SystemException("PAYMENT_RECORD_CREATE_ERROR", "支付记录创建失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新支付状态
     *
     * @param paymentId 支付记录ID
     * @param status 新状态
     * @param thirdPartyOrderNo 第三方订单号
     * @param thirdPartyTransactionNo 第三方交易号
     * @return 是否更新成功
     */
    public boolean updatePaymentStatus(String paymentId, Integer status,
                                     String thirdPartyOrderNo, String thirdPartyTransactionNo) {
        log.info("[支付记录管理] 更新支付状态: paymentId={}, status={}", paymentId, status);

        try {
            PaymentRecordEntity paymentRecord = paymentRecordDao.selectById(paymentId);
            if (paymentRecord == null) {
                throw new IllegalArgumentException("支付记录不存在");
            }

            // 更新状态和时间
            paymentRecord.setPaymentStatus(status);
            if (thirdPartyOrderNo != null) {
                paymentRecord.setThirdPartyOrderNo(thirdPartyOrderNo);
            }
            if (thirdPartyTransactionNo != null) {
                paymentRecord.setThirdPartyTransactionNo(thirdPartyTransactionNo);
            }

            LocalDateTime now = LocalDateTime.now();
            if (status == 3) { // 支付成功
                paymentRecord.setPaymentTime(now);
                paymentRecord.setCompleteTime(now);
            } else if (status == 4 || status == 7) { // 支付失败或已取消
                paymentRecord.setCompleteTime(now);
            }

            int result = paymentRecordDao.updateById(paymentRecord);
            boolean success = result > 0;

            if (success) {
                log.info("[支付记录管理] 支付状态更新成功: paymentId={}, status={}", paymentId, status);
            } else {
                log.error("[支付记录管理] 支付状态更新失败: paymentId={}, status={}", paymentId, status);
            }

            return success;

        } catch (Exception e) {
            log.error("[支付记录管理] 支付状态更新异常: paymentId={}, status={}, error={}",
                    paymentId, status, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 处理退款
     *
     * @param refundRecord 退款记录实体
     * @return 处理结果
     */
    public Map<String, Object> processRefund(PaymentRefundRecordEntity refundRecord) {
        log.info("[支付记录管理] 处理退款: paymentId={}, amount={}, type={}",
                refundRecord.getPaymentId(), refundRecord.getRefundAmount(), refundRecord.getRefundType());

        try {
            // 1. 验证原支付记录
            PaymentRecordEntity paymentRecord = paymentRecordDao.selectById(refundRecord.getPaymentId());
            if (paymentRecord == null) {
                throw new IllegalArgumentException("原支付记录不存在");
            }

            // 2. 检查退款金额
            if (refundRecord.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("退款金额必须大于0");
            }

            // 3. 检查退款金额是否超过可退款金额
            BigDecimal availableRefundAmount = calculateAvailableRefundAmount(paymentRecord.getPaymentId());
            if (refundRecord.getRefundAmount().compareTo(availableRefundAmount) > 0) {
                throw new IllegalArgumentException("退款金额超过可退款金额");
            }

            // 4. 创建退款记录
            refundRecord.setRefundId("REF-" + UUID.randomUUID().toString().replace("-", ""));
            refundRecord.setRefundNo(generateRefundNo());
            refundRecord.setRefundTransactionNo(generateRefundTransactionNo());
            refundRecord.setRefundStatus(1); // 申请中
            refundRecord.setApplyTime(LocalDateTime.now());

            if (refundRecord.getActualRefundAmount() == null) {
                refundRecord.setActualRefundAmount(refundRecord.getRefundAmount());
            }

            int result = paymentRefundRecordDao.insert(refundRecord);
            if (result <= 0) {
                log.error("[支付记录管理] 退款记录创建失败, refundId={}", refundRecord.getRefundId());
                throw new BusinessException("REFUND_RECORD_CREATE_FAILED", "退款记录创建失败");
            }

            // 5. 更新原支付记录的退款金额
            BigDecimal currentRefundAmount = paymentRecord.getRefundAmount() != null ?
                paymentRecord.getRefundAmount() : BigDecimal.ZERO;
            paymentRecord.setRefundAmount(currentRefundAmount.add(refundRecord.getRefundAmount()));

            // 6. 如果是全额退款，更新支付状态
            if (refundRecord.getRefundType() == 1) { // 全额退款
                paymentRecord.setPaymentStatus(5); // 已退款
            } else {
                paymentRecord.setPaymentStatus(6); // 部分退款
            }

            paymentRecordDao.updateById(paymentRecord);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("refundId", refundRecord.getRefundId());
            response.put("refundNo", refundRecord.getRefundNo());
            response.put("message", "退款申请已提交");

            log.info("[支付记录管理] 退款处理成功: refundId={}, paymentId={}, amount={}",
                    refundRecord.getRefundId(), refundRecord.getPaymentId(), refundRecord.getRefundAmount());

            return response;

        } catch (Exception e) {
            log.error("[支付记录管理] 退款处理失败: paymentId={}, amount={}, error={}",
                    refundRecord.getPaymentId(), refundRecord.getRefundAmount(), e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    /**
     * 执行对账
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param merchantId 商户ID（可选）
     * @return 对账结果
     */
    public Map<String, Object> performReconciliation(LocalDateTime startTime, LocalDateTime endTime, Long merchantId) {
        log.info("[支付记录管理] 执行对账: startTime={}, endTime={}, merchantId={}", startTime, endTime, merchantId);

        try {
            // 1. 查询本地支付记录
            List<PaymentRecordEntity> localPayments = paymentRecordDao.selectForReconciliation(
                startTime, endTime, merchantId);

            // 2. 查询第三方支付记录
            Map<String, Object> thirdPartyRequest = new HashMap<>();
            thirdPartyRequest.put("startTime", startTime);
            thirdPartyRequest.put("endTime", endTime);
            if (merchantId != null) {
                thirdPartyRequest.put("merchantId", merchantId);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> thirdPartyPayments = (List<Map<String, Object>>) gatewayServiceClient
                .callCommonService("/api/v1/payment/third-party/reconciliation",
                    org.springframework.http.HttpMethod.POST, thirdPartyRequest, Map.class)
                .getData();

            // 3. 执行对账逻辑
            Map<String, Object> reconciliationResult = performReconciliationLogic(localPayments, thirdPartyPayments);

            log.info("[支付记录管理] 对账完成: localCount={}, thirdPartyCount={}, mismatchCount={}",
                    localPayments.size(), thirdPartyPayments.size(), reconciliationResult.get("mismatchCount"));

            return reconciliationResult;

        } catch (Exception e) {
            log.error("[支付记录管理] 对账失败: startTime={}, endTime={}, error={}", startTime, endTime, e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    /**
     * 获取用户支付统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    public Map<String, Object> getUserPaymentStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("[支付记录管理] 获取用户支付统计: userId={}, startTime={}, endTime={}", userId, startTime, endTime);

        try {
            // 查询用户支付记录
            List<PaymentRecordEntity> payments = paymentRecordDao.selectUserPaymentStatistics(userId, startTime, endTime);

            // 计算统计数据
            Map<String, Object> statistics = new HashMap<>();

            BigDecimal totalAmount = payments.stream()
                .filter(p -> p.getPaymentStatus() == 3) // 支付成功
                .map(PaymentRecordEntity::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalRefundAmount = payments.stream()
                .filter(p -> p.getPaymentStatus() >= 5) // 已退款或部分退款
                .map(p -> p.getRefundAmount() != null ? p.getRefundAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            long successCount = payments.stream()
                .filter(p -> p.getPaymentStatus() == 3)
                .count();

            long refundCount = payments.stream()
                .filter(p -> p.getPaymentStatus() >= 5)
                .count();

            statistics.put("userId", userId);
            statistics.put("startTime", startTime);
            statistics.put("endTime", endTime);
            statistics.put("totalAmount", totalAmount);
            statistics.put("totalRefundAmount", totalRefundAmount);
            statistics.put("actualAmount", totalAmount.subtract(totalRefundAmount));
            statistics.put("totalCount", payments.size());
            statistics.put("successCount", successCount);
            statistics.put("refundCount", refundCount);
            statistics.put("successRate", !payments.isEmpty() ? (double) successCount / payments.size() : 0.0);

            // 按支付方式分组统计
            Map<Integer, Long> paymentMethodStats = payments.stream()
                .collect(Collectors.groupingBy(PaymentRecordEntity::getPaymentMethod, Collectors.counting()));
            statistics.put("paymentMethodStats", paymentMethodStats);

            // 按业务类型分组统计
            Map<Integer, Long> businessTypeStats = payments.stream()
                .collect(Collectors.groupingBy(PaymentRecordEntity::getBusinessType, Collectors.counting()));
            statistics.put("businessTypeStats", businessTypeStats);

            return statistics;

        } catch (Exception e) {
            log.error("[支付记录管理] 用户支付统计获取失败: userId={}, error={}", userId, e.getMessage(), e);
            throw new SystemException("USER_PAYMENT_STATISTICS_ERROR", "用户支付统计获取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取商户结算统计
     *
     * @param merchantId 商户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 结算统计
     */
    public Map<String, Object> getMerchantSettlementStatistics(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("[支付记录管理] 获取商户结算统计: merchantId={}, startTime={}, endTime={}", merchantId, startTime, endTime);

        try {
            // 查询商户支付记录
            List<PaymentRecordEntity> payments = paymentRecordDao.selectMerchantSettlementStatistics(merchantId, startTime, endTime);

            // 计算结算统计数据
            Map<String, Object> statistics = new HashMap<>();

            BigDecimal totalAmount = payments.stream()
                .filter(p -> p.getPaymentStatus() == 3) // 支付成功
                .map(PaymentRecordEntity::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalFee = payments.stream()
                .filter(p -> p.getPaymentFee() != null && p.getPaymentStatus() == 3)
                .map(PaymentRecordEntity::getPaymentFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalSettlementAmount = payments.stream()
                .filter(p -> p.getSettlementAmount() != null && p.getSettlementStatus() == 2) // 已结算
                .map(PaymentRecordEntity::getSettlementAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            long settledCount = payments.stream()
                .filter(p -> p.getSettlementStatus() == 2)
                .count();

            long unsettledCount = payments.stream()
                .filter(p -> p.getPaymentStatus() == 3 && p.getSettlementStatus() == 1)
                .count();

            statistics.put("merchantId", merchantId);
            statistics.put("startTime", startTime);
            statistics.put("endTime", endTime);
            statistics.put("totalAmount", totalAmount);
            statistics.put("totalFee", totalFee);
            statistics.put("totalSettlementAmount", totalSettlementAmount);
            statistics.put("netAmount", totalAmount.subtract(totalFee));
            statistics.put("pendingSettlementAmount", totalAmount.subtract(totalSettlementAmount));
            statistics.put("totalCount", payments.size());
            statistics.put("settledCount", settledCount);
            statistics.put("unsettledCount", unsettledCount);

            return statistics;

        } catch (Exception e) {
            log.error("[支付记录管理] 商户结算统计获取失败: merchantId={}, error={}", merchantId, e.getMessage(), e);
            throw new SystemException("MERCHANT_SETTLEMENT_STATISTICS_ERROR", "商户结算统计获取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 生成交易流水号
     */
    private String generateTransactionNo() {
        return "TXN" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        return "REF" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 生成退款交易流水号
     */
    private String generateRefundTransactionNo() {
        return "RTX" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 计算可退款金额
     */
    private BigDecimal calculateAvailableRefundAmount(String paymentId) {
        try {
            PaymentRecordEntity paymentRecord = paymentRecordDao.selectById(paymentId);
            if (paymentRecord == null) {
                return BigDecimal.ZERO;
            }

            // 可退款金额 = 实付金额 - 已退款金额
            BigDecimal paidAmount = paymentRecord.getActualAmount();
            BigDecimal refundedAmount = paymentRecord.getRefundAmount() != null ?
                paymentRecord.getRefundAmount() : BigDecimal.ZERO;

            return paidAmount.subtract(refundedAmount);

        } catch (Exception e) {
            log.error("[支付记录管理] 计算可退款金额失败: paymentId={}, error={}", paymentId, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 执行对账逻辑
     */
    private Map<String, Object> performReconciliationLogic(List<PaymentRecordEntity> localPayments,
                                                         List<Map<String, Object>> thirdPartyPayments) {
        Map<String, Object> result = new HashMap<>();

        // 构建第三方支付记录映射
        Map<String, Map<String, Object>> thirdPartyMap = thirdPartyPayments.stream()
            .collect(Collectors.toMap(
                payment -> (String) payment.get("transactionNo"),
                payment -> payment
            ));

        int matchedCount = 0;
        int mismatchCount = 0;
        List<Map<String, Object>> mismatches = new ArrayList<>();

        for (PaymentRecordEntity localPayment : localPayments) {
            String transactionNo = localPayment.getTransactionNo();
            Map<String, Object> thirdPartyPayment = thirdPartyMap.get(transactionNo);

            if (thirdPartyPayment != null) {
                // 检查金额是否匹配
                BigDecimal localAmount = localPayment.getActualAmount();
                BigDecimal thirdPartyAmount = new BigDecimal(thirdPartyPayment.get("amount").toString());

                if (localAmount.compareTo(thirdPartyAmount) == 0) {
                    matchedCount++;
                } else {
                    mismatchCount++;
                    mismatches.add(createMismatchRecord(localPayment, thirdPartyPayment, "金额不匹配"));
                }
            } else {
                mismatchCount++;
                mismatches.add(createMissingRecord(localPayment, "第三方记录缺失"));
            }
        }

        result.put("success", true);
        result.put("localCount", localPayments.size());
        result.put("thirdPartyCount", thirdPartyPayments.size());
        result.put("matchedCount", matchedCount);
        result.put("mismatchCount", mismatchCount);
        result.put("mismatches", mismatches);

        return result;
    }

    /**
     * 创建不匹配记录
     */
    private Map<String, Object> createMismatchRecord(PaymentRecordEntity localPayment,
                                                      Map<String, Object> thirdPartyPayment,
                                                      String reason) {
        Map<String, Object> mismatch = new HashMap<>();
        mismatch.put("localPayment", localPayment);
        mismatch.put("thirdPartyPayment", thirdPartyPayment);
        mismatch.put("reason", reason);
        mismatch.put("localAmount", localPayment.getActualAmount());
        mismatch.put("thirdPartyAmount", thirdPartyPayment.get("amount"));
        return mismatch;
    }

    /**
     * 创建缺失记录
     */
    private Map<String, Object> createMissingRecord(PaymentRecordEntity localPayment, String reason) {
        Map<String, Object> missing = new HashMap<>();
        missing.put("localPayment", localPayment);
        missing.put("reason", reason);
        missing.put("localAmount", localPayment.getActualAmount());
        return missing;
    }
}




