package net.lab1024.sa.consume.exception;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.exception.BusinessException;

/**
 * 消费业务异常
 * <p>
 * 用于处理消费模块的业务异常情况
 * 继承全局BusinessException，确保与全局标准一致
 * 可预期的异常，用户可根据错误码进行相应处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumeBusinessException extends BusinessException {

    /**
     * 错误详情
     */
    private Object details;

    public ConsumeBusinessException(String code, String message) {
        super(code, message);
    }

    public ConsumeBusinessException(String code, String message, Object details) {
        super(code, message);
        this.details = details;
    }

    public ConsumeBusinessException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public ConsumeBusinessException(String code, String message, Object details, Throwable cause) {
        super(code, message, cause);
        this.details = details;
    }

    // ==================== 常用静态工厂方法 ====================

    /**
     * 账户不存在异常
     */
    public static ConsumeBusinessException accountNotFound(Long accountId) {
        return new ConsumeBusinessException("ACCOUNT_NOT_FOUND",
            "账户不存在: " + accountId, accountId);
    }

    /**
     * 账户状态异常
     */
    public static ConsumeBusinessException accountStatusInvalid(Integer status) {
        return new ConsumeBusinessException("ACCOUNT_STATUS_INVALID",
            "账户状态异常: " + status, status);
    }

    /**
     * 余额不足异常
     */
    public static ConsumeBusinessException insufficientBalance(String balance, String amount) {
        return new ConsumeBusinessException("INSUFFICIENT_BALANCE",
            "余额不足，当前余额: " + balance + "，消费金额: " + amount,
            Map.of("balance", balance, "amount", amount));
    }

    /**
     * 交易不存在异常
     */
    public static ConsumeBusinessException transactionNotFound(String transactionId) {
        return new ConsumeBusinessException("TRANSACTION_NOT_FOUND",
            "交易记录不存在: " + transactionId, transactionId);
    }

    /**
     * 交易状态异常
     */
    public static ConsumeBusinessException transactionStatusInvalid(String status) {
        return new ConsumeBusinessException("TRANSACTION_STATUS_INVALID",
            "交易状态异常: " + status, status);
    }

    /**
     * 设备离线异常
     */
    public static ConsumeBusinessException deviceOffline(String deviceId) {
        return new ConsumeBusinessException("DEVICE_OFFLINE",
            "设备离线: " + deviceId, deviceId);
    }

    /**
     * 权限不足异常
     */
    public static ConsumeBusinessException permissionDenied(String permission) {
        return new ConsumeBusinessException("PERMISSION_DENIED",
            "权限不足: " + permission, permission);
    }

    /**
     * 操作不支持异常
     */
    public static ConsumeBusinessException operationNotSupported(String operation) {
        return new ConsumeBusinessException("OPERATION_NOT_SUPPORTED",
            "操作不支持: " + operation, operation);
    }

    /**
     * 获取错误详情
     */
    public Object getDetails() {
        return details;
    }
}