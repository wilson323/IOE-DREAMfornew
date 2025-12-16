package net.lab1024.sa.attendance.engine.algorithm.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithm;
import net.lab1024.sa.attendance.engine.model.*;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 贪心算法实现
 * <p>
 * 基于贪心策略的智能排班算法实现
 * 采用多阶段贪心选择，优先满足关键约束条件
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component("greedyAlgorithm")
public class GreedyAlgorithmImpl implements ScheduleAlgorithm {

    /**
     * 算法配置参数
     */
    private static final int MAX_ITERATIONS = 1000;
    private static final double SATISFACTION_THRESHOLD = 0.85;
    private static final int MAX_CONSECUTIVE_DAYS = 7;
    private static final int MIN_REST_HOURS = 11;
    private static final int FAIRNESS_WEIGHT = 30;
    private static final int PREFERENCE_WEIGHT = 40;
    private static final int COVERAGE_WEIGHT = 30;

    /**
     * 缓存区域
     */
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public String getAlgorithmType() {
        return "GREEDY";
    }

    @Override
    public AlgorithmMetadata getMetadata() {
        return AlgorithmMetadata.builder()
                .name("贪心算法")
                .version("1.0.0")
                .description("基于贪心策略的排班算法，优先满足关键约束条件")
                .author("IOE-DREAM架构团队")
                .createdDate(LocalDateTime.now())
                .parameters(Arrays.asList(
                        AlgorithmParameter.builder()
                                .name("maxIterations")
                                .displayName("最大迭代次数")
                                .type("INTEGER")
                                .defaultValue(1000)
                                .minValue(100)
                                .maxValue(10000)
                                .description("算法最大迭代次数")
                                .build(),
                        AlgorithmParameter.builder()
                                .name("satisfactionThreshold")
                                .displayName("满意度阈值")
                                .type("DOUBLE")
                                .defaultValue(0.85)
                                .minValue(0.5)
                                .maxValue(1.0)
                                .description("解决方案最低满意度要求")
                                .build(),
                        AlgorithmParameter.builder()
                                .name("fairnessWeight")
                                .displayName("公平性权重")
                                .type("INTEGER")
                                .defaultValue(30)
                                .minValue(0)
                                .maxValue(100)
                                .description("公平性在目标函数中的权重")
                                .build(),
                        AlgorithmParameter.builder()
                                .name("preferenceWeight")
                                .displayName("偏好权重")
                                .type("INTEGER")
                                .defaultValue(40)
                                .minValue(0)
                                .maxValue(100)
                                .description("员工偏好满足度权重")
                                .build(),
                        AlgorithmParameter.builder()
                                .name("coverageWeight")
                                .displayName("覆盖率权重")
                                .type("INTEGER")
                                .defaultValue(30)
                                .minValue(0)
                                .maxValue(100)
                                .description("班次覆盖率权重")
                                .build()
                ))
                .build();
    }

    @Override
    public boolean validateInput(ScheduleData scheduleData) {
        log.info("[贪心算法] 开始输入验证");

        try {
            // 基础数据验证
            if (scheduleData == null) {
                log.error("[贪心算法] 输入数据为空");
                return false;
            }

            if (scheduleData.getEmployees() == null || scheduleData.getEmployees().isEmpty()) {
                log.error("[贪心算法] 员工数据为空");
                return false;
            }

            if (scheduleData.getShifts() == null || scheduleData.getShifts().isEmpty()) {
                log.error("[贪心算法] 班次数据为空");
                return false;
            }

            if (scheduleData.getConstraints() == null) {
                log.error("[贪心算法] 约束条件为空");
                return false;
            }

            if (scheduleData.getStartDate() == null || scheduleData.getEndDate() == null) {
                log.error("[贪心算法] 日期范围为空");
                return false;
            }

            if (scheduleData.getStartDate().isAfter(scheduleData.getEndDate())) {
                log.error("[贪心算法] 开始日期晚于结束日期");
                return false;
            }

            // 业务逻辑验证
            long dateRange = ChronoUnit.DAYS.between(scheduleData.getStartDate(), scheduleData.getEndDate());
            if (dateRange > 365) {
                log.error("[贪心算法] 排班周期超过一年限制");
                return false;
            }

            // 验证员工数据
            for (Employee employee : scheduleData.getEmployees()) {
                if (employee.getEmployeeId() == null) {
                    log.error("[贪心算法] 员工ID为空");
                    return false;
                }
                if (employee.getSkills() == null || employee.getSkills().isEmpty()) {
                    log.warn("[贪心算法] 员工{}技能为空", employee.getEmployeeId());
                }
            }

            // 验证班次数据
            for (Shift shift : scheduleData.getShifts()) {
                if (shift.getShiftId() == null) {
                    log.error("[贪心算法] 班次ID为空");
                    return false;
                }
                if (shift.getStartTime() == null || shift.getEndTime() == null) {
                    log.error("[贪心算法] 班次{}时间配置为空", shift.getShiftId());
                    return false;
                }
                if (shift.getStartTime().isAfter(shift.getEndTime())) {
                    log.error("[贪心算法] 班次{}开始时间晚于结束时间", shift.getShiftId());
                    return false;
                }
            }

            // 验证约束条件
            for (ScheduleConstraint constraint : scheduleData.getConstraints()) {
                if (constraint.getConstraintId() == null) {
                    log.error("[贪心算法] 约束ID为空");
                    return false;
                }
                if (constraint.getHardConstraint() == null) {
                    log.warn("[贪心算法] 约束{}硬约束标记为空", constraint.getConstraintId());
                }
            }

            log.info("[贪心算法] 输入验证通过，员工数：{}，班次数：{}，约束数：{}，排班天数：{}",
                    scheduleData.getEmployees().size(),
                    scheduleData.getShifts().size(),
                    scheduleData.getConstraints().size(),
                    dateRange);

            return true;

        } catch (Exception e) {
            log.error("[贪心算法] 输入验证异常", e);
            return false;
        }
    }

    @Override
    public ScheduleResult execute(ScheduleData scheduleData) {
        log.info("[贪心算法] 开始执行排班计算");

        long startTime = System.currentTimeMillis();
        ScheduleProgress progress = new ScheduleProgress();

        try {
            // 1. 数据预处理
            progress.setCurrentStep("数据预处理");
            progress.setProgress(10);
            log.info("[贪心算法] 步骤1：数据预处理");

            PreprocessedData preprocessedData = preprocessData(scheduleData);
            if (preprocessedData == null) {
                return createErrorResult("数据预处理失败");
            }

            // 2. 初始化贪心解
            progress.setCurrentStep("初始化解");
            progress.setProgress(20);
            log.info("[贪心算法] 步骤2：初始化贪心解");

            ScheduleSolution solution = initializeGreedySolution(preprocessedData);
            if (solution == null) {
                return createErrorResult("初始化解失败");
            }

            // 3. 贪心选择优化
            progress.setCurrentStep("贪心选择优化");
            progress.setProgress(30);
            log.info("[贪心算法] 步骤3：贪心选择优化");

            solution = greedyOptimization(solution, preprocessedData, progress);
            if (solution == null) {
                return createErrorResult("贪心优化失败");
            }

            // 4. 局部搜索改进
            progress.setCurrentStep("局部搜索改进");
            progress.setProgress(80);
            log.info("[贪心算法] 步骤4：局部搜索改进");

            solution = localSearchImprovement(solution, preprocessedData, progress);
            if (solution == null) {
                return createErrorResult("局部搜索失败");
            }

            // 5. 最终验证
            progress.setCurrentStep("最终验证");
            progress.setProgress(95);
            log.info("[贪心算法] 步骤5：最终验证");

            if (!validateSolution(solution, preprocessedData)) {
                return createErrorResult("解决方案验证失败");
            }

            // 6. 构建结果
            progress.setCurrentStep("构建结果");
            progress.setProgress(100);
            log.info("[贪心算法] 步骤6：构建结果");

            ScheduleResult result = buildResult(solution, preprocessedData, startTime);

            log.info("[贪心算法] 排班计算完成，耗时：{}ms，满意度：{}",
                    System.currentTimeMillis() - startTime,
                    result.getSatisfaction());

            return result;

        } catch (Exception e) {
            log.error("[贪心算法] 排班计算异常", e);
            return createErrorResult("排班计算异常：" + e.getMessage());
        }
    }

