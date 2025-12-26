package net.lab1024.sa.attendance.engine;

import net.lab1024.sa.attendance.engine.model.*;
import net.lab1024.sa.attendance.entity.SmartSchedulePlanEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 智能排班引擎接口
 * <p>
 * 提供智能排班的核心功能，支持多种排班算法和策略
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface ScheduleEngine {

    /**
     * 执行智能排班
     *
     * @param request 排班请求
     * @return 排班结果
     */
    ScheduleResult executeIntelligentSchedule(ScheduleRequest request);

    /**
     * 生成排班计划
     *
     * @param planId 计划ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班计划
     */
    SmartSchedulePlanEntity generateSmartSchedulePlanEntity(Long planId, LocalDate startDate, LocalDate endDate);

    /**
     * 验证排班冲突
     *
     * @param scheduleData 排班数据
     * @return 冲突检测结果
     */
    ConflictDetectionResult validateScheduleConflicts(ScheduleData scheduleData);

    /**
     * 解决排班冲突
     *
     * @param conflicts 冲突列表
     * @param resolutionStrategy 解决策略
     * @return 解决方案
     */
    ConflictResolution resolveScheduleConflicts(List<ScheduleConflict> conflicts, String resolutionStrategy);

    /**
     * 优化排班结果
     *
     * @param scheduleData 原始排班数据
     * @param optimizationTarget 优化目标
     * @return 优化后的排班结果
     */
    OptimizedSchedule optimizeSchedule(ScheduleData scheduleData, String optimizationTarget);

    /**
     * 预测排班效果
     *
     * @param scheduleData 排班数据
     * @return 预测结果
     */
    SchedulePrediction predictScheduleEffect(ScheduleData scheduleData);

    /**
     * 获取排班统计信息
     *
     * @param planId 计划ID
     * @return 统计信息
     */
    ScheduleStatistics getScheduleStatistics(Long planId);
}
