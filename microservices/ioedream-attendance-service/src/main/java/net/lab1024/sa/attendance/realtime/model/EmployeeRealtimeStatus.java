package net.lab1024.sa.attendance.realtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 员工实时状态
 * <p>
 * 封装员工的实时考勤状态信息
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
public class EmployeeRealtimeStatus {

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
     * 班次ID
     */
    private Long shiftId;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 当前考勤状态
     */
    private AttendanceStatus currentStatus;

    /**
     * 当前工作状态
     */
    private WorkStatus workStatus;

    /**
     * 今日首次打卡时间
     */
    private LocalDateTime firstClockInTime;

    /**
     * 今日最后一次打卡时间
     */
    private LocalDateTime lastClockOutTime;

    /**
     * 当前工作开始时间
     */
    private LocalDateTime currentWorkStartTime;

    /**
     * 当前工作时长（分钟）
     */
    private Long currentWorkDurationMinutes;

    /**
     * 今日工作总时长（分钟）
     */
    private Long todayWorkDurationMinutes;

    /**
     * 今日标准工作时长（分钟）
     */
    private Long standardWorkDurationMinutes;

    /**
     * 工作完成率（百分比）
     */
    private BigDecimal workCompletionRate;

    /**
     * 是否迟到
     */
    private Boolean isLate;

    /**
     * 迟到时长（分钟）
     */
    private Integer lateMinutes;

    /**
     * 是否早退
     */
    private Boolean isEarlyLeave;

    /**
     * 早退时长（分钟）
     */
    private Integer earlyLeaveMinutes;

    /**
     * 是否缺勤
     */
    private Boolean isAbsent;

    /**
     * 是否在加班
     */
    private Boolean isOvertime;

    /**
     * 当前加班时长（分钟）
     */
    private Long currentOvertimeMinutes;

    /**
     * 今日加班总时长（分钟）
     */
    private Long todayOvertimeMinutes;

    /**
     * 是否在请假
     */
    private Boolean isOnLeave;

    /**
     * 请假类型
     */
    private LeaveType leaveType;

    /**
     * 请假剩余时长（分钟）
     */
    private Long remainingLeaveMinutes;

    /**
     * 是否在出差
     */
    private Boolean isOnBusinessTrip;

    /**
     * 出差地点
     */
    private String businessTripLocation;

    /**
     * 当前所在位置
     */
    private String currentLocation;

    /**
     * 最后考勤位置
     */
    private String lastAttendanceLocation;

    /**
     * 当前考勤设备
     */
    private String currentDeviceName;

    /**
     * 今日考勤次数
     */
    private Integer todayAttendanceCount;

    /**
     * 今日异常次数
     */
    private Integer todayAnomalyCount;

    /**
     * 最后状态更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 实时状态数据时间戳
     */
    private LocalDateTime statusTimestamp;

    /**
     * 状态数据有效期（分钟）
     */
    private Integer dataValidityMinutes;

    /**
     * 状态数据来源
     */
    private String dataSource;

    /**
     * 考勤状态枚举
     */
    public enum AttendanceStatus {
        NORMAL_WORKING("正常工作"),
        LATE_WORKING("迟到工作中"),
        EARLY_LEAVE("已早退"),
        ABSENT("缺勤"),
        ON_LEAVE("请假中"),
        ON_BUSINESS_TRIP("出差中"),
        OVERTIME_WORKING("加班中"),
        BREAK_TIME("休息中"),
        OFF_DUTY("下班"),
        UNKNOWN_STATUS("状态未知");

        private final String description;

        AttendanceStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 工作状态枚举
     */
    public enum WorkStatus {
        ACTIVE("活跃"),
        IDLE("空闲"),
        BUSY("忙碌"),
        OFFLINE("离线"),
        UNAVAILABLE("不可用");

        private final String description;

        WorkStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 请假类型枚举
     */
    public enum LeaveType {
        SICK_LEAVE("病假"),
        PERSONAL_LEAVE("事假"),
        ANNUAL_LEAVE("年假"),
        MATERNITY_LEAVE("产假"),
        PATERNITY_LEAVE("陪产假"),
        MARRIAGE_LEAVE("婚假"),
        FUNERAL_LEAVE("丧假"),
        COMPENSATORY_LEAVE("调休");

        private final String description;

        LeaveType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 计算工作完成率
     */
    public void calculateWorkCompletionRate() {
        if (standardWorkDurationMinutes != null && standardWorkDurationMinutes > 0 && todayWorkDurationMinutes != null) {
            BigDecimal completed = BigDecimal.valueOf(todayWorkDurationMinutes);
            BigDecimal standard = BigDecimal.valueOf(standardWorkDurationMinutes);
            this.workCompletionRate = completed.divide(standard, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.workCompletionRate = BigDecimal.ZERO;
        }
    }

    /**
     * 格式化工作时长显示
     */
    public String getFormattedWorkDuration() {
        if (currentWorkDurationMinutes == null) {
            return "0小时0分钟";
        }
        long hours = currentWorkDurationMinutes / 60;
        long minutes = currentWorkDurationMinutes % 60;
        return hours + "小时" + minutes + "分钟";
    }

    /**
     * 格式化加班时长显示
     */
    public String getFormattedOvertimeDuration() {
        if (currentOvertimeMinutes == null) {
            return "0小时0分钟";
        }
        long hours = currentOvertimeMinutes / 60;
        long minutes = currentOvertimeMinutes % 60;
        return hours + "小时" + minutes + "分钟";
    }

    /**
     * 检查状态数据是否有效
     */
    public boolean isStatusDataValid() {
        if (statusTimestamp == null || dataValidityMinutes == null) {
            return false;
        }
        LocalDateTime expiryTime = statusTimestamp.plusMinutes(dataValidityMinutes);
        return LocalDateTime.now().isBefore(expiryTime);
    }

    /**
     * 获取工作状态总览描述
     */
    public String getWorkStatusSummary() {
        StringBuilder summary = new StringBuilder();

        if (currentStatus != null) {
            summary.append("当前状态: ").append(currentStatus.getDescription());
        }

        if (isLate != null && isLate) {
            summary.append(" | 迟到").append(lateMinutes != null ? lateMinutes + "分钟" : "");
        }

        if (isEarlyLeave != null && isEarlyLeave) {
            summary.append(" | 早退").append(earlyLeaveMinutes != null ? earlyLeaveMinutes + "分钟" : "");
        }

        if (isOvertime != null && isOvertime) {
            summary.append(" | 加班中");
        }

        if (isOnLeave != null && isOnLeave && leaveType != null) {
            summary.append(" | ").append(leaveType.getDescription());
        }

        if (isOnBusinessTrip != null && isOnBusinessTrip) {
            summary.append(" | 出差中");
            if (businessTripLocation != null) {
                summary.append("(").append(businessTripLocation).append(")");
            }
        }

        return summary.toString();
    }

    /**
     * 获取工作时长完成度描述
     */
    public String getWorkDurationSummary() {
        if (todayWorkDurationMinutes == null || standardWorkDurationMinutes == null) {
            return "工作时长: 未统计";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("今日工作: ").append(getFormattedWorkDuration());
        summary.append(" / 标准: ").append(standardWorkDurationMinutes / 60).append("小时");

        if (workCompletionRate != null) {
            summary.append(" (完成").append(workCompletionRate.setScale(1, RoundingMode.HALF_UP)).append("%)");
        }

        return summary.toString();
    }
}