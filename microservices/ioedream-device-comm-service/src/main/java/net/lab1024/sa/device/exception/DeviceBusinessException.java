package net.lab1024.sa.device.exception;

/**
 * 设备业务异常
 * <p>
 * 统一处理设备相关的业务异常
 * 继承自全局异常体系
 * 提供详细的错误分类和错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
public class DeviceBusinessException extends RuntimeException {

    /**
     * 异常代码枚举
     */
    public enum ErrorCode {
        // 设备基础管理异常 (1000-1099)
        DEVICE_NOT_FOUND("DEVICE_NOT_FOUND", "设备不存在"),
        DEVICE_CODE_DUPLICATE("DEVICE_CODE_DUPLICATE", "设备编码已存在"),
        DEVICE_NAME_DUPLICATE("DEVICE_NAME_DUPLICATE", "设备名称已存在"),
        INVALID_DEVICE_TYPE("INVALID_DEVICE_TYPE", "设备类型不正确"),
        INVALID_DEVICE_STATUS("INVALID_DEVICE_STATUS", "设备状态不正确"),
        DEVICE_DISABLED("DEVICE_DISABLED", "设备已禁用"),
        DEVICE_DELETED("DEVICE_DELETED", "设备已删除"),

        // 设备连接异常 (1100-1199)
        DEVICE_OFFLINE("DEVICE_OFFLINE", "设备离线"),
        DEVICE_CONNECTION_FAILED("DEVICE_CONNECTION_FAILED", "设备连接失败"),
        DEVICE_CONNECTION_TIMEOUT("DEVICE_CONNECTION_TIMEOUT", "设备连接超时"),
        DEVICE_HEARTBEAT_FAILED("DEVICE_HEARTBEAT_FAILED", "设备心跳失败"),
        DEVICE_COMMUNICATION_ERROR("DEVICE_COMMUNICATION_ERROR", "设备通信错误"),
        DEVICE_PROTOCOL_ERROR("DEVICE_PROTOCOL_ERROR", "设备协议错误"),
        DEVICE_RESPONSE_TIMEOUT("DEVICE_RESPONSE_TIMEOUT", "设备响应超时"),
        DEVICE_BUSY("DEVICE_BUSY", "设备忙碌"),

        // 设备配置异常 (1200-1299)
        DEVICE_CONFIG_NOT_FOUND("DEVICE_CONFIG_NOT_FOUND", "设备配置不存在"),
        DEVICE_CONFIG_INVALID("DEVICE_CONFIG_INVALID", "设备配置无效"),
        DEVICE_PARAMETER_ERROR("DEVICE_PARAMETER_ERROR", "设备参数错误"),
        DEVICE_CALIBRATION_FAILED("DEVICE_CALIBRATION_FAILED", "设备校准失败"),
        DEVICE_FIRMWARE_ERROR("DEVICE_FIRMWARE_ERROR", "设备固件错误"),
        DEVICE_LICENSE_EXPIRED("DEVICE_LICENSE_EXPIRED", "设备许可证过期"),

        // 设备操作异常 (1300-1399)
        DEVICE_OPERATION_NOT_SUPPORTED("DEVICE_OPERATION_NOT_SUPPORTED", "设备不支持此操作"),
        DEVICE_OPERATION_FAILED("DEVICE_OPERATION_FAILED", "设备操作失败"),
        DEVICE_LOCKED("DEVICE_LOCKED", "设备已锁定"),
        DEVICE_MAINTENANCE_MODE("DEVICE_MAINTENANCE_MODE", "设备处于维护模式"),
        DEVICE_SECURITY_VIOLATION("DEVICE_SECURITY_VIOLATION", "设备安全违规"),
        DEVICE_AUTHENTICATION_FAILED("DEVICE_AUTHENTICATION_FAILED", "设备认证失败"),

        // 设备数据异常 (1400-1499)
        DEVICE_DATA_CORRUPTION("DEVICE_DATA_CORRUPTION", "设备数据损坏"),
        DEVICE_DATA_INCOMPLETE("DEVICE_DATA_INCOMPLETE", "设备数据不完整"),
        DEVICE_DATA_VALIDATION_FAILED("DEVICE_DATA_VALIDATION_FAILED", "设备数据验证失败"),
        DEVICE_STORAGE_FULL("DEVICE_STORAGE_FULL", "设备存储空间不足"),
        DEVICE_MEMORY_ERROR("DEVICE_MEMORY_ERROR", "设备内存错误"),

        // 协议相关异常 (1500-1599)
        PROTOCOL_NOT_SUPPORTED("PROTOCOL_NOT_SUPPORTED", "不支持的协议"),
        PROTOCOL_VERSION_MISMATCH("PROTOCOL_VERSION_MISMATCH", "协议版本不匹配"),
        PROTOCOL_MESSAGE_INVALID("PROTOCOL_MESSAGE_INVALID", "协议消息无效"),
        PROTOCOL_CHECKSUM_ERROR("PROTOCOL_CHECKSUM_ERROR", "协议校验和错误"),
        PROTOCOL_SEQUENCE_ERROR("PROTOCOL_SEQUENCE_ERROR", "协议序列错误"),

        // 网络相关异常 (1600-1699)
        NETWORK_UNREACHABLE("NETWORK_UNREACHABLE", "网络不可达"),
        NETWORK_TIMEOUT("NETWORK_TIMEOUT", "网络超时"),
        NETWORK_BANDWIDTH_LIMITED("NETWORK_BANDWIDTH_LIMITED", "网络带宽限制"),
        NETWORK_FIREWALL_BLOCKED("NETWORK_FIREWALL_BLOCKED", "网络防火墙阻止"),

        // 权限和安全异常 (1700-1799)
        DEVICE_ACCESS_DENIED("DEVICE_ACCESS_DENIED", "设备访问被拒绝"),
        INSUFFICIENT_PRIVILEGES("INSUFFICIENT_PRIVILEGES", "权限不足"),
        DEVICE_SECURITY_POLICY_VIOLATION("DEVICE_SECURITY_POLICY_VIOLATION", "违反设备安全策略"),

        // 供应商相关异常 (1800-1899)
        VENDOR_NOT_SUPPORTED("VENDOR_NOT_SUPPORTED", "不支持的设备供应商"),
        VENDOR_PROTOCOL_INCOMPATIBLE("VENDOR_PROTOCOL_INCOMPATIBLE", "供应商协议不兼容"),
        VENDOR_FIRMWARE_NOT_FOUND("VENDOR_FIRMWARE_NOT_FOUND", "供应商固件未找到"),

        // 通用业务异常 (1900-1999)
        INVALID_PARAMETER("INVALID_PARAMETER", "参数错误"),
        OPERATION_NOT_SUPPORTED("OPERATION_NOT_SUPPORTED", "不支持的操作"),
        BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "违反业务规则"),
        BATCH_OPERATION_FAILED("BATCH_OPERATION_FAILED", "批量操作失败"),
        DEVICE_INITIALIZATION_FAILED("DEVICE_INITIALIZATION_FAILED", "设备初始化失败");

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

    public DeviceBusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null);
    }

    public DeviceBusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public DeviceBusinessException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public DeviceBusinessException(ErrorCode errorCode, String message, Object businessId) {
        super(message);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    public DeviceBusinessException(ErrorCode errorCode, String message, Object businessId, Throwable cause) {
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
     * 获取错误代码（兼容性方法）
     */
    public String getCode() {
        return errorCode.getCode();
    }

    /**
     * 获取详细信息（兼容性方法）
     */
    public Object getDetails() {
        return businessId;
    }

    // ==================== 兼容性构造函数 ====================

    /**
     * 兼容性构造函数（用于快速修复）
     */
    public DeviceBusinessException(String code, String message) {
        super(message);
        this.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
        this.businessId = code;
    }

    /**
     * 兼容性构造函数（仅消息）
     */
    public DeviceBusinessException(String message) {
        this("UNKNOWN_ERROR", message);
    }

    // ==================== 便捷工厂方法 ====================

    /**
     * 设备不存在异常
     */
    public static DeviceBusinessException notFound(Object deviceId) {
        return new DeviceBusinessException(ErrorCode.DEVICE_NOT_FOUND,
            "设备不存在", deviceId);
    }

    /**
     * 设备编码重复异常
     */
    public static DeviceBusinessException duplicateCode(String deviceCode) {
        return new DeviceBusinessException(ErrorCode.DEVICE_CODE_DUPLICATE,
            "设备编码已存在: " + deviceCode, deviceCode);
    }

    /**
     * 设备离线异常
     */
    public static DeviceBusinessException deviceOffline(Object deviceId) {
        return new DeviceBusinessException(ErrorCode.DEVICE_OFFLINE,
            "设备离线", deviceId);
    }

    /**
     * 设备连接失败异常
     */
    public static DeviceBusinessException connectionFailed(Object deviceId, String reason) {
        return new DeviceBusinessException(ErrorCode.DEVICE_CONNECTION_FAILED,
            "设备连接失败: " + reason, deviceId);
    }

    /**
     * 设备连接超时异常
     */
    public static DeviceBusinessException connectionTimeout(Object deviceId) {
        return new DeviceBusinessException(ErrorCode.DEVICE_CONNECTION_TIMEOUT,
            "设备连接超时", deviceId);
    }

    /**
     * 设备通信错误异常
     */
    public static DeviceBusinessException communicationError(Object deviceId, String error) {
        return new DeviceBusinessException(ErrorCode.DEVICE_COMMUNICATION_ERROR,
            "设备通信错误: " + error, deviceId);
    }

    /**
     * 设备响应超时异常
     */
    public static DeviceBusinessException responseTimeout(Object deviceId) {
        return new DeviceBusinessException(ErrorCode.DEVICE_RESPONSE_TIMEOUT,
            "设备响应超时", deviceId);
    }

    /**
     * 设备忙碌异常
     */
    public static DeviceBusinessException deviceBusy(Object deviceId) {
        return new DeviceBusinessException(ErrorCode.DEVICE_BUSY,
            "设备忙碌，请稍后重试", deviceId);
    }

    /**
     * 设备配置无效异常
     */
    public static DeviceBusinessException invalidConfig(Object deviceId, String reason) {
        return new DeviceBusinessException(ErrorCode.DEVICE_CONFIG_INVALID,
            "设备配置无效: " + reason, deviceId);
    }

    /**
     * 设备不支持操作异常
     */
    public static DeviceBusinessException operationNotSupported(Object deviceId, String operation) {
        return new DeviceBusinessException(ErrorCode.DEVICE_OPERATION_NOT_SUPPORTED,
            "设备不支持操作: " + operation, deviceId);
    }

    /**
     * 设备操作失败异常
     */
    public static DeviceBusinessException operationFailed(Object deviceId, String operation, String reason) {
        return new DeviceBusinessException(ErrorCode.DEVICE_OPERATION_FAILED,
            "设备操作失败: " + operation + ", 原因: " + reason, deviceId);
    }

    /**
     * 设备锁定异常
     */
    public static DeviceBusinessException deviceLocked(Object deviceId) {
        return new DeviceBusinessException(ErrorCode.DEVICE_LOCKED,
            "设备已锁定", deviceId);
    }

    /**
     * 设备维护模式异常
     */
    public static DeviceBusinessException deviceMaintenanceMode(Object deviceId) {
        return new DeviceBusinessException(ErrorCode.DEVICE_MAINTENANCE_MODE,
            "设备处于维护模式", deviceId);
    }

    /**
     * 设备访问被拒绝异常
     */
    public static DeviceBusinessException accessDenied(Object deviceId) {
        return new DeviceBusinessException(ErrorCode.DEVICE_ACCESS_DENIED,
            "设备访问被拒绝", deviceId);
    }

    /**
     * 协议不支持异常
     */
    public static DeviceBusinessException protocolNotSupported(String protocol) {
        return new DeviceBusinessException(ErrorCode.PROTOCOL_NOT_SUPPORTED,
            "不支持的协议: " + protocol, protocol);
    }

    /**
     * 网络不可达异常
     */
    public static DeviceBusinessException networkUnreachable(Object deviceId) {
        return new DeviceBusinessException(ErrorCode.NETWORK_UNREACHABLE,
            "网络不可达", deviceId);
    }

    /**
     * 网络超时异常
     */
    public static DeviceBusinessException networkTimeout(Object deviceId) {
        return new DeviceBusinessException(ErrorCode.NETWORK_TIMEOUT,
            "网络超时", deviceId);
    }

    /**
     * 供应商不支持异常
     */
    public static DeviceBusinessException vendorNotSupported(String vendor) {
        return new DeviceBusinessException(ErrorCode.VENDOR_NOT_SUPPORTED,
            "不支持的设备供应商: " + vendor, vendor);
    }

    /**
     * 参数错误异常
     */
    public static DeviceBusinessException invalidParameter(String message) {
        return new DeviceBusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 违反业务规则异常
     */
    public static DeviceBusinessException businessRuleViolation(String message) {
        return new DeviceBusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    /**
     * 批量操作失败异常
     */
    public static DeviceBusinessException batchOperationFailed(String operation, String reason) {
        return new DeviceBusinessException(ErrorCode.BATCH_OPERATION_FAILED,
            "批量" + operation + "失败: " + reason);
    }

    /**
     * 创建验证失败的异常
     */
    public static DeviceBusinessException validationFailed(java.util.List<String> errors) {
        String message = "验证失败: " + String.join(", ", errors);
        return new DeviceBusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 创建数据库操作失败的异常
     */
    public static DeviceBusinessException databaseError(String operation, String details) {
        return new DeviceBusinessException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "数据库操作失败: " + operation + ", 详情: " + details);
    }
}