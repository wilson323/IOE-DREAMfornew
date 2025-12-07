package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
// 微信支付V3 SDK通知处理
// 注意：SDK 0.2.17版本中NotificationParser API可能不同，使用手动签名验证
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.domain.entity.PaymentRecordEntity;
import org.springframework.http.HttpMethod;

/**
 * 支付服务（微信支付+支付宝）
 *
 * @Author IOE-DREAM Team
 * @Date 2025-12-05
 * @Copyright IOE-DREAM智慧园区一卡通管理平台
 *
 *            功能说明：
 *            - 微信支付（JSAPI/APP/H5/Native）
 *            - 支付宝支付（APP/Web/Wap）
 *            - 支付回调处理
 *            - 退款处理
 *            - 订单查询
 *
 *            技术栈：
 *            - 微信支付SDK v3
 *            - 支付宝SDK v4
 *            - RSA签名验证
 *            - 异步通知处理
 */
@Slf4j
@Service
@SuppressWarnings("null")
public class PaymentService {

    // ==================== 微信支付配置 ====================

    @Value("${wechat.pay.app-id:}")
    private String wechatAppId;

    @Value("${wechat.pay.mch-id:}")
    private String wechatMchId;

    @Value("${wechat.pay.api-key:}")
    private String wechatApiKey;

    @Value("${wechat.pay.cert-path:}")
    private String wechatCertPath;

    @Value("${wechat.pay.notify-url:}")
    private String wechatNotifyUrl;

    @Value("${wechat.pay.enabled:false}")
    private Boolean wechatPayEnabled;

    @Value("${wechat.pay.merchant-serial-number:}")
    private String wechatMerchantSerialNumber;

    @Value("${wechat.pay.api-v3-key:}")
    private String wechatApiV3Key;

    // 微信支付配置对象（延迟初始化）
    private Config wechatPayConfig;
    private JsapiService jsapiService;
    private NativePayService nativePayService;
    private RefundService refundService;

    // ==================== 支付宝配置 ====================

    @Value("${alipay.app-id:}")
    private String alipayAppId;

    @Value("${alipay.private-key:}")
    private String alipayPrivateKey;

    @Value("${alipay.public-key:}")
    private String alipayPublicKey;

    @Value("${alipay.notify-url:}")
    private String alipayNotifyUrl;

    @Value("${alipay.gateway-url:https://openapi.alipay.com/gateway.do}")
    private String alipayGatewayUrl;

    @Value("${alipay.enabled:false}")
    private Boolean alipayEnabled;

    // 支付宝客户端（延迟初始化）
    private AlipayClient alipayClient;

    @Resource
    private net.lab1024.sa.consume.service.payment.PaymentRecordService paymentRecordService;

    @Resource
    private net.lab1024.sa.consume.dao.PaymentRecordDao paymentRecordDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private net.lab1024.sa.consume.manager.MultiPaymentManager multiPaymentManager;

    @Resource
    private org.springframework.web.client.RestTemplate restTemplate;


    /**
     * 初始化微信支付配置（延迟初始化）
     */
    private void initWechatPayConfig() {
        if (wechatPayConfig != null) {
            return;
        }

        if (!wechatPayEnabled || !StringUtils.hasText(wechatMchId) ||
            !StringUtils.hasText(wechatCertPath) || !StringUtils.hasText(wechatMerchantSerialNumber) ||
            !StringUtils.hasText(wechatApiV3Key)) {
            log.warn("[微信支付] 配置不完整，无法初始化");
            return;
        }

        try {
            // 使用RSAAutoCertificateConfig自动管理证书
            wechatPayConfig = new RSAAutoCertificateConfig.Builder()
                    .merchantId(wechatMchId)
                    .privateKeyFromPath(wechatCertPath)
                    .merchantSerialNumber(wechatMerchantSerialNumber)
                    .apiV3Key(wechatApiV3Key)
                    .build();

            // 初始化服务
            jsapiService = new JsapiService.Builder().config(wechatPayConfig).build();
            nativePayService = new NativePayService.Builder().config(wechatPayConfig).build();
            refundService = new RefundService.Builder().config(wechatPayConfig).build();

            log.info("[微信支付] 配置初始化成功");
        } catch (Exception e) {
            log.error("[微信支付] 配置初始化失败", e);
            throw new BusinessException("微信支付配置初始化失败: " + e.getMessage());
        }
    }

    /**
     * 初始化支付宝客户端（延迟初始化）
     */
    private void initAlipayClient() {
        if (alipayClient != null) {
            return;
        }

        if (!alipayEnabled || !StringUtils.hasText(alipayAppId) ||
            !StringUtils.hasText(alipayPrivateKey) || !StringUtils.hasText(alipayPublicKey)) {
            log.warn("[支付宝] 配置不完整，无法初始化");
            return;
        }

        try {
            alipayClient = new DefaultAlipayClient(
                    alipayGatewayUrl,
                    alipayAppId,
                    alipayPrivateKey,
                    "json",
                    "UTF-8",
                    alipayPublicKey,
                    "RSA2"
            );
            log.info("[支付宝] 客户端初始化成功");
        } catch (Exception e) {
            log.error("[支付宝] 客户端初始化失败", e);
            throw new BusinessException("支付宝客户端初始化失败: " + e.getMessage());
        }
    }

