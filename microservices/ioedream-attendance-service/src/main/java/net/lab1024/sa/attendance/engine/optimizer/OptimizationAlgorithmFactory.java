package net.lab1024.sa.attendance.engine.optimizer;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationResult;
import org.springframework.stereotype.Component;

/**
 * 优化算法工厂类
 * <p>
 * 根据配置选择合适的优化算法：
 * - GENETIC_ALGORITHM: 遗传算法（适合大规模问题）
 * - SIMULATED_ANNEALING: 模拟退火算法（适合中小规模问题）
 * - HYBRID: 混合算法（结合两种算法的优势）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class OptimizationAlgorithmFactory {

    private final GeneticScheduleOptimizer geneticOptimizer;
    private final SimulatedAnnealingOptimizer simulatedAnnealingOptimizer;

    public OptimizationAlgorithmFactory(GeneticScheduleOptimizer geneticOptimizer,
                                       SimulatedAnnealingOptimizer simulatedAnnealingOptimizer) {
        this.geneticOptimizer = geneticOptimizer;
        this.simulatedAnnealingOptimizer = simulatedAnnealingOptimizer;
    }

    /**
     * 优化算法类型
     */
    public enum AlgorithmType {
        GENETIC_ALGORITHM("遗传算法"),
        SIMULATED_ANNEALING("模拟退火"),
        HYBRID("混合算法");

        private final String description;

        AlgorithmType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 执行优化（自动选择算法）
     *
     * @param config 优化配置
     * @return 优化结果
     */
    public OptimizationResult optimize(OptimizationConfig config) {
        // 根据问题规模自动选择算法
        AlgorithmType algorithmType = selectAlgorithm(config);
        return optimize(config, algorithmType);
    }

    /**
     * 执行优化（指定算法）
     *
     * @param config        优化配置
     * @param algorithmType 算法类型
     * @return 优化结果
     */
    public OptimizationResult optimize(OptimizationConfig config, AlgorithmType algorithmType) {
        log.info("[算法工厂] 使用算法: {}, 规模: {}员工 × {}天",
                algorithmType.getDescription(), config.getEmployeeCount(), config.getPeriodDays());

        switch (algorithmType) {
            case GENETIC_ALGORITHM:
                return geneticOptimizer.optimize(config);

            case SIMULATED_ANNEALING:
                return simulatedAnnealingOptimizer.optimize(config);

            case HYBRID:
                return optimizeHybrid(config);

            default:
                throw new IllegalArgumentException("未知的算法类型: " + algorithmType);
        }
    }

    /**
     * 混合算法优化（先用遗传算法快速收敛，再用模拟退火精细优化）
     */
    private OptimizationResult optimizeHybrid(OptimizationConfig config) {
        log.info("[混合算法] 开始两阶段优化");

        // 阶段1: 遗传算法（快速收敛到较优解）
        log.info("[混合算法] 阶段1: 遗传算法快速搜索");
        OptimizationConfig gaConfig = cloneConfig(config);
        gaConfig.setMaxGenerations(config.getMaxGenerations() / 2); // 使用一半迭代次数
        OptimizationResult gaResult = geneticOptimizer.optimize(gaConfig);

        // 阶段2: 模拟退火（在遗传算法结果基础上精细优化）
        log.info("[混合算法] 阶段2: 模拟退火精细优化");
        OptimizationConfig saConfig = cloneConfig(config);
        saConfig.setMaxGenerations(config.getMaxGenerations() / 2); // 使用一半迭代次数
        // 可以将遗传算法的最优解作为模拟退火的初始解（需要额外实现）

        OptimizationResult saResult = simulatedAnnealingOptimizer.optimize(saConfig);

        // 选择更优的结果
        OptimizationResult finalResult = gaResult.getBestFitness() >= saResult.getBestFitness() ?
                gaResult : saResult;

        // TODO: 设置算法类型（需要添加algorithmVersion字段到OptimizationResult）
        // finalResult.setAlgorithmType("HYBRID");
        log.info("[混合算法] 两阶段优化完成: bestFitness={}", finalResult.getBestFitness());

        return finalResult;
    }

    /**
     * 根据问题规模自动选择算法
     */
    private AlgorithmType selectAlgorithm(OptimizationConfig config) {
        int problemSize = config.getEmployeeCount() * (int) config.getPeriodDays();

        // 小规模问题（<500）：使用模拟退火
        if (problemSize < 500) {
            return AlgorithmType.SIMULATED_ANNEALING;
        }

        // 大规模问题（>=2000）：使用遗传算法
        if (problemSize >= 2000) {
            return AlgorithmType.GENETIC_ALGORITHM;
        }

        // 中等规模问题：使用混合算法
        return AlgorithmType.HYBRID;
    }

    /**
     * 克隆配置（避免修改原配置）
     */
    private OptimizationConfig cloneConfig(OptimizationConfig original) {
        return OptimizationConfig.builder()
                .planId(original.getPlanId())
                .employeeIds(original.getEmployeeIds())
                .shiftIds(original.getShiftIds())
                .startDate(original.getStartDate())
                .endDate(original.getEndDate())
                .optimizationGoal(original.getOptimizationGoal())
                .maxConsecutiveWorkDays(original.getMaxConsecutiveWorkDays())
                .minRestDays(original.getMinRestDays())
                .minDailyStaff(original.getMinDailyStaff())
                .fairnessWeight(original.getFairnessWeight())
                .costWeight(original.getCostWeight())
                .efficiencyWeight(original.getEfficiencyWeight())
                .satisfactionWeight(original.getSatisfactionWeight())
                .algorithmType(original.getAlgorithmType())
                .populationSize(original.getPopulationSize())
                .maxGenerations(original.getMaxGenerations())
                .crossoverRate(original.getCrossoverRate())
                .mutationRate(original.getMutationRate())
                .initialTemperature(original.getInitialTemperature())
                .coolingRate(original.getCoolingRate())
                .build();
    }

    /**
     * 获取算法建议
     */
    public String getAlgorithmRecommendation(OptimizationConfig config) {
        int problemSize = config.getEmployeeCount() * (int) config.getPeriodDays();
        AlgorithmType recommendedAlgorithm = selectAlgorithm(config);

        StringBuilder sb = new StringBuilder();
        sb.append("问题规模: ").append(problemSize).append(" (").append(config.getEmployeeCount())
                .append("员工 × ").append(config.getPeriodDays()).append("天)\n");
        sb.append("推荐算法: ").append(recommendedAlgorithm.getDescription()).append("\n");
        sb.append("算法说明: ");

        switch (recommendedAlgorithm) {
            case SIMULATED_ANNEALING:
                sb.append("模拟退火适合小规模问题，能够快速找到较优解，避免陷入局部最优。");
                break;
            case GENETIC_ALGORITHM:
                sb.append("遗传算法适合大规模问题，通过种群进化全局搜索，找到全局最优解。");
                break;
            case HYBRID:
                sb.append("混合算法结合遗传算法的全局搜索能力和模拟退火的局部优化能力，适合中等规模问题。");
                break;
        }

        return sb.toString();
    }
}
