package net.lab1024.sa.attendance.solver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤排班解决方案 - OptaPlanner PlanningSolution
 *
 * 核心概念:
 * - PlanningSolution: 代表一个完整的排班解决方案
 * - PlanningEntity: 可变化的规划实体 (ShiftAssignment)
 * - ProblemFact: 不可变的问题事实 (Employee, Shift)
 * - PlanningScore: 方案质量评分 (HardSoftScore)
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PlanningSolution
public class AttendanceScheduleSolution {

    // ============================================================
    // Problem Facts (不可变的问题事实)
    // ============================================================

    /**
     * 员工列表 - 不可变的事实
     */
    @ProblemFactCollectionProperty
    private List<Employee> employees;

    /**
     * 班次列表 - 不可变的事实
     */
    @ProblemFactCollectionProperty
    private List<Shift> shifts;

    // ============================================================
    // Planning Entities (可变化的规划实体)
    // ============================================================

    /**
     * 班次分配列表 - OptaPlanner会优化这些实体的分配
     */
    @PlanningEntityCollectionProperty
    private List<ShiftAssignment> shiftAssignments;

    // ============================================================
    // Score (方案评分)
    // ============================================================

    /**
     * HardSoftScore: 硬约束得分 + 软约束得分
     * - Hard Score: 必须满足的约束(负数表示违规次数)
     * - Soft Score: 尽量满足的约束(负数表示未满足程度)
     */
    @PlanningScore
    private HardSoftScore score;

    // ============================================================
    // Value Range Providers (值范围提供者)
    // ============================================================

    /**
     * 员工值范围 - 供ShiftAssignment使用
     * ShiftAssignment的employee字段会从这个范围中选择
     */
    @ValueRangeProvider(id = "employeeRange")
    public List<Employee> getEmployeeRange() {
        return employees;
    }

    /**
     * 班次值范围 - 供ShiftAssignment使用
     * ShiftAssignment的shift字段会从这个范围中选择
     */
    @ValueRangeProvider(id = "shiftRange")
    public List<Shift> getShiftRange() {
        return shifts;
    }

    // ============================================================
    // Solution Metadata (方案元数据)
    // ============================================================

    /**
     * 方案ID
     */
    private Long planId;

    /**
     * 方案名称
     */
    private String planName;

    /**
     * 排班计划开始日期
     */
    private LocalDate planningHorizonStart;

    /**
     * 排班计划结束日期
     */
    private LocalDate planningHorizonEnd;

    // ============================================================
    // Computed Properties (计算属性)
    // ============================================================

    /**
     * 获取方案难度等级
     */
    public String getDifficultyLevel() {
        int totalEmployees = employees.size();
        int totalShifts = shifts.size();
        int totalAssignments = shiftAssignments.size();

        // 计算搜索空间大小: employees ^ shiftAssignments
        double searchSpace = Math.pow(totalEmployees, totalAssignments);

        if (searchSpace > 1e100) {
            return "EXTREME"; // 极难
        } else if (searchSpace > 1e50) {
            return "HARD";    // 困难
        } else if (searchSpace > 1e20) {
            return "MEDIUM";  // 中等
        } else {
            return "EASY";    // 简单
        }
    }

    /**
     * 获取求解统计信息
     */
    public String getSolvingStatistics() {
        return String.format(
            "Employees: %d, Shifts: %d, Assignments: %d, Score: %s",
            employees.size(),
            shifts.size(),
            shiftAssignments.size(),
            score != null ? score.toString() : "N/A"
        );
    }

    /**
     * 计算员工利用率
     */
    public double calculateUtilizationRate() {
        if (employees.isEmpty()) {
            return 0.0;
        }

        long assignedCount = shiftAssignments.stream()
                .filter(sa -> sa.getEmployee() != null)
                .map(sa -> sa.getEmployee().getId())
                .distinct()
                .count();

        return (double) assignedCount / employees.size() * 100.0;
    }

    /**
     * 计算班次分配完成率
     */
    public double calculateAssignmentCompletionRate() {
        if (shiftAssignments.isEmpty()) {
            return 0.0;
        }

        long assignedCount = shiftAssignments.stream()
                .filter(sa -> sa.getEmployee() != null)
                .count();

        return (double) assignedCount / shiftAssignments.size() * 100.0;
    }
}