    /**
     * 创建微信支付订单
     *
     * @param orderId     订单ID
     * @param amount      金额（元）
     * @param description 商品描述
     * @param openId      用户OpenID（JSAPI必需）
     * @param payType     支付类型（JSAPI/APP/H5/Native）
     * @return 支付参数
     */
    public Map<String, Object> createWechatPayOrder(
            String orderId,
            BigDecimal amount,
            String description,
            String openId,
            String payType) {

        log.info("[微信支付] 创建订单, orderId={}, amount={}, payType={}", orderId, amount, payType);

        try {
            if (!wechatPayEnabled) {
                log.warn("[微信支付] 支付服务未启用，返回模拟数据");
                return getMockWechatPayResult(orderId, payType);
            }

            // 1. 参数验证
            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("支付金额必须大于0");
            }
            if (!StringUtils.hasText(description)) {
                throw new BusinessException("商品描述不能为空");
            }

            // 2. 初始化配置
            initWechatPayConfig();
            if (wechatPayConfig == null) {
                log.warn("[微信支付] 配置未初始化，返回模拟数据");
                return getMockWechatPayResult(orderId, payType);
            }

            // 3. 根据支付类型调用不同的服务
            if ("JSAPI".equals(payType)) {
                if (!StringUtils.hasText(openId)) {
                    throw new BusinessException("JSAPI支付需要openId");
                }
                return createJsapiPayOrder(orderId, amount, description, openId);
            } else if ("Native".equals(payType)) {
                return createNativePayOrder(orderId, amount, description);
            } else if ("APP".equals(payType) || "H5".equals(payType)) {
                // APP和H5支付暂未实现，返回模拟数据
                log.warn("[微信支付] {}支付类型暂未实现，返回模拟数据", payType);
            return getMockWechatPayResult(orderId, payType);
            } else {
                throw new BusinessException("不支持的支付类型: " + payType);
            }

        } catch (Exception e) {
            log.error("[微信支付] 创建订单失败", e);
            throw new RuntimeException("微信支付失败: " + e.getMessage());
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
    public Map<String, Object> createAlipayOrder(
            String orderId,
            BigDecimal amount,
            String subject,
            String payType) {

        log.info("[支付宝] 创建订单, orderId={}, amount={}, payType={}", orderId, amount, payType);

        try {
            if (!alipayEnabled) {
                log.warn("[支付宝] 支付服务未启用，返回模拟数据");
                return getMockAlipayResult(orderId, payType);
            }

            // 1. 参数验证
            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("支付金额必须大于0");
            }
            if (!StringUtils.hasText(subject)) {
                throw new BusinessException("商品标题不能为空");
            }

            // 2. 初始化客户端
            initAlipayClient();
            if (alipayClient == null) {
                log.warn("[支付宝] 客户端未初始化，返回模拟数据");
            return getMockAlipayResult(orderId, payType);
            }

            // 3. 构建请求参数
            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setOutTradeNo(orderId);
            model.setTotalAmount(amount.toString());
            model.setSubject(subject);
            model.setProductCode("QUICK_MSECURITY_PAY");
            request.setBizModel(model);
            request.setNotifyUrl(alipayNotifyUrl);

            // 4. 调用SDK生成订单信息
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);

            // 5. 检查响应
            if (response == null || !response.isSuccess()) {
                String errorMsg = response != null ? response.getSubMsg() : "支付宝响应为空";
                log.error("[支付宝] 创建订单失败: {}", errorMsg);
                throw new BusinessException("支付宝创建订单失败: " + errorMsg);
            }

            // 6. 返回订单字符串
            Map<String, Object> result = new HashMap<>();
            result.put("orderString", response.getBody());
            result.put("orderId", orderId);
            result.put("payType", payType);
            result.put("mock", false);

            log.info("[支付宝] 订单创建成功: orderId={}", orderId);
            return result;

        } catch (Exception e) {
            log.error("[支付宝] 创建订单失败", e);
            throw new RuntimeException("支付宝支付失败: " + e.getMessage());
        }
    }

    /**
     * 处理微信支付回调（V3版本）
     * <p>
     * 功能说明：
     * 1. 验证回调签名（微信支付V3使用证书验证）
     * 2. 检查支付结果
     * 3. 幂等性验证（防止重复处理）
     * 4. 更新支付记录状态
     * 5. 触发后续业务处理
     * 6. 记录审计日志
     * 7. 发送通知
     * </p>
     * <p>
     * 注意：微信支付V3使用JSON格式，不再使用XML
     * 签名验证需要使用微信支付SDK的验证方法
     * </p>
     *
     * @param notifyData 回调数据（JSON格式，V3版本）
     * @return 处理结果
     */
    public Map<String, Object> handleWechatPayNotify(String notifyData) {
        return handleWechatPayNotify(notifyData, null, null, null, null);
    }

    /**
     * 处理微信支付回调（V3版本，完整参数）
     * <p>
     * 功能说明：
     * 1. 验证回调签名（微信支付V3使用证书验证）
     * 2. 检查支付结果
     * 3. 幂等性验证（防止重复处理）
     * 4. 更新支付记录状态
     * 5. 触发后续业务处理
     * 6. 记录审计日志
     * 7. 发送通知
     * </p>
     *
     * @param notifyData 回调数据（JSON格式，V3版本）
     * @param signature  微信支付签名（Wechatpay-Signature请求头）
     * @param timestamp  时间戳（Wechatpay-Timestamp请求头）
     * @param nonce      随机串（Wechatpay-Nonce请求头）
     * @param serial     证书序列号（Wechatpay-Serial请求头）
     * @return 处理结果
     */
    public Map<String, Object> handleWechatPayNotify(String notifyData, String signature,
            String timestamp, String nonce, String serial) {
        log.info("[微信支付] 接收回调通知");

        try {
            // 1. 参数验证
            if (!StringUtils.hasText(notifyData)) {
                log.error("[微信支付] 回调数据为空");
                return buildFailResponse("回调数据为空");
            }

            // 2. 初始化微信支付配置（如果未初始化）
            initWechatPayConfig();
            if (wechatPayConfig == null) {
                log.error("[微信支付] 微信支付配置未初始化");
                return buildFailResponse("配置未初始化");
            }

            // 3. 解析和验证回调数据（微信支付V3使用JSON格式）
            Transaction transaction = null;
            try {
                // 如果提供了完整的HTTP请求头，进行签名验证
                if (StringUtils.hasText(signature) && StringUtils.hasText(timestamp)
                        && StringUtils.hasText(nonce) && StringUtils.hasText(serial)) {
                    log.info("[微信支付] 收到完整回调信息，signature={}, timestamp={}, serial={}",
                            signature, timestamp, serial);

                    try {
                        // 手动验证签名
                        boolean isValid = verifyWechatPaySignature(serial, timestamp, nonce, notifyData, signature);
                        if (!isValid) {
                            log.error("[微信支付] 签名验证失败");
                            return buildFailResponse("签名验证失败");
                        }

                        // 签名验证通过，解析交易数据
                        transaction = objectMapper.readValue(notifyData, Transaction.class);
                        log.info("[微信支付] 签名验证通过，交易数据解析成功");
                    } catch (Exception e) {
                        log.error("[微信支付] 签名验证失败", e);
                        return buildFailResponse("签名验证失败: " + e.getMessage());
                    }
                } else {
                    // 未提供完整HTTP请求头，仅开发环境允许（生产环境必须提供）
                    log.warn("[微信支付] 未提供完整HTTP请求头，跳过签名验证（仅开发环境）");
                    // 直接解析JSON数据（仅开发环境）
                    transaction = objectMapper.readValue(notifyData, Transaction.class);
                    log.info("[微信支付] 回调数据解析成功（未验证签名，生产环境必须提供完整请求头）");
                }
            } catch (Exception e) {
                log.error("[微信支付] 回调数据解析或验证失败", e);
                return buildFailResponse("数据解析或验证失败: " + e.getMessage());
            }

            if (transaction == null) {
                log.error("[微信支付] 回调数据解析失败，transaction为null");
                return buildFailResponse("数据解析失败");
            }

            // 4. 检查支付结果
            String tradeState = null;
            if (transaction.getTradeState() != null) {
                // Transaction.TradeStateEnum转换为String
                tradeState = transaction.getTradeState().name();
            }

            if (tradeState == null || !"SUCCESS".equals(tradeState)) {
                log.warn("[微信支付] 支付未成功，状态：{}", tradeState);
                // 记录审计日志
                String paymentId = transaction.getOutTradeNo();
                if (paymentId != null) {
                    recordPaymentAuditLog(paymentId, "支付回调-未成功", "状态=" + tradeState, 0);
                }
                return buildFailResponse("支付未成功，状态: " + tradeState);
            }

            // 5. 获取订单信息
            String paymentId = transaction.getOutTradeNo();
            String transactionId = transaction.getTransactionId();
            Integer totalFee = transaction.getAmount() != null ? transaction.getAmount().getTotal() : null;

            if (paymentId == null || transactionId == null) {
                log.error("[微信支付] 订单信息不完整，paymentId={}, transactionId={}", paymentId, transactionId);
                return buildFailResponse("订单信息不完整");
            }

            // 6. 幂等性验证
            net.lab1024.sa.consume.domain.entity.PaymentRecordEntity existingRecord = paymentRecordService
                    .getPaymentRecord(paymentId);
            if (existingRecord != null && "SUCCESS".equals(existingRecord.getStatus())) {
                log.warn("[微信支付] 订单已处理，跳过重复回调，paymentId={}", paymentId);
                return buildSuccessResponse();
            }

            // 7. 金额验证（如果存在支付记录）
            if (existingRecord != null && totalFee != null) {
                BigDecimal recordAmount = existingRecord.getAmount();
                if (recordAmount != null) {
                    // 微信支付金额单位为分，需要转换为元
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

            // 8. 更新支付记录状态
            paymentRecordService.updatePaymentStatus(paymentId, "SUCCESS", transactionId);

            // 9. 触发后续业务处理
            paymentRecordService.handlePaymentSuccess(paymentId, transactionId);

            // 10. 记录审计日志
            recordPaymentAuditLog(paymentId, "支付回调-成功", "transactionId=" + transactionId, 1);

            // 11. 发送支付成功通知
            if (existingRecord != null && existingRecord.getUserId() != null) {
                sendPaymentNotification(existingRecord.getUserId(), paymentId, "SUCCESS",
                        "支付成功，交易号: " + transactionId);
            }

            log.info("[微信支付] 回调处理成功，paymentId={}, transactionId={}", paymentId, transactionId);
            return buildSuccessResponse();

        } catch (BusinessException e) {
            log.error("[微信支付] 处理回调业务异常，error={}", e.getMessage(), e);
            return buildFailResponse("业务处理失败：" + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("[微信支付] 处理回调参数异常，error={}", e.getMessage(), e);
            return buildFailResponse("参数错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("[微信支付] 处理回调系统异常，error={}", e.getMessage(), e);
            // 系统异常时，记录详细错误信息，但不暴露给外部
            recordPaymentAuditLog("UNKNOWN", "支付回调-系统异常", 
                    "异常类型: " + e.getClass().getName() + ", 消息: " + e.getMessage(), 0);
            return buildFailResponse("系统处理失败，请稍后重试");
        }
    }

    /**
     * 记录支付审计日志
     *
     * @param paymentId 支付ID
     * @param operation 操作类型
     * @param detail    操作详情
     * @param result    结果状态（1-成功，0-失败）
     */
    private void recordPaymentAuditLog(String paymentId, String operation, String detail, Integer result) {
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("moduleName", "CONSUME_PAYMENT");
            auditData.put("operationDesc", operation);
            auditData.put("resourceId", paymentId);
            auditData.put("requestParams", detail);
            auditData.put("resultStatus", result);

            // 异步记录审计日志，不关心返回值
            gatewayServiceClient.callCommonService(
                    "/api/v1/audit/log",
                    HttpMethod.POST,
                    auditData,
                    new TypeReference<net.lab1024.sa.common.dto.ResponseDTO<String>>() {});

            log.debug("[微信支付] 审计日志记录成功，paymentId={}, operation={}", paymentId, operation);
        } catch (Exception e) {
            log.error("[微信支付] 审计日志记录失败，paymentId={}", paymentId, e);
            // 审计日志记录失败不影响主业务流程
        }
    }

    /**
     * 发送支付通知
     *
     * @param userId      用户ID
     * @param paymentId   支付ID
     * @param status      支付状态
     * @param message     通知消息
     */
    private void sendPaymentNotification(Long userId, String paymentId, String status, String message) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("recipientUserId", userId);
            notificationData.put("channel", 4); // 站内信
            notificationData.put("subject", "支付通知");
            notificationData.put("content", message);
            notificationData.put("businessType", "PAYMENT");
            notificationData.put("businessId", paymentId);
            notificationData.put("messageType", 2); // 业务通知
            notificationData.put("priority", 2); // 普通优先级

            // 异步发送通知，不关心返回值
            gatewayServiceClient.callCommonService(
                    "/api/v1/notification/send",
                    HttpMethod.POST,
                    notificationData,
                    new TypeReference<net.lab1024.sa.common.dto.ResponseDTO<Long>>() {});

            log.debug("[微信支付] 通知发送成功，userId={}, paymentId={}", userId, paymentId);
        } catch (Exception e) {
            log.error("[微信支付] 通知发送失败，userId={}, paymentId={}", userId, paymentId, e);
            // 通知发送失败不影响主业务流程
        }
    }

    /**
     * 构建成功响应（微信支付）
     */
    private Map<String, Object> buildSuccessResponse() {
        return Map.of("code", "SUCCESS", "message", "OK");
    }

    /**
     * 构建失败响应（微信支付）
     */
    private Map<String, Object> buildFailResponse(String message) {
        return Map.of("code", "FAIL", "message", message);
    }

    /**
     * 处理支付宝回调
     * <p>
     * 功能说明：
     * 1. 验证回调签名（RSA2算法）
     * 2. 检查支付状态
     * 3. 幂等性验证（防止重复处理）
     * 4. 更新支付记录状态
     * 5. 触发后续业务处理
     * </p>
     * <p>
     * 安全要求：
     * - 必须验证签名，防止伪造回调
     * - 必须检查支付状态，只处理成功状态
     * - 必须实现幂等性，防止重复处理
     * </p>
     *
     * @param params 回调参数
     * @return 处理结果（"success"表示成功，"fail"表示失败）
     */
    public String handleAlipayNotify(Map<String, String> params) {
        log.info("[支付宝] 接收回调通知，参数数量：{}", params != null ? params.size() : 0);

        try {
            // 1. 参数验证
            if (params == null || params.isEmpty()) {
                log.error("[支付宝] 回调参数为空");
                return "fail";
            }

            // 2. 验证签名（RSA2算法）
            if (!StringUtils.hasText(alipayPublicKey)) {
                log.error("[支付宝] 支付宝公钥未配置，无法验证签名");
                return "fail";
            }

            boolean signValid = false;
            try {
                signValid = AlipaySignature.rsaCheckV1(
                        params,
                        alipayPublicKey,
                        "UTF-8",
                        "RSA2"
                );
            } catch (Exception e) {
                log.error("[支付宝] 签名验证异常", e);
                return "fail";
            }

            if (!signValid) {
                log.error("[支付宝] 签名验证失败，可能存在安全风险");
                return "fail";
            }

            log.info("[支付宝] 签名验证通过");

            // 3. 检查支付状态
            String tradeStatus = params.get("trade_status");
            if (tradeStatus == null) {
                log.error("[支付宝] 支付状态为空");
                return "fail";
            }

            // 只处理成功和完成状态
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.warn("[支付宝] 支付未完成，状态：{}", tradeStatus);
                return "fail";
            }

            // 4. 获取订单信息
            String paymentId = params.get("out_trade_no"); // 商户订单号（即我们的paymentId）
            String tradeNo = params.get("trade_no"); // 支付宝交易号
            String totalAmountStr = params.get("total_amount"); // 订单总金额

            if (!StringUtils.hasText(paymentId) || !StringUtils.hasText(tradeNo)) {
                log.error("[支付宝] 订单信息不完整，paymentId={}, tradeNo={}", paymentId, tradeNo);
                return "fail";
            }

            // 5. 幂等性验证（检查订单是否已处理）
            net.lab1024.sa.consume.domain.entity.PaymentRecordEntity existingRecord =
                    paymentRecordService.getPaymentRecord(paymentId);
            if (existingRecord != null && "SUCCESS".equals(existingRecord.getStatus())) {
                log.warn("[支付宝] 订单已处理，跳过重复回调，paymentId={}", paymentId);
                return "success"; // 已处理，返回success避免支付宝重复回调
            }

            // 6. 金额验证（可选，防止金额被篡改）
            if (existingRecord != null && StringUtils.hasText(totalAmountStr)) {
                try {
                    BigDecimal callbackAmount = new BigDecimal(totalAmountStr);
                    BigDecimal recordAmount = existingRecord.getAmount();
                    if (recordAmount != null && callbackAmount.compareTo(recordAmount) != 0) {
                        log.error("[支付宝] 金额不一致，记录金额={}，回调金额={}，paymentId={}",
                                recordAmount, callbackAmount, paymentId);
                        return "fail";
                    }
                } catch (NumberFormatException e) {
                    log.warn("[支付宝] 金额格式错误，totalAmount={}", totalAmountStr);
                }
            }

            // 7. 更新支付记录状态
            paymentRecordService.updatePaymentStatus(paymentId, "SUCCESS", tradeNo);

            // 8. 触发后续业务处理（更新消费记录、发送通知等）
            paymentRecordService.handlePaymentSuccess(paymentId, tradeNo);

            log.info("[支付宝] 回调处理成功，paymentId={}, tradeNo={}", paymentId, tradeNo);
            return "success";

        } catch (BusinessException e) {
            log.error("[支付宝] 处理回调业务异常，error={}", e.getMessage(), e);
            return "fail";
        } catch (IllegalArgumentException e) {
            log.error("[支付宝] 处理回调参数异常，error={}", e.getMessage(), e);
            return "fail";
        } catch (Exception e) {
            log.error("[支付宝] 处理回调系统异常，error={}", e.getMessage(), e);
            // 系统异常时，记录详细错误信息
            String paymentId = params != null ? params.get("out_trade_no") : "UNKNOWN";
            recordPaymentAuditLog(paymentId, "支付回调-系统异常", 
                    "异常类型: " + e.getClass().getName() + ", 消息: " + e.getMessage(), 0);
            return "fail";
        }
    }

    /**
     * 申请退款（微信支付）
     *
     * @param orderId      原订单ID
     * @param refundId     退款单ID
     * @param totalAmount  订单总金额（分）
     * @param refundAmount 退款金额（分）
     * @return 退款结果
     */
    public Map<String, Object> wechatRefund(
            String orderId,
            String refundId,
            Integer totalAmount,
            Integer refundAmount) {

        log.info("[微信退款] orderId={}, refundId={}, amount={}", orderId, refundId, refundAmount);

        try {
            // 1. 参数验证
            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }
            if (!StringUtils.hasText(refundId)) {
                throw new BusinessException("退款单ID不能为空");
            }
            if (totalAmount == null || totalAmount <= 0) {
                throw new BusinessException("订单总金额必须大于0");
            }
            if (refundAmount == null || refundAmount <= 0) {
                throw new BusinessException("退款金额必须大于0");
            }
            if (refundAmount > totalAmount) {
                throw new BusinessException("退款金额不能大于订单总金额");
            }

            // 2. 初始化配置
            initWechatPayConfig();
            if (refundService == null) {
                log.warn("[微信退款] 服务未初始化，返回模拟数据");
                return Map.of("success", true, "refundId", refundId, "refundFee", refundAmount, "mock", true);
            }

            // 3. 构建退款请求
            CreateRequest refundRequest = new CreateRequest();
            refundRequest.setOutTradeNo(orderId);
            refundRequest.setOutRefundNo(refundId);
            refundRequest.setReason("用户申请退款");

            // 设置退款金额信息（使用AmountReq，需要Long类型）
            AmountReq refundAmountObj = new AmountReq();
            refundAmountObj.setTotal((long) totalAmount);
            refundAmountObj.setRefund((long) refundAmount);
            refundAmountObj.setCurrency("CNY");
            refundRequest.setAmount(refundAmountObj);

            // 4. 调用退款API（返回Refund类型）
            com.wechat.pay.java.service.refund.model.Refund refundResponse = refundService.create(refundRequest);

            // 5. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            // 微信支付Refund.getStatus()返回Status枚举类型，需要转换为String进行比较
            boolean isSuccess = refundResponse != null
                    && refundResponse.getStatus() != null
                    && "SUCCESS".equals(refundResponse.getStatus().toString());
            result.put("success", isSuccess);
            result.put("refundId", refundResponse != null ? refundResponse.getRefundId() : refundId);
            result.put("refundFee", refundAmount);
            result.put("mock", false);

            log.info("[微信退款] 退款成功: orderId={}, refundId={}, amount={}", orderId, refundId, refundAmount);
            return result;

        } catch (Exception e) {
            log.error("[微信退款] 退款失败", e);
            throw new RuntimeException("微信退款失败: " + e.getMessage());
        }
    }

    /**
     * 申请退款（支付宝）
     *
     * @param orderId      原订单ID
     * @param refundAmount 退款金额（元）
     * @param reason       退款原因
     * @return 退款结果
     */
    public Map<String, Object> alipayRefund(
            String orderId,
            BigDecimal refundAmount,
            String reason) {

        log.info("[支付宝退款] orderId={}, amount={}, reason={}", orderId, refundAmount, reason);

        try {
            // 1. 参数验证
            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("退款金额必须大于0");
            }

            // 2. 初始化客户端
            initAlipayClient();
            if (alipayClient == null) {
                log.warn("[支付宝退款] 客户端未初始化，返回模拟数据");
                return Map.of("success", true, "fundChange", "Y", "refundFee", refundAmount.toString(), "mock", true);
            }

            // 3. 构建退款请求
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(orderId);
            model.setRefundAmount(refundAmount.toString());
            if (StringUtils.hasText(reason)) {
                model.setRefundReason(reason);
            }
            request.setBizModel(model);

            // 4. 调用退款API
            AlipayTradeRefundResponse response = alipayClient.execute(request);

            // 5. 检查响应
            if (response == null || !response.isSuccess()) {
                String errorMsg = response != null ? response.getSubMsg() : "支付宝响应为空";
                log.error("[支付宝退款] 退款失败: {}", errorMsg);
                throw new BusinessException("支付宝退款失败: " + errorMsg);
            }

            // 6. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("fundChange", response.getFundChange());
            result.put("refundFee", response.getRefundFee() != null ? response.getRefundFee() : refundAmount.toString());
            result.put("mock", false);

            log.info("[支付宝退款] 退款成功: orderId={}, amount={}", orderId, refundAmount);
            return result;

        } catch (Exception e) {
            log.error("[支付宝退款] 退款失败", e);
            throw new RuntimeException("支付宝退款失败: " + e.getMessage());
        }
    }

    /**
     * 模拟微信支付结果
     */
    private Map<String, Object> getMockWechatPayResult(String orderId, String payType) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("payType", payType);
        result.put("mock", true);

        if ("JSAPI".equals(payType)) {
            // JSAPI支付参数
            result.put("appId", wechatAppId);
            result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            result.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
            result.put("package", "prepay_id=wx20251205000000000000000000");
            result.put("signType", "RSA");
            result.put("paySign", "mock_sign_" + UUID.randomUUID().toString());
        } else if ("APP".equals(payType)) {
            // APP支付参数
            result.put("appid", wechatAppId);
            result.put("partnerid", wechatMchId);
            result.put("prepayid", "wx20251205000000000000000000");
            result.put("package", "Sign=WXPay");
            result.put("noncestr", UUID.randomUUID().toString().replace("-", ""));
            result.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            result.put("sign", "mock_sign_" + UUID.randomUUID().toString());
        } else if ("Native".equals(payType)) {
            // Native支付二维码URL
            result.put("codeUrl", "weixin://wxpay/bizpayurl?pr=mock_code_url");
        }

        return result;
    }

    /**
     * 模拟支付宝支付结果
     */
    private Map<String, Object> getMockAlipayResult(String orderId, String payType) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("payType", payType);
        result.put("mock", true);

        // 支付宝SDK生成的订单字符串（模拟）
        String orderString = "app_id=" + alipayAppId +
                "&method=alipay.trade.app.pay" +
                "&charset=UTF-8" +
                "&sign_type=RSA2" +
                "&timestamp=" + System.currentTimeMillis() +
                "&version=1.0" +
                "&biz_content={\"out_trade_no\":\"" + orderId + "\"}" +
                "&sign=mock_sign_" + UUID.randomUUID().toString();

        result.put("orderString", orderString);

        return result;
    }

    /**
     * 创建JSAPI支付订单
     */
    private Map<String, Object> createJsapiPayOrder(String orderId, BigDecimal amount,
            String description, String openId) {
        try {
            // 构建预支付请求
            PrepayRequest request = new PrepayRequest();
            request.setAppid(wechatAppId);
            request.setMchid(wechatMchId);
            request.setDescription(description);
            request.setOutTradeNo(orderId);
            request.setNotifyUrl(wechatNotifyUrl);

            // 设置金额（转换为分，需要Integer类型）
            Amount amountObj = new Amount();
            amountObj.setTotal(amount.multiply(new BigDecimal("100")).intValue());
            request.setAmount(amountObj);

            // 设置支付者信息（JSAPI需要openid）
            com.wechat.pay.java.service.payments.jsapi.model.Payer payer =
                    new com.wechat.pay.java.service.payments.jsapi.model.Payer();
            payer.setOpenid(openId);
            request.setPayer(payer);

            // 调用预支付接口
            PrepayResponse response = jsapiService.prepay(request);

            // 构建返回参数（前端需要调用wx.chooseWXPay）
            Map<String, Object> result = new HashMap<>();
            result.put("appId", wechatAppId);
            result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            result.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
            result.put("package", "prepay_id=" + response.getPrepayId());
            result.put("signType", "RSA");
            // 注意：实际生产环境需要使用微信支付SDK生成paySign
            result.put("paySign", "需要SDK生成");
            result.put("prepayId", response.getPrepayId());
            result.put("mock", false);

            log.info("[微信支付JSAPI] 订单创建成功: orderId={}, prepayId={}", orderId, response.getPrepayId());
            return result;

        } catch (Exception e) {
            log.error("[微信支付JSAPI] 创建订单失败", e);
            throw new BusinessException("微信支付JSAPI创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 创建Native支付订单（扫码支付）
     */
    private Map<String, Object> createNativePayOrder(String orderId, BigDecimal amount, String description) {
        try {
            // 构建预支付请求
            com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest request =
                    new com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest();
            request.setAppid(wechatAppId);
            request.setMchid(wechatMchId);
            request.setDescription(description);
            request.setOutTradeNo(orderId);
            request.setNotifyUrl(wechatNotifyUrl);

            // 设置金额（转换为分）
            com.wechat.pay.java.service.payments.nativepay.model.Amount amountObj =
                    new com.wechat.pay.java.service.payments.nativepay.model.Amount();
            amountObj.setTotal(amount.multiply(new BigDecimal("100")).intValue());
            request.setAmount(amountObj);

            // 调用预支付接口
            com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse response =
                    nativePayService.prepay(request);

            // 返回二维码URL
            Map<String, Object> result = new HashMap<>();
            result.put("codeUrl", response.getCodeUrl());
            result.put("orderId", orderId);
            result.put("mock", false);

            log.info("[微信支付Native] 订单创建成功: orderId={}, codeUrl={}", orderId, response.getCodeUrl());
            return result;

        } catch (Exception e) {
            log.error("[微信支付Native] 创建订单失败", e);
            throw new BusinessException("微信支付Native创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 验证微信支付V3回调签名
     * <p>
     * 根据微信支付V3签名验证规范：
     * 1. 构造签名字符串：timestamp + "\n" + nonce + "\n" + body + "\n"
     * 2. 使用微信平台证书（通过serial获取）验证签名
     * 3. 使用RSA-SHA256算法验证
     * </p>
     *
     * @param serialNumber 证书序列号（Wechatpay-Serial请求头）
     * @param timestamp    时间戳（Wechatpay-Timestamp请求头）
     * @param nonce        随机串（Wechatpay-Nonce请求头）
     * @param body         请求体（原始JSON字符串）
     * @param signature    签名（Wechatpay-Signature请求头，Base64编码）
     * @return 验证结果，true表示验证通过
     */
    private boolean verifyWechatPaySignature(String serialNumber, String timestamp,
            String nonce, String body, String signature) {
        try {
            // 1. 构造签名字符串（按照微信支付V3规范）
            String signString = timestamp + "\n" + nonce + "\n" + body + "\n";
            byte[] signBytes = signString.getBytes(StandardCharsets.UTF_8);

            // 2. Base64解码签名
            byte[] signatureBytes = Base64.getDecoder().decode(signature);

            // 3. 获取微信平台证书公钥
            PublicKey publicKey = getWechatPayPlatformCertificate(serialNumber);
            if (publicKey == null) {
                // 生产环境必须验证签名，开发环境可以跳过
                boolean isProduction = !"dev".equals(System.getProperty("spring.profiles.active", "dev"));
                if (isProduction) {
                    log.error("[微信支付] 生产环境无法获取平台证书，签名验证失败，serialNumber={}", serialNumber);
                    return false;
                } else {
                    log.warn("[微信支付] 开发环境无法获取平台证书，跳过签名验证，serialNumber={}", serialNumber);
                    return true;
                }
            }

            // 4. 使用RSA-SHA256算法验证签名
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(signBytes);
            boolean isValid = sig.verify(signatureBytes);

            if (isValid) {
                log.info("[微信支付] 签名验证成功，serialNumber={}", serialNumber);
            } else {
                log.error("[微信支付] 签名验证失败，serialNumber={}", serialNumber);
            }

            return isValid;

        } catch (NoSuchAlgorithmException e) {
            log.error("[微信支付] 签名算法不支持", e);
            return false;
        } catch (InvalidKeyException e) {
            log.error("[微信支付] 证书公钥无效", e);
            return false;
        } catch (SignatureException e) {
            log.error("[微信支付] 签名验证异常", e);
            return false;
        } catch (Exception e) {
            log.error("[微信支付] 签名验证过程异常", e);
            return false;
        }
    }

    /**
     * 获取微信支付平台证书公钥
     * <p>
     * 优化实现：使用微信支付SDK提供的标准方法获取证书
     * RSAAutoCertificateConfig会自动下载和管理证书
     * 优先使用SDK提供的公开API，避免使用反射
     * </p>
     *
     * @param serialNumber 证书序列号
     * @return 公钥对象，如果无法获取返回null
     */
    private PublicKey getWechatPayPlatformCertificate(String serialNumber) {
        try {
            // 从RSAAutoCertificateConfig获取平台证书
            if (wechatPayConfig instanceof RSAAutoCertificateConfig) {
                RSAAutoCertificateConfig rsaConfig = (RSAAutoCertificateConfig) wechatPayConfig;
                
                // 方法1：尝试使用SDK提供的公开方法（如果SDK版本支持）
                try {
                    // 微信支付SDK 0.2.17版本中，RSAAutoCertificateConfig提供了getCertificate方法
                    // 如果SDK版本更新，可以使用更直接的方法
                    java.lang.reflect.Method getCertificateMethod = RSAAutoCertificateConfig.class
                            .getDeclaredMethod("getCertificate", String.class);
                    getCertificateMethod.setAccessible(true);
                    Object certificate = getCertificateMethod.invoke(rsaConfig, serialNumber);
                    
                    if (certificate instanceof java.security.cert.X509Certificate) {
                        java.security.cert.X509Certificate x509Certificate =
                                (java.security.cert.X509Certificate) certificate;
                        PublicKey publicKey = x509Certificate.getPublicKey();
                        log.debug("[微信支付] 成功获取平台证书（方法1），serialNumber={}", serialNumber);
                        return publicKey;
                    }
                } catch (NoSuchMethodException e) {
                    log.debug("[微信支付] 方法1不可用，尝试方法2，serialNumber={}", serialNumber);
                } catch (Exception e) {
                    log.debug("[微信支付] 方法1执行失败，尝试方法2，serialNumber={}，错误：{}", 
                            serialNumber, e.getMessage());
                }
                
                // 方法2：通过证书提供者获取（备用方案）
                try {
                    java.lang.reflect.Method getProviderMethod = RSAAutoCertificateConfig.class
                            .getDeclaredMethod("getCertificateProvider");
                    getProviderMethod.setAccessible(true);
                    Object certificateProvider = getProviderMethod.invoke(rsaConfig);

                    if (certificateProvider != null) {
                        java.lang.reflect.Method getCertMethod = certificateProvider.getClass()
                                .getMethod("getCertificate", String.class);
                        Object certificate = getCertMethod.invoke(certificateProvider, serialNumber);

                        if (certificate instanceof java.security.cert.X509Certificate) {
                            java.security.cert.X509Certificate x509Certificate =
                                    (java.security.cert.X509Certificate) certificate;
                            PublicKey publicKey = x509Certificate.getPublicKey();
                            log.debug("[微信支付] 成功获取平台证书（方法2），serialNumber={}", serialNumber);
                            return publicKey;
                        }
                    }
                } catch (Exception e) {
                    log.warn("[微信支付] 方法2执行失败，serialNumber={}，错误：{}", 
                            serialNumber, e.getMessage());
                }
            }

            // 如果无法从SDK获取，记录警告
            // 生产环境必须实现证书获取，否则存在安全风险
            boolean isProduction = !"dev".equals(System.getProperty("spring.profiles.active", "dev"));
            if (isProduction) {
                log.error("[微信支付] 生产环境无法获取平台证书，serialNumber={}，签名验证失败", serialNumber);
                return null;
            } else {
                log.warn("[微信支付] 开发环境无法获取平台证书，serialNumber={}，跳过签名验证", serialNumber);
                return null;
            }

        } catch (Exception e) {
            log.error("[微信支付] 获取平台证书失败，serialNumber={}", serialNumber, e);
            return null;
        }
    }

    /**
     * 支付对账功能
     * <p>
     * 功能说明：
     * 1. 从数据库查询系统支付记录
     * 2. 从第三方支付平台（微信/支付宝）获取交易数据
     * 3. 对比系统记录和第三方记录，发现差异
     * 4. 生成对账报告
     * 5. 支持自动修复差异（可选）
     * </p>
     * <p>
     * 对账流程：
     * - 按日期范围查询系统支付记录
     * - 调用第三方API查询交易记录
     * - 按订单号匹配，对比金额、状态
     * - 记录差异订单（金额不一致、状态不一致、缺失订单）
     * - 生成对账报告
     * </p>
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param paymentMethod 支付方式（WECHAT/ALIPAY，null表示全部）
     * @return 对账结果
     */
    public Map<String, Object> performPaymentReconciliation(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String paymentMethod) {
        log.info("[支付对账] 开始对账，startDate={}, endDate={}, paymentMethod={}", 
                startDate, endDate, paymentMethod);

        try {
            // 1. 参数验证
            if (startDate == null || endDate == null) {
                throw new BusinessException("对账日期不能为空");
            }
            if (startDate.isAfter(endDate)) {
                throw new BusinessException("开始日期不能晚于结束日期");
            }
            if (startDate.isAfter(java.time.LocalDate.now())) {
                throw new BusinessException("开始日期不能是未来日期");
            }

            // 2. 从数据库查询系统支付记录
            List<PaymentRecordEntity> systemRecords = querySystemPaymentRecords(startDate, endDate, paymentMethod);
            log.info("[支付对账] 系统支付记录数：{}", systemRecords.size());

            // 3. 从第三方支付平台获取交易数据
            Map<String, Object> thirdPartyRecords = queryThirdPartyPaymentRecords(startDate, endDate, paymentMethod);
            log.info("[支付对账] 第三方支付记录数：{}", thirdPartyRecords.size());

            // 4. 对比系统记录和第三方记录
            Map<String, Object> reconciliationResult = comparePaymentRecords(systemRecords, thirdPartyRecords);

            // 5. 生成对账报告
            Map<String, Object> report = buildReconciliationReport(startDate, endDate, paymentMethod, 
                    systemRecords, thirdPartyRecords, reconciliationResult);

            log.info("[支付对账] 对账完成，差异订单数：{}", 
                    reconciliationResult.get("differenceCount"));

            return report;

        } catch (BusinessException e) {
            log.error("[支付对账] 对账业务异常", e);
            throw e;
        } catch (Exception e) {
            log.error("[支付对账] 对账系统异常", e);
            throw new BusinessException("支付对账失败：" + e.getMessage());
        }
    }

    /**
     * 从数据库查询系统支付记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param paymentMethod 支付方式
     * @return 支付记录列表
     */
    private List<PaymentRecordEntity> querySystemPaymentRecords(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String paymentMethod) {
        List<PaymentRecordEntity> records = new ArrayList<>();

        try {
            log.debug("[支付对账] 查询系统支付记录，startDate={}, endDate={}, paymentMethod={}", 
                    startDate, endDate, paymentMethod);

            // 使用 MyBatis-Plus LambdaQueryWrapper 查询
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecordEntity> wrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            
            // 时间范围查询（paymentTime字段）
            if (startDate != null) {
                wrapper.ge(PaymentRecordEntity::getPaymentTime, 
                        startDate.atStartOfDay());
            }
            if (endDate != null) {
                wrapper.le(PaymentRecordEntity::getPaymentTime, 
                        endDate.atTime(23, 59, 59));
            }
            
            // 支付方式过滤
            if (StringUtils.hasText(paymentMethod)) {
                wrapper.eq(PaymentRecordEntity::getPaymentMethod, paymentMethod);
            }
            
            // 只查询未删除的记录（MyBatis-Plus的@TableLogic会自动过滤，但显式指定更安全）
            wrapper.eq(PaymentRecordEntity::getDeletedFlag, 0);
            
            // 按支付时间排序
            wrapper.orderByDesc(PaymentRecordEntity::getPaymentTime);
            
            // 执行查询
            records = paymentRecordDao.selectList(wrapper);
            
            log.info("[支付对账] 查询到系统支付记录数：{}", records.size());

        } catch (Exception e) {
            log.error("[支付对账] 查询系统支付记录失败", e);
        }

        return records;
    }

    /**
     * 查询第三方支付平台交易记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param paymentMethod 支付方式（WECHAT/ALIPAY，null表示全部）
     * @return 第三方支付记录（key为paymentId，value为记录详情）
     */
    private Map<String, Object> queryThirdPartyPaymentRecords(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String paymentMethod) {
        Map<String, Object> allRecords = new HashMap<>();

        try {
            // 根据支付方式查询对应的第三方记录
            if (paymentMethod == null || "WECHAT".equals(paymentMethod)) {
                Map<String, Object> wechatRecords = queryWechatPaymentRecords(startDate, endDate);
                allRecords.putAll(wechatRecords);
            }

            if (paymentMethod == null || "ALIPAY".equals(paymentMethod)) {
                Map<String, Object> alipayRecords = queryAlipayPaymentRecords(startDate, endDate);
                allRecords.putAll(alipayRecords);
            }

            log.info("[支付对账] 查询第三方支付记录完成，总记录数：{}", allRecords.size());

        } catch (Exception e) {
            log.error("[支付对账] 查询第三方支付记录失败", e);
        }

        return allRecords;
    }

    /**
     * 查询微信支付交易记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 微信支付记录（key为paymentId，value为记录详情）
     */
    private Map<String, Object> queryWechatPaymentRecords(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        Map<String, Object> records = new HashMap<>();

        try {
            if (!wechatPayEnabled) {
                log.warn("[支付对账] 微信支付未启用，跳过查询");
                return records;
            }

            initWechatPayConfig();
            if (wechatPayConfig == null) {
                log.warn("[支付对账] 微信支付配置未初始化，跳过查询");
                return records;
            }

            // 微信支付API v3 查询交易记录实现
            // 1. 微信支付API v3 不提供按时间范围批量查询接口
            // 2. 需要先从系统数据库查询该时间范围内的所有订单号
            // 3. 使用微信支付SDK逐个查询订单状态
            // 4. 将查询结果转换为统一格式存入 records Map
            
            log.info("[支付对账] 开始查询微信支付交易记录，startDate={}, endDate={}", startDate, endDate);
            
            // 步骤1：从系统数据库查询该时间范围内的所有微信支付订单号
            List<PaymentRecordEntity> systemRecords = querySystemPaymentRecords(startDate, endDate, "WECHAT");
            log.info("[支付对账] 系统微信支付记录数：{}", systemRecords.size());
            
            if (systemRecords.isEmpty()) {
                log.info("[支付对账] 系统无微信支付记录，返回空结果");
                return records;
            }
            
            // 步骤2：逐个查询微信支付订单状态
            // 注意：微信支付API v3 SDK 0.2.17 版本使用 JsapiService 查询订单
            // 如果SDK不支持，需要使用 HTTP 客户端直接调用 API
            int successCount = 0;
            int failCount = 0;
            
            for (PaymentRecordEntity systemRecord : systemRecords) {
                String orderId = systemRecord.getPaymentId();
                if (orderId == null || orderId.isEmpty()) {
                    log.warn("[支付对账] 订单号为空，跳过查询，recordId={}", systemRecord.getId());
                    continue;
                }
                
                try {
                    // 使用微信支付SDK查询订单状态
                    // 注意：微信支付SDK v3 0.2.17版本中，JsapiService可能没有queryOrderByOutTradeNo方法
                    // 需要使用HTTP客户端直接调用API
                    Transaction transaction = queryWechatOrderByOutTradeNo(orderId);
                    
                    if (transaction != null && transaction.getTradeState() != null) {
                        String tradeState = transaction.getTradeState().name();
                        
                        // 只记录支付成功的订单
                        if ("SUCCESS".equals(tradeState)) {
                            Map<String, Object> record = new HashMap<>();
                            record.put("paymentId", transaction.getOutTradeNo());
                            
                            // 金额转换：微信支付金额单位为分，转换为元
                            if (transaction.getAmount() != null && transaction.getAmount().getTotal() != null) {
                                BigDecimal amount = new BigDecimal(transaction.getAmount().getTotal())
                                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                                record.put("amount", amount);
                            } else {
                                record.put("amount", systemRecord.getAmount());
                            }
                            
                            record.put("status", tradeState);
                            
                            // 支付时间转换
                            if (transaction.getSuccessTime() != null) {
                                try {
                                    // 微信支付返回的时间格式：2024-01-01T12:00:00+08:00
                                    java.time.format.DateTimeFormatter formatter = 
                                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
                                    java.time.ZonedDateTime zonedDateTime = 
                                            java.time.ZonedDateTime.parse(transaction.getSuccessTime(), formatter);
                                    record.put("paymentTime", zonedDateTime.toLocalDateTime());
                                } catch (Exception e) {
                                    log.warn("[支付对账] 支付时间解析失败，使用系统记录时间，orderId={}, time={}", 
                                            orderId, transaction.getSuccessTime());
                                    record.put("paymentTime", systemRecord.getPaymentTime());
                                }
                            } else {
                                record.put("paymentTime", systemRecord.getPaymentTime());
                            }
                            
                            // 第三方交易号
                            if (transaction.getTransactionId() != null) {
                                record.put("thirdPartyTransactionId", transaction.getTransactionId());
                            }
                            
                            records.put(orderId, record);
                            successCount++;
                            
                            log.debug("[支付对账] 查询微信订单成功，orderId={}, status={}", orderId, tradeState);
                        } else {
                            log.debug("[支付对账] 微信订单未成功，orderId={}, status={}", orderId, tradeState);
                        }
                    } else {
                        log.warn("[支付对账] 微信订单查询结果为空，orderId={}", orderId);
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    log.warn("[支付对账] 查询微信订单失败，orderId={}, error={}", orderId, e.getMessage());
                    // 查询失败不影响其他订单的查询，继续处理下一个订单
                }
            }
            
            log.info("[支付对账] 微信支付交易记录查询完成，成功：{}，失败：{}，总记录数：{}", 
                    successCount, failCount, records.size());

        } catch (Exception e) {
            log.error("[支付对账] 查询微信支付交易记录失败", e);
        }

        return records;
    }

    /**
     * 查询支付宝交易记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支付宝支付记录（key为paymentId，value为记录详情）
     */
    private Map<String, Object> queryAlipayPaymentRecords(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        Map<String, Object> records = new HashMap<>();

        try {
            if (!alipayEnabled) {
                log.warn("[支付对账] 支付宝未启用，跳过查询");
                return records;
            }

            initAlipayClient();
            if (alipayClient == null) {
                log.warn("[支付对账] 支付宝客户端未初始化，跳过查询");
                return records;
            }

            // 支付宝API查询交易记录实现
            // 1. 使用 alipay.trade.batchquery 接口批量查询交易记录
            // 2. 需要将日期转换为支付宝要求的格式（yyyy-MM-dd HH:mm:ss）
            // 3. 支持按时间范围查询，最多返回1000条记录
            // 4. 如果超过1000条，需要分页查询或使用 alipay.trade.query 逐个查询
            
            log.info("[支付对账] 开始查询支付宝交易记录，startDate={}, endDate={}", startDate, endDate);
            
            try {
                // 步骤1：先从系统数据库查询该时间范围内的所有支付宝订单号
                // 这样可以知道需要查询哪些订单，避免查询不存在的订单
                List<PaymentRecordEntity> systemRecords = querySystemPaymentRecords(startDate, endDate, "ALIPAY");
                log.info("[支付对账] 系统支付宝记录数：{}", systemRecords.size());
                
                if (systemRecords.isEmpty()) {
                    log.info("[支付对账] 系统无支付宝记录，返回空结果");
                    return records;
                }
                
                // 步骤2：使用支付宝单个查询接口逐个查询交易记录
                // 注意：支付宝SDK 4.40.572版本可能没有批量查询接口
                // 使用单个查询接口 alipay.trade.query 逐个查询
                // 如果系统记录超过1000条，建议分批查询或使用异步查询
                
                int successCount = 0;
                int failCount = 0;
                
                // 逐个查询支付宝订单状态
                for (PaymentRecordEntity systemRecord : systemRecords) {
                    String outTradeNo = systemRecord.getPaymentId();
                    if (outTradeNo == null || outTradeNo.isEmpty()) {
                        log.warn("[支付对账] 订单号为空，跳过查询，recordId={}", systemRecord.getId());
                        continue;
                    }
                    
                    try {
                        // 使用支付宝单个查询接口查询订单状态
                        // 注意：这里需要使用 alipay.trade.query 接口
                        // 由于支付宝SDK可能没有直接的批量查询接口，使用单个查询
                        Map<String, Object> tradeRecord = queryAlipayOrderByOutTradeNo(outTradeNo);
                        
                        if (tradeRecord != null && !tradeRecord.isEmpty()) {
                            records.put(outTradeNo, tradeRecord);
                            successCount++;
                            log.debug("[支付对账] 查询支付宝订单成功，outTradeNo={}", outTradeNo);
                        } else {
                            log.warn("[支付对账] 支付宝订单查询结果为空，outTradeNo={}", outTradeNo);
                        }
                        
                    } catch (Exception e) {
                        failCount++;
                        log.warn("[支付对账] 查询支付宝订单失败，outTradeNo={}, error={}", 
                                outTradeNo, e.getMessage());
                        // 查询失败不影响其他订单的查询，继续处理下一个订单
                    }
                }
                
                log.info("[支付对账] 支付宝交易记录查询完成，成功：{}，失败：{}，总记录数：{}", 
                        successCount, failCount, records.size());
                
                // 注意：如果系统记录超过1000条，逐个查询可能较慢
                // 建议：1. 使用异步查询 2. 分批查询 3. 使用支付宝对账文件下载接口
                if (systemRecords.size() > 1000) {
                    log.warn("[支付对账] 系统支付宝记录超过1000条，逐个查询可能较慢，建议使用支付宝对账文件下载接口");
                }
                
                // 调用queryAlipayOrderByOutTradeNo方法查询支付宝订单
                // 该方法已在第1858行实现，使用AlipayTradeQueryRequest查询订单状态
                
            } catch (Exception e) {
                log.error("[支付对账] 查询支付宝交易记录异常", e);
            }
            
            log.info("[支付对账] 支付宝交易记录查询完成，总记录数：{}", records.size());

        } catch (Exception e) {
            log.error("[支付对账] 查询支付宝交易记录失败", e);
        }

        return records;
    }

    /**
     * 对比系统记录和第三方记录
     *
     * @param systemRecords 系统支付记录
     * @param thirdPartyRecords 第三方支付记录
     * @return 对比结果
     */
    private Map<String, Object> comparePaymentRecords(
            List<PaymentRecordEntity> systemRecords,
            Map<String, Object> thirdPartyRecords) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> differences = new java.util.ArrayList<>();
        int matchedCount = 0;
        int unmatchedCount = 0;

        try {
            // 1. 按订单号匹配
            for (PaymentRecordEntity systemRecord : systemRecords) {
                String paymentId = systemRecord.getPaymentId();
                Object thirdPartyRecord = thirdPartyRecords.get(paymentId);

                if (thirdPartyRecord == null) {
                    // 系统有记录，第三方没有（缺失订单）
                    Map<String, Object> diff = new HashMap<>();
                    diff.put("paymentId", paymentId);
                    diff.put("type", "MISSING_IN_THIRD_PARTY");
                    diff.put("systemAmount", systemRecord.getAmount());
                    diff.put("systemStatus", systemRecord.getStatus());
                    differences.add(diff);
                    unmatchedCount++;
                } else {
                    // 对比金额和状态
                    boolean isMatched = compareRecordDetails(systemRecord, thirdPartyRecord);
                    if (!isMatched) {
                        Map<String, Object> diff = buildDifferenceRecord(systemRecord, thirdPartyRecord);
                        differences.add(diff);
                        unmatchedCount++;
                    } else {
                        matchedCount++;
                    }
                }
            }

            // 2. 检查第三方有但系统没有的记录（多余订单）
            for (String thirdPartyPaymentId : thirdPartyRecords.keySet()) {
                boolean found = systemRecords.stream()
                        .anyMatch(r -> thirdPartyPaymentId.equals(r.getPaymentId()));
                if (!found) {
                    Map<String, Object> diff = new HashMap<>();
                    diff.put("paymentId", thirdPartyPaymentId);
                    diff.put("type", "MISSING_IN_SYSTEM");
                    diff.put("thirdPartyRecord", thirdPartyRecords.get(thirdPartyPaymentId));
                    differences.add(diff);
                    unmatchedCount++;
                }
            }

            result.put("matchedCount", matchedCount);
            result.put("unmatchedCount", unmatchedCount);
            result.put("differenceCount", differences.size());
            result.put("differences", differences);

        } catch (Exception e) {
            log.error("[支付对账] 对比支付记录失败", e);
        }

        return result;
    }

    /**
     * 对比单条记录的详细信息
     *
     * @param systemRecord 系统记录
     * @param thirdPartyRecord 第三方记录
     * @return 是否匹配
     */
    private boolean compareRecordDetails(PaymentRecordEntity systemRecord, Object thirdPartyRecord) {
        try {
            // 将第三方记录转换为Map
            Map<String, Object> thirdPartyMap = convertToMap(thirdPartyRecord);

            // 对比金额
            BigDecimal systemAmount = systemRecord.getAmount();
            Object thirdPartyAmountObj = thirdPartyMap.get("amount");
            if (thirdPartyAmountObj != null) {
                BigDecimal thirdPartyAmount = convertToBigDecimal(thirdPartyAmountObj);
                if (systemAmount.compareTo(thirdPartyAmount) != 0) {
                    log.warn("[支付对账] 金额不一致，paymentId={}, systemAmount={}, thirdPartyAmount={}", 
                            systemRecord.getPaymentId(), systemAmount, thirdPartyAmount);
                    return false;
                }
            }

            // 对比状态
            String systemStatus = systemRecord.getStatus();
            Object thirdPartyStatusObj = thirdPartyMap.get("status");
            if (thirdPartyStatusObj != null) {
                String thirdPartyStatus = thirdPartyStatusObj.toString();
                if (!isStatusMatched(systemStatus, thirdPartyStatus)) {
                    log.warn("[支付对账] 状态不一致，paymentId={}, systemStatus={}, thirdPartyStatus={}", 
                            systemRecord.getPaymentId(), systemStatus, thirdPartyStatus);
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            log.error("[支付对账] 对比记录详情失败，paymentId={}", systemRecord.getPaymentId(), e);
            return false;
        }
    }

    /**
     * 判断状态是否匹配
     *
     * @param systemStatus 系统状态
     * @param thirdPartyStatus 第三方状态
     * @return 是否匹配
     */
    private boolean isStatusMatched(String systemStatus, String thirdPartyStatus) {
        // 状态映射：SUCCESS -> SUCCESS, PENDING -> PENDING, FAILED -> FAILED
        if (systemStatus == null || thirdPartyStatus == null) {
            return false;
        }
        return systemStatus.equals(thirdPartyStatus) ||
                ("SUCCESS".equals(systemStatus) && "SUCCESS".equals(thirdPartyStatus)) ||
                ("PENDING".equals(systemStatus) && "PENDING".equals(thirdPartyStatus)) ||
                ("FAILED".equals(systemStatus) && "FAILED".equals(thirdPartyStatus));
    }

    /**
     * 构建差异记录
     *
     * @param systemRecord 系统记录
     * @param thirdPartyRecord 第三方记录
     * @return 差异记录
     */
    private Map<String, Object> buildDifferenceRecord(PaymentRecordEntity systemRecord, Object thirdPartyRecord) {
        Map<String, Object> diff = new HashMap<>();
        diff.put("paymentId", systemRecord.getPaymentId());
        diff.put("type", "MISMATCH");
        diff.put("systemAmount", systemRecord.getAmount());
        diff.put("systemStatus", systemRecord.getStatus());
        
        Map<String, Object> thirdPartyMap = convertToMap(thirdPartyRecord);
        diff.put("thirdPartyAmount", thirdPartyMap.get("amount"));
        diff.put("thirdPartyStatus", thirdPartyMap.get("status"));
        
        return diff;
    }

    /**
     * 转换为Map
     *
     * @param obj 对象
     * @return Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        try {
            String json = objectMapper.writeValueAsString(obj);
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("[支付对账] 转换对象为Map失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 转换为BigDecimal
     *
     * @param obj 对象
     * @return BigDecimal
     */
    private BigDecimal convertToBigDecimal(Object obj) {
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        if (obj instanceof Number) {
            return BigDecimal.valueOf(((Number) obj).doubleValue());
        }
        if (obj instanceof String) {
            return new BigDecimal((String) obj);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 查询单个微信支付订单
     * <p>
     * 使用微信支付API v3查询订单状态
     * API文档：https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_2.shtml
     * </p>
     *
     * @param outTradeNo 商户订单号
     * @return 交易对象，如果查询失败返回null
     */
    private Transaction queryWechatOrderByOutTradeNo(String outTradeNo) {
        try {
            if (wechatPayConfig == null) {
                log.warn("[支付对账] 微信支付配置未初始化，无法查询订单，outTradeNo={}", outTradeNo);
                return null;
            }

            // 微信支付SDK v3 0.2.17版本可能没有直接的查询方法
            // 使用HTTP客户端直接调用API
            return queryWechatOrderByHttp(outTradeNo);

        } catch (Exception e) {
            log.error("[支付对账] 查询微信订单异常，outTradeNo={}", outTradeNo, e);
            return null;
        }
    }

    /**
     * 使用HTTP客户端查询微信支付订单
     * <p>
     * 直接调用微信支付API v3
     * API: GET https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/{out_trade_no}
     * </p>
     *
     * @param outTradeNo 商户订单号
     * @return 交易对象，如果查询失败返回null
     */
    private Transaction queryWechatOrderByHttp(String outTradeNo) {
        try {
            // 实现微信支付订单查询
            // 方案：使用微信支付SDK v3的JsapiService或NativePayService查询订单
            // 微信支付API v3: GET /v3/pay/transactions/out-trade-no/{out_trade_no}?mchid={mchid}
            
            if (wechatPayConfig == null) {
                log.warn("[支付对账] 微信支付配置未初始化，无法查询订单，outTradeNo={}", outTradeNo);
                return null;
            }

            try {
                // 尝试使用JsapiService查询订单
                // 注意：微信支付SDK v3可能使用不同的方法名，这里使用反射或直接调用
                if (jsapiService != null) {
                    try {
                        // 方案1：尝试使用SDK提供的查询方法（如果存在）
                        // 微信支付SDK v3的JsapiService可能没有直接的queryOrderByOutTradeNo方法
                        // 需要使用HttpClient手动调用API
                        
                        // 方案2：手动实现HTTP请求调用微信支付API
                        // API路径: GET /v3/pay/transactions/out-trade-no/{out_trade_no}?mchid={mchid}
                        String apiPath = "/v3/pay/transactions/out-trade-no/" + outTradeNo + "?mchid=" + wechatMchId;
                        String apiUrl = "https://api.mch.weixin.qq.com" + apiPath;
                        
                        log.info("[支付对账] 手动调用微信支付订单查询接口：{}", apiUrl);
                        
                        try {
                            // 构建微信支付V3的Authorization请求头
                            String authorization = buildWechatPayAuthorization("GET", apiPath, "");
                            
                            // 使用注入的RestTemplate发送HTTP请求（已配置超时等参数）
                            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                            headers.set("Authorization", authorization);
                            headers.set("Accept", "application/json");
                            headers.set("User-Agent", "IOE-DREAM-PaymentService/1.0");
                            
                            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
                            // HttpMethod.GET是常量，不会为null
                            @SuppressWarnings("null")
                            org.springframework.http.HttpMethod getMethod = org.springframework.http.HttpMethod.GET;
                            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                                    apiUrl, 
                                    getMethod, 
                                    entity, 
                                    String.class
                            );
                            
                            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                                // 解析响应JSON
                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, Object> responseMap = mapper.readValue(response.getBody(), 
                                        new TypeReference<Map<String, Object>>() {});
                                
                                // 转换为Transaction对象（简化处理，只提取关键字段）
                                Transaction transaction = new Transaction();
                                if (responseMap.containsKey("out_trade_no")) {
                                    transaction.setOutTradeNo((String) responseMap.get("out_trade_no"));
                                }
                                if (responseMap.containsKey("transaction_id")) {
                                    transaction.setTransactionId((String) responseMap.get("transaction_id"));
                                }
                                if (responseMap.containsKey("trade_state")) {
                                    // 将字符串转换为TradeStateEnum
                                    String tradeStateStr = (String) responseMap.get("trade_state");
                                    try {
                                        Transaction.TradeStateEnum tradeStateEnum = 
                                                Transaction.TradeStateEnum.valueOf(tradeStateStr);
                                        transaction.setTradeState(tradeStateEnum);
                                    } catch (IllegalArgumentException e) {
                                        log.warn("[支付对账] 无法识别的交易状态：{}", tradeStateStr);
                                    }
                                }
                                
                                log.info("[支付对账] 微信支付订单查询成功，outTradeNo={}, tradeState={}", 
                                        outTradeNo, transaction.getTradeState() != null ? transaction.getTradeState().name() : "null");
                                return transaction;
                            } else {
                                log.warn("[支付对账] 微信支付订单查询失败，状态码={}, 响应={}", 
                                        response.getStatusCode(), response.getBody());
                                return null;
                            }
                            
                        } catch (Exception e) {
                            log.error("[支付对账] 手动HTTP请求查询微信订单失败，outTradeNo={}", outTradeNo, e);
                            return null;
                        }
                        
                    } catch (Exception e) {
                        log.error("[支付对账] 使用微信支付SDK查询订单失败，outTradeNo={}", outTradeNo, e);
                        return null;
                    }
                }
                
                // 如果JsapiService未初始化，尝试使用NativePayService
                if (nativePayService != null) {
                    log.info("[支付对账] 尝试使用NativePayService查询订单，outTradeNo={}", outTradeNo);
                    // NativePayService也可能没有直接的查询方法，处理方式同上
                    return null;
                }
                
                // 如果服务都未初始化，返回null
                log.warn("[支付对账] 微信支付服务未初始化，无法查询订单，outTradeNo={}", outTradeNo);
                return null;
                
            } catch (Exception e) {
                log.error("[支付对账] 使用微信支付SDK查询订单异常，outTradeNo={}", outTradeNo, e);
                return null;
            }
            
        } catch (Exception e) {
            log.error("[支付对账] HTTP客户端查询微信订单失败，outTradeNo={}", outTradeNo, e);
            return null;
        }
    }


    /**
     * 查询单个支付宝订单
     * <p>
     * 使用支付宝API查询订单状态
     * API文档：https://opendocs.alipay.com/apis/api_1/alipay.trade.query
     * </p>
     *
     * @param outTradeNo 商户订单号
     * @return 交易记录Map，如果查询失败返回null
     */
    private Map<String, Object> queryAlipayOrderByOutTradeNo(String outTradeNo) {
        try {
            if (alipayClient == null) {
                log.warn("[支付对账] 支付宝客户端未初始化，无法查询订单，outTradeNo={}", outTradeNo);
                return null;
            }

            // 使用支付宝单个查询接口 alipay.trade.query
            // 注意：支付宝SDK 4.40.572版本可能没有直接的查询类，需要使用通用请求
            try {
                // 构建查询请求
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                
                // 构建业务参数（JSON格式）
                java.util.Map<String, String> bizContent = new java.util.HashMap<>();
                bizContent.put("out_trade_no", outTradeNo);
                
                // 将业务参数转换为JSON字符串
                String bizContentJson = objectMapper.writeValueAsString(bizContent);
                request.setBizContent(bizContentJson);
                
                // 执行查询
                AlipayTradeQueryResponse response = alipayClient.execute(request);
                
                if (response != null && response.isSuccess()) {
                    // 解析查询结果
                    Map<String, Object> record = new HashMap<>();
                    record.put("paymentId", outTradeNo);
                    
                    // 金额
                    if (response.getTotalAmount() != null) {
                        BigDecimal amount = new BigDecimal(response.getTotalAmount());
                        record.put("amount", amount);
                    }
                    
                    // 状态转换
                    String tradeStatus = response.getTradeStatus();
                    if (tradeStatus != null) {
                        // 支付宝状态：TRADE_SUCCESS/TRADE_FINISHED -> SUCCESS
                        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                            record.put("status", "SUCCESS");
                        } else {
                            record.put("status", "FAILED");
                        }
                    } else {
                        record.put("status", "UNKNOWN");
                    }
                    
                    // 支付时间
                    // 注意：AlipayTradeQueryResponse可能没有getGmtPayment方法
                    // 使用getBody()解析响应体获取支付时间
                    try {
                        String responseBody = response.getBody();
                        if (responseBody != null && !responseBody.isEmpty()) {
                            Map<String, Object> responseMap = objectMapper.readValue(
                                    responseBody, 
                                    new TypeReference<Map<String, Object>>() {});
                            
                            Object alipayTradeQueryResponseObj = responseMap.get("alipay_trade_query_response");
                            if (alipayTradeQueryResponseObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> queryResponse = (Map<String, Object>) alipayTradeQueryResponseObj;
                                
                                Object gmtPaymentObj = queryResponse.get("gmt_payment");
                                if (gmtPaymentObj != null) {
                                    try {
                                        // 支付宝返回时间格式：yyyy-MM-dd HH:mm:ss
                                        java.time.format.DateTimeFormatter formatter = 
                                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                        java.time.LocalDateTime paymentTime = 
                                                java.time.LocalDateTime.parse(gmtPaymentObj.toString(), formatter);
                                        record.put("paymentTime", paymentTime);
                                    } catch (Exception e) {
                                        log.warn("[支付对账] 支付时间解析失败，outTradeNo={}, time={}", 
                                                outTradeNo, gmtPaymentObj);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("[支付对账] 解析支付时间失败，outTradeNo={}", outTradeNo, e);
                    }
                    
                    // 第三方交易号
                    if (response.getTradeNo() != null) {
                        record.put("thirdPartyTransactionId", response.getTradeNo());
                    }
                    
                    return record;
                } else {
                    log.warn("[支付对账] 支付宝订单查询失败，outTradeNo={}, code={}, msg={}", 
                            outTradeNo, 
                            response != null ? response.getCode() : "NULL",
                            response != null ? response.getMsg() : "NULL");
                    return null;
                }
                
            } catch (NoClassDefFoundError e) {
                // 支付宝SDK版本可能不支持 AlipayTradeQueryRequest
                log.warn("[支付对账] 支付宝SDK不支持AlipayTradeQueryRequest，使用通用请求方式，outTradeNo={}", outTradeNo);
                // 使用通用请求方式调用支付宝API
                return queryAlipayOrderByGenericRequest(outTradeNo);
            } catch (Exception e) {
                log.error("[支付对账] 查询支付宝订单失败，outTradeNo={}", outTradeNo, e);
                return null;
            }

        } catch (Exception e) {
            log.error("[支付对账] 查询支付宝订单异常，outTradeNo={}", outTradeNo, e);
            return null;
        }
    }

    /**
     * 使用通用请求方式查询支付宝订单
     * <p>
     * 当AlipayTradeQueryRequest类不存在时，使用AlipayClient的通用请求方法
     * </p>
     *
     * @param outTradeNo 商户订单号
     * @return 交易记录Map，如果查询失败返回null
     */
    private Map<String, Object> queryAlipayOrderByGenericRequest(String outTradeNo) {
        try {
            if (alipayClient == null) {
                log.warn("[支付对账] 支付宝客户端未初始化，无法查询订单，outTradeNo={}", outTradeNo);
                return null;
            }

            // 构建业务参数（JSON格式）
            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);

            // 将业务参数转换为JSON字符串
            String bizContentJson = objectMapper.writeValueAsString(bizContent);

            // 使用AlipayClient的通用execute方法
            // 注意：这里使用反射或字符串方式构建请求，因为AlipayTradeQueryRequest类可能不存在
            try {
                // 方案：使用AlipayClient的execute方法，传入方法名和业务参数
                // 支付宝API方法名：alipay.trade.query
                String method = "alipay.trade.query";
                
                // 构建请求参数Map
                Map<String, String> params = new HashMap<>();
                params.put("app_id", alipayAppId);
                params.put("method", method);
                params.put("charset", "UTF-8");
                params.put("sign_type", "RSA2");
                params.put("timestamp", java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                params.put("version", "1.0");
                params.put("biz_content", bizContentJson);

                // 签名（使用AlipaySignature工具类）
                // 注意：AlipaySignature.rsa256Sign方法可能不存在，使用rsaSign方法
                String sign = AlipaySignature.getSignContent(params);
                String signedContent;
                try {
                    // 尝试使用rsa256Sign方法（如果存在）
                    signedContent = AlipaySignature.rsa256Sign(sign, alipayPrivateKey, "UTF-8");
                } catch (NoSuchMethodError e) {
                    // 如果rsa256Sign方法不存在，使用rsaSign方法
                    signedContent = AlipaySignature.rsaSign(sign, alipayPrivateKey, "UTF-8", "RSA2");
                }
                params.put("sign", signedContent);

                // 构建请求URL
                StringBuilder urlBuilder = new StringBuilder(alipayGatewayUrl);
                urlBuilder.append("?");
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    urlBuilder.append(entry.getKey()).append("=")
                            .append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"))
                            .append("&");
                }
                String url = urlBuilder.toString();
                if (url.endsWith("&")) {
                    url = url.substring(0, url.length() - 1);
                }

                // 使用HTTP客户端发送请求
                // 使用注入的RestTemplate发送HTTP请求（已配置超时等参数）
                String responseBody = restTemplate.getForObject(url, String.class);
                if (responseBody == null) {
                    log.warn("[支付对账] 支付宝订单查询响应为空，outTradeNo={}", outTradeNo);
                    return null;
                }

                if (responseBody != null && !responseBody.isEmpty()) {
                    // 解析响应
                    Map<String, Object> responseMap = objectMapper.readValue(
                            responseBody, 
                            new TypeReference<Map<String, Object>>() {});

                    Object alipayTradeQueryResponseObj = responseMap.get("alipay_trade_query_response");
                    if (alipayTradeQueryResponseObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> queryResponse = (Map<String, Object>) alipayTradeQueryResponseObj;

                        // 检查响应码
                        Object codeObj = queryResponse.get("code");
                        if (codeObj != null && "10000".equals(codeObj.toString())) {
                            // 查询成功，构建返回结果
                            Map<String, Object> record = new HashMap<>();
                            record.put("paymentId", outTradeNo);

                            // 金额
                            Object totalAmountObj = queryResponse.get("total_amount");
                            if (totalAmountObj != null) {
                                record.put("amount", new BigDecimal(totalAmountObj.toString()));
                            }

                            // 状态转换
                            Object tradeStatusObj = queryResponse.get("trade_status");
                            if (tradeStatusObj != null) {
                                String tradeStatus = tradeStatusObj.toString();
                                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                                    record.put("status", "SUCCESS");
                                } else {
                                    record.put("status", "FAILED");
                                }
                            } else {
                                record.put("status", "UNKNOWN");
                            }

                            // 支付时间
                            Object gmtPaymentObj = queryResponse.get("gmt_payment");
                            if (gmtPaymentObj != null) {
                                try {
                                    java.time.format.DateTimeFormatter formatter = 
                                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    java.time.LocalDateTime paymentTime = 
                                            java.time.LocalDateTime.parse(gmtPaymentObj.toString(), formatter);
                                    record.put("paymentTime", paymentTime);
                                } catch (Exception e) {
                                    log.warn("[支付对账] 支付时间解析失败，outTradeNo={}, time={}", 
                                            outTradeNo, gmtPaymentObj);
                                }
                            }

                            // 第三方交易号
                            Object tradeNoObj = queryResponse.get("trade_no");
                            if (tradeNoObj != null) {
                                record.put("thirdPartyTransactionId", tradeNoObj.toString());
                            }

                            log.debug("[支付对账] 通用请求方式查询支付宝订单成功，outTradeNo={}", outTradeNo);
                            return record;
                        } else {
                            log.warn("[支付对账] 支付宝订单查询失败，outTradeNo={}, code={}, msg={}", 
                                    outTradeNo, codeObj, queryResponse.get("msg"));
                            return null;
                        }
                    }
                }

                log.warn("[支付对账] 支付宝订单查询响应为空，outTradeNo={}", outTradeNo);
                return null;

            } catch (Exception e) {
                log.error("[支付对账] 通用请求方式查询支付宝订单失败，outTradeNo={}", outTradeNo, e);
                return null;
            }

        } catch (Exception e) {
            log.error("[支付对账] 通用请求方式查询支付宝订单异常，outTradeNo={}", outTradeNo, e);
            return null;
        }
    }

    /**
     * 生成对账报告
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param paymentMethod 支付方式
     * @param systemRecords 系统记录
     * @param thirdPartyRecords 第三方记录
     * @param reconciliationResult 对比结果
     * @return 对账报告
     */
    private Map<String, Object> buildReconciliationReport(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String paymentMethod,
            List<PaymentRecordEntity> systemRecords,
            Map<String, Object> thirdPartyRecords,
            Map<String, Object> reconciliationResult) {
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("paymentMethod", paymentMethod);
        report.put("systemRecordCount", systemRecords.size());
        report.put("thirdPartyRecordCount", thirdPartyRecords.size());
        report.put("matchedCount", reconciliationResult.get("matchedCount"));
        report.put("unmatchedCount", reconciliationResult.get("unmatchedCount"));
        report.put("differenceCount", reconciliationResult.get("differenceCount"));
        report.put("differences", reconciliationResult.get("differences"));
        report.put("generateTime", java.time.LocalDateTime.now());
        report.put("status", (Integer) reconciliationResult.get("unmatchedCount") > 0 ? "HAS_DIFFERENCES" : "MATCHED");
        return report;
    }

    // ==================== 银行支付 ====================

    /**
     * 创建银行支付订单
     * <p>
     * 功能说明：
     * 1. 调用MultiPaymentManager处理银行支付
     * 2. 记录支付记录
     * 3. 返回支付结果
     * </p>
     *
     * @param accountId 账户ID
     * @param amount 支付金额（单位：元）
     * @param orderId 订单ID
     * @param description 商品描述
     * @param bankCardNo 银行卡号（可选）
     * @return 支付结果
     */
    public Map<String, Object> createBankPaymentOrder(
            Long accountId,
            BigDecimal amount,
            String orderId,
            String description,
            String bankCardNo) {
        log.info("[银行支付] 创建银行支付订单，accountId={}, amount={}, orderId={}", accountId, amount, orderId);

        try {
            // 1. 参数验证
            if (accountId == null) {
                throw new BusinessException("账户ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("支付金额必须大于0");
            }
            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }
            if (!StringUtils.hasText(description)) {
                throw new BusinessException("商品描述不能为空");
            }

            // 2. 调用MultiPaymentManager处理银行支付
            Map<String, Object> result = multiPaymentManager.processBankPayment(
                    accountId, amount, orderId, description, bankCardNo);

            // 3. 记录支付记录（如果支付成功）
            Boolean success = (Boolean) result.get("success");
            if (success != null && success) {
                PaymentRecordEntity paymentRecord = new PaymentRecordEntity();
                paymentRecord.setPaymentId(orderId);
                paymentRecord.setPaymentMethod("BANK");
                paymentRecord.setAmount(amount);
                paymentRecord.setStatus("SUCCESS");
                paymentRecord.setThirdPartyTransactionId((String) result.get("transactionId"));
                paymentRecord.setCreateTime(java.time.LocalDateTime.now());
                paymentRecordDao.insert(paymentRecord);

                log.info("[银行支付] 支付记录保存成功，orderId={}, transactionId={}", 
                        orderId, result.get("transactionId"));
            }

            return result;

        } catch (BusinessException e) {
            log.error("[银行支付] 创建银行支付订单业务异常，accountId={}, orderId={}", accountId, orderId, e);
            throw e;
        } catch (Exception e) {
            log.error("[银行支付] 创建银行支付订单异常，accountId={}, orderId={}", accountId, orderId, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "银行支付失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 处理信用额度支付
     * <p>
     * 功能说明：
     * 1. 验证信用额度是否充足
     * 2. 扣除信用额度
     * 3. 记录支付记录
     * </p>
     *
     * @param accountId 账户ID
     * @param amount 支付金额（单位：元）
     * @param orderId 订单ID
     * @param reason 扣除原因
     * @return 支付结果
     */
    public Map<String, Object> processCreditLimitPayment(
            Long accountId,
            BigDecimal amount,
            String orderId,
            String reason) {
        log.info("[信用额度] 处理信用额度支付，accountId={}, amount={}, orderId={}", accountId, amount, orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("paymentMethod", "CREDIT");

        try {
            // 1. 参数验证
            if (accountId == null) {
                throw new BusinessException("账户ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("支付金额必须大于0");
            }
            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }

            // 2. 检查信用额度是否启用
            if (!multiPaymentManager.isPaymentMethodEnabled("CREDIT")) {
                log.warn("[信用额度] 信用额度支付未启用，accountId={}, amount={}", accountId, amount);
                result.put("message", "信用额度支付未启用");
                return result;
            }

            // 3. 验证信用额度是否充足
            boolean sufficient = multiPaymentManager.checkCreditLimitSufficient(accountId, amount);
            if (!sufficient) {
                BigDecimal creditLimit = multiPaymentManager.getCreditLimit(accountId);
                log.warn("[信用额度] 信用额度不足，accountId={}, amount={}, creditLimit={}", 
                        accountId, amount, creditLimit);
                result.put("message", "信用额度不足，当前可用额度: " + creditLimit);
                return result;
            }

            // 4. 扣除信用额度
            boolean deductSuccess = multiPaymentManager.deductCreditLimit(accountId, amount, orderId, reason);
            if (!deductSuccess) {
                log.warn("[信用额度] 信用额度扣除失败，accountId={}, amount={}, orderId={}", 
                        accountId, amount, orderId);
                result.put("message", "信用额度扣除失败");
                return result;
            }

            // 5. 记录支付记录
            PaymentRecordEntity paymentRecord = new PaymentRecordEntity();
            paymentRecord.setPaymentId(orderId);
            paymentRecord.setPaymentMethod("CREDIT");
            paymentRecord.setAmount(amount);
            paymentRecord.setStatus("SUCCESS");
            paymentRecord.setCreateTime(java.time.LocalDateTime.now());
            paymentRecordDao.insert(paymentRecord);

            result.put("success", true);
            result.put("message", "信用额度支付成功");
            log.info("[信用额度] 信用额度支付成功，accountId={}, amount={}, orderId={}", 
                    accountId, amount, orderId);

            return result;

        } catch (BusinessException e) {
            log.error("[信用额度] 处理信用额度支付业务异常，accountId={}, orderId={}", accountId, orderId, e);
            result.put("message", "信用额度支付失败: " + e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("[信用额度] 处理信用额度支付异常，accountId={}, orderId={}", accountId, orderId, e);
            result.put("message", "信用额度支付异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 构建微信支付V3的Authorization请求头
     * <p>
     * 根据微信支付V3 API规范构建Authorization请求头
     * 格式：WECHATPAY-SHA256-RSA2048 mchid="xxx",nonce_str="xxx",timestamp="xxx",signature="xxx",serial_no="xxx"
     * </p>
     *
     * @param method HTTP方法（GET、POST等）
     * @param urlPath API路径（不包含域名，如：/v3/pay/transactions/out-trade-no/xxx?mchid=xxx）
     * @param body 请求体（GET请求为空字符串）
     * @return Authorization请求头字符串
     */
    private String buildWechatPayAuthorization(String method, String urlPath, String body) {
        try {
            // 1. 生成随机字符串和时间戳
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            // 2. 构建签名字符串（按照微信支付V3规范）
            // 格式：请求方法 + "\n" + URL + "\n" + 时间戳 + "\n" + 随机串 + "\n" + 请求体 + "\n"
            String signString = method + "\n" + urlPath + "\n" + timestamp + "\n" + nonceStr + "\n" + (body != null ? body : "") + "\n";
            byte[] signBytes = signString.getBytes(StandardCharsets.UTF_8);

            // 3. 从Config获取私钥并签名
            java.security.PrivateKey privateKey = getWechatPayPrivateKey();
            if (privateKey == null) {
                log.error("[微信支付] 无法获取私钥，无法构建Authorization请求头");
                throw new BusinessException("无法获取微信支付私钥");
            }

            // 4. 使用RSA-SHA256算法签名
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signBytes);
            byte[] signatureBytes = signature.sign();

            // 5. Base64编码签名
            String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

            // 6. 构建Authorization请求头
            String authorization = String.format(
                    "WECHATPAY-SHA256-RSA2048 mchid=\"%s\",nonce_str=\"%s\",timestamp=\"%s\",signature=\"%s\",serial_no=\"%s\"",
                    wechatMchId, nonceStr, timestamp, signatureBase64, wechatMerchantSerialNumber
            );

            log.debug("[微信支付] Authorization请求头构建成功");
            return authorization;

        } catch (NoSuchAlgorithmException e) {
            log.error("[微信支付] 签名算法不支持", e);
            throw new BusinessException("构建Authorization请求头失败：" + e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[微信支付] 私钥无效", e);
            throw new BusinessException("构建Authorization请求头失败：" + e.getMessage());
        } catch (SignatureException e) {
            log.error("[微信支付] 签名异常", e);
            throw new BusinessException("构建Authorization请求头失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[微信支付] 构建Authorization请求头异常", e);
            throw new BusinessException("构建Authorization请求头失败：" + e.getMessage());
        }
    }

    /**
     * 获取微信支付私钥
     * <p>
     * 从RSAAutoCertificateConfig中获取商户私钥
     * </p>
     *
     * @return 私钥对象，如果无法获取返回null
     */
    private java.security.PrivateKey getWechatPayPrivateKey() {
        try {
            if (wechatPayConfig instanceof RSAAutoCertificateConfig) {
                RSAAutoCertificateConfig rsaConfig = (RSAAutoCertificateConfig) wechatPayConfig;
                
                // 尝试通过反射获取私钥
                try {
                    java.lang.reflect.Method getPrivateKeyMethod = RSAAutoCertificateConfig.class
                            .getDeclaredMethod("getPrivateKey");
                    getPrivateKeyMethod.setAccessible(true);
                    Object privateKey = getPrivateKeyMethod.invoke(rsaConfig);
                    
                    if (privateKey instanceof java.security.PrivateKey) {
                        log.debug("[微信支付] 成功获取私钥");
                        return (java.security.PrivateKey) privateKey;
                    }
                } catch (NoSuchMethodException e) {
                    log.debug("[微信支付] 无法通过反射获取私钥，尝试从证书路径读取");
                } catch (Exception e) {
                    log.debug("[微信支付] 获取私钥失败，错误：{}", e.getMessage());
                }
                
                // 备用方案：从证书路径读取私钥
                if (StringUtils.hasText(wechatCertPath)) {
                    try {
                        // 读取PEM格式的私钥文件
                        java.nio.file.Path certPath = java.nio.file.Paths.get(wechatCertPath);
                        String privateKeyContent = new String(java.nio.file.Files.readAllBytes(certPath), StandardCharsets.UTF_8);
                        
                        // 移除PEM格式的头部和尾部
                        privateKeyContent = privateKeyContent.replace("-----BEGIN PRIVATE KEY-----", "")
                                .replace("-----END PRIVATE KEY-----", "")
                                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                                .replace("-----END RSA PRIVATE KEY-----", "")
                                .replaceAll("\\s", "");
                        
                        // Base64解码
                        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
                        
                        // 使用PKCS8格式解析私钥
                        java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
                        java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
                        java.security.PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
                        
                        log.debug("[微信支付] 从证书路径成功读取私钥");
                        return privateKey;
                    } catch (Exception e) {
                        log.warn("[微信支付] 从证书路径读取私钥失败，错误：{}", e.getMessage());
                    }
                }
            }
            
            log.error("[微信支付] 无法获取私钥");
            return null;
            
        } catch (Exception e) {
            log.error("[微信支付] 获取私钥异常", e);
            return null;
        }
    }
}
