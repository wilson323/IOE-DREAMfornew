package net.lab1024.sa.consume.service.payment;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 微信支付服务接口
 * <p>
 * 专门处理微信支付相关业务，从PaymentService拆分而来
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
public interface WechatPayService {

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
    Map<String, Object> createWechatPayOrder(
            String orderId,
            BigDecimal amount,
            String description,
            String openId,
            String payType);

    /**
     * 创建JSAPI支付订单
     *
     * @param orderId     订单ID
     * @param amount      金额（元）
     * @param description 商品描述
     * @param openId      用户OpenID
     * @return 支付参数
     */
    Map<String, Object> createJsapiPayOrder(String orderId, BigDecimal amount,
            String description, String openId);

    /**
     * 创建Native支付订单（扫码支付）
     *
     * @param orderId     订单ID
     * @param amount      金额（元）
     * @param description 商品描述
     * @return 支付参数（包含二维码URL）
     */
    Map<String, Object> createNativePayOrder(String orderId, BigDecimal amount, String description);

    /**
     * 创建APP支付订单
     *
     * @param orderId     订单ID
     * @param amount      金额（元）
     * @param description 商品描述
     * @return 支付参数
     */
    Map<String, Object> createAppPayOrder(String orderId, BigDecimal amount, String description);

    /**
     * 创建H5支付订单
     *
     * @param orderId     订单ID
     * @param amount      金额（元）
     * @param description 商品描述
     * @return 支付参数（包含H5支付跳转URL）
     */
    Map<String, Object> createH5PayOrder(String orderId, BigDecimal amount, String description);

    /**
     * 申请退款（微信支付）
     *
     * @param orderId      原订单ID
     * @param refundId     退款单ID
     * @param totalAmount  订单总金额（分）
     * @param refundAmount 退款金额（分）
     * @return 退款结果
     */
    Map<String, Object> wechatRefund(
            String orderId,
            String refundId,
            Integer totalAmount,
            Integer refundAmount);

    /**
     * 查询微信支付订单状态
     *
     * @param orderId 订单ID
     * @return 订单状态信息
     */
    Map<String, Object> queryOrderStatus(String orderId);
}
