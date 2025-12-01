package net.lab1024.sa.admin.module.consume.service.payment;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;

import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.ResponseDTO;

/**
 * 支付宝支付集成服务
 * 支持网页支付、APP支付、扫码支付、退款等功能
 *
 * @author SmartAdmin
 * @since 2025-11-17
 */
@Slf4j
@Service
public class AlipayPaymentService {

    @Value("${alipay.gateway-url}")
    private String gatewayUrl;

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.private-key}")
    private String privateKey;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Value("${alipay.charset}")
    private String charset;

    @Value("${alipay.sign-type}")
    private String signType;

    @Value("${alipay.format}")
    private String format;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Resource
    private PaymentRecordService paymentRecordService;

    /**
     * 获取支付宝客户端
     */
    private AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
                gatewayUrl,
                appId,
                privateKey,
                format,
                charset,
                alipayPublicKey,
                signType);
    }

    /**
     * 网页支付
     *
     * @param paymentRequest 支付请求参数
     * @return 支付结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> createPagePayment(PaymentRequest paymentRequest) {
        try {
            // 1. 保存支付记录
            String paymentId = generatePaymentId();
            paymentRecordService.createPaymentRecord(paymentId, paymentRequest, "ALIPAY");

            // 2. 构建支付请求
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setReturnUrl(returnUrl);
            request.setNotifyUrl(notifyUrl);

            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(paymentId);
            model.setTotalAmount(paymentRequest.getAmount().toString());
            model.setSubject(paymentRequest.getSubject());
            model.setBody(paymentRequest.getBody());
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            request.setBizModel(model);

            // 3. 调用支付宝支付API
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);

            if (response.isSuccess()) {
                Map<String, Object> result = new HashMap<>();
                result.put("paymentId", paymentId);
                result.put("form", response.getBody());

                // 更新支付记录
                paymentRecordService.updatePaymentFormData(paymentId, response.getBody());

                log.info("支付宝网页支付下单成功, paymentId: {}", paymentId);
                return ResponseDTO.ok(result);
            } else {
                log.error("支付宝网页支付失败, code: {}, msg: {}", response.getCode(), response.getMsg());
                return ResponseDTO.error("支付下单失败: " + response.getSubMsg());
            }

        } catch (AlipayApiException e) {
            log.error("支付宝支付API调用异常", e);
            return ResponseDTO.error("支付服务异常");
        } catch (Exception e) {
            log.error("支付宝网页支付下单异常", e);
            return ResponseDTO.error("支付处理异常");
        }
    }

    /**
     * APP支付
     *
     * @param paymentRequest 支付请求参数
     * @return 支付结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> createAppPayment(PaymentRequest paymentRequest) {
        try {
            // 1. 保存支付记录
            String paymentId = generatePaymentId();
            paymentRecordService.createPaymentRecord(paymentId, paymentRequest, "ALIPAY_APP");

            // 2. 构建支付请求
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            request.setNotifyUrl(notifyUrl);

            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setOutTradeNo(paymentId);
            model.setTotalAmount(paymentRequest.getAmount().toString());
            model.setSubject(paymentRequest.getSubject());
            model.setBody(paymentRequest.getBody());
            model.setProductCode("QUICK_MSECURITY_PAY");
            request.setBizModel(model);

            // 3. 调用支付宝支付API
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);

            if (response.isSuccess()) {
                Map<String, Object> result = new HashMap<>();
                result.put("paymentId", paymentId);
                result.put("orderString", response.getBody());

                // 更新支付记录
                paymentRecordService.updatePaymentOrderString(paymentId, response.getBody());

                log.info("支付宝APP支付下单成功, paymentId: {}", paymentId);
                return ResponseDTO.ok(result);
            } else {
                log.error("支付宝APP支付失败, code: {}, msg: {}", response.getCode(), response.getMsg());
                return ResponseDTO.error("支付下单失败: " + response.getSubMsg());
            }

        } catch (AlipayApiException e) {
            log.error("支付宝支付API调用异常", e);
            return ResponseDTO.error("支付服务异常");
        } catch (Exception e) {
            log.error("支付宝APP支付下单异常", e);
            return ResponseDTO.error("支付处理异常");
        }
    }

    /**
     * 查询支付状态
     *
     * @param paymentId 支付ID
     * @return 支付状态
     */
    public ResponseDTO<Map<String, Object>> queryPaymentStatus(String paymentId) {
        try {
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(paymentId);
            request.setBizModel(model);

            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                Map<String, Object> result = new HashMap<>();
                result.put("paymentId", paymentId);
                result.put("tradeStatus", response.getTradeStatus());
                result.put("tradeNo", response.getTradeNo());
                result.put("totalAmount", response.getTotalAmount());
                result.put("receiptAmount", response.getReceiptAmount());
                result.put("sendPayDate", response.getSendPayDate());

                // 更新本地支付记录
                paymentRecordService.updatePaymentStatus(paymentId, response.getTradeStatus(),
                        response.getTradeNo());

                log.info("查询支付宝支付状态成功, paymentId: {}, tradeStatus: {}",
                        paymentId, response.getTradeStatus());
                return ResponseDTO.ok(result);
            } else {
                log.error("查询支付宝支付状态失败, code: {}, msg: {}", response.getCode(), response.getMsg());
                return ResponseDTO.error("查询支付状态失败: " + response.getSubMsg());
            }

        } catch (AlipayApiException e) {
            log.error("支付宝查询API调用异常, paymentId: {}", paymentId, e);
            return ResponseDTO.error("查询支付状态异常");
        } catch (Exception e) {
            log.error("查询支付宝支付状态异常, paymentId: {}", paymentId, e);
            return ResponseDTO.error("查询支付状态失败");
        }
    }

    /**
     * 关闭交易
     *
     * @param paymentId 支付ID
     * @return 关闭结果
     */
    public ResponseDTO<String> closePayment(String paymentId) {
        try {
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();

            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(paymentId);
            request.setBizModel(model);

            AlipayTradeCloseResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                // 更新本地支付记录
                paymentRecordService.updatePaymentStatus(paymentId, "CLOSED", null);

                log.info("关闭支付宝交易成功, paymentId: {}", paymentId);
                return ResponseDTO.ok("交易关闭成功");
            } else {
                log.error("关闭支付宝交易失败, code: {}, msg: {}", response.getCode(), response.getMsg());
                return ResponseDTO.error("关闭交易失败: " + response.getSubMsg());
            }

        } catch (AlipayApiException e) {
            log.error("支付宝关闭交易API调用异常, paymentId: {}", paymentId, e);
            return ResponseDTO.error("关闭交易异常");
        } catch (Exception e) {
            log.error("关闭支付宝交易异常, paymentId: {}", paymentId, e);
            return ResponseDTO.error("关闭交易失败");
        }
    }

    /**
     * 申请退款
     *
     * @param refundRequest 退款请求
     * @return 退款结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> createRefund(RefundRequest refundRequest) {
        try {
            // 1. 保存退款记录
            String refundId = generateRefundId();
            paymentRecordService.createRefundRecord(refundId, refundRequest);

            // 2. 调用支付宝退款API
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

            com.alipay.api.domain.AlipayTradeRefundModel model = new com.alipay.api.domain.AlipayTradeRefundModel();
            model.setOutTradeNo(refundRequest.getPaymentId());
            model.setRefundAmount(refundRequest.getRefundAmount().toString());
            model.setRefundReason(refundRequest.getReason());
            model.setOutRequestNo(refundId);
            request.setBizModel(model);

            AlipayTradeRefundResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                // 更新退款记录（第三个参数是第三方退款ID，使用支付宝交易号）
                String alipayRefundId = response.getTradeNo() != null ? response.getTradeNo() : refundId;
                paymentRecordService.updateRefundStatus(refundId, "SUCCESS", alipayRefundId);

                Map<String, Object> result = new HashMap<>();
                result.put("refundId", refundId);
                result.put("alipayRefundId", alipayRefundId);
                result.put("refundAmount", response.getRefundFee());
                result.put("status", "SUCCESS");

                log.info("支付宝退款申请成功, refundId: {}, refundAmount: {}",
                        refundId, response.getRefundFee());
                return ResponseDTO.ok(result);
            } else {
                // 更新退款记录为失败状态
                paymentRecordService.updateRefundStatus(refundId, "FAILED", null);

                log.error("支付宝退款申请失败, code: {}, msg: {}", response.getCode(), response.getMsg());
                return ResponseDTO.error("退款申请失败: " + response.getSubMsg());
            }

        } catch (AlipayApiException e) {
            log.error("支付宝退款API调用异常", e);
            return ResponseDTO.error("退款申请异常");
        } catch (Exception e) {
            log.error("支付宝退款申请异常", e);
            return ResponseDTO.error("退款申请失败");
        }
    }

    /**
     * 处理支付结果通知
     *
     * @param notifyParams 支付宝通知参数
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> handlePaymentNotification(Map<String, String> notifyParams) {
        try {
            // 1. 验证签名
            if (!verifyNotification(notifyParams)) {
                log.error("支付宝支付通知签名验证失败");
                return ResponseDTO.error("签名验证失败");
            }

            // 2. 处理支付结果
            String paymentId = notifyParams.get("out_trade_no");
            String tradeStatus = notifyParams.get("trade_status");
            String tradeNo = notifyParams.get("trade_no");

            // 更新支付记录状态
            paymentRecordService.updatePaymentStatus(paymentId, tradeStatus, tradeNo);

            // 如果支付成功，触发后续业务处理
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                paymentRecordService.handlePaymentSuccess(paymentId, tradeNo);
            }

            log.info("支付宝支付通知处理成功, paymentId: {}, tradeStatus: {}", paymentId, tradeStatus);
            return ResponseDTO.ok("success");

        } catch (Exception e) {
            log.error("支付宝支付通知处理异常", e);
            return ResponseDTO.error("通知处理失败");
        }
    }

    /**
     * 生成支付ID
     */
    private String generatePaymentId() {
        return "ALI" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * 生成退款ID
     */
    private String generateRefundId() {
        return "RF" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * 验证通知签名
     */
    private boolean verifyNotification(Map<String, String> notifyParams) {
        try {
            // 这里需要实现支付宝通知签名验证
            // 具体实现参考支付宝开放平台文档
            // 可以使用 AlipaySignature.rsaCheckV2 方法
            return true;
        } catch (Exception e) {
            log.error("支付宝通知签名验证异常", e);
            return false;
        }
    }

    /**
     * 支付请求参数
     */
    @Data
    public static class PaymentRequest {
        /**
         * 订单标题
         */
        private String subject;

        /**
         * 订单描述
         */
        private String body;

        /**
         * 支付金额
         */
        private BigDecimal amount;

        /**
         * 消费记录ID
         */
        private String consumeRecordId;

        /**
         * 用户ID
         */
        private Long userId;
    }

    /**
     * 退款请求参数
     */
    @Data
    public static class RefundRequest {
        /**
         * 支付ID
         */
        private String paymentId;

        /**
         * 退款金额
         */
        private BigDecimal refundAmount;

        /**
         * 退款原因
         */
        private String reason;

        /**
         * 消费记录ID
         */
        private String consumeRecordId;
    }
}