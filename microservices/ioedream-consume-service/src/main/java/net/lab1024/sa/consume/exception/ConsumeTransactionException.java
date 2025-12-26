package net.lab1024.sa.consume.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消费交易异常
 * <p>
 * 专门用于处理交易相关的异常情况
 * 继承ConsumeBusinessException，确保与全局标准一致
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumeTransactionException extends ConsumeBusinessException {

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 设备ID
     */
    private String deviceId;

    public ConsumeTransactionException(String code, String message, String transactionId) {
        super(code, message);
        this.transactionId = transactionId;
    }

    public ConsumeTransactionException(String code, String message, String transactionId, Long accountId) {
        super(code, message);
        this.transactionId = transactionId;
        this.accountId = accountId;
    }

    public ConsumeTransactionException(String code, String message, String transactionId, Long accountId, String deviceId) {
        super(code, message);
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.deviceId = deviceId;
    }

    // ==================== 常用静态工厂方法 ====================

    /**
     * 交易不存在
     */
    public static ConsumeTransactionException notFound(String transactionId) {
        return new ConsumeTransactionException("TRANSACTION_NOT_FOUND",
            "交易记录不存在: " + transactionId, transactionId);
    }

    /**
     * 交易已存在
     */
    public static ConsumeTransactionException alreadyExists(String transactionId) {
        return new ConsumeTransactionException("TRANSACTION_ALREADY_EXISTS",
            "交易记录已存在: " + transactionId, transactionId);
    }

    /**
     * 交易状态异常
     */
    public static ConsumeTransactionException invalidStatus(String transactionId, String status) {
        ConsumeTransactionException exception = new ConsumeTransactionException("TRANSACTION_STATUS_INVALID",
            "交易状态异常: " + transactionId + "，状态: " + status, transactionId);
        exception.setDetails(java.util.Map.of("transactionId", transactionId, "status", status));
        return exception;
    }

    /**
     * 交易执行失败
     */
    public static ConsumeTransactionException executeFailed(String transactionId, String reason) {
        ConsumeTransactionException exception = new ConsumeTransactionException("TRANSACTION_EXECUTE_FAILED",
            "交易执行失败: " + transactionId + "，原因: " + reason, transactionId);
        exception.setDetails(java.util.Map.of("transactionId", transactionId, "reason", reason));
        return exception;
    }

    /**
     * 交易撤销失败
     */
    public static ConsumeTransactionException cancelFailed(String transactionId, String reason) {
        ConsumeTransactionException exception = new ConsumeTransactionException("TRANSACTION_CANCEL_FAILED",
            "交易撤销失败: " + transactionId + "，原因: " + reason, transactionId);
        exception.setDetails(java.util.Map.of("transactionId", transactionId, "reason", reason));
        return exception;
    }

    /**
     * 设备离线
     */
    public static ConsumeTransactionException deviceOffline(String deviceId, String transactionId) {
        ConsumeTransactionException exception = new ConsumeTransactionException("DEVICE_OFFLINE",
            "设备离线无法执行交易: " + deviceId, transactionId, null, deviceId);
        exception.setDetails(java.util.Map.of("deviceId", deviceId, "transactionId", transactionId));
        return exception;
    }

    /**
     * 交易重复
     */
    public static ConsumeTransactionException duplicate(String transactionId) {
        ConsumeTransactionException exception = new ConsumeTransactionException("TRANSACTION_DUPLICATE",
            "检测到重复交易: " + transactionId, transactionId);
        exception.setDetails(java.util.Map.of("transactionId", transactionId));
        return exception;
    }

    /**
     * 交易超时
     */
    public static ConsumeTransactionException timeout(String transactionId, String reason) {
        ConsumeTransactionException exception = new ConsumeTransactionException("TRANSACTION_TIMEOUT",
            "交易超时: " + transactionId + "，原因: " + reason, transactionId);
        exception.setDetails(java.util.Map.of("transactionId", transactionId, "reason", reason));
        return exception;
    }

    /**
     * 交易金额异常
     */
    public static ConsumeTransactionException invalidAmount(String transactionId, String amount) {
        ConsumeTransactionException exception = new ConsumeTransactionException("INVALID_TRANSACTION_AMOUNT",
            "交易金额异常: " + transactionId + "，金额: " + amount, transactionId);
        exception.setDetails(java.util.Map.of("transactionId", transactionId, "amount", amount));
        return exception;
    }

    /**
     * 金额无效（兼容性方法）
     */
    public static ConsumeTransactionException invalidAmount(String amount) {
        ConsumeTransactionException exception = new ConsumeTransactionException("INVALID_AMOUNT",
            "金额无效: " + amount, null);
        exception.setDetails(java.util.Map.of("amount", amount));
        return exception;
    }
}