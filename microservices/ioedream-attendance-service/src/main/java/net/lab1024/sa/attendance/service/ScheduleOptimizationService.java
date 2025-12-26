package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationResult;

import java.util.Map;

/**
 * 排班优化服务接口
 * <p>
 * 核心功能：
 * - 执行优化算法
 * - 生成排班方案
 * - 评估优化结果
 * - 对比不同算法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ScheduleOptimizationService {

    /**
     * 执行遗传算法优化
     *
     * @param config 优化配置
     * @return 优化结果
     */
    OptimizationResult optimizeWithGeneticAlgorithm(OptimizationConfig config);

    /**
     * 执行模拟退火优化
     *
     * @param config 优化配置
     * @return 优化结果
     */
    OptimizationResult optimizeWithSimulatedAnnealing(OptimizationConfig config);

    /**
     * 执行混合算法优化
     *
     * @param config 优化配置
     * @return 优化结果
     */
    OptimizationResult optimizeWithHybridAlgorithm(OptimizationConfig config);

    /**
     * 自动选择最优算法并执行优化
     *
     * @param config 优化配置
     * @return 优化结果
     */
    OptimizationResult optimizeAuto(OptimizationConfig config);

    /**
     * 评估优化结果
     *
     * @param result 优化结果
     * @return 评估报告
     */
    Map<String, Object> evaluateResult(OptimizationResult result);

    /**
     * 对比不同算法的优化结果
     *
     * @param config 优化配置
     * @return 算法对比结果
     */
    Map<String, OptimizationResult> compareAlgorithms(OptimizationConfig config);

    /**
     * 验证优化配置
     *
     * @param config 优化配置
     * @return 验证结果
     */
    Map<String, Object> validateConfig(OptimizationConfig config);

    /**
     * 获取算法性能统计
     *
     * @param config 优化配置
     * @param runCount 运行次数
     * @return 性能统计数据
     */
    Map<String, Object> getAlgorithmPerformance(OptimizationConfig config, int runCount);
}
