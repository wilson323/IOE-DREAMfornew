package net.lab1024.sa.attendance.solver.constraint;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.solver.model.AttendanceScheduleSolution;
import net.lab1024.sa.attendance.solver.model.Employee;
import net.lab1024.sa.attendance.solver.model.Shift;
import net.lab1024.sa.attendance.solver.model.ShiftAssignment;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考勤排班约束提供者
 *
 * 定义智能排班的所有约束条件，分为硬约束（必须满足）和软约束（应该满足）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Slf4j
@Component
public class AttendanceScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            // ==================== 硬约束（必须满足） ====================

            // 约束1: 同一员工不能分配到时间冲突的班次
            employeeTimeConflict(constraintFactory),

            // 约束2: 员工必须具备班次所需的技能
            requiredSkillNotSatisfied(constraintFactory),

            // 约束3: 员工每天班次数不能超过上限
            maxShiftsPerDayExceeded(constraintFactory),

            // 约束4: 相邻班次之间必须满足最小休息时间
            minRestHoursNotSatisfied(constraintFactory),

            // 约束5: 员工连续工作天数不能超过上限
            maxConsecutiveDaysExceeded(constraintFactory),

            // ==================== 软约束（应该满足） ====================

            // 约束6: 员工班次偏好匹配度
            shiftPreferenceMismatch(constraintFactory),

            // 约束7: 工作负载公平性
            workloadFairness(constraintFactory),

            // 约束8: 成本优化
            costOptimization(constraintFactory),

            // 约束9: 员工满意度
            employeeSatisfaction(constraintFactory),

            // 约束10: 未分配班次惩罚
            unassignedShiftPenalty(constraintFactory)
        };
    }

    /**
     * 硬约束1: 同一员工不能分配到时间冲突的班次
     * 惩罚值: 每个冲突 -1000hard
     */
    private Constraint employeeTimeConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
            .from(ShiftAssignment.class)
            .join(ShiftAssignment.class)
            .filter((assignment1, assignment2) ->
                assignment1.getEmployee() != null &&
                assignment2.getEmployee() != null &&
                assignment1.getEmployee().equals(assignment2.getEmployee()) &&
                !assignment1.equals(assignment2) &&
                assignment1.getShift() != null &&
                assignment2.getShift() != null &&
                assignment1.getShift().hasTimeConflict(assignment2.getShift()))
            .penalize("员工时间冲突", HardSoftScore.ONE_HARD,
                (assignment1, assignment2) -> {
                    int conflictScore = assignment1.calculateConflictScore(assignment2);
                    log.warn("[排班约束] 员工时间冲突: employee={}, shift1={}, shift2={}, score={}",
                        assignment1.getEmployee().getName(),
                        assignment1.getShift().getShiftName(),
                        assignment2.getShift().getShiftName(),
                        conflictScore);
                    return Math.abs(conflictScore);
                });
    }

    /**
     * 硬约束2: 员工必须具备班次所需的技能
     * 惩罚值: 每个技能不匹配 -1000hard
     */
    private Constraint requiredSkillNotSatisfied(ConstraintFactory constraintFactory) {
        return constraintFactory
            .from(ShiftAssignment.class)
            .filter(assignment ->
                assignment.getEmployee() != null &&
                assignment.getShift() != null &&
                !assignment.satisfiesSkillRequirements())
            .penalize("技能不满足", HardSoftScore.ONE_HARD,
                assignment -> {
                    List<String> missingSkills = assignment.getShift().getRequiredSkills().stream()
                        .filter(skill -> !assignment.getEmployee().hasSkill(skill))
                        .collect(Collectors.toList());
                    log.warn("[排班约束] 技能不满足: employee={}, shift={}, missingSkills={}",
                        assignment.getEmployee().getName(),
                        assignment.getShift().getShiftName(),
                        missingSkills);
                    return missingSkills.size() * 1000;
                });
    }

    /**
     * 硬约束3: 员工每天班次数不能超过上限
     * 惩罚值: 每超额1个班次 -100hard
     */
    private Constraint maxShiftsPerDayExceeded(ConstraintFactory constraintFactory) {
        return constraintFactory
            .from(ShiftAssignment.class)
            .filter(assignment -> assignment.getEmployee() != null && assignment.getShift() != null)
            .groupBy(
                assignment -> assignment.getEmployee(),
                assignment -> assignment.getShift().getShiftDate(),
                assignment -> assignment.getShift().getShiftDate(),
                ConstraintCollectors.count())
            .filter((employee, date, count) -> count > employee.getMaxShiftsPerDay())
            .penalize("超过每日最大班次数", HardSoftScore.ofHard(100),
                (employee, date, count) -> {
                    int excess = count - employee.getMaxShiftsPerDay();
                    log.warn("[排班约束] 超过每日最大班次数: employee={}, date={}, assigned={}, max={}",
                        employee.getName(), date, count, employee.getMaxShiftsPerDay());
                    return excess;
                });
    }

    /**
     * 硬约束4: 相邻班次之间必须满足最小休息时间
     * 惩罚值: 每小时休息时间不足 -100hard
     */
    private Constraint minRestHoursNotSatisfied(ConstraintFactory constraintFactory) {
        return constraintFactory
            .from(ShiftAssignment.class)
            .filter(assignment -> assignment.getEmployee() != null && assignment.getShift() != null)
            .join(ShiftAssignment.class)
            .filter((assignment1, assignment2) ->
                assignment1.getEmployee().equals(assignment2.getEmployee()) &&
                !assignment1.equals(assignment2) &&
                assignment1.getShift() != null &&
                assignment2.getShift() != null &&
                assignment1.getShift().getShiftDate() != null &&
                assignment2.getShift().getShiftDate() != null)
            .penalize("休息时间不足", HardSoftScore.ofHard(100),
                (assignment1, assignment2) -> {
                    long restHours = calculateRestHours(assignment1, assignment2);
                    long minRestRequired = assignment1.getEmployee().getMinRestHours();
                    if (restHours < minRestRequired) {
                        long deficit = minRestRequired - restHours;
                        log.warn("[排班约束] 休息时间不足: employee={}, shift1={}, shift2={}, restHours={}, required={}",
                            assignment1.getEmployee().getName(),
                            assignment1.getShift().getShiftName(),
                            assignment2.getShift().getShiftName(),
                            restHours, minRestRequired);
                        return (int) deficit;
                    }
                    return 0;
                });
    }

    /**
     * 硬约束5: 员工连续工作天数不能超过上限
     * 惩罚值: 每超额1天 -100hard
     */
    private Constraint maxConsecutiveDaysExceeded(ConstraintFactory constraintFactory) {
        return constraintFactory
            .from(ShiftAssignment.class)
            .filter(assignment -> assignment.getEmployee() != null && assignment.getShift() != null)
            .groupBy(
                assignment -> assignment.getEmployee(),
                ConstraintCollectors.toSet(assignment -> assignment.getShift().getShiftDate()))
            .filter((employee, dates) -> {
                long consecutiveDays = calculateMaxConsecutiveDays(dates);
                return consecutiveDays > employee.getMaxConsecutiveShifts();
            })
            .penalize("超过最大连续工作天数", HardSoftScore.ofHard(100),
                (employee, dates) -> {
                    long consecutiveDays = calculateMaxConsecutiveDays(dates);
                    int excess = (int) (consecutiveDays - employee.getMaxConsecutiveShifts());
                    log.warn("[排班约束] 超过最大连续工作天数: employee={}, consecutiveDays={}, max={}",
                        employee.getName(), consecutiveDays, employee.getMaxConsecutiveShifts());
                    return excess;
                });
    }

    /**
     * 软约束6: 员工班次偏好匹配度
     * 奖励值: 每个偏好匹配 +10soft
     */
    private Constraint shiftPreferenceMismatch(ConstraintFactory constraintFactory) {
        return constraintFactory
            .from(ShiftAssignment.class)
            .filter(assignment ->
                assignment.getEmployee() != null &&
                assignment.getShift() != null &&
                assignment.getEmployee().getShiftPreferences() != null)
            .reward("班次偏好匹配", HardSoftScore.ofSoft(10),
                assignment -> {
                    // 简化实现：基于班次类型匹配偏好
                    String shiftType = assignment.getShift().getShiftType();
                    String preferences = assignment.getEmployee().getShiftPreferences();
                    int score = 0;
                    if (preferences.contains(shiftType)) {
                        score = 10;
                    }
                    assignment.setPreferenceScore(score);
                    log.debug("[排班约束] 班次偏好匹配: employee={}, shiftType={}, score={}",
                        assignment.getEmployee().getName(), shiftType, score);
                    return score;
                });
    }

    /**
     * 软约束7: 工作负载公平性
     * 惩罚值: 与平均工作负载的偏差每1小时 -1soft
     */
    private Constraint workloadFairness(ConstraintFactory constraintFactory) {
        return constraintFactory
            .from(ShiftAssignment.class)
            .filter(assignment -> assignment.getEmployee() != null && assignment.getShift() != null)
            .groupBy(
                assignment -> assignment.getEmployee(),
                ConstraintCollectors.sum(assignment -> (long) assignment.getShift().getDurationHours()))
            .join(ShiftAssignment.class)
            .filter((pair, assignment) ->
                assignment.getEmployee() != null &&
                assignment.getShift() != null &&
                assignment.getEmployee().equals(pair.getKey()))
            .penalize("工作负载不均", HardSoftScore.ONE_SOFT,
                (pair, assignment) -> {
                    Map.Entry<Employee, Long> entry = (Map.Entry<Employee, Long>) pair;
                    long employeeHours = entry.getValue();
                    // 简化实现：假设平均工作负载为40小时/周
                    long averageHours = 40;
                    long deviation = Math.abs(employeeHours - averageHours);
                    assignment.setFairnessScore((int) -deviation);
                    log.debug("[排班约束] 工作负载: employee={}, hours={}, deviation={}",
                        entry.getKey().getName(), employeeHours, deviation);
                    return (int) deviation;
                });
    }

    /**
     * 软约束8: 成本优化
     * 惩罚值: 每个成本等级单位 -5soft
     */
    private Constraint costOptimization(ConstraintFactory constraintFactory) {
        return constraintFactory
            .from(ShiftAssignment.class)
            .filter(assignment ->
                assignment.getEmployee() != null &&
                assignment.getShift() != null)
            .penalize("成本优化", HardSoftScore.ofSoft(5),
                assignment -> {
                    int costLevel = assignment.getEmployee().getCostLevel();
                    double duration = assignment.getShift().getDurationHours();
                    int costScore = (int) (costLevel * duration);
                    log.debug("[排班约束] 成本计算: employee={}, shift={}, costLevel={}, duration={}, score={}",
                        assignment.getEmployee().getName(),
                        assignment.getShift().getShiftName(),
                        costLevel, duration, costScore);
                    return costScore;
                });
    }

    /**
     * 软约束9: 员工满意度
     * 奖励值: 每个满意度单位 +1soft
     */
    private Constraint employeeSatisfaction(ConstraintFactory constraintFactory) {
        return constraintFactory
            .from(ShiftAssignment.class)
            .filter(assignment ->
                assignment.getEmployee() != null &&
                assignment.getShift() != null)
            .reward("员工满意度", HardSoftScore.ONE_SOFT,
                assignment -> {
                    // 综合偏好匹配度和公平性计算满意度
                    int preferenceScore = assignment.getPreferenceScore() != null ?
                        assignment.getPreferenceScore() : 0;
                    int fairnessScore = assignment.getFairnessScore() != null ?
                        assignment.getFairnessScore() : 0;
                    int satisfactionScore = (preferenceScore + fairnessScore + 100) / 2;
                    log.debug("[排班约束] 员工满意度: employee={}, shift={}, satisfactionScore={}",
                        assignment.getEmployee().getName(),
                        assignment.getShift().getShiftName(),
                        satisfactionScore);
                    return Math.max(0, satisfactionScore);
                });
    }

    /**
     * 软约束10: 未分配班次惩罚
     * 惩罚值: 每个未分配班次 -1000soft
     */
    private Constraint unassignedShiftPenalty(ConstraintFactory constraintFactory) {
        return constraintFactory
            .from(ShiftAssignment.class)
            .filter(assignment -> assignment.getEmployee() == null && assignment.getShift() != null)
            .penalize("未分配班次", HardSoftScore.ofSoft(1000),
                assignment -> {
                    log.warn("[排班约束] 未分配班次: shift={}, date={}",
                        assignment.getShift().getShiftName(),
                        assignment.getShift().getShiftDate());
                    return 1;
                });
    }

    /**
     * 计算两个班次之间的休息时间（小时）
     */
    private long calculateRestHours(ShiftAssignment assignment1, ShiftAssignment assignment2) {
        if (assignment1.getShift().getEndTime() == null ||
            assignment2.getShift().getStartTime() == null) {
            return 24; // 默认24小时
        }

        // 判断时间顺序
        if (assignment1.getShift().getEndTime().isBefore(assignment2.getShift().getStartTime())) {
            return Duration.between(
                assignment1.getShift().getEndTime(),
                assignment2.getShift().getStartTime()
            ).toHours();
        } else {
            return Duration.between(
                assignment2.getShift().getEndTime(),
                assignment1.getShift().getStartTime()
            ).toHours();
        }
    }

    /**
     * 计算最大连续工作天数
     */
    private long calculateMaxConsecutiveDays(java.util.Set<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return 0;
        }

        List<LocalDate> sortedDates = dates.stream()
            .sorted()
            .collect(Collectors.toList());

        long maxConsecutive = 1;
        long currentConsecutive = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate prev = sortedDates.get(i - 1);
            LocalDate curr = sortedDates.get(i);

            if (curr.equals(prev.plusDays(1))) {
                currentConsecutive++;
            } else {
                maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
                currentConsecutive = 1;
            }
        }

        return Math.max(maxConsecutive, currentConsecutive);
    }
}
