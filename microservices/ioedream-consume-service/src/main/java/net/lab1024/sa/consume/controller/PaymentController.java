package net.lab1024.sa.consume.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.service.PaymentService;

/**
 * 支付管理PC端控制器
 * <p>
 * 提供PC端支付管理相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 微信支付订单创建
 * - 支付宝支付订单创建
 * - 支付回调处理
 * - 退款处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consume/payment")
@Tag(name = "支付管理PC端", description = "微信支付、支付宝支付、退款、通知处理等API")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    /**
     * 创建微信支付订单
     * <p>
     * 创建微信支付V3预支付订单，支持多种支付方式（JSAPI、APP、H5、Native）
     * 返回支付所需参数，前端使用这些参数调用微信支付SDK完成支付
     * </p>
     *
     * @param orderId 订单ID（必填，业务订单号）
     * @param amount 金额（必填，单位：元）
     * @param description 商品描述（必填）
     * @param openId 用户OpenID（JSAPI支付时必需）
     * @param payType 支付类型（必填：JSAPI/APP/H5/Native）
     * @return 支付参数，包含prepay_id、timeStamp、nonceStr、sign等
     * @apiNote 示例请求：
     * <pre>
     * POST /api/v1/consume/payment/wechat/createOrder?orderId=ORDER001&amount=100.00&description=测试订单&payType=JSAPI&openId=oUpF8uMuAJO_M2pxb1Q9zNjWeS6o
     * </pre>
     */
    @PostMapping("/wechat/createOrder")
    @Operation(
        summary = "创建微信支付订单",
        description = "创建微信支付V3预支付订单，支持多种支付方式（JSAPI、APP、H5、Native）。返回支付所需参数，前端使用这些参数调用微信支付SDK完成支付。",
        tags = {"支付管理PC端"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "创建成功，返回支付参数",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Map.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "参数错误或支付配置错误"
    )
    @PreAuthorize("hasRole('CONSUME_MANAGER') or hasRole('CONSUME_USER')")
    public ResponseDTO<Map<String, Object>> createWechatPayOrder(
            @Parameter(description = "订单ID（业务订单号）", required = true, example = "ORDER001")
            @RequestParam @NotBlank String orderId,
            @Parameter(description = "金额（单位：元）", required = true, example = "100.00")
            @RequestParam @NotNull BigDecimal amount,
            @Parameter(description = "商品描述", required = true, example = "测试订单")
            @RequestParam @NotBlank String description,
            @Parameter(description = "用户OpenID（JSAPI支付时必需）", example = "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o")
            @RequestParam(required = false) String openId,
            @Parameter(description = "支付类型（JSAPI/APP/H5/Native）", required = true, example = "JSAPI")
            @RequestParam @NotBlank String payType) {
        log.info("[支付管理] 创建微信支付订单，orderId={}, amount={}, payType={}", orderId, amount, payType);
        try {
            Map<String, Object> result = paymentService.createWechatPayOrder(
                    orderId, amount, description, openId, payType);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[支付管理] 创建微信支付订单失败，orderId={}, amount={}", orderId, amount, e);
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
    @Operation(
        summary = "处理微信支付回调",
        description = "接收并处理微信支付平台的回调通知，验证签名、更新订单状态、处理业务逻辑。此接口不需要认证，由微信支付平台直接调用。",
        tags = {"支付管理PC端"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "处理成功，返回SUCCESS",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Map.class)
        )
    )
    public Map<String, Object> handleWechatPayNotify(
            @Parameter(description = "通知数据（JSON格式，微信支付平台发送的加密数据）", required = true)
            @RequestBody String notifyData) {
        log.info("[支付管理] 接收微信支付回调通知，notifyData长度={}", 
                notifyData != null ? notifyData.length() : 0);
        try {
            Map<String, Object> result = paymentService.handleWechatPayNotify(notifyData);
            return result;
        } catch (Exception e) {
            log.error("[支付管理] 处理微信支付回调失败", e);
            return Map.of("code", "FAIL", "message", "处理回调失败: " + e.getMessage());
        }
    }

    /**
     * 创建支付宝支付订单
     *
     * @param orderId 订单ID
     * @param amount 金额（元）
     * @param subject 商品标题
     * @param payType 支付类型（APP/Web/Wap）
     * @return 支付参数
     */
    @PostMapping("/alipay/createOrder")
    @Operation(summary = "创建支付宝支付订单", description = "创建支付宝预支付订单，返回支付所需参数")
    @PreAuthorize("hasRole('CONSUME_MANAGER') or hasRole('CONSUME_USER')")
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
        } catch (Exception e) {
            log.error("[支付管理] 创建支付宝支付订单失败，orderId={}, amount={}", orderId, amount, e);
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
    @Operation(summary = "处理支付宝支付回调", description = "接收并处理支付宝平台的回调通知")
    public String handleAlipayNotify(@RequestParam Map<String, String> params) {
        log.info("[支付管理] 接收支付宝支付回调通知，参数数量={}", params != null ? params.size() : 0);
        try {
            String result = paymentService.handleAlipayNotify(params);
            return result;
        } catch (Exception e) {
            log.error("[支付管理] 处理支付宝支付回调失败", e);
            return "fail";
        }
    }

    /**
     * 微信支付退款
     *
     * @param orderId 订单ID
     * @param refundId 退款单ID
     * @param totalAmount 订单总金额（分）
     * @param refundAmount 退款金额（分）
     * @return 退款结果
     */
    @PostMapping("/wechat/refund")
    @Operation(summary = "微信支付退款", description = "发起微信支付订单的退款申请")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
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
        } catch (Exception e) {
            log.error("[支付管理] 微信支付退款失败，orderId={}, refundId={}", orderId, refundId, e);
            return ResponseDTO.error("WECHAT_REFUND_ERROR", "微信支付退款失败: " + e.getMessage());
        }
    }

    /**
     * 支付宝退款
     *
     * @param orderId 订单ID
     * @param refundAmount 退款金额（元）
     * @param reason 退款原因（可选）
     * @return 退款结果
     */
    @PostMapping("/alipay/refund")
    @Operation(summary = "支付宝退款", description = "发起支付宝支付订单的退款申请")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Map<String, Object>> alipayRefund(
            @RequestParam @NotBlank String orderId,
            @RequestParam @NotNull BigDecimal refundAmount,
            @RequestParam(required = false) String reason) {
        log.info("[支付管理] 支付宝退款，orderId={}, refundAmount={}, reason={}", 
                orderId, refundAmount, reason);
        try {
            Map<String, Object> result = paymentService.alipayRefund(orderId, refundAmount, reason);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[支付管理] 支付宝退款失败，orderId={}, refundAmount={}", orderId, refundAmount, e);
            return ResponseDTO.error("ALIPAY_REFUND_ERROR", "支付宝退款失败: " + e.getMessage());
        }
    }

    /**
     * 创建银行支付订单
     *
     * @param accountId 账户ID
     * @param amount 金额（元）
     * @param orderId 订单ID
     * @param description 商品描述
     * @param bankCardNo 银行卡号（可选）
     * @return 支付结果
     */
    @PostMapping("/bank/createOrder")
    @Operation(summary = "创建银行支付订单", description = "创建银行支付订单，调用银行支付网关API")
    @PreAuthorize("hasRole('CONSUME_MANAGER') or hasRole('CONSUME_USER')")
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
        } catch (Exception e) {
            log.error("[支付管理] 创建银行支付订单失败，accountId={}, orderId={}", accountId, orderId, e);
            return ResponseDTO.error("CREATE_BANK_ORDER_ERROR", "创建银行支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 处理信用额度支付
     *
     * @param accountId 账户ID
     * @param amount 金额（元）
     * @param orderId 订单ID
     * @param reason 扣除原因（可选）
     * @return 支付结果
     */
    @PostMapping("/credit/processPayment")
    @Operation(summary = "处理信用额度支付", description = "使用信用额度进行支付，扣除信用额度")
    @PreAuthorize("hasRole('CONSUME_MANAGER') or hasRole('CONSUME_USER')")
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
        } catch (Exception e) {
            log.error("[支付管理] 处理信用额度支付失败，accountId={}, orderId={}", accountId, orderId, e);
            return ResponseDTO.error("CREDIT_PAYMENT_ERROR", "处理信用额度支付失败: " + e.getMessage());
        }
    }
}

