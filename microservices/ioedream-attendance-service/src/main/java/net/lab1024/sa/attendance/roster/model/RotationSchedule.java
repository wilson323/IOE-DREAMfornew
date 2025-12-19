package net.lab1024.sa.attendance.roster.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 轮班安排
 * <p>
 * 封装轮班安排的详细信息
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
public class RotationSchedule {

    /**
     * 安排ID
     */
    private String scheduleId;

    /**
     * 轮班制度ID
     */
    private String rotationSystemId;

    /**
     * 制度名称
     */
    private String systemName;

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
     * 安排日期
     */
    private LocalDate scheduleDate;

    /**
     * 班次ID
     */
    private String shiftId;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 班次类型
     */
    private RotationSystemConfig.ShiftType shiftType;

    /**
     * 工作开始时间
     */
    private LocalDateTime workStartTime;

    /**
     * 工作结束时间
     */
    private LocalDateTime workEndTime;

    /**
     * 休息开始时间
     */
    private LocalDateTime restStartTime;

    /**
     * 休息结束时间
     */
    private LocalDateTime restEndTime;

    /**
     * 工作地点
     */
    private String workLocation;

    /**
     * 工作岗位
     */
    private String workPosition;

    /**
     * 安排状态
     */
    private ScheduleStatus status;

    /**
     * 考勤状态
     */
    private AttendanceStatus attendanceStatus;

    /**
     * 实际打卡时间
     */
    private LocalDateTime actualClockInTime;

    /**
     * 实际下班时间
     */
    private LocalDateTime actualClockOutTime;

    /**
     * 是否迟到
     */
    private Boolean isLate;

    /**
     * 迟到分钟数
     */
    private Integer lateMinutes;

    /**
     * 是否早退
     */
    private Boolean isEarlyLeave;

    /**
     * 早退分钟数
     */
    private Integer earlyLeaveMinutes;

    /**
     * 是否加班
     */
    private Boolean isOvertime;

    /**
     * 加班分钟数
     */
    private Integer overtimeMinutes;

    /**
     * 是否请假
     */
    private Boolean isOnLeave;

    /**
     * 请假类型
     */
    private String leaveType;

    /**
     * 是否换班
     */
    private Boolean isShiftExchange;

    /**
     * 换班员工ID
     */
    private Long exchangeEmployeeId;

    /**
     * 换班员工姓名
     */
    private String exchangeEmployeeName;

    /**
     * 交接班信息
     */
    private HandoverInfo handoverInfo;

    /**
     * 工作任务
     */
    private List<WorkTask> workTasks;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 安排优先级
     */
    private Integer priority;

    /**
     * 安排人
     */
    private Long assignedBy;

