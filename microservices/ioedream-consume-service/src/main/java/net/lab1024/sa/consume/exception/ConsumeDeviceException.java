package net.lab1024.sa.consume.exception;

/**
 * 消费设备异常
 * <p>
 * 统一处理设备相关的业务异常
 * 继承自全局BusinessException
 * 提供详细的错误分类和错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public class ConsumeDeviceException extends RuntimeException {

    /**
     * 异常代码枚举
     */
    public enum ErrorCode {
        DEVICE_NOT_FOUND("DEVICE_NOT_FOUND", "设备不存在"),
        DEVICE_CODE_DUPLICATE("DEVICE_CODE_DUPLICATE", "设备编码已存在"),
        DEVICE_IP_DUPLICATE("DEVICE_IP_DUPLICATE", "设备IP地址已存在"),
        DEVICE_MAC_DUPLICATE("DEVICE_MAC_DUPLICATE", "设备MAC地址已存在"),
        INVALID_DEVICE_TYPE("INVALID_DEVICE_TYPE", "设备类型不正确"),
        INVALID_DEVICE_STATUS("INVALID_DEVICE_STATUS", "设备状态不正确"),
        DEVICE_OFFLINE("DEVICE_OFFLINE", "设备离线"),
        DEVICE_FAULT("DEVICE_FAULT", "设备故障"),
        DEVICE_MAINTENANCE("DEVICE_MAINTENANCE", "设备维护中"),
        DEVICE_DISABLED("DEVICE_DISABLED", "设备已停用"),
        DEVICE_CONFIG_INVALID("DEVICE_CONFIG_INVALID", "设备配置无效"),
        DEVICE_COMMUNICATION_FAILED("DEVICE_COMMUNICATION_FAILED", "设备通信失败"),
        DEVICE_OPERATION_FAILED("DEVICE_OPERATION_FAILED", "设备操作失败"),
        DEVICE_NOT_OPERABLE("DEVICE_NOT_OPERABLE", "设备不可操作"),
        DEVICE_HEALTH_CHECK_FAILED("DEVICE_HEALTH_CHECK_FAILED", "设备健康检查失败"),
        BATCH_OPERATION_FAILED("BATCH_OPERATION_FAILED", "批量操作失败"),
        INVALID_STATUS_TRANSITION("INVALID_STATUS_TRANSITION", "状态转换不合法"),
        DEVICE_CODE_EMPTY("DEVICE_CODE_EMPTY", "设备编码不能为空"),
        DEVICE_NAME_EMPTY("DEVICE_NAME_EMPTY", "设备名称不能为空"),
        INVALID_PARAMETER("INVALID_PARAMETER", "参数错误"),
        INVALID_IP_ADDRESS("INVALID_IP_ADDRESS", "IP地址格式不正确"),
        INVALID_MAC_ADDRESS("INVALID_MAC_ADDRESS", "MAC地址格式不正确"),
        CONFIG_VALIDATION_FAILED("CONFIG_VALIDATION_FAILED", "配置验证失败"),
        DEVICE_TIMEOUT("DEVICE_TIMEOUT", "设备操作超时"),
        DEVICE_PERMISSION_DENIED("DEVICE_PERMISSION_DENIED", "设备操作权限不足"),
        DEVICE_CONFLICT("DEVICE_CONFLICT", "设备冲突"),
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

    public ConsumeDeviceException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null);
    }

    public ConsumeDeviceException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public ConsumeDeviceException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public ConsumeDeviceException(ErrorCode errorCode, String message, Object businessId) {
        super(message);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    public ConsumeDeviceException(ErrorCode errorCode, String message, Object businessId, Throwable cause) {
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
     * 设备不存在异常
     */
    public static ConsumeDeviceException notFound(Object deviceId) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_NOT_FOUND,
            "设备不存在", deviceId);
    }

    /**
     * 设备编码重复异常
     */
    public static ConsumeDeviceException duplicateCode(String deviceCode) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_CODE_DUPLICATE,
            "设备编码已存在: " + deviceCode, deviceCode);
    }

    /**
     * 设备IP重复异常
     */
    public static ConsumeDeviceException duplicateIp(String ipAddress) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_IP_DUPLICATE,
            "设备IP地址已存在: " + ipAddress, ipAddress);
    }

    /**
     * 设备MAC重复异常
     */
    public static ConsumeDeviceException duplicateMac(String macAddress) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_MAC_DUPLICATE,
            "设备MAC地址已存在: " + macAddress, macAddress);
    }

    /**
     * 设备类型不正确异常
     */
    public static ConsumeDeviceException invalidDeviceType(String message) {
        return new ConsumeDeviceException(ErrorCode.INVALID_DEVICE_TYPE, message);
    }

    /**
     * 设备状态不正确异常
     */
    public static ConsumeDeviceException invalidDeviceStatus(String message) {
        return new ConsumeDeviceException(ErrorCode.INVALID_DEVICE_STATUS, message);
    }

    /**
     * 设备离线异常
     */
    public static ConsumeDeviceException deviceOffline(Object deviceId) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_OFFLINE,
            "设备离线", deviceId);
    }

    /**
     * 设备故障异常
     */
    public static ConsumeDeviceException deviceFault(Object deviceId) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_FAULT,
            "设备故障", deviceId);
    }

    /**
     * 设备维护中异常
     */
    public static ConsumeDeviceException deviceMaintenance(Object deviceId) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_MAINTENANCE,
            "设备维护中", deviceId);
    }

    /**
     * 设备已停用异常
     */
    public static ConsumeDeviceException deviceDisabled(Object deviceId) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_DISABLED,
            "设备已停用", deviceId);
    }

    /**
     * 设备配置无效异常
     */
    public static ConsumeDeviceException configInvalid(Object deviceId, String message) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_CONFIG_INVALID,
            "设备配置无效: " + message, deviceId);
    }

    /**
     * 设备通信失败异常
     */
    public static ConsumeDeviceException communicationFailed(Object deviceId, String message) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_COMMUNICATION_FAILED,
            "设备通信失败: " + message, deviceId);
    }

    /**
     * 设备操作失败异常
     */
    public static ConsumeDeviceException operationFailed(Object deviceId, String operation, String reason) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_OPERATION_FAILED,
            "设备操作失败: " + operation + ", 原因: " + reason, deviceId);
    }

    /**
     * 设备不可操作异常
     */
    public static ConsumeDeviceException notOperable(Object deviceId, String operation) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_NOT_OPERABLE,
            "设备不可执行操作: " + operation, deviceId);
    }

    /**
     * 设备健康检查失败异常
     */
    public static ConsumeDeviceException healthCheckFailed(Object deviceId, String reason) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_HEALTH_CHECK_FAILED,
            "设备健康检查失败: " + reason, deviceId);
    }

    /**
     * 批量操作失败异常
     */
    public static ConsumeDeviceException batchOperationFailed(String operation, String reason) {
        return new ConsumeDeviceException(ErrorCode.BATCH_OPERATION_FAILED,
            "批量" + operation + "失败: " + reason);
    }

    /**
     * 状态转换不合法异常
     */
    public static ConsumeDeviceException invalidStatusTransition(Object deviceId, String transition) {
        return new ConsumeDeviceException(ErrorCode.INVALID_STATUS_TRANSITION,
            "状态转换不合法: " + transition, deviceId);
    }

    /**
     * 设备编码为空异常
     */
    public static ConsumeDeviceException codeEmpty() {
        return new ConsumeDeviceException(ErrorCode.DEVICE_CODE_EMPTY);
    }

    /**
     * 设备名称为空异常
     */
    public static ConsumeDeviceException nameEmpty() {
        return new ConsumeDeviceException(ErrorCode.DEVICE_NAME_EMPTY);
    }

    /**
     * IP地址格式不正确异常
     */
    public static ConsumeDeviceException invalidIpAddress(String ipAddress) {
        return new ConsumeDeviceException(ErrorCode.INVALID_IP_ADDRESS,
            "IP地址格式不正确: " + ipAddress);
    }

    /**
     * MAC地址格式不正确异常
     */
    public static ConsumeDeviceException invalidMacAddress(String macAddress) {
        return new ConsumeDeviceException(ErrorCode.INVALID_MAC_ADDRESS,
            "MAC地址格式不正确: " + macAddress);
    }

    /**
     * 配置验证失败异常
     */
    public static ConsumeDeviceException configValidationFailed(Object deviceId, java.util.List<String> errors) {
        String message = "配置验证失败: " + String.join(", ", errors);
        return new ConsumeDeviceException(ErrorCode.CONFIG_VALIDATION_FAILED, message, deviceId);
    }

    /**
     * 设备操作超时异常
     */
    public static ConsumeDeviceException deviceTimeout(Object deviceId, String operation) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_TIMEOUT,
            "设备操作超时: " + operation, deviceId);
    }

    /**
     * 设备操作权限不足异常
     */
    public static ConsumeDeviceException permissionDenied(Object deviceId, String operation) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_PERMISSION_DENIED,
            "设备操作权限不足: " + operation, deviceId);
    }

    /**
     * 设备冲突异常
     */
    public static ConsumeDeviceException deviceConflict(String message) {
        return new ConsumeDeviceException(ErrorCode.DEVICE_CONFLICT, message);
    }

    /**
     * 违反业务规则异常
     */
    public static ConsumeDeviceException businessRuleViolation(String message) {
        return new ConsumeDeviceException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    /**
     * 操作不支持异常
     */
    public static ConsumeDeviceException operationNotSupported(String operation) {
        return new ConsumeDeviceException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "不支持的操作: " + operation);
    }

    /**
     * 参数错误异常
     */
    public static ConsumeDeviceException invalidParameter(String message) {
        return new ConsumeDeviceException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 创建验证失败的异常
     */
    public static ConsumeDeviceException validationFailed(java.util.List<String> errors) {
        String message = "验证失败: " + String.join(", ", errors);
        return new ConsumeDeviceException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 创建数据库操作失败的异常
     */
    public static ConsumeDeviceException databaseError(String operation, String details) {
        return new ConsumeDeviceException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "数据库操作失败: " + operation + ", 详情: " + details);
    }
}