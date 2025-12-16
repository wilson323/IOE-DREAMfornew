package net.lab1024.sa.consume.service.payment;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付服务接口
 * <p>
 * 专门处理支付宝支付相关业务，从PaymentService拆分而来
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
public interface AlipayPayService {

    /**
     * 创建支付宝支付订单
     *
     * @param orderId 订单ID
     * @param amount  金额（元）
     * @param subject 商品标题
     * @param payType 支付类型（APP/Web/Wap）
     * @return 支付参数
     */
    Map<String, Object> createAlipayOrder(
            String orderId,
            BigDecimal amount,
            String subject,
            String payType);

    /**
     * 创建支付宝APP支付订单
     *
     * @param orderId 订单ID
     * @param amount  金额（元）
     * @param subject 商品标题
     * @return 支付参数（包含orderString）
     */
    Map<String, Object> createAppPayOrder(String orderId, BigDecimal amount, String subject);

    /**
     * 创建支付宝Web支付订单
     *
     * @param orderId 订单ID
     * @param amount  金额（元）
     * @param subject 商品标题
     * @return 支付参数（包含支付表单HTML）
     */
    Map<String, Object> createWebPayOrder(String orderId, BigDecimal amount, String subject);

    /**
     * 创建支付宝Wap支付订单
     *
     * @param orderId 订单ID
     * @param amount  金额（元）
     * @param subject 商品标题
     * @return 支付参数（包含支付跳转URL）
     */
    Map<String, Object> createWapPayOrder(String orderId, BigDecimal amount, String subject);

    /**
     * 申请退款（支付宝）
     *
     * @param orderId      原订单ID
     * @param refundAmount 退款金额（元）
     * @param reason       退款原因
     * @return 退款结果
     */
    Map<String, Object> alipayRefund(
            String orderId,
            BigDecimal refundAmount,
            String reason);

    /**
     * 查询支付宝订单状态
     *
     * @param orderId 订单ID
     * @return 订单状态信息
     */
    Map<String, Object> queryOrderStatus(String orderId);
}
