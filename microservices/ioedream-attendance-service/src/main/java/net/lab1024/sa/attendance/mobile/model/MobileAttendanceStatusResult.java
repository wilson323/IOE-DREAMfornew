package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 移动端考勤状态查询结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端考勤状态查询结果")
public class MobileAttendanceStatusResult {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "查询日期", example = "2025-12-16")
    private LocalDate queryDate;

    @Schema(description = "考勤状态", example = "NORMAL", allowableValues = {"NORMAL", "LATE", "EARLY", "ABSENT", "LEAVE", "OVERTIME"})
    private String attendanceStatus;

    @Schema(description = "上班打卡时间", example = "2025-12-16T09:00:00")
    private LocalDateTime clockInTime;

    @Schema(description = "下班打卡时间", example = "2025-12-16T18:00:00")
    private LocalDateTime clockOutTime;

    @Schema(description = "工作时长（小时）", example = "8.5")
    private Double workHours;

    @Schema(description = "标准工作时长（小时）", example = "8.0")
    private Double standardWorkHours;

    @Schema(description = "迟到分钟数", example = "0")
    private Integer lateMinutes;

    @Schema(description = "早退分钟数", example = "0")
    private Integer earlyLeaveMinutes;

    @Schema(description = "加班时长（小时）", example = "0.5")
    private Double overtimeHours;

    @Schema(description = "是否缺勤", example = "false")
    private Boolean isAbsent;

    @Schema(description = "是否请假", example = "false")
    private Boolean onLeave;

    @Schema(description = "打卡详情")
    private List<AttendanceRecord> attendanceRecords;

    @Schema(description = "考勤统计")
    private AttendanceStatistics statistics;

    @Schema(description = "班次信息")
    private ShiftInfo shiftInfo;

    @Schema(description = "位置信息")
    private LocationInfo locationInfo;

    @Schema(description = "时间戳", example = "1703020800000")
    private Long timestamp;

    /**
     * 考勤记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "考勤记录")
    public static class AttendanceRecord {

        @Schema(description = "记录ID", example = "1001")
        private Long recordId;

        @Schema(description = "打卡类型", example = "IN", allowableValues = {"IN", "OUT"})
        private String clockType;

        @Schema(description = "打卡时间", example = "2025-12-16T09:00:00")
        private LocalDateTime clockTime;

        @Schema(description = "打卡位置", example = "公司总部")
        private String clockLocation;

        @Schema(description = "设备类型", example = "MOBILE")
        private String deviceType;

        @Schema(description = "生物识别类型", example = "FACE")
        private String biometricType;

        @Schema(description = "识别置信度", example = "0.98")
        private Double confidence;

        @Schema(description = "打卡备注", example = "正常上班打卡")
        private String remark;

        @Schema(description = "打卡状态", example = "NORMAL", allowableValues = {"NORMAL", "LATE", "EARLY", "ABNORMAL"})
        private String clockStatus;
    }

    /**
     * 考勤统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "考勤统计")
    public static class AttendanceStatistics {

        @Schema(description = "本月出勤天数", example = "22")
        private Integer monthlyAttendanceDays;

        @Schema(description = "本月迟到次数", example = "1")
        private Integer monthlyLateCount;

        @Schema(description = "本月早退次数", example = "0")
        private Integer monthlyEarlyLeaveCount;

        @Schema(description = "本月请假天数", example = "2")
        private Integer monthlyLeaveDays;

        @Schema(description = "本月加班时长（小时）", example = "8.5")
        private Double monthlyOvertimeHours;

        @Schema(description = "本月工作时长（小时）", example = "176.0")
        private Double monthlyWorkHours;

        @Schema(description = "本年出勤天数", example = "240")
        private Integer yearlyAttendanceDays;

        @Schema(description = "本年迟到次数", example = "5")
        private Integer yearlyLateCount;

        @Schema(description = "本年早退次数", example = "2")
        private Integer yearlyEarlyLeaveCount;

        @Schema(description = "本年请假天数", example = "15")
        private Integer yearlyLeaveDays;

        @Schema(description = "本年加班时长（小时）", example = "120.0")
        private Double yearlyOvertimeHours;

        @Schema(description = "本年工作时长（小时）", example = "1920.0")
        private Double yearlyWorkHours;
    }

    /**
     * 班次信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "班次信息")
    public static class ShiftInfo {

        @Schema(description = "班次ID", example = "SHIFT_001")
        private Long shiftId;

        @Schema(description = "班次名称", example = "正常班")
        private String shiftName;

        @Schema(description = "班次类型", example = "REGULAR", allowableValues = {"REGULAR", "FLEXIBLE", "ROTATING"})
        private String shiftType;

        @Schema(description = "标准上班时间", example = "2025-12-16T09:00:00")
        private LocalDateTime standardClockInTime;

        @Schema(description = "标准下班时间", example = "2025-12-16T18:00:00")
        private LocalDateTime standardClockOutTime;

        @Schema(description = "休息开始时间", example = "2025-12-16T12:00:00")
        private LocalDateTime breakStartTime;

        @Schema(description = "休息结束时间", example = "2025-12-16T13:00:00")
        private LocalDateTime breakEndTime;

        @Schema(description = "工作时长（小时）", example = "8.0")
        private Double workHours;

        @Schema(description = "休息时长（小时）", example = "1.0")
        private Double breakHours;

        @Schema(description = "弹性上班时间（分钟）", example = "15")
        private Integer flexibleClockInMinutes;

        @Schema(description = "弹性下班时间（分钟）", example = "15")
        private Integer flexibleClockOutMinutes;
    }

    /**
     * 位置信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "位置信息")
    public static class LocationInfo {

        @Schema(description = "打卡位置", example = "公司总部")
        private String clockLocation;

        @Schema(description = "详细地址", example = "北京市朝阳区建国门外大街1号")
        private String detailedAddress;

        @Schema(description = "纬度", example = "39.9042")
        private Double latitude;

        @Schema(description = "经度", example = "116.4074")
        private Double longitude;

        @Schema(description = "精度（米）", example = "10.5")
        private Double accuracy;

        @Schema(description = "地理围栏ID", example = "GEOFENCE_001")
        private String geofenceId;

        @Schema(description = "地理围栏名称", example = "办公区域")
        private String geofenceName;

        @Schema(description = "是否在围栏内", example = "true")
        private Boolean withinGeofence;

        @Schema(description = "与围栏边界的距离（米）", example = "5.2")
        private Double distanceToGeofence;
    }
}
