package net.lab1024.sa.attendance.engine.conflict;

import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.util.List;

/**
 * 排班冲突解决器接口
 * <p>
 * 负责解决检测到的排班冲突
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface ConflictResolver {

    /**
     * 自动解决冲突
     *
     * @param conflictResult 冲突检测结果
     * @param scheduleData    排班数据
     * @return 解决结果
     */
    ConflictResolutionResult resolveConflicts(ConflictDetectionResult conflictResult, ScheduleData scheduleData);

    /**
     * 解决时间冲突
     *
     * @param timeConflict 时间冲突
     * @param scheduleData 排班数据
     * @return 解决结果
     */
    ConflictResolutionResult resolveTimeConflict(TimeConflict timeConflict, ScheduleData scheduleData);

    /**
     * 解决技能冲突
     *
     * @param skillConflict 技能冲突
     * @param scheduleData   排班数据
     * @return 解决结果
     */
    ConflictResolutionResult resolveSkillConflict(SkillConflict skillConflict, ScheduleData scheduleData);

    /**
     * 解决工作时长冲突
     *
     * @param workHourConflict 工作时长冲突
     * @param scheduleData      排班数据
     * @return 解决结果
     */
    ConflictResolutionResult resolveWorkHourConflict(WorkHourConflict workHourConflict, ScheduleData scheduleData);

    /**
     * 解决班次容量冲突
     *
     * @param capacityConflict 班次容量冲突
     * @param scheduleData     排班数据
     * @return 解决结果
     */
    ConflictResolutionResult resolveCapacityConflict(CapacityConflict capacityConflict, ScheduleData scheduleData);

    /**
     * 批量解决冲突
     *
     * @param conflictResults 冲突检测结果列表
     * @param scheduleData     排班数据
     * @return 批量解决结果
     */
    BatchResolutionResult resolveBatchConflicts(List<ConflictDetectionResult> conflictResults, ScheduleData scheduleData);

    /**
     * 获取解决策略
     *
     * @param conflictType 冲突类型
     * @return 解决策略
     */
    ResolutionStrategy getResolutionStrategy(String conflictType);

    /**
     * 验证解决方案
     *
     * @param resolutionResult 解决结果
     * @param scheduleData      排班数据
     * @return 验证结果
     */
    boolean validateResolution(ConflictResolutionResult resolutionResult, ScheduleData scheduleData);

    /**
     * 获取解决统计信息
     *
     * @param resolutionResults 解决结果列表
     * @return 解决统计信息
     */
    ResolutionStatistics getResolutionStatistics(List<ConflictResolutionResult> resolutionResults);
}