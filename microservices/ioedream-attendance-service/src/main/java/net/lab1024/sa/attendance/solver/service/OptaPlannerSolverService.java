package net.lab1024.sa.attendance.solver.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.ScheduleEngine;
import net.lab1024.sa.attendance.engine.model.ScheduleRequest;
import net.lab1024.sa.attendance.engine.model.ScheduleResult;
import net.lab1024.sa.attendance.solver.config.OptaPlannerSolverConfig;
import net.lab1024.sa.attendance.solver.listener.AttendanceSolvingListener;
import net.lab1024.sa.attendance.solver.model.AttendanceScheduleSolution;
import net.lab1024.sa.attendance.solver.model.Employee;
import net.lab1024.sa.attendance.solver.model.Shift;
import net.lab1024.sa.attendance.solver.model.ShiftAssignment;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OptaPlanner智能排班服务
 *
 * 集成OptaPlanner约束求解引擎到现有排班系统
 * 性能目标: 100 employees × 30 days < 30 seconds
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Slf4j
@Service
public class OptaPlannerSolverService {

    @Resource
    private SolverFactory<AttendanceScheduleSolution> solverFactory;

    @Resource
    private Solver<AttendanceScheduleSolution> solver;

    @Resource
    private AttendanceSolvingListener solvingListener;

    @Resource
    private ScheduleEngine scheduleEngine;

    /**
     * 执行智能排班求解
     *
     * @param request 排班请求
     * @return 排班结果
     */
    public ScheduleResult solve(ScheduleRequest request) {
        log.info("[智能排班] 开始OptaPlanner求解: planId={}, startDate={}, endDate={}",
            request.getPlanId(), request.getStartDate(), request.getEndDate());

        long startTime = System.currentTimeMillis();

        try {
            // 1. 构建问题模型
            AttendanceScheduleSolution problem = buildProblem(request);
            log.info("[智能排班] 问题模型构建完成: {} 员工, {} 班次, {} 分配",
                problem.getEmployees().size(),
                problem.getShifts().size(),
                problem.getShiftAssignments().size());

            // 2. 执行求解
            AttendanceScheduleSolution bestSolution = solveInternal(problem);

            // 3. 转换结果
            ScheduleResult result = convertToScheduleResult(bestSolution);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("[智能排班] 求解完成: 耗时={}ms, 分数=[{}|{}]",
                duration,
                bestSolution.getScore().hardScore(),
                bestSolution.getScore().softScore());

            // 性能目标验证
            if (duration > 30000) {
                log.warn("[智能排班] 性能警告: 求解耗时={}ms, 超过30秒目标", duration);
            } else {
                log.info("[智能排班] 性能目标达成: {}ms < 30000ms", duration);
            }

            return result;

        } catch (Exception e) {
            log.error("[智能排班] 求解失败: planId={}, error={}",
                request.getPlanId(), e.getMessage(), e);
            throw new RuntimeException("智能排班求解失败: " + e.getMessage(), e);
        }
    }

    /**
     * 内部求解逻辑
     */
    private AttendanceScheduleSolution solveInternal(AttendanceScheduleSolution problem) {
        log.info("[智能排班] 开始求解: 难度等级={}", problem.getDifficultyLevel());

        // 添加求解监听器
        Solver<AttendanceScheduleSolution> solvingSolver = solver;
        // solvingSolver.addEventListener(solvingListener);

        // 执行求解
        AttendanceScheduleSolution bestSolution = solvingSolver.solve(problem);

        log.info("[智能排班] 求解完成: 最终分数={}", bestSolution.getScore());

        return bestSolution;
    }

    /**
     * 构建OptaPlanner问题模型
     */
    private AttendanceScheduleSolution buildProblem(ScheduleRequest request) {
        log.info("[智能排班] 构建问题模型");

        // 1. 提取员工数据
        List<Employee> employees = extractEmployees(request);

        // 2. 提取班次数据
        List<Shift> shifts = extractShifts(request);

        // 3. 创建分配任务
        List<ShiftAssignment> assignments = createAssignments(shifts);

        // 4. 构建解决方案对象
        AttendanceScheduleSolution solution = AttendanceScheduleSolution.builder()
            .employees(employees)
            .shifts(shifts)
            .shiftAssignments(assignments)
            .build();

        log.info("[智能排班] 问题模型构建完成");
        return solution;
    }

    /**
     * 从ScheduleRequest提取员工数据
     */
    private List<Employee> extractEmployees(ScheduleRequest request) {
        log.info("[智能排班] 提取员工数据");

        // TODO: 从现有系统获取员工数据
        // 这里应该调用现有的员工服务获取完整的员工信息，包括技能、约束等

        List<Employee> employees = new ArrayList<>();

        // 示例数据 - 实际应该从数据库或服务获取
        for (int i = 1; i <= 100; i++) {
            Employee employee = Employee.builder()
                .id((long) i)
                .name("员工" + i)
                .employeeCode("EMP" + String.format("%04d", i))
                .skills(List.of("普通技能"))
                .maxShiftsPerDay(3)
                .maxConsecutiveShifts(6)
                .minRestHours(11)
                .available(true)
                .costLevel(3)
                .build();

            employees.add(employee);
        }

        log.info("[智能排班] 员工数据提取完成: {} 个员工", employees.size());
        return employees;
    }

