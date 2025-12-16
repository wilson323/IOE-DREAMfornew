package net.lab1024.sa.attendance.engine.conflict.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.conflict.*;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private final Map<String, Integer> detectionStatistics = new ConcurrentHashMap<>();

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
    public EmployeeConflictResult detectEmployeeConflicts(Long employeeId, List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData) {
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
                        .scheduleRecordId1(scheduleRecord.getId())
                        .scheduleRecordId2(existing.getId())
                        .shiftId1(scheduleRecord.getShiftId())
                        .shiftId2(existing.getShiftId())
                        .conflictStartTime1(scheduleRecord.getStartTime())
                        .conflictEndTime1(scheduleRecord.getEndTime())
                        .conflictStartTime2(existing.getStartTime())
                        .conflictEndTime2(existing.getEndTime())
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
        SkillConflictResult result = SkillConflictResult.builder()
                .hasConflict(false)
                .build();

        // TODO: 实现技能冲突检测逻辑
        // 检查员工是否具备班次所需的技能

        return result;
    }

    @Override
    public WorkHourConflictResult detectWorkHourConflict(Long employeeId, List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData) {
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
                        .actualWorkHours(entry.getValue())
                        .maxAllowedHours(MAX_WORK_HOURS_PER_DAY)
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
                    .actualWorkHours(weeklyWorkHours)
                    .maxAllowedHours(MAX_WORK_HOURS_PER_WEEK)
                    .severity(3)
                    .status(TimeConflict.ConflictStatus.PENDING)
                    .build();

            result.setConflict(conflict);
            result.setHasConflict(true);
        }

        return result;
    }

    @Override
    public CapacityConflictResult detectCapacityConflict(Long shiftId, List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData) {
        CapacityConflictResult result = CapacityConflictResult.builder()
                .hasConflict(false)
                .build();

        // TODO: 实现班次容量冲突检测逻辑
        // 检查班次的人员数量是否超过容量限制

        return result;
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
            EmployeeConflictResult employeeConflict = detectEmployeeConflicts(entry.getKey(), entry.getValue(), scheduleData);
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
            result.getConflictTypeStatistics().forEach((type, count) ->
                statistics.getConflictTypeStatistics().merge(type, count, Integer::sum));
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
        int calculatedTotal = (conflictResult.getTimeConflicts() != null ? conflictResult.getTimeConflicts().size() : 0) +
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
                            .scheduleRecordId1(record1.getId())
                            .scheduleRecordId2(record2.getId())
                            .shiftId1(record1.getShiftId())
                            .shiftId2(record2.getShiftId())
                            .conflictStartTime1(record1.getStartTime())
                            .conflictEndTime1(record1.getEndTime())
                            .conflictStartTime2(record2.getStartTime())
                            .conflictEndTime2(record2.getEndTime())
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
    private void detectSkillConflicts(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData, ConflictDetectionResult result) {
        // TODO: 实现技能冲突检测逻辑
        // 检查员工是否具备班次所需的技能
    }

    /**
     * 检测工作时长冲突
     */
    private void detectWorkHourConflicts(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData, ConflictDetectionResult result) {
        // 按员工分组
        Map<Long, List<ScheduleRecord>> employeeRecords = scheduleRecords.stream()
                .collect(Collectors.groupingBy(ScheduleRecord::getEmployeeId));

        for (Map.Entry<Long, List<ScheduleRecord>> entry : employeeRecords.entrySet()) {
            WorkHourConflictResult workHourConflict = detectWorkHourConflict(entry.getKey(), entry.getValue(), scheduleData);
            if (workHourConflict.getHasConflict()) {
                result.addWorkHourConflict(workHourConflict.getConflict());
            }
        }
    }

    /**
     * 检测班次容量冲突
     */
    private void detectCapacityConflicts(List<ScheduleRecord> scheduleRecords, ScheduleData scheduleData, ConflictDetectionResult result) {
        // 按班次分组
        Map<Long, List<ScheduleRecord>> shiftRecords = scheduleRecords.stream()
                .collect(Collectors.groupingBy(ScheduleRecord::getShiftId));

        for (Map.Entry<Long, List<ScheduleRecord>> entry : shiftRecords.entrySet()) {
            CapacityConflictResult capacityConflict = detectCapacityConflict(entry.getKey(), entry.getValue(), scheduleData);
            if (capacityConflict.getHasConflict()) {
                result.addCapacityConflict(capacityConflict.getConflict());
            }
        }
    }

    /**
     * 检查时间是否重叠
     */
    private boolean isTimeOverlap(ScheduleRecord record1, ScheduleRecord record2) {
        return record1.getStartTime().isBefore(record2.getEndTime()) &&
               record2.getStartTime().isBefore(record1.getEndTime());
    }

    /**
     * 计算重叠时长（分钟）
     */
    private Integer calculateOverlapDuration(ScheduleRecord record1, ScheduleRecord record2) {
        LocalDateTime overlapStart = record1.getStartTime().isAfter(record2.getStartTime()) ?
                                   record1.getStartTime() : record2.getStartTime();
        LocalDateTime overlapEnd = record1.getEndTime().isBefore(record2.getEndTime()) ?
                                 record1.getEndTime() : record2.getEndTime();

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
            String dateKey = record.getStartTime().toLocalDate().toString();
            int workHours = (int) ChronoUnit.HOURS.between(record.getStartTime(), record.getEndTime());
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
        statistics.put("WORK_HOUR_CONFLICTS", result.getWorkHourConflicts() != null ? result.getWorkHourConflicts().size() : 0);
        statistics.put("CAPACITY_CONFLICTS", result.getCapacityConflicts() != null ? result.getCapacityConflicts().size() : 0);
        statistics.put("OTHER_CONFLICTS", result.getOtherConflicts() != null ? result.getOtherConflicts().size() : 0);

        result.setConflictTypeStatistics(statistics);
    }
}