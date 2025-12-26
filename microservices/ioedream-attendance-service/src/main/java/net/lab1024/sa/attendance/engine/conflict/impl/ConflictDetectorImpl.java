package net.lab1024.sa.attendance.engine.conflict.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


import net.lab1024.sa.attendance.engine.conflict.BatchConflictResult;
import net.lab1024.sa.attendance.engine.conflict.CapacityConflict;
import net.lab1024.sa.attendance.engine.conflict.CapacityConflictResult;
import net.lab1024.sa.attendance.engine.conflict.ConflictDetectionResult;
import net.lab1024.sa.attendance.engine.conflict.ConflictDetector;
import net.lab1024.sa.attendance.engine.conflict.ConflictStatistics;
import net.lab1024.sa.attendance.engine.conflict.EmployeeConflictResult;
import net.lab1024.sa.attendance.engine.conflict.OtherConflict;
import net.lab1024.sa.attendance.engine.conflict.SkillConflict;
import net.lab1024.sa.attendance.engine.conflict.SkillConflictResult;
import net.lab1024.sa.attendance.engine.conflict.TimeConflict;
import net.lab1024.sa.attendance.engine.conflict.TimeConflictResult;
import net.lab1024.sa.attendance.engine.conflict.WorkHourConflict;
import net.lab1024.sa.attendance.engine.conflict.WorkHourConflictResult;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

