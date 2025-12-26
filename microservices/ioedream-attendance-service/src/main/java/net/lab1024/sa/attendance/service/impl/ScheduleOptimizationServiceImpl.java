package net.lab1024.sa.attendance.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationResult;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationAlgorithmFactory;
import net.lab1024.sa.attendance.service.ScheduleOptimizationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 排班优化服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class ScheduleOptimizationServiceImpl implements ScheduleOptimizationService {

    @Resource
    private OptimizationAlgorithmFactory optimizationAlgorithmFactory;

    @Override
    public OptimizationResult optimizeWithGeneticAlgorithm(OptimizationConfig config) {
        log.info("[优化服务] 执行遗传算法优化: {}员工 × {}天",
                config.getEmployeeCount(), config.getPeriodDays());

        return optimizationAlgorithmFactory.optimize(config,
                OptimizationAlgorithmFactory.AlgorithmType.GENETIC_ALGORITHM);
    }

    @Override
    public OptimizationResult optimizeWithSimulatedAnnealing(OptimizationConfig config) {
        log.info("[优化服务] 执行模拟退火优化: {}员工 × {}天",
                config.getEmployeeCount(), config.getPeriodDays());

        return optimizationAlgorithmFactory.optimize(config,
                OptimizationAlgorithmFactory.AlgorithmType.SIMULATED_ANNEALING);
    }

    @Override
    public OptimizationResult optimizeWithHybridAlgorithm(OptimizationConfig config) {
        log.info("[优化服务] 执行混合算法优化: {}员工 × {}天",
                config.getEmployeeCount(), config.getPeriodDays());

        return optimizationAlgorithmFactory.optimize(config,
                OptimizationAlgorithmFactory.AlgorithmType.HYBRID);
    }

    @Override
    public OptimizationResult optimizeAuto(OptimizationConfig config) {
        log.info("[优化服务] 自动选择算法优化: {}员工 × {}天",
                config.getEmployeeCount(), config.getPeriodDays());

        return optimizationAlgorithmFactory.optimize(config);
    }

    @Override
    public Map<String, Object> evaluateResult(OptimizationResult result) {
        log.info("[优化服务] 评估优化结果: fitness={}", result.getBestFitness());

        Map<String, Object> evaluation = new HashMap<>();

        // 基本信息
        evaluation.put("bestFitness", result.getBestFitness());
        evaluation.put("qualityLevel", result.getQualityLevel());
        evaluation.put("qualityDescription", result.getQualityLevelDescription());
        evaluation.put("isHighQuality", result.isHighQualitySolution());
        evaluation.put("isAcceptable", result.isAcceptableSolution());

        // 执行信息
        evaluation.put("iterations", result.getIterations());
        evaluation.put("executionDurationMs", result.getExecutionDurationMs());
        evaluation.put("executionDurationSeconds", result.getExecutionDurationSeconds());
        evaluation.put("executionSpeed", result.getExecutionSpeed());
        evaluation.put("converged", result.getConverged());

        // 分项得分
        evaluation.put("fairnessScore", result.getFairnessScore());
        evaluation.put("costScore", result.getCostScore());
        evaluation.put("efficiencyScore", result.getEfficiencyScore());
        evaluation.put("satisfactionScore", result.getSatisfactionScore());

        // 统计信息
        evaluation.put("totalOvertimeShifts", result.getTotalOvertimeShifts());
        evaluation.put("totalOvertimeCost", result.getTotalOvertimeCost());
        evaluation.put("avgWorkDaysPerEmployee", result.getAvgWorkDaysPerEmployee());
        evaluation.put("workDaysStandardDeviation", result.getWorkDaysStandardDeviation());

        // 违规统计
        evaluation.put("consecutiveWorkViolations", result.getConsecutiveWorkViolations());
        evaluation.put("restDaysViolations", result.getRestDaysViolations());
        evaluation.put("dailyStaffViolations", result.getDailyStaffViolations());
        evaluation.put("totalViolations", result.getTotalViolations());

        // 性价比
        evaluation.put("costEffectivenessScore", result.getCostEffectivenessScore());

        // 摘要
        evaluation.put("summary", result.getSummary());

        return evaluation;
    }

    @Override
    public Map<String, OptimizationResult> compareAlgorithms(OptimizationConfig config) {
        log.info("[优化服务] 对比不同算法: {}员工 × {}天",
                config.getEmployeeCount(), config.getPeriodDays());

        Map<String, OptimizationResult> comparison = new HashMap<>();

        // 遗传算法
        OptimizationResult gaResult = optimizeWithGeneticAlgorithm(config);
        comparison.put("遗传算法", gaResult);

        // 模拟退火
        OptimizationResult saResult = optimizeWithSimulatedAnnealing(config);
        comparison.put("模拟退火", saResult);

        // 混合算法
        OptimizationResult hybridResult = optimizeWithHybridAlgorithm(config);
        comparison.put("混合算法", hybridResult);

        log.info("[优化服务] 算法对比完成: GA={}, SA={}, Hybrid={}",
                gaResult.getBestFitness(), saResult.getBestFitness(), hybridResult.getBestFitness());

        return comparison;
    }

    @Override
    public Map<String, Object> validateConfig(OptimizationConfig config) {
        log.info("[优化服务] 验证优化配置");

        Map<String, Object> validation = new HashMap<>();

        try {
            // 验证配置
            config.validate();

            validation.put("valid", true);
            validation.put("message", "配置验证通过");
            validation.put("problemSize", config.getEmployeeCount() * config.getPeriodDays());
            validation.put("algorithmRecommendation",
                    optimizationAlgorithmFactory.getAlgorithmRecommendation(config));

        } catch (Exception e) {
            validation.put("valid", false);
            validation.put("message", "配置验证失败: " + e.getMessage());
            validation.put("error", e.getMessage());
        }

        return validation;
    }

    @Override
    public Map<String, Object> getAlgorithmPerformance(OptimizationConfig config, int runCount) {
        log.info("[优化服务] 获取算法性能统计: runCount={}", runCount);

        Map<String, Object> performance = new HashMap<>();

        // TODO: 实现性能统计功能
        // 1. 多次运行算法
        // 2. 统计平均适应度、最好适应度、最差适应度
        // 3. 统计平均执行时间
        // 4. 统计收敛率

        performance.put("message", "性能统计功能待实现");

        return performance;
    }
}
