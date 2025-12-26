package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import java.time.LocalDate;
import java.util.*;

/**
 * 染色体类 - 遗传算法的核心数据结构
 * <p>
 * 表示一个排班方案（个体）的基因编码
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-25
 */
@Data
public class Chromosome implements Comparable<Chromosome> {
    // 基因：员工ID -> (日期 -> 班次ID)
    private Map<Long, Map<LocalDate, Long>> genes = new HashMap<>();

    // 适应度相关
    private Double fitness = 0.0;
    private Double fairnessScore = 0.0;
    private Double costScore = 0.0;
    private Double efficiencyScore = 0.0;
    private Double satisfactionScore = 0.0;

    /**
     * 初始化染色体（原始方法，保留兼容性）
     */
    public void initialize(List<Long> employeeIds, List<LocalDate> dates, List<Long> shiftIds) {
        genes = new HashMap<>();
        Random random = new Random();
        for (Long employeeId : employeeIds) {
            Map<LocalDate, Long> schedule = new HashMap<>();
            for (LocalDate date : dates) {
                Long shiftId = shiftIds.get(random.nextInt(shiftIds.size()));
                schedule.put(date, shiftId);
            }
            genes.put(employeeId, schedule);
        }
    }

    /**
     * 获取指定员工在指定日期的班次
     */
    public Long getShift(Long employeeId, LocalDate date) {
        Map<LocalDate, Long> schedule = genes.get(employeeId);
        return schedule != null ? schedule.get(date) : null;
    }

    /**
     * 设置指定员工在指定日期的班次
     */
    public void setShift(Long employeeId, LocalDate date, Long shiftId) {
        genes.computeIfAbsent(employeeId, k -> new HashMap<>()).put(date, shiftId);
    }

    /**
     * 获取所有员工ID
     */
    public Set<Long> getEmployeeIds() {
        return genes.keySet();
    }

    /**
     * 获取指定员工的排班计划
     */
    public Map<LocalDate, Long> getSchedule(Long employeeId) {
        return genes.getOrDefault(employeeId, new HashMap<>());
    }

