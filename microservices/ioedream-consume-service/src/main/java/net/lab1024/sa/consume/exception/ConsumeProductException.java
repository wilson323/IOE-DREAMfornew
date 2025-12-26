package net.lab1024.sa.consume.exception;

/**
 * 消费产品异常
 * <p>
 * 统一处理产品相关的业务异常
 * 继承自全局BusinessException
 * 提供详细的错误分类和错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public class ConsumeProductException extends RuntimeException {

    /**
     * 异常代码枚举
     */
    public enum ErrorCode {
        PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "产品不存在"),
        PRODUCT_CODE_DUPLICATE("PRODUCT_CODE_DUPLICATE", "产品编码已存在"),
        PRODUCT_NAME_DUPLICATE("PRODUCT_NAME_DUPLICATE", "产品名称已存在"),
        INVALID_PRODUCT_TYPE("INVALID_PRODUCT_TYPE", "产品类型不正确"),
        INVALID_PRICE("INVALID_PRICE", "价格设置不正确"),
        INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "库存不足"),
        STOCK_UPDATE_FAILED("STOCK_UPDATE_FAILED", "库存更新失败"),
        PRODUCT_OFF_SALE("PRODUCT_OFF_SALE", "产品已下架"),
        PRODUCT_DISCONTINUED("PRODUCT_DISCONTINUED", "产品已停产"),
        CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "产品分类不存在"),
        INVALID_TIME_PERIOD("INVALID_TIME_PERIOD", "销售时间段格式不正确"),
        PRODUCT_USED_IN_TRANSACTION("PRODUCT_USED_IN_TRANSACTION", "产品已被使用，无法删除"),
        PRODUCT_CODE_EMPTY("PRODUCT_CODE_EMPTY", "产品编码不能为空"),
        PRODUCT_NAME_EMPTY("PRODUCT_NAME_EMPTY", "产品名称不能为空"),
        INVALID_PARAMETER("INVALID_PARAMETER", "参数错误"),
        DISCOUNT_NOT_ALLOWED("DISCOUNT_NOT_ALLOWED", "产品不允许折扣"),
        DISCOUNT_RATE_EXCEEDED("DISCOUNT_RATE_EXCEEDED", "折扣比例超出限制"),
        PRICE_VIOLATION("PRICE_VIOLATION", "价格设置违反规则"),
        STOCK_VIOLATION("STOCK_VIOLATION", "库存设置违反规则"),
        BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "违反业务规则"),
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

    public ConsumeProductException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null);
    }

    public ConsumeProductException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public ConsumeProductException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public ConsumeProductException(ErrorCode errorCode, String message, Object businessId) {
        super(message);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    public ConsumeProductException(ErrorCode errorCode, String message, Object businessId, Throwable cause) {
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
     * 获取错误代码（兼容性方法）
     */
    public String getCode() {
        return errorCode.getCode();
    }

    /**
     * 获取详细信息
     */
    public Object getDetails() {
        return businessId;
    }

    // ==================== 兼容性构造函数 ====================

    /**
     * 兼容性构造函数（用于快速修复）
     * @param code 错误代码
     * @param message 错误信息
     */
    public ConsumeProductException(String code, String message) {
        super(message);
        // 使用通用错误码
        this.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
        this.businessId = code;
    }

    /**
     * 兼容性构造函数（仅消息）
     * @param message 错误信息
     */
    public ConsumeProductException(String message) {
        this("UNKNOWN_ERROR", message);
    }

    // ==================== 便捷工厂方法 ====================

    /**
     * 产品不存在异常
     */
    public static ConsumeProductException notFound(Object productId) {
        return new ConsumeProductException(ErrorCode.PRODUCT_NOT_FOUND,
            "产品不存在", productId);
    }

    /**
     * 产品编码重复异常
     */
    public static ConsumeProductException duplicateCode(String productCode) {
        return new ConsumeProductException(ErrorCode.PRODUCT_CODE_DUPLICATE,
            "产品编码已存在: " + productCode, productCode);
    }

    /**
     * 产品名称重复异常
     */
    public static ConsumeProductException duplicateName(String productName) {
        return new ConsumeProductException(ErrorCode.PRODUCT_NAME_DUPLICATE,
            "产品名称已存在: " + productName, productName);
    }

    /**
     * 产品类型不正确异常
     */
    public static ConsumeProductException invalidProductType(String message) {
        return new ConsumeProductException(ErrorCode.INVALID_PRODUCT_TYPE, message);
    }

    /**
     * 价格设置错误异常
     */
    public static ConsumeProductException invalidPrice(String message) {
        return new ConsumeProductException(ErrorCode.INVALID_PRICE, message);
    }

    /**
     * 库存不足异常
     */
    public static ConsumeProductException insufficientStock(Object productId, Object currentStock) {
        return new ConsumeProductException(ErrorCode.INSUFFICIENT_STOCK,
            "库存不足，当前库存: " + currentStock, productId);
    }

    /**
     * 库存更新失败异常
     */
    public static ConsumeProductException stockUpdateFailed(Object productId, String reason) {
        return new ConsumeProductException(ErrorCode.STOCK_UPDATE_FAILED,
            "库存更新失败: " + reason, productId);
    }

    /**
     * 产品已下架异常
     */
    public static ConsumeProductException productOffSale(Object productId) {
        return new ConsumeProductException(ErrorCode.PRODUCT_OFF_SALE,
            "产品已下架", productId);
    }

    /**
     * 产品已停产异常
     */
    public static ConsumeProductException productDiscontinued(Object productId) {
        return new ConsumeProductException(ErrorCode.PRODUCT_DISCONTINUED,
            "产品已停产", productId);
    }

    /**
     * 产品分类不存在异常
     */
    public static ConsumeProductException categoryNotFound(Long categoryId) {
        return new ConsumeProductException(ErrorCode.CATEGORY_NOT_FOUND,
            "产品分类不存在", categoryId);
    }

    /**
     * 时间段格式错误异常
     */
    public static ConsumeProductException invalidTimePeriod(String timePeriods) {
        return new ConsumeProductException(ErrorCode.INVALID_TIME_PERIOD,
            "销售时间段格式不正确: " + timePeriods, timePeriods);
    }

    /**
     * 产品已被使用异常
     */
    public static ConsumeProductException usedInTransaction(Object productId, java.util.Map<String, Long> relatedRecords) {
        String message = "产品已被使用，无法删除。关联记录数: " + relatedRecords;
        return new ConsumeProductException(ErrorCode.PRODUCT_USED_IN_TRANSACTION,
            message, productId);
    }

    /**
     * 产品编码为空异常
     */
    public static ConsumeProductException codeEmpty() {
        return new ConsumeProductException(ErrorCode.PRODUCT_CODE_EMPTY);
    }

    /**
     * 产品名称为空异常
     */
    public static ConsumeProductException nameEmpty() {
        return new ConsumeProductException(ErrorCode.PRODUCT_NAME_EMPTY);
    }

    /**
     * 参数错误异常
     */
    public static ConsumeProductException invalidParameter(String message) {
        return new ConsumeProductException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 不允许折扣异常
     */
    public static ConsumeProductException discountNotAllowed(Object productId) {
        return new ConsumeProductException(ErrorCode.DISCOUNT_NOT_ALLOWED,
            "产品不允许折扣", productId);
    }

    /**
     * 折扣比例超出限制异常
     */
    public static ConsumeProductException discountRateExceeded(Object productId, String message) {
        return new ConsumeProductException(ErrorCode.DISCOUNT_RATE_EXCEEDED,
            "折扣比例超出限制: " + message, productId);
    }

    /**
     * 价格违反规则异常
     */
    public static ConsumeProductException priceViolation(String message) {
        return new ConsumeProductException(ErrorCode.PRICE_VIOLATION, message);
    }

    /**
     * 库存违反规则异常
     */
    public static ConsumeProductException stockViolation(String message) {
        return new ConsumeProductException(ErrorCode.STOCK_VIOLATION, message);
    }

    /**
     * 违反业务规则异常
     */
    public static ConsumeProductException businessRuleViolation(String message) {
        return new ConsumeProductException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    /**
     * 操作不支持异常
     */
    public static ConsumeProductException operationNotSupported(String operation) {
        return new ConsumeProductException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "不支持的操作: " + operation);
    }

    /**
     * 创建验证失败的异常
     */
    public static ConsumeProductException validationFailed(java.util.List<String> errors) {
        String message = "验证失败: " + String.join(", ", errors);
        return new ConsumeProductException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 创建数据库操作失败的异常
     */
    public static ConsumeProductException databaseError(String operation, String details) {
        return new ConsumeProductException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "数据库操作失败: " + operation + ", 详情: " + details);
    }
}