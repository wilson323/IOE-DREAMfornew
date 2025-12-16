package net.lab1024.sa.consume.service.payment.adapter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;

/**
 * 支付宝渠道适配器
 */
@Slf4j
@Component
public class AlipayPayAdapter {

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

    private AlipayClient alipayClient;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    public boolean isEnabled() {
        return alipayEnabled != null && alipayEnabled;
    }

    public void initIfNeeded() {
        initAlipayClient();
    }

    public boolean isReady() {
        return alipayClient != null;
    }

    public boolean verifyNotifySignature(Map<String, String> params) {
        try {
            if (!StringUtils.hasText(alipayPublicKey)) {
                return false;
            }
            return AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
        } catch (Exception e) {
            log.warn("[支付宝] 回调签名校验失败", e);
            return false;
        }
    }

    public Map<String, Object> createAlipayOrder(
            String orderId,
            BigDecimal amount,
            String subject,
            String payType) {

        log.info("[支付宝] 创建订单, orderId={}, amount={}, payType={}", orderId, amount, payType);

        try {
            if (!isEnabled()) {
                log.warn("[支付宝] 支付服务未启用，返回模拟数据");
                return getMockAlipayResult(orderId, payType);
            }

            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("支付金额必须大于0");
            }
            if (!StringUtils.hasText(subject)) {
                throw new BusinessException("商品标题不能为空");
            }

            initAlipayClient();
            if (alipayClient == null) {
                log.warn("[支付宝] 客户端未初始化，返回模拟数据");
                return getMockAlipayResult(orderId, payType);
            }

            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setOutTradeNo(orderId);
            model.setTotalAmount(amount.toString());
            model.setSubject(subject);
            model.setProductCode("QUICK_MSECURITY_PAY");
            request.setBizModel(model);
            request.setNotifyUrl(alipayNotifyUrl);

            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);