    /**
     * 克隆染色体
     */
    public Chromosome clone() {
        Chromosome cloned = new Chromosome();
        cloned.setFitness(this.fitness);
        cloned.setFairnessScore(this.fairnessScore);
        cloned.setCostScore(this.costScore);
        cloned.setEfficiencyScore(this.efficiencyScore);
        cloned.setSatisfactionScore(this.satisfactionScore);

        Map<Long, Map<LocalDate, Long>> clonedGenes = new HashMap<>();
        for (Map.Entry<Long, Map<LocalDate, Long>> entry : genes.entrySet()) {
            clonedGenes.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        cloned.setGenes(clonedGenes);
        return cloned;
    }

    @Override
    public int compareTo(Chromosome other) {
        return Double.compare(other.fitness, this.fitness);
    }

    // ==================== 遗传算法核心方法 ====================

    /**
     * 随机初始化染色体（静态工厂方法）
     */
    public static Chromosome random(OptimizationConfig config) {
        Chromosome chromosome = new Chromosome();

        // 计算日期范围
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = config.getStartDate();
        while (!current.isAfter(config.getEndDate())) {
            dates.add(current);
            current = current.plusDays(1);
        }

        // 初始化基因
        chromosome.initialize(config.getEmployeeIds(), dates, config.getShiftIds());

        return chromosome;
    }

    /**
     * 交叉操作（单点交叉）
     */
    public Chromosome crossover(Chromosome other) {
        Chromosome offspring = new Chromosome();

        Random random = new Random();
        Set<Long> employeeIds = new HashSet<>(this.genes.keySet());
        employeeIds.addAll(other.genes.keySet());

        // 对每个员工进行交叉
        for (Long employeeId : employeeIds) {
            Map<LocalDate, Long> thisSchedule = this.genes.get(employeeId);
            Map<LocalDate, Long> otherSchedule = other.genes.get(employeeId);

            if (thisSchedule == null || otherSchedule == null) {
                if (thisSchedule != null) {
                    offspring.getGenes().put(employeeId, new HashMap<>(thisSchedule));
                }
                if (otherSchedule != null) {
                    offspring.getGenes().put(employeeId, new HashMap<>(otherSchedule));
                }
                continue;
            }

            // 随机选择交叉点
            List<LocalDate> dates = new ArrayList<>(thisSchedule.keySet());
            if (dates.isEmpty()) {
                continue;
            }

            int crossoverPoint = random.nextInt(dates.size());
            Map<LocalDate, Long> newSchedule = new HashMap<>();

            for (int i = 0; i < dates.size(); i++) {
                LocalDate date = dates.get(i);
                if (i < crossoverPoint) {
                    newSchedule.put(date, thisSchedule.get(date));
                } else {
                    newSchedule.put(date, otherSchedule.get(date));
                }
            }

            offspring.getGenes().put(employeeId, newSchedule);
        }

        return offspring;
    }

    /**
     * 变异操作
     */
    public void mutate(OptimizationConfig config) {
        Random random = new Random();
        double mutationRate = config.getMutationRate();

        // 对每个基因进行变异
        for (Map.Entry<Long, Map<LocalDate, Long>> employeeEntry : this.genes.entrySet()) {
            Long employeeId = employeeEntry.getKey();
            Map<LocalDate, Long> schedule = employeeEntry.getValue();

            for (Map.Entry<LocalDate, Long> dateEntry : schedule.entrySet()) {
                // 根据变异率决定是否变异
                if (random.nextDouble() < mutationRate) {
                    LocalDate date = dateEntry.getKey();
                    // 随机选择新的班次
                    Long newShiftId = config.getShiftIds().get(
                        random.nextInt(config.getShiftIds().size())
                    );
                    schedule.put(date, newShiftId);
                }
            }
        }
    }

    /**
     * 复制染色体（别名方法）
     */
    public Chromosome copy() {
        return clone();
    }

    // ==================== 统计方法 ====================

    /**
     * 统计员工工作天数
     */
    public int countEmployeeWorkDays(long employeeId) {
        Map<LocalDate, Long> schedule = genes.get(employeeId);
        if (schedule == null) {
            return 0;
        }

        int count = 0;
        for (Map.Entry<LocalDate, Long> entry : schedule.entrySet()) {
            // 假设shiftId > 0表示工作日
            if (entry.getValue() != null && entry.getValue() > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * 统计加班班次（假设shiftId > 10000为加班）
     */
    public int countOvertimeShifts() {
        int count = 0;
        for (Map<LocalDate, Long> schedule : genes.values()) {
            for (Long shiftId : schedule.values()) {
                if (shiftId != null && shiftId > 10000) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 统计指定日期的在岗人员数
     */
    public int countStaffOnDay(int dayIndex) {
        // 获取所有日期
        if (genes.isEmpty()) {
            return 0;
        }

        Map<LocalDate, Long> firstSchedule = genes.values().iterator().next();
        List<LocalDate> dates = new ArrayList<>(firstSchedule.keySet());
        Collections.sort(dates);

        if (dayIndex < 0 || dayIndex >= dates.size()) {
            return 0;
        }

        LocalDate targetDate = dates.get(dayIndex);
        int count = 0;

        for (Map<LocalDate, Long> schedule : genes.values()) {
            Long shiftId = schedule.get(targetDate);
            if (shiftId != null && shiftId > 0) {
                count++;
            }
        }

        return count;
    }

    /**
     * 统计连续工作违规数
     */
    public int countConsecutiveWorkViolations(Integer maxConsecutiveDays) {
        if (maxConsecutiveDays == null) {
            maxConsecutiveDays = 7; // 默认最大连续工作7天
        }

        int violations = 0;

        for (Map<LocalDate, Long> schedule : genes.values()) {
            List<LocalDate> dates = new ArrayList<>(schedule.keySet());
            Collections.sort(dates);

            int consecutiveWorkDays = 0;

            for (LocalDate date : dates) {
                Long shiftId = schedule.get(date);
                if (shiftId != null && shiftId > 0) {
                    consecutiveWorkDays++;
                    if (consecutiveWorkDays > maxConsecutiveDays) {
                        violations++;
                    }
                } else {
                    consecutiveWorkDays = 0;
                }
            }
        }

        return violations;
    }

    // ==================== 评估方法 ====================

    /**
     * 评估适应度（加权计算）
     */
    public double evaluateFitness(OptimizationConfig config) {
        double fairness = calculateFairness(config);
        double cost = calculateCost(config);
        double efficiency = calculateEfficiency(config);
        double satisfaction = calculateSatisfaction(config);

        // 加权计算
        this.fitness = config.getFairnessWeight() * fairness +
                       config.getCostWeight() * cost +
                       config.getEfficiencyWeight() * efficiency +
                       config.getSatisfactionWeight() * satisfaction;

        // 保存各维度得分
        this.fairnessScore = fairness;
        this.costScore = cost;
        this.efficiencyScore = efficiency;
        this.satisfactionScore = satisfaction;

        return this.fitness;
    }

    /**
     * 验证约束条件
     */
    public boolean validateConstraints(OptimizationConfig config) {
        // 检查最大连续工作天数
        int violations = countConsecutiveWorkViolations(config.getMaxConsecutiveWorkDays());
        if (violations > 0) {
            return false;
        }

        return true;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 计算公平性得分（工作天数标准差的倒数）
     */
    private double calculateFairness(OptimizationConfig config) {
        List<Integer> workDaysList = new ArrayList<>();

        for (Long employeeId : config.getEmployeeIds()) {
            workDaysList.add(countEmployeeWorkDays(employeeId));
        }

        if (workDaysList.isEmpty()) {
            return 0.0;
        }

        // 计算平均值
        double mean = workDaysList.stream()
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(0.0);

        // 计算标准差
        double variance = workDaysList.stream()
                                       .mapToDouble(days -> Math.pow(days - mean, 2))
                                       .average()
                                       .orElse(0.0);

        double stdDev = Math.sqrt(variance);

        // 公平性得分 = 1 / (1 + 标准差)
        return 1.0 / (1.0 + stdDev);
    }

    /**
     * 计算成本得分（加班成本越低越好）
     */
    private double calculateCost(OptimizationConfig config) {
        int overtimeShifts = countOvertimeShifts();
        double totalCost = overtimeShifts * config.getOvertimeCostPerShift();

        // 成本得分 = 1 / (1 + 总成本)
        return 1.0 / (1.0 + totalCost / 1000.0); // 归一化
    }

    /**
     * 计算效率得分（人员利用率）
     */
    private double calculateEfficiency(OptimizationConfig config) {
        int totalShifts = 0;
        int assignedShifts = 0;

        for (Map<LocalDate, Long> schedule : genes.values()) {
            for (Long shiftId : schedule.values()) {
                totalShifts++;
                if (shiftId != null && shiftId > 0) {
                    assignedShifts++;
                }
            }
        }

        if (totalShifts == 0) {
            return 0.0;
        }

        // 效率得分 = 已分配班次 / 总班次
        return (double) assignedShifts / totalShifts;
    }

    /**
     * 计算满意度得分（简化实现）
     */
    private double calculateSatisfaction(OptimizationConfig config) {
        // 简化实现：基于公平性和效率
        double fairness = calculateFairness(config);
        double efficiency = calculateEfficiency(config);

        return (fairness + efficiency) / 2.0;
    }

    /**
     * 辅助方法：按日期统计人员数
     */
    private int countStaffOnDay(LocalDate date) {
        int count = 0;
        for (Map<LocalDate, Long> schedule : genes.values()) {
            Long shiftId = schedule.get(date);
            if (shiftId != null && shiftId > 0) {
                count++;
            }
        }
        return count;
    }
}
