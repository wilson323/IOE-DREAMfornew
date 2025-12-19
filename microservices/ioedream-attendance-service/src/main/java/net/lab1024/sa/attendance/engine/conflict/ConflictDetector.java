package net.lab1024.sa.attendance.engine.conflict;

import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.util.List;
import java.util.Map;

/**
 * 排班冲突检测器接口
 * <p>
 * 负责检测排班方案中的各种冲突情况
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface ConflictDetector {

    /**
     * 检测排班记录中的冲突
     *
     * @param scheduleRecords 排班记录列表
     * @param scheduleData     排班数据
     * @return 冲突检测结果
     */
    ConflictDetectionResult detectConflicts(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData);

    /**
     * 检测单个员工的排班冲突
     *
     * @param employeeId    员工ID
     * @param scheduleRecords 员工的排班记录
     * @param scheduleData  排班数据
     * @return 该员工的冲突检测结果
     */
    EmployeeConflictResult detectEmployeeConflicts(Long employeeId, List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData);

    /**
     * 检测时间冲突
     *
     * @param scheduleRecord 待检测的排班记录
     * @param existingRecords 已存在的排班记录
     * @return 时间冲突检测结果
     */
    TimeConflictResult detectTimeConflict(ScheduleRecord scheduleRecord, List<ScheduleRecord> existingRecords);

    /**
     * 检测技能冲突
     *
     * @param scheduleRecord 排班记录
     * @param scheduleData    排班数据
     * @return 技能冲突检测结果
     */
    SkillConflictResult detectSkillConflict(ScheduleRecord scheduleRecord, ScheduleData scheduleData);

    /**
     * 检测工作时长冲突
     *
     * @param employeeId       员工ID
     * @param scheduleRecords  员工的排班记录
     * @param scheduleData     排班数据
     * @return 工作时长冲突检测结果
     */
    WorkHourConflictResult detectWorkHourConflict(Long employeeId, List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData);

    /**
     * 检测班次容量冲突
     *
     * @param shiftId        班次ID
     * @param scheduleRecords 该班次的排班记录
     * @param scheduleData   排班数据
     * @return 班次容量冲突检测结果
     */
    CapacityConflictResult detectCapacityConflict(Long shiftId, List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData);

    /**
     * 批量检测冲突
     *
     * @param scheduleRecords 排班记录列表
     * @param scheduleData     排班数据
     * @return 批量冲突检测结果
     */
    BatchConflictResult detectBatchConflicts(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData);

    /**
     * 获取冲突检测统计信息
     *
     * @param conflictResults 冲突检测结果列表
     * @return 冲突检测统计信息
     */
    ConflictStatistics getConflictStatistics(List<ConflictDetectionResult> conflictResults);

    /**
     * 验证冲突检测结果
     *
     * @param conflictResult 冲突检测结果
     * @return 验证结果
     */
    boolean validateConflictResult(ConflictDetectionResult conflictResult);
}