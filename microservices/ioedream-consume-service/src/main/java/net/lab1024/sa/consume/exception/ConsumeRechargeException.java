package net.lab1024.sa.consume.exception;

/**
 * 消费充值异常
 * <p>
 * 统一处理充值相关的业务异常 继承自全局BusinessException 提供详细的错误分类和错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public class ConsumeRechargeException extends RuntimeException {

    /**
     * 异常代码枚举
     */
    public enum ErrorCode {
        RECORD_NOT_FOUND("RECORD_NOT_FOUND", "充值记录不存在"), TRANSACTION_DUPLICATE("TRANSACTION_DUPLICATE",
                "交易流水号重复"), THIRD_PARTY_DUPLICATE("THIRD_PARTY_DUPLICATE", "第三方交易号重复"), INVALID_AMOUNT("INVALID_AMOUNT",
                        "充值金额不正确"), INVALID_RECHARGE_WAY("INVALID_RECHARGE_WAY", "充值方式不正确"), INVALID_RECHARGE_CHANNEL(
                                "INVALID_RECHARGE_CHANNEL",
                                "充值渠道不正确"), INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "余额不足"), RECHARGE_FAILED(
                                        "RECHARGE_FAILED",
                                        "充值失败"), RECHARGE_TIMEOUT("RECHARGE_TIMEOUT", "充值超时"), THIRD_PARTY_ERROR(
                                                "THIRD_PARTY_ERROR",
                                                "第三方支付错误"), REVERSAL_NOT_ALLOWED("REVERSAL_NOT_ALLOWED",
                                                        "不允许冲正"), REVERSAL_FAILED("REVERSAL_FAILED",
                                                                "冲正失败"), USER_NOT_FOUND("USER_NOT_FOUND",
                                                                        "用户不存在"), ACCOUNT_FROZEN("ACCOUNT_FROZEN",
                                                                                "账户已冻结"), DEVICE_OFFLINE(
                                                                                        "DEVICE_OFFLINE",
                                                                                        "充值设备离线"), BATCH_RECHARGE_FAILED(
                                                                                                "BATCH_RECHARGE_FAILED",
                                                                                                "批量充值失败"), VALIDATION_FAILED(
                                                                                                        "VALIDATION_FAILED",
                                                                                                        "验证失败"), AUDIT_REJECTED(
                                                                                                                "AUDIT_REJECTED",
                                                                                                                "审核被拒绝"), ABNORMAL_RECHARGE(
                                                                                                                        "ABNORMAL_RECHARGE",
                                                                                                                        "异常充值"), AMOUNT_LIMIT_EXCEEDED(
                                                                                                                                "AMOUNT_LIMIT_EXCEEDED",
                                                                                                                                "超出充值限额"), FREQUENCY_LIMIT_EXCEEDED(
                                                                                                                                        "FREQUENCY_LIMIT_EXCEEDED",
                                                                                                                                        "超出充值频率限制"), TRANSACTION_NO_EMPTY(
                                                                                                                                                "TRANSACTION_NO_EMPTY",
                                                                                                                                                "交易流水号不能为空"), USER_ID_EMPTY(
                                                                                                                                                        "USER_ID_EMPTY",
                                                                                                                                                        "用户ID不能为空"), INVALID_STATUS(
                                                                                                                                                                "INVALID_STATUS",
                                                                                                                                                                "充值状态不正确"), OPERATOR_NOT_AUTHORIZED(
                                                                                                                                                                        "OPERATOR_NOT_AUTHORIZED",
                                                                                                                                                                        "操作员无权限"), BUSINESS_RULE_VIOLATION(
                                                                                                                                                                                "BUSINESS_RULE_VIOLATION",
                                                                                                                                                                                "违反业务规则"), OPERATION_NOT_SUPPORTED(
                                                                                                                                                                                        "OPERATION_NOT_SUPPORTED",
                                                                                                                                                                                        "不支持的操作"), DATABASE_ERROR(
                                                                                                                                                                                                "DATABASE_ERROR",
                                                                                                                                                                                                "数据库操作失败"), SYSTEM_ERROR(
                                                                                                                                                                                                        "SYSTEM_ERROR",
                                                                                                                                                                                                        "系统错误");

        private final String code;
        private final String defaultMessage;

        ErrorCode (String code, String defaultMessage) {
            this.code = code;
            this.defaultMessage = defaultMessage;
        }

        public String getCode () {
            return code;
        }

        public String getDefaultMessage () {
            return defaultMessage;
        }
    }

    private final ErrorCode errorCode;
    private final Object businessId;

    public ConsumeRechargeException (ErrorCode errorCode) {
        this (errorCode, errorCode.getDefaultMessage (), null);
    }

    public ConsumeRechargeException (ErrorCode errorCode, String message) {
        this (errorCode, message, null);
    }

    public ConsumeRechargeException (ErrorCode errorCode, String message, Throwable cause) {
        this (errorCode, message, null, cause);
    }

    public ConsumeRechargeException (ErrorCode errorCode, String message, Object businessId) {
        super (message);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    public ConsumeRechargeException (ErrorCode errorCode, String message, Object businessId, Throwable cause) {
        super (message, cause);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    /**
     * 获取异常代码
     */
    public ErrorCode getErrorCode () {
        return errorCode;
    }

    /**
     * 获取业务ID
     */
    public Object getBusinessId () {
        return businessId;
    }

    /**
     * 获取错误代码
     */
    public String getErrorCodeStr () {
        return errorCode.getCode ();
    }

    /**
     * 获取详细信息
     */
    public Object getDetails () {
        return businessId;
    }

    /**
     * 获取错误代码（兼容性方法）
     */
    public String getCode () {
        return errorCode.getCode ();
    }

    // ==================== 便捷工厂方法 ====================

    /**
     * 记录不存在异常
     */
    public static ConsumeRechargeException recordNotFound (Object recordId) {
        return new ConsumeRechargeException (ErrorCode.RECORD_NOT_FOUND, "充值记录不存在", recordId);
    }

    /**
     * 交易流水号重复异常
     */
    public static ConsumeRechargeException duplicateTransaction (String transactionNo) {
        return new ConsumeRechargeException (ErrorCode.TRANSACTION_DUPLICATE, "交易流水号已存在: " + transactionNo,
                transactionNo);
    }

    /**
     * 第三方交易号重复异常
     */
    public static ConsumeRechargeException duplicateThirdPartyNo (String thirdPartyNo) {
        return new ConsumeRechargeException (ErrorCode.THIRD_PARTY_DUPLICATE, "第三方交易号已存在: " + thirdPartyNo,
                thirdPartyNo);
    }

    /**
     * 交易重复异常（同时包含交易流水号和第三方交易号）
     */
    public static ConsumeRechargeException duplicateTransaction (String transactionNo, String thirdPartyNo) {
        StringBuilder message = new StringBuilder ("交易信息重复");
        if (transactionNo != null && !transactionNo.trim ().isEmpty ()) {
            message.append (", 交易流水号: ").append (transactionNo);
        }
        if (thirdPartyNo != null && !thirdPartyNo.trim ().isEmpty ()) {
            message.append (", 第三方交易号: ").append (thirdPartyNo);
        }
        return new ConsumeRechargeException (ErrorCode.TRANSACTION_DUPLICATE, message.toString ());
    }

    /**
     * 充值金额错误异常
     */
    public static ConsumeRechargeException invalidAmount (String message) {
        return new ConsumeRechargeException (ErrorCode.INVALID_AMOUNT, message);
    }

    /**
     * 充值方式错误异常
     */
    public static ConsumeRechargeException invalidRechargeWay (Integer rechargeWay) {
        return new ConsumeRechargeException (ErrorCode.INVALID_RECHARGE_WAY, "充值方式不正确: " + rechargeWay);
    }

    /**
     * 充值渠道错误异常
     */
    public static ConsumeRechargeException invalidRechargeChannel (Integer rechargeChannel) {
        return new ConsumeRechargeException (ErrorCode.INVALID_RECHARGE_CHANNEL, "充值渠道不正确: " + rechargeChannel);
    }

    /**
     * 余额不足异常
     */
    public static ConsumeRechargeException insufficientBalance (Object userId, Object balance) {
        return new ConsumeRechargeException (ErrorCode.INSUFFICIENT_BALANCE, "账户余额不足，当前余额: " + balance, userId);
    }

    /**
     * 充值失败异常
     */
    public static ConsumeRechargeException rechargeFailed (Object recordId, String reason) {
        return new ConsumeRechargeException (ErrorCode.RECHARGE_FAILED, "充值失败: " + reason, recordId);
    }

    /**
     * 充值超时异常
     */
    public static ConsumeRechargeException rechargeTimeout (Object transactionNo) {
        return new ConsumeRechargeException (ErrorCode.RECHARGE_TIMEOUT, "充值超时", transactionNo);
    }

    /**
     * 第三方支付错误异常
     */
    public static ConsumeRechargeException thirdPartyError (String thirdPartyNo, String errorDetail) {
        return new ConsumeRechargeException (ErrorCode.THIRD_PARTY_ERROR, "第三方支付错误: " + errorDetail, thirdPartyNo);
    }

    /**
     * 不允许冲正异常
     */
    public static ConsumeRechargeException reversalNotAllowed (Object recordId, String reason) {
        return new ConsumeRechargeException (ErrorCode.REVERSAL_NOT_ALLOWED, "不允许冲正: " + reason, recordId);
    }

    /**
     * 冲正失败异常
     */
    public static ConsumeRechargeException reversalFailed (Object recordId, String reason) {
        return new ConsumeRechargeException (ErrorCode.REVERSAL_FAILED, "冲正失败: " + reason, recordId);
    }

    /**
     * 用户不存在异常
     */
    public static ConsumeRechargeException userNotFound (Long userId) {
        return new ConsumeRechargeException (ErrorCode.USER_NOT_FOUND, "用户不存在", userId);
    }

    /**
     * 账户冻结异常
     */
    public static ConsumeRechargeException accountFrozen (Long userId) {
        return new ConsumeRechargeException (ErrorCode.ACCOUNT_FROZEN, "账户已冻结", userId);
    }

    /**
     * 设备离线异常
     */
    public static ConsumeRechargeException deviceOffline (Long deviceId) {
        return new ConsumeRechargeException (ErrorCode.DEVICE_OFFLINE, "充值设备离线", deviceId);
    }

    /**
     * 批量充值失败异常
     */
    public static ConsumeRechargeException batchRechargeFailed (String batchNo, String reason) {
        return new ConsumeRechargeException (ErrorCode.BATCH_RECHARGE_FAILED, "批量充值失败: " + reason, batchNo);
    }

    /**
     * 验证失败异常
     */
    public static ConsumeRechargeException validationFailed (java.util.List<String> errors) {
        String message = "验证失败: " + String.join (", ", errors);
        return new ConsumeRechargeException (ErrorCode.VALIDATION_FAILED, message);
    }

    /**
     * 审核被拒绝异常
     */
    public static ConsumeRechargeException auditRejected (Object recordId, String reason) {
        return new ConsumeRechargeException (ErrorCode.AUDIT_REJECTED, "审核被拒绝: " + reason, recordId);
    }

    /**
     * 异常充值异常
     */
    public static ConsumeRechargeException abnormalRecharge (Object recordId, String reason) {
        return new ConsumeRechargeException (ErrorCode.ABNORMAL_RECHARGE, "检测到异常充值: " + reason, recordId);
    }

    /**
     * 超出充值限额异常
     */
    public static ConsumeRechargeException amountLimitExceeded (Object userId, String limitType, String limit) {
        return new ConsumeRechargeException (ErrorCode.AMOUNT_LIMIT_EXCEEDED, "超出" + limitType + "充值限额: " + limit,
                userId);
    }

    /**
     * 超出充值频率限制异常
     */
    public static ConsumeRechargeException frequencyLimitExceeded (Object userId, String reason) {
        return new ConsumeRechargeException (ErrorCode.FREQUENCY_LIMIT_EXCEEDED, "超出充值频率限制: " + reason, userId);
    }

    /**
     * 交易流水号为空异常
     */
    public static ConsumeRechargeException transactionNoEmpty () {
        return new ConsumeRechargeException (ErrorCode.TRANSACTION_NO_EMPTY);
    }

    /**
     * 用户ID为空异常
     */
    public static ConsumeRechargeException userIdEmpty () {
        return new ConsumeRechargeException (ErrorCode.USER_ID_EMPTY);
    }

    /**
     * 充值状态不正确异常
     */
    public static ConsumeRechargeException invalidStatus (Integer status) {
        return new ConsumeRechargeException (ErrorCode.INVALID_STATUS, "充值状态不正确: " + status);
    }

    /**
     * 操作员无权限异常
     */
    public static ConsumeRechargeException operatorNotAuthorized (Long operatorId, String operation) {
        return new ConsumeRechargeException (ErrorCode.OPERATOR_NOT_AUTHORIZED, "操作员无权限执行操作: " + operation, operatorId);
    }

    /**
     * 违反业务规则异常
     */
    public static ConsumeRechargeException businessRuleViolation (String message) {
        return new ConsumeRechargeException (ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    /**
     * 操作不支持异常
     */
    public static ConsumeRechargeException operationNotSupported (String operation) {
        return new ConsumeRechargeException (ErrorCode.OPERATION_NOT_SUPPORTED, "不支持的操作: " + operation);
    }

    /**
     * 数据库操作失败异常
     */
    public static ConsumeRechargeException databaseError (String operation, String details) {
        return new ConsumeRechargeException (ErrorCode.DATABASE_ERROR, "数据库操作失败: " + operation + ", 详情: " + details);
    }

    /**
     * 系统错误异常
     */
    public static ConsumeRechargeException systemError (String message, Throwable cause) {
        return new ConsumeRechargeException (ErrorCode.SYSTEM_ERROR, "系统错误: " + message, null, cause);
    }

    /**
     * 创建数据库操作失败的异常
     */
    public static ConsumeRechargeException databaseError (String operation) {
        return new ConsumeRechargeException (ErrorCode.DATABASE_ERROR, "数据库操作失败: " + operation);
    }

    /**
     * 参数错误异常
     */
    public static ConsumeRechargeException invalidParameter (String message) {
        return new ConsumeRechargeException (ErrorCode.INVALID_AMOUNT, message);
    }
}
