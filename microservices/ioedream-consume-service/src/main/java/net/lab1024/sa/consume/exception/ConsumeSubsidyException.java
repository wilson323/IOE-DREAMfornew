package net.lab1024.sa.consume.exception;

/**
 * 消费补贴异常
 * <p>
 * 统一处理补贴相关的业务异常
 * 继承自全局BusinessException
 * 提供详细的错误分类和错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public class ConsumeSubsidyException extends RuntimeException {

    /**
     * 异常代码枚举
     */
    public enum ErrorCode {
        SUBSIDY_NOT_FOUND("SUBSIDY_NOT_FOUND", "补贴不存在"),
        SUBSIDY_CODE_DUPLICATE("SUBSIDY_CODE_DUPLICATE", "补贴编码已存在"),
        INVALID_SUBSIDY_TYPE("INVALID_SUBSIDY_TYPE", "补贴类型不正确"),
        INVALID_SUBSIDY_PERIOD("INVALID_SUBSIDY_PERIOD", "补贴周期不正确"),
        INVALID_AMOUNT("INVALID_AMOUNT", "金额设置不正确"),
        INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "补贴余额不足"),
        DAILY_LIMIT_EXCEEDED("DAILY_LIMIT_EXCEEDED", "超出每日使用限额"),
        SUBSIDY_EXPIRED("SUBSIDY_EXPIRED", "补贴已过期"),
        SUBSIDY_NOT_EFFECTIVE("SUBSIDY_NOT_EFFECTIVE", "补贴未生效"),
        SUBSIDY_USED("SUBSIDY_USED", "补贴已使用"),
        SUBSIDY_VOID("SUBSIDY_VOID", "补贴已作废"),
        USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在"),
        DEPARTMENT_NOT_FOUND("DEPARTMENT_NOT_FOUND", "部门不存在"),
        INVALID_DATE_RANGE("INVALID_DATE_RANGE", "日期范围不正确"),
        USAGE_LIMIT_VIOLATION("USAGE_LIMIT_VIOLATION", "违反使用限制"),
        CONFLICTING_SUBSIDY("CONFLICTING_SUBSIDY", "存在冲突的补贴"),
        AUTO_RENEW_FAILED("AUTO_RENEW_FAILED", "自动续期失败"),
        SUBSIDY_CODE_EMPTY("SUBSIDY_CODE_EMPTY", "补贴编码不能为空"),
        SUBSIDY_NAME_EMPTY("SUBSIDY_NAME_EMPTY", "补贴名称不能为空"),
        INVALID_PARAMETER("INVALID_PARAMETER", "参数错误"),
        APPROVAL_REQUIRED("APPROVAL_REQUIRED", "需要审批"),
        APPROVAL_REJECTED("APPROVAL_REJECTED", "审批被拒绝"),
        BATCH_OPERATION_FAILED("BATCH_OPERATION_FAILED", "批量操作失败"),
        BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "违反业务规则"),
        OPERATION_NOT_SUPPORTED("OPERATION_NOT_SUPPORTED", "不支持的操作"),
        USAGE_TIME_RESTRICTION("USAGE_TIME_RESTRICTION", "使用时间限制");

        private final String code;
        private final String defaultMessage;

        ErrorCode(String code, String defaultMessage) {
            this.code = code;
            this.defaultMessage = defaultMessage;
        }

        public String getCode() {
            return code;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    private final ErrorCode errorCode;
    private final Object businessId;

    public ConsumeSubsidyException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null);
    }

    public ConsumeSubsidyException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public ConsumeSubsidyException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public ConsumeSubsidyException(ErrorCode errorCode, String message, Object businessId) {
        super(message);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    public ConsumeSubsidyException(ErrorCode errorCode, String message, Object businessId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    /**
     * 获取异常代码
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 获取业务ID
     */
    public Object getBusinessId() {
        return businessId;
    }

    /**
     * 获取错误代码
     */
    public String getErrorCodeStr() {
        return errorCode.getCode();
    }

    /**
     * 获取详细信息
     */
    public Object getDetails() {
        return businessId;
    }

    /**
     * 获取错误代码（兼容性方法）
     */
    public String getCode() {
        return errorCode.getCode();
    }

    // ==================== 便捷工厂方法 ====================

    /**
     * 补贴不存在异常
     */
    public static ConsumeSubsidyException notFound(Object subsidyId) {
        return new ConsumeSubsidyException(ErrorCode.SUBSIDY_NOT_FOUND,
            "补贴不存在", subsidyId);
    }

    /**
     * 补贴编码重复异常
     */
    public static ConsumeSubsidyException duplicateCode(String subsidyCode) {
        return new ConsumeSubsidyException(ErrorCode.SUBSIDY_CODE_DUPLICATE,
            "补贴编码已存在: " + subsidyCode, subsidyCode);
    }

    /**
     * 补贴类型不正确异常
     */
    public static ConsumeSubsidyException invalidSubsidyType(String message) {
        return new ConsumeSubsidyException(ErrorCode.INVALID_SUBSIDY_TYPE, message);
    }

    /**
     * 补贴周期不正确异常
     */
    public static ConsumeSubsidyException invalidSubsidyPeriod(String message) {
        return new ConsumeSubsidyException(ErrorCode.INVALID_SUBSIDY_PERIOD, message);
    }

    /**
     * 金额设置错误异常
     */
    public static ConsumeSubsidyException invalidAmount(String message) {
        return new ConsumeSubsidyException(ErrorCode.INVALID_AMOUNT, message);
    }

    /**
     * 补贴余额不足异常
     */
    public static ConsumeSubsidyException insufficientBalance(Object subsidyId, Object balance) {
        return new ConsumeSubsidyException(ErrorCode.INSUFFICIENT_BALANCE,
            "补贴余额不足，当前余额: " + balance, subsidyId);
    }

    /**
     * 超出每日限额异常
     */
    public static ConsumeSubsidyException dailyLimitExceeded(Object subsidyId, String message) {
        return new ConsumeSubsidyException(ErrorCode.DAILY_LIMIT_EXCEEDED,
            "超出每日使用限额: " + message, subsidyId);
    }

    /**
     * 补贴已过期异常
     */
    public static ConsumeSubsidyException subsidyExpired(Object subsidyId) {
        return new ConsumeSubsidyException(ErrorCode.SUBSIDY_EXPIRED,
            "补贴已过期", subsidyId);
    }

    /**
     * 补贴未生效异常
     */
    public static ConsumeSubsidyException subsidyNotEffective(Object subsidyId) {
        return new ConsumeSubsidyException(ErrorCode.SUBSIDY_NOT_EFFECTIVE,
            "补贴未生效", subsidyId);
    }

    /**
     * 补贴已使用异常
     */
    public static ConsumeSubsidyException subsidyUsed(Object subsidyId) {
        return new ConsumeSubsidyException(ErrorCode.SUBSIDY_USED,
            "补贴已使用", subsidyId);
    }

    /**
     * 补贴已作废异常
     */
    public static ConsumeSubsidyException subsidyVoid(Object subsidyId) {
        return new ConsumeSubsidyException(ErrorCode.SUBSIDY_VOID,
            "补贴已作废", subsidyId);
    }

    /**
     * 用户不存在异常
     */
    public static ConsumeSubsidyException userNotFound(Long userId) {
        return new ConsumeSubsidyException(ErrorCode.USER_NOT_FOUND,
            "用户不存在", userId);
    }

    /**
     * 部门不存在异常
     */
    public static ConsumeSubsidyException departmentNotFound(Long departmentId) {
        return new ConsumeSubsidyException(ErrorCode.DEPARTMENT_NOT_FOUND,
            "部门不存在", departmentId);
    }

    /**
     * 日期范围不正确异常
     */
    public static ConsumeSubsidyException invalidDateRange(String message) {
        return new ConsumeSubsidyException(ErrorCode.INVALID_DATE_RANGE, message);
    }

    /**
     * 违反使用限制异常
     */
    public static ConsumeSubsidyException usageLimitViolation(Object subsidyId, String message) {
        return new ConsumeSubsidyException(ErrorCode.USAGE_LIMIT_VIOLATION,
            "违反使用限制: " + message, subsidyId);
    }

    /**
     * 存在冲突补贴异常
     */
    public static ConsumeSubsidyException conflictingSubsidy(String message) {
        return new ConsumeSubsidyException(ErrorCode.CONFLICTING_SUBSIDY, message);
    }

    /**
     * 自动续期失败异常
     */
    public static ConsumeSubsidyException autoRenewFailed(Object subsidyId, String reason) {
        return new ConsumeSubsidyException(ErrorCode.AUTO_RENEW_FAILED,
            "自动续期失败: " + reason, subsidyId);
    }

    /**
     * 补贴编码为空异常
     */
    public static ConsumeSubsidyException codeEmpty() {
        return new ConsumeSubsidyException(ErrorCode.SUBSIDY_CODE_EMPTY);
    }

    /**
     * 补贴名称为空异常
     */
    public static ConsumeSubsidyException nameEmpty() {
        return new ConsumeSubsidyException(ErrorCode.SUBSIDY_NAME_EMPTY);
    }

    /**
     * 参数错误异常
     */
    public static ConsumeSubsidyException invalidParameter(String message) {
        return new ConsumeSubsidyException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 需要审批异常
     */
    public static ConsumeSubsidyException approvalRequired(Object subsidyId) {
        return new ConsumeSubsidyException(ErrorCode.APPROVAL_REQUIRED,
            "补贴需要审批", subsidyId);
    }

    /**
     * 审批被拒绝异常
     */
    public static ConsumeSubsidyException approvalRejected(Object subsidyId, String reason) {
        return new ConsumeSubsidyException(ErrorCode.APPROVAL_REJECTED,
            "补贴审批被拒绝: " + reason, subsidyId);
    }

    /**
     * 批量操作失败异常
     */
    public static ConsumeSubsidyException batchOperationFailed(String operation, String reason) {
        return new ConsumeSubsidyException(ErrorCode.BATCH_OPERATION_FAILED,
            "批量" + operation + "失败: " + reason);
    }

    /**
     * 违反业务规则异常
     */
    public static ConsumeSubsidyException businessRuleViolation(String message) {
        return new ConsumeSubsidyException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    /**
     * 操作不支持异常
     */
    public static ConsumeSubsidyException operationNotSupported(String operation) {
        return new ConsumeSubsidyException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "不支持的操作: " + operation);
    }

    /**
     * 使用时间限制异常
     */
    public static ConsumeSubsidyException usageTimeRestriction(Object subsidyId, String timeRestriction) {
        return new ConsumeSubsidyException(ErrorCode.USAGE_TIME_RESTRICTION,
            "使用时间受限: " + timeRestriction, subsidyId);
    }

    /**
     * 创建验证失败的异常
     */
    public static ConsumeSubsidyException validationFailed(java.util.List<String> errors) {
        String message = "验证失败: " + String.join(", ", errors);
        return new ConsumeSubsidyException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 创建数据库操作失败的异常
     */
    public static ConsumeSubsidyException databaseError(String operation, String details) {
        return new ConsumeSubsidyException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "数据库操作失败: " + operation + ", 详情: " + details);
    }
}