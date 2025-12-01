package net.lab1024.sa.admin.module.attendance.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 区域考勤配置DTO
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AreaAttendanceConfigDTO {

    /**
     * 区域ID
     */
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    /**
     * 区域名称
     */
    @NotBlank(message = "区域名称不能为空")
    private String areaName;

    /**
     * 是否启用考勤
     */
    @NotNull(message = "考勤启用状态不能为空")
    private Boolean attendanceEnabled;

    /**
     * 工作日配置
     */
    private WorkDayConfig workDayConfig;

    /**
     * 打卡规则配置
     */
    private PunchRuleConfig punchRuleConfig;

    /**
     * 异常处理配置
     */
    private ExceptionHandlingConfig exceptionHandlingConfig;

    /**
     * 考勤设备列表
     */
    private List<Long> attendanceDeviceIds;

    /**
     * 排班规则列表
     */
    private List<Long> shiftRuleIds;

    /**
     * 扩展配置
     */
    private Map<String, Object> extendedConfig;

    /**
     * 工作日配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkDayConfig {

        /**
         * 工作日类型：WEEKDAYS-周一到周五，CUSTOM-自定义
         */
        private String workDayType;

        /**
         * 工作日列表（1-7，1代表周一）
         */
        private List<Integer> workDays;

        /**
         * 是否启用节假日
         */
        private Boolean holidayEnabled;

        /**
         * 节假日是否工作
         */
        private Boolean holidayWork;
    }

    /**
     * 打卡规则配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PunchRuleConfig {

        /**
         * 上班时间
         */
        private LocalTime workStartTime;

        /**
         * 下班时间
         */
        private LocalTime workEndTime;

        /**
         * 上班打卡开始时间（允许提前打卡）
         */
        private LocalTime punchInStartTime;

        /**
         * 上班打卡结束时间（迟到截止时间）
         */
        private LocalTime punchInEndTime;

        /**
         * 下班打卡开始时间（早退截止时间）
         */
        private LocalTime punchOutStartTime;

        /**
         * 下班打卡结束时间（允许延后打卡）
         */
        private LocalTime punchOutEndTime;

        /**
         * 迟到宽限时间（分钟）
         */
        private Integer lateToleranceMinutes;

        /**
         * 早退宽限时间（分钟）
         */
        private Integer earlyLeaveToleranceMinutes;

        /**
         * 是否必须打卡上班
         */
        private Boolean requirePunchIn;

        /**
         * 是否必须打卡下班
         */
        private Boolean requirePunchOut;
    }

    /**
     * 异常处理配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExceptionHandlingConfig {

        /**
         * 是否自动处理异常
         */
        private Boolean autoHandleExceptions;

        /**
         * 迟到处理方式：IGNORE-忽略，WARNING-警告，DEDUCTION-扣款
         */
        private String lateHandling;

        /**
         * 早退处理方式：IGNORE-忽略，WARNING-警告，DEDUCTION-扣款
         */
        private String earlyLeaveHandling;

        /**
         * 缺卡处理方式：IGNORE-忽略，WARNING-警告，DEDUCTION-扣款
         */
        private String absenceHandling;

        /**
         * 是否发送异常通知
         */
        private Boolean sendExceptionNotification;

        /**
         * 通知人员列表
         */
        private List<Long> notificationUserIds;
    }

    /**
     * 验证配置有效性
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return areaId != null && areaName != null && attendanceEnabled != null;
    }

    /**
     * 检查是否需要打卡
     *
     * @param punchType 打卡类型
     * @return 是否需要打卡
     */
    public boolean isPunchRequired(String punchType) {
        if (punchRuleConfig == null) {
            return true;
        }

        if ("IN".equals(punchType)) {
            return Boolean.TRUE.equals(punchRuleConfig.getRequirePunchIn());
        } else if ("OUT".equals(punchType)) {
            return Boolean.TRUE.equals(punchRuleConfig.getRequirePunchOut());
        }

        return true;
    }
}