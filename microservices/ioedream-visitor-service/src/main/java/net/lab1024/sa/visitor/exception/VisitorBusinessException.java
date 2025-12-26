package net.lab1024.sa.visitor.exception;

/**
 * 访客业务异常
 * <p>
 * 统一处理访客相关的业务异常
 * 继承自全局异常体系
 * 提供详细的错误分类和错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
public class VisitorBusinessException extends RuntimeException {

    /**
     * 异常代码枚举
     */
    public enum ErrorCode {
        // 访客预约管理异常 (1000-1099)
        VISITOR_APPOINTMENT_NOT_FOUND("VISITOR_APPOINTMENT_NOT_FOUND", "访客预约不存在"),
        VISITOR_APPOINTMENT_DUPLICATE("VISITOR_APPOINTMENT_DUPLICATE", "访客预约重复"),
        VISITOR_APPOINTMENT_INVALID("VISITOR_APPOINTMENT_INVALID", "访客预约无效"),
        VISITOR_APPOINTMENT_EXPIRED("VISITOR_APPOINTMENT_EXPIRED", "访客预约已过期"),
        VISITOR_APPOINTMENT_CANCELLED("VISITOR_APPOINTMENT_CANCELLED", "访客预约已取消"),
        VISITOR_APPOINTMENT_COMPLETED("VISITOR_APPOINTMENT_COMPLETED", "访客预约已完成"),
        VISITOR_APPOINTMENT_REJECTED("VISITOR_APPOINTMENT_REJECTED", "访客预约被拒绝"),
        VISITOR_APPOINTMENT_PENDING("VISITOR_APPOINTMENT_PENDING", "访客预约待审批"),
        VISITOR_APPOINTMENT_APPROVED("VISITOR_APPOINTMENT_APPROVED", "访客预约已批准"),
        VISITOR_APPOINTMENT_TIME_CONFLICT("VISITOR_APPOINTMENT_TIME_CONFLICT", "访客预约时间冲突"),
        VISITOR_APPOINTMENT_QUOTA_EXCEEDED("VISITOR_APPOINTMENT_QUOTA_EXCEEDED", "访客预约配额超限"),
        VISITOR_APPOINTMENT_PERIOD_INVALID("VISITOR_APPOINTMENT_PERIOD_INVALID", "访客预约期间无效"),
        VISITOR_APPOINTMENT_VISITOR_LIMIT_EXCEEDED("VISITOR_APPOINTMENT_VISITOR_LIMIT_EXCEEDED", "访客预约访客数超限"),
        VISITOR_APPOINTMENT_DURATION_INVALID("VISITOR_APPOINTMENT_DURATION_INVALID", "访客预约时长无效"),
        VISITOR_APPOINTMENT_REASON_INVALID("VISITOR_APPOINTMENT_REASON_INVALID", "访客预约原因无效"),
        VISITOR_APPOINTMENT_PURPOSE_INVALID("VISITOR_APPOINTMENT_PURPOSE_INVALID", "访客预约目的无效"),

        // 访客信息管理异常 (1100-1199)
        VISITOR_NOT_FOUND("VISITOR_NOT_FOUND", "访客不存在"),
        VISITOR_INFO_INVALID("VISITOR_INFO_INVALID", "访客信息无效"),
        VISITOR_IDENTITY_INVALID("VISITOR_IDENTITY_INVALID", "访客身份信息无效"),
        VISITOR_PHONE_INVALID("VISITOR_PHONE_INVALID", "访客电话号码无效"),
        VISITOR_EMAIL_INVALID("VISITOR_EMAIL_INVALID", "访客邮箱地址无效"),
        VISITOR_PHOTO_INVALID("VISITOR_PHOTO_INVALID", "访客照片无效"),
        VISITOR_DOCUMENT_INVALID("VISITOR_DOCUMENT_INVALID", "访客证件信息无效"),
        VISITOR_DOCUMENT_EXPIRED("VISITOR_DOCUMENT_EXPIRED", "访客证件已过期"),
        VISITOR_BLACKLISTED("VISITOR_BLACKLISTED", "访客在黑名单中"),
        BLACKLIST_VISITOR_DETECTED("BLACKLIST_VISITOR_DETECTED", "检测到黑名单访客"),
        SUSPICIOUS_BEHAVIOR_DETECTED("SUSPICIOUS_BEHAVIOR_DETECTED", "检测到可疑行为"),
        VISITOR_IDENTITY_VERIFICATION_FAILED("VISITOR_IDENTITY_VERIFICATION_FAILED", "访客身份验证失败"),
        VISITOR_VISIT_HISTORY_CONFLICT("VISITOR_VISIT_HISTORY_CONFLICT", "访客访问历史冲突"),
        VISITOR_REGISTRATION_FAILED("VISITOR_REGISTRATION_FAILED", "访客注册失败"),
        VISITOR_BIOMETRIC_REGISTRATION_FAILED("VISITOR_BIOMETRIC_REGISTRATION_FAILED", "访客生物特征注册失败"),
        VISITOR_TEMPORARY_CREDENTIAL_FAILED("VISITOR_TEMPORARY_CREDENTIAL_FAILED", "访客临时凭证生成失败"),

        // 访客审批流程异常 (1200-1299)
        APPROVAL_WORKFLOW_NOT_FOUND("APPROVAL_WORKFLOW_NOT_FOUND", "审批工作流不存在"),
        APPROVAL_WORKFLOW_INVALID("APPROVAL_WORKFLOW_INVALID", "审批工作流无效"),
        APPROVAL_WORKFLOW_INTERRUPTED("APPROVAL_WORKFLOW_INTERRUPTED", "审批工作流中断"),
        APPROVAL_WORKFLOW_TIMEOUT("APPROVAL_WORKFLOW_TIMEOUT", "审批工作流超时"),
        APPROVAL_STEP_NOT_FOUND("APPROVAL_STEP_NOT_FOUND", "审批步骤不存在"),
        APPROVAL_STEP_INVALID("APPROVAL_STEP_INVALID", "审批步骤无效"),
        APPROVAL_STEP_SKIPPED("APPROVAL_STEP_SKIPPED", "审批步骤被跳过"),
        APPROVAL_CHAIN_BROKEN("APPROVAL_CHAIN_BROKEN", "审批链断裂"),
        APPROVAL_DELEGATION_FAILED("APPROVAL_DELEGATION_FAILED", "审批授权失败"),
        APPROVAL_ESCALATION_FAILED("APPROVAL_ESCALATION_FAILED", "审批升级失败"),
        APPROVAL_CONDITION_NOT_MET("APPROVAL_CONDITION_NOT_MET", "审批条件未满足"),
        APPROVAL_POLICY_VIOLATION("APPROVAL_POLICY_VIOLATION", "违反审批政策"),
        APPROVAL_HISTORY_NOT_FOUND("APPROVAL_HISTORY_NOT_FOUND", "审批历史不存在"),
        APPROVAL_SIGNATURE_INVALID("APPROVAL_SIGNATURE_INVALID", "审批签名无效"),
        APPROVAL_DOCUMENT_MISSING("APPROVAL_DOCUMENT_MISSING", "审批文档缺失"),
        APPROVAL_COMMENT_REQUIRED("APPROVAL_COMMENT_REQUIRED", "需要审批意见"),
        APPROVER_NOT_FOUND("APPROVER_NOT_FOUND", "审批人不存在"),
        APPROVAL_CHAIN_ERROR("APPROVAL_CHAIN_ERROR", "审批链错误"),
        VISITOR_APPROVAL_ALREADY_PROCESSED("VISITOR_APPROVAL_ALREADY_PROCESSED", "访客审批已处理"),

        // 访客到达和离开管理异常 (1300-1399)
        VISITOR_ARRIVAL_NOT_REGISTERED("VISITOR_ARRIVAL_NOT_REGISTERED", "访客到达未注册"),
        VISITOR_ARRIVAL_DUPLICATE("VISITOR_ARRIVAL_DUPLICATE", "访客到达重复注册"),
        VISITOR_DEPARTURE_NOT_REGISTERED("VISITOR_DEPARTURE_NOT_REGISTERED", "访客离开未注册"),
        VISITOR_DEPARTURE_DUPLICATE("VISITOR_DEPARTURE_DUPLICATE", "访客离开重复注册"),
        VISITOR_STAY_TIME_EXCEEDED("VISITOR_STAY_TIME_EXCEEDED", "访客停留时间超限"),
        VISITOR_STAY_TIME_INVALID("VISITOR_STAY_TIME_INVALID", "访客停留时间无效"),
        VISITOR_CHECK_IN_FAILED("VISITOR_CHECK_IN_FAILED", "访客登记失败"),
        VISITOR_CHECK_OUT_FAILED("VISITOR_CHECK_OUT_FAILED", "访客签退失败"),
        VISITOR_EXIT_VALIDATION_FAILED("VISITOR_EXIT_VALIDATION_FAILED", "访客离场验证失败"),
        VISITOR_TEMPORARY_PASSES_EXCEEDED("VISITOR_TEMPORARY_PASSES_EXCEEDED", "访客临时通行证超限"),
        VISITOR_GUIDE_NOT_ASSIGNED("VISITOR_GUIDE_NOT_ASSIGNED", "访客接待人未分配"),
        VISITOR_GUIDE_UNAVAILABLE("VISITOR_GUIDE_UNAVAILABLE", "访客接待人不可用"),
        VISITOR_AREA_ACCESS_DENIED("VISITOR_AREA_ACCESS_DENIED", "访客区域访问被拒绝"),
        VISITOR_SECURITY_CHECK_FAILED("VISITOR_SECURITY_CHECK_FAILED", "访客安全检查失败"),
        VISITOR_BIOMETRIC_VERIFICATION_FAILED("VISITOR_BIOMETRIC_VERIFICATION_FAILED", "访客生物特征验证失败"),

        // 访客通行证管理异常 (1400-1499)
        VISITOR_PASS_NOT_FOUND("VISITOR_PASS_NOT_FOUND", "访客通行证不存在"),
        VISITOR_PASS_INVALID("VISITOR_PASS_INVALID", "访客通行证无效"),
        VISITOR_PASS_EXPIRED("VISITOR_PASS_EXPIRED", "访客通行证已过期"),
        VISITOR_PASS_REVOKED("VISITOR_PASS_REVOKED", "访客通行证已撤销"),
        VISITOR_PASS_GENERATION_FAILED("VISITOR_PASS_GENERATION_FAILED", "访客通行证生成失败"),
        VISITOR_PASS_PRINTING_FAILED("VISITOR_PASS_PRINTING_FAILED", "访客通行证打印失败"),
        VISITOR_PASS_ACTIVATION_FAILED("VISITOR_PASS_ACTIVATION_FAILED", "访客通行证激活失败"),
        VISITOR_PASS_DEACTIVATION_FAILED("VISITOR_PASS_DEACTIVATION_FAILED", "访客通行证停用失败"),
        VISITOR_PASS_REPLACEMENT_FAILED("VISITOR_PASS_REPLACEMENT_FAILED", "访客通行证替换失败"),
        VISITOR_PASS_QR_CODE_INVALID("VISITOR_PASS_QR_CODE_INVALID", "访客通行证二维码无效"),
        VISITOR_PASS_BARCODE_INVALID("VISITOR_PASS_BARCODE_INVALID", "访客通行证条形码无效"),
        VISITOR_PASS_RFID_INVALID("VISITOR_PASS_RFID_INVALID", "访客通行证RFID无效"),
        VISITOR_PASS_PHOTO_INVALID("VISITOR_PASS_PHOTO_INVALID", "访客通行证照片无效"),
        VISITOR_PASS_SIGNATURE_INVALID("VISITOR_PASS_SIGNATURE_INVALID", "访客通行证签名无效"),

        // 访客区域和权限管理异常 (1500-1599)
        VISITOR_AREA_NOT_FOUND("VISITOR_AREA_NOT_FOUND", "访客区域不存在"),
        VISITOR_AREA_TIME_RESTRICTION("VISITOR_AREA_TIME_RESTRICTION", "访客区域时间限制"),
        VISITOR_AREA_CAPACITY_EXCEEDED("VISITOR_AREA_CAPACITY_EXCEEDED", "访客区域容量超限"),
        VISITOR_AREA_SECURITY_LEVEL_MISMATCH("VISITOR_AREA_SECURITY_LEVEL_MISMATCH", "访客区域安全级别不匹配"),
        VISITOR_AREA_SUPERVISION_REQUIRED("VISITOR_AREA_SUPERVISION_REQUIRED", "访客区域需要监督"),
        VISITOR_AREA_PERMISSION_EXPIRED("VISITOR_AREA_PERMISSION_EXPIRED", "访客区域权限已过期"),
        VISITOR_AREA_ACCESS_CONFLICT("VISITOR_AREA_ACCESS_CONFLICT", "访客区域访问冲突"),
        VISITOR_AREA_EQUIPMENT_NOT_AVAILABLE("VISITOR_AREA_EQUIPMENT_NOT_AVAILABLE", "访客区域设备不可用"),
        VISITOR_AREA_EMERGENCY_LOCKDOWN("VISITOR_AREA_EMERGENCY_LOCKDOWN", "访客区域紧急锁定"),
        VISITOR_AREA_MAINTENANCE_MODE("VISITOR_AREA_MAINTENANCE_MODE", "访客区域维护模式"),
        VISITOR_AREA_POLICY_VIOLATION("VISITOR_AREA_POLICY_VIOLATION", "违反访客区域政策"),
        VISITOR_AREA_AUDIT_LOG_MISSING("VISITOR_AREA_AUDIT_LOG_MISSING", "访客区域审计日志缺失"),

        // 访客陪同管理异常 (1600-1699)
        VISITOR_ESCORT_NOT_FOUND("VISITOR_ESCORT_NOT_FOUND", "访客陪同人不存在"),
        VISITOR_ESCORT_UNAVAILABLE("VISITOR_ESCORT_UNAVAILABLE", "访客陪同人不可用"),
        VISITOR_ESCORT_ASSIGNMENT_CONFLICT("VISITOR_ESCORT_ASSIGNMENT_CONFLICT", "访客陪同人分配冲突"),
        VISITOR_ESCORT_TIME_LIMIT_EXCEEDED("VISITOR_ESCORT_TIME_LIMIT_EXCEEDED", "访客陪同时间超限"),
        VISITOR_ESCORT_AREA_LIMIT_EXCEEDED("VISITOR_ESCORT_AREA_LIMIT_EXCEEDED", "访客陪同区域限制超限"),
        VISITOR_ESCORT_CAPACITY_EXCEEDED("VISITOR_ESCORT_CAPACITY_EXCEEDED", "访客陪同人数超限"),
        VISITOR_ESCORT_PERMISSION_DENIED("VISITOR_ESCORT_PERMISSION_DENIED", "访客陪同权限被拒绝"),
        VISITOR_ESCORT_HANDOVER_FAILED("VISITOR_ESCORT_HANDOVER_FAILED", "访客陪同交接失败"),
        VISITOR_ESCORT_ABANDONED("VISITOR_ESCORT_ABANDONED", "访客被陪同人遗弃"),
        VISITOR_ESCORT_DEVIATION("VISITOR_ESCORT_DEVIATION", "访客偏离陪同路线"),
        VISITOR_ESCORT_EMERGENCY("VISITOR_ESCORT_EMERGENCY", "访客陪同紧急情况"),
        VISITOR_ESCORT_PROTOCOL_VIOLATION("VISITOR_ESCORT_PROTOCOL_VIOLATION", "违反访客陪同协议"),
        VISITOR_ESCORT_DOCUMENTATION_MISSING("VISITOR_ESCORT_DOCUMENTATION_MISSING", "访客陪同文档缺失"),
        VISITOR_ESCORT_AUTHENTICATION_FAILED("VISITOR_ESCORT_AUTHENTICATION_FAILED", "访客陪同认证失败"),

        // 访客访客车辆管理异常 (1700-1799)
        VISITOR_VEHICLE_NOT_FOUND("VISITOR_VEHICLE_NOT_FOUND", "访客车辆不存在"),
        VISITOR_VEHICLE_REGISTRATION_INVALID("VISITOR_VEHICLE_REGISTRATION_INVALID", "访客车辆注册信息无效"),
        VISITOR_VEHICLE_LICENSE_PLATE_INVALID("VISITOR_VEHICLE_LICENSE_PLATE_INVALID", "访客车辆车牌号无效"),
        VISITOR_VEHICLE_DOCUMENT_INVALID("VISITOR_VEHICLE_DOCUMENT_INVALID", "访客车辆证件信息无效"),
        VISITOR_VEHICLE_INSURANCE_EXPIRED("VISITOR_VEHICLE_INSURANCE_EXPIRED", "访客车辆保险已过期"),
        VISITOR_VEHICLE_INSPECTION_INVALID("VISITOR_VEHICLE_INSPECTION_INVALID", "访客车辆检验无效"),
        VISITOR_VEHICLE_ACCESS_DENIED("VISITOR_VEHICLE_ACCESS_DENIED", "访客车辆访问被拒绝"),
        VISITOR_VEHICLE_PARKING_SPACE_NOT_AVAILABLE("VISITOR_VEHICLE_PARKING_SPACE_NOT_AVAILABLE", "访客车辆停车位不可用"),
        VISITOR_VEHICLE_PARKING_TIME_EXCEEDED("VISITOR_VEHICLE_PARKING_TIME_EXCEEDED", "访客车辆停车时间超限"),
        VISITOR_VEHICLE_CHARGING_STATION_UNAVAILABLE("VISITOR_VEHICLE_CHARGING_STATION_UNAVAILABLE", "访客车辆充电站不可用"),
        VISITOR_VEHICLE_SECURITY_CHECK_FAILED("VISITOR_VEHICLE_SECURITY_CHECK_FAILED", "访客车辆安全检查失败"),
        VISITOR_VEHICLE_EVACUATION_REQUIRED("VISITOR_VEHICLE_EVACUATION_REQUIRED", "访客车辆需要疏散"),
        VISITOR_VEHICLE_GUIDANCE_FAILED("VISITOR_VEHICLE_GUIDANCE_FAILED", "访客车辆引导失败"),
        VISITOR_VEHICLE_TRACKING_FAILED("VISITOR_VEHICLE_TRACKING_FAILED", "访客车辆跟踪失败"),

        // 访客数据和报告管理异常 (1800-1899)
        VISITOR_DATA_NOT_FOUND("VISITOR_DATA_NOT_FOUND", "访客数据不存在"),
        VISITOR_DATA_CORRUPTION("VISITOR_DATA_CORRUPTION", "访客数据损坏"),
        VISITOR_DATA_BACKUP_FAILED("VISITOR_DATA_BACKUP_FAILED", "访客数据备份失败"),
        VISITOR_DATA_RESTORE_FAILED("VISITOR_DATA_RESTORE_FAILED", "访客数据恢复失败"),
        VISITOR_DATA_SYNC_FAILED("VISITOR_DATA_SYNC_FAILED", "访客数据同步失败"),
        VISITOR_REPORT_GENERATION_FAILED("VISITOR_REPORT_GENERATION_FAILED", "访客报表生成失败"),
        VISITOR_REPORT_TEMPLATE_NOT_FOUND("VISITOR_REPORT_TEMPLATE_NOT_FOUND", "访客报表模板不存在"),
        VISITOR_STATISTICS_CALCULATION_FAILED("VISITOR_STATISTICS_CALCULATION_FAILED", "访客统计计算失败"),
        VISITOR_ANALYTICS_DATA_MISSING("VISITOR_ANALYTICS_DATA_MISSING", "访客分析数据缺失"),
        VISITOR_DATA_RETENTION_POLICY_VIOLATION("VISITOR_DATA_RETENTION_POLICY_VIOLATION", "违反访客数据保留政策"),
        VISITOR_DATA_PRIVACY_VIOLATION("VISITOR_DATA_PRIVACY_VIOLATION", "违反访客数据隐私政策"),
        VISITOR_DATA_ARCHIVING_FAILED("VISITOR_DATA_ARCHIVING_FAILED", "访客数据归档失败"),
        VISITOR_DATA_PURGE_FAILED("VISITOR_DATA_PURGE_FAILED", "访客数据清理失败"),

        // 访客集成和接口异常 (1900-1999)
        EXTERNAL_SYSTEM_NOT_RESPONDING("EXTERNAL_SYSTEM_NOT_RESPONDING", "外部系统无响应"),
        EXTERNAL_SYSTEM_API_FAILED("EXTERNAL_SYSTEM_API_FAILED", "外部系统API失败"),
        EXTERNAL_SYSTEM_DATA_MISMATCH("EXTERNAL_SYSTEM_DATA_MISMATCH", "外部系统数据不匹配"),
        EXTERNAL_SYSTEM_AUTHENTICATION_FAILED("EXTERNAL_SYSTEM_AUTHENTICATION_FAILED", "外部系统认证失败"),
        EXTERNAL_SYSTEM_RATE_LIMIT_EXCEEDED("EXTERNAL_SYSTEM_RATE_LIMIT_EXCEEDED", "外部系统频率限制超限"),
        EXTERNAL_SYSTEM_CONNECTION_FAILED("EXTERNAL_SYSTEM_CONNECTION_FAILED", "外部系统连接失败"),
        EXTERNAL_SYSTEM_DATA_FORMAT_INVALID("EXTERNAL_SYSTEM_DATA_FORMAT_INVALID", "外部系统数据格式无效"),
        EXTERNAL_SYSTEM_VERSION_INCOMPATIBLE("EXTERNAL_SYSTEM_VERSION_INCOMPATIBLE", "外部系统版本不兼容"),
        EXTERNAL_SYSTEM_MAINTENANCE("EXTERNAL_SYSTEM_MAINTENANCE", "外部系统维护中"),
        EXTERNAL_SYSTEM_PERMISSION_DENIED("EXTERNAL_SYSTEM_PERMISSION_DENIED", "外部系统权限被拒绝"),
        EMAIL_NOTIFICATION_FAILED("EMAIL_NOTIFICATION_FAILED", "邮件通知失败"),
        SMS_NOTIFICATION_FAILED("SMS_NOTIFICATION_FAILED", "短信通知失败"),
        PUSH_NOTIFICATION_FAILED("PUSH_NOTIFICATION_FAILED", "推送通知失败"),
        WEBHOOK_CALLBACK_FAILED("WEBHOOK_CALLBACK_FAILED", "Webhook回调失败"),

        // 访客安全和合规异常 (2000-2099)
        VISITOR_SECURITY_BREACH_DETECTED("VISITOR_SECURITY_BREACH_DETECTED", "检测到访客安全违规"),
        VISITOR_BEHAVIOR_ANOMALY("VISITOR_BEHAVIOR_ANOMALY", "访客行为异常"),
        VISITOR_RESTRICTION_VIOLATION("VISITOR_RESTRICTION_VIOLATION", "违反访客限制"),
        VISITOR_COMPLIANCE_VIOLATION("VISITOR_COMPLIANCE_VIOLATION", "违反访客合规要求"),
        VISITOR_AUDIT_FINDING("VISITOR_AUDIT_FINDING", "访客审计发现"),
        VISITOR_REGULATORY_VIOLATION("VISITOR_REGULATORY_VIOLATION", "违反访客监管要求"),
        VISITOR_DATA_PROTECTION_VIOLATION("VISITOR_DATA_PROTECTION_VIOLATION", "违反访客数据保护规定"),
        VISITOR_ACCESS_LOG_MISSING("VISITOR_ACCESS_LOG_MISSING", "访客访问日志缺失"),
        VISITOR_INCIDENT_REPORTING_FAILED("VISITOR_INCIDENT_REPORTING_FAILED", "访客事故报告失败"),
        VISITOR_EMERGENCY_RESPONSE_FAILED("VISITOR_EMERGENCY_RESPONSE_FAILED", "访客应急响应失败"),
        VISITOR_SECURITY_POLICY_VIOLATION("VISITOR_SECURITY_POLICY_VIOLATION", "违反访客安全政策"),
        VISITOR_BACKGROUND_CHECK_FAILED("VISITOR_BACKGROUND_CHECK_FAILED", "访客背景检查失败"),
        VISITOR_RISK_ASSESSMENT_FAILED("VISITOR_RISK_ASSESSMENT_FAILED", "访客风险评估失败"),

        // 通用业务异常 (2100-2199)
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

    public VisitorBusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null);
    }

    public VisitorBusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public VisitorBusinessException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public VisitorBusinessException(ErrorCode errorCode, String message, Object businessId) {
        super(message);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    public VisitorBusinessException(ErrorCode errorCode, String message, Object businessId, Throwable cause) {
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
    public VisitorBusinessException(String code, String message) {
        super(message);
        this.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
        this.businessId = code;
    }

    /**
     * 兼容性构造函数（仅消息）
     */
    public VisitorBusinessException(String message) {
        this("UNKNOWN_ERROR", message);
    }

    // ==================== 便捷工厂方法 ====================

    /**
     * 访客预约不存在异常
     */
    public static VisitorBusinessException visitorAppointmentNotFound(Object appointmentId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_APPOINTMENT_NOT_FOUND,
            "访客预约不存在", appointmentId);
    }

    /**
     * 访客预约重复异常
     */
    public static VisitorBusinessException visitorAppointmentDuplicate(Object appointmentId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_APPOINTMENT_DUPLICATE,
            "访客预约重复", appointmentId);
    }

    /**
     * 访客预约已过期异常
     */
    public static VisitorBusinessException visitorAppointmentExpired(Object appointmentId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_APPOINTMENT_EXPIRED,
            "访客预约已过期", appointmentId);
    }

    /**
     * 访客预约时间冲突异常
     */
    public static VisitorBusinessException visitorAppointmentTimeConflict(String time) {
        return new VisitorBusinessException(ErrorCode.VISITOR_APPOINTMENT_TIME_CONFLICT,
            "访客预约时间冲突: " + time, time);
    }

    /**
     * 访客不存在异常
     */
    public static VisitorBusinessException visitorNotFound(Object visitorId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_NOT_FOUND,
            "访客不存在", visitorId);
    }

    /**
     * 访客信息无效异常
     */
    public static VisitorBusinessException visitorInfoInvalid(String field, String value) {
        return new VisitorBusinessException(ErrorCode.VISITOR_INFO_INVALID,
            "访客信息无效: " + field + "=" + value, field);
    }

    /**
     * 访客证件信息无效异常
     */
    public static VisitorBusinessException visitorDocumentInvalid(String document) {
        return new VisitorBusinessException(ErrorCode.VISITOR_DOCUMENT_INVALID,
            "访客证件信息无效", document);
    }

    /**
     * 访客在黑名单中异常
     */
    public static VisitorBusinessException visitorBlacklisted(Object visitorId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_BLACKLISTED,
            "访客在黑名单中", visitorId);
    }

    /**
     * 审批工作流不存在异常
     */
    public static VisitorBusinessException approvalWorkflowNotFound(Object workflowId) {
        return new VisitorBusinessException(ErrorCode.APPROVAL_WORKFLOW_NOT_FOUND,
            "审批工作流不存在", workflowId);
    }

    /**
     * 审批工作流超时异常
     */
    public static VisitorBusinessException approvalWorkflowTimeout(Object workflowId) {
        return new VisitorBusinessException(ErrorCode.APPROVAL_WORKFLOW_TIMEOUT,
            "审批工作流超时", workflowId);
    }

    /**
     * 需要审批意见异常
     */
    public static VisitorBusinessException approvalCommentRequired() {
        return new VisitorBusinessException(ErrorCode.APPROVAL_COMMENT_REQUIRED,
            "需要审批意见");
    }

    /**
     * 访客到达未注册异常
     */
    public static VisitorBusinessException visitorArrivalNotRegistered(Object visitorId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_ARRIVAL_NOT_REGISTERED,
            "访客到达未注册", visitorId);
    }

    /**
     * 访客停留时间超限异常
     */
    public static VisitorBusinessException visitorStayTimeExceeded(Object visitorId, String stayTime) {
        return new VisitorBusinessException(ErrorCode.VISITOR_STAY_TIME_EXCEEDED,
            "访客停留时间超限: " + stayTime, visitorId);
    }

    /**
     * 访客登记失败异常
     */
    public static VisitorBusinessException visitorCheckInFailed(Object visitorId, String reason) {
        return new VisitorBusinessException(ErrorCode.VISITOR_CHECK_IN_FAILED,
            "访客登记失败: " + reason, visitorId);
    }

    /**
     * 访客签退失败异常
     */
    public static VisitorBusinessException visitorCheckOutFailed(Object visitorId, String reason) {
        return new VisitorBusinessException(ErrorCode.VISITOR_CHECK_OUT_FAILED,
            "访客签退失败: " + reason, visitorId);
    }

    /**
     * 访客通行证不存在异常
     */
    public static VisitorBusinessException visitorPassNotFound(Object passId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_PASS_NOT_FOUND,
            "访客通行证不存在", passId);
    }

    /**
     * 访客通行证已过期异常
     */
    public static VisitorBusinessException visitorPassExpired(Object passId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_PASS_EXPIRED,
            "访客通行证已过期", passId);
    }

    /**
     * 访客区域访问被拒绝异常
     */
    public static VisitorBusinessException visitorAreaAccessDenied(Object visitorId, Object areaId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_AREA_ACCESS_DENIED,
            "访客区域访问被拒绝", visitorId + "|" + areaId);
    }

    /**
     * 访客区域时间限制异常
     */
    public static VisitorBusinessException visitorAreaTimeRestriction(Object visitorId, Object areaId, String time) {
        return new VisitorBusinessException(ErrorCode.VISITOR_AREA_TIME_RESTRICTION,
            "访客区域时间限制: " + time, visitorId + "|" + areaId);
    }

    /**
     * 访客陪同人不存在异常
     */
    public static VisitorBusinessException visitorEscortNotFound(Object escortId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_ESCORT_NOT_FOUND,
            "访客陪同人不存在", escortId);
    }

    /**
     * 访客陪同人不可用异常
     */
    public static VisitorBusinessException visitorEscortUnavailable(Object escortId, String reason) {
        return new VisitorBusinessException(ErrorCode.VISITOR_ESCORT_UNAVAILABLE,
            "访客陪同人不可用: " + reason, escortId);
    }

    /**
     * 访客车辆不存在异常
     */
    public static VisitorBusinessException visitorVehicleNotFound(Object vehicleId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_VEHICLE_NOT_FOUND,
            "访客车辆不存在", vehicleId);
    }

    /**
     * 访客车辆访问被拒绝异常
     */
    public static VisitorBusinessException visitorVehicleAccessDenied(Object vehicleId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_VEHICLE_ACCESS_DENIED,
            "访客车辆访问被拒绝", vehicleId);
    }

    /**
     * 访客数据不存在异常
     */
    public static VisitorBusinessException visitorDataNotFound(String dataType, Object dataId) {
        return new VisitorBusinessException(ErrorCode.VISITOR_DATA_NOT_FOUND,
            "访客数据不存在: " + dataType + "-" + dataId, dataType + "|" + dataId);
    }

    /**
     * 访客报表生成失败异常
     */
    public static VisitorBusinessException visitorReportGenerationFailed(String reportType, String reason) {
        return new VisitorBusinessException(ErrorCode.VISITOR_REPORT_GENERATION_FAILED,
            "访客报表生成失败: " + reportType + ", 原因: " + reason, reportType);
    }

    /**
     * 邮件通知失败异常
     */
    public static VisitorBusinessException emailNotificationFailed(String recipient, String reason) {
        return new VisitorBusinessException(ErrorCode.EMAIL_NOTIFICATION_FAILED,
            "邮件通知失败: " + recipient + ", 原因: " + reason, recipient);
    }

    /**
     * 短信通知失败异常
     */
    public static VisitorBusinessException smsNotificationFailed(String recipient, String reason) {
        return new VisitorBusinessException(ErrorCode.SMS_NOTIFICATION_FAILED,
            "短信通知失败: " + recipient + ", 原因: " + reason, recipient);
    }

    /**
     * 检测到访客安全违规异常
     */
    public static VisitorBusinessException visitorSecurityBreachDetected(Object visitorId, String violation) {
        return new VisitorBusinessException(ErrorCode.VISITOR_SECURITY_BREACH_DETECTED,
            "检测到访客安全违规: " + violation, visitorId);
    }

    /**
     * 访客行为异常异常
     */
    public static VisitorBusinessException visitorBehaviorAnomaly(Object visitorId, String anomaly) {
        return new VisitorBusinessException(ErrorCode.VISITOR_BEHAVIOR_ANOMALY,
            "访客行为异常: " + anomaly, visitorId);
    }

    /**
     * 违反访客限制异常
     */
    public static VisitorBusinessException visitorRestrictionViolation(Object visitorId, String restriction) {
        return new VisitorBusinessException(ErrorCode.VISITOR_RESTRICTION_VIOLATION,
            "违反访客限制: " + restriction, visitorId);
    }

    /**
     * 外部系统无响应异常
     */
    public static VisitorBusinessException externalSystemNotResponding(String systemName) {
        return new VisitorBusinessException(ErrorCode.EXTERNAL_SYSTEM_NOT_RESPONDING,
            "外部系统无响应: " + systemName, systemName);
    }

    /**
     * 外部系统API失败异常
     */
    public static VisitorBusinessException externalSystemApiFailed(String systemName, String api) {
        return new VisitorBusinessException(ErrorCode.EXTERNAL_SYSTEM_API_FAILED,
            "外部系统API失败: " + systemName + "-" + api, systemName + "|" + api);
    }

    /**
     * 参数错误异常
     */
    public static VisitorBusinessException invalidParameter(String message) {
        return new VisitorBusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 违反业务规则异常
     */
    public static VisitorBusinessException businessRuleViolation(String message) {
        return new VisitorBusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    /**
     * 批量操作失败异常
     */
    public static VisitorBusinessException batchOperationFailed(String operation, String reason) {
        return new VisitorBusinessException(ErrorCode.BATCH_OPERATION_FAILED,
            "批量" + operation + "失败: " + reason);
    }

    /**
     * 系统维护中异常
     */
    public static VisitorBusinessException systemMaintenance() {
        return new VisitorBusinessException(ErrorCode.SYSTEM_MAINTENANCE,
            "系统维护中，请稍后重试");
    }

    /**
     * 请求频率超限异常
     */
    public static VisitorBusinessException rateLimitExceeded(String client) {
        return new VisitorBusinessException(ErrorCode.RATE_LIMIT_EXCEEDED,
            "请求频率超限", client);
    }

    /**
     * 操作超时异常
     */
    public static VisitorBusinessException operationTimeout(String operation, String timeout) {
        return new VisitorBusinessException(ErrorCode.OPERATION_TIMEOUT,
            "操作超时: " + operation + "-" + timeout, operation);
    }

    /**
     * 创建验证失败的异常
     */
    public static VisitorBusinessException validationFailed(java.util.List<String> errors) {
        String message = "验证失败: " + String.join(", ", errors);
        return new VisitorBusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 创建数据库操作失败的异常
     */
    public static VisitorBusinessException databaseError(String operation, String details) {
        return new VisitorBusinessException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "数据库操作失败: " + operation + ", 详情: " + details);
    }
}