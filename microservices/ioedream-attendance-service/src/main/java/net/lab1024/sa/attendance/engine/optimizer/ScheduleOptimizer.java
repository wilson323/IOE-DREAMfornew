package net.lab1024.sa.attendance.engine.optimizer;

import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;
import net.lab1024.sa.attendance.engine.optimizer.model.*;

import java.util.List;
import java.util.Map;

/**
 * 排班优化器接口
 * <p>
 * 负责对排班方案进行优化调整
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface ScheduleOptimizer {

    /**
     * 优化排班方案
     *
     * @param scheduleRecords 原始排班记录
     * @param scheduleData     排班数据
     * @param optimizationGoals 优化目标
     * @return 优化结果
     */
    OptimizationResult optimizeSchedule(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData, List<OptimizationGoal> optimizationGoals);

    /**
     * 优化工作负载均衡
     *
     * @param scheduleRecords 排班记录
     * @param scheduleData     排班数据
     * @return 工作负载优化结果
     */
    WorkloadOptimizationResult optimizeWorkloadBalance(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData);

    /**
     * 优化成本
     *
     * @param scheduleRecords 排班记录
     * @param scheduleData     排班数据
     * @param budgetConstraint 预算约束
     * @return 成本优化结果
     */
    CostOptimizationResult optimizeCost(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData, BudgetConstraint budgetConstraint);

    /**
     * 优化员工满意度
     *
     * @param scheduleRecords 排班记录
     * @param scheduleData     排班数据
     * @param employeePreferences 员工偏好
     * @return 满意度优化结果
     */
    SatisfactionOptimizationResult optimizeSatisfaction(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData, Map<Long, EmployeePreference> employeePreferences);

    /**
     * 优化班次覆盖率
     *
     * @param scheduleRecords 排班记录
     * @param scheduleData     排班数据
     * @param coverageTargets 覆盖率目标
     * @return 覆盖率优化结果
     */
    CoverageOptimizationResult optimizeCoverage(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData, CoverageTargets coverageTargets);

    /**
     * 多目标优化
     *
     * @param scheduleRecords 排班记录
     * @param scheduleData     排班数据
     * @param multiObjectiveGoals 多目标优化配置
     * @return 多目标优化结果
     */
    MultiObjectiveOptimizationResult optimizeMultiObjective(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData, MultiObjectiveGoals multiObjectiveGoals);

    /**
     * 局部优化
     *
     * @param scheduleRecords 排班记录
     * @param scheduleData     排班数据
     * @param optimizationScope 优化范围
     * @return 局部优化结果
     */
    LocalOptimizationResult optimizeLocal(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData, OptimizationScope optimizationScope);

    /**
     * 评估优化效果
     *
     * @param originalSchedule 原始排班
     * @param optimizedSchedule 优化后排班
     * @param scheduleData      排班数据
     * @return 评估结果
     */
    OptimizationEvaluation evaluateOptimization(List<ScheduleRecord> originalSchedule, List<ScheduleRecord> optimizedSchedule, ScheduleData scheduleData);

    /**
     * 获取优化统计信息
     *
     * @param optimizationResults 优化结果列表
     * @return 优化统计信息
     */
    OptimizationStatistics getOptimizationStatistics(List<OptimizationResult> optimizationResults);

    /**
     * 验证优化结果
     *
     * @param optimizationResult 优化结果
     * @param scheduleData        排班数据
     * @return 验证结果
     */
    boolean validateOptimizationResult(OptimizationResult optimizationResult, ScheduleData scheduleData);

    /**
     * 获取优化建议
     *
     * @param scheduleRecords 排班记录
     * @param scheduleData     排班数据
     * @return 优化建议列表
     */
    List<OptimizationSuggestion> getOptimizationSuggestions(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData);
}