/**
 * 冲突检测器实现类
 * <p>
 * 提供全面的排班冲突检测功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class ConflictDetectorImpl implements ConflictDetector {


    // 冲突检测配置
    private static final int MAX_WORK_HOURS_PER_DAY = 12;
    private static final int MAX_WORK_HOURS_PER_WEEK = 60;
    private static final int MIN_REST_HOURS = 11;
    private static final int SEVERE_CONFLICT_THRESHOLD = 3;

    // 检测统计
    private final Map<String, Integer> detectionStatistics = new HashMap<>();

    @Override
    public ConflictDetectionResult detectConflicts(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData) {
        log.info("[冲突检测] 开始检测排班冲突，记录数量: {}", scheduleRecords.size());

        LocalDateTime startTime = LocalDateTime.now();
        ConflictDetectionResult result = ConflictDetectionResult.builder()
                .detectionId(UUID.randomUUID().toString())
                .detectionStartTime(startTime)
                .timeConflicts(new ArrayList<>())
                .skillConflicts(new ArrayList<>())
                .workHourConflicts(new ArrayList<>())
                .capacityConflicts(new ArrayList<>())
                .otherConflicts(new ArrayList<>())
                .conflictTypeStatistics(new HashMap<>())
                .resolutionSuggestions(new ArrayList<>())
                .hasConflicts(false)
                .totalConflicts(0)
                .severeConflicts(0)
                .minorConflicts(0)
                .algorithmVersion("1.0.0")
                .detectionParameters(new HashMap<>())
                .build();

        try {
            // 1. 检测时间冲突
            detectTimeConflicts(scheduleRecords, result);

            // 2. 检测技能冲突
            detectSkillConflicts(scheduleRecords, scheduleData, result);

            // 3. 检测工作时长冲突
            detectWorkHourConflicts(scheduleRecords, scheduleData, result);

            // 4. 检测班次容量冲突
            detectCapacityConflicts(scheduleRecords, scheduleData, result);

            // 5. 计算冲突严重程度评分
            calculateConflictSeverityScore(result);

            // 6. 生成修复建议
            generateResolutionSuggestions(result);

            // 7. 更新统计信息
            updateDetectionStatistics(result);

            LocalDateTime endTime = LocalDateTime.now();
            result.setDetectionEndTime(endTime);
            result.setDetectionDuration(ChronoUnit.MILLIS.between(startTime, endTime));

            log.info("[冲突检测] 检测完成，总冲突数: {}，严重冲突: {}，耗时: {}ms",
                    result.getTotalConflicts(), result.getSevereConflicts(), result.getDetectionDuration());

            return result;

        } catch (Exception e) {
            log.error("[冲突检测] 检测过程中发生异常", e);
            result.setHasConflicts(true);
            result.addOtherConflict(OtherConflict.builder()
                    .conflictId(UUID.randomUUID().toString())
                    .conflictType("DETECTION_ERROR")
                    .conflictDescription("冲突检测过程中发生异常: " + e.getMessage())
                    .severity(5)
                    .status(TimeConflict.ConflictStatus.ESCALATED)
                    .build());
            return result;
        }
    }

    @Override
    public EmployeeConflictResult detectEmployeeConflicts(Long employeeId, List<ScheduleRecord> scheduleRecords,
            ScheduleData scheduleData) {
        log.debug("[冲突检测] 检测员工{}的排班冲突", employeeId);

        EmployeeConflictResult result = EmployeeConflictResult.builder()
                .employeeId(employeeId)
                .conflicts(new ArrayList<>())
                .build();

        // 筛选该员工的排班记录
        List<ScheduleRecord> employeeRecords = scheduleRecords.stream()
                .filter(record -> employeeId.equals(record.getEmployeeId()))
                .collect(Collectors.toList());

        // 检测该员工的各种冲突
        for (int i = 0; i < employeeRecords.size(); i++) {
            for (int j = i + 1; j < employeeRecords.size(); j++) {
                ScheduleRecord record1 = employeeRecords.get(i);
                ScheduleRecord record2 = employeeRecords.get(j);

                // 检测时间冲突
                TimeConflictResult timeConflict = detectTimeConflict(record1, Collections.singletonList(record2));
                if (timeConflict.hasConflict()) {
                    result.addConflict(timeConflict.getConflict());
                }
            }
        }

        // 检测工作时长冲突
        WorkHourConflictResult workHourConflict = detectWorkHourConflict(employeeId, employeeRecords, scheduleData);
        if (workHourConflict.hasConflict()) {
            result.addConflict(workHourConflict.getConflict());
        }

        result.setTotalConflicts(result.getConflicts().size());
        result.setHasConflicts(result.getTotalConflicts() > 0);

        return result;
    }

    @Override
    public TimeConflictResult detectTimeConflict(ScheduleRecord scheduleRecord, List<ScheduleRecord> existingRecords) {
        TimeConflictResult result = TimeConflictResult.builder()
                .hasConflict(false)
                .build();

        for (ScheduleRecord existing : existingRecords) {
            if (isTimeOverlap(scheduleRecord, existing)) {
                TimeConflict conflict = TimeConflict.builder()
                        .conflictId(UUID.randomUUID().toString())
                        .conflictType("TIME_OVERLAP")
                        .conflictDescription("排班时间重叠")
                        .employeeId(scheduleRecord.getEmployeeId())
                        .scheduleRecordId1(scheduleRecord.getRecordId())
                        .scheduleRecordId2(existing.getRecordId())
                        .shiftId1(scheduleRecord.getShiftId())
                        .shiftId2(existing.getShiftId())
                        .conflictStartTime1(scheduleRecord.getWorkStartTime())
                        .conflictEndTime1(scheduleRecord.getWorkEndTime())
                        .conflictStartTime2(existing.getWorkStartTime())
                        .conflictEndTime2(existing.getWorkEndTime())
                        .conflictDuration(calculateOverlapDuration(scheduleRecord, existing))
                        .severity(calculateTimeConflictSeverity(scheduleRecord, existing))
                        .conflictReason("排班时间存在重叠")
                        .autoResolvable(true)
                        .suggestedResolution("调整其中一个排班的时间")
                        .detectionTime(LocalDateTime.now())
                        .detectionAlgorithm("TIME_OVERLAP_DETECTION")
                        .status(TimeConflict.ConflictStatus.PENDING)
                        .build();

                result.setConflict(conflict);
                result.setHasConflict(true);
                break;
            }
        }

        return result;
    }

    @Override
    public SkillConflictResult detectSkillConflict(ScheduleRecord scheduleRecord, ScheduleData scheduleData) {
        log.debug("[冲突检测] 检测技能冲突: employeeId={}, shiftId={}",
                scheduleRecord.getEmployeeId(), scheduleRecord.getShiftId());

        SkillConflictResult result = SkillConflictResult.builder()
                .hasConflict(false)
                .build();

        try {
            // 1. 获取班次所需的技能
            List<String> requiredSkills = getRequiredSkillsForShift(scheduleRecord.getShiftId(), scheduleData);
            if (requiredSkills == null || requiredSkills.isEmpty()) {
                // 班次无技能要求
                return result;
            }

            // 2. 获取员工具备的技能
            List<String> employeeSkills = getEmployeeSkills(scheduleRecord.getEmployeeId(), scheduleData);
            if (employeeSkills == null || employeeSkills.isEmpty()) {
                // 员工无技能记录
                SkillConflict conflict = SkillConflict.builder()
                        .conflictId(UUID.randomUUID().toString())
                        .conflictType("MISSING_ALL_SKILLS")
                        .conflictDescription("员工缺少班次所需的所有技能")
                        .employeeId(scheduleRecord.getEmployeeId())
                        .shiftId(scheduleRecord.getShiftId())
                        .requiredSkills(requiredSkills)
                        .employeeSkills(new ArrayList<>())
                        .missingSkills(requiredSkills)
                        .severity(5)
                        // .status() - SkillConflict 没有此字段，已移除
                        .detectionTime(java.time.LocalDateTime.now())
                        .build();

                result.setConflict(conflict);
                result.setHasConflict(true);
                return result;
            }

            // 3. 检查技能匹配情况
            List<String> missingSkills = new ArrayList<>();
            for (String requiredSkill : requiredSkills) {
                if (!employeeSkills.contains(requiredSkill)) {
                    missingSkills.add(requiredSkill);
                }
            }

            // 4. 如果有缺失技能，创建冲突记录
            if (!missingSkills.isEmpty()) {
                SkillConflict conflict = SkillConflict.builder()
                        .conflictId(UUID.randomUUID().toString())
                        .conflictType(missingSkills.size() == requiredSkills.size() ? "MISSING_ALL_SKILLS" : "MISSING_PARTIAL_SKILLS")
                        .conflictDescription(String.format("员工缺少%d项技能: %s", missingSkills.size(), missingSkills))
                        .employeeId(scheduleRecord.getEmployeeId())
                        .shiftId(scheduleRecord.getShiftId())
                        .requiredSkills(requiredSkills)
                        .employeeSkills(employeeSkills)
                        .missingSkills(missingSkills)
                        .severity(calculateSkillConflictSeverity(requiredSkills, missingSkills))
                        // .status() - SkillConflict 没有此字段，已移除
                        .detectionTime(java.time.LocalDateTime.now())
                        .build();

                result.setConflict(conflict);
                result.setHasConflict(true);

                log.debug("[冲突检测] 发现技能冲突: employeeId={}, missingSkills={}",
                        scheduleRecord.getEmployeeId(), missingSkills);
            }

            return result;

        } catch (Exception e) {
            log.error("[冲突检测] 检测技能冲突失败", e);
            return result;
        }
    }

    @Override
    public WorkHourConflictResult detectWorkHourConflict(Long employeeId, List<ScheduleRecord> scheduleRecords,
            ScheduleData scheduleData) {
        WorkHourConflictResult result = WorkHourConflictResult.builder()
                .hasConflict(false)
                .build();

        // 计算每日工作时长
        Map<String, Integer> dailyWorkHours = calculateDailyWorkHours(scheduleRecords);

        // 计算每周工作时长
        Integer weeklyWorkHours = dailyWorkHours.values().stream().mapToInt(Integer::intValue).sum();

        // 检查每日工作时长
        for (Map.Entry<String, Integer> entry : dailyWorkHours.entrySet()) {
            if (entry.getValue() > MAX_WORK_HOURS_PER_DAY) {
                WorkHourConflict conflict = WorkHourConflict.builder()
                        .conflictId(UUID.randomUUID().toString())
                        .conflictType("DAILY_WORK_HOUR_EXCEEDED")
                        .conflictDescription("每日工作时长超限")
                        .employeeId(employeeId)
                        .workDate(entry.getKey())
                        .actualWorkHours(entry.getValue() != null ? entry.getValue().doubleValue() : null)
                        .maxAllowedWorkHours((double) MAX_WORK_HOURS_PER_DAY)
                        .severity(4)
                        .status(TimeConflict.ConflictStatus.PENDING)
                        .build();

                result.setConflict(conflict);
                result.setHasConflict(true);
                break;
            }
        }

        // 检查每周工作时长
        if (weeklyWorkHours > MAX_WORK_HOURS_PER_WEEK) {
            WorkHourConflict conflict = WorkHourConflict.builder()
                    .conflictId(UUID.randomUUID().toString())
                    .conflictType("WEEKLY_WORK_HOUR_EXCEEDED")
                    .conflictDescription("每周工作时长超限")
                    .employeeId(employeeId)
                    .actualWorkHours(weeklyWorkHours != null ? weeklyWorkHours.doubleValue() : null)
                    .maxAllowedWorkHours((double) MAX_WORK_HOURS_PER_WEEK)
                    .severity(3)
                    .status(TimeConflict.ConflictStatus.PENDING)
                    .build();

            result.setConflict(conflict);
            result.setHasConflict(true);
        }

        return result;
    }

    @Override
    public CapacityConflictResult detectCapacityConflict(Long shiftId, List<ScheduleRecord> scheduleRecords,
            ScheduleData scheduleData) {
        log.debug("[冲突检测] 检测容量冲突: shiftId={}, assignedCount={}",
                shiftId, scheduleRecords.size());

        CapacityConflictResult result = CapacityConflictResult.builder()
                .hasConflict(false)
                .build();

        try {
            // 1. 获取班次容量限制
            Integer capacityLimit = getShiftCapacity(shiftId, scheduleData);
            if (capacityLimit == null || capacityLimit <= 0) {
                // 班次无容量限制
                return result;
            }

            // 2. 统计已分配人员数量
            int assignedCount = scheduleRecords.size();

            // 3. 检查是否超过容量限制
            if (assignedCount > capacityLimit) {
                int overloadCount = assignedCount - capacityLimit;

                CapacityConflict conflict = CapacityConflict.builder()
                        .conflictId(UUID.randomUUID().toString())
                        .conflictType("CAPACITY_EXCEEDED")
                        .conflictDescription(String.format("班次人员数量超过容量限制: %d/%d (+%d)",
                                assignedCount, capacityLimit, overloadCount))
                        .shiftId(shiftId)
                        .actualEmployeeCount(assignedCount)  // 原字段: assignedCount
                        .maxCapacity(capacityLimit)           // 原字段: capacityLimit
                        .overCapacityCount(overloadCount)     // 原字段: overloadCount
                        .severity(calculateCapacityConflictSeverity(assignedCount, capacityLimit))
                        // .status() - CapacityConflict 没有此字段，已移除
                        // .overloadedEmployeeIds() - CapacityConflict 没有此字段，已移除
                        .detectionTime(java.time.LocalDateTime.now())
                        .build();

                result.setConflict(conflict);
                result.setHasConflict(true);

                log.warn("[冲突检测] 发现容量冲突: shiftId={}, assigned={}, limit={}, overload={}",
                        shiftId, assignedCount, capacityLimit, overloadCount);
            } else if (assignedCount == capacityLimit) {
                // 容量已满，但未超载
                log.debug("[冲突检测] 班次容量已满: shiftId={}, assigned={}", shiftId, assignedCount);
            }

            return result;

        } catch (Exception e) {
            log.error("[冲突检测] 检测容量冲突失败", e);
            return result;
        }
    }

    @Override
    public BatchConflictResult detectBatchConflicts(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData) {
        log.info("[冲突检测] 开始批量冲突检测");

        BatchConflictResult result = BatchConflictResult.builder()
                .totalRecords(scheduleRecords.size())
                .conflictResults(new ArrayList<>())
                .build();

        // 按员工分组检测
        Map<Long, List<ScheduleRecord>> employeeRecords = scheduleRecords.stream()
                .collect(Collectors.groupingBy(ScheduleRecord::getEmployeeId));

        for (Map.Entry<Long, List<ScheduleRecord>> entry : employeeRecords.entrySet()) {
            EmployeeConflictResult employeeConflict = detectEmployeeConflicts(entry.getKey(), entry.getValue(),
                    scheduleData);
            if (employeeConflict.getHasConflicts()) {
                result.getConflictResults().add(employeeConflict);
            }
        }

        // 检测全局冲突
        ConflictDetectionResult globalConflict = detectConflicts(scheduleRecords, scheduleData);
        if (globalConflict.getHasConflicts()) {
            result.setGlobalConflict(globalConflict);
        }

        result.setTotalEmployees(employeeRecords.size());
        result.setEmployeesWithConflicts(result.getConflictResults().size());
        result.setHasConflicts(result.getEmployeesWithConflicts() > 0 || globalConflict.getHasConflicts());

        return result;
    }

    @Override
    public ConflictStatistics getConflictStatistics(List<ConflictDetectionResult> conflictResults) {
        ConflictStatistics statistics = ConflictStatistics.builder()
                .totalDetections(conflictResults.size())
                .totalConflicts(0)
                .severeConflicts(0)
                .minorConflicts(0)
                .conflictTypeStatistics(new HashMap<>())
                .build();

        for (ConflictDetectionResult result : conflictResults) {
            statistics.setTotalConflicts(statistics.getTotalConflicts() + result.getTotalConflicts());
            statistics.setSevereConflicts(statistics.getSevereConflicts() + result.getSevereConflicts());
            statistics.setMinorConflicts(statistics.getMinorConflicts() + result.getMinorConflicts());

            // 合并冲突类型统计
            result.getConflictTypeStatistics()
                    .forEach((type, count) -> statistics.getConflictTypeStatistics().merge(type, count, Integer::sum));
        }

        return statistics;
    }

    @Override
    public boolean validateConflictResult(ConflictDetectionResult conflictResult) {
        if (conflictResult == null) {
            return false;
        }

        // 验证基本字段
        if (conflictResult.getDetectionId() == null || conflictResult.getDetectionStartTime() == null) {
            return false;
        }

        // 验证冲突数据一致性
        if (conflictResult.getHasConflicts() && conflictResult.getTotalConflicts() == 0) {
            return false;
        }

        // 验证冲突数量计算
        int calculatedTotal = (conflictResult.getTimeConflicts() != null ? conflictResult.getTimeConflicts().size() : 0)
                +
                (conflictResult.getSkillConflicts() != null ? conflictResult.getSkillConflicts().size() : 0) +
                (conflictResult.getWorkHourConflicts() != null ? conflictResult.getWorkHourConflicts().size() : 0) +
                (conflictResult.getCapacityConflicts() != null ? conflictResult.getCapacityConflicts().size() : 0) +
                (conflictResult.getOtherConflicts() != null ? conflictResult.getOtherConflicts().size() : 0);

        return !conflictResult.getHasConflicts() || calculatedTotal == conflictResult.getTotalConflicts();
    }

    /**
     * 检测时间冲突
     */
    private void detectTimeConflicts(List<ScheduleRecord> scheduleRecords, ConflictDetectionResult result) {
        for (int i = 0; i < scheduleRecords.size(); i++) {
            for (int j = i + 1; j < scheduleRecords.size(); j++) {
                ScheduleRecord record1 = scheduleRecords.get(i);
                ScheduleRecord record2 = scheduleRecords.get(j);

                // 只检测同一员工的时间冲突
                if (!record1.getEmployeeId().equals(record2.getEmployeeId())) {
                    continue;
                }

                if (isTimeOverlap(record1, record2)) {
                    TimeConflict conflict = TimeConflict.builder()
                            .conflictId(UUID.randomUUID().toString())
                            .conflictType("TIME_OVERLAP")
                            .conflictDescription("同一员工排班时间重叠")
                            .employeeId(record1.getEmployeeId())
                            .scheduleRecordId1(record1.getRecordId())
                            .scheduleRecordId2(record2.getRecordId())
                            .shiftId1(record1.getShiftId())
                            .shiftId2(record2.getShiftId())
                            .conflictStartTime1(record1.getWorkStartTime())
                            .conflictEndTime1(record1.getWorkEndTime())
                            .conflictStartTime2(record2.getWorkStartTime())
                            .conflictEndTime2(record2.getWorkEndTime())
                            .conflictDuration(calculateOverlapDuration(record1, record2))
                            .severity(calculateTimeConflictSeverity(record1, record2))
                            .conflictReason("同一员工在同一时间段内被安排了两个班次")
                            .autoResolvable(true)
                            .suggestedResolution("调整其中一个排班的时间或删除其中一个排班")
                            .detectionTime(LocalDateTime.now())
                            .detectionAlgorithm("TIME_OVERLAP_DETECTION")
                            .status(TimeConflict.ConflictStatus.PENDING)
                            .build();

                    result.addTimeConflict(conflict);
                }
            }
        }
    }

    /**
     * 检测技能冲突
     */
    private void detectSkillConflicts(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData,
            ConflictDetectionResult result) {
        log.debug("[冲突检测] 开始批量检测技能冲突，记录数量: {}", scheduleRecords.size());

        int conflictCount = 0;
        for (ScheduleRecord record : scheduleRecords) {
            try {
                SkillConflictResult skillConflict = detectSkillConflict(record, scheduleData);
                if (skillConflict.getHasConflict()) {
                    result.addSkillConflict(skillConflict.getConflict());
                    conflictCount++;
                }
            } catch (Exception e) {
                log.error("[冲突检测] 检测技能冲突失败: employeeId={}, shiftId={}",
                        record.getEmployeeId(), record.getShiftId(), e);
            }
        }

        log.debug("[冲突检测] 技能冲突检测完成: 检测{}条记录，发现{}个冲突",
                scheduleRecords.size(), conflictCount);
    }

    /**
     * 检测工作时长冲突
     */
    private void detectWorkHourConflicts(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData,
            ConflictDetectionResult result) {
        // 按员工分组
        Map<Long, List<ScheduleRecord>> employeeRecords = scheduleRecords.stream()
                .collect(Collectors.groupingBy(ScheduleRecord::getEmployeeId));

        for (Map.Entry<Long, List<ScheduleRecord>> entry : employeeRecords.entrySet()) {
            WorkHourConflictResult workHourConflict = detectWorkHourConflict(entry.getKey(), entry.getValue(),
                    scheduleData);
            if (workHourConflict.getHasConflict()) {
                result.addWorkHourConflict(workHourConflict.getConflict());
            }
        }
    }

    /**
     * 检测班次容量冲突
     */
    private void detectCapacityConflicts(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData,
            ConflictDetectionResult result) {
        // 按班次分组
        Map<Long, List<ScheduleRecord>> shiftRecords = scheduleRecords.stream()
                .collect(Collectors.groupingBy(ScheduleRecord::getShiftId));

        for (Map.Entry<Long, List<ScheduleRecord>> entry : shiftRecords.entrySet()) {
            CapacityConflictResult capacityConflict = detectCapacityConflict(entry.getKey(), entry.getValue(),
                    scheduleData);
            if (capacityConflict.getHasConflict()) {
                result.addCapacityConflict(capacityConflict.getConflict());
            }
        }
    }

    /**
     * 检查时间是否重叠
     */
    private boolean isTimeOverlap(ScheduleRecord record1, ScheduleRecord record2) {
        return record1.getWorkStartTime().isBefore(record2.getWorkEndTime()) &&
                record2.getWorkStartTime().isBefore(record1.getWorkEndTime());
    }

    /**
     * 计算重叠时长（分钟）
     */
    private Integer calculateOverlapDuration(ScheduleRecord record1, ScheduleRecord record2) {
        LocalDateTime overlapStart = record1.getWorkStartTime().isAfter(record2.getWorkStartTime()) ? record1.getWorkStartTime()
                : record2.getWorkStartTime();
        LocalDateTime overlapEnd = record1.getWorkEndTime().isBefore(record2.getWorkEndTime()) ? record1.getWorkEndTime()
                : record2.getWorkEndTime();

        return (int) ChronoUnit.MINUTES.between(overlapStart, overlapEnd);
    }

    /**
     * 计算时间冲突严重程度
     */
    private Integer calculateTimeConflictSeverity(ScheduleRecord record1, ScheduleRecord record2) {
        int overlapDuration = calculateOverlapDuration(record1, record2);

        if (overlapDuration >= 480) { // 8小时以上
            return 5;
        } else if (overlapDuration >= 240) { // 4小时以上
            return 4;
        } else if (overlapDuration >= 120) { // 2小时以上
            return 3;
        } else if (overlapDuration >= 60) { // 1小时以上
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * 计算每日工作时长
     */
    private Map<String, Integer> calculateDailyWorkHours(List<ScheduleRecord> scheduleRecords) {
        Map<String, Integer> dailyHours = new HashMap<>();

        for (ScheduleRecord record : scheduleRecords) {
            String dateKey = record.getWorkStartTime().toLocalDate().toString();
            int workHours = (int) ChronoUnit.HOURS.between(record.getWorkStartTime(), record.getWorkEndTime());
            dailyHours.merge(dateKey, workHours, Integer::sum);
        }

        return dailyHours;
    }

    /**
     * 计算冲突严重程度评分
     */
    private void calculateConflictSeverityScore(ConflictDetectionResult result) {
        if (result.getTotalConflicts() == 0) {
            result.setConflictSeverityScore(0.0);
            return;
        }

        double score = 0.0;
        int totalConflicts = result.getTotalConflicts();

        // 严重冲突权重更高
        if (result.getSevereConflicts() != null) {
            score += result.getSevereConflicts() * 10.0;
        }

        // 一般冲突权重较低
        if (result.getMinorConflicts() != null) {
            score += result.getMinorConflicts() * 3.0;
        }

        result.setConflictSeverityScore(Math.min(score / totalConflicts, 100.0));
    }

    /**
     * 生成修复建议
     */
    private void generateResolutionSuggestions(ConflictDetectionResult result) {
        List<String> suggestions = new ArrayList<>();

        if (result.getTimeConflicts() != null && !result.getTimeConflicts().isEmpty()) {
            suggestions.add("调整排班时间以避免时间重叠");
        }

        if (result.getWorkHourConflicts() != null && !result.getWorkHourConflicts().isEmpty()) {
            suggestions.add("调整工作时长以符合法规要求");
        }

        if (result.getCapacityConflicts() != null && !result.getCapacityConflicts().isEmpty()) {
            suggestions.add("调整班次人员分配以符合容量限制");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("未发现需要修复的冲突");
        }

        result.setResolutionSuggestions(suggestions);
    }

    /**
     * 更新检测统计信息
     */
    private void updateDetectionStatistics(ConflictDetectionResult result) {
        Map<String, Integer> statistics = new HashMap<>();

        statistics.put("TIME_CONFLICTS", result.getTimeConflicts() != null ? result.getTimeConflicts().size() : 0);
        statistics.put("SKILL_CONFLICTS", result.getSkillConflicts() != null ? result.getSkillConflicts().size() : 0);
        statistics.put("WORK_HOUR_CONFLICTS",
                result.getWorkHourConflicts() != null ? result.getWorkHourConflicts().size() : 0);
        statistics.put("CAPACITY_CONFLICTS",
                result.getCapacityConflicts() != null ? result.getCapacityConflicts().size() : 0);
        statistics.put("OTHER_CONFLICTS", result.getOtherConflicts() != null ? result.getOtherConflicts().size() : 0);

        result.setConflictTypeStatistics(statistics);
    }

    // ==================== 技能冲突辅助方法 ====================

    /**
     * 获取班次所需的技能
     */
    private List<String> getRequiredSkillsForShift(Long shiftId, ScheduleData scheduleData) {
        if (scheduleData == null || scheduleData.getShiftRequirements() == null) {
            return new ArrayList<>();
        }

        Map<Long, List<String>> shiftRequirements = scheduleData.getShiftRequirements();
        return shiftRequirements.getOrDefault(shiftId, new ArrayList<>());
    }

    /**
     * 获取员工具备的技能
     */
    private List<String> getEmployeeSkills(Long employeeId, ScheduleData scheduleData) {
        if (scheduleData == null || scheduleData.getEmployeeSkills() == null) {
            return new ArrayList<>();
        }

        Map<Long, List<String>> employeeSkills = scheduleData.getEmployeeSkills();
        return employeeSkills.getOrDefault(employeeId, new ArrayList<>());
    }

    /**
     * 计算技能冲突严重程度
     */
    private Integer calculateSkillConflictSeverity(List<String> requiredSkills, List<String> missingSkills) {
        if (missingSkills.isEmpty()) {
            return 0;
        }

        // 缺失所有技能 = 最严重（5级）
        if (missingSkills.size() == requiredSkills.size()) {
            return 5;
        }

        // 缺失一半以上技能 = 严重（4级）
        if (missingSkills.size() >= requiredSkills.size() / 2.0) {
            return 4;
        }

        // 缺少核心技能（假设前3个为核心）= 中等（3级）
        if (!missingSkills.isEmpty() && requiredSkills.indexOf(missingSkills.get(0)) < 3) {
            return 3;
        }

        // 缺少一般技能 = 轻微（2级）
        return 2;
    }

    // ==================== 容量冲突辅助方法 ====================

    /**
     * 获取班次容量限制
     */
    private Integer getShiftCapacity(Long shiftId, ScheduleData scheduleData) {
        if (scheduleData == null || scheduleData.getShiftCapacities() == null) {
            return null;
        }

        Map<Long, Integer> shiftCapacities = scheduleData.getShiftCapacities();
        return shiftCapacities.get(shiftId);
    }

    /**
     * 计算容量冲突严重程度
     */
    private Integer calculateCapacityConflictSeverity(int assignedCount, int capacityLimit) {
        int overloadPercent = (int) (((assignedCount - capacityLimit) * 100.0) / capacityLimit);

        // 超载50%以上 = 最严重（5级）
        if (overloadPercent >= 50) {
            return 5;
        }

        // 超载30%以上 = 严重（4级）
        if (overloadPercent >= 30) {
            return 4;
        }

        // 超载10%以上 = 中等（3级）
        if (overloadPercent >= 10) {
            return 3;
        }

        // 超载10%以下 = 轻微（2级）
        return 2;
    }
}
