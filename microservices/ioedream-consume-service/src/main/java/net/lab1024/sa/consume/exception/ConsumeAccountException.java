package net.lab1024.sa.consume.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消费账户异常
 * <p>
 * 专门用于处理账户相关的异常情况
 * 继承ConsumeBusinessException，确保与全局标准一致
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumeAccountException extends ConsumeBusinessException {

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 用户ID
     */
    private Long userId;

    public ConsumeAccountException(String code, String message, Long accountId) {
        super(code, message);
        this.accountId = accountId;
    }

    public ConsumeAccountException(String code, String message, Long accountId, Long userId) {
        super(code, message);
        this.accountId = accountId;
        this.userId = userId;
    }

    public ConsumeAccountException(String code, String message, Long accountId, Object details) {
        super(code, message, details);
        this.accountId = accountId;
    }

    // ==================== 常用静态工厂方法 ====================

    /**
     * 无效金额
     */
    public static ConsumeAccountException invalidAmount(String amount) {
        return new ConsumeAccountException("INVALID_AMOUNT",
            "无效金额: " + amount, null);
    }

    /**
     * 并发修改
     */
    public static ConsumeAccountException concurrentModification(Long accountId) {
        return new ConsumeAccountException("CONCURRENT_MODIFICATION",
            "并发修改，请重试: " + accountId, accountId);
    }

    /**
     * 账户不存在
     */
    public static ConsumeAccountException notFound(Long accountId) {
        return new ConsumeAccountException("ACCOUNT_NOT_FOUND",
            "账户不存在: " + accountId, accountId);
    }

    /**
     * 账户已冻结
     */
    public static ConsumeAccountException frozen(Long accountId) {
        return new ConsumeAccountException("ACCOUNT_FROZEN",
            "账户已冻结: " + accountId, accountId);
    }

    /**
     * 账户已注销
     */
    public static ConsumeAccountException closed(Long accountId) {
        return new ConsumeAccountException("ACCOUNT_CLOSED",
            "账户已注销: " + accountId, accountId);
    }

    /**
     * 用户账户不存在
     */
    public static ConsumeAccountException userNotFound(Long userId) {
        return new ConsumeAccountException("USER_ACCOUNT_NOT_FOUND",
            "用户账户不存在: " + userId, null, userId);
    }

    /**
     * 账户余额不足
     */
    public static ConsumeAccountException insufficientBalance(Long accountId, String balance, String amount) {
        return new ConsumeAccountException("INSUFFICIENT_BALANCE",
            "账户余额不足，当前余额: " + balance + "，需要金额: " + amount, accountId,
            java.util.Map.of("balance", balance, "amount", amount));
    }

    /**
     * 账户状态异常
     */
    public static ConsumeAccountException invalidStatus(Long accountId, Integer status) {
        return new ConsumeAccountException("ACCOUNT_STATUS_INVALID",
            "账户状态异常: " + accountId + "，状态: " + status, accountId,
            java.util.Map.of("accountId", accountId, "status", status));
    }

    /**
     * 充值失败
     */
    public static ConsumeAccountException rechargeFailed(Long accountId, String reason) {
        return new ConsumeAccountException("RECHARGE_FAILED",
            "账户充值失败: " + accountId + "，原因: " + reason, accountId,
            java.util.Map.of("accountId", accountId, "reason", reason));
    }

    /**
     * 账户已存在
     */
    public static ConsumeAccountException accountAlreadyExists(String message) {
        return new ConsumeAccountException("ACCOUNT_ALREADY_EXISTS", message, null);
    }

    /**
     * 创建失败
     */
    public static ConsumeAccountException createFailed(String message) {
        return new ConsumeAccountException("CREATE_FAILED", message, null);
    }

    /**
     * 更新失败
     */
    public static ConsumeAccountException updateFailed(String message) {
        return new ConsumeAccountException("UPDATE_FAILED", message, null);
    }

    /**
     * 扣减失败
     */
    public static ConsumeAccountException deductFailed(String message) {
        return new ConsumeAccountException("DEDUCT_FAILED", message, null);
    }

    /**
     * 退款失败
     */
    public static ConsumeAccountException refundFailed(String message) {
        return new ConsumeAccountException("REFUND_FAILED", message, null);
    }

    /**
     * 关闭失败
     */
    public static ConsumeAccountException closeFailed(String message) {
        return new ConsumeAccountException("CLOSE_FAILED", message, null);
    }

    /**
     * 查询失败
     */
    public static ConsumeAccountException queryFailed(String message) {
        return new ConsumeAccountException("QUERY_FAILED", message, null);
    }

    /**
     * 账户不存在
     */
    public static ConsumeAccountException accountNotFound(String message) {
        return new ConsumeAccountException("ACCOUNT_NOT_FOUND", message, null);
    }

    /**
     * 记录不存在
     */
    public static ConsumeAccountException recordNotFound(String message) {
        return new ConsumeAccountException("RECORD_NOT_FOUND", message, null);
    }

    /**
     * 取消失败
     */
    public static ConsumeAccountException cancelFailed(String message) {
        return new ConsumeAccountException("CANCEL_FAILED", message, null);
    }

    /**
     * 导出失败
     */
    public static ConsumeAccountException exportFailed(String message) {
        return new ConsumeAccountException("EXPORT_FAILED", message, null);
    }
}