    /**
     * 安排人姓名
     */
    private String assignedByName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 预警信息
     */
    private List<AlertInfo> alertInfos;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 安排状态枚举
     */
    public enum ScheduleStatus {
        SCHEDULED("已安排"),
        CONFIRMED("已确认"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消"),
        EXCHANGED("已换班"),
        ABSENT("缺勤"),
        ON_LEAVE("请假中");

        private final String description;

        ScheduleStatus(String description) {
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
        PENDING("待打卡"),
        CLOCKED_IN("已上班"),
        CLOCKED_OUT("已下班"),
        ABSENT("缺勤"),
        LATE("迟到"),
        EARLY_LEAVE("早退"),
        OVERTIME("加班中"),
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
     * 交接班信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HandoverInfo {
        /**
         * 交接班状态
         */
        private HandoverStatus handoverStatus;

        /**
         * 上班交接人
         */
        private String previousHandoverPerson;

        /**
         * 下班交接人
         */
        private String nextHandoverPerson;

        /**
         * 交接时间
         */
        private LocalDateTime handoverTime;

        /**
         * 交接内容
         */
        private String handoverContent;

        /**
         * 交接确认状态
         */
        private Boolean confirmationStatus;

        /**
         * 交接备注
         */
        private String handoverRemarks;
    }

    /**
     * 交接班状态枚举
     */
    public enum HandoverStatus {
        NOT_REQUIRED("无需交接"),
        PENDING("待交接"),
        IN_PROGRESS("交接中"),
        COMPLETED("已完成"),
        FAILED("交接失败"),
        EXCEPTION("交接异常");

        private final String description;

        HandoverStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 工作任务
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkTask {
        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 任务名称
         */
        private String taskName;

        /**
         * 任务描述
         */
        private String taskDescription;

        /**
         * 任务优先级
         */
        private Integer priority;

        /**
         * 任务状态
         */
        private String taskStatus;

        /**
         * 预计完成时间
         */
        private LocalDateTime estimatedCompletionTime;

        /**
         * 实际完成时间
         */
        private LocalDateTime actualCompletionTime;

        /**
         * 任务备注
         */
        private String taskRemarks;
    }

    /**
     * 预警信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertInfo {
        /**
         * 预警ID
         */
        private String alertId;

        /**
         * 预警类型
         */
        private String alertType;

        /**
         * 预警标题
         */
        private String alertTitle;

        /**
         * 预警描述
         */
        private String alertDescription;

        /**
         * 预警级别
         */
        private String alertLevel;

        /**
         * 预警时间
         */
        private LocalDateTime alertTime;

        /**
         * 是否已处理
         */
        private Boolean isHandled;

        /**
         * 处理时间
         */
        private LocalDateTime handleTime;

        /**
         * 处理人
         */
        private Long handlerId;

        /**
         * 处理人姓名
         */
        private String handlerName;

        /**
         * 处理备注
         */
        private String handleRemarks;
    }

    /**
     * 获取工作时长（分钟）
     */
    public Integer getWorkDurationMinutes() {
        if (workStartTime == null || workEndTime == null) {
            return null;
        }

        if (restStartTime != null && restEndTime != null) {
            // 有休息时间，计算净工作时间
            long totalDuration = java.time.Duration.between(workStartTime, workEndTime).toMinutes();
            long restDuration = java.time.Duration.between(restStartTime, restEndTime).toMinutes();
            return (int) (totalDuration - restDuration);
        } else {
            // 无休息时间，计算总工作时间
            return (int) java.time.Duration.between(workStartTime, workEndTime).toMinutes();
        }
    }

    /**
     * 获取格式化的工作时长
     */
    public String getFormattedWorkDuration() {
        Integer duration = getWorkDurationMinutes();
        if (duration == null) {
            return "未知";
        }

        int hours = duration / 60;
        int minutes = duration % 60;
        return hours + "小时" + minutes + "分钟";
    }

    /**
     * 检查是否为跨天工作
     */
    public Boolean isOvernightWork() {
        if (workStartTime == null || workEndTime == null) {
            return null;
        }

        LocalDate startDate = workStartTime.toLocalDate();
        LocalDate endDate = workEndTime.toLocalDate();
        return !startDate.equals(endDate);
    }

    /**
     * 检查是否需要交接班
     */
    public Boolean requiresHandover() {
        return shiftType == RotationSystemConfig.ShiftType.NIGHT ||
               shiftType == RotationSystemConfig.ShiftType.GRAVEYARD ||
               isOvernightWork();
    }

    /**
     * 获取班次时段描述
     */
    public String getShiftTimeDescription() {
        if (workStartTime == null || workEndTime == null) {
            return "时间未定";
        }

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        String startTime = workStartTime.format(formatter);
        String endTime = workEndTime.format(formatter);

        if (isOvernightWork()) {
            return startTime + "(次日) - " + endTime;
        } else {
            return startTime + " - " + endTime;
        }
    }

    /**
     * 获取考勤状态描述
     */
    public String getAttendanceStatusDescription() {
        StringBuilder statusDesc = new StringBuilder();

        if (attendanceStatus != null) {
            statusDesc.append(attendanceStatus.getDescription());
        }

        if (Boolean.TRUE.equals(isLate) && lateMinutes != null) {
            statusDesc.append(" (迟到").append(lateMinutes).append("分钟)");
        }

        if (Boolean.TRUE.equals(isEarlyLeave) && earlyLeaveMinutes != null) {
            statusDesc.append(" (早退").append(earlyLeaveMinutes).append("分钟)");
        }

        if (Boolean.TRUE.equals(isOvertime) && overtimeMinutes != null) {
            statusDesc.append(" (加班").append(overtimeMinutes).append("分钟)");
        }

        return statusDesc.toString();
    }

    /**
     * 获取当前状态描述
     */
    public String getCurrentStatusDescription() {
        LocalDateTime now = LocalDateTime.now();

        if (status == ScheduleStatus.CANCELLED) {
            return "已取消";
        }

        if (status == ScheduleStatus.ON_LEAVE) {
            return "请假中";
        }

        if (status == ScheduleStatus.ABSENT) {
            return "缺勤";
        }

        if (workStartTime == null || workEndTime == null) {
            return "时间未定";
        }

        if (now.isBefore(workStartTime)) {
            return "未开始";
        }

        if (now.isAfter(workEndTime)) {
            return "已结束";
        }

        if (restStartTime != null && restEndTime != null) {
            if (now.isAfter(restStartTime) && now.isBefore(restEndTime)) {
                return "休息中";
            }
        }

        return "工作中";
    }

    /**
     * 检查是否有预警
     */
    public Boolean hasAlerts() {
        return alertInfos != null && !alertInfos.isEmpty();
    }

    /**
     * 获取未处理预警数量
     */
    public Integer getUnhandledAlertCount() {
        if (alertInfos == null) {
            return 0;
        }

        return (int) alertInfos.stream()
                .filter(alert -> Boolean.FALSE.equals(alert.getIsHandled()))
                .count();
    }

    /**
     * 检查安排是否有效
     */
    public boolean isValid() {
        if (scheduleId == null || employeeId == null || scheduleDate == null) {
            return false;
        }

        if (workStartTime != null && workEndTime != null) {
            return !workStartTime.isAfter(workEndTime);
        }

        if (restStartTime != null && restEndTime != null) {
            return !restStartTime.isAfter(restEndTime);
        }

        return true;
    }

    /**
     * 获取工作安排摘要
     */
    public String getScheduleSummary() {
        StringBuilder summary = new StringBuilder();

        summary.append(employeeName != null ? employeeName : "未知员工");
        summary.append(" - ");

        if (shiftName != null) {
            summary.append(shiftName);
        } else if (shiftType != null) {
            summary.append(shiftType.getDescription());
        } else {
            summary.append("未定班次");
        }

        summary.append(" - ");
        summary.append(getShiftTimeDescription());

        if (workLocation != null) {
            summary.append(" - ");
            summary.append(workLocation);
        }

        if (attendanceStatus != null) {
            summary.append(" - ");
            summary.append(attendanceStatus.getDescription());
        }

        return summary.toString();
    }
}