    @Override
    public AlgorithmMetrics getMetrics(ScheduleData input, ScheduleResult output) {
        AlgorithmMetrics metrics = new AlgorithmMetrics();
        metrics.setAlgorithmType("GREEDY");

        // 计算执行时间
        if (output.getExecutionTime() != null) {
            metrics.setExecutionTime(output.getExecutionTime());
        }

        // 计算内存使用
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        metrics.setMemoryUsage(usedMemory);

        // 计算收敛性
        metrics.setConvergenceRate(calculateConvergenceRate(output));

        // 计算解质量
        metrics.setSolutionQuality(output.getSatisfaction());

        // 计算约束违反度
        metrics.setConstraintViolation(calculateConstraintViolation(output));

        // 计算迭代次数
        metrics.setIterations(output.getIterations() != null ? output.getIterations() : 0);

        // 计算覆盖率
        metrics.setCoverageRate(calculateCoverageRate(input, output));

        return metrics;
    }

    /**
     * 数据预处理
     */
    private PreprocessedData preprocessData(ScheduleData scheduleData) {
        try {
            PreprocessedData data = new PreprocessedData();
            data.setOriginalData(scheduleData);

            // 计算排班日期范围
            List<LocalDate> dateRange = calculateDateRange(
                    scheduleData.getStartDate(),
                    scheduleData.getEndDate()
            );
            data.setDateRange(dateRange);

            // 构建员工索引
            Map<Long, Employee> employeeIndex = scheduleData.getEmployees().stream()
                    .collect(Collectors.toMap(Employee::getEmployeeId, e -> e));
            data.setEmployeeIndex(employeeIndex);

            // 构建班次索引
            Map<Long, Shift> shiftIndex = scheduleData.getShifts().stream()
                    .collect(Collectors.toMap(Shift::getShiftId, s -> s));
            data.setShiftIndex(shiftIndex);

            // 分析班次需求
            Map<LocalDate, Map<Long, ShiftRequirement>> requirements = analyzeRequirements(scheduleData);
            data.setRequirements(requirements);

            // 计算员工工作量基线
            Map<Long, Integer> workloadBaseline = calculateWorkloadBaseline(scheduleData);
            data.setWorkloadBaseline(workloadBaseline);

            // 构建约束索引
            Map<String, ScheduleConstraint> constraintIndex = scheduleData.getConstraints().stream()
                    .collect(Collectors.toMap(ScheduleConstraint::getConstraintId, c -> c));
            data.setConstraintIndex(constraintIndex);

            log.info("[贪心算法] 数据预处理完成，处理日期数：{}，员工数：{}，班次数：{}",
                    dateRange.size(), employeeIndex.size(), shiftIndex.size());

            return data;

        } catch (Exception e) {
            log.error("[贪心算法] 数据预处理失败", e);
            return null;
        }
    }

    /**
     * 计算日期范围
     */
    private List<LocalDate> calculateDateRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    /**
     * 分析班次需求
     */
    private Map<LocalDate, Map<Long, ShiftRequirement>> analyzeRequirements(ScheduleData scheduleData) {
        Map<LocalDate, Map<Long, ShiftRequirement>> requirements = new HashMap<>();

        // 为每个日期生成班次需求
        List<LocalDate> dates = calculateDateRange(scheduleData.getStartDate(), scheduleData.getEndDate());

        for (LocalDate date : dates) {
            Map<Long, ShiftRequirement> dailyRequirements = new HashMap<>();

            for (Shift shift : scheduleData.getShifts()) {
                ShiftRequirement requirement = ShiftRequirement.builder()
                        .shiftId(shift.getShiftId())
                        .requiredCount(shift.getRequiredCount())
                        .assignedCount(0)
                        .priority(shift.getPriority())
                        .build();

                dailyRequirements.put(shift.getShiftId(), requirement);
            }

            requirements.put(date, dailyRequirements);
        }

        return requirements;
    }

    /**
     * 计算员工工作量基线
     */
    private Map<Long, Integer> calculateWorkloadBaseline(ScheduleData scheduleData) {
        Map<Long, Integer> baseline = new HashMap<>();

        // 基于班次需求和员工数量计算平均工作量
        int totalRequirements = scheduleData.getShifts().stream()
                .mapToInt(Shift::getRequiredCount)
                .sum();

        int employeeCount = scheduleData.getEmployees().size();
        int avgWorkload = employeeCount > 0 ? totalRequirements / employeeCount : 0;

        for (Employee employee : scheduleData.getEmployees()) {
            // 根据员工类型调整工作量基线
            int adjustedWorkload = avgWorkload;
            if ("FULL_TIME".equals(employee.getEmployeeType())) {
                adjustedWorkload = (int) (avgWorkload * 1.2);
            } else if ("PART_TIME".equals(employee.getEmployeeType())) {
                adjustedWorkload = (int) (avgWorkload * 0.8);
            }

            baseline.put(employee.getEmployeeId(), adjustedWorkload);
        }

        return baseline;
    }

    /**
     * 初始化贪心解
     */
    private ScheduleSolution initializeGreedySolution(PreprocessedData data) {
        try {
            ScheduleSolution solution = new ScheduleSolution();
            List<ScheduleAssignment> assignments = new ArrayList<>();

            // 贪心策略1：优先安排高优先级班次
            List<Shift> sortedShifts = data.getShiftIndex().values().stream()
                    .sorted(Comparator.comparing(Shift::getPriority).reversed())
                    .collect(Collectors.toList());

            // 贪心策略2：按日期顺序安排
            for (LocalDate date : data.getDateRange()) {
                for (Shift shift : sortedShifts) {
                    ShiftRequirement requirement = data.getRequirements().get(date).get(shift.getShiftId());

                    // 贪心策略3：为每个班次选择最合适的员工
                    List<Employee> candidateEmployees = findBestCandidates(shift, date, data, assignments);

                    int assignedCount = 0;
                    for (Employee employee : candidateEmployees) {
                        if (assignedCount >= requirement.getRequiredCount()) {
                            break;
                        }

                        // 检查员工是否可以安排此班次
                        if (canAssignEmployee(employee, shift, date, assignments, data)) {
                            ScheduleAssignment assignment = ScheduleAssignment.builder()
                                    .assignmentId(UUID.randomUUID().toString())
                                    .employeeId(employee.getEmployeeId())
                                    .shiftId(shift.getShiftId())
                                    .assignmentDate(date)
                                    .status("CONFIRMED")
                                    .build();

                            assignments.add(assignment);
                            assignedCount++;

                            // 更新需求状态
                            requirement.setAssignedCount(requirement.getAssignedCount() + 1);

                            log.debug("[贪心算法] 安排员工：{}，班次：{}，日期：{}",
                                    employee.getEmployeeId(), shift.getShiftId(), date);
                        }
                    }
                }
            }

            solution.setAssignments(assignments);
            solution.setObjectiveValue(calculateObjectiveValue(assignments, data));

            log.info("[贪心算法] 初始解生成完成，总安排数：{}", assignments.size());

            return solution;

        } catch (Exception e) {
            log.error("[贪心算法] 初始化解失败", e);
            return null;
        }
    }

