package net.lab1024.sa.consume.exception;

/**
 * 消费餐次分类异常
 * <p>
 * 统一处理餐次分类相关的业务异常
 * 继承自全局BusinessException
 * 提供详细的错误分类和错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public class ConsumeMealCategoryException extends RuntimeException {

    /**
     * 异常代码枚举
     */
    public enum ErrorCode {
        CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "分类不存在"),
        CATEGORY_CODE_DUPLICATE("CATEGORY_CODE_DUPLICATE", "分类编码已存在"),
        INVALID_CATEGORY_LEVEL("INVALID_CATEGORY_LEVEL", "分类层级不合法"),
        PARENT_CATEGORY_NOT_FOUND("PARENT_CATEGORY_NOT_FOUND", "父分类不存在"),
        CATEGORY_HAS_CHILDREN("CATEGORY_HAS_CHILDREN", "分类下存在子分类，无法删除"),
        SYSTEM_CATEGORY_NOT_DELETEABLE("SYSTEM_CATEGORY_NOT_DELETEABLE", "系统预设分类不能删除"),
        CATEGORY_USED_IN_TRANSACTION("CATEGORY_USED_IN_TRANSACTION", "分类已被使用，无法删除"),
        INVALID_TIME_PERIOD("INVALID_TIME_PERIOD", "时间段格式不正确"),
        INVALID_AMOUNT_LIMIT("INVALID_AMOUNT_LIMIT", "金额限制设置不正确"),
        CATEGORY_CODE_EMPTY("CATEGORY_CODE_EMPTY", "分类编码不能为空"),
        CATEGORY_NAME_EMPTY("CATEGORY_NAME_EMPTY", "分类名称不能为空"),
        INVALID_PARAMETER("INVALID_PARAMETER", "参数错误"),
        DUPLICATE_NAME("DUPLICATE_NAME", "分类名称已存在"),
        CATEGORY_LEVEL_EXCEEDED("CATEGORY_LEVEL_EXCEEDED", "分类层级超出限制"),
        INVALID_SORT_ORDER("INVALID_SORT_ORDER", "排序号设置不正确"),
        BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "违反业务规则"),
        CATEGORY_RELATION_ERROR("CATEGORY_RELATION_ERROR", "分类关系错误"),
        OPERATION_NOT_SUPPORTED("OPERATION_NOT_SUPPORTED", "不支持的操作");

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

    public ConsumeMealCategoryException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.businessId = null;
    }

    public ConsumeMealCategoryException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.businessId = null;
    }

    public ConsumeMealCategoryException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.businessId = null;
    }

    public ConsumeMealCategoryException(ErrorCode errorCode, String message, Object businessId) {
        super(message);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    public ConsumeMealCategoryException(ErrorCode errorCode, String message, Object businessId, Throwable cause) {
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
     * 分类不存在异常
     */
    public static ConsumeMealCategoryException notFound(Object categoryId) {
        return new ConsumeMealCategoryException(ErrorCode.CATEGORY_NOT_FOUND,
            "分类不存在", categoryId);
    }

    /**
     * 分类编码重复异常
     */
    public static ConsumeMealCategoryException duplicateCode(String categoryCode) {
        return new ConsumeMealCategoryException(ErrorCode.CATEGORY_CODE_DUPLICATE,
            "分类编码已存在: " + categoryCode, categoryCode);
    }

    /**
     * 分类名称重复异常
     */
    public static ConsumeMealCategoryException duplicateName(String categoryName) {
        return new ConsumeMealCategoryException(ErrorCode.DUPLICATE_NAME,
            "分类名称已存在: " + categoryName, categoryName);
    }

    /**
     * 分类层级不合法异常
     */
    public static ConsumeMealCategoryException invalidCategoryLevel(String message) {
        return new ConsumeMealCategoryException(ErrorCode.INVALID_CATEGORY_LEVEL, message);
    }

    /**
     * 父分类不存在异常
     */
    public static ConsumeMealCategoryException parentNotFound(Long parentId) {
        return new ConsumeMealCategoryException(ErrorCode.PARENT_CATEGORY_NOT_FOUND,
            "父分类不存在", parentId);
    }

    /**
     * 分类有子分类异常
     */
    public static ConsumeMealCategoryException hasChildren(Long categoryId, int childrenCount) {
        return new ConsumeMealCategoryException(ErrorCode.CATEGORY_HAS_CHILDREN,
            "分类下存在" + childrenCount + "个子分类，无法删除", categoryId);
    }

    /**
     * 系统预设分类不能删除异常
     */
    public static ConsumeMealCategoryException systemCategoryNotDeletable(Long categoryId) {
        return new ConsumeMealCategoryException(ErrorCode.SYSTEM_CATEGORY_NOT_DELETEABLE,
            "系统预设分类不能删除", categoryId);
    }

    /**
     * 分类已被使用异常
     */
    public static ConsumeMealCategoryException usedInTransaction(Long categoryId, java.util.Map<String, Long> relatedRecords) {
        String message = "分类已被使用，无法删除。关联记录数: " + relatedRecords;
        return new ConsumeMealCategoryException(ErrorCode.CATEGORY_USED_IN_TRANSACTION,
            message, categoryId);
    }

    /**
     * 时间段格式错误异常
     */
    public static ConsumeMealCategoryException invalidTimePeriod(String timePeriods) {
        return new ConsumeMealCategoryException(ErrorCode.INVALID_TIME_PERIOD,
            "时间段格式不正确: " + timePeriods, timePeriods);
    }

    /**
     * 金额限制设置错误异常
     */
    public static ConsumeMealCategoryException invalidAmountLimit(String message) {
        return new ConsumeMealCategoryException(ErrorCode.INVALID_AMOUNT_LIMIT, message);
    }

    /**
     * 分类编码为空异常
     */
    public static ConsumeMealCategoryException codeEmpty() {
        return new ConsumeMealCategoryException(ErrorCode.CATEGORY_CODE_EMPTY);
    }

    /**
     * 分类名称为空异常
     */
    public static ConsumeMealCategoryException nameEmpty() {
        return new ConsumeMealCategoryException(ErrorCode.CATEGORY_NAME_EMPTY);
    }

    /**
     * 参数错误异常
     */
    public static ConsumeMealCategoryException invalidParameter(String message) {
        return new ConsumeMealCategoryException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 分类层级超出限制异常
     */
    public static ConsumeMealCategoryException levelExceeded(int level) {
        return new ConsumeMealCategoryException(ErrorCode.CATEGORY_LEVEL_EXCEEDED,
            "分类层级超出限制，最大层级为3，当前层级: " + level);
    }

    /**
     * 排序号错误异常
     */
    public static ConsumeMealCategoryException invalidSortOrder(String message) {
        return new ConsumeMealCategoryException(ErrorCode.INVALID_SORT_ORDER, message);
    }

    /**
     * 违反业务规则异常
     */
    public static ConsumeMealCategoryException businessRuleViolation(String message) {
        return new ConsumeMealCategoryException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    /**
     * 分类关系错误异常
     */
    public static ConsumeMealCategoryException relationError(String message) {
        return new ConsumeMealCategoryException(ErrorCode.CATEGORY_RELATION_ERROR, message);
    }

    /**
     * 操作不支持异常
     */
    public static ConsumeMealCategoryException operationNotSupported(String operation) {
        return new ConsumeMealCategoryException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "不支持的操作: " + operation);
    }

    /**
     * 创建验证失败的异常
     */
    public static ConsumeMealCategoryException validationFailed(java.util.List<String> errors) {
        String message = "验证失败: " + String.join(", ", errors);
        return new ConsumeMealCategoryException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 创建数据库操作失败的异常
     */
    public static ConsumeMealCategoryException databaseError(String operation, String details) {
        return new ConsumeMealCategoryException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "数据库操作失败: " + operation + ", 详情: " + details);
    }
}