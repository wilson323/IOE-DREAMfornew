package net.lab1024.sa.attendance.roster.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 轮班制度配置
 * <p>
 * 封装轮班制度的配置信息
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
public class RotationSystemConfig {

    /**
     * 轮班制度名称
     */
    private String systemName;

    /**
     * 轮班制度描述
     */
    private String systemDescription;

    /**
     * 轮班制度类型
     */
    private RotationSystemType systemType;

    /**
     * 适用部门ID列表
     */
    private List<Long> departmentIds;

    /**
     * 适用员工ID列表
     */
    private List<Long> employeeIds;

    /**
     * 轮班周期类型
     */
    private RotationCycleType cycleType;

    /**
     * 轮班周期天数
     */
    private Integer cycleDays;

    /**
     * 班次配置列表
     */
    private List<ShiftConfig> shiftConfigs;

    /**
     * 轮班规则配置
     */
    private RotationRules rotationRules;

    /**
     * 交接班配置
     */
    private HandoverConfig handoverConfig;

    /**
     * 休息配置
     */
    private RestConfig restConfig;

    /**
     * 加班配置
     */
    private OvertimeConfig overtimeConfig;

    /**
     * 生效日期
     */
    private LocalDateTime effectiveDate;

    /**
     * 失效日期
     */
    private LocalDateTime expiryDate;

    /**
     * 制度状态
     */
    private SystemStatus status;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 轮班制度类型枚举
     */
    public enum RotationSystemType {
        THREE_SHIFT("三班倒"),
        FOUR_SHIFT("四班三倒"),
        TWO_SHIFT("两班倒"),
        MULTI_SHIFT("多班倒"),
        FLEXIBLE_SHIFT("灵活排班"),
        CUSTOM_SHIFT("自定义排班");

        private final String description;

        RotationSystemType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 轮班周期类型枚举
     */
    public enum RotationCycleType {
        DAILY("每天轮换"),
        WEEKLY("每周轮换"),
        MONTHLY("每月轮换"),
        CUSTOM_DAYS("自定义天数轮换"),
        CONTINUOUS("连续不轮换");

        private final String description;

        RotationCycleType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 制度状态枚举
     */
    public enum SystemStatus {
        DRAFT("草稿"),
        ACTIVE("生效中"),
        SUSPENDED("已暂停"),
        EXPIRED("已过期"),
        ARCHIVED("已归档");

        private final String description;

        SystemStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 班次配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftConfig {
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
        private ShiftType shiftType;

        /**
         * 工作开始时间
         */
        private LocalTime workStartTime;

        /**
         * 工作结束时间
         */
        private LocalTime workEndTime;

        /**
         * 休息开始时间
         */
        private LocalTime restStartTime;

        /**
         * 休息结束时间
         */
        private LocalTime restEndTime;

        /**
         * 工作时长（分钟）
         */
        private Integer workDurationMinutes;

        /**
         * 休息时长（分钟）
         */
        private Integer restDurationMinutes;

        /**
         * 跨天工作
         */
        private Boolean isOvernight;

        /**
         * 班次优先级
         */
        private Integer priority;

        /**
         * 班次描述
         */
        private String description;

        /**
         * 班次颜色标识
         */
        private String colorCode;

        /**
         * 是否需要考勤打卡
         */
        private Boolean requiresAttendance;

        /**
         * 最少工作人数
         */
        private Integer minRequiredEmployees;

        /**
         * 最多工作人数
         */
        private Integer maxAllowedEmployees;

        /**
         * 班次扩展属性
         */
        private Map<String, Object> shiftAttributes;
    }

    /**
     * 班次类型枚举
     */
    public enum ShiftType {
        EARLY_MORNING("早班", "06:00-14:00"),
        MORNING("上午班", "08:00-16:00"),
        AFTERNOON("下午班", "14:00-22:00"),
        EVENING("晚班", "16:00-24:00"),
        NIGHT("夜班", "22:00-06:00"),
        GRAVEYARD("大夜班", "00:00-08:00"),
        FLEXIBLE("灵活班次", null),
        CUSTOM("自定义班次", null);

        private final String description;
        private final String typicalHours;

        ShiftType(String description, String typicalHours) {
            this.description = description;
            this.typicalHours = typicalHours;
        }

        public String getDescription() {
            return description;
        }

        public String getTypicalHours() {
            return typicalHours;
        }
    }

    /**
     * 轮班规则配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RotationRules {
        /**
         * 连续工作最大天数
         */
        private Integer maxConsecutiveWorkDays;

        /**
         * 连续休息最小天数
         */
        private Integer minConsecutiveRestDays;

        /**
         * 每月最多加班小时数
         */
        private Integer maxMonthlyOvertimeHours;

        /**
         * 每周最多工作天数
         */
        private Integer maxWeeklyWorkDays;

        /**
         * 两次夜班间隔最小天数
         */
        private Integer minDaysBetweenNightShifts;

        /**
         * 班次轮换间隔
         */
        private Integer shiftRotationInterval;

        /**
         * 是否允许连班
         */
        private Boolean allowConsecutiveShifts;

        /**
         * 是否允许跨天工作
         */
        private Boolean allowOvernightWork;

        /**
         * 节假日轮班规则
         */
        private HolidayRotationRule holidayRule;

        /**
         * 紧急情况轮班规则
         */
        private EmergencyRotationRule emergencyRule;

        /**
         * 员工偏好权重
         */
        private Map<String, Double> preferenceWeights;
    }

    /**
     * 假日轮班规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HolidayRotationRule {
        /**
         * 是否在节假日轮班
         */
        private Boolean rotateOnHolidays;

        /**
         * 节假日班次类型
         */
        private ShiftType holidayShiftType;

        /**
         * 节假日工资倍数
         */
        private Double holidayWageMultiplier;

        /**
         * 是否给予额外休息
         */
        private Boolean provideAdditionalRest;
    }

    /**
     * 紧急情况轮班规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmergencyRotationRule {
        /**
         * 紧急情况响应时间（分钟）
         */
        private Integer responseTimeMinutes;

        /**
         * 紧急轮换规则
         */
        private String emergencyRotationRule;

        /**
         * 是否允许强制轮班
         */
        private Boolean allowForcedRotation;

        /**
         * 紧急轮班补偿规则
         */
        private String emergencyCompensationRule;
    }

    /**
     * 交接班配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HandoverConfig {
        /**
         * 交接班时间（分钟）
         */
        private Integer handoverDurationMinutes;

        /**
         * 交接班提前时间（分钟）
         */
        private Integer handoverEarlyMinutes;

        /**
         * 是否必须面对面交接
         */
        private Boolean requireFaceToFaceHandover;

        /**
         * 交接班检查清单
         */
        private List<String> handoverChecklist;

        /**
         * 交接班记录要求
         */
        private HandoverRecordRequirement recordRequirement;

        /**
         * 异常交接处理规则
         */
        private String abnormalHandoverRule;
    }

    /**
     * 交接班记录要求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HandoverRecordRequirement {
        /**
         * 是否必须记录工作状态
         */
        private Boolean recordWorkStatus;

        /**
         * 是否必须记录设备状态
         */
        private Boolean recordEquipmentStatus;

        /**
         * 是否必须记录异常情况
         */
        private Boolean recordAbnormalSituations;

        /**
         * 是否必须记录待处理事项
         */
        private Boolean recordPendingItems;
    }

    /**
     * 休息配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestConfig {
        /**
         * 每天休息时间（分钟）
         */
        private Integer dailyRestMinutes;

        /**
         * 每周休息天数
         */
        private Integer weeklyRestDays;

        /**
         * 每月休息天数
         */
        private Integer monthlyRestDays;

        /**
         * 休息时间安排策略
         */
        private RestArrangementStrategy restStrategy;

        /**
         * 累积休息天数上限
         */
        private Integer maxAccumulatedRestDays;

        /**
         * 休息天数清零周期
         */
        private Integer restDaysResetCycle;
    }

    /**
     * 休息安排策略
     */
    public enum RestArrangementStrategy {
        FIXED_DAYS("固定天数"),
        FLEXIBLE_DAYS("灵活天数"),
        ACCUMULATED_DAYS("累积天数"),
        PERFORMANCE_BASED("绩效导向");

        private final String description;

        RestArrangementStrategy(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 加班配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OvertimeConfig {
        /**
         * 是否允许加班
         */
        private Boolean allowOvertime;

        /**
         * 每天最大加班时长（分钟）
         */
        private Integer maxDailyOvertimeMinutes;

        /**
         * 每周最大加班时长（分钟）
         */
        private Integer maxWeeklyOvertimeMinutes;

        /**
         * 加班申请提前时间（小时）
         */
        private Integer overtimeAdvanceHours;

        /**
         * 加班审批规则
         */
        private String overtimeApprovalRule;

        /**
         * 加班补偿方式
         */
        private OvertimeCompensationType compensationType;

        /**
         * 加班工资倍数
         */
        private Double overtimeWageMultiplier;
    }

    /**
     * 加班补偿类型
     */
    public enum OvertimeCompensationType {
        PAYMENT("加班费"),
        TIME_OFF("调休"),
        MIXED("混合补偿");

        private final String description;

        OvertimeCompensationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 验证配置是否有效
     */
    public boolean isValid() {
        if (systemName == null || systemName.trim().isEmpty()) {
            return false;
        }

        if (systemType == null || cycleType == null) {
            return false;
        }

        if (cycleDays != null && cycleDays <= 0) {
            return false;
        }

        if (shiftConfigs != null && !shiftConfigs.isEmpty()) {
            for (ShiftConfig shiftConfig : shiftConfigs) {
                if (shiftConfig.getShiftName() == null || shiftConfig.getWorkStartTime() == null || shiftConfig.getWorkEndTime() == null) {
                    return false;
                }
            }
        }

        if (effectiveDate != null && expiryDate != null && effectiveDate.isAfter(expiryDate)) {
            return false;
        }

        return true;
    }

    /**
     * 获取轮班制度摘要
     */
    public String getSystemSummary() {
        StringBuilder summary = new StringBuilder();

        summary.append(systemName != null ? systemName : "未命名制度");
        summary.append(" - ");
        summary.append(systemType != null ? systemType.getDescription() : "未知类型");

        if (cycleType != null && cycleDays != null) {
            summary.append(" (");
            summary.append(cycleType.getDescription());
            summary.append(cycleDays);
            summary.append("天)");
        }

        if (shiftConfigs != null) {
            summary.append(" - ");
            summary.append(shiftConfigs.size());
            summary.append("个班次");
        }

        return summary.toString();
    }
}