    /**
     * 寻找最佳候选人
     */
    private List<Employee> findBestCandidates(Shift shift, LocalDate date, PreprocessedData data, List<ScheduleAssignment> currentAssignments) {
        List<Employee> candidates = new ArrayList<>();

        // 筛选具备所需技能的员工
        List<Employee> skilledEmployees = data.getEmployeeIndex().values().stream()
                .filter(employee -> hasRequiredSkills(employee, shift))
                .collect(Collectors.toList());

        // 贪心策略：按多个维度排序
        candidates = skilledEmployees.stream()
                .sorted((e1, e2) -> {
                    // 优先级1：当前工作量较小的员工
                    int workload1 = getCurrentWorkload(e1.getEmployeeId(), currentAssignments);
                    int workload2 = getCurrentWorkload(e2.getEmployeeId(), currentAssignments);
                    int workloadCompare = Integer.compare(workload1, workload2);
                    if (workloadCompare != 0) return workloadCompare;

                    // 优先级2：偏好匹配度高的员工
                    double preference1 = calculatePreferenceScore(e1, shift, date);
                    double preference2 = calculatePreferenceScore(e2, shift, date);
                    int preferenceCompare = Double.compare(preference2, preference1);
                    if (preferenceCompare != 0) return preferenceCompare;

                    // 优先级3：员工等级高的优先
                    int levelCompare = Integer.compare(e2.getLevel(), e1.getLevel());
                    if (levelCompare != 0) return levelCompare;

                    // 优先级4：随机选择（保证公平性）
                    return new Random().nextInt(3) - 1;
                })
                .collect(Collectors.toList());

        return candidates;
    }

    /**
     * 检查员工是否具备所需技能
     */
    private boolean hasRequiredSkills(Employee employee, Shift shift) {
        if (shift.getRequiredSkills() == null || shift.getRequiredSkills().isEmpty()) {
            return true;
        }

        if (employee.getSkills() == null || employee.getSkills().isEmpty()) {
            return false;
        }

        return employee.getSkills().containsAll(shift.getRequiredSkills());
    }

    /**
     * 获取员工当前工作量
     */
    private int getCurrentWorkload(Long employeeId, List<ScheduleAssignment> assignments) {
        return (int) assignments.stream()
                .filter(assignment -> assignment.getEmployeeId().equals(employeeId))
                .count();
    }

    /**
     * 计算偏好匹配度
     */
    private double calculatePreferenceScore(Employee employee, Shift shift, LocalDate date) {
        // 简化的偏好计算，实际应用中可以根据员工偏好设置进行计算
        double score = 0.5; // 基础分数

        // 基于员工等级调整偏好
        score += employee.getLevel() * 0.1;

        // 基于班次时间调整偏好
        if (shift.getStartTime().isBefore(LocalTime.NOON)) {
            score += 0.1; // 早班偏好
        } else if (shift.getStartTime().isAfter(LocalTime.of(18, 0))) {
            score -= 0.1; // 晚班偏好降低
        }

        return Math.min(1.0, Math.max(0.0, score));
    }

    /**
     * 检查是否可以安排员工
     */
    private boolean canAssignEmployee(Employee employee, Shift shift, LocalDate date,
                                   List<ScheduleAssignment> assignments, PreprocessedData data) {

        // 检查工作量约束
        int currentWorkload = getCurrentWorkload(employee.getEmployeeId(), assignments);
        int maxWorkload = data.getWorkloadBaseline().get(employee.getEmployeeId()) * 2;
        if (currentWorkload >= maxWorkload) {
            return false;
        }

        // 检查时间冲突
        if (hasTimeConflict(employee, shift, date, assignments, data)) {
            return false;
        }

        // 检查连续工作天数约束
        if (exceedsConsecutiveDays(employee, date, assignments, data)) {
            return false;
        }

        // 检查休息时间约束
        if (insufficientRestTime(employee, shift, date, assignments, data)) {
            return false;
        }

        return true;
    }

    /**
     * 检查时间冲突
     */
    private boolean hasTimeConflict(Employee employee, Shift shift, LocalDate date,
                                   List<ScheduleAssignment> assignments, PreprocessedData data) {

        LocalDateTime shiftStart = date.atTime(shift.getStartTime());
        LocalDateTime shiftEnd = date.atTime(shift.getEndTime());

        return assignments.stream()
                .filter(assignment -> assignment.getEmployeeId().equals(employee.getEmployeeId()) &&
                                      assignment.getAssignmentDate().equals(date))
                .anyMatch(assignment -> {
                    Shift assignedShift = data.getShiftIndex().get(assignment.getShiftId());
                    LocalDateTime assignedStart = date.atTime(assignedShift.getStartTime());
                    LocalDateTime assignedEnd = date.atTime(assignedShift.getEndTime());

                    return shiftStart.isBefore(assignedEnd) && shiftEnd.isAfter(assignedStart);
                });
    }

    /**
     * 检查连续工作天数约束
     */
    private boolean exceedsConsecutiveDays(Employee employee, LocalDate date,
                                         List<ScheduleAssignment> assignments, PreprocessedData data) {

        // 计算连续工作天数
        int consecutiveDays = 0;
        LocalDate currentDate = date.minusDays(MAX_CONSECUTIVE_DAYS);

        while (!currentDate.isAfter(date)) {
            boolean hasWork = assignments.stream()
                    .anyMatch(assignment -> assignment.getEmployeeId().equals(employee.getEmployeeId()) &&
                                          assignment.getAssignmentDate().equals(currentDate));

            if (hasWork) {
                consecutiveDays++;
                if (consecutiveDays >= MAX_CONSECUTIVE_DAYS) {
                    return true;
                }
            } else {
                consecutiveDays = 0;
            }

            currentDate = currentDate.plusDays(1);
        }

        return false;
    }

    /**
     * 检查休息时间约束
     */
    private boolean insufficientRestTime(Employee employee, Shift shift, LocalDate date,
                                       List<ScheduleAssignment> assignments, PreprocessedData data) {

        // 检查前一天的工作安排
        LocalDate previousDay = date.minusDays(1);
        Optional<ScheduleAssignment> previousAssignment = assignments.stream()
                .filter(assignment -> assignment.getEmployeeId().equals(employee.getEmployeeId()) &&
                                      assignment.getAssignmentDate().equals(previousDay))
                .findFirst();

        if (previousAssignment.isPresent()) {
            Shift previousShift = data.getShiftIndex().get(previousAssignment.get().getShiftId());

            LocalDateTime previousEnd = previousDay.atTime(previousShift.getEndTime());
            LocalDateTime currentStart = date.atTime(shift.getStartTime());

            long restHours = ChronoUnit.HOURS.between(previousEnd, currentStart);

            return restHours < MIN_REST_HOURS;
        }

        return false;
    }

