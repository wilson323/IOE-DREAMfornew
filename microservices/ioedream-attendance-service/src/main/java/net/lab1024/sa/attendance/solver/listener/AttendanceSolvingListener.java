package net.lab1024.sa.attendance.solver.listener;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.solver.model.AttendanceScheduleSolution;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.api.solver.change.ProblemChange;
import org.optaplanner.core.api.solver.event.SolverEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 排班求解监听器
 *
 * 监控OptaPlanner求解过程，记录求解进度、最佳分数变化等信息
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Slf4j
@Component
public class AttendanceSolvingListener implements SolverEventListener<AttendanceScheduleSolution> {

    private LocalDateTime solvingStartTime;
    private long bestScoreTimestamp;
    private int bestHardScore;
    private int bestSoftScore;
    private long totalSteps;

    @Override
    public void solvingStarted(SolverEvent<AttendanceScheduleSolution> event) {
        solvingStartTime = LocalDateTime.now();
        bestScoreTimestamp = System.currentTimeMillis();
        bestHardScore = Integer.MIN_VALUE;
        bestSoftScore = Integer.MIN_VALUE;
        totalSteps = 0;

        log.info("[排班求解] ========== 求解开始 ==========");
        log.info("[排班求解] 开始时间: {}", solvingStartTime);
        log.info("[排班求解] 问题规模: {} 员工, {} 班次, {} 分配",
            event.getScoreDirector().getWorkingSolution().getEmployees().size(),
            event.getScoreDirector().getWorkingSolution().getShifts().size(),
            event.getScoreDirector().getWorkingSolution().getShiftAssignments().size());

        String difficulty = event.getScoreDirector().getWorkingSolution().getDifficultyLevel();
        log.info("[排班求解] 难度等级: {}", difficulty);
    }

    @Override
    public void solvingEnded(SolverEvent<AttendanceScheduleSolution> event) {
        LocalDateTime solvingEndTime = LocalDateTime.now();
        Duration duration = Duration.between(solvingStartTime, solvingEndTime);

        log.info("[排班求解] ========== 求解结束 ==========");
        log.info("[排班求解] 结束时间: {}", solvingEndTime);
        log.info("[排班求解] 总耗时: {} 秒", duration.toSecondsPart());
        log.info("[排班求解] 总步数: {}", totalSteps);

        AttendanceScheduleSolution bestSolution = event.getBestSolution();
        if (bestSolution != null && bestSolution.getScore() != null) {
            log.info("[排班求解] 最终分数: {}", bestSolution.getScore());
            log.info("[排班求解] 硬约束分数: {}", bestSolution.getScore().hardScore());
            log.info("[排班求解] 软约束分数: {}", bestSolution.getScore().softScore());
        }

        log.info("[排班求解] ======================================");
    }

    @Override
    public void stepEnded(SolverEvent<AttendanceScheduleSolution> event) {
        totalSteps++;

        AttendanceScheduleSolution currentSolution = event.getNewBestSolution();
        if (currentSolution != null && currentSolution.getScore() != null) {
            int currentHardScore = currentSolution.getScore().hardScore();
            int currentSoftScore = currentSolution.getScore().softScore();

            // 检测到最佳分数更新
            if (currentHardScore > bestHardScore ||
                (currentHardScore == bestHardScore && currentSoftScore > bestSoftScore)) {

                long currentTime = System.currentTimeMillis();
                long timeSinceLastImprovement = currentTime - bestScoreTimestamp;

                bestHardScore = currentHardScore;
                bestSoftScore = currentSoftScore;
                bestScoreTimestamp = currentTime;

                log.info("[排班求解] 最佳分数更新: 步数={}, 分数=[{}|{}], 距上次改进={}ms",
                    totalSteps,
                    currentHardScore,
                    currentSoftScore,
                    timeSinceLastImprovement);

                // 每100步输出详细统计
                if (totalSteps % 100 == 0) {
                    logSolverStatistics(currentSolution);
                }
            }
        }

        // 每10步输出进度
        if (totalSteps % 10 == 0) {
            log.debug("[排班求解] 进度: 步数={}", totalSteps);
        }
    }

    @Override
    public void problemChanged(SolverEvent<AttendanceScheduleSolution> event) {
        log.debug("[排班求解] 问题变更检测");
    }

    /**
     * 输出求解统计信息
     */
    private void logSolverStatistics(AttendanceScheduleSolution solution) {
        log.info("[排班求解] ========== 求解统计 ==========");

        // 员工分配统计
        if (solution.getShiftAssignments() != null) {
            long assignedCount = solution.getShiftAssignments().stream()
                .filter(assignment -> assignment.getEmployee() != null)
                .count();

            long unassignedCount = solution.getShiftAssignments().size() - assignedCount;

            log.info("[排班求解] 分配统计: 已分配={}, 未分配={}, 总计={}",
                assignedCount, unassignedCount, solution.getShiftAssignments().size());

            // 员工工作负载统计
            solution.getEmployees().forEach(employee -> {
                long assignmentCount = solution.getShiftAssignments().stream()
                    .filter(assignment -> employee.equals(assignment.getEmployee()))
                    .count();

                double totalHours = solution.getShiftAssignments().stream()
                    .filter(assignment -> employee.equals(assignment.getEmployee()) &&
                        assignment.getShift() != null)
                    .mapToDouble(assignment -> assignment.getShift().getDurationHours())
                    .sum();

                log.info("[排班求解] 员工负载: {}, 班次数={}, 总工时={}",
                    employee.getName(), assignmentCount, totalHours);
            });
        }

        log.info("[排班求解] ================================");
    }

    /**
     * 获取求解统计信息
     */
    public SolvingStatistics getStatistics() {
        return SolvingStatistics.builder()
            .solvingStartTime(solvingStartTime)
            .totalSteps(totalSteps)
            .bestHardScore(bestHardScore)
            .bestSoftScore(bestSoftScore)
            .build();
    }

    /**
     * 求解统计信息DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class SolvingStatistics {
        private LocalDateTime solvingStartTime;
        private long totalSteps;
        private int bestHardScore;
        private int bestSoftScore;

        public long getDurationSeconds() {
            if (solvingStartTime == null) {
                return 0;
            }
            return Duration.between(solvingStartTime, LocalDateTime.now()).getSeconds();
        }
    }

    /**
     * 问题变更适配器
     *
     * 用于运行时修改问题数据
     */
    @lombok.Getter
    @lombok.RequiredArgsConstructor
    public static class AttendanceProblemChange implements ProblemChange<AttendanceScheduleSolution> {

        private final String changeType;
        private final Runnable changeAction;

        @Override
        public void doChange(ScoreDirector<AttendanceScheduleSolution> scoreDirector) {
            log.info("[排班求解] 执行问题变更: type={}", changeType);

            try {
                // 执行变更动作
                changeAction.run();

                // 通知求解器问题已变更
                scoreDirector.lookUpWorkingSolution();
                scoreDirector.triggerVariableListeners();

                log.info("[排班求解] 问题变更完成: type={}", changeType);
            } catch (Exception e) {
                log.error("[排班求解] 问题变更失败: type={}, error={}", changeType, e.getMessage(), e);
                throw e;
            }
        }
    }
}
