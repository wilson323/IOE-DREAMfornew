package net.lab1024.sa.consume.service.payment.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.java.service.payments.model.Transaction;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.service.payment.PaymentCallbackService;
import net.lab1024.sa.consume.service.payment.PaymentRecordService;
import net.lab1024.sa.consume.service.payment.adapter.AlipayPayAdapter;
import net.lab1024.sa.consume.service.payment.adapter.WechatPayAdapter;

/**
 * 支付回调服务实现类
 * <p>
 * 专门处理支付回调相关业务，从PaymentService拆分而来
 * 严格遵循CLAUDE.md规范：
 * - Service实现类使用@Service注解
 * - 依赖注入使用@Resource注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class PaymentCallbackServiceImpl implements PaymentCallbackService {

    @Resource
    private WechatPayAdapter wechatPayAdapter;

    @Resource
    private AlipayPayAdapter alipayPayAdapter;

    @Resource
    private PaymentRecordService paymentRecordService;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> handleWechatPayNotify(String notifyData) {
        return handleWechatPayNotify(notifyData, null, null, null, null);
    }

    @Override
    public Map<String, Object> handleWechatPayNotify(String notifyData, String signature,
            String timestamp, String nonce, String serial) {
        log.info("[微信支付] 接收回调通知");

        try {
            if (!StringUtils.hasText(notifyData)) {
                log.error("[微信支付] 回调数据为空");
                return buildFailResponse("回调数据为空");
            }

            wechatPayAdapter.initIfNeeded();
            if (!wechatPayAdapter.isReady()) {
                log.error("[微信支付] 微信支付配置未初始化");
                return buildFailResponse("配置未初始化");
            }

            Transaction transaction = null;
            try {
                if (StringUtils.hasText(signature) && StringUtils.hasText(timestamp)
                        && StringUtils.hasText(nonce) && StringUtils.hasText(serial)) {
                    log.info("[微信支付] 收到完整回调信息，进行签名验证");

                    // 已通过StringUtils.hasText验证非空，使用Objects.requireNonNull确保类型安全
                    boolean isValid = wechatPayAdapter.verifyWechatPaySignature(
                            java.util.Objects.requireNonNull(serial),
                            java.util.Objects.requireNonNull(timestamp),
                            java.util.Objects.requireNonNull(nonce),
                            java.util.Objects.requireNonNull(notifyData),
                            java.util.Objects.requireNonNull(signature));
                    if (!isValid) {
                        log.error("[微信支付] 签名验证失败");
                        return buildFailResponse("签名验证失败");
                    }

                    transaction = objectMapper.readValue(notifyData, Transaction.class);
                    log.info("[微信支付] 签名验证通过，交易数据解析成功");
                } else {
                    log.warn("[微信支付] 未提供完整HTTP请求头，跳过签名验证（仅开发环境）");
                    transaction = objectMapper.readValue(notifyData, Transaction.class);
                }
            } catch (Exception e) {
                log.error("[微信支付] 回调数据解析或验证失败", e);
                return buildFailResponse("数据解析或验证失败: " + e.getMessage());
            }

            if (transaction == null) {
                log.error("[微信支付] 回调数据解析失败，transaction为null");
                return buildFailResponse("数据解析失败");
            }

            String tradeState = transaction.getTradeState() != null ? transaction.getTradeState().name() : null;
            if (tradeState == null || !"SUCCESS".equals(tradeState)) {
                log.warn("[微信支付] 支付未成功，状态：{}", tradeState);
                String paymentId = transaction.getOutTradeNo();
                if (paymentId != null) {
                    recordPaymentAuditLog(paymentId, "支付回调-未成功", "状态=" + tradeState, 0);
                }
                return buildFailResponse("支付未成功，状态: " + tradeState);
            }

            String paymentId = transaction.getOutTradeNo();
            String transactionId = transaction.getTransactionId();
            Integer totalFee = transaction.getAmount() != null ? transaction.getAmount().getTotal() : null;

            if (paymentId == null || transactionId == null) {
                log.error("[微信支付] 订单信息不完整，paymentId={}, transactionId={}", paymentId, transactionId);
                return buildFailResponse("订单信息不完整");
            }

            PaymentRecordEntity existingRecord = paymentRecordService.getPaymentRecord(paymentId);
            if (existingRecord != null && existingRecord.getPaymentStatus() != null && existingRecord.getPaymentStatus() == 3) {
                log.warn("[微信支付] 订单已处理，跳过重复回调，paymentId={}", paymentId);
                return buildSuccessResponse();
            }

            if (existingRecord != null && totalFee != null) {
                BigDecimal recordAmount = existingRecord.getPaymentAmount();
                if (recordAmount != null) {
                    BigDecimal callbackAmount = new BigDecimal(totalFee).divide(new BigDecimal("100"));
                    if (callbackAmount.compareTo(recordAmount) != 0) {
                        log.error("[微信支付] 金额不一致，记录金额={}，回调金额={}，paymentId={}",
                                recordAmount, callbackAmount, paymentId);
                        recordPaymentAuditLog(paymentId, "支付回调-金额不一致",
                                "记录金额=" + recordAmount + ",回调金额=" + callbackAmount, 0);
                        return buildFailResponse("金额不一致");
                    }
                }
            }

            paymentRecordService.updatePaymentStatus(paymentId, "SUCCESS", transactionId);
            paymentRecordService.handlePaymentSuccess(paymentId, transactionId);
            recordPaymentAuditLog(paymentId, "支付回调-成功", "transactionId=" + transactionId, 1);

            if (existingRecord != null && existingRecord.getUserId() != null) {
                sendPaymentNotification(existingRecord.getUserId(), paymentId, "SUCCESS",
                        "支付成功，交易号: " + transactionId);
            }

            log.info("[微信支付] 回调处理成功，paymentId={}, transactionId={}", paymentId, transactionId);
            return buildSuccessResponse();

        } catch (BusinessException e) {
            log.error("[微信支付] 处理回调业务异常，error={}", e.getMessage(), e);
            return buildFailResponse("业务处理失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[微信支付] 处理回调未知异常，error={}", e.getMessage(), e);
            recordPaymentAuditLog("UNKNOWN", "支付回调-系统异常",
                    "异常类型: " + e.getClass().getName() + ", 消息: " + e.getMessage(), 0);
            return buildFailResponse("系统处理失败，请稍后重试");
        }
    }

    @Override
    public String handleAlipayNotify(Map<String, String> params) {
        log.info("[支付宝] 接收回调通知，参数数量：{}", params != null ? params.size() : 0);

        try {
            if (params == null || params.isEmpty()) {
                log.error("[支付宝] 回调参数为空");
                return "fail";
            }

            if (!alipayPayAdapter.verifyNotifySignature(params)) {
                log.error("[支付宝] 签名验证失败，可能存在安全风险");
                return "fail";
            }

            log.info("[支付宝] 签名验证通过");

            String tradeStatus = params.get("trade_status");
            if (tradeStatus == null) {
                log.error("[支付宝] 支付状态为空");
                return "fail";
            }

            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.warn("[支付宝] 支付未完成，状态：{}", tradeStatus);
                return "fail";
            }

            String paymentIdRaw = params.get("out_trade_no");
            String tradeNoRaw = params.get("trade_no");
            String totalAmountStr = params.get("total_amount");

            if (!StringUtils.hasText(paymentIdRaw) || !StringUtils.hasText(tradeNoRaw)) {
                log.error("[支付宝] 订单信息不完整，paymentId={}, tradeNo={}", paymentIdRaw, tradeNoRaw);
                return "fail";
            }

            // 已验证非空，安全转换
            String paymentId = java.util.Objects.requireNonNull(paymentIdRaw);
            String tradeNo = java.util.Objects.requireNonNull(tradeNoRaw);

            PaymentRecordEntity existingRecord = paymentRecordService.getPaymentRecord(paymentId);
            if (existingRecord != null && existingRecord.getPaymentStatus() != null && existingRecord.getPaymentStatus() == 3) {
                log.warn("[支付宝] 订单已处理，跳过重复回调，paymentId={}", paymentId);
                return "success";
            }

            if (existingRecord != null && StringUtils.hasText(totalAmountStr)) {
                try {
                    BigDecimal callbackAmount = new BigDecimal(totalAmountStr);
                    BigDecimal recordAmount = existingRecord.getPaymentAmount();
                    if (recordAmount != null && callbackAmount.compareTo(recordAmount) != 0) {
                        log.error("[支付宝] 金额不一致，记录金额={}，回调金额={}，paymentId={}",
                                recordAmount, callbackAmount, paymentId);
                        return "fail";
                    }
                } catch (NumberFormatException e) {
                    log.warn("[支付宝] 金额格式错误，totalAmount={}", totalAmountStr);
                }
            }

            paymentRecordService.updatePaymentStatus(paymentId, "SUCCESS", tradeNo);
            paymentRecordService.handlePaymentSuccess(paymentId, tradeNo);

            log.info("[支付宝] 回调处理成功，paymentId={}, tradeNo={}", paymentId, tradeNo);
            return "success";

        } catch (BusinessException e) {
            log.error("[支付宝] 处理回调业务异常，error={}", e.getMessage(), e);
            return "fail";
        } catch (Exception e) {
            log.error("[支付宝] 处理回调未知异常，error={}", e.getMessage(), e);
            String paymentId = params != null ? params.get("out_trade_no") : "UNKNOWN";
            recordPaymentAuditLog(paymentId, "支付回调-系统异常",
                    "异常类型: " + e.getClass().getName() + ", 消息: " + e.getMessage(), 0);
            return "fail";
        }
    }

    @Override
    public void recordPaymentAuditLog(String paymentId, String operation, String detail, Integer result) {
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("moduleName", "CONSUME_PAYMENT");
            auditData.put("operationDesc", operation);
            auditData.put("resourceId", paymentId);
            auditData.put("requestParams", detail);
            auditData.put("resultStatus", result);

            gatewayServiceClient.callCommonService(
                    "/api/v1/audit/log",
                    HttpMethod.POST,
                    auditData,
                    new TypeReference<ResponseDTO<String>>() {});

            log.debug("[支付回调] 审计日志记录成功，paymentId={}, operation={}", paymentId, operation);
        } catch (Exception e) {
            log.error("[支付回调] 审计日志记录失败，paymentId={}", paymentId, e);
        }
    }

    @Override
    public void sendPaymentNotification(Long userId, String paymentId, String status, String message) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("recipientUserId", userId);
            notificationData.put("channel", 4);
            notificationData.put("subject", "支付通知");
            notificationData.put("content", message);
            notificationData.put("businessType", "PAYMENT");
            notificationData.put("businessId", paymentId);
            notificationData.put("messageType", 2);
            notificationData.put("priority", 2);

            gatewayServiceClient.callCommonService(
                    "/api/v1/notification/send",
                    HttpMethod.POST,
                    notificationData,
                    new TypeReference<ResponseDTO<Long>>() {});

            log.debug("[支付回调] 通知发送成功，userId={}, paymentId={}", userId, paymentId);
        } catch (Exception e) {
            log.error("[支付回调] 通知发送失败，userId={}, paymentId={}", userId, paymentId, e);
        }
    }

    private Map<String, Object> buildSuccessResponse() {
        return Map.of("code", "SUCCESS", "message", "OK");
    }

    private Map<String, Object> buildFailResponse(String message) {
        return Map.of("code", "FAIL", "message", message != null ? message : "未知错误");
    }
}