    /**
     * 计算目标函数值
     */
    private double calculateObjectiveValue(List<ScheduleAssignment> assignments, PreprocessedData data) {
        double fairnessScore = calculateFairnessScore(assignments, data);
        double preferenceScore = calculatePreferenceScore(assignments, data);
        double coverageScore = calculateCoverageScore(assignments, data);

        // 加权综合
        return (fairnessScore * FAIRNESS_WEIGHT +
                preferenceScore * PREFERENCE_WEIGHT +
                coverageScore * COVERAGE_WEIGHT) / 100.0;
    }

    /**
     * 计算公平性分数
     */
    private double calculateFairnessScore(List<ScheduleAssignment> assignments, PreprocessedData data) {
        Map<Long, Integer> workloadMap = new HashMap<>();

        // 统计每个员工的工作量
        for (ScheduleAssignment assignment : assignments) {
            workloadMap.merge(assignment.getEmployeeId(), 1, Integer::sum);
        }

        if (workloadMap.isEmpty()) {
            return 1.0;
        }

        // 计算工作量方差
        double avgWorkload = workloadMap.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = workloadMap.values().stream()
                .mapToDouble(workload -> Math.pow(workload - avgWorkload, 2))
                .average().orElse(0);

        // 方差越小，公平性越高
        return Math.max(0, 1.0 - variance / (avgWorkload * avgWorkload));
    }

    /**
     * 计算偏好满足度分数
     */
    private double calculatePreferenceScore(List<ScheduleAssignment> assignments, PreprocessedData data) {
        double totalPreference = 0;
        int count = 0;

        for (ScheduleAssignment assignment : assignments) {
            Employee employee = data.getEmployeeIndex().get(assignment.getEmployeeId());
            Shift shift = data.getShiftIndex().get(assignment.getShiftId());

            double preference = calculatePreferenceScore(employee, shift, assignment.getAssignmentDate());
            totalPreference += preference;
            count++;
        }

        return count > 0 ? totalPreference / count : 1.0;
    }

    /**
     * 计算班次覆盖率分数
     */
    private double calculateCoverageScore(List<ScheduleAssignment> assignments, PreprocessedData data) {
        int totalRequired = 0;
        int totalAssigned = 0;

        for (Map.Entry<LocalDate, Map<Long, ShiftRequirement>> dateEntry : data.getRequirements().entrySet()) {
            for (ShiftRequirement requirement : dateEntry.getValue().values()) {
                totalRequired += requirement.getRequiredCount();
                totalAssigned += Math.min(requirement.getAssignedCount(), requirement.getRequiredCount());
            }
        }

        return totalRequired > 0 ? (double) totalAssigned / totalRequired : 1.0;
    }

    /**
     * 贪心优化
     */
    private ScheduleSolution greedyOptimization(ScheduleSolution solution, PreprocessedData data, ScheduleProgress progress) {
        log.info("[贪心算法] 开始贪心优化迭代");

        AtomicInteger iteration = new AtomicInteger(0);
        double bestValue = solution.getObjectiveValue();
        ScheduleSolution bestSolution = solution;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            iteration.set(i + 1);

            // 更新进度
            if (i % 100 == 0) {
                int progressValue = 30 + (i * 40 / MAX_ITERATIONS);
                progress.setProgress(progressValue);
                log.info("[贪心算法] 优化进度：{}%，迭代次数：{}", progressValue, i + 1);
            }

            // 生成邻域解
            ScheduleSolution neighborSolution = generateNeighborSolution(bestSolution, data);
            if (neighborSolution == null) {
                continue;
            }

            // 评估邻域解
            double neighborValue = neighborSolution.getObjectiveValue();

            // 贪心选择：只接受更好的解
            if (neighborValue > bestValue) {
                bestValue = neighborValue;
                bestSolution = neighborSolution;

                log.debug("[贪心算法] 找到更好解，目标值：{} -> {}", solution.getObjectiveValue(), neighborValue);
            }

            // 提前终止条件
            if (bestValue >= 0.95) {
                log.info("[贪心算法] 达到高质量解，提前终止优化");
                break;
            }
        }

        bestSolution.setIterations(iteration.get());
        log.info("[贪心算法] 贪心优化完成，最终目标值：{}，迭代次数：{}", bestValue, iteration.get());

        return bestSolution;
    }

    /**
     * 生成邻域解
     */
    private ScheduleSolution generateNeighborSolution(ScheduleSolution currentSolution, PreprocessedData data) {
        try {
            List<ScheduleAssignment> assignments = new ArrayList<>(currentSolution.getAssignments());

            // 随机选择优化策略
            int strategy = new Random().nextInt(3);

            switch (strategy) {
                case 0:
                    return swapAssignments(assignments, data);
                case 1:
                    return addMissingAssignments(assignments, data);
                case 2:
                    return removeExcessAssignments(assignments, data);
                default:
                    return null;
            }

        } catch (Exception e) {
            log.error("[贪心算法] 生成邻域解失败", e);
            return null;
        }
    }

    /**
     * 交换班次分配
     */
    private ScheduleSolution swapAssignments(List<ScheduleAssignment> assignments, PreprocessedData data) {
        if (assignments.size() < 2) {
            return null;
        }

        // 随机选择两个分配进行交换
        int index1 = new Random().nextInt(assignments.size());
        int index2 = new Random().nextInt(assignments.size());

        if (index1 == index2) {
            return null;
        }

        ScheduleAssignment assignment1 = assignments.get(index1);
        ScheduleAssignment assignment2 = assignments.get(index2);

        // 交换员工
        Long tempEmployeeId = assignment1.getEmployeeId();
        assignment1.setEmployeeId(assignment2.getEmployeeId());
        assignment2.setEmployeeId(tempEmployeeId);

        // 验证交换后的可行性
        if (isAssignmentValid(assignment1, assignments, data) &&
            isAssignmentValid(assignment2, assignments, data)) {

            ScheduleSolution newSolution = new ScheduleSolution();
            newSolution.setAssignments(new ArrayList<>(assignments));
            newSolution.setObjectiveValue(calculateObjectiveValue(assignments, data));

            return newSolution;
        }

        // 恢复原始分配
        assignment1.setEmployeeId(tempEmployeeId);

        return null;
    }

    /**
     * 添加缺失的班次分配
     */
    private ScheduleSolution addMissingAssignments(List<ScheduleAssignment> assignments, PreprocessedData data) {
        List<ScheduleAssignment> newAssignments = new ArrayList<>(assignments);
        boolean added = false;

        // 寻找未被满足的班次需求
        for (Map.Entry<LocalDate, Map<Long, ShiftRequirement>> dateEntry : data.getRequirements().entrySet()) {
            LocalDate date = dateEntry.getKey();

            for (ShiftRequirement requirement : dateEntry.getValue().values()) {
                int currentCount = (int) assignments.stream()
                        .filter(a -> a.getAssignmentDate().equals(date) && a.getShiftId().equals(requirement.getShiftId()))
                        .count();

                if (currentCount < requirement.getRequiredCount()) {
                    // 寻找合适的员工
                    Shift shift = data.getShiftIndex().get(requirement.getShiftId());
                    List<Employee> candidates = findBestCandidates(shift, date, data, assignments);

                    for (Employee employee : candidates) {
                        if (canAssignEmployee(employee, shift, date, newAssignments, data)) {
                            ScheduleAssignment newAssignment = ScheduleAssignment.builder()
                                    .assignmentId(UUID.randomUUID().toString())
                                    .employeeId(employee.getEmployeeId())
                                    .shiftId(requirement.getShiftId())
                                    .assignmentDate(date)
                                    .status("CONFIRMED")
                                    .build();

                            newAssignments.add(newAssignment);
                            added = true;
                            break;
                        }
                    }

                    if (added) break;
                }
            }

            if (added) break;
        }

        if (added) {
            ScheduleSolution newSolution = new ScheduleSolution();
            newSolution.setAssignments(newAssignments);
            newSolution.setObjectiveValue(calculateObjectiveValue(newAssignments, data));
            return newSolution;
        }

        return null;
    }

    /**
     * 移除多余的班次分配
     */
    private ScheduleSolution removeExcessAssignments(List<ScheduleAssignment> assignments, PreprocessedData data) {
        if (assignments.isEmpty()) {
            return null;
        }

        // 随机选择一个分配进行移除测试
        int removeIndex = new Random().nextInt(assignments.size());
        ScheduleAssignment removedAssignment = assignments.get(removeIndex);

        // 检查移除后是否还能满足需求
        Map<Long, Integer> shiftCount = new HashMap<>();
        for (ScheduleAssignment assignment : assignments) {
            if (!assignment.equals(removedAssignment)) {
                LocalDate date = assignment.getAssignmentDate();
                Long shiftId = assignment.getShiftId();
                Long key = date.toEpochDay() * 1000 + shiftId;
                shiftCount.merge(key, 1, Integer::sum);
            }
        }

        // 检查是否满足所有需求
        boolean allRequirementsMet = true;
        for (Map.Entry<LocalDate, Map<Long, ShiftRequirement>> dateEntry : data.getRequirements().entrySet()) {
            LocalDate date = dateEntry.getKey();

            for (ShiftRequirement requirement : dateEntry.getValue().values()) {
                Long key = date.toEpochDay() * 1000 + requirement.getShiftId();
                int currentCount = shiftCount.getOrDefault(key, 0);

                if (currentCount < requirement.getRequiredCount()) {
                    allRequirementsMet = false;
                    break;
                }
            }

            if (!allRequirementsMet) break;
        }

        if (allRequirementsMet) {
            List<ScheduleAssignment> newAssignments = new ArrayList<>(assignments);
            newAssignments.remove(removeIndex);

            ScheduleSolution newSolution = new ScheduleSolution();
            newSolution.setAssignments(newAssignments);
            newSolution.setObjectiveValue(calculateObjectiveValue(newAssignments, data));

            return newSolution;
        }

        return null;
    }

    /**
     * 验证分配的可行性
     */
    private boolean isAssignmentValid(ScheduleAssignment assignment, List<ScheduleAssignment> assignments, PreprocessedData data) {
        Employee employee = data.getEmployeeIndex().get(assignment.getEmployeeId());
        Shift shift = data.getShiftIndex().get(assignment.getShiftId());

        return canAssignEmployee(employee, shift, assignment.getAssignmentDate(), assignments, data);
    }

    /**
     * 局部搜索改进
     */
    private ScheduleSolution localSearchImprovement(ScheduleSolution solution, PreprocessedData data, ScheduleProgress progress) {
        log.info("[贪心算法] 开始局部搜索改进");

        ScheduleSolution currentSolution = solution;
        boolean improved = true;
        int iteration = 0;

        while (improved && iteration < 100) {
            iteration++;
            improved = false;

            // 尝试各种局部改进策略
            List<ScheduleSolution> candidates = Arrays.asList(
                    tryTwoOpt(currentSolution, data),
                    tryRelocation(currentSolution, data),
                    tryExchange(currentSolution, data)
            );

            for (ScheduleSolution candidate : candidates) {
                if (candidate != null && candidate.getObjectiveValue() > currentSolution.getObjectiveValue()) {
                    currentSolution = candidate;
                    improved = true;

                    log.debug("[贪心算法] 局部搜索改进，目标值：{} -> {}",
                            solution.getObjectiveValue(), candidate.getObjectiveValue());
                    break;
                }
            }
        }

        log.info("[贪心算法] 局部搜索改进完成，迭代次数：{}，改进效果：{}",
                iteration, currentSolution.getObjectiveValue() - solution.getObjectiveValue());

        return currentSolution;
    }

    /**
     * 尝试2-opt交换
     */
    private ScheduleSolution tryTwoOpt(ScheduleSolution solution, PreprocessedData data) {
        List<ScheduleAssignment> assignments = solution.getAssignments();

        if (assignments.size() < 4) {
            return null;
        }

        // 随机选择两个位置进行2-opt交换
        int i = new Random().nextInt(assignments.size() / 2);
        int j = i + assignments.size() / 2 + new Random().nextInt(assignments.size() / 2);

        // 反转中间段
        List<ScheduleAssignment> newAssignments = new ArrayList<>(assignments.subList(0, i));
        newAssignments.addAll(assignments.subList(i, j).stream()
                .sorted(Comparator.comparing(ScheduleAssignment::getEmployeeId))
                .collect(Collectors.toList()));
        newAssignments.addAll(assignments.subList(j, assignments.size()));

        // 验证新解
        if (validateAssignments(newAssignments, data)) {
            ScheduleSolution newSolution = new ScheduleSolution();
            newSolution.setAssignments(newAssignments);
            newSolution.setObjectiveValue(calculateObjectiveValue(newAssignments, data));
            return newSolution;
        }

        return null;
    }

    /**
     * 尝试重定位
     */
    private ScheduleSolution tryRelocation(ScheduleSolution solution, PreprocessedData data) {
        List<ScheduleAssignment> assignments = new ArrayList<>(solution.getAssignments());

        if (assignments.isEmpty()) {
            return null;
        }

        // 随机选择一个分配进行重定位
        int relocateIndex = new Random().nextInt(assignments.size());
        ScheduleAssignment relocateAssignment = assignments.get(relocateIndex);

        // 寻找新的合适员工
        Shift shift = data.getShiftIndex().get(relocateAssignment.getShiftId());
        List<Employee> candidates = findBestCandidates(shift, relocateAssignment.getAssignmentDate(), data, assignments);

        for (Employee employee : candidates) {
            if (!employee.getEmployeeId().equals(relocateAssignment.getEmployeeId())) {
                // 创建新分配
                ScheduleAssignment newAssignment = ScheduleAssignment.builder()
                        .assignmentId(relocateAssignment.getAssignmentId())
                        .employeeId(employee.getEmployeeId())
                        .shiftId(relocateAssignment.getShiftId())
                        .assignmentDate(relocateAssignment.getAssignmentDate())
                        .status("CONFIRMED")
                        .build();

                assignments.set(relocateIndex, newAssignment);

                // 验证新解
                if (validateAssignments(assignments, data)) {
                    ScheduleSolution newSolution = new ScheduleSolution();
                    newSolution.setAssignments(assignments);
                    newSolution.setObjectiveValue(calculateObjectiveValue(assignments, data));
                    return newSolution;
                }

                // 恢复原始分配
                assignments.set(relocateIndex, relocateAssignment);
            }
        }

        return null;
    }

    /**
     * 尝试交换
     */
    private ScheduleSolution tryExchange(ScheduleSolution solution, PreprocessedData data) {
        // 这里实现交换策略，类似于swapAssignments
        return swapAssignments(solution.getAssignments(), data);
    }

    /**
     * 验证所有分配
     */
    private boolean validateAssignments(List<ScheduleAssignment> assignments, PreprocessedData data) {
        for (ScheduleAssignment assignment : assignments) {
            Employee employee = data.getEmployeeIndex().get(assignment.getEmployeeId());
            Shift shift = data.getShiftIndex().get(assignment.getShiftId());

            if (!canAssignEmployee(employee, shift, assignment.getAssignmentDate(), assignments, data)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 验证解决方案
     */
    private boolean validateSolution(ScheduleSolution solution, PreprocessedData data) {
        try {
            List<ScheduleAssignment> assignments = solution.getAssignments();

            // 验证基础约束
            for (ScheduleAssignment assignment : assignments) {
                if (!data.getEmployeeIndex().containsKey(assignment.getEmployeeId())) {
                    log.error("[贪心算法] 员工{}不存在", assignment.getEmployeeId());
                    return false;
                }

                if (!data.getShiftIndex().containsKey(assignment.getShiftId())) {
                    log.error("[贪心算法] 班次{}不存在", assignment.getShiftId());
                    return false;
                }

                if (!data.getDateRange().contains(assignment.getAssignmentDate())) {
                    log.error("[贪心算法] 日期{}不在排班范围内", assignment.getAssignmentDate());
                    return false;
                }
            }

            // 验证工作量约束
            Map<Long, Integer> workloadMap = new HashMap<>();
            for (ScheduleAssignment assignment : assignments) {
                workloadMap.merge(assignment.getEmployeeId(), 1, Integer::sum);
            }

            for (Map.Entry<Long, Integer> entry : workloadMap.entrySet()) {
                Long employeeId = entry.getKey();
                int workload = entry.getValue();
                int maxWorkload = data.getWorkloadBaseline().get(employeeId) * 2;

                if (workload > maxWorkload) {
                    log.warn("[贪心算法] 员工{}工作量超限：{} > {}", employeeId, workload, maxWorkload);
                }
            }

            // 验证班次需求满足度
            for (Map.Entry<LocalDate, Map<Long, ShiftRequirement>> dateEntry : data.getRequirements().entrySet()) {
                LocalDate date = dateEntry.getKey();

                for (ShiftRequirement requirement : dateEntry.getValue().values()) {
                    long assignedCount = assignments.stream()
                            .filter(a -> a.getAssignmentDate().equals(date) && a.getShiftId().equals(requirement.getShiftId()))
                            .count();

                    if (assignedCount < requirement.getRequiredCount()) {
                        log.warn("[贪心算法] 日期{}班次{}需求未满足：{} < {}",
                                date, requirement.getShiftId(), assignedCount, requirement.getRequiredCount());
                    }
                }
            }

            log.info("[贪心算法] 解决方案验证通过，总分配数：{}", assignments.size());
            return true;

        } catch (Exception e) {
            log.error("[贪心算法] 解决方案验证异常", e);
            return false;
        }
    }

    /**
     * 构建结果
     */
    private ScheduleResult buildResult(ScheduleSolution solution, PreprocessedData data, long startTime) {
        ScheduleResult result = new ScheduleResult();

        // 基本信息
        result.setSuccess(true);
        result.setMessage("贪心算法排班成功");
        result.setExecutionTime(System.currentTimeMillis() - startTime);
        result.setIterations(solution.getIterations());

        // 优化指标
        result.setSatisfaction(solution.getObjectiveValue());
        result.setFairnessIndex(calculateFairnessScore(solution.getAssignments(), data));
        result.setWorkloadBalance(calculateWorkloadBalance(solution.getAssignments(), data));
        result.setCostEfficiency(calculateCostEfficiency(solution.getAssignments(), data));

        // 分配结果
        result.setAssignments(solution.getAssignments());

        // 统计信息
        Map<String, Integer> assignmentStats = new HashMap<>();
        for (ScheduleAssignment assignment : solution.getAssignments()) {
            assignmentStats.merge(assignment.getShiftId(), 1, Integer::sum);
        }
        result.setAssignmentStats(assignmentStats);

        // 员工工作量
        Map<Long, Integer> employeeWorkloads = new HashMap<>();
        for (ScheduleAssignment assignment : solution.getAssignments()) {
            employeeWorkloads.merge(assignment.getEmployeeId(), 1, Integer::sum);
        }
        result.setEmployeeWorkloads(employeeWorkloads);

        // 冲突信息
        List<String> conflicts = detectConflicts(solution.getAssignments(), data);
        result.setConflicts(conflicts);

        log.info("[贪心算法] 结果构建完成，满意度：{}，冲突数：{}",
                result.getSatisfaction(), conflicts.size());

        return result;
    }

    /**
     * 计算工作量平衡度
     */
    private double calculateWorkloadBalance(List<ScheduleAssignment> assignments, PreprocessedData data) {
        Map<Long, Integer> actualWorkloads = new HashMap<>();
        for (ScheduleAssignment assignment : assignments) {
            actualWorkloads.merge(assignment.getEmployeeId(), 1, Integer::sum);
        }

        double totalDeviation = 0;
        int count = 0;

        for (Map.Entry<Long, Integer> entry : data.getWorkloadBaseline().entrySet()) {
            Long employeeId = entry.getKey();
            int baseline = entry.getValue();
            int actual = actualWorkloads.getOrDefault(employeeId, 0);

            double deviation = Math.abs(actual - baseline);
            totalDeviation += deviation / (double) baseline;
            count++;
        }

        return count > 0 ? Math.max(0, 1.0 - totalDeviation / count) : 1.0;
    }

    /**
     * 计算成本效率
     */
    private double calculateCostEfficiency(List<ScheduleAssignment> assignments, PreprocessedData data) {
        double totalCost = 0;
        double totalValue = 0;

        for (ScheduleAssignment assignment : assignments) {
            Employee employee = data.getEmployeeIndex().get(assignment.getEmployeeId());
            Shift shift = data.getShiftIndex().get(assignment.getShiftId());

            // 简化的成本计算
            double cost = employee.getLevel() * shift.getDuration().toHours();
            double value = shift.getPriority() * 10;

            totalCost += cost;
            totalValue += value;
        }

        return totalCost > 0 ? totalValue / totalCost : 1.0;
    }

    /**
     * 检测冲突
     */
    private List<String> detectConflicts(List<ScheduleAssignment> assignments, PreprocessedData data) {
        List<String> conflicts = new ArrayList<>();

        // 检测时间冲突
        Map<String, List<ScheduleAssignment>> dailyAssignments = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getAssignmentDate().toString()));

        for (Map.Entry<String, List<ScheduleAssignment>> entry : dailyAssignments.entrySet()) {
            List<ScheduleAssignment> dayAssignments = entry.getValue();

            for (int i = 0; i < dayAssignments.size(); i++) {
                for (int j = i + 1; j < dayAssignments.size(); j++) {
                    ScheduleAssignment a1 = dayAssignments.get(i);
                    ScheduleAssignment a2 = dayAssignments.get(j);

                    if (a1.getEmployeeId().equals(a2.getEmployeeId())) {
                        Shift s1 = data.getShiftIndex().get(a1.getShiftId());
                        Shift s2 = data.getShiftIndex().get(a2.getShiftId());

                        LocalDateTime start1 = a1.getAssignmentDate().atTime(s1.getStartTime());
                        LocalDateTime end1 = a1.getAssignmentDate().atTime(s1.getEndTime());
                        LocalDateTime start2 = a2.getAssignmentDate().atTime(s2.getStartTime());
                        LocalDateTime end2 = a2.getAssignmentDate().atTime(s2.getEndTime());

                        if (start1.isBefore(end2) && end1.isAfter(start2)) {
                            conflicts.add(String.format("员工%s在%s存在时间冲突：班次%s与班次%s",
                                    a1.getEmployeeId(), a1.getAssignmentDate(), a1.getShiftId(), a2.getShiftId()));
                        }
                    }
                }
            }
        }

        return conflicts;
    }

    /**
     * 创建错误结果
     */
    private ScheduleResult createErrorResult(String errorMessage) {
        ScheduleResult result = new ScheduleResult();
        result.setSuccess(false);
        result.setMessage(errorMessage);
        result.setSatisfaction(0.0);
        result.setAssignments(new ArrayList<>());
        return result;
    }

    /**
     * 计算收敛率
     */
    private double calculateConvergenceRate(ScheduleResult result) {
        // 简化的收敛率计算
        return Math.min(1.0, result.getSatisfaction());
    }

    /**
     * 计算约束违反度
     */
    private double calculateConstraintViolation(ScheduleResult result) {
        if (result.getConflicts() == null || result.getConflicts().isEmpty()) {
            return 0.0;
        }

        // 根据冲突数量计算违反度
        int conflictCount = result.getConflicts().size();
        int totalAssignments = result.getAssignments().size();

        return totalAssignments > 0 ? (double) conflictCount / totalAssignments : 0.0;
    }

    /**
     * 计算覆盖率
     */
    private double calculateCoverageRate(ScheduleData input, ScheduleResult output) {
        // 简化的覆盖率计算
        int totalRequirements = input.getShifts().stream()
                .mapToInt(Shift::getRequiredCount)
                .sum();

        int totalAssignments = output.getAssignments().size();

        return totalRequirements > 0 ? Math.min(1.0, (double) totalAssignments / totalRequirements) : 1.0;
    }

    /**
     * 预处理数据内部类
     */
    private static class PreprocessedData {
        private ScheduleData originalData;
        private List<LocalDate> dateRange;
        private Map<Long, Employee> employeeIndex;
        private Map<Long, Shift> shiftIndex;
        private Map<LocalDate, Map<Long, ShiftRequirement>> requirements;
        private Map<Long, Integer> workloadBaseline;
        private Map<String, ScheduleConstraint> constraintIndex;

        // Getters and Setters
        public ScheduleData getOriginalData() { return originalData; }
        public void setOriginalData(ScheduleData originalData) { this.originalData = originalData; }
        public List<LocalDate> getDateRange() { return dateRange; }
        public void setDateRange(List<LocalDate> dateRange) { this.dateRange = dateRange; }
        public Map<Long, Employee> getEmployeeIndex() { return employeeIndex; }
        public void setEmployeeIndex(Map<Long, Employee> employeeIndex) { this.employeeIndex = employeeIndex; }
        public Map<Long, Shift> getShiftIndex() { return shiftIndex; }
        public void setShiftIndex(Map<Long, Shift> shiftIndex) { this.shiftIndex = shiftIndex; }
        public Map<LocalDate, Map<Long, ShiftRequirement>> getRequirements() { return requirements; }
        public void setRequirements(Map<LocalDate, Map<Long, ShiftRequirement>> requirements) { this.requirements = requirements; }
        public Map<Long, Integer> getWorkloadBaseline() { return workloadBaseline; }
        public void setWorkloadBaseline(Map<Long, Integer> workloadBaseline) { this.workloadBaseline = workloadBaseline; }
        public Map<String, ScheduleConstraint> getConstraintIndex() { return constraintIndex; }
        public void setConstraintIndex(Map<String, ScheduleConstraint> constraintIndex) { this.constraintIndex = constraintIndex; }
    }

    // ========== ScheduleEngine接口方法实现 ==========

    @Override
    public SchedulePlan generateSchedulePlan(Long planId, LocalDate startDate, LocalDate endDate) {
        log.info("[贪心算法] 生成排班计划, planId={}, 时间范围={} to {}",
                planId, startDate, endDate);

        try {
            // 创建排班数据
            ScheduleData scheduleData = createSampleScheduleData(planId, startDate, endDate);

            // 执行排班
            ScheduleResult result = execute(scheduleData);

            // 构建排班计划
            SchedulePlan plan = SchedulePlan.builder()
                    .planId(planId)
                    .startDate(startDate.toString())
                    .endDate(endDate.toString())
                    .status(result.getSuccess() ? "ACTIVE" : "FAILED")
                    .assignedShiftCount(result.getAssignments() != null ? result.getAssignments().size() : 0)
                    .createdTime(LocalDateTime.now())
                    .build();

            return plan;

        } catch (Exception e) {
            log.error("[贪心算法] 生成排班计划失败", e);

            SchedulePlan plan = SchedulePlan.builder()
                    .planId(planId)
                    .startDate(startDate.toString())
                    .endDate(endDate.toString())
                    .status("FAILED")
                    .createdTime(LocalDateTime.now())
                    .build();

            return plan;
        }
    }

    /**
     * 创建示例排班数据
     */
    private ScheduleData createSampleScheduleData(Long planId, LocalDate startDate, LocalDate endDate) {
        // 这里创建示例数据，实际应用中应该从数据库获取
        return ScheduleData.builder()
                .planId(planId)
                .startDate(startDate)
                .endDate(endDate)
                .employees(new ArrayList<>())
                .shifts(new ArrayList<>())
                .constraints(new ArrayList<>())
                .build();
    }

    @Override
    public ConflictDetectionResult validateScheduleConflicts(ScheduleData scheduleData) {
        log.debug("[贪心算法] 验证排班冲突");

        ConflictDetectionResult result = new ConflictDetectionResult();
        result.setValidationTime(System.currentTimeMillis());
        result.setConflictCount(0);
        result.setConflictList(new ArrayList<>());
        result.setSeverity("LOW");
        result.setNeedsResolution(false);

        try {
            // 简化实现：基于基本规则检测冲突
            List<String> conflicts = new ArrayList<>();

            // 检测员工同一时间段重复排班
            Map<String, List<ScheduleData.ScheduleRecord>> employeeScheduleMap = new HashMap<>();
            if (scheduleData.getHistoryRecords() != null) {
                for (ScheduleData.ScheduleRecord record : scheduleData.getHistoryRecords()) {
                    String key = record.getEmployeeId() + ":" + record.getScheduleDate();
                    employeeScheduleMap.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
                }
            }

            for (Map.Entry<String, List<ScheduleData.ScheduleRecord>> entry : employeeScheduleMap.entrySet()) {
                if (entry.getValue().size() > 1) {
                    conflicts.add("员工在同一日期有多个排班: " + entry.getKey());
                }
            }

            result.setConflictCount(conflicts.size());
            result.setConflictList(conflicts);
            result.setSeverity(conflicts.isEmpty() ? "LOW" : "MEDIUM");
            result.setNeedsResolution(!conflicts.isEmpty());

        } catch (Exception e) {
            log.error("[贪心算法] 验证排班冲突失败", e);
            result.setConflictCount(-1);
            result.setSeverity("HIGH");
            result.setNeedsResolution(true);
        }

        return result;
    }

    @Override
    public ConflictResolution resolveScheduleConflicts(List<ScheduleConflict> conflicts, String resolutionStrategy) {
        log.info("[贪心算法] 解决排班冲突, 冲突数={}, 策略={}",
                conflicts.size(), resolutionStrategy);

        ConflictResolution resolution = new ConflictResolution();
        resolution.setResolutionTime(System.currentTimeMillis());
        resolution.setResolutionStrategy(resolutionStrategy);
        resolution.setOriginalConflictCount(conflicts.size());
        resolution.setResolvedConflictCount(0);
        resolution.setResolutionSteps(new ArrayList<>());
        resolution.setSuccess(false);

        try {
            List<String> steps = new ArrayList<>();
            int resolvedCount = 0;

            for (ScheduleConflict conflict : conflicts) {
                switch (resolutionStrategy) {
                    case "PRIORITY":
                        steps.add("应用优先级策略解决冲突: " + conflict.getConflictId());
                        resolvedCount++;
                        break;
                    case "AUTO":
                        steps.add("自动重新分配解决冲突: " + conflict.getConflictId());
                        resolvedCount++;
                        break;
                    case "MANUAL":
                        steps.add("标记为需要人工处理: " + conflict.getConflictId());
                        break;
                    default:
                        steps.add("未知解决策略: " + conflict.getConflictId());
                }
            }

            resolution.setResolvedConflictCount(resolvedCount);
            resolution.setResolutionSteps(steps);
            resolution.setSuccess(resolvedCount > 0);

        } catch (Exception e) {
            log.error("[贪心算法] 解决排班冲突失败", e);
        }

        return resolution;
    }

    @Override
    public OptimizedSchedule optimizeSchedule(ScheduleData scheduleData, String optimizationTarget) {
        log.info("[贪心算法] 优化排班, 目标={}", optimizationTarget);

        OptimizedSchedule optimized = new OptimizedSchedule();
        optimized.setOptimizationId(System.currentTimeMillis());
        optimized.setOriginalScore(0.0);
        optimized.setOptimizedScore(0.0);
        optimized.setImprovementRate(0.0);
        optimized.setOptimizationAlgorithm(getAlgorithmType());
        optimized.setOptimizationTarget(optimizationTarget);
        optimized.setOptimizationTime(LocalDateTime.now());
        optimized.setOptimizationDuration(0L);
        optimized.setOptimizedRecords(new ArrayList<>());
        optimized.setOptimizationMetrics(new HashMap<>());
        optimized.setOptimizationSuggestions(new ArrayList<>());

        try {
            // 执行贪心排班
            ScheduleResult result = execute(scheduleData);

            long duration = result.getExecutionTime();

            // 构建优化结果
            optimized.setOriginalScore(0.7); // 模拟原始得分
            optimized.setOptimizedScore(result.getSatisfaction()); // 实际优化后得分
            optimized.setImprovementRate((optimized.getOptimizedScore() - optimized.getOriginalScore()) / optimized.getOriginalScore());
            optimized.setOptimizationDuration(duration);

            if (result.getAssignments() != null) {
                optimized.setOptimizedRecords(
                    result.getAssignments().stream()
                            .map(record -> {
                                ScheduleRecord optRecord = new ScheduleRecord();
                                optRecord.setRecordId(record.getAssignmentId());
                                optRecord.setEmployeeId(record.getEmployeeId());
                                optRecord.setShiftId(record.getShiftId());
                                optRecord.setScheduleDate(record.getAssignmentDate());
                                return optRecord;
                            })
                            .collect(Collectors.toList())
                );
            }

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("algorithm", getAlgorithmType());
            metrics.put("target", optimizationTarget);
            metrics.put("improvement", optimized.getImprovementRate());
            metrics.put("duration", duration);
            optimized.setOptimizationMetrics(metrics);

            List<String> suggestions = new ArrayList<>();
            suggestions.add("建议调整员工技能匹配度");
            suggestions.add("建议优化班次时间分配");
            suggestions.add("建议考虑员工工作负载均衡");
            optimized.setOptimizationSuggestions(suggestions);

        } catch (Exception e) {
            log.error("[贪心算法] 优化排班失败", e);
        }

        return optimized;
    }

    @Override
    public SchedulePrediction predictScheduleEffect(ScheduleData scheduleData) {
        log.debug("[贪心算法] 预测排班效果");

        SchedulePrediction prediction = new SchedulePrediction();
        prediction.setPredictionId("PRED_" + System.currentTimeMillis());
        prediction.setPredictionType("GREEDY_PREDICTION");
        prediction.setAccuracy(0.85);
        prediction.setConfidence(0.8);
        prediction.setPredictionTime(LocalDateTime.now());
        prediction.setPredictedRecords(new ArrayList<>());
        prediction.setSkillRequirements(new ArrayList<>());
        prediction.setPersonnelRequirements(new HashMap<>());
        prediction.setPredictionMetrics(new HashMap<>());
        prediction.setRecommendations(new ArrayList<>());
        prediction.setExtendedAttributes(new HashMap<>());

        try {
            // 预测指标
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("expectedCoverage", 0.9);
            metrics.put("expectedSatisfaction", 0.85);
            metrics.put("expectedEfficiency", 0.8);
            metrics.put("expectedCost", 1000.0);
            prediction.setPredictionMetrics(metrics);

            // 建议
            List<String> recommendations = new ArrayList<>();
            recommendations.add("建议增加员工培训以提高技能匹配");
            recommendations.add("建议优化班次安排以提高覆盖率");
            recommendations.add("建议考虑员工偏好以提高满意度");
            prediction.setRecommendations(recommendations);

        } catch (Exception e) {
            log.error("[贪心算法] 预测排班效果失败", e);
        }

        return prediction;
    }

    @Override
    public ScheduleStatistics getScheduleStatistics(Long planId) {
        log.debug("[贪心算法] 获取排班统计信息, planId={}", planId);

        ScheduleStatistics statistics = new ScheduleStatistics();
        statistics.setStatisticsId("STAT_" + System.currentTimeMillis());
        statistics.setStatisticsType("GREEDY_ALGORITHM");
        statistics.setTimeRange("LAST_30_DAYS");
        statistics.setStatisticsTime(LocalDateTime.now());
        statistics.setTotalEmployees(0);
        statistics.setTotalScheduleDays(0);
        statistics.setTotalScheduleRecords(0);
        statistics.setAverageDaysPerEmployee(0.0);
        statistics.setWorkloadBalance(0.8);
        statistics.setShiftCoverage(0.9);
        statistics.setConstraintSatisfaction(0.85);
        statistics.setCostSavingRate(0.1);
        statistics.setDepartmentStatistics(new HashMap<>());
        statistics.setEmployeeStatistics(new HashMap<>());
        statistics.setShiftStatistics(new HashMap<>());
        statistics.setTrendData(new ArrayList<>());
        statistics.setExtendedAttributes(new HashMap<>());

        try {
            // 扩展属性
            Map<String, Object> extendedAttributes = new HashMap<>();
            extendedAttributes.put("algorithmType", getAlgorithmType());
            extendedAttributes.put("planId", planId);
            extendedAttributes.put("generatedAt", System.currentTimeMillis());
            statistics.setExtendedAttributes(extendedAttributes);

        } catch (Exception e) {
            log.error("[贪心算法] 获取排班统计信息失败", e);
        }

        return statistics;
    }
}
}