            if (response == null || !response.isSuccess()) {
                String errorMsg = response != null ? response.getSubMsg() : "支付宝响应为空";
                log.error("[支付宝] 创建订单失败: {}", errorMsg);
                throw new BusinessException("支付宝创建订单失败: " + errorMsg);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("orderString", response.getBody());
            result.put("orderId", orderId);
            result.put("payType", payType);
            result.put("mock", false);

            log.info("[支付宝] 订单创建成功: orderId={}", orderId);
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[支付宝] 创建订单参数错误: error={}", e.getMessage(), e);
            throw new ParamException("ALIPAY_ORDER_PARAM_ERROR", "支付宝创建订单参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.error("[支付宝] 创建订单业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (SystemException e) {
            log.error("[支付宝] 创建订单系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("ALIPAY_ORDER_SYSTEM_ERROR", "支付宝创建订单失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[支付宝] 创建订单未知异常", e);
            throw new SystemException("ALIPAY_ORDER_SYSTEM_ERROR", "支付宝创建订单失败: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> alipayRefund(
            String orderId,
            BigDecimal refundAmount,
            String reason) {

        log.info("[支付宝退款] orderId={}, amount={}, reason={}", orderId, refundAmount, reason);

        try {
            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("退款金额必须大于0");
            }

            initAlipayClient();
            if (alipayClient == null) {
                log.warn("[支付宝退款] 客户端未初始化，返回模拟数据");
                return Map.of("success", true, "fundChange", "Y", "refundFee", refundAmount.toString(), "mock", true);
            }

            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(orderId);
            model.setRefundAmount(refundAmount.toString());
            if (StringUtils.hasText(reason)) {
                model.setRefundReason(reason);
            }
            request.setBizModel(model);

            AlipayTradeRefundResponse response = alipayClient.execute(request);

            if (response == null || !response.isSuccess()) {
                String errorMsg = response != null ? response.getSubMsg() : "支付宝响应为空";
                log.error("[支付宝退款] 退款失败: {}", errorMsg);
                throw new BusinessException("支付宝退款失败: " + errorMsg);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("fundChange", response.getFundChange());
            result.put("refundFee", response.getRefundFee() != null ? response.getRefundFee() : refundAmount.toString());
            result.put("mock", false);

            log.info("[支付宝退款] 退款成功: orderId={}, amount={}", orderId, refundAmount);
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[支付宝退款] 退款参数错误: error={}", e.getMessage(), e);
            throw new ParamException("ALIPAY_REFUND_PARAM_ERROR", "支付宝退款参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.error("[支付宝退款] 退款业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (SystemException e) {
            log.error("[支付宝退款] 退款系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("ALIPAY_REFUND_SYSTEM_ERROR", "支付宝退款失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[支付宝退款] 退款未知异常", e);
            throw new SystemException("ALIPAY_REFUND_SYSTEM_ERROR", "支付宝退款失败: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> queryAlipayOrderByOutTradeNo(String outTradeNo) {
        try {
            if (alipayClient == null) {
                log.warn("[支付对账] 支付宝客户端未初始化，无法查询订单，outTradeNo={}", outTradeNo);
                return null;
            }

            try {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

                java.util.Map<String, String> bizContent = new java.util.HashMap<>();
                bizContent.put("out_trade_no", outTradeNo);

                String bizContentJson = objectMapper.writeValueAsString(bizContent);
                request.setBizContent(bizContentJson);

                AlipayTradeQueryResponse response = alipayClient.execute(request);

                if (response != null && response.isSuccess()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("paymentId", outTradeNo);

                    if (response.getTotalAmount() != null) {
                        BigDecimal amount = new BigDecimal(response.getTotalAmount());
                        record.put("amount", amount);
                    }

                    String tradeStatus = response.getTradeStatus();
                    if (tradeStatus != null) {
                        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                            record.put("status", "SUCCESS");
                        } else {
                            record.put("status", "FAILED");
                        }
                    } else {
                        record.put("status", "UNKNOWN");
                    }

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
                log.warn("[支付对账] 支付宝SDK不支持AlipayTradeQueryRequest，使用通用请求方式，outTradeNo={}", outTradeNo);
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

    private Map<String, Object> queryAlipayOrderByGenericRequest(String outTradeNo) {
        try {
            if (alipayClient == null) {
                log.warn("[支付对账] 支付宝客户端未初始化，无法查询订单，outTradeNo={}", outTradeNo);
                return null;
            }

            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);

            String bizContentJson = objectMapper.writeValueAsString(bizContent);

            try {
                String method = "alipay.trade.query";

                Map<String, String> params = new HashMap<>();
                params.put("app_id", alipayAppId);
                params.put("method", method);
                params.put("charset", "UTF-8");
                params.put("sign_type", "RSA2");
                params.put("timestamp", java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                params.put("version", "1.0");
                params.put("biz_content", bizContentJson);

                String sign = AlipaySignature.getSignContent(params);
                String signedContent;
                try {
                    signedContent = AlipaySignature.rsa256Sign(sign, alipayPrivateKey, "UTF-8");
                } catch (NoSuchMethodError e) {
                    signedContent = AlipaySignature.rsaSign(sign, alipayPrivateKey, "UTF-8", "RSA2");
                }
                params.put("sign", signedContent);

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

                // 使用非null断言，因为url已经过验证
                @SuppressWarnings("null")
                String responseBody = restTemplate.getForObject(url, String.class);
                if (responseBody == null || responseBody.isEmpty()) {
                    log.warn("[支付对账] 支付宝订单查询响应为空，outTradeNo={}", outTradeNo);
                    return null;
                }

                if (!responseBody.isEmpty()) {
                    Map<String, Object> responseMap = objectMapper.readValue(
                            responseBody,
                            new TypeReference<Map<String, Object>>() {});

                    Object alipayTradeQueryResponseObj = responseMap.get("alipay_trade_query_response");
                    if (alipayTradeQueryResponseObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> queryResponse = (Map<String, Object>) alipayTradeQueryResponseObj;

                        Object codeObj = queryResponse.get("code");
                        if (codeObj != null && "10000".equals(codeObj.toString())) {
                            Map<String, Object> record = new HashMap<>();
                            record.put("paymentId", outTradeNo);

                            Object totalAmountObj = queryResponse.get("total_amount");
                            if (totalAmountObj != null) {
                                record.put("amount", new BigDecimal(totalAmountObj.toString()));
                            }

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

    private void initAlipayClient() {
        if (alipayClient != null) {
            return;
        }

        if (!isEnabled() || !StringUtils.hasText(alipayAppId) ||
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
        } catch (IllegalArgumentException | ParamException e) {
            log.error("[支付宝] 客户端初始化参数错误: error={}", e.getMessage(), e);
            throw new ParamException("ALIPAY_CLIENT_CONFIG_PARAM_ERROR", "支付宝客户端配置参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.error("[支付宝] 客户端初始化业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (SystemException e) {
            log.error("[支付宝] 客户端初始化系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("ALIPAY_CLIENT_CONFIG_SYSTEM_ERROR", "支付宝客户端初始化失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[支付宝] 客户端初始化未知异常", e);
            throw new SystemException("ALIPAY_CLIENT_CONFIG_SYSTEM_ERROR", "支付宝客户端初始化失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> getMockAlipayResult(String orderId, String payType) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("payType", payType);
        result.put("mock", true);

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
}



