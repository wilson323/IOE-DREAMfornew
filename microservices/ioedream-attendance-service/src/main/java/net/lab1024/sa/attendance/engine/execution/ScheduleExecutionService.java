package net.lab1024.sa.attendance.engine.execution;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.ScheduleAlgorithm;
import net.lab1024.sa.attendance.engine.ScheduleEngine;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithmFactory;
import net.lab1024.sa.attendance.engine.conflict.ConflictDetector;
import net.lab1024.sa.attendance.engine.conflict.ConflictResolver;
import net.lab1024.sa.attendance.engine.model.ConflictDetectionResult;
import net.lab1024.sa.attendance.engine.model.ConflictResolution;
import net.lab1024.sa.attendance.engine.model.OptimizedSchedule;
import net.lab1024.sa.attendance.engine.model.ScheduleConflict;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleRequest;
import net.lab1024.sa.attendance.engine.model.ScheduleResult;
import net.lab1024.sa.attendance.engine.optimizer.ScheduleOptimizer;
import net.lab1024.sa.attendance.entity.SmartSchedulePlanEntity;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排班执行服务（P2-Batch3阶段1创建）
 * <p>
 * 负责智能排班的核心执行逻辑，包括请求验证、数据准备、排班计算、冲突处理、优化、
 * 统计信息生成等完整流程
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class ScheduleExecutionService {

    private final ScheduleAlgorithmFactory algorithmFactory;
    private final ConflictDetector conflictDetector;
    private final ConflictResolver conflictResolver;
    private final ScheduleOptimizer scheduleOptimizer;

    /**
     * 构造函数注入依赖
     */
    public ScheduleExecutionService(ScheduleAlgorithmFactory algorithmFactory,
            ConflictDetector conflictDetector,
            ConflictResolver conflictResolver,
            ScheduleOptimizer scheduleOptimizer) {
        this.algorithmFactory = algorithmFactory;
        this.conflictDetector = conflictDetector;
        this.conflictResolver = conflictResolver;
        this.scheduleOptimizer = scheduleOptimizer;
    }

    /**
     * 执行智能排班
     * <p>
     * 这是排班执行的核心方法，完成从请求验证到结果生成的完整流程
     * </p>
     *
     * @param request 排班请求
     * @return 排班结果
     */
    public ScheduleResult executeSchedule(ScheduleRequest request) {
        log.info("[排班执行服务] 开始执行智能排班, 计划ID: {}, 算法: {}",
                request.getPlanId(), request.getScheduleAlgorithm());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 1. 验证请求参数
            validateRequest(request);

            // 2. 获取排班算法
            ScheduleAlgorithm algorithm = algorithmFactory.getAlgorithm(request.getScheduleAlgorithm());

            // 3. 执行排班计算
            ScheduleData scheduleData = prepareData(request);
            ScheduleResult result = algorithm.generateSchedule(scheduleData);

            // 4. 检测和解决冲突
            ConflictDetectionResult conflictResult = validateConflicts(scheduleData);
            if (conflictResult != null && conflictResult.getConflicts() != null
                    && !conflictResult.getConflicts().isEmpty()) {
                // 转换为model.ScheduleConflict
                List<ScheduleConflict> scheduleConflicts = conflictResult.getConflicts().stream().map(conf -> {
                    ScheduleConflict scheduleConflict = new ScheduleConflict();
                    scheduleConflict.setConflictId(conf.getConflictId());
                    scheduleConflict.setConflictType(conf.getConflictType());
                    scheduleConflict.setDescription(conf.getDescription());
                    scheduleConflict.setSeverity(conf.getSeverity());
                    scheduleConflict.setAffectedUsers(conf.getAffectedEmployees());
                    scheduleConflict.setConflictDate(conf.getConflictDate());
                    scheduleConflict.setTimeSlots(conf.getTimeSlots());
                    return scheduleConflict;
                }).collect(Collectors.toList());

                ConflictResolution resolution = resolveConflicts(scheduleConflicts,
                        request.getConflictResolution());
                result = applyConflictResolution(result, resolution);
            }

            // 5. 优化排班结果
            if (request.getOptimizationTarget() != null) {
                OptimizedSchedule optimizedSchedule = optimize(scheduleData, request.getOptimizationTarget());
                result = applyOptimization(result, optimizedSchedule);
            }

            // 6. 生成统计信息
            if (result.getStatistics() == null) {
                Map<String, Object> statisticsMap = generateStatistics(result);
                result.setStatistics(statisticsMap);
            }

            // 7. 设置执行时间和算法
            stopWatch.stop();
            result.setExecutionTime(stopWatch.getTotalTimeMillis());
            result.setAlgorithmUsed(request.getScheduleAlgorithm());

            log.info("[排班执行服务] 智能排班完成, 结果状态: {}, 耗时: {}ms",
                    result.getStatus(), result.getExecutionTime());

            return result;

        } catch (Exception e) {
            log.error("[排班执行服务] 智能排班失败", e);

            stopWatch.stop();
            return ScheduleResult.builder()
                    .status("FAILED")
                    .message("排班失败: " + e.getMessage())
                    .executionTime(stopWatch.getTotalTimeMillis())
                    .needsReview(true)
                    .build();
        }
    }

    /**
     * 生成排班计划实体
     *
     * @param planId    计划ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 排班计划实体
     */
    public SmartSchedulePlanEntity generatePlanEntity(Long planId, LocalDate startDate, LocalDate endDate) {
        log.info("[排班执行服务] 生成排班计划实体, 计划ID: {}, 开始日期: {}, 结束日期: {}",
                planId, startDate, endDate);

        try {
            SmartSchedulePlanEntity plan = new SmartSchedulePlanEntity();
            plan.setPlanId(planId);
            plan.setStartDate(startDate);
            plan.setEndDate(endDate);

            // TODO: 实现具体的排班计划生成逻辑

            return plan;

        } catch (Exception e) {
            log.error("[排班执行服务] 生成排班计划实体失败", e);
            throw new RuntimeException("生成排班计划实体失败", e);
        }
    }

    /**
     * 验证排班请求
     *
     * @param request 排班请求
     */
    private void validateRequest(ScheduleRequest request) {
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
     * 根据架构设计，Engine层是纯业务逻辑层，不直接访问数据库
     * 数据准备应该由调用方（Controller/Service）完成，或通过注入的Service获取
     * 当前实现返回基础结构，实际数据应由调用方通过ScheduleService准备
     * </p>
     *
     * @param request 排班请求
     * @return 排班数据（基础结构，需要调用方补充数据）
     */
    private ScheduleData prepareData(ScheduleRequest request) {
        log.debug("[排班执行服务] 准备排班数据, planId={}, startDate={}, endDate={}",
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
        log.warn("[排班执行服务] 排班数据准备：基础结构已创建，但员工、班次等数据需要由调用方补充");

        return scheduleData;
    }

    /**
     * 验证排班冲突
     *
     * @param scheduleData 排班数据
     * @return 冲突检测结果
     */
    private ConflictDetectionResult validateConflicts(ScheduleData scheduleData) {
        log.debug("[排班执行服务] 验证排班冲突");

        // 从ScheduleData中提取排班记录列表
        List<net.lab1024.sa.attendance.engine.model.ScheduleRecord> scheduleRecords =
                convertToModelRecords(scheduleData.getHistoryRecords() != null
                        ? scheduleData.getHistoryRecords()
                        : new ArrayList<>());

        // 调用ConflictDetector
        net.lab1024.sa.attendance.engine.conflict.ConflictDetectionResult conflictResult =
                conflictDetector.detectConflicts(scheduleRecords, scheduleData);

        // 转换为model包中的ConflictDetectionResult
        return ConflictDetectionResult.builder()
                .detectionId(conflictResult.getDetectionId())
                .hasConflicts(conflictResult.getHasConflicts())
                .totalConflicts(conflictResult.getTotalConflicts())
                .skillConflicts(0) // 暂时设为0，后续完善
                .workHourConflicts(0) // 暂时设为0，后续完善
                .capacityConflicts(0) // 暂时设为0，后续完善
                .otherConflicts(0) // 暂时设为0，后续完善
                .conflicts(convertToModelConflicts(conflictResult.getConflicts()))
                .build();
    }

    /**
     * 解决排班冲突
     *
     * @param conflicts           冲突列表
     * @param resolutionStrategy 解决策略
     * @return 冲突解决方案
     */
    private ConflictResolution resolveConflicts(List<ScheduleConflict> conflicts,
            String resolutionStrategy) {
        log.debug("[排班执行服务] 解决排班冲突, 冲突数量: {}, 策略: {}",
                conflicts.size(), resolutionStrategy);

        // 调用ConflictResolver
        return conflictResolver.resolveConflicts(conflicts, resolutionStrategy);
    }

    /**
     * 应用冲突解决方案
     *
     * @param result    排班结果
     * @param resolution 冲突解决方案
     * @return 应用解决方案后的排班结果
     */
    private ScheduleResult applyConflictResolution(ScheduleResult result,
            ConflictResolution resolution) {
        log.debug("[排班执行服务] 应用冲突解决方案");

        if (resolution == null || !resolution.getResolutionSuccessful()) {
            log.warn("[排班执行服务] 冲突解决失败，保持原排班结果");
            return result;
        }

        // 应用解决方案到排班结果
        // TODO: 实现具体的冲突解决应用逻辑

        return result;
    }

    /**
     * 优化排班
     *
     * @param scheduleData       排班数据
     * @param optimizationTarget 优化目标
     * @return 优化后的排班
     */
    private OptimizedSchedule optimize(ScheduleData scheduleData, String optimizationTarget) {
        log.debug("[排班执行服务] 优化排班, 目标: {}", optimizationTarget);

        // 调用ScheduleOptimizer
        return scheduleOptimizer.optimizeSchedule(scheduleData, optimizationTarget);
    }

    /**
     * 应用优化
     *
     * @param result           排班结果
     * @param optimizedSchedule 优化后的排班
     * @return 应用优化后的排班结果
     */
    private ScheduleResult applyOptimization(ScheduleResult result, OptimizedSchedule optimizedSchedule) {
        log.debug("[排班执行服务] 应用排班优化");

        if (optimizedSchedule == null || !optimizedSchedule.getOptimizationSuccessful()) {
            log.warn("[排班执行服务] 排班优化失败，保持原排班结果");
            return result;
        }

        // 应用优化到排班结果
        // TODO: 实现具体的优化应用逻辑

        return result;
    }

    /**
     * 生成统计信息
     *
     * @param result 排班结果
     * @return 统计信息Map
     */
    private Map<String, Object> generateStatistics(ScheduleResult result) {
        log.debug("[排班执行服务] 生成统计信息");

        Map<String, Object> statisticsMap = new HashMap<>();

        // TODO: 从结果数据计算实际统计信息
        statisticsMap.put("totalUsers", result.getScheduleRecords().size());
        statisticsMap.put("totalDays", 7); // 临时值
        statisticsMap.put("totalRecords", result.getScheduleRecords().size());
        statisticsMap.put("averageDaysPerUser", 5.0); // 临时值
        statisticsMap.put("workloadBalance", 0.8); // 临时值
        statisticsMap.put("shiftCoverage", 0.9); // 临时值
        statisticsMap.put("constraintSatisfaction", 0.85); // 临时值
        statisticsMap.put("costSaving", 0.1); // 临时值

        return statisticsMap;
    }

    /**
     * 转换为模型记录列表
     */
    private List<net.lab1024.sa.attendance.engine.model.ScheduleRecord> convertToModelRecords(
            List<Object> historyRecords) {
        // TODO: 实现记录转换逻辑
        return new ArrayList<>();
    }

    /**
     * 转换为模型冲突列表
     */
    private List<ConflictDetectionResult.ScheduleConflict> convertToModelConflicts(
            List<net.lab1024.sa.attendance.engine.conflict.ScheduleConflict> conflicts) {
        if (conflicts == null) {
            return new ArrayList<>();
        }

        return conflicts.stream().map(conf -> {
            ConflictDetectionResult.ScheduleConflict modelConflict =
                    new ConflictDetectionResult.ScheduleConflict();
            modelConflict.setConflictId(conf.getConflictId());
            modelConflict.setConflictType(conf.getConflictType());
            modelConflict.setDescription(conf.getDescription());
            modelConflict.setSeverity(conf.getSeverity());
            modelConflict.setAffectedEmployees(conf.getAffectedEmployees());
            modelConflict.setConflictDate(conf.getConflictDate());
            modelConflict.setTimeSlots(conf.getTimeSlots());
            return modelConflict;
        }).collect(Collectors.toList());
    }
}
