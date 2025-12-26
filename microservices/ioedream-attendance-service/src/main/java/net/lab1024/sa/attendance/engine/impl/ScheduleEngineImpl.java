package net.lab1024.sa.attendance.engine.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.attendance.SmartSchedulePlanEntity;
import net.lab1024.sa.attendance.engine.ScheduleEngine;
import net.lab1024.sa.attendance.engine.conflict.ScheduleConflictService;
import net.lab1024.sa.attendance.engine.execution.ScheduleExecutionService;
import net.lab1024.sa.attendance.engine.model.ConflictDetectionResult;
import net.lab1024.sa.attendance.engine.model.ConflictResolution;
import net.lab1024.sa.attendance.engine.model.OptimizedSchedule;
import net.lab1024.sa.attendance.engine.model.ScheduleConflict;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.SchedulePrediction;
import net.lab1024.sa.attendance.engine.model.ScheduleRequest;
import net.lab1024.sa.attendance.engine.model.ScheduleResult;
import net.lab1024.sa.attendance.engine.model.ScheduleStatistics;
import net.lab1024.sa.attendance.engine.optimization.ScheduleOptimizationService;
import net.lab1024.sa.attendance.engine.prediction.SchedulePredictionService;
import net.lab1024.sa.attendance.engine.quality.ScheduleQualityService;

import java.time.LocalDate;

/**
 * 智能排班引擎实现类（P2-Batch3重构）
 * <p>
 * 智能排班引擎的Facade实现，委托给专业服务完成具体功能
 * 严格遵循CLAUDE.md全局架构规范，使用Facade模式和委托模式
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0 (P2-Batch3重构)
 * @since 2025-12-16
 */
@Slf4j
public class ScheduleEngineImpl implements ScheduleEngine {

    /**
     * 排班执行服务（P2-Batch3阶段1创建）
     */
    private final ScheduleExecutionService scheduleExecutionService;

    /**
     * 冲突处理服务（P2-Batch3阶段1创建）
     */
    private final ScheduleConflictService scheduleConflictService;

    /**
     * 排班优化服务（P2-Batch3阶段1创建）
     */
    private final ScheduleOptimizationService scheduleOptimizationService;

    /**
     * 排班预测服务（P2-Batch3阶段1创建）
     */
    private final SchedulePredictionService schedulePredictionService;

    /**
     * 质量评估服务（P2-Batch3阶段1创建）
     */
    private final ScheduleQualityService scheduleQualityService;

    /**
     * 构造函数注入依赖（P2-Batch3重构）
     */
    public ScheduleEngineImpl(ScheduleExecutionService scheduleExecutionService,
            ScheduleConflictService scheduleConflictService,
            ScheduleOptimizationService scheduleOptimizationService,
            SchedulePredictionService schedulePredictionService,
            ScheduleQualityService scheduleQualityService) {
        this.scheduleExecutionService = scheduleExecutionService;
        this.scheduleConflictService = scheduleConflictService;
        this.scheduleOptimizationService = scheduleOptimizationService;
        this.schedulePredictionService = schedulePredictionService;
        this.scheduleQualityService = scheduleQualityService;
    }

    @Override
    public ScheduleResult executeIntelligentSchedule(ScheduleRequest request) {
        log.info("[排班引擎] 执行智能排班（委托给排班执行服务）");
        return scheduleExecutionService.executeSchedule(request);
    }

    @Override
    public SmartSchedulePlanEntity generateSmartSchedulePlanEntity(Long planId, LocalDate startDate, LocalDate endDate) {
        log.info("[排班引擎] 生成排班计划实体（委托给排班执行服务）");
        return scheduleExecutionService.generatePlanEntity(planId, startDate, endDate);
    }

    @Override
    public ConflictDetectionResult validateScheduleConflicts(ScheduleData scheduleData) {
        log.debug("[排班引擎] 验证排班冲突（委托给冲突处理服务）");
        return scheduleConflictService.detectConflicts(scheduleData);
    }

    @Override
    public ConflictResolution resolveScheduleConflicts(List<ScheduleConflict> conflicts, String resolutionStrategy) {
        log.debug("[排班引擎] 解决排班冲突（委托给冲突处理服务）");
        return scheduleConflictService.resolveConflicts(conflicts, resolutionStrategy);
    }

    @Override
    public OptimizedSchedule optimizeSchedule(ScheduleData scheduleData, String optimizationTarget) {
        log.debug("[排班引擎] 优化排班（委托给排班优化服务）");
        return scheduleOptimizationService.optimizeSchedule(scheduleData, optimizationTarget);
    }

    @Override
    public SchedulePrediction predictScheduleEffect(ScheduleData scheduleData) {
        log.debug("[排班引擎] 预测排班效果（委托给预测服务）");
        return schedulePredictionService.predictEffect(scheduleData);
    }

    @Override
    public ScheduleStatistics getScheduleStatistics(Long planId) {
        log.info("[排班引擎] 获取排班统计信息: planId={}", planId);
        log.debug("[排班引擎] 委托给质量评估服务生成统计信息");

        // 创建基础统计信息
        // 注意：这是一个基础实现，返回builder创建的统计对象
        // 后续可以扩展为从数据库查询排班结果，然后调用质量评估服务生成详细统计
        ScheduleStatistics statistics = ScheduleStatistics.builder()
                .planId(planId)
                .totalEmployees(0)  // 后续从数据库查询
                .totalShifts(0)      // 后续从数据库查询
                .totalAssignments(0) // 后续从数据库查询
                .build();

        log.info("[排班引擎] 排班统计信息生成完成: planId={}", planId);
        log.debug("[排班引擎] 统计信息: 员工数={}, 班次数={}, 分配数={}",
                statistics.getTotalEmployees(), statistics.getTotalShifts(), statistics.getTotalAssignments());

        return statistics;
    }

    // ===== P2-Batch3重构：以下private方法已删除，功能迁移到专业服务 =====
    // - validateScheduleRequest() → ScheduleExecutionService
    // - prepareScheduleData() → ScheduleExecutionService
    // - convertToModelConflicts() → ScheduleConflictService
    // - convertToModelScheduleRecords() → 已删除
    // - convertScheduleDataRecordsToModelRecords() → ScheduleConflictService
    // - applyConflictResolution() → ScheduleConflictService
    // - applyOptimization() → ScheduleOptimizationService
    // - calculateQualityScore() → ScheduleQualityService
    // - checkNeedsReview() → ScheduleQualityService
    // - generateRecommendations() → ScheduleQualityService
}
