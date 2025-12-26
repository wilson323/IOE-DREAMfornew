package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.engine.conflict.ScheduleConflict;
import net.lab1024.sa.attendance.engine.model.Chromosome;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;

import java.util.List;
import java.util.Map;

/**
 * 排班冲突服务接口
 * <p>
 * 核心功能：
 * - 检测排班冲突
 * - 解决排班冲突
 * - 生成冲突报告
 * - 冲突统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ScheduleConflictService {

    /**
     * 检测排班方案中的所有冲突
     *
     * @param chromosome 染色体（排班方案）
     * @param config     优化配置
     * @return 冲突列表
     */
    List<ScheduleConflict> detectConflicts(Chromosome chromosome, OptimizationConfig config);

    /**
     * 检测员工相关冲突
     *
     * @param chromosome 染色体
     * @param config     优化配置
     * @return 冲突列表
     */
    List<ScheduleConflict> detectEmployeeConflicts(Chromosome chromosome, OptimizationConfig config);

    /**
     * 检测班次相关冲突
     *
     * @param chromosome 染色体
     * @param config     优化配置
     * @return 冲突列表
     */
    List<ScheduleConflict> detectShiftConflicts(Chromosome chromosome, OptimizationConfig config);

    /**
     * 检测日期相关冲突
     *
     * @param chromosome 染色体
     * @param config     优化配置
     * @return 冲突列表
     */
    List<ScheduleConflict> detectDateConflicts(Chromosome chromosome, OptimizationConfig config);

    /**
     * 自动解决冲突
     *
     * @param conflicts 冲突列表
     * @param config    优化配置
     * @return 解决后的排班方案
     */
    Chromosome resolveConflicts(List<ScheduleConflict> conflicts, OptimizationConfig config);

    /**
     * 生成冲突报告
     *
     * @param conflicts 冲突列表
     * @return 冲突报告
     */
    Map<String, Object> generateConflictReport(List<ScheduleConflict> conflicts);

    /**
     * 统计冲突严重程度分布
     *
     * @param conflicts 冲突列表
     * @return 严重程度分布
     */
    Map<Integer, Integer> getSeverityDistribution(List<ScheduleConflict> conflicts);

    /**
     * 统计冲突类型分布
     *
     * @param conflicts 冲突列表
     * @return 类型分布
     */
    Map<String, Integer> getTypeDistribution(List<ScheduleConflict> conflicts);

    /**
     * 生成冲突摘要
     *
     * @param conflicts 冲突列表
     * @return 冲突摘要
     */
    String generateConflictSummary(List<ScheduleConflict> conflicts);
}
