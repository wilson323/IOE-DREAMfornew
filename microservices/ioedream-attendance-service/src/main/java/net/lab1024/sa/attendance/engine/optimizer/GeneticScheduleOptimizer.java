package net.lab1024.sa.attendance.engine.optimizer;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.Chromosome;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * 遗传算法排班优化器
 * <p>
 * 使用遗传算法优化排班方案：
 * - 种群初始化
 * - 选择（Selection）
 * - 交叉（Crossover）
 * - 变异（Mutation）
 * - 适应度评估
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class GeneticScheduleOptimizer {

    private final Random random = new Random();

    /**
     * 优化排班方案
     *
     * @param config 优化配置
     * @return 优化结果
     */
    public OptimizationResult optimize(OptimizationConfig config) {
        log.info("[遗传算法] 开始优化排班: employees={}, days={}, iterations={}",
                config.getEmployeeCount(), config.getPeriodDays(), config.getMaxIterations());

        long startTime = System.currentTimeMillis();

        // 1. 初始化种群
        List<Chromosome> population = initializePopulation(config);
        log.info("[遗传算法] 种群初始化完成: populationSize={}", population.size());

        // 2. 迭代优化
        Chromosome bestChromosome = null;
        double bestFitness = 0.0;
        int iterationWithoutImprovement = 0;

        for (int iteration = 0; iteration < config.getMaxIterations(); iteration++) {
            // 评估适应度
            for (Chromosome chromosome : population) {
                double fitness = evaluateFitness(chromosome, config);
                chromosome.setFitness(fitness);
            }

            // 排序（保留最优个体）
            population.sort(Comparator.comparingDouble(Chromosome::getFitness).reversed());
            Chromosome currentBest = population.get(0);

            // 更新全局最优
            if (currentBest.getFitness() > bestFitness) {
                bestFitness = currentBest.getFitness();
                bestChromosome = currentBest;
                iterationWithoutImprovement = 0;

                log.debug("[遗传算法] 迭代 {}: 新的最优解, fitness={}", iteration, bestFitness);
            } else {
                iterationWithoutImprovement++;
            }

            // 检查收敛条件
            if (iterationWithoutImprovement >= 50 || bestFitness >= 0.95) {
                log.info("[遗传算法] 收敛: iteration={}, bestFitness={}", iteration, bestFitness);
                break;
            }

            // 3. 选择
            List<Chromosome> selected = selection(population, config);

            // 4. 交叉
            List<Chromosome> offspring = crossover(selected, config);

            // 5. 变异
            mutate(offspring, config);

            // 6. 精英保留策略
            population = elitismReplacement(population, offspring, config);
        }

        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;

        // 构建结果
        OptimizationResult result = new OptimizationResult();
        result.setBestChromosome(bestChromosome);
        result.setBestFitness(bestFitness);
        result.setIterations(config.getMaxIterations());
        result.setExecutionDurationMs(durationMs);
        result.setConverged(iterationWithoutImprovement >= 50);

        log.info("[遗传算法] 优化完成: bestFitness={}, duration={}ms", bestFitness, durationMs);

        return result;
    }

    /**
     * 初始化种群
     */
    private List<Chromosome> initializePopulation(OptimizationConfig config) {
        List<Chromosome> population = new ArrayList<>();
        int populationSize = config.getPopulationSize();

        for (int i = 0; i < populationSize; i++) {
            Chromosome chromosome = Chromosome.random(config);
            population.add(chromosome);
        }

        return population;
    }

    /**
     * 评估适应度
     */
    private double evaluateFitness(Chromosome chromosome, OptimizationConfig config) {
        // 1. 公平性得分（40%）
        double fairnessScore = evaluateFairness(chromosome, config);

        // 2. 成本得分（30%）
        double costScore = evaluateCost(chromosome, config);

        // 3. 效率得分（20%）
        double efficiencyScore = evaluateEfficiency(chromosome, config);

        // 4. 满意度得分（10%）
        double satisfactionScore = evaluateSatisfaction(chromosome, config);

        // 综合适应度
        double fitness = fairnessScore * 0.4
                + costScore * 0.3
                + efficiencyScore * 0.2
                + satisfactionScore * 0.1;

        return fitness;
    }

    /**
     * 评估公平性
     */
    private double evaluateFairness(Chromosome chromosome, OptimizationConfig config) {
        // 计算工作日数的标准差（标准差越小，越公平）
        double[] workDayCounts = new double[config.getEmployeeCount()];
        double total = 0;

        for (int i = 0; i < config.getEmployeeCount(); i++) {
            workDayCounts[i] = chromosome.countEmployeeWorkDays((long) i);
            total += workDayCounts[i];
        }

        double mean = total / config.getEmployeeCount();
        double variance = 0;

        for (double count : workDayCounts) {
            variance += Math.pow(count - mean, 2);
        }

        variance /= config.getEmployeeCount();
        double stdDev = Math.sqrt(variance);

        // 转换为得分（标准差越小，得分越高）
        double score = 1.0 / (1.0 + stdDev);

        return score;
    }

    /**
     * 评估成本
     */
    private double evaluateCost(Chromosome chromosome, OptimizationConfig config) {
        // 计算加班成本（周末和节假日工作）
        double overtimeCost = chromosome.countOvertimeShifts() * config.getOvertimeCostPerShift();

        // 转换为得分（成本越低，得分越高）
        double maxCost = config.getEmployeeCount() * config.getPeriodDays() * config.getOvertimeCostPerShift();
        double score = 1.0 - (overtimeCost / maxCost);

        return Math.max(0, score);
    }

    /**
     * 评估效率
     */
    private double evaluateEfficiency(Chromosome chromosome, OptimizationConfig config) {
        // 计算人员利用率（实际在岗人数 / 需求人数）
        double totalUtilization = 0;
        int validDays = 0;

        for (int day = 0; day < config.getPeriodDays(); day++) {
            int actualStaff = chromosome.countStaffOnDay(day);
            int requiredStaff = config.getMinDailyStaff();

            if (requiredStaff > 0) {
                double utilization = (double) actualStaff / requiredStaff;
                // 限制在合理范围内（0.8-1.2）
                utilization = Math.max(0.8, Math.min(1.2, utilization));
                totalUtilization += utilization;
                validDays++;
            }
        }

        double avgUtilization = validDays > 0 ? totalUtilization / validDays : 0;

        // 转换为得分
        double score = Math.min(1.0, avgUtilization);

        return score;
    }

    /**
     * 评估满意度
     */
    private double evaluateSatisfaction(Chromosome chromosome, OptimizationConfig config) {
        // 计算连续工作天数违规次数
        int violations = chromosome.countConsecutiveWorkViolations(config.getMaxConsecutiveWorkDays());

        // 转换为得分（违规越少，得分越高）
        int maxPossibleViolations = config.getEmployeeCount() * (int) config.getPeriodDays();
        double score = 1.0 - (double) violations / maxPossibleViolations;

        return Math.max(0, score);
    }

    /**
     * 选择（轮盘赌选择）
     */
    private List<Chromosome> selection(List<Chromosome> population, OptimizationConfig config) {
        List<Chromosome> selected = new ArrayList<>();
        int selectionSize = (int) (population.size() * config.getSelectionRate());

        // 计算总适应度
        double totalFitness = population.stream()
                .mapToDouble(Chromosome::getFitness)
                .sum();

        for (int i = 0; i < selectionSize; i++) {
            double roulette = random.nextDouble() * totalFitness;
            double currentSum = 0;

            for (Chromosome chromosome : population) {
                currentSum += chromosome.getFitness();
                if (currentSum >= roulette) {
                    selected.add(chromosome);
                    break;
                }
            }
        }

        return selected;
    }

    /**
     * 交叉（单点交叉）
     */
    private List<Chromosome> crossover(List<Chromosome> parents, OptimizationConfig config) {
        List<Chromosome> offspring = new ArrayList<>();

        for (int i = 0; i < parents.size(); i += 2) {
            if (i + 1 < parents.size()) {
                Chromosome parent1 = parents.get(i);
                Chromosome parent2 = parents.get(i + 1);

                if (random.nextDouble() < config.getCrossoverRate()) {
                    Chromosome child = parent1.crossover(parent2);
                    offspring.add(child);
                    // 只生成一个后代，补充另一个父代
                    offspring.add(parent2);
                } else {
                    offspring.add(parent1);
                    offspring.add(parent2);
                }
            } else {
                // 奇数个父代，直接添加
                offspring.add(parents.get(i));
            }
        }

        return offspring;
    }

    /**
     * 变异
     */
    private void mutate(List<Chromosome> chromosomes, OptimizationConfig config) {
        for (Chromosome chromosome : chromosomes) {
            if (random.nextDouble() < config.getMutationRate()) {
                chromosome.mutate(config);
            }
        }
    }

    /**
     * 精英保留策略
     */
    private List<Chromosome> elitismReplacement(List<Chromosome> population,
                                                  List<Chromosome> offspring,
                                                  OptimizationConfig config) {
        int eliteSize = (int) (population.size() * config.getElitismRate());
        List<Chromosome> newPopulation = new ArrayList<>();

        // 保留精英个体
        for (int i = 0; i < eliteSize; i++) {
            newPopulation.add(population.get(i));
        }

        // 添加后代
        for (int i = 0; i < offspring.size() && newPopulation.size() < population.size(); i++) {
            newPopulation.add(offspring.get(i));
        }

        // 如果还没填满，随机生成补充
        while (newPopulation.size() < population.size()) {
            newPopulation.add(Chromosome.random(config));
        }

        return newPopulation;
    }
}
