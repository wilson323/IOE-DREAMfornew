package net.lab1024.sa.video.exception;

/**
 * 视频业务异常
 * <p>
 * 统一处理视频相关的业务异常
 * 继承自全局异常体系
 * 提供详细的错误分类和错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
public class VideoBusinessException extends RuntimeException {

    /**
     * 异常代码枚举
     */
    public enum ErrorCode {
        // 视频设备管理异常 (1000-1099)
        VIDEO_DEVICE_NOT_FOUND("VIDEO_DEVICE_NOT_FOUND", "视频设备不存在"),
        VIDEO_DEVICE_OFFLINE("VIDEO_DEVICE_OFFLINE", "视频设备离线"),
        VIDEO_DEVICE_MALFUNCTION("VIDEO_DEVICE_MALFUNCTION", "视频设备故障"),
        VIDEO_DEVICE_BUSY("VIDEO_DEVICE_BUSY", "视频设备忙碌"),
        VIDEO_DEVICE_MAINTENANCE("VIDEO_DEVICE_MAINTENANCE", "视频设备维护中"),
        VIDEO_DEVICE_NOT_AUTHORIZED("VIDEO_DEVICE_NOT_AUTHORIZED", "视频设备未授权"),
        VIDEO_DEVICE_CONFIGURATION_INVALID("VIDEO_DEVICE_CONFIGURATION_INVALID", "视频设备配置无效"),
        VIDEO_DEVICE_FIRMWARE_OUTDATED("VIDEO_DEVICE_FIRMWARE_OUTDATED", "视频设备固件过时"),
        VIDEO_DEVICE_LICENSE_EXPIRED("VIDEO_DEVICE_LICENSE_EXPIRED", "视频设备许可证过期"),
        VIDEO_DEVICE_STORAGE_FULL("VIDEO_DEVICE_STORAGE_FULL", "视频设备存储空间不足"),

        // 视频流异常 (1100-1199)
        VIDEO_STREAM_NOT_FOUND("VIDEO_STREAM_NOT_FOUND", "视频流不存在"),
        VIDEO_STREAM_UNAVAILABLE("VIDEO_STREAM_UNAVAILABLE", "视频流不可用"),
        VIDEO_STREAM_INTERRUPTED("VIDEO_STREAM_INTERRUPTED", "视频流中断"),
        VIDEO_STREAM_QUALITY_POOR("VIDEO_STREAM_QUALITY_POOR", "视频流质量差"),
        VIDEO_STREAM_BANDWIDTH_INSUFFICIENT("VIDEO_STREAM_BANDWIDTH_INSUFFICIENT", "视频流带宽不足"),
        VIDEO_STREAM_CODEC_UNSUPPORTED("VIDEO_STREAM_CODEC_UNSUPPORTED", "视频流编码不支持"),
        VIDEO_STREAM_RESOLUTION_INVALID("VIDEO_STREAM_RESOLUTION_INVALID", "视频流分辨率无效"),
        VIDEO_STREAM_FRAME_RATE_INVALID("VIDEO_STREAM_FRAME_RATE_INVALID", "视频流帧率无效"),
        VIDEO_STREAM_BITRATE_INVALID("VIDEO_STREAM_BITRATE_INVALID", "视频流比特率无效"),
        VIDEO_STREAM_CONNECTION_FAILED("VIDEO_STREAM_CONNECTION_FAILED", "视频流连接失败"),

        // 视频录制异常 (1200-1299)
        RECORDING_NOT_STARTED("RECORDING_NOT_STARTED", "录制未开始"),
        RECORDING_ALREADY_STARTED("RECORDING_ALREADY_STARTED", "录制已开始"),
        RECORDING_FAILED("RECORDING_FAILED", "录制失败"),
        RECORDING_SCHEDULE_CONFLICT("RECORDING_SCHEDULE_CONFLICT", "录制计划冲突"),
        RECORDING_STORAGE_FULL("RECORDING_STORAGE_FULL", "录制存储空间不足"),
        RECORDING_FILE_CORRUPTION("RECORDING_FILE_CORRUPTION", "录制文件损坏"),
        RECORDING_PERMISSION_DENIED("RECORDING_PERMISSION_DENIED", "录制权限被拒绝"),
        RECORDING_TIME_EXCEEDED("RECORDING_TIME_EXCEEDED", "录制时间超限"),
        RECORDING_FORMAT_UNSUPPORTED("RECORDING_FORMAT_UNSUPPORTED", "录制格式不支持"),
        RECORDING_QUALITY_INVALID("RECORDING_QUALITY_INVALID", "录制质量无效"),

        // 视频回放异常 (1300-1399)
        PLAYBACK_FILE_NOT_FOUND("PLAYBACK_FILE_NOT_FOUND", "回放文件不存在"),
        PLAYBACK_FILE_CORRUPTION("PLAYBACK_FILE_CORRUPTION", "回放文件损坏"),
        PLAYBACK_PERMISSION_DENIED("PLAYBACK_PERMISSION_DENIED", "回放权限被拒绝"),
        PLAYBACK_TIME_RANGE_INVALID("PLAYBACK_TIME_RANGE_INVALID", "回放时间范围无效"),
        PLAYBACK_SPEED_INVALID("PLAYBACK_SPEED_INVALID", "回放速度无效"),
        PLAYBACK_STREAM_INTERRUPTED("PLAYBACK_STREAM_INTERRUPTED", "回放流中断"),
        PLAYBACK_BUFFERING_FAILED("PLAYBACK_BUFFERING_FAILED", "回放缓冲失败"),
        PLAYBACK_CONCURRENT_LIMIT_EXCEEDED("PLAYBACK_CONCURRENT_LIMIT_EXCEEDED", "回放并发数超限"),
        PLAYBACK_CODEC_UNSUPPORTED("PLAYBACK_CODEC_UNSUPPORTED", "回放编码不支持"),
        PLAYBACK_AUTHENTICATION_FAILED("PLAYBACK_AUTHENTICATION_FAILED", "回放认证失败"),

        // 视频分析异常 (1400-1499)
        VIDEO_ANALYSIS_FAILED("VIDEO_ANALYSIS_FAILED", "视频分析失败"),
        FACE_DETECTION_FAILED("FACE_DETECTION_FAILED", "人脸检测失败"),
        FACE_RECOGNITION_FAILED("FACE_RECOGNITION_FAILED", "人脸识别失败"),
        OBJECT_DETECTION_FAILED("OBJECT_DETECTION_FAILED", "物体检测失败"),
        MOTION_DETECTION_FAILED("MOTION_DETECTION_FAILED", "运动检测失败"),
        BEHAVIOR_ANALYSIS_FAILED("BEHAVIOR_ANALYSIS_FAILED", "行为分析失败"),
        CROWD_ANALYSIS_FAILED("CROWD_ANALYSIS_FAILED", "人群分析失败"),
        LICENSE_PLATE_RECOGNITION_FAILED("LICENSE_PLATE_RECOGNITION_FAILED", "车牌识别失败"),
        VIDEO_ANALYSIS_MODEL_NOT_FOUND("VIDEO_ANALYSIS_MODEL_NOT_FOUND", "视频分析模型不存在"),
        VIDEO_ANALYSIS_MODEL_INVALID("VIDEO_ANALYSIS_MODEL_INVALID", "视频分析模型无效"),

        // AI边缘计算异常 (1500-1599)
        EDGE_AI_PROCESSING_FAILED("EDGE_AI_PROCESSING_FAILED", "边缘AI处理失败"),
        EDGE_MODEL_NOT_LOADED("EDGE_MODEL_NOT_LOADED", "边缘模型未加载"),
        EDGE_INFERENCE_TIMEOUT("EDGE_INFERENCE_TIMEOUT", "边缘推理超时"),
        EDGE_GPU_INSUFFICIENT("EDGE_GPU_INSUFFICIENT", "边缘GPU资源不足"),
        EDGE_MEMORY_INSUFFICIENT("EDGE_MEMORY_INSUFFICIENT", "边缘内存不足"),
        EDGE_FIRMWARE_INCOMPATIBLE("EDGE_FIRMWARE_INCOMPATIBLE", "边缘固件不兼容"),
        EDGE_ALGORITHM_NOT_SUPPORTED("EDGE_ALGORITHM_NOT_SUPPORTED", "边缘算法不支持"),
        EDGE_DATA_TRANSFER_FAILED("EDGE_DATA_TRANSFER_FAILED", "边缘数据传输失败"),
        EDGE_CALIBRATION_FAILED("EDGE_CALIBRATION_FAILED", "边缘校准失败"),
        EDGE_PERFORMANCE_DEGRADED("EDGE_PERFORMANCE_DEGRADED", "边缘性能下降"),

        // 告警管理异常 (1600-1699)
        ALERT_RULE_NOT_FOUND("ALERT_RULE_NOT_FOUND", "告警规则不存在"),
        ALERT_RULE_INVALID("ALERT_RULE_INVALID", "告警规则无效"),
        ALERT_RULE_CONFLICT("ALERT_RULE_CONFLICT", "告警规则冲突"),
        ALERT_THRESHOLD_EXCEEDED("ALERT_THRESHOLD_EXCEEDED", "告警阈值超限"),
        ALERT_NOTIFICATION_FAILED("ALERT_NOTIFICATION_FAILED", "告警通知失败"),
        ALERT_FALSE_POSITIVE("ALERT_FALSE_POSITIVE", "告警误报"),
        ALERT_SUPPRESSION_ACTIVE("ALERT_SUPPRESSION_ACTIVE", "告警抑制激活"),
        ALERT_CONFIGURATION_INVALID("ALERT_CONFIGURATION_INVALID", "告警配置无效"),
        ALERT_ESCALATION_FAILED("ALERT_ESCALATION_FAILED", "告警升级失败"),
        ALERT_LOGGING_FAILED("ALERT_LOGGING_FAILED", "告警记录失败"),

        // 视频存储异常 (1700-1799)
        VIDEO_STORAGE_NOT_FOUND("VIDEO_STORAGE_NOT_FOUND", "视频存储不存在"),
        VIDEO_STORAGE_FULL("VIDEO_STORAGE_FULL", "视频存储空间不足"),
        VIDEO_STORAGE_ACCESS_DENIED("VIDEO_STORAGE_ACCESS_DENIED", "视频存储访问被拒绝"),
        VIDEO_STORAGE_CONNECTION_FAILED("VIDEO_STORAGE_CONNECTION_FAILED", "视频存储连接失败"),
        VIDEO_STORAGE_BACKUP_FAILED("VIDEO_STORAGE_BACKUP_FAILED", "视频存储备份失败"),
        VIDEO_STORAGE_RESTORE_FAILED("VIDEO_STORAGE_RESTORE_FAILED", "视频存储恢复失败"),
        VIDEO_STORAGE_ENCRYPTION_FAILED("VIDEO_STORAGE_ENCRYPTION_FAILED", "视频存储加密失败"),
        VIDEO_STORAGE_RETENTION_POLICY_VIOLATION("VIDEO_STORAGE_RETENTION_POLICY_VIOLATION", "违反视频存储保留策略"),
        VIDEO_STORAGE_ARCHIVING_FAILED("VIDEO_STORAGE_ARCHIVING_FAILED", "视频存储归档失败"),
        VIDEO_STORAGE_PURGE_FAILED("VIDEO_STORAGE_PURGE_FAILED", "视频存储清理失败"),

        // 网络传输异常 (1800-1899)
        NETWORK_CONNECTION_FAILED("NETWORK_CONNECTION_FAILED", "网络连接失败"),
        NETWORK_BANDWIDTH_INSUFFICIENT("NETWORK_BANDWIDTH_INSUFFICIENT", "网络带宽不足"),
        NETWORK_LATENCY_HIGH("NETWORK_LATENCY_HIGH", "网络延迟过高"),
        NETWORK_PACKET_LOSS("NETWORK_PACKET_LOSS", "网络丢包"),
        NETWORK_FIREWALL_BLOCKED("NETWORK_FIREWALL_BLOCKED", "网络防火墙阻止"),
        NETWORK_QOS_VIOLATION("NETWORK_QOS_VIOLATION", "网络QoS违规"),
        NETWORK_PROTOCOL_MISMATCH("NETWORK_PROTOCOL_MISMATCH", "网络协议不匹配"),
        NETWORK_AUTHENTICATION_FAILED("NETWORK_AUTHENTICATION_FAILED", "网络认证失败"),
        NETWORK_ENCRYPTION_FAILED("NETWORK_ENCRYPTION_FAILED", "网络加密失败"),
        NETWORK_LOAD_BALANCER_FAILED("NETWORK_LOAD_BALANCER_FAILED", "网络负载均衡器失败"),

        // 视频质量异常 (1900-1999)
        VIDEO_QUALITY_DEGRADED("VIDEO_QUALITY_DEGRADED", "视频质量下降"),
        VIDEO_RESOLUTION_INVALID("VIDEO_RESOLUTION_INVALID", "视频分辨率无效"),
        VIDEO_FRAME_RATE_LOW("VIDEO_FRAME_RATE_LOW", "视频帧率过低"),
        VIDEO_BITRATE_LOW("VIDEO_BITRATE_LOW", "视频比特率过低"),
        VIDEO_NOISE_LEVEL_HIGH("VIDEO_NOISE_LEVEL_HIGH", "视频噪声水平过高"),
        VIDEO_BRIGHTNESS_INVALID("VIDEO_BRIGHTNESS_INVALID", "视频亮度无效"),
        VIDEO_CONTRAST_INVALID("VIDEO_CONTRAST_INVALID", "视频对比度无效"),
        VIDEO_COLOR_BALANCE_INVALID("VIDEO_COLOR_BALANCE_INVALID", "视频色彩平衡无效"),
        VIDEO_STABILITY_POOR("VIDEO_STABILITY_POOR", "视频稳定性差"),
        VIDEO_CLARITY_POOR("VIDEO_CLARITY_POOR", "视频清晰度差"),

        // 权限和安全异常 (2000-2099)
        VIDEO_ACCESS_DENIED("VIDEO_ACCESS_DENIED", "视频访问被拒绝"),
        VIDEO_CONTROL_PERMISSION_DENIED("VIDEO_CONTROL_PERMISSION_DENIED", "视频控制权限被拒绝"),
        VIDEO_VIEW_PERMISSION_DENIED("VIDEO_VIEW_PERMISSION_DENIED", "视频查看权限被拒绝"),
        VIDEO_EXPORT_PERMISSION_DENIED("VIDEO_EXPORT_PERMISSION_DENIED", "视频导出权限被拒绝"),
        VIDEO_DELETE_PERMISSION_DENIED("VIDEO_DELETE_PERMISSION_DENIED", "视频删除权限被拒绝"),
        VIDEO_CONFIG_PERMISSION_DENIED("VIDEO_CONFIG_PERMISSION_DENIED", "视频配置权限被拒绝"),
        VIDEO_ANALYTICS_PERMISSION_DENIED("VIDEO_ANALYTICS_PERMISSION_DENIED", "视频分析权限被拒绝"),
        VIDEO_PRIVACY_POLICY_VIOLATION("VIDEO_PRIVACY_POLICY_VIOLATION", "违反视频隐私政策"),
        VIDEO_SECURITY_POLICY_VIOLATION("VIDEO_SECURITY_POLICY_VIOLATION", "违反视频安全政策"),
        VIDEO_AUDIT_LOG_MISSING("VIDEO_AUDIT_LOG_MISSING", "视频审计日志缺失"),

        // 供应商和协议异常 (2100-2199)
        VENDOR_NOT_SUPPORTED("VENDOR_NOT_SUPPORTED", "不支持的供应商"),
        VENDOR_PROTOCOL_INCOMPATIBLE("VENDOR_PROTOCOL_INCOMPATIBLE", "供应商协议不兼容"),
        VENDOR_FIRMWARE_NOT_FOUND("VENDOR_FIRMWARE_NOT_FOUND", "供应商固件未找到"),
        VENDOR_LICENSE_INVALID("VENDOR_LICENSE_INVALID", "供应商许可证无效"),
        VENDOR_API_ACCESS_DENIED("VENDOR_API_ACCESS_DENIED", "供应商API访问被拒绝"),
        VENDOR_API_RATE_LIMITED("VENDOR_API_RATE_LIMITED", "供应商API频率限制"),
        VENDOR_CREDENTIAL_INVALID("VENDOR_CREDENTIAL_INVALID", "供应商凭证无效"),
        VENDOR_SERVICE_UNAVAILABLE("VENDOR_SERVICE_UNAVAILABLE", "供应商服务不可用"),
        VENDOR_INTEGRATION_FAILED("VENDOR_INTEGRATION_FAILED", "供应商集成失败"),
        VENDOR_VERSION_MISMATCH("VENDOR_VERSION_MISMATCH", "供应商版本不匹配"),

        // 通用业务异常 (2200-2299)
        INVALID_PARAMETER("INVALID_PARAMETER", "参数错误"),
        OPERATION_NOT_SUPPORTED("OPERATION_NOT_SUPPORTED", "不支持的操作"),
        BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "违反业务规则"),
        BATCH_OPERATION_FAILED("BATCH_OPERATION_FAILED", "批量操作失败"),
        SYSTEM_MAINTENANCE("SYSTEM_MAINTENANCE", "系统维护中"),
        RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "请求频率超限"),
        DATA_CONSISTENCY_ERROR("DATA_CONSISTENCY_ERROR", "数据一致性错误"),
        RESOURCE_NOT_AVAILABLE("RESOURCE_NOT_AVAILABLE", "资源不可用"),
        OPERATION_TIMEOUT("OPERATION_TIMEOUT", "操作超时"),
        CONCURRENT_OPERATION_LIMIT_EXCEEDED("CONCURRENT_OPERATION_LIMIT_EXCEEDED", "并发操作数超限");

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

    public VideoBusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null);
    }

    public VideoBusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public VideoBusinessException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public VideoBusinessException(ErrorCode errorCode, String message, Object businessId) {
        super(message);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    public VideoBusinessException(ErrorCode errorCode, String message, Object businessId, Throwable cause) {
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
    public VideoBusinessException(String code, String message) {
        super(message);
        this.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
        this.businessId = code;
    }

    /**
     * 兼容性构造函数（仅消息）
     */
    public VideoBusinessException(String message) {
        this("UNKNOWN_ERROR", message);
    }

    // ==================== 便捷工厂方法 ====================

    /**
     * 视频设备不存在异常
     */
    public static VideoBusinessException videoDeviceNotFound(Object deviceId) {
        return new VideoBusinessException(ErrorCode.VIDEO_DEVICE_NOT_FOUND,
            "视频设备不存在", deviceId);
    }

    /**
     * 视频设备离线异常
     */
    public static VideoBusinessException videoDeviceOffline(Object deviceId) {
        return new VideoBusinessException(ErrorCode.VIDEO_DEVICE_OFFLINE,
            "视频设备离线", deviceId);
    }

    /**
     * 视频设备故障异常
     */
    public static VideoBusinessException videoDeviceMalfunction(Object deviceId, String reason) {
        return new VideoBusinessException(ErrorCode.VIDEO_DEVICE_MALFUNCTION,
            "视频设备故障: " + reason, deviceId);
    }

    /**
     * 视频流不存在异常
     */
    public static VideoBusinessException videoStreamNotFound(Object streamId) {
        return new VideoBusinessException(ErrorCode.VIDEO_STREAM_NOT_FOUND,
            "视频流不存在", streamId);
    }

    /**
     * 视频流不可用异常
     */
    public static VideoBusinessException videoStreamUnavailable(Object streamId, String reason) {
        return new VideoBusinessException(ErrorCode.VIDEO_STREAM_UNAVAILABLE,
            "视频流不可用: " + reason, streamId);
    }

    /**
     * 视频流中断异常
     */
    public static VideoBusinessException videoStreamInterrupted(Object streamId, String reason) {
        return new VideoBusinessException(ErrorCode.VIDEO_STREAM_INTERRUPTED,
            "视频流中断: " + reason, streamId);
    }

    /**
     * 视频流带宽不足异常
     */
    public static VideoBusinessException videoStreamBandwidthInsufficient(Object deviceId, String bandwidth) {
        return new VideoBusinessException(ErrorCode.VIDEO_STREAM_BANDWIDTH_INSUFFICIENT,
            "视频流带宽不足: " + bandwidth, deviceId);
    }

    /**
     * 录制失败异常
     */
    public static VideoBusinessException recordingFailed(Object deviceId, String reason) {
        return new VideoBusinessException(ErrorCode.RECORDING_FAILED,
            "录制失败: " + reason, deviceId);
    }

    /**
     * 录制存储空间不足异常
     */
    public static VideoBusinessException recordingStorageFull(Object deviceId) {
        return new VideoBusinessException(ErrorCode.RECORDING_STORAGE_FULL,
            "录制存储空间不足", deviceId);
    }

    /**
     * 录制权限被拒绝异常
     */
    public static VideoBusinessException recordingPermissionDenied(Object userId, Object deviceId) {
        return new VideoBusinessException(ErrorCode.RECORDING_PERMISSION_DENIED,
            "录制权限被拒绝", userId + "|" + deviceId);
    }

    /**
     * 回放文件不存在异常
     */
    public static VideoBusinessException playbackFileNotFound(Object fileId) {
        return new VideoBusinessException(ErrorCode.PLAYBACK_FILE_NOT_FOUND,
            "回放文件不存在", fileId);
    }

    /**
     * 回放权限被拒绝异常
     */
    public static VideoBusinessException playbackPermissionDenied(Object userId, Object fileId) {
        return new VideoBusinessException(ErrorCode.PLAYBACK_PERMISSION_DENIED,
            "回放权限被拒绝", userId + "|" + fileId);
    }

    /**
     * 回放时间范围无效异常
     */
    public static VideoBusinessException playbackTimeRangeInvalid(String timeRange) {
        return new VideoBusinessException(ErrorCode.PLAYBACK_TIME_RANGE_INVALID,
            "回放时间范围无效: " + timeRange, timeRange);
    }

    /**
     * 人脸检测失败异常
     */
    public static VideoBusinessException faceDetectionFailed(Object deviceId, String reason) {
        return new VideoBusinessException(ErrorCode.FACE_DETECTION_FAILED,
            "人脸检测失败: " + reason, deviceId);
    }

    /**
     * 人脸识别失败异常
     */
    public static VideoBusinessException faceRecognitionFailed(Object deviceId, String reason) {
        return new VideoBusinessException(ErrorCode.FACE_RECOGNITION_FAILED,
            "人脸识别失败: " + reason, deviceId);
    }

    /**
     * 边缘AI处理失败异常
     */
    public static VideoBusinessException edgeAiProcessingFailed(Object deviceId, String reason) {
        return new VideoBusinessException(ErrorCode.EDGE_AI_PROCESSING_FAILED,
            "边缘AI处理失败: " + reason, deviceId);
    }

    /**
     * 边缘模型未加载异常
     */
    public static VideoBusinessException edgeModelNotLoaded(Object deviceId, String model) {
        return new VideoBusinessException(ErrorCode.EDGE_MODEL_NOT_LOADED,
            "边缘模型未加载: " + model, deviceId);
    }

    /**
     * 告警规则不存在异常
     */
    public static VideoBusinessException alertRuleNotFound(Object ruleId) {
        return new VideoBusinessException(ErrorCode.ALERT_RULE_NOT_FOUND,
            "告警规则不存在", ruleId);
    }

    /**
     * 告警通知失败异常
     */
    public static VideoBusinessException alertNotificationFailed(Object alertId, String reason) {
        return new VideoBusinessException(ErrorCode.ALERT_NOTIFICATION_FAILED,
            "告警通知失败: " + reason, alertId);
    }

    /**
     * 视频存储空间不足异常
     */
    public static VideoBusinessException videoStorageFull(Object storageId) {
        return new VideoBusinessException(ErrorCode.VIDEO_STORAGE_FULL,
            "视频存储空间不足", storageId);
    }

    /**
     * 网络连接失败异常
     */
    public static VideoBusinessException networkConnectionFailed(Object deviceId, String network) {
        return new VideoBusinessException(ErrorCode.NETWORK_CONNECTION_FAILED,
            "网络连接失败: " + network, deviceId);
    }

    /**
     * 网络带宽不足异常
     */
    public static VideoBusinessException networkBandwidthInsufficient(Object deviceId, String required, String available) {
        return new VideoBusinessException(ErrorCode.NETWORK_BANDWIDTH_INSUFFICIENT,
            "网络带宽不足: 需要" + required + ", 可用" + available, deviceId);
    }

    /**
     * 视频访问被拒绝异常
     */
    public static VideoBusinessException videoAccessDenied(Object userId, Object deviceId) {
        return new VideoBusinessException(ErrorCode.VIDEO_ACCESS_DENIED,
            "视频访问被拒绝", userId + "|" + deviceId);
    }

    /**
     * 违反视频隐私政策异常
     */
    public static VideoBusinessException videoPrivacyPolicyViolation(String violation) {
        return new VideoBusinessException(ErrorCode.VIDEO_PRIVACY_POLICY_VIOLATION,
            "违反视频隐私政策: " + violation, violation);
    }

    /**
     * 供应商不支持异常
     */
    public static VideoBusinessException vendorNotSupported(String vendor) {
        return new VideoBusinessException(ErrorCode.VENDOR_NOT_SUPPORTED,
            "不支持的供应商: " + vendor, vendor);
    }

    /**
     * 供应商协议不兼容异常
     */
    public static VideoBusinessException vendorProtocolIncompatible(String vendor, String protocol) {
        return new VideoBusinessException(ErrorCode.VENDOR_PROTOCOL_INCOMPATIBLE,
            "供应商协议不兼容: " + vendor + "-" + protocol, vendor);
    }

    /**
     * 参数错误异常
     */
    public static VideoBusinessException invalidParameter(String message) {
        return new VideoBusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 违反业务规则异常
     */
    public static VideoBusinessException businessRuleViolation(String message) {
        return new VideoBusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    /**
     * 批量操作失败异常
     */
    public static VideoBusinessException batchOperationFailed(String operation, String reason) {
        return new VideoBusinessException(ErrorCode.BATCH_OPERATION_FAILED,
            "批量" + operation + "失败: " + reason);
    }

    /**
     * 系统维护中异常
     */
    public static VideoBusinessException systemMaintenance() {
        return new VideoBusinessException(ErrorCode.SYSTEM_MAINTENANCE,
            "系统维护中，请稍后重试");
    }

    /**
     * 请求频率超限异常
     */
    public static VideoBusinessException rateLimitExceeded(Object deviceId) {
        return new VideoBusinessException(ErrorCode.RATE_LIMIT_EXCEEDED,
            "请求频率超限", deviceId);
    }

    /**
     * 操作超时异常
     */
    public static VideoBusinessException operationTimeout(String operation, Object deviceId) {
        return new VideoBusinessException(ErrorCode.OPERATION_TIMEOUT,
            "操作超时: " + operation, deviceId);
    }

    /**
     * 并发操作数超限异常
     */
    public static VideoBusinessException concurrentOperationLimitExceeded(Object deviceId) {
        return new VideoBusinessException(ErrorCode.CONCURRENT_OPERATION_LIMIT_EXCEEDED,
            "并发操作数超限", deviceId);
    }

    /**
     * 创建验证失败的异常
     */
    public static VideoBusinessException validationFailed(java.util.List<String> errors) {
        String message = "验证失败: " + String.join(", ", errors);
        return new VideoBusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 创建数据库操作失败的异常
     */
    public static VideoBusinessException databaseError(String operation, String details) {
        return new VideoBusinessException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "数据库操作失败: " + operation + ", 详情: " + details);
    }
}