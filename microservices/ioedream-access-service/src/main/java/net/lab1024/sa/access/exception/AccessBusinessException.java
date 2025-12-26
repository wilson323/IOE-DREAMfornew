package net.lab1024.sa.access.exception;

/**
 * 门禁业务异常
 * <p>
 * 统一处理门禁相关的业务异常
 * 继承自全局异常体系
 * 提供详细的错误分类和错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
public class AccessBusinessException extends RuntimeException {

    /**
     * 异常代码枚举
     */
    public enum ErrorCode {
        // 访问权限异常 (1000-1099)
        ACCESS_DENIED("ACCESS_DENIED", "访问被拒绝"),
        PERMISSION_NOT_FOUND("PERMISSION_NOT_FOUND", "权限不存在"),
        PERMISSION_EXPIRED("PERMISSION_EXPIRED", "权限已过期"),
        PERMISSION_REVOKED("PERMISSION_REVOKED", "权限已撤销"),
        INSUFFICIENT_PRIVILEGES("INSUFFICIENT_PRIVILEGES", "权限不足"),
        AREA_ACCESS_DENIED("AREA_ACCESS_DENIED", "区域访问被拒绝"),
        TIME_ACCESS_DENIED("TIME_ACCESS_DENIED", "时间访问被拒绝"),
        INVALID_CREDENTIAL("INVALID_CREDENTIAL", "无效凭证"),

        // 身份验证异常 (1100-1199)
        AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "身份验证失败"),
        USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在"),
        USER_DISABLED("USER_DISABLED", "用户已禁用"),
        USER_LOCKED("USER_LOCKED", "用户已锁定"),
        BIOMETRIC_NOT_FOUND("BIOMETRIC_NOT_FOUND", "生物特征未找到"),
        BIOMETRIC_EXPIRED("BIOMETRIC_EXPIRED", "生物特征已过期"),
        BIOMETRIC_MATCH_FAILED("BIOMETRIC_MATCH_FAILED", "生物特征匹配失败"),
        CARD_NOT_FOUND("CARD_NOT_FOUND", "卡片未找到"),
        CARD_EXPIRED("CARD_EXPIRED", "卡片已过期"),
        CARD_DISABLED("CARD_DISABLED", "卡片已禁用"),
        CARD_LOST("CARD_LOST", "卡片已挂失"),

        // 门禁设备异常 (1200-1299)
        DEVICE_NOT_FOUND("DEVICE_NOT_FOUND", "门禁设备不存在"),
        DEVICE_OFFLINE("DEVICE_OFFLINE", "门禁设备离线"),
        DEVICE_MALFUNCTION("DEVICE_MALFUNCTION", "门禁设备故障"),
        DEVICE_BUSY("DEVICE_BUSY", "门禁设备忙碌"),
        DEVICE_MAINTENANCE("DEVICE_MAINTENANCE", "门禁设备维护中"),
        DOOR_CONTROL_FAILED("DOOR_CONTROL_FAILED", "门控操作失败"),
        DOOR_OPEN_TIMEOUT("DOOR_OPEN_TIMEOUT", "开门超时"),
        DOOR_FORCE_OPENED("DOOR_FORCE_OPENED", "门被强行打开"),
        DOOR_NOT_CLOSED("DOOR_NOT_CLOSED", "门未关闭"),
        DOOR_SENSOR_ERROR("DOOR_SENSOR_ERROR", "门传感器错误"),

        // 反潜回异常 (1300-1399)
        ANTI_PASSBACK_VIOLATION("ANTI_PASSBACK_VIOLATION", "违反反潜回规则"),
        DUPLICATE_ACCESS("DUPLICATE_ACCESS", "重复访问"),
        CONCURRENT_ACCESS("CONCURRENT_ACCESS", "并发访问"),
        PASSBACK_TIMEOUT("PASSBACK_TIMEOUT", "潜回超时"),
        DIRECTION_VIOLATION("DIRECTION_VIOLATION", "方向违规"),
        AREA_SEQUENCE_VIOLATION("AREA_SEQUENCE_VIOLATION", "区域顺序违规"),

        // 访问记录异常 (1400-1499)
        RECORD_NOT_FOUND("RECORD_NOT_FOUND", "访问记录不存在"),
        RECORD_DUPLICATE("RECORD_DUPLICATE", "访问记录重复"),
        RECORD_CORRUPTION("RECORD_CORRUPTION", "访问记录损坏"),
        RECORD_UPLOAD_FAILED("RECORD_UPLOAD_FAILED", "记录上传失败"),
        BATCH_RECORD_PROCESS_FAILED("BATCH_RECORD_PROCESS_FAILED", "批量记录处理失败"),

        // 多模态认证异常 (1500-1599)
        MULTIMODAL_AUTH_FAILED("MULTIMODAL_AUTH_FAILED", "多模态认证失败"),
        AUTH_FACTOR_INSUFFICIENT("AUTH_FACTOR_INSUFFICIENT", "认证因子不足"),
        AUTH_FACTOR_MISMATCH("AUTH_FACTOR_MISMATCH", "认证因子不匹配"),
        AUTH_SEQUENCE_INVALID("AUTH_SEQUENCE_INVALID", "认证序列无效"),
        BIOMETRIC_QUALITY_POOR("BIOMETRIC_QUALITY_POOR", "生物特征质量差"),
        FACE_RECOGNITION_FAILED("FACE_RECOGNITION_FAILED", "人脸识别失败"),
        FINGERPRINT_RECOGNITION_FAILED("FINGERPRINT_RECOGNITION_FAILED", "指纹识别失败"),
        IRIS_RECOGNITION_FAILED("IRIS_RECOGNITION_FAILED", "虹膜识别失败"),
        NFC_READ_FAILED("NFC_READ_FAILED", "NFC读取失败"),
        VERIFICATION_TIMEOUT("VERIFICATION_TIMEOUT", "验证超时"),

        // 区域 management 异常 (1600-1699)
        AREA_NOT_FOUND("AREA_NOT_FOUND", "区域不存在"),
        AREA_CAPACITY_EXCEEDED("AREA_CAPACITY_EXCEEDED", "区域容量超限"),
        AREA_TIME_RESTRICTION("AREA_TIME_RESTRICTION", "区域时间限制"),
        AREA_LEVEL_VIOLATION("AREA_LEVEL_VIOLATION", "区域级别违规"),
        AREA_SECURITY_BREACH("AREA_SECURITY_BREACH", "区域安全违规"),

        // 离线记录异常 (1700-1799)
        OFFLINE_RECORD_NOT_FOUND("OFFLINE_RECORD_NOT_FOUND", "离线记录不存在"),
        OFFLINE_RECORD_CORRUPTION("OFFLINE_RECORD_CORRUPTION", "离线记录损坏"),
        OFFLINE_REPLAY_FAILED("OFFLINE_REPLAY_FAILED", "离线记录重放失败"),
        OFFLINE_SYNC_FAILED("OFFLINE_SYNC_FAILED", "离线同步失败"),
        DEVICE_MEMORY_FULL("DEVICE_MEMORY_FULL", "设备存储空间不足"),

        // 配置管理异常 (1800-1899)
        CONFIG_NOT_FOUND("CONFIG_NOT_FOUND", "配置不存在"),
        CONFIG_INVALID("CONFIG_INVALID", "配置无效"),
        ACCESS_POLICY_NOT_FOUND("ACCESS_POLICY_NOT_FOUND", "访问策略不存在"),
        ACCESS_POLICY_INVALID("ACCESS_POLICY_INVALID", "访问策略无效"),
        TIME_SCHEDULE_CONFLICT("TIME_SCHEDULE_CONFLICT", "时间表冲突"),
        AREA_POLICY_CONFLICT("AREA_POLICY_CONFLICT", "区域策略冲突"),

        // 监控和告警异常 (1900-1999)
        MONITOR_DATA_NOT_FOUND("MONITOR_DATA_NOT_FOUND", "监控数据不存在"),
        ALARM_RULE_NOT_FOUND("ALARM_RULE_NOT_FOUND", "告警规则不存在"),
        ALARM_THRESHOLD_EXCEEDED("ALARM_THRESHOLD_EXCEEDED", "告警阈值超限"),
        ABNORMAL_ACCESS_PATTE("ABNORMAL_ACCESS_PATTE", "异常访问模式"),
        SECURITY_BREACH_DETECTED("SECURITY_BREACH_DETECTED", "检测到安全违规"),

        // 通用业务异常 (2000-2099)
        INVALID_PARAMETER("INVALID_PARAMETER", "参数错误"),
        OPERATION_NOT_SUPPORTED("OPERATION_NOT_SUPPORTED", "不支持的操作"),
        BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "违反业务规则"),
        BATCH_OPERATION_FAILED("BATCH_OPERATION_FAILED", "批量操作失败"),
        SYSTEM_MAINTENANCE("SYSTEM_MAINTENANCE", "系统维护中"),
        RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "请求频率超限"),
        DATA_CONSISTENCY_ERROR("DATA_CONSISTENCY_ERROR", "数据一致性错误");

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

    public AccessBusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null);
    }

    public AccessBusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public AccessBusinessException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public AccessBusinessException(ErrorCode errorCode, String message, Object businessId) {
        super(message);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    public AccessBusinessException(ErrorCode errorCode, String message, Object businessId, Throwable cause) {
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
    public AccessBusinessException(String code, String message) {
        super(message);
        this.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
        this.businessId = code;
    }

    /**
     * 兼容性构造函数（仅消息）
     */
    public AccessBusinessException(String message) {
        this("UNKNOWN_ERROR", message);
    }

    // ==================== 便捷工厂方法 ====================

    /**
     * 访问被拒绝异常
     */
    public static AccessBusinessException accessDenied(Object userId, Object areaId) {
        return new AccessBusinessException(ErrorCode.ACCESS_DENIED,
            "访问被拒绝", userId + "|" + areaId);
    }

    /**
     * 权限不存在异常
     */
    public static AccessBusinessException permissionNotFound(Object permissionId) {
        return new AccessBusinessException(ErrorCode.PERMISSION_NOT_FOUND,
            "权限不存在", permissionId);
    }

    /**
     * 权限已过期异常
     */
    public static AccessBusinessException permissionExpired(Object permissionId) {
        return new AccessBusinessException(ErrorCode.PERMISSION_EXPIRED,
            "权限已过期", permissionId);
    }

    /**
     * 身份验证失败异常
     */
    public static AccessBusinessException authenticationFailed(Object userId) {
        return new AccessBusinessException(ErrorCode.AUTHENTICATION_FAILED,
            "身份验证失败", userId);
    }

    /**
     * 用户不存在异常
     */
    public static AccessBusinessException useotFound(Object userId) {
        return new AccessBusinessException(ErrorCode.USER_NOT_FOUND,
            "用户不存在", userId);
    }

    /**
     * 用户已禁用异常
     */
    public static AccessBusinessException userDisabled(Object userId) {
        return new AccessBusinessException(ErrorCode.USER_DISABLED,
            "用户已禁用", userId);
    }

    /**
     * 生物特征未找到异常
     */
    public static AccessBusinessException biometricNotFound(Object userId, String biometricType) {
        return new AccessBusinessException(ErrorCode.BIOMETRIC_NOT_FOUND,
            "生物特征未找到: " + biometricType, userId);
    }

    /**
     * 生物特征匹配失败异常
     */
    public static AccessBusinessException biometricMatchFailed(Object userId, String biometricType) {
        return new AccessBusinessException(ErrorCode.BIOMETRIC_MATCH_FAILED,
            "生物特征匹配失败: " + biometricType, userId);
    }

    /**
     * 卡片不存在异常
     */
    public static AccessBusinessException cardNotFound(String cardNumber) {
        return new AccessBusinessException(ErrorCode.CARD_NOT_FOUND,
            "卡片不存在", cardNumber);
    }

    /**
     * 卡片已过期异常
     */
    public static AccessBusinessException cardExpired(String cardNumber) {
        return new AccessBusinessException(ErrorCode.CARD_EXPIRED,
            "卡片已过期", cardNumber);
    }

    /**
     * 门禁设备不存在异常
     */
    public static AccessBusinessException deviceNotFound(Object deviceId) {
        return new AccessBusinessException(ErrorCode.DEVICE_NOT_FOUND,
            "门禁设备不存在", deviceId);
    }

    /**
     * 门禁设备离线异常
     */
    public static AccessBusinessException deviceOffline(Object deviceId) {
        return new AccessBusinessException(ErrorCode.DEVICE_OFFLINE,
            "门禁设备离线", deviceId);
    }

    /**
     * 门控操作失败异常
     */
    public static AccessBusinessException doorControlFailed(Object deviceId, String operation) {
        return new AccessBusinessException(ErrorCode.DOOR_CONTROL_FAILED,
            "门控操作失败: " + operation, deviceId);
    }

    /**
     * 违反反潜回规则异常
     */
    public static AccessBusinessException antiPassbackViolation(Object userId, Object deviceId) {
        return new AccessBusinessException(ErrorCode.ANTI_PASSBACK_VIOLATION,
            "违反反潜回规则", userId + "|" + deviceId);
    }

    /**
     * 重复访问异常
     */
    public static AccessBusinessException duplicateAccess(Object userId, Object deviceId) {
        return new AccessBusinessException(ErrorCode.DUPLICATE_ACCESS,
            "重复访问", userId + "|" + deviceId);
    }

    /**
     * 区域不存在异常
     */
    public static AccessBusinessException areaNotFound(Object areaId) {
        return new AccessBusinessException(ErrorCode.AREA_NOT_FOUND,
            "区域不存在", areaId);
    }

    /**
     * 区域访问被拒绝异常
     */
    public static AccessBusinessException areaAccessDenied(Object userId, Object areaId) {
        return new AccessBusinessException(ErrorCode.AREA_ACCESS_DENIED,
            "区域访问被拒绝", userId + "|" + areaId);
    }

    /**
     * 区域容量超限异常
     */
    public static AccessBusinessException areaCapacityExceeded(Object areaId, Object currentCount, Object maxCount) {
        return new AccessBusinessException(ErrorCode.AREA_CAPACITY_EXCEEDED,
            "区域容量超限: " + currentCount + "/" + maxCount, areaId);
    }

    /**
     * 离线记录不存在异常
     */
    public static AccessBusinessException offlineRecordNotFound(Object recordId) {
        return new AccessBusinessException(ErrorCode.OFFLINE_RECORD_NOT_FOUND,
            "离线记录不存在", recordId);
    }

    /**
     * 离线记录重放失败异常
     */
    public static AccessBusinessException offlineReplayFailed(Object deviceId, String reason) {
        return new AccessBusinessException(ErrorCode.OFFLINE_REPLAY_FAILED,
            "离线记录重放失败: " + reason, deviceId);
    }

    /**
     * 多模态认证失败异常
     */
    public static AccessBusinessException multimodalAuthFailed(Object userId, String reason) {
        return new AccessBusinessException(ErrorCode.MULTIMODAL_AUTH_FAILED,
            "多模态认证失败: " + reason, userId);
    }

    /**
     * 人脸识别失败异常
     */
    public static AccessBusinessException faceRecognitionFailed(Object userId, String reason) {
        return new AccessBusinessException(ErrorCode.FACE_RECOGNITION_FAILED,
            "人脸识别失败: " + reason, userId);
    }

    /**
     * 指纹识别失败异常
     */
    public static AccessBusinessException fingerprintRecognitionFailed(Object userId, String reason) {
        return new AccessBusinessException(ErrorCode.FINGERPRINT_RECOGNITION_FAILED,
            "指纹识别失败: " + reason, userId);
    }

    /**
     * 虹膜识别失败异常
     */
    public static AccessBusinessException irisRecognitionFailed(Object userId, String reason) {
        return new AccessBusinessException(ErrorCode.IRIS_RECOGNITION_FAILED,
            "虹膜识别失败: " + reason, userId);
    }

    /**
     * NFC读取失败异常
     */
    public static AccessBusinessException nfcReadFailed(String cardNumber, String reason) {
        return new AccessBusinessException(ErrorCode.NFC_READ_FAILED,
            "NFC读取失败: " + reason, cardNumber);
    }

    /**
     * 验证超时异常
     */
    public static AccessBusinessException verificationTimeout(Object deviceId) {
        return new AccessBusinessException(ErrorCode.VERIFICATION_TIMEOUT,
            "验证超时", deviceId);
    }

    /**
     * 安全违规检测异常
     */
    public static AccessBusinessException securityBreachDetected(Object userId, String violation) {
        return new AccessBusinessException(ErrorCode.SECURITY_BREACH_DETECTED,
            "检测到安全违规: " + violation, userId);
    }

    /**
     * 配置不存在异常
     */
    public static AccessBusinessException configNotFound(String configType) {
        return new AccessBusinessException(ErrorCode.CONFIG_NOT_FOUND,
            "配置不存在: " + configType, configType);
    }

    /**
     * 访问策略不存在异常
     */
    public static AccessBusinessException accessPolicyNotFound(Object policyId) {
        return new AccessBusinessException(ErrorCode.ACCESS_POLICY_NOT_FOUND,
            "访问策略不存在", policyId);
    }

    /**
     * 参数错误异常
     */
    public static AccessBusinessException invalidParameter(String message) {
        return new AccessBusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 违反业务规则异常
     */
    public static AccessBusinessException businessRuleViolation(String message) {
        return new AccessBusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    /**
     * 批量操作失败异常
     */
    public static AccessBusinessException batchOperationFailed(String operation, String reason) {
        return new AccessBusinessException(ErrorCode.BATCH_OPERATION_FAILED,
            "批量" + operation + "失败: " + reason);
    }

    /**
     * 系统维护中异常
     */
    public static AccessBusinessException systemMaintenance() {
        return new AccessBusinessException(ErrorCode.SYSTEM_MAINTENANCE,
            "系统维护中，请稍后重试");
    }

    /**
     * 请求频率超限异常
     */
    public static AccessBusinessException rateLimitExceeded(Object deviceId) {
        return new AccessBusinessException(ErrorCode.RATE_LIMIT_EXCEEDED,
            "请求频率超限", deviceId);
    }

    /**
     * 创建验证失败的异常
     */
    public static AccessBusinessException validationFailed(java.util.List<String> errors) {
        String message = "验证失败: " + String.join(", ", errors);
        return new AccessBusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 创建数据库操作失败的异常
     */
    public static AccessBusinessException databaseError(String operation, String details) {
        return new AccessBusinessException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "数据库操作失败: " + operation + ", 详情: " + details);
    }
}