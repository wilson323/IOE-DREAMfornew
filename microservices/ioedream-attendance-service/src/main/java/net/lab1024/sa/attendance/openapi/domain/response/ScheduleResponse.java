package net.lab1024.sa.attendance.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 排班信息响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "排班信息响应")
public class ScheduleResponse {

    @Schema(description = "排班ID", example = "10001")
    private Long scheduleId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    @Schema(description = "排班日期", example = "2025-12-16")
    private String scheduleDate;

    @Schema(description = "星期几", example = "1")
    private Integer dayOfWeek;

    @Schema(description = "星期名称", example = "周一")
    private String dayName;

    @Schema(description = "班次ID", example = "1")
    private Long shiftId;

    @Schema(description = "班次名称", example = "标准班")
    private String shiftName;

    @Schema(description = "班次类型", example = "normal", allowableValues = {"normal", "flexible", "night", "overtime"})
    private String shiftType;

    @Schema(description = "班次类型名称", example = "标准班")
    private String shiftTypeName;

    @Schema(description = "上班时间", example = "09:00")
    private String workStartTime;

    @Schema(description = "下班时间", example = "18:00")
    private String workEndTime;

    @Schema(description = "午休开始时间", example = "12:00")
    private String breakStartTime;

    @Schema(description = "午休结束时间", example = "13:00")
    private String breakEndTime;

    @Schema(description = "工作时长（小时）", example = "8.0")
    private Double workHours;

    @Schema(description = "午休时长（分钟）", example = "60")
    private Integer breakMinutes;

    @Schema(description = "排班状态", example = "1", allowableValues = {"0", "1", "2"})
    private Integer scheduleStatus;

    @Schema(description = "排班状态名称", example = "正常")
    private String scheduleStatusName;

    @Schema(description = "是否工作日", example = "true")
    private Boolean isWorkDay;

    @Schema(description = "是否节假日", example = "false")
    private Boolean isHoliday;

    @Schema(description = "假期名称", example = "")
    private String holidayName;

    @Schema(description = "排班负责人ID", example = "1002")
    private Long schedulerId;

    @Schema(description = "排班负责人姓名", example = "排班管理员")
    private String schedulerName;

    @Schema(description = "排班时间", example = "2025-12-01T10:30:00")
    private LocalDateTime scheduleTime;

    @Schema(description = "生效时间", example = "2025-12-16T00:00:00")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间", example = "2025-12-16T23:59:59")
    private LocalDateTime expireTime;

    @Schema(description = "备用班次", example = "true")
    private Boolean isBackup;

    @Schema(description = "主班次ID", example = "2")
    private Long primaryShiftId;

    @Schema(description = "主班次名称", example = "弹性班")
    private String primaryShiftName;

    @Schema(description = "考勤规则")
    private List<AttendanceRule> attendanceRules;

    @Schema(description = "排班备注", example = "")
    private String remark;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "考勤规则")
    public static class AttendanceRule {

        @Schema(description = "规则ID", example = "1")
        private Long ruleId;

        @Schema(description = "规则名称", example = "迟到规则")
        private String ruleName;

        @Schema(description = "规则类型", example = "late", allowableValues = {"late", "early", "absent", "overtime"})
        private String ruleType;

        @Schema(description = "规则内容", example = "上班打卡时间超过9:00算迟到")
        private String ruleContent;

        @Schema(description = "规则值", example = "09:00")
        private String ruleValue;

        @Schema(description = "容忍时间（分钟）", example = "5")
        private Integer toleranceMinutes;

        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;
    }
}
