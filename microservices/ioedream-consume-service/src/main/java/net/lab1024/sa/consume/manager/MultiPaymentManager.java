package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 多支付方式管理Manager接口
 * <p>
 * 用于管理多种支付方式的统一调用和信用额度扣除
 * 严格遵循CLAUDE.md规范：
 * - Manager接口定义在业务服务模块中
 * - 保持为纯Java接口
 * </p>
 * <p>
 * 业务场景：
 * - 银行支付网关API调用
 * - 信用额度扣除逻辑
 * - 支付方式选择（微信/支付宝/银行/信用额度）
 * - 支付结果处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface MultiPaymentManager {

    /**
     * 处理银行支付
     * <p>
     * 功能说明：
     * 1. 调用银行支付网关API
     * 2. 处理支付结果
     * 3. 记录支付记录
     * </p>
     *
     * @param accountId 账户ID
     * @param amount 支付金额（单位：元）
     * @param orderId 订单ID
     * @param description 商品描述
     * @param bankCardNo 银行卡号（可选，如果为空则从账户信息获取）
     * @return 支付结果（包含支付状态、交易号等）
     */
    Map<String, Object> processBankPayment(
            Long accountId,
            BigDecimal amount,
            String orderId,
            String description,
            String bankCardNo);

    /**
     * 处理信用额度扣除
     * <p>
     * 功能说明：
     * 1. 验证信用额度是否充足
     * 2. 扣除信用额度
     * 3. 记录信用额度使用记录
     * </p>
     *
     * @param accountId 账户ID
     * @param amount 扣除金额（单位：元）
     * @param orderId 订单ID
     * @param reason 扣除原因
     * @return 是否成功
     */
    boolean deductCreditLimit(Long accountId, BigDecimal amount, String orderId, String reason);

    /**
     * 验证信用额度是否充足
     *
     * @param accountId 账户ID
     * @param amount 需要金额（单位：元）
     * @return 是否充足
     */
    boolean checkCreditLimitSufficient(Long accountId, BigDecimal amount);

    /**
     * 获取账户信用额度
     *
     * @param accountId 账户ID
     * @return 信用额度（单位：元）
     */
    BigDecimal getCreditLimit(Long accountId);

    /**
     * 检查支付方式是否启用
     *
     * @param paymentMethod 支付方式（WECHAT/ALIPAY/BANK/CREDIT）
     * @return 是否启用
     */
    boolean isPaymentMethodEnabled(String paymentMethod);
}