    /**
     * 从ScheduleRequest提取班次数据
     */
    private List<Shift> extractShifts(ScheduleRequest request) {
        log.info("[智能排班] 提取班次数据");

        List<Shift> shifts = new ArrayList<>();

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        // 为每一天创建班次（早班、中班、晚班）
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;

        for (int day = 0; day < daysBetween; day++) {
            LocalDate currentDate = startDate.plusDays(day);

            // 早班 (08:00-16:00)
            shifts.add(Shift.builder()
                .id((long) (day * 3 + 1))
                .shiftName(currentDate + "-早班")
                .startTime(currentDate.atTime(8, 0))
                .endTime(currentDate.atTime(16, 0))
                .shiftDate(currentDate)
                .shiftType("MORNING")
                .requiredSkills(List.of())
                .requiredEmployees(10)
                .location("办公区")
                .build());

            // 中班 (16:00-24:00)
            shifts.add(Shift.builder()
                .id((long) (day * 3 + 2))
                .shiftName(currentDate + "-中班")
                .startTime(currentDate.atTime(16, 0))
                .endTime(currentDate.atTime(0, 0).plusDays(1))
                .shiftDate(currentDate)
                .shiftType("AFTERNOON")
                .requiredSkills(List.of())
                .requiredEmployees(5)
                .location("办公区")
                .isCrossDay(true)
                .build());

            // 晚班 (00:00-08:00)
            shifts.add(Shift.builder()
                .id((long) (day * 3 + 3))
                .shiftName(currentDate + "-晚班")
                .startTime(currentDate.atTime(0, 0))
                .endTime(currentDate.atTime(8, 0))
                .shiftDate(currentDate)
                .shiftType("NIGHT")
                .requiredSkills(List.of())
                .requiredEmployees(3)
                .location("办公区")
                .build());
        }

        log.info("[智能排班] 班次数据提取完成: {} 个班次, {} 天",
            shifts.size(), daysBetween);

        return shifts;
    }

    /**
     * 创建分配任务
     */
    private List<ShiftAssignment> createAssignments(List<Shift> shifts) {
        log.info("[智能排班] 创建分配任务");

        List<ShiftAssignment> assignments = shifts.stream()
            .map(shift -> ShiftAssignment.builder()
                .id(shift.getId())
                .shift(shift)
                .assignmentDate(shift.getShiftDate())
                .assignmentStatus("UNASSIGNED")
                .pinned(false)
                .build())
            .collect(Collectors.toList());

        log.info("[智能排班] 分配任务创建完成: {} 个任务", assignments.size());

        return assignments;
    }

    /**
     * 转换OptaPlanner解决方案为ScheduleResult
     */
    private ScheduleResult convertToScheduleResult(AttendanceScheduleSolution solution) {
        log.info("[智能排班] 转换结果");

        // TODO: 完整的转换逻辑
        // 这里需要将OptaPlanner的结果转换为系统内部的ScheduleResult格式

        ScheduleResult result = new ScheduleResult();
        result.setSuccess(true);

        // 统计分配信息
        long assignedCount = solution.getShiftAssignments().stream()
            .filter(assignment -> assignment.getEmployee() != null)
            .count();

        long unassignedCount = solution.getShiftAssignments().size() - assignedCount;

        result.setMessage(String.format(
            "排班完成: 已分配=%d, 未分配=%d, 分数=[%d|%d]",
            assignedCount, unassignedCount,
            solution.getScore().hardScore(),
            solution.getScore().softScore()
        ));

        log.info("[智能排班] 结果转换完成: {}", result.getMessage());

        return result;
    }

    /**
     * 获取求解统计信息
     */
    public AttendanceSolvingListener.SolvingStatistics getStatistics() {
        return solvingListener.getStatistics();
    }

    /**
     * 验证性能目标是否达成
     *
     * @param employeeCount 员工数量
     * @param dayCount 排班天数
     * @param durationMs 实际耗时（毫秒）
     * @return 是否达成目标
     */
    public boolean validatePerformanceGoal(int employeeCount, int dayCount, long durationMs) {
        // 目标: 100 employees × 30 days < 30 seconds
        int targetEmployees = 100;
        int targetDays = 30;
        long targetDurationMs = 30000; // 30秒

        // 计算复杂度比例
        double complexityRatio = (double) (employeeCount * dayCount) / (targetEmployees * targetDays);

        // 计算允许的最大耗时
        long allowedDurationMs = (long) (targetDurationMs * complexityRatio);

        boolean goalAchieved = durationMs <= allowedDurationMs;

        log.info("[智能排班] 性能目标验证: {}×{}={} (目标={}), 实际耗时={}ms, 允许耗时={}ms, 达成={}",
            employeeCount, dayCount, employeeCount * dayCount,
            targetEmployees * targetDays,
            durationMs, allowedDurationMs, goalAchieved);

        return goalAchieved;
    }
}
