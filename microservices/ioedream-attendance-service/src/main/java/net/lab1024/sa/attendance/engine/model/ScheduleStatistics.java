package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 排班统计模型
 * <p>
 * 排班统计的数据结构
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
public class ScheduleStatistics {

    /**
     * 统计ID
     */
    private String statisticsId;

    /**
     * 统计类型
     */
    private String statisticsType;

    /**
     * 统计时间范围
     */
    private String timeRange;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    /**
     * 总员工数
     */
    private Integer totalEmployees;

    /**
     * 总排班天数
     */
    private Integer totalScheduleDays;

    /**
     * 总排班记录数
     */
    private Integer totalScheduleRecords;

    /**
     * 平均每人排班天数
     */
    private Double averageDaysPerEmployee;

    /**
     * 工作负载均衡度
     */
    private Double workloadBalance;

    /**
     * 班次覆盖率
     */
    private Double shiftCoverage;

    /**
     * 约束满足率
     */
    private Double constraintSatisfaction;

    /**
     * 成本节约率
     */
    private Double costSavingRate;

    /**
     * 部门统计
     */
    private Map<Long, DepartmentStatistics> departmentStatistics;

    /**
     * 员工统计
     */
    private Map<Long, EmployeeStatistics> employeeStatistics;

    /**
     * 班次统计
     */
    private Map<Long, ShiftStatistics> shiftStatistics;

    /**
     * 趋势数据
     */
    private List<TrendData> trendData;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentStatistics {
        private Long departmentId;
        private String departmentName;
        private Integer employeeCount;
        private Integer totalShifts;
        private Double averageWorkload;
        private Double coverageRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeStatistics {
        private Long employeeId;
        private String employeeName;
        private Integer assignedShifts;
        private Double totalWorkHours;
        private Double utilizationRate;
        private List<String> assignedSkills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftStatistics {
        private Long shiftId;
        private String shiftName;
        private Integer assignedCount;
        private Integer requiredCount;
        private Double coverageRate;
        private List<Long> assignedEmployees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private String date;
        private Double workloadIndex;
        private Double coverageIndex;
        private Double costIndex;
        private Map<String, Object> trendDetails;
    }
}