package net.lab1024.sa.attendance.realtime.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 考勤事件
 * <p>
 * 封装考勤相关的事件信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEvent {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 事件类型
     */
    private AttendanceEventType eventType;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 考勤设备ID
     */
    private Long deviceId;

    /**
     * 考勤设备名称
     */
    private String deviceName;

    /**
     * 考勤位置
     */
    private String attendanceLocation;

    /**
     * 考勤方式
     */
    private AttendanceMethod attendanceMethod;

    /**
     * 生物识别信息
     */
    private BiometricInfo biometricInfo;

    /**
     * 考勤记录ID（如果存在）
     */
    private Long attendanceRecordId;

    /**
     * 班次ID
     */
    private Long shiftId;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 计划上班时间
     */
    private LocalDateTime plannedStartTime;

    /**
     * 计划下班时间
     */
    private LocalDateTime plannedEndTime;

    /**
     * 实际打卡时间
     */
    private LocalDateTime actualTime;

    /**
     * 打卡类型（上班/下班）
     */
    private ClockType clockType;

    /**
     * 考勤状态
     */
    private AttendanceStatus attendanceStatus;

    /**
     * 是否迟到
     */
    private Boolean isLate;

    /**
     * 是否早退
     */
    private Boolean isEarlyLeave;

    /**
     * 迟到/早退时长（分钟）
     */
    private Integer deviationMinutes;

    /**
     * 事件来源系统
     */
    private String sourceSystem;

    /**
     * 事件优先级
     */
    private Integer priority;

    /**
     * 事件数据
     */
    private Map<String, Object> eventData;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 事件处理状态
     */
    private EventProcessingStatus processingStatus;

    /**
     * 处理开始时间
     */
    private LocalDateTime processingStartTime;

    /**
     * 处理结束时间
     */
    private LocalDateTime processingEndTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 事件版本
     */
    private String eventVersion;

    /**
     * 是否需要人工审核
     */
    private Boolean requiresManualReview;

    /**
     * 关联事件ID列表（用于事件关联）
     */
    private java.util.List<String> relatedEventIds;

    /**
     * 事件标签
     */
    private java.util.List<String> eventTags;

    /**
     * 考勤事件类型枚举
     */
    public enum AttendanceEventType {
        CLOCK_IN("打卡上班"),
        CLOCK_OUT("打卡下班"),
        BREAK_IN("休息开始"),
        BREAK_OUT("休息结束"),
        OVERTIME_IN("加班开始"),
        OVERTIME_OUT("加班结束"),
        LEAVE_IN("请假开始"),
        LEAVE_OUT("请假结束"),
        BUSINESS_TRIP_IN("出差开始"),
        BUSINESS_TRIP_OUT("出差结束"),
        ABSENCE("缺勤"),
        LATE_ARRIVAL("迟到"),
        EARLY_DEPARTURE("早退"),
        SCHEDULE_CHANGE("排班变更"),
        DEVICE_MALFUNCTION("设备故障"),
        EXCEPTION("异常事件");

        private final String description;

        AttendanceEventType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 考勤方式枚举
     */
    public enum AttendanceMethod {
        FACE_RECOGNITION("人脸识别"),
        FINGERPRINT("指纹识别"),
        PALM_RECOGNITION("掌纹识别"),
        IRIS_RECOGNITION("虹膜识别"),
        VOICE_RECOGNITION("声纹识别"),
        RFID_CARD("RFID卡"),
        QR_CODE("二维码"),
        PASSWORD("密码"),
        MANUAL("人工登记"),
        GPS_LOCATION("GPS定位"),
        WIFI_LOCATION("WiFi定位");

        private final String description;

        AttendanceMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 生物识别信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BiometricInfo {
        private String biometricType; // 生物识别类型
        private String biometricTemplate; // 生物特征模板
        private Double matchScore; // 匹配分数
        private String biometricImage; // 生物特征图像
        private LocalDateTime captureTime; // 采集时间
        private String deviceInfo; // 设备信息
        private Map<String, Object> additionalInfo; // 附加信息
    }

    /**
     * 打卡类型枚举
     */
    public enum ClockType {
        CLOCK_IN("上班打卡"),
        CLOCK_OUT("下班打卡");

        private final String description;

        ClockType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 考勤状态枚举
     */
    public enum AttendanceStatus {
        NORMAL("正常"),
        LATE("迟到"),
        EARLY_LEAVE("早退"),
        ABSENT("缺勤"),
        LEAVE("请假"),
        BUSINESS_TRIP("出差"),
        OVERTIME("加班"),
        BREAK("休息"),
        EXCEPTION("异常");

        private final String description;

        AttendanceStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 事件处理状态枚举
     */
    public enum EventProcessingStatus {
        PENDING("待处理"),
        PROCESSING("处理中"),
        COMPLETED("已完成"),
        FAILED("处理失败"),
        SKIPPED("已跳过"),
        RETRY("重试中");

        private final String description;

        EventProcessingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}