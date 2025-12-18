package net.lab1024.sa.attendance.engine.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.ScheduleEngine;
import net.lab1024.sa.attendance.engine.model.*;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithmFactory;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithm;
import net.lab1024.sa.attendance.engine.conflict.ConflictDetector;
import net.lab1024.sa.attendance.engine.conflict.ConflictResolver;
import net.lab1024.sa.attendance.engine.optimizer.ScheduleOptimizer;
import net.lab1024.sa.attendance.engine.prediction.SchedulePredictor;
import net.lab1024.sa.attendance.engine.statistics.ScheduleStatisticsCalculator;

import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Map;

/**
 * 智能排班引擎实现类
 * <p>
 * 智能排班引擎的核心实现，支持多种排班算法和策略
 * 严格遵循CLAUDE.md全局架构规范，不使用Spring注解
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class ScheduleEngineImpl implements ScheduleEngine {

    private final ScheduleAlgorithmFactory algorithmFactory;
    private final ConflictDetector conflictDetector;
    private final ConflictResolver conflictResolver;
    private final ScheduleOptimizer scheduleOptimizer;
    private final SchedulePredictor schedulePredictor;
    private final ScheduleStatisticsCalculator statisticsCalculator;

    /**
     * 构造函数注入依赖
     */
    public ScheduleEngineImpl(
            ScheduleAlgorithmFactory algorithmFactory,
            ConflictDetector conflictDetector,
            ConflictResolver conflictResolver,
            ScheduleOptimizer scheduleOptimizer,
            SchedulePredictor schedulePredictor,
            ScheduleStatisticsCalculator statisticsCalculator) {
        this.algorithmFactory = algorithmFactory;
        this.conflictDetector = conflictDetector;
        this.conflictResolver = conflictResolver;
        this.scheduleOptimizer = scheduleOptimizer;
        this.schedulePredictor = schedulePredictor;
        this.statisticsCalculator = statisticsCalculator;
    }

    @Override
    public ScheduleResult executeIntelligentSchedule(ScheduleRequest request) {
        log.info("[排班引擎] 开始执行智能排班, 计划ID: {}, 算法: {}",
                request.getPlanId(), request.getScheduleAlgorithm());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 1. 验证请求参数
            validateScheduleRequest(request);

            // 2. 获取排班算法
            ScheduleAlgorithm algorithm = algorithmFactory.getAlgorithm(request.getScheduleAlgorithm());

            // 3. 执行排班计算
            ScheduleData scheduleData = prepareScheduleData(request);
            ScheduleResult result = algorithm.generateSchedule(scheduleData);

            // 4. 检测和解决冲突
            ConflictDetectionResult conflictResult = validateScheduleConflicts(scheduleData);
            if (conflictResult != null && conflictResult.getConflicts() != null && !conflictResult.getConflicts().isEmpty()) {
                ConflictResolution resolution = resolveScheduleConflicts(
                        conflictResult.getConflicts(),
                        request.getConflictResolution()
                );
                result = applyConflictResolution(result, resolution);
            }

            // 5. 优化排班结果
            if (request.getOptimizationTarget() != null) {
                OptimizedSchedule optimizedSchedule = optimizeSchedule(scheduleData, request.getOptimizationTarget());
                result = applyOptimization(result, optimizedSchedule);
            }

            // 6. 生成统计信息
            ScheduleStatistics statistics = calculateScheduleStatistics(result.getPlanId());
            result.setStatistics(statistics);

            // 7. 设置执行时间和算法
            stopWatch.stop();
            result.setExecutionTime(stopWatch.getLastTaskTimeMillis());
            result.setAlgorithmUsed(request.getScheduleAlgorithm());

            // 8. 设置质量评分
            Double qualityScore = calculateQualityScore(result);
            result.setQualityScore(qualityScore);

            // 9. 检查是否需要人工审核
            Boolean needsReview = checkNeedsReview(result);
            result.setNeedsReview(needsReview);

            // 10. 生成推荐建议
            List<String> recommendations = generateRecommendations(result);
            result.setRecommendations(recommendations);

            log.info("[排班引擎] 智能排班完成, 结果状态: {}, 耗时: {}ms, 质量评分: {}",
                    result.getStatus(), result.getExecutionTime(), result.getQualityScore());

            return result;

        } catch (Exception e) {
            log.error("[排班引擎] 智能排班失败", e);

            return ScheduleResult.builder()
                    .status("FAILED")
                    .message("排班失败: " + e.getMessage())
                    .executionTime(stopWatch.getLastTaskTimeMillis())
                    .needsReview(true)
                    .build();
        }
    }

    @Override
    public SchedulePlan generateSchedulePlan(Long planId, LocalDate startDate, LocalDate endDate) {
        log.info("[排班引擎] 生成排班计划, 计划ID: {}, 开始日期: {}, 结束日期: {}",
                planId, startDate, endDate);

        try {
            // 实现排班计划生成逻辑
            SchedulePlan plan = new SchedulePlan();
            plan.setPlanId(planId);
            plan.setStartDate(startDate);
            plan.setEndDate(endDate);

            // TODO: 实现具体的排班计划生成逻辑

            return plan;

        } catch (Exception e) {
            log.error("[排班引擎] 生成排班计划失败", e);
            throw new RuntimeException("生成排班计划失败", e);
        }
    }

    @Override
    public ConflictDetectionResult validateScheduleConflicts(ScheduleData scheduleData) {
        log.debug("[排班引擎] 验证排班冲突");
        return conflictDetector.detectConflicts(scheduleData);
    }

    @Override
    public ConflictResolution resolveScheduleConflicts(List<ScheduleConflict> conflicts, String resolutionStrategy) {
        log.info("[排班引擎] 解决排班冲突, 冲突数量: {}, 解决策略: {}",
                conflicts.size(), resolutionStrategy);
        return conflictResolver.resolveConflicts(conflicts, resolutionStrategy);
    }

    @Override
    public OptimizedSchedule optimizeSchedule(ScheduleData scheduleData, String optimizationTarget) {
        log.info("[排班引擎] 优化排班结果, 优化目标: {}", optimizationTarget);
        return scheduleOptimizer.optimize(scheduleData, optimizationTarget);
    }

    @Override
    public SchedulePrediction predictScheduleEffect(ScheduleData scheduleData) {
        log.debug("[排班引擎] 预测排班效果");
        return schedulePredictor.predict(scheduleData);
    }

    @Override
    public ScheduleStatistics getScheduleStatistics(Long planId) {
        log.debug("[排班引擎] 获取排班统计, 计划ID: {}", planId);
        return statisticsCalculator.calculate(planId);
    }

    /**
     * 验证排班请求参数
     */
    private void validateScheduleRequest(ScheduleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("排班请求不能为空");
        }

        if (request.getPlanId() == null) {
            throw new IllegalArgumentException("排班计划ID不能为空");
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("排班开始日期和结束日期不能为空");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("排班开始日期不能晚于结束日期");
        }

        if (request.getScheduleAlgorithm() == null) {
            throw new IllegalArgumentException("排班算法不能为空");
        }
    }

    /**
     * 准备排班数据
     * <p>
     * 注意：根据架构设计，Engine层是纯业务逻辑层，不直接访问数据库
     * 数据准备应该由调用方（Controller/Service）完成，或通过注入的Service获取
     * 当前实现返回基础结构，实际数据应由调用方通过ScheduleService准备
     * </p>
     *
     * @param request 排班请求
     * @return 排班数据（基础结构，需要调用方补充数据）
     */
    private ScheduleData prepareScheduleData(ScheduleRequest request) {
        log.debug("[排班引擎] 准备排班数据, planId={}, startDate={}, endDate={}",
                request.getPlanId(), request.getStartDate(), request.getEndDate());

        ScheduleData scheduleData = ScheduleData.builder()
                .planId(request.getPlanId())
                .planName(request.getPlanName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .timeRange((int) java.time.temporal.ChronoUnit.DAYS.between(
                        request.getStartDate(), request.getEndDate()) + 1)
                .build();

        // 注意：员工、班次、历史记录等数据应由调用方通过ScheduleService准备
        // 这里只创建基础结构，避免Engine层直接访问数据库
        log.warn("[排班引擎] 排班数据准备：基础结构已创建，但员工、班次等数据需要由调用方补充");

        return scheduleData;
    }

    /**
     * 应用冲突解决方案
     * <p>
     * 根据冲突解决方案更新排班结果
     * </p>
     *
     * @param result 排班结果
     * @param resolution 冲突解决方案
     * @return 更新后的排班结果
     */
    private ScheduleResult applyConflictResolution(ScheduleResult result, ConflictResolution resolution) {
        log.info("[排班引擎] 应用冲突解决方案, resolutionId={}, resolvedCount={}, unresolvedCount={}",
                resolution.getResolutionId(), resolution.getResolvedConflictCount(),
                resolution.getUnresolvedConflictCount());

        if (resolution == null || resolution.getModifiedRecords() == null || resolution.getModifiedRecords().isEmpty()) {
            log.warn("[排班引擎] 冲突解决方案为空或没有修改记录，跳过应用");
            return result;
        }

        // 1. 更新冲突状态
        if (result.getConflicts() != null) {
            // 移除已解决的冲突
            List<ScheduleResult.ScheduleConflict> remainingConflicts = result.getConflicts().stream()
                    .filter(conflict -> {
                        // 检查该冲突是否在解决方案中已解决
                        return resolution.getModifiedRecords().stream()
                                .noneMatch(mod -> mod.getModificationReason() != null &&
                                        mod.getModificationReason().contains(conflict.getConflictId()));
                    })
                    .collect(java.util.stream.Collectors.toList());
            result.setConflicts(remainingConflicts);
        }

        // 2. 应用修改的排班记录
        if (resolution.getModifiedRecords() != null) {
            for (ConflictResolution.ScheduleRecordModification modification : resolution.getModifiedRecords()) {
                if (modification.getModifiedRecord() != null && result.getScheduleRecords() != null) {
                    // 查找并更新对应的排班记录
                    result.getScheduleRecords().stream()
                            .filter(record -> record.getRecordId() != null &&
                                    record.getRecordId().equals(modification.getRecordId()))
                            .findFirst()
                            .ifPresent(record -> {
                                // 更新记录信息（根据修改类型）
                                log.debug("[排班引擎] 应用排班记录修改, recordId={}, type={}",
                                        modification.getRecordId(), modification.getModificationType());
                                // 注意：这里只更新结果对象，实际数据库更新应由Service层完成
                            });
                }
            }
        }

        // 3. 更新结果状态
        if (resolution.getUnresolvedConflictCount() != null && resolution.getUnresolvedConflictCount() > 0) {
            result.setStatus("PARTIAL"); // 部分成功
            result.setMessage("排班完成，但存在 " + resolution.getUnresolvedConflictCount() + " 个未解决的冲突");
        } else {
            result.setStatus("SUCCESS");
            result.setMessage("排班完成，所有冲突已解决");
        }

        log.info("[排班引擎] 冲突解决方案应用完成, resolutionRate={}%",
                resolution.getResolutionRate() != null ? resolution.getResolutionRate() * 100 : 0);

        return result;
    }

    /**
     * 应用优化结果
     * <p>
     * 根据优化结果更新排班记录
     * </p>
     *
     * @param result 排班结果
     * @param optimizedSchedule 优化后的排班
     * @return 更新后的排班结果
     */
    private ScheduleResult applyOptimization(ScheduleResult result, OptimizedSchedule optimizedSchedule) {
        log.info("[排班引擎] 应用优化结果, optimizationId={}, improvementRate={}%",
                optimizedSchedule.getOptimizationId(),
                optimizedSchedule.getImprovementRate() != null ? optimizedSchedule.getImprovementRate() * 100 : 0);

        if (optimizedSchedule == null || optimizedSchedule.getOptimizedRecords() == null ||
                optimizedSchedule.getOptimizedRecords().isEmpty()) {
            log.warn("[排班引擎] 优化结果为空，跳过应用");
            return result;
        }

        // 1. 更新排班记录
        if (result.getScheduleRecords() != null && optimizedSchedule.getOptimizedRecords() != null) {
            // 用优化后的记录替换原记录
            result.setScheduleRecords(optimizedSchedule.getOptimizedRecords());
            log.debug("[排班引擎] 已应用优化后的排班记录, count={}", optimizedSchedule.getOptimizedRecords().size());
        }

        // 2. 更新优化指标
        if (optimizedSchedule.getOptimizationMetrics() != null) {
            if (result.getOptimizationMetrics() == null) {
                result.setOptimizationMetrics(new java.util.HashMap<>());
            }
            result.getOptimizationMetrics().putAll(optimizedSchedule.getOptimizationMetrics());
        }

        // 3. 更新质量评分
        if (optimizedSchedule.getOptimizedScore() != null) {
            result.setQualityScore(optimizedSchedule.getOptimizedScore());
        }

        // 4. 添加优化建议到推荐列表
        if (optimizedSchedule.getOptimizationSuggestions() != null && !optimizedSchedule.getOptimizationSuggestions().isEmpty()) {
            if (result.getRecommendations() == null) {
                result.setRecommendations(new java.util.ArrayList<>());
            }
            result.getRecommendations().addAll(optimizedSchedule.getOptimizationSuggestions());
        }

        log.info("[排班引擎] 优化结果应用完成, originalScore={}, optimizedScore={}, improvementRate={}%",
                optimizedSchedule.getOriginalScore(),
                optimizedSchedule.getOptimizedScore(),
                optimizedSchedule.getImprovementRate() != null ? optimizedSchedule.getImprovementRate() * 100 : 0);

        return result;
    }

    /**
     * 计算排班统计信息
     */
    private ScheduleStatistics calculateScheduleStatistics(Long planId) {
        return statisticsCalculator.calculate(planId);
    }

    /**
     * 计算质量评分
     * <p>
     * 综合评估排班结果的质量，包括覆盖率、均衡度、冲突数等因素
     * </p>
     *
     * @param result 排班结果
     * @return 质量评分（0-100）
     */
    private Double calculateQualityScore(ScheduleResult result) {
        if (result == null) {
            return 0.0;
        }

        log.debug("[排班引擎] 计算质量评分");

        double score = 100.0; // 初始满分

        // 1. 冲突数量影响（每个冲突扣5分，严重冲突扣10分）
        if (result.getConflicts() != null && !result.getConflicts().isEmpty()) {
            int conflictPenalty = 0;
            for (ScheduleResult.ScheduleConflict conflict : result.getConflicts()) {
                if ("CRITICAL".equals(conflict.getSeverity())) {
                    conflictPenalty += 10;
                } else if ("HIGH".equals(conflict.getSeverity())) {
                    conflictPenalty += 7;
                } else if ("MEDIUM".equals(conflict.getSeverity())) {
                    conflictPenalty += 5;
                } else {
                    conflictPenalty += 2;
                }
            }
            score -= Math.min(conflictPenalty, 50); // 最多扣50分
        }

        // 2. 排班覆盖率影响（如果统计信息中有覆盖率）
        if (result.getStatistics() != null && result.getStatistics().getShiftCoverage() != null) {
            double coverage = result.getStatistics().getShiftCoverage();
            if (coverage < 0.8) {
                score -= (0.8 - coverage) * 30; // 覆盖率低于80%时扣分
            }
        }

        // 3. 工作负载均衡度影响
        if (result.getStatistics() != null && result.getStatistics().getWorkloadBalance() != null) {
            double balance = result.getStatistics().getWorkloadBalance();
            if (balance < 0.7) {
                score -= (0.7 - balance) * 20; // 均衡度低于70%时扣分
            }
        }

        // 4. 约束满足率影响
        if (result.getStatistics() != null && result.getStatistics().getConstraintSatisfaction() != null) {
            double satisfaction = result.getStatistics().getConstraintSatisfaction();
            if (satisfaction < 0.9) {
                score -= (0.9 - satisfaction) * 25; // 约束满足率低于90%时扣分
            }
        }

        // 5. 确保评分在0-100范围内
        score = Math.max(0.0, Math.min(100.0, score));

        log.debug("[排班引擎] 质量评分计算完成, score={}", score);

        return score;
    }

    /**
     * 检查是否需要人工审核
     */
    private Boolean checkNeedsReview(ScheduleResult result) {
        // 如果存在严重冲突，需要人工审核
        if (result.getConflicts() != null &&
            result.getConflicts().stream().anyMatch(conflict -> "CRITICAL".equals(conflict.getSeverity()))) {
            return true;
        }

        // 如果质量评分低于阈值，需要人工审核
        if (result.getQualityScore() != null && result.getQualityScore() < 70.0) {
            return true;
        }

        return false;
    }

    /**
     * 生成推荐建议
     */
    private List<String> generateRecommendations(ScheduleResult result) {
        // TODO: 实现推荐建议生成逻辑
        return List.of("建议定期审查排班结果", "考虑员工偏好优化排班");
    }
}