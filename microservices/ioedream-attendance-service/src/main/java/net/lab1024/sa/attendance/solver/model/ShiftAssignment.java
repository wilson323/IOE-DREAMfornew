package net.lab1024.sa.attendance.solver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 * 班次分配实体 - OptaPlanner Planning Entity
 *
 * 核心概念:
 * - Planning Entity: OptaPlanner会优化这些实体的分配
 * - PlanningVariable: 可变化的规划变量 (Employee)
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PlanningEntity
public class ShiftAssignment {

    /**
     * 分配ID
     */
    private Long id;

    /**
     * 班次 - Problem Fact (不可变)
     * 使用@PlanningVariableReference关联到PlanningVariable
     */
    private Shift shift;

    /**
     * 员工 - Planning Variable (可变)
     * OptaPlanner会自动从employeeRange中选择合适的员工
     * valueRangeProviderRefs = "employeeRange" 对应AttendanceScheduleSolution中定义的值范围
     */
    @PlanningVariable(valueRangeProviderRefs = "employeeRange", nullable = true)
    private Employee employee;

    /**
     * 分配日期
     */
    private java.time.LocalDate assignmentDate;

    /**
     * 分配状态 (ASSIGNED-已分配, UNASSIGNED-未分配, CANCELLED-已取消)
     */
    @Builder.Default
    private String assignmentStatus = "UNASSIGNED";

    /**
     * 是否固定 (固定后OptaPlanner不会修改)
     */
    @Builder.Default
    private boolean pinned = false;

    /**
     * 偏好得分 (0-100, 员工对该班次的偏好程度)
     */
    private Integer preferenceScore;

    /**
     * 公平性得分 (0-100, 该分配的公平程度)
     */
    private Integer fairnessScore;

    /**
     * 连续工作天数
     */
    private Integer consecutiveDays;

    // ============================================================
    // Computed Properties (计算属性)
    // ============================================================

    /**
     * 检查是否已分配员工
     */
    public boolean isAssigned() {
        return employee != null;
    }

    /**
     * 检查是否满足技能要求
     */
    public boolean satisfiesSkillRequirements() {
        if (shift == null || employee == null) {
            return false;
        }

        if (shift.getRequiredSkills() == null || shift.getRequiredSkills().isEmpty()) {
            return true; // 无技能要求
        }

        return employee.hasAllSkills(shift.getRequiredSkills());
    }

    /**
     * 计算时间冲突得分 (负数表示冲突)
     */
    public int calculateConflictScore(ShiftAssignment other) {
        if (this.employee == null || other.employee == null ||
            !this.employee.getId().equals(other.employee.getId())) {
            return 0; // 不是同一员工，无冲突
        }

        if (this.shift == null || other.shift == null) {
            return 0;
        }

        if (this.shift.hasTimeConflict(other.shift)) {
            return -1000; // 严重冲突
        }

        return 0;
    }

    /**
     * 计算连续工作天数
     */
    public int calculateConsecutiveDays(java.time.LocalDate currentDate, java.util.List<ShiftAssignment> allAssignments) {
        if (employee == null) {
            return 0;
        }

        int consecutiveDays = 0;
        java.time.LocalDate checkDate = currentDate.minusDays(1);

        // 向前查找连续工作天数
        while (true) {
            final java.time.LocalDate searchDate = checkDate;
            boolean hasShiftOnDate = allAssignments.stream()
                    .filter(sa -> sa.getEmployee() != null &&
                                 sa.getEmployee().getId().equals(employee.getId()))
                    .anyMatch(sa -> sa.getShift() != null &&
                                    sa.getShift().isOnDate(searchDate));

            if (hasShiftOnDate) {
                consecutiveDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }

        return consecutiveDays;
    }

    /**
     * 计算该分配的质量得分
     */
    public int calculateQualityScore() {
        if (!isAssigned()) {
            return 0;
        }

        int score = 0;

        // 1. 技能匹配得分 (50分)
        if (satisfiesSkillRequirements()) {
            score += 50;
        }

        // 2. 偏好得分 (30分)
        if (preferenceScore != null) {
            score += (preferenceScore * 30) / 100;
        }

        // 3. 公平性得分 (20分)
        if (fairnessScore != null) {
            score += (fairnessScore * 20) / 100;
        }

        return score;
    }

    /**
     * 获取分配摘要信息
     */
    public String getSummary() {
        if (shift == null) {
            return "ShiftAssignment: No shift";
        }

        if (employee == null) {
            return String.format("ShiftAssignment: %s (Unassigned)",
                                shift.getShiftName());
        }

        return String.format("ShiftAssignment: %s -> %s (%s)",
                            shift.getShiftName(),
                            employee.getName(),
                            assignmentStatus);
    }
}
