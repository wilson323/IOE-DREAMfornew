package net.lab1024.sa.attendance.exception;

/**
 * 考勤业务异常
 * <p>
 * 统一处理考勤相关的业务异常
 * 继承自全局异常体系
 * 提供详细的错误分类和错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
public class AttendanceBusinessException extends RuntimeException {

    /**
     * 异常代码枚举
     */
    public enum ErrorCode {
        // 考勤记录异常 (1000-1099)
        ATTENDANCE_RECORD_NOT_FOUND("ATTENDANCE_RECORD_NOT_FOUND", "考勤记录不存在"),
        ATTENDANCE_RECORD_DUPLICATE("ATTENDANCE_RECORD_DUPLICATE", "考勤记录重复"),
        ATTENDANCE_RECORD_INVALID("ATTENDANCE_RECORD_INVALID", "考勤记录无效"),
        ATTENDANCE_TIME_INVALID("ATTENDANCE_TIME_INVALID", "考勤时间无效"),
        ATTENDANCE_LOCATION_INVALID("ATTENDANCE_LOCATION_INVALID", "考勤位置无效"),
        ATTENDANCE_DEVICE_NOT_FOUND("ATTENDANCE_DEVICE_NOT_FOUND", "考勤设备不存在"),
        ATTENDANCE_DEVICE_OFFLINE("ATTENDANCE_DEVICE_OFFLINE", "考勤设备离线"),

        // 考勤状态异常 (1100-1199)
        CLOCK_IN_FAILED("CLOCK_IN_FAILED", "打卡失败"),
        CLOCK_OUT_FAILED("CLOCK_OUT_FAILED", "签退失败"),
        CLOCK_IN_ALREADY("CLOCK_IN_ALREADY", "已打卡"),
        CLOCK_OUT_ALREADY("CLOCK_OUT_ALREADY", "已签退"),
        CLOCK_IN_REQUIRED("CLOCK_IN_REQUIRED", "需要先打卡"),
        CLOCK_OUT_REQUIRED("CLOCK_OUT_REQUIRED", "需要先签退"),
        CLOCK_IN_OUT_MISMATCH("CLOCK_IN_OUT_MISMATCH", "打卡签退不匹配"),
        MULTIPLE_CLOCK_IN("MULTIPLE_CLOCK_IN", "多次打卡"),
        CLOCK_TIME_RESTRICTION("CLOCK_TIME_RESTRICTION", "打卡时间限制"),

        // 班次管理异常 (1200-1299)
        SCHEDULE_NOT_FOUND("SCHEDULE_NOT_FOUND", "班次不存在"),
        SCHEDULE_CONFLICT("SCHEDULE_CONFLICT", "班次冲突"),
        SCHEDULE_INVALID("SCHEDULE_INVALID", "班次无效"),
        SCHEDULE_NOT_ASSIGNED("SCHEDULE_NOT_ASSIGNED", "未分配班次"),
        SCHEDULE_EXPIRED("SCHEDULE_EXPIRED", "班次已过期"),
        SCHEDULE_NOT_EFFECTIVE("SCHEDULE_NOT_EFFECTIVE", "班次未生效"),
        SCHEDULE_TEMPLATE_NOT_FOUND("SCHEDULE_TEMPLATE_NOT_FOUND", "班次模板不存在"),
        WORK_SHIFT_NOT_FOUND("WORK_SHIFT_NOT_FOUND", "工作班次不存在"),
        WORK_TIME_INVALID("WORK_TIME_INVALID", "工作时间无效"),

        // 考勤统计异常 (1300-1399)
        STATISTICS_DATA_NOT_FOUND("STATISTICS_DATA_NOT_FOUND", "统计数据不存在"),
        STATISTICS_PERIOD_INVALID("STATISTICS_PERIOD_INVALID", "统计期间无效"),
        STATISTICS_CALCULATION_FAILED("STATISTICS_CALCULATION_FAILED", "统计计算失败"),
        REPORT_GENERATION_FAILED("REPORT_GENERATION_FAILED", "报表生成失败"),
        DATA_AGGREGATION_ERROR("DATA_AGGREGATION_ERROR", "数据聚合错误"),
        ABNORMAL_ATTENDANCE_DETECTED("ABNORMAL_ATTENDANCE_DETECTED", "检测到异常考勤"),

        // 请假管理异常 (1400-1499)
        LEAVE_APPLICATION_NOT_FOUND("LEAVE_APPLICATION_NOT_FOUND", "请假申请不存在"),
        LEAVE_APPLICATION_DUPLICATE("LEAVE_APPLICATION_DUPLICATE", "重复请假申请"),
        LEAVE_BALANCE_INSUFFICIENT("LEAVE_BALANCE_INSUFFICIENT", "假期余额不足"),
        LEAVE_PERIOD_INVALID("LEAVE_PERIOD_INVALID", "请假期间无效"),
        LEAVE_OVERLAP("LEAVE_OVERLAP", "请假期间重叠"),
        LEAVE_NOT_APPROVED("LEAVE_NOT_APPROVED", "请假未审批"),
        LEAVE_ALREADY_USED("LEAVE_ALREADY_USED", "假期已使用"),
        LEAVE_TYPE_NOT_FOUND("LEAVE_TYPE_NOT_FOUND", "请假类型不存在"),
        LEAVE_POLICY_VIOLATION("LEAVE_POLICY_VIOLATION", "违反请假政策"),
        LEAVE_CANCELLATION_FAILED("LEAVE_CANCELLATION_FAILED", "请假取消失败"),

        // 出差管理异常 (1500-1599)
        TRIP_APPLICATION_NOT_FOUND("TRIP_APPLICATION_NOT_FOUND", "出差申请不存在"),
        TRIP_APPLICATION_DUPLICATE("TRIP_APPLICATION_DUPLICATE", "重复出差申请"),
        TRIP_PERIOD_INVALID("TRIP_PERIOD_INVALID", "出差期间无效"),
        TRIP_NOT_APPROVED("TRIP_NOT_APPROVED", "出差未审批"),
        TRIP_ALREADY_USED("TRIP_ALREADY_USED", "出差已使用"),
        TRIP_TYPE_NOT_FOUND("TRIP_TYPE_NOT_FOUND", "出差类型不存在"),
        TRIP_POLICY_VIOLATION("TRIP_POLICY_VIOLATION", "违反出差政策"),
        TRIP_CANCELLATION_FAILED("TRIP_CANCELLATION_FAILED", "出差取消失败"),

        // 加班管理异常 (1600-1699)
        OVERTIME_APPLICATION_NOT_FOUND("OVERTIME_APPLICATION_NOT_FOUND", "加班申请不存在"),
        OVERTIME_APPLICATION_DUPLICATE("OVERTIME_APPLICATION_DUPLICATE", "重复加班申请"),
        OVERTIME_PERIOD_INVALID("OVERTIME_PERIOD_INVALID", "加班期间无效"),
        OVERTIME_NOT_APPROVED("OVERTIME_NOT_APPROVED", "加班未审批"),
        OVERTIME_ALREADY_USED("OVERTIME_ALREADY_USED", "加班已使用"),
        OVERTIME_LIMIT_EXCEEDED("OVERTIME_LIMIT_EXCEEDED", "加班时长超限"),
        OVERTIME_POLICY_VIOLATION("OVERTIME_POLICY_VIOLATION", "违反加班政策"),
        OVERTIME_CANCELLATION_FAILED("OVERTIME_CANCELLATION_FAILED", "加班取消失败"),

        // 补卡管理异常 (1700-1799)
        SUPPLEMENT_APPLICATION_NOT_FOUND("SUPPLEMENT_APPLICATION_NOT_FOUND", "补卡申请不存在"),
        SUPPLEMENT_APPLICATION_DUPLICATE("SUPPLEMENT_APPLICATION_DUPLICATE", "重复补卡申请"),
        SUPPLEMENT_PERIOD_INVALID("SUPPLEMENT_PERIOD_INVALID", "补卡期间无效"),
        SUPPLEMENT_NOT_APPROVED("SUPPLEMENT_NOT_APPROVED", "补卡未审批"),
        SUPPLEMENT_ALREADY_USED("SUPPLEMENT_ALREADY_USED", "补卡已使用"),
        SUPPLEMENT_POLICY_VIOLATION("SUPPLEMENT_POLICY_VIOLATION", "违反补卡政策"),
        SUPPLEMENT_REASON_INVALID("SUPPLEMENT_REASON_INVALID", "补卡原因无效"),
        SUPPLEMENT_LIMIT_EXCEEDED("SUPPLEMENT_LIMIT_EXCEEDED", "补卡次数超限"),
        SUPPLEMENT_CANCELLATION_FAILED("SUPPLEMENT_CANCELLATION_FAILED", "补卡取消失败"),

        // 排班管理异常 (1800-1899)
        SCHEDULING_CONFLICT("SCHEDULING_CONFLICT", "排班冲突"),
        SCHEDULING_RULE_VIOLATION("SCHEDULING_RULE_VIOLATION", "违反排班规则"),
        SCHEDULING_ALGORITHM_FAILED("SCHEDULING_ALGORITHM_FAILED", "排班算法失败"),
        SCHEDULING_CONSTRAINT_INVALID("SCHEDULING_CONSTRAINT_INVALID", "排班约束无效"),
        SCHEDULING_PREFERENCE_CONFLICT("SCHEDULING_PREFERENCE_CONFLICT", "排班偏好冲突"),
        SCHEDULING_FAIRNESS_VIOLATION("SCHEDULING_FAIRNESS_VIOLATION", "排班公平性违规"),
        SCHEDULING_GENERATION_FAILED("SCHEDULING_GENERATION_FAILED", "排班生成失败"),
        SCHEDULING_OPTIMIZATION_FAILED("SCHEDULING_OPTIMIZATION_FAILED", "排班优化失败"),

        // 考勤规则异常 (1900-1999)
        ATTENDANCE_RULE_NOT_FOUND("ATTENDANCE_RULE_NOT_FOUND", "考勤规则不存在"),
        ATTENDANCE_RULE_INVALID("ATTENDANCE_RULE_INVALID", "考勤规则无效"),
        ATTENDANCE_POLICY_VIOLATION("ATTENDANCE_POLICY_VIOLATION", "违反考勤政策"),
        FLEXIBLE_TIME_RULE_VIOLATION("FLEXIBLE_TIME_RULE_VIOLATION", "违反弹性时间规则"),
        OVERTIME_RULE_VIOLATION("OVERTIME_RULE_VIOLATION", "违反加班规则"),
        HOLIDAY_RULE_VIOLATION("HOLIDAY_RULE_VIOLATION", "违反节假日规则"),
        LATE_RULE_VIOLATION("LATE_RULE_VIOLATION", "违反迟到规则"),
        ABSENTEEISM_RULE_VIOLATION("ABSENTEEISM_RULE_VIOLATION", "违反缺勤规则"),

        // 移动考勤异常 (2000-2099)
        MOBILE_LOCATION_INVALID("MOBILE_LOCATION_INVALID", "移动位置无效"),
        MOBILE_LOCATION_PERMISSION_DENIED("MOBILE_LOCATION_PERMISSION_DENIED", "移动位置权限被拒绝"),
        MOBILE_GPS_SIGNAL_WEAK("MOBILE_GPS_SIGNAL_WEAK", "移动GPS信号弱"),
        MOBILE_NETWORK_ERROR("MOBILE_NETWORK_ERROR", "移动网络错误"),
        MOBILE_DEVICE_NOT_AUTHORIZED("MOBILE_DEVICE_NOT_AUTHORIZED", "移动设备未授权"),
        MOBILE_BIOMETRIC_FAILED("MOBILE_BIOMETRIC_FAILED", "移动生物特征验证失败"),
        GEO_FENCE_VIOLATION("GEO_FENCE_VIOLATION", "地理围栏违规"),

        // 数据同步异常 (2100-2199)
        DATA_SYNC_FAILED("DATA_SYNC_FAILED", "数据同步失败"),
        DATA_SYNC_CONFLICT("DATA_SYNC_CONFLICT", "数据同步冲突"),
        DEVICE_DATA_INCONSISTENT("DEVICE_DATA_INCONSISTENT", "设备数据不一致"),
        REAL_TIME_SYNC_FAILED("REAL_TIME_SYNC_FAILED", "实时同步失败"),
        BATCH_SYNC_FAILED("BATCH_SYNC_FAILED", "批量同步失败"),
        SYNC_TIMEOUT("SYNC_TIMEOUT", "同步超时"),

        // 通用业务异常 (2200-2299)
        INVALID_PARAMETER("INVALID_PARAMETER", "参数错误"),
        OPERATION_NOT_SUPPORTED("OPERATION_NOT_SUPPORTED", "不支持的操作"),
        BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "违反业务规则"),
        BATCH_OPERATION_FAILED("BATCH_OPERATION_FAILED", "批量操作失败"),
        SYSTEM_MAINTENANCE("SYSTEM_MAINTENANCE", "系统维护中"),
        RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "请求频率超限"),
        DATA_CONSISTENCY_ERROR("DATA_CONSISTENCY_ERROR", "数据一致性错误"),
        ATTENDANCE_PERIOD_CLOSED("ATTENDANCE_PERIOD_CLOSED", "考勤期间已关闭");

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

    public AttendanceBusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null);
    }

    public AttendanceBusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public AttendanceBusinessException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public AttendanceBusinessException(ErrorCode errorCode, String message, Object businessId) {
        super(message);
        this.errorCode = errorCode;
        this.businessId = businessId;
    }

    public AttendanceBusinessException(ErrorCode errorCode, String message, Object businessId, Throwable cause) {
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
    public AttendanceBusinessException(String code, String message) {
        super(message);
        this.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
        this.businessId = code;
    }

    /**
     * 兼容性构造函数（仅消息）
     */
    public AttendanceBusinessException(String message) {
        this("UNKNOWN_ERROR", message);
    }

    // ==================== 便捷工厂方法 ====================

    /**
     * 考勤记录不存在异常
     */
    public static AttendanceBusinessException recordNotFound(Object recordId) {
        return new AttendanceBusinessException(ErrorCode.ATTENDANCE_RECORD_NOT_FOUND,
            "考勤记录不存在", recordId);
    }

    /**
     * 考勤记录重复异常
     */
    public static AttendanceBusinessException recordDuplicate(Object userId, String date) {
        return new AttendanceBusinessException(ErrorCode.ATTENDANCE_RECORD_DUPLICATE,
            "考勤记录重复", userId + "|" + date);
    }

    /**
     * 打卡失败异常
     */
    public static AttendanceBusinessException clockInFailed(Object userId, String reason) {
        return new AttendanceBusinessException(ErrorCode.CLOCK_IN_FAILED,
            "打卡失败: " + reason, userId);
    }

    /**
     * 签退失败异常
     */
    public static AttendanceBusinessException clockOutFailed(Object userId, String reason) {
        return new AttendanceBusinessException(ErrorCode.CLOCK_OUT_FAILED,
            "签退失败: " + reason, userId);
    }

    /**
     * 已打卡异常
     */
    public static AttendanceBusinessException clockInAlready(Object userId, String date) {
        return new AttendanceBusinessException(ErrorCode.CLOCK_IN_ALREADY,
            "已打卡: " + date, userId);
    }

    /**
     * 已签退异常
     */
    public static AttendanceBusinessException clockOutAlready(Object userId, String date) {
        return new AttendanceBusinessException(ErrorCode.CLOCK_OUT_ALREADY,
            "已签退: " + date, userId);
    }

    /**
     * 班次不存在异常
     */
    public static AttendanceBusinessException scheduleNotFound(Object scheduleId) {
        return new AttendanceBusinessException(ErrorCode.SCHEDULE_NOT_FOUND,
            "班次不存在", scheduleId);
    }

    /**
     * 班次冲突异常
     */
    public static AttendanceBusinessException scheduleConflict(Object userId, Object scheduleId) {
        return new AttendanceBusinessException(ErrorCode.SCHEDULE_CONFLICT,
            "班次冲突", userId + "|" + scheduleId);
    }

    /**
     * 未分配班次异常
     */
    public static AttendanceBusinessException scheduleNotAssigned(Object userId) {
        return new AttendanceBusinessException(ErrorCode.SCHEDULE_NOT_ASSIGNED,
            "未分配班次", userId);
    }

    /**
     * 请假申请不存在异常
     */
    public static AttendanceBusinessException leaveApplicationNotFound(Object applicationId) {
        return new AttendanceBusinessException(ErrorCode.LEAVE_APPLICATION_NOT_FOUND,
            "请假申请不存在", applicationId);
    }

    /**
     * 假期余额不足异常
     */
    public static AttendanceBusinessException leaveBalanceInsufficient(Object userId, String leaveType) {
        return new AttendanceBusinessException(ErrorCode.LEAVE_BALANCE_INSUFFICIENT,
            "假期余额不足: " + leaveType, userId);
    }

    /**
     * 请假期间重叠异常
     */
    public static AttendanceBusinessException leaveOverlap(Object userId, String period) {
        return new AttendanceBusinessException(ErrorCode.LEAVE_OVERLAP,
            "请假期间重叠: " + period, userId);
    }

    /**
     * 出差申请不存在异常
     */
    public static AttendanceBusinessException tripApplicationNotFound(Object applicationId) {
        return new AttendanceBusinessException(ErrorCode.TRIP_APPLICATION_NOT_FOUND,
            "出差申请不存在", applicationId);
    }

    /**
     * 加班申请不存在异常
     */
    public static AttendanceBusinessException overtimeApplicationNotFound(Object applicationId) {
        return new AttendanceBusinessException(ErrorCode.OVERTIME_APPLICATION_NOT_FOUND,
            "加班申请不存在", applicationId);
    }

    /**
     * 加班时长超限异常
     */
    public static AttendanceBusinessException overtimeLimitExceeded(Object userId, String reason) {
        return new AttendanceBusinessException(ErrorCode.OVERTIME_LIMIT_EXCEEDED,
            "加班时长超限: " + reason, userId);
    }

    /**
     * 补卡申请不存在异常
     */
    public static AttendanceBusinessException supplementApplicationNotFound(Object applicationId) {
        return new AttendanceBusinessException(ErrorCode.SUPPLEMENT_APPLICATION_NOT_FOUND,
            "补卡申请不存在", applicationId);
    }

    /**
     * 补卡次数超限异常
     */
    public static AttendanceBusinessException supplementLimitExceeded(Object userId) {
        return new AttendanceBusinessException(ErrorCode.SUPPLEMENT_LIMIT_EXCEEDED,
            "补卡次数超限", userId);
    }

    /**
     * 排班冲突异常
     */
    public static AttendanceBusinessException schedulingConflict(Object userId, String date) {
        return new AttendanceBusinessException(ErrorCode.SCHEDULING_CONFLICT,
            "排班冲突", userId + "|" + date);
    }

    /**
     * 排班生成失败异常
     */
    public static AttendanceBusinessException schedulingGenerationFailed(String period, String reason) {
        return new AttendanceBusinessException(ErrorCode.SCHEDULING_GENERATION_FAILED,
            "排班生成失败: " + period + ", 原因: " + reason, period);
    }

    /**
     * 考勤规则不存在异常
     */
    public static AttendanceBusinessException attendanceRuleNotFound(Object ruleId) {
        return new AttendanceBusinessException(ErrorCode.ATTENDANCE_RULE_NOT_FOUND,
            "考勤规则不存在", ruleId);
    }

    /**
     * 违反考勤政策异常
     */
    public static AttendanceBusinessException attendancePolicyViolation(Object userId, String violation) {
        return new AttendanceBusinessException(ErrorCode.ATTENDANCE_POLICY_VIOLATION,
            "违反考勤政策: " + violation, userId);
    }

    /**
     * 违反弹性时间规则异常
     */
    public static AttendanceBusinessException flexibleTimeRuleViolation(Object userId, String reason) {
        return new AttendanceBusinessException(ErrorCode.FLEXIBLE_TIME_RULE_VIOLATION,
            "违反弹性时间规则: " + reason, userId);
    }

    /**
     * 移动位置无效异常
     */
    public static AttendanceBusinessException mobileLocationInvalid(Object userId, String location) {
        return new AttendanceBusinessException(ErrorCode.MOBILE_LOCATION_INVALID,
            "移动位置无效: " + location, userId);
    }

    /**
     * 地理围栏违规异常
     */
    public static AttendanceBusinessException geoFenceViolation(Object userId, String location) {
        return new AttendanceBusinessException(ErrorCode.GEO_FENCE_VIOLATION,
            "地理围栏违规: " + location, userId);
    }

    /**
     * 数据同步失败异常
     */
    public static AttendanceBusinessException dataSyncFailed(String dataType, String reason) {
        return new AttendanceBusinessException(ErrorCode.DATA_SYNC_FAILED,
            "数据同步失败: " + dataType + ", 原因: " + reason, dataType);
    }

    /**
     * 设备数据不一致异常
     */
    public static AttendanceBusinessException deviceDataInconsistent(Object deviceId, String conflict) {
        return new AttendanceBusinessException(ErrorCode.DEVICE_DATA_INCONSISTENT,
            "设备数据不一致: " + conflict, deviceId);
    }

    /**
     * 考勤期间已关闭异常
     */
    public static AttendanceBusinessException attendancePeriodClosed(String period) {
        return new AttendanceBusinessException(ErrorCode.ATTENDANCE_PERIOD_CLOSED,
            "考勤期间已关闭: " + period, period);
    }

    /**
     * 检测到异常考勤异常
     */
    public static AttendanceBusinessException abnormalAttendanceDetected(Object userId, String abnormality) {
        return new AttendanceBusinessException(ErrorCode.ABNORMAL_ATTENDANCE_DETECTED,
            "检测到异常考勤: " + abnormality, userId);
    }

    /**
     * 参数错误异常
     */
    public static AttendanceBusinessException invalidParameter(String message) {
        return new AttendanceBusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 违反业务规则异常
     */
    public static AttendanceBusinessException businessRuleViolation(String message) {
        return new AttendanceBusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    /**
     * 批量操作失败异常
     */
    public static AttendanceBusinessException batchOperationFailed(String operation, String reason) {
        return new AttendanceBusinessException(ErrorCode.BATCH_OPERATION_FAILED,
            "批量" + operation + "失败: " + reason);
    }

    /**
     * 系统维护中异常
     */
    public static AttendanceBusinessException systemMaintenance() {
        return new AttendanceBusinessException(ErrorCode.SYSTEM_MAINTENANCE,
            "系统维护中，请稍后重试");
    }

    /**
     * 创建验证失败的异常
     */
    public static AttendanceBusinessException validationFailed(java.util.List<String> errors) {
        String message = "验证失败: " + String.join(", ", errors);
        return new AttendanceBusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * 创建数据库操作失败的异常
     */
    public static AttendanceBusinessException databaseError(String operation, String details) {
        return new AttendanceBusinessException(ErrorCode.OPERATION_NOT_SUPPORTED,
            "数据库操作失败: " + operation + ", 详情: " + details);
    }
}