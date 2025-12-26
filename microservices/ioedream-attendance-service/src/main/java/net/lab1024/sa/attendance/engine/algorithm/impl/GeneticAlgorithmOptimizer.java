package net.lab1024.sa.attendance.engine.algorithm.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.common.entity.attendance.ScheduleRecordEntity;
import net.lab1024.sa.common.entity.attendance.WorkShiftEntity;
import net.lab1024.sa.common.system.employee.domain.entity.EmployeeEntity;

/**
 * 遗传算法排班优化器
 * <p>
 * 使用遗传算法优化员工排班，实现：
 * - 种群初始化
 * - 适应度评估
 * - 选择操作（锦标赛选择）
 * - 交叉操作（单点交叉）
 * - 变异操作（交换变异）
 * - 精英保留策略
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class GeneticAlgorithmOptimizer {

    // ==================== 遗传算法参数 ====================

    /** 种群大小 */
    private static final int POPULATION_SIZE = 100;

    /** 最大迭代次数 */
    private static final int MAX_GENERATIONS = 500;

    /** 变异率 */
    private static final double MUTATION_RATE = 0.02;

    /** 交叉率 */
    private static final double CROSSOVER_RATE = 0.8;

    /** 精英保留数量 */
    private static final int ELITE_COUNT = 5;

    /** 锦标赛选择规模 */
    private static final int TOURNAMENT_SIZE = 5;

    /** 随机数生成器 */
    private final Random random = new Random();

    // ==================== 适应度权重 ====================

    /** 工作负荷均衡权重 */
    private static final double WORKLOAD_BALANCE_WEIGHT = 0.3;

    /** 班次公平性权重 */
    private static final double SHIFT_FAIRNESS_WEIGHT = 0.25;

    /** 周末偏好权重 */
    private static final double WEEKEND_PREFERENCE_WEIGHT = 0.2;

    /** 技能匹配权重 */
    private static final double SKILL_MATCH_WEIGHT = 0.15;

    /** 约束满足权重 */
    private static final double CONSTRAINT_SATISFACTION_WEIGHT = 0.1;

    // ==================== 主优化方法 ====================

    /**
     * 遗传算法优化主方法
     *
     * @param employees    员工列表
     * @param shifts       班次列表
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param constraints  约束条件
     * @return 优化后的排班记录列表
     */
    public List<ScheduleRecordEntity> optimize(
            List<EmployeeEntity> employees,
            List<WorkShiftEntity> shifts,
            LocalDate startDate,
            LocalDate endDate,
            Map<String, Object> constraints) {

        log.info("[遗传算法] 开始优化排班: employees={}, shifts={}, period={}-{}",
                employees.size(), shifts.size(), startDate, endDate);

        long startTime = System.currentTimeMillis();

        try {
            // 1. 初始化种群
            List<ScheduleChromosome> population = initializePopulation(
                    employees, shifts, startDate, endDate, constraints);

            log.debug("[遗传算法] 初始种群生成完成: populationSize={}", population.size());

            // 2. 计算初始适应度
            evaluatePopulation(population, employees, shifts, constraints);

            // 记录初始最优解
            ScheduleChromosome bestChromosome = getBestChromosome(population);
            double bestFitness = bestChromosome.getFitness();
            log.info("[遗传算法] 初始最优适应度: {}", bestFitness);

            // 3. 进化迭代
            int stagnationCount = 0; // 停滞计数器
            double previousBestFitness = bestFitness;

            for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
                // 3.1 选择操作
                List<ScheduleChromosome> selected = selection(population);

                // 3.2 交叉操作
                List<ScheduleChromosome> offspring = crossover(selected, employees, shifts, startDate, endDate);

                // 3.3 变异操作
                mutate(offspring, shifts, constraints);

                // 3.4 评估子代适应度
                evaluatePopulation(offspring, employees, shifts, constraints);

                // 3.5 精英保留 + 环境选择
                population = evolutionaryReplacement(population, offspring);

                // 3.6 更新最优解
                bestChromosome = getBestChromosome(population);
                double currentBestFitness = bestChromosome.getFitness();

                // 3.7 记录进化过程
                if (generation % 50 == 0 || currentBestFitness > previousBestFitness) {
                    log.info("[遗传算法] 代数: {}, 最优适应度: {}, 平均适应度: {}",
                            generation, currentBestFitness,
                            population.stream().mapToDouble(ScheduleChromosome::getFitness).average().orElse(0));
                }

                // 3.8 早停判断（适应度无提升）
                if (currentBestFitness > previousBestFitness) {
                    previousBestFitness = currentBestFitness;
                    stagnationCount = 0;
                } else {
                    stagnationCount++;
                    if (stagnationCount >= 50) {
                        log.info("[遗传算法] 适应度连续50代无提升，提前终止: 最终适应度={}", currentBestFitness);
                        break;
                    }
                }
            }

            // 4. 返回最优解
            List<ScheduleRecordEntity> bestSchedule = bestChromosome.getGenes();

            long endTime = System.currentTimeMillis();
            log.info("[遗传算法] 优化完成: 耗时={}ms, 最终适应度={}, 排班记录数={}",
                    endTime - startTime, bestChromosome.getFitness(), bestSchedule.size());

            return bestSchedule;

        } catch (Exception e) {
            log.error("[遗传算法] 优化失败", e);
            throw new RuntimeException("遗传算法优化失败: " + e.getMessage(), e);
        }
    }

    // ==================== 种群初始化 ====================

    /**
     * 初始化种群
     */
    private List<ScheduleChromosome> initializePopulation(
            List<EmployeeEntity> employees,
            List<WorkShiftEntity> shifts,
            LocalDate startDate,
            LocalDate endDate,
            Map<String, Object> constraints) {

        List<ScheduleChromosome> population = new ArrayList<>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<ScheduleRecordEntity> genes = createRandomSchedule(
                    employees, shifts, startDate, endDate, constraints);
            population.add(new ScheduleChromosome(genes));
        }

        return population;
    }

    /**
     * 创建随机排班
     */
    private List<ScheduleRecordEntity> createRandomSchedule(
            List<EmployeeEntity> employees,
            List<WorkShiftEntity> shifts,
            LocalDate startDate,
            LocalDate endDate,
            Map<String, Object> constraints) {

        List<ScheduleRecordEntity> schedule = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            for (EmployeeEntity employee : employees) {
                // 随机选择班次（根据约束条件）
                WorkShiftEntity shift = selectRandomShift(shifts, currentDate, constraints);
                if (shift != null) {
                    ScheduleRecordEntity record = createScheduleRecord(employee, shift, currentDate);
                    schedule.add(record);
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return schedule;
    }

    /**
     * 随机选择班次
     */
    private WorkShiftEntity selectRandomShift(List<WorkShiftEntity> shifts, LocalDate date, Map<String, Object> constraints) {
        // 过滤出适用当天的班次
        List<WorkShiftEntity> applicableShifts = shifts.stream()
                .filter(shift -> isShiftApplicable(shift, date))
                .collect(Collectors.toList());

        if (applicableShifts.isEmpty()) {
            return null;
        }

        // 随机选择
        return applicableShifts.get(random.nextInt(applicableShifts.size()));
    }

    /**
     * 检查班次是否适用
     */
    private boolean isShiftApplicable(WorkShiftEntity shift, LocalDate date) {
        // 检查班次状态（使用deletedFlag判断）
        Integer deletedFlag = shift.getDeletedFlag();
        if (deletedFlag != null && deletedFlag != 0) {
            return false;
        }

        // TODO: 工作日限制功能待扩展
        // WorkShiftEntity当前没有workingDays字段，需要在后续版本中添加
        // if (shift.getWorkingDays() != null && !shift.getWorkingDays().isEmpty()) {
        //     DayOfWeek dayOfWeek = date.getDayOfWeek();
        //     String dayName = dayOfWeek.toString();
        //     if (!shift.getWorkingDays().contains(dayName)) {
        //         return false;
        //     }
        // }

        return true;
    }

    /**
     * 创建排班记录
     */
    private ScheduleRecordEntity createScheduleRecord(EmployeeEntity employee, WorkShiftEntity shift, LocalDate date) {
        ScheduleRecordEntity record = new ScheduleRecordEntity();
        record.setEmployeeId(employee.getId());
        record.setScheduleDate(date);
        record.setShiftId(shift.getShiftId());
        record.setScheduleType("正常排班");
        record.setIsTemporary(false);
        record.setStatus(1); // 正常状态
        record.setWorkHours(shift.getWorkDuration().doubleValue());
        record.setCreateUserId(1L); // 系统创建
        record.setPriority(1);
        return record;
    }

    // ==================== 适应度评估 ====================

    /**
     * 评估种群适应度
     */
    private void evaluatePopulation(
            List<ScheduleChromosome> population,
            List<EmployeeEntity> employees,
            List<WorkShiftEntity> shifts,
            Map<String, Object> constraints) {

        for (ScheduleChromosome chromosome : population) {
            double fitness = calculateFitness(chromosome.getGenes(), employees, shifts, constraints);
            chromosome.setFitness(fitness);
        }
    }

    /**
     * 计算适应度
     */
    private double calculateFitness(
            List<ScheduleRecordEntity> schedule,
            List<EmployeeEntity> employees,
            List<WorkShiftEntity> shifts,
            Map<String, Object> constraints) {

        // 1. 工作负荷均衡度 (0-1)
        double workloadBalance = calculateWorkloadBalance(schedule, employees);

        // 2. 班次公平性 (0-1)
        double shiftFairness = calculateShiftFairness(schedule, employees, shifts);

        // 3. 周末偏好满足度 (0-1)
        double weekendPreference = calculateWeekendPreference(schedule, constraints);

        // 4. 技能匹配度 (0-1)
        double skillMatch = calculateSkillMatch(schedule, employees, shifts);

        // 5. 约束满足度 (0-1，有惩罚)
        double constraintSatisfaction = calculateConstraintSatisfaction(schedule, constraints);

        // 6. 加权总和
        double fitness = WORKLOAD_BALANCE_WEIGHT * workloadBalance
                + SHIFT_FAIRNESS_WEIGHT * shiftFairness
                + WEEKEND_PREFERENCE_WEIGHT * weekendPreference
                + SKILL_MATCH_WEIGHT * skillMatch
                + CONSTRAINT_SATISFACTION_WEIGHT * constraintSatisfaction;

        return fitness;
    }

    /**
     * 计算工作负荷均衡度
     */
    private double calculateWorkloadBalance(List<ScheduleRecordEntity> schedule, List<EmployeeEntity> employees) {
        // 计算每个员工的总工作时长
        Map<Long, Double> employeeWorkHours = new HashMap<>();
        for (EmployeeEntity employee : employees) {
            employeeWorkHours.put(employee.getId(), 0.0);
        }

        for (ScheduleRecordEntity record : schedule) {
            Long employeeId = record.getEmployeeId();
            Double hours = record.getWorkDuration() != null ? record.getWorkDuration() : 0.0;
            employeeWorkHours.merge(employeeId, hours, Double::sum);
        }

        // 计算标准差（越小越均衡）
        List<Double> hoursList = new ArrayList<>(employeeWorkHours.values());
        if (hoursList.isEmpty()) {
            return 0.0;
        }

        double mean = hoursList.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = hoursList.stream()
                .mapToDouble(hours -> Math.pow(hours - mean, 2))
                .average().orElse(0);
        double stdDev = Math.sqrt(variance);

        // 转换为适应度（标准差越小，适应度越高）
        return 1.0 / (1.0 + stdDev);
    }

    /**
     * 计算班次公平性
     */
    private double calculateShiftFairness(
            List<ScheduleRecordEntity> schedule,
            List<EmployeeEntity> employees,
            List<WorkShiftEntity> shifts) {

        // 计算每个班次的分配次数
        Map<Long, Map<Long, Integer>> shiftAssignmentCount = new HashMap<>();
        for (EmployeeEntity employee : employees) {
            shiftAssignmentCount.put(employee.getId(), new HashMap<>());
            for (WorkShiftEntity shift : shifts) {
                shiftAssignmentCount.get(employee.getId()).put(shift.getShiftId(), 0);
            }
        }

        for (ScheduleRecordEntity record : schedule) {
            Long employeeId = record.getEmployeeId();
            Long shiftId = record.getShiftId();
            shiftAssignmentCount.get(employeeId).merge(shiftId, 1, Integer::sum);
        }

        // 计算公平性（使用基尼系数）
        List<Integer> allCounts = shiftAssignmentCount.values().stream()
                .flatMap(map -> map.values().stream())
                .collect(Collectors.toList());

        if (allCounts.isEmpty()) {
            return 1.0;
        }

        Collections.sort(allCounts);
        int n = allCounts.size();
        double sum = allCounts.stream().mapToInt(Integer::intValue).sum();
        if (sum == 0) {
            return 1.0;
        }

        double giniSum = 0;
        for (int i = 0; i < n; i++) {
            giniSum += (2 * (i + 1) - n - 1) * allCounts.get(i);
        }
        double gini = giniSum / (n * sum);

        // 基尼系数越小越公平，转换为适应度
        return 1.0 - gini;
    }

    /**
     * 计算周末偏好满足度
     */
    private double calculateWeekendPreference(List<ScheduleRecordEntity> schedule, Map<String, Object> constraints) {
        Boolean weekendBalance = (Boolean) constraints.getOrDefault("weekendBalance", true);

        if (!weekendBalance) {
            return 1.0; // 不需要周末均衡
        }

        // 计算周末班次分配均衡度
        Map<Long, Integer> weekendCount = new HashMap<>();
        Map<Long, Integer> totalWeekends = new HashMap<>();

        for (ScheduleRecordEntity record : schedule) {
            LocalDate date = record.getScheduleDate();
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                Long employeeId = record.getEmployeeId();
                weekendCount.merge(employeeId, 1, Integer::sum);
                totalWeekends.merge(employeeId, 1, Integer::sum);
            }
        }

        // 计算均衡度
        if (totalWeekends.isEmpty()) {
            return 1.0;
        }

        List<Integer> counts = new ArrayList<>(weekendCount.values());
        if (counts.isEmpty()) {
            return 1.0;
        }

        double mean = counts.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = counts.stream()
                .mapToDouble(count -> Math.pow(count - mean, 2))
                .average().orElse(0);

        return 1.0 / (1.0 + Math.sqrt(variance));
    }

    /**
     * 计算技能匹配度
     */
    private double calculateSkillMatch(
            List<ScheduleRecordEntity> schedule,
            List<EmployeeEntity> employees,
            List<WorkShiftEntity> shifts) {

        // 简化实现：假设所有员工都具备所需技能
        // 实际应用中应根据员工技能和班次要求进行匹配
        return 1.0;
    }

    /**
     * 计算约束满足度
     */
    private double calculateConstraintSatisfaction(List<ScheduleRecordEntity> schedule, Map<String, Object> constraints) {
        double satisfaction = 1.0;
        int violationCount = 0;

        // 1. 检查最大连续工作天数
        int maxConsecutiveDays = (int) constraints.getOrDefault("maxConsecutiveDays", 6);
        if (checkConsecutiveDaysViolation(schedule, maxConsecutiveDays)) {
            violationCount++;
            satisfaction -= 0.2;
        }

        // 2. 检查最小休息时长
        int minRestHours = (int) constraints.getOrDefault("minRestHours", 12);
        if (checkRestHoursViolation(schedule, minRestHours)) {
            violationCount++;
            satisfaction -= 0.2;
        }

        // 3. 检查最大周工作时长
        double maxWeeklyHours = (double) constraints.getOrDefault("maxWeeklyHours", 48.0);
        if (checkWeeklyHoursViolation(schedule, maxWeeklyHours)) {
            violationCount++;
            satisfaction -= 0.2;
        }

        // 4. 检查每天同一班次人数限制
        if (checkDailyShiftCapacityViolation(schedule)) {
            violationCount++;
            satisfaction -= 0.2;
        }

        // 5. 检查员工冲突（同一员工同一天多次排班）
        if (checkEmployeeConflictViolation(schedule)) {
            violationCount++;
            satisfaction -= 0.2;
        }

        return Math.max(0.0, satisfaction);
    }

    /**
     * 检查连续工作天数违规
     */
    private boolean checkConsecutiveDaysViolation(List<ScheduleRecordEntity> schedule, int maxDays) {
        // 按员工分组
        Map<Long, List<ScheduleRecordEntity>> employeeSchedules = schedule.stream()
                .collect(Collectors.groupingBy(ScheduleRecordEntity::getEmployeeId));

        for (Map.Entry<Long, List<ScheduleRecordEntity>> entry : employeeSchedules.entrySet()) {
            List<LocalDate> dates = entry.getValue().stream()
                    .map(ScheduleRecordEntity::getScheduleDate)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            int consecutive = 0;
            LocalDate prevDate = null;

            for (LocalDate date : dates) {
                if (prevDate != null && date.equals(prevDate.plusDays(1))) {
                    consecutive++;
                    if (consecutive > maxDays) {
                        return true;
                    }
                } else {
                    consecutive = 1;
                }
                prevDate = date;
            }
        }

        return false;
    }

    /**
     * 检查休息时长违规
     */
    private boolean checkRestHoursViolation(List<ScheduleRecordEntity> schedule, int minRestHours) {
        // 简化实现：检查相邻班次的间隔
        // 实际应用中需要根据班次的具体开始/结束时间计算
        return false;
    }

    /**
     * 检查周工作时长违规
     */
    private boolean checkWeeklyHoursViolation(List<ScheduleRecordEntity> schedule, double maxHours) {
        // 按员工和周分组
        Map<Long, Map<Integer, Double>> employeeWeeklyHours = new HashMap<>();

        for (ScheduleRecordEntity record : schedule) {
            Long employeeId = record.getEmployeeId();
            LocalDate date = record.getScheduleDate();
            int weekOfYear = date.getYear() * 100 + date.getDayOfWeek().getValue(); // 简化周计算

            employeeWeeklyHours
                    .computeIfAbsent(employeeId, k -> new HashMap<>())
                    .merge(weekOfYear, record.getWorkDuration() != null ? record.getWorkDuration() : 0.0, Double::sum);
        }

        // 检查是否超过最大时长
        for (Map<Integer, Double> weeklyHours : employeeWeeklyHours.values()) {
            for (double hours : weeklyHours.values()) {
                if (hours > maxHours) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查每日班次容量违规
     */
    private boolean checkDailyShiftCapacityViolation(List<ScheduleRecordEntity> schedule) {
        // 简化实现：假设班次容量充足
        return false;
    }

    /**
     * 检查员工冲突违规
     */
    private boolean checkEmployeeConflictViolation(List<ScheduleRecordEntity> schedule) {
        // 检查同一员工同一天是否有多次排班
        Map<String, Long> keyCount = new HashMap<>();

        for (ScheduleRecordEntity record : schedule) {
            String key = record.getEmployeeId() + "_" + record.getScheduleDate();
            if (keyCount.containsKey(key)) {
                return true; // 发现重复
            }
            keyCount.put(key, 1L);
        }

        return false;
    }

    // ==================== 选择操作 ====================

    /**
     * 锦标赛选择
     */
    private List<ScheduleChromosome> selection(List<ScheduleChromosome> population) {
        List<ScheduleChromosome> selected = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {
            ScheduleChromosome winner = tournamentSelection(population);
            selected.add(winner);
        }

        return selected;
    }

    /**
     * 锦标赛选择单个个体
     */
    private ScheduleChromosome tournamentSelection(List<ScheduleChromosome> population) {
        ScheduleChromosome best = null;

        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int index = random.nextInt(population.size());
            ScheduleChromosome contestant = population.get(index);

            if (best == null || contestant.getFitness() > best.getFitness()) {
                best = contestant;
            }
        }

        return best;
    }

    // ==================== 交叉操作 ====================

    /**
     * 单点交叉
     */
    private List<ScheduleChromosome> crossover(
            List<ScheduleChromosome> parents,
            List<EmployeeEntity> employees,
            List<WorkShiftEntity> shifts,
            LocalDate startDate,
            LocalDate endDate) {

        List<ScheduleChromosome> offspring = new ArrayList<>();

        for (int i = 0; i < parents.size(); i += 2) {
            if (i + 1 >= parents.size()) {
                offspring.add(parents.get(i));
                continue;
            }

            ScheduleChromosome parent1 = parents.get(i);
            ScheduleChromosome parent2 = parents.get(i + 1);

            // 根据交叉率决定是否交叉
            if (random.nextDouble() < CROSSOVER_RATE) {
                List<ScheduleRecordEntity> genes1 = parent1.getGenes();
                List<ScheduleRecordEntity> genes2 = parent2.getGenes();

                if (genes1.size() > 1 && genes2.size() > 1) {
                    int crossoverPoint = random.nextInt(Math.min(genes1.size(), genes2.size()));

                    List<ScheduleRecordEntity> child1Genes = new ArrayList<>(genes1.subList(0, crossoverPoint));
                    child1Genes.addAll(genes2.subList(crossoverPoint, genes2.size()));

                    List<ScheduleRecordEntity> child2Genes = new ArrayList<>(genes2.subList(0, crossoverPoint));
                    child2Genes.addAll(genes1.subList(crossoverPoint, genes1.size()));

                    offspring.add(new ScheduleChromosome(child1Genes));
                    offspring.add(new ScheduleChromosome(child2Genes));
                } else {
                    offspring.add(parent1);
                    offspring.add(parent2);
                }
            } else {
                offspring.add(parent1);
                offspring.add(parent2);
            }
        }

        return offspring;
    }

    // ==================== 变异操作 ====================

    /**
     * 交换变异
     */
    private void mutate(List<ScheduleChromosome> population, List<WorkShiftEntity> shifts, Map<String, Object> constraints) {
        for (ScheduleChromosome chromosome : population) {
            if (random.nextDouble() < MUTATION_RATE) {
                performMutation(chromosome, shifts, constraints);
            }
        }
    }

    /**
     * 执行变异操作
     */
    private void performMutation(ScheduleChromosome chromosome, List<WorkShiftEntity> shifts, Map<String, Object> constraints) {
        List<ScheduleRecordEntity> genes = chromosome.getGenes();

        if (genes.isEmpty()) {
            return;
        }

        // 随机选择一个基因进行变异
        int index = random.nextInt(genes.size());
        ScheduleRecordEntity record = genes.get(index);

        // 随机选择一个新班次
        WorkShiftEntity newShift = selectRandomShift(shifts, record.getScheduleDate(), constraints);
        if (newShift != null && !newShift.getShiftId().equals(record.getShiftId())) {
            record.setShiftId(newShift.getShiftId());
            record.setWorkHours(newShift.getWorkDuration().doubleValue());
        }
    }

    // ==================== 环境选择（精英保留） ====================

    /**
     * 进化替代策略：精英保留 + 最佳个体选择
     */
    private List<ScheduleChromosome> evolutionaryReplacement(
            List<ScheduleChromosome> population,
            List<ScheduleChromosome> offspring) {

        // 合并父代和子代
        List<ScheduleChromosome> combined = new ArrayList<>();
        combined.addAll(population);
        combined.addAll(offspring);

        // 按适应度排序
        combined.sort(Comparator.comparingDouble(ScheduleChromosome::getFitness).reversed());

        // 选择前POPULATION_SIZE个个体
        return combined.stream()
                .limit(POPULATION_SIZE)
                .collect(Collectors.toList());
    }

    /**
     * 获取最优染色体
     */
    private ScheduleChromosome getBestChromosome(List<ScheduleChromosome> population) {
        return population.stream()
                .max(Comparator.comparingDouble(ScheduleChromosome::getFitness))
                .orElse(null);
    }

    // ==================== 染色体类 ====================

    /**
     * 染色体类：表示一个排班方案
     */
    public static class ScheduleChromosome {
        private final List<ScheduleRecordEntity> genes;
        private double fitness;

        public ScheduleChromosome(List<ScheduleRecordEntity> genes) {
            this.genes = new ArrayList<>(genes);
            this.fitness = 0.0;
        }

        public List<ScheduleRecordEntity> getGenes() {
            return genes;
        }

        public double getFitness() {
            return fitness;
        }

        public void setFitness(double fitness) {
            this.fitness = fitness;
        }
    }
}
