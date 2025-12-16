package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 排班数据模型
 * <p>
 * 智能排班引擎的核心数据结构
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
public class ScheduleData {

    /**
     * 计划ID
     */
    private Long planId;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 时间范围（天）
     */
    private Integer timeRange;

    /**
     * 员工列表
     */
    private List<EmployeeData> employees;

    /**
     * 可用班次列表
     */
    private List<ShiftData> availableShifts;

    /**
     * 部门列表
     */
    private List<DepartmentData> departments;

    /**
     * 技能列表
     */
    private List<SkillData> skills;

    /**
     * 约束条件
     */
    private ScheduleConstraints constraints;

    /**
     * 优化目标
     */
    private OptimizationTarget optimizationTarget;

    /**
     * 历史排班数据
     */
    private List<ScheduleRecord> historyRecords;

    /**
     * 排班偏好设置
     */
    private Map<Long, EmployeePreference> preferences;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 员工数据内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeData {
        private Long employeeId;
        private String employeeName;
        private String employeeCode;
        private Long departmentId;
        private String departmentName;
        private String position;
        private Integer level;
        private List<String> skills;
        private Map<String, Integer> skillProficiency; // 技能熟练度 0-100
        private Set<LocalDate> availableDates; // 可用日期
        private Set<String> preferredShiftTypes; // 偏好班次类型
        private Set<DayOfWeek> preferredDaysOff; // 偏好休息日
        private Double hourlyRate; // 小时费率
        private Integer maxWorkingHours; // 最大工作时长
        private Integer minWorkingHours; // 最小工作时长
        private Integer maxConsecutiveDays; // 最大连续工作天数
        private Boolean prefersContinuousShifts; // 是否偏好连续班次
        private Map<String, Object> employeeAttributes; // 员工扩展属性

        // 动态计算属性
        private Double skillScore; // 技能得分
        private Double availabilityScore; // 可用性得分
        private Double costEfficiency; // 成本效率
    }

    /**
     * 班次数据内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftData {
        private Long shiftId;
        private String shiftName;
        private String shiftCode;
        private String shiftType; // NORMAL, PEAK, NIGHT, OVERTIME
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer duration; // 持续时间（分钟）
        private Integer requiredEmployeeCount; // 需要员工数量
        private Integer minEmployeeCount; // 最少员工数量
        private List<String> requiredSkills; // 必需技能
        private List<String> preferredSkills; // 优先技能
        private String workLocation; // 工作地点
        private String departmentId; // 所属部门
        private Double difficulty; // 难度系数 0-1
        private Double importance; // 重要性 0-1
        private Boolean isCritical; // 是否关键岗位
        private Set<DayOfWeek> activeDays; // 激活日期
        private Map<String, Object> shiftAttributes; // 班次扩展属性

        // 动态计算属性
        private Double difficulty; // 班次难度
        private Double importance; // 班次重要性
    }

    /**
     * 部门数据内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentData {
        private Long departmentId;
        private String departmentName;
        private String departmentCode;
        private Long parentId; // 父部门ID
        private Integer level; // 部门层级
        private String managerId; // 部门经理ID
        private Integer employeeCount; // 员工数量
        private Map<String, Object> departmentAttributes;
    }

    /**
     * 技能数据内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillData {
        private String skillCode;
        private String skillName;
        private String category; // 技能分类
        private String description;
        private Integer level; // 技能等级
        private Map<String, Object> skillAttributes;
    }

    /**
     * 约束条件内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleConstraints {
        // 工作时间约束
        private Integer dailyMaxHours; // 每日最大工作时长
        private Integer weeklyMaxHours; // 每周最大工作时长
        private Integer minRestHours; // 最小休息时长
        private Integer maxConsecutiveDays; // 最大连续工作天数

        // 技能约束
        private Boolean mustMatchSkills; // 是否必须匹配技能
        private Double minSkillMatchRatio; // 最小技能匹配比例

        // 公平性约束
        private Double workloadBalanceThreshold; // 工作负载均衡阈值
        private Integer maxShiftDifference; // 最大班次差异数

        // 法律约束
        private Integer maxWeeklyHours; // 每周最大工作时长（法律）
        private Integer minRestDays; // 最少休息天数
        private Boolean respectHolidays; // 是否尊重节假日

        // 业务约束
        private Boolean ensureCriticalCoverage; // 确保关键岗位覆盖
        private Integer minCoverageRate; // 最小覆盖率
        private Map<String, Object> customConstraints; // 自定义约束
    }

    /**
     * 优化目标内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationTarget {
        private String primaryTarget; // 主要目标：BALANCE, COST, PREFERENCE, EFFICIENCY
        private List<String> secondaryTargets; // 次要目标
        private Map<String, Double> weights; // 目标权重
        private Map<String, Object> optimizationParameters; // 优化参数
    }

    /**
     * 员工偏好内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeePreference {
        private Long employeeId;
        private Map<String, Integer> shiftTypePreference; // 班次类型偏好分数
        private Map<DayOfWeek, Integer> dayPreference; // 日期偏好分数
        private Map<String, Integer> locationPreference; // 地点偏好分数
        private Map<Long, Integer> colleaguePreference; // 同事偏好分数
        private Integer workLifeBalance; // 工作生活平衡偏好
        private Map<String, Object> customPreferences; // 自定义偏好
    }

    /**
     * 排班记录内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleRecord {
        private Long recordId;
        private Long employeeId;
        private Long shiftId;
        private LocalDate scheduleDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String workLocation;
        private Integer actualDuration; // 实际工作时长
        private String status; // SCHEDULED, COMPLETED, ABSENT
        private String source; // AUTO, MANUAL, SYSTEM
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
        private Map<String, Object> recordAttributes; // 记录扩展属性
    }
}