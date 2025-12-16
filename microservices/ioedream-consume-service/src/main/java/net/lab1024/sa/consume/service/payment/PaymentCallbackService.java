package net.lab1024.sa.consume.service.payment;

import java.util.Map;

/**
 * 支付回调服务接口
 * <p>
 * 专门处理支付回调相关业务，从PaymentService拆分而来
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
public interface PaymentCallbackService {

    /**
     * 处理微信支付回调（简化版）
     *
     * @param notifyData 回调数据（JSON格式，V3版本）
     * @return 处理结果
     */
    Map<String, Object> handleWechatPayNotify(String notifyData);

    /**
     * 处理微信支付回调（完整参数）
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
    Map<String, Object> handleWechatPayNotify(String notifyData, String signature,
            String timestamp, String nonce, String serial);

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
     *
     * @param params 回调参数
     * @return 处理结果（"success"表示成功，"fail"表示失败）
     */
    String handleAlipayNotify(Map<String, String> params);

    /**
     * 记录支付审计日志
     *
     * @param paymentId 支付ID
     * @param operation 操作类型
     * @param detail    操作详情
     * @param result    结果状态（1-成功，0-失败）
     */
    void recordPaymentAuditLog(String paymentId, String operation, String detail, Integer result);

    /**
     * 发送支付通知
     *
     * @param userId      用户ID
     * @param paymentId   支付ID
     * @param status      支付状态
     * @param message     通知消息
     */
    void sendPaymentNotification(Long userId, String paymentId, String status, String message);
}
