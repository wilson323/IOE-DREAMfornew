package net.lab1024.sa.attendance.strategy.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.entity.WorkShiftEntity;
import net.lab1024.sa.attendance.strategy.WorkTimeCalculateStrategy;
import net.lab1024.sa.attendance.strategy.WorkTimeCalculateStrategyFactory;
import net.lab1024.sa.attendance.strategy.model.CalculateContext;
import net.lab1024.sa.attendance.strategy.model.CalculateResult;
import net.lab1024.sa.attendance.strategy.model.PunchRecord;
import net.lab1024.sa.attendance.strategy.model.PunchRecord.PunchType;
import net.lab1024.sa.attendance.strategy.model.WorkTimeSpan;
import net.lab1024.sa.attendance.strategy.model.WorkTimeSpan.TimeSpanType;

/**
 * 标准工时制计算策略
 * <p>
 * 适用于固定上下班时间的班次：
 * - 白班（DAY_SHIFT）
 * - 夜班（NIGHT_SHIFT）
 * - 兼职班（PART_TIME_SHIFT）
 * - 特殊班（SPECIAL_SHIFT）
 * </p>
 *
 * <p>核心计算规则：</p>
 * <ul>
 *   <li>上班时间：固定时间（如09:00）</li>
 *   <li>下班时间：固定时间（如18:00）</li>
 *   <li>迟到：上班打卡时间晚于规定时间</li>
 *   <li>早退：下班打卡时间早于规定时间</li>
 *   <li>加班：下班打卡时间晚于加班开始时间</li>
 * </ul>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Component
@Tag(name = "标准工时制计算策略", description = "固定上下班时间的工时计算策略")
public class StandardWorkTimeStrategy implements WorkTimeCalculateStrategy {

    /**
     * 默认允许提前打卡时长（分钟）
     */
    private static final Integer DEFAULT_EARLY_PUNCH_MINUTES = 120;

    /**
     * 默认允许延后打卡时长（分钟）
     */
    private static final Integer DEFAULT_LATE_PUNCH_MINUTES = 120;

    @Override
    public String getStrategyType() {
        return WorkTimeCalculateStrategyFactory.STRATEGY_STANDARD;
    }

    @Override
    public String getStrategyName() {
        return "标准工时制";
    }

    @Override
    public CalculateResult calculate(CalculateContext context) {
        log.info("[标准工时策略] 开始计算工时: employeeId={}, attendanceDate={}, shiftName={}",
                context.getEmployeeId(), context.getAttendanceDate(), context.getWorkShift().getShiftName());

        try {
            // 1. 验证打卡记录
            List<PunchRecord> punchRecords = context.getPunchRecords();
            boolean isValid = validatePunchRecords(punchRecords, context.getWorkShift());

            if (!isValid) {
                return CalculateResult.builder()
                        .employeeId(context.getEmployeeId())
                        .attendanceDate(context.getAttendanceDate())
                        .scheduleId(context.getScheduleId())
                        .shiftId(context.getWorkShift().getShiftId())
                        .shiftName(context.getWorkShift().getShiftName())
                        .isValid(false)
                        .validationErrorMessage("打卡记录无效或不完整")
                        .attendanceStatus(CalculateResult.AttendanceStatus.INVALID)
                        .calculatedAt(System.currentTimeMillis())
                        .build();
            }

            // 2. 分离上班和下班打卡记录
            Map<PunchType, List<PunchRecord>> groupedRecords = punchRecords.stream()
                    .collect(Collectors.groupingBy(PunchRecord::getPunchType));

            List<PunchRecord> clockInRecords = groupedRecords.getOrDefault(PunchType.IN, new ArrayList<>());
            List<PunchRecord> clockOutRecords = groupedRecords.getOrDefault(PunchType.OUT, new ArrayList<>());

            // 3. 获取有效的上班和下班打卡时间
            LocalDateTime clockInTime = getClockInTime(clockInRecords, context);
            LocalDateTime clockOutTime = getClockOutTime(clockOutRecords, context);

            if (clockInTime == null || clockOutTime == null) {
                return CalculateResult.builder()
                        .employeeId(context.getEmployeeId())
                        .attendanceDate(context.getAttendanceDate())
                        .scheduleId(context.getScheduleId())
                        .shiftId(context.getWorkShift().getShiftId())
                        .shiftName(context.getWorkShift().getShiftName())
                        .isValid(false)
                        .validationErrorMessage("缺少必要的打卡记录")
                        .attendanceStatus(CalculateResult.AttendanceStatus.INVALID)
                        .calculatedAt(System.currentTimeMillis())
                        .build();
            }

            // 4. 计算迟到和早退
            Integer lateMinutes = calculateLateMinutes(clockInTime, context.getWorkShift());
            Integer earlyLeaveMinutes = calculateEarlyLeaveMinutes(clockOutTime, context.getWorkShift());

            // 5. 计算加班时长
            Integer overtimeMinutes = calculateOvertimeMinutes(clockOutTime, context.getWorkShift());

            // 6. 计算工作时段
            List<WorkTimeSpan> workTimeSpans = calculateWorkTimeSpans(punchRecords, context.getWorkShift());

            // 7. 计算实际工作时长
            Integer totalWorkMinutes = calculateTotalWorkMinutes(workTimeSpans);

            // 8. 计算标准工作时长
            Integer standardWorkMinutes = calculateCoreWorkMinutes(context.getWorkShift());

            // 9. 确定考勤状态
            CalculateResult.AttendanceStatus status = determineAttendanceStatus(
                    lateMinutes, earlyLeaveMinutes, overtimeMinutes, totalWorkMinutes, standardWorkMinutes);

            // 10. 构建计算结果
            return CalculateResult.builder()
                    .employeeId(context.getEmployeeId())
                    .attendanceDate(context.getAttendanceDate())
                    .scheduleId(context.getScheduleId())
                    .shiftId(context.getWorkShift().getShiftId())
                    .shiftName(context.getWorkShift().getShiftName())
                    .actualWorkHours(CalculateResult.calculateWorkHours(totalWorkMinutes))
                    .standardWorkHours(CalculateResult.calculateWorkHours(standardWorkMinutes))
                    .lateMinutes(lateMinutes)
                    .earlyLeaveMinutes(earlyLeaveMinutes)
                    .overtimeHours(CalculateResult.calculateWorkHours(overtimeMinutes))
                    .workTimeSpans(workTimeSpans)
                    .breakMinutes(calculateBreakMinutes(context.getWorkShift()))
                    .coreWorkHours(CalculateResult.calculateWorkHours(standardWorkMinutes))
                    .isOvernight(isOvernightShift(context.getWorkShift()))
                    .firstPunchTime(clockInTime)
                    .lastPunchTime(clockOutTime)
                    .punchCount(punchRecords.size())
                    .attendanceStatus(status)
                    .isValid(true)
                    .calculatedAt(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("[标准工时策略] 计算异常: employeeId={}, error={}",
                    context.getEmployeeId(), e.getMessage(), e);
            return CalculateResult.builder()
                    .employeeId(context.getEmployeeId())
                    .attendanceDate(context.getAttendanceDate())
                    .scheduleId(context.getScheduleId())
                    .shiftId(context.getWorkShift().getShiftId())
                    .shiftName(context.getWorkShift().getShiftName())
                    .isValid(false)
                    .validationErrorMessage("计算异常: " + e.getMessage())
                    .attendanceStatus(CalculateResult.AttendanceStatus.INVALID)
                    .calculatedAt(System.currentTimeMillis())
                    .build();
        }
    }

    @Override
    public boolean validatePunchRecords(List<PunchRecord> punchRecords, WorkShiftEntity workShift) {
        if (punchRecords == null || punchRecords.isEmpty()) {
            log.warn("[标准工时策略] 打卡记录为空");
            return false;
        }

        // 检查是否有上班打卡
        boolean hasClockIn = punchRecords.stream()
                .anyMatch(r -> r.getPunchType() == PunchType.IN);

        // 检查是否有下班打卡
        boolean hasClockOut = punchRecords.stream()
                .anyMatch(r -> r.getPunchType() == PunchType.OUT);

        // 检查打卡记录是否有效
        boolean allValid = punchRecords.stream()
                .allMatch(PunchRecord::isValid);

        boolean isValid = hasClockIn && hasClockOut && allValid;

        if (!isValid) {
            log.warn("[标准工时策略] 打卡记录验证失败: hasClockIn={}, hasClockOut={}, allValid={}",
                    hasClockIn, hasClockOut, allValid);
        }

        return isValid;
    }

    @Override
    public Integer calculateLateMinutes(LocalDateTime punchTime, WorkShiftEntity workShift) {
        if (punchTime == null || workShift == null || workShift.getWorkStartTime() == null) {
            return 0;
        }

        // 构建标准的上班时间
        LocalDate punchDate = punchTime.toLocalDate();
        LocalDateTime standardStartTime = LocalDateTime.of(punchDate, workShift.getWorkStartTime());

        // 计算迟到时长（分钟）
        long lateMinutes = java.time.Duration.between(standardStartTime, punchTime).toMinutes();

        // 如果打卡时间早于或等于上班时间，不算迟到
        if (lateMinutes <= 0) {
            return 0;
        }

        log.debug("[标准工时策略] 计算迟到: punchTime={}, standardTime={}, lateMinutes={}",
                punchTime, standardStartTime, lateMinutes);

        return (int) lateMinutes;
    }

    @Override
    public Integer calculateEarlyLeaveMinutes(LocalDateTime punchTime, WorkShiftEntity workShift) {
        if (punchTime == null || workShift == null || workShift.getWorkEndTime() == null) {
            return 0;
        }

        // 判断是否跨天班次
        LocalDate punchDate = punchTime.toLocalDate();
        LocalDate endDate = isOvernightShift(workShift) ? punchDate.plusDays(1) : punchDate;
        LocalDateTime standardEndTime = LocalDateTime.of(endDate, workShift.getWorkEndTime());

        // 计算早退时长（分钟）
        long earlyLeaveMinutes = java.time.Duration.between(punchTime, standardEndTime).toMinutes();

        // 如果下班打卡时间晚于或等于下班时间，不算早退
        if (earlyLeaveMinutes <= 0) {
            return 0;
        }

        log.debug("[标准工时策略] 计算早退: punchTime={}, standardTime={}, earlyLeaveMinutes={}",
                punchTime, standardEndTime, earlyLeaveMinutes);

        return (int) earlyLeaveMinutes;
    }

    @Override
    public Integer calculateOvertimeMinutes(LocalDateTime endTime, WorkShiftEntity workShift) {
        if (endTime == null || workShift == null) {
            return 0;
        }

        // 获取加班开始时间
        LocalTime overtimeStart = workShift.getWorkStartTime();
        if (overtimeStart == null) {
            // 如果没有配置加班开始时间，使用下班时间
            overtimeStart = workShift.getWorkEndTime();
        }

        // 判断是否跨天班次
        LocalDate endDate = endTime.toLocalDate();
        LocalDate overtimeStartDate = isOvernightShift(workShift) ? endDate.minusDays(1) : endDate;
        LocalDateTime overtimeStartTime = LocalDateTime.of(overtimeStartDate, overtimeStart);

        // 计算加班时长（分钟）
        long overtimeMinutes = java.time.Duration.between(overtimeStartTime, endTime).toMinutes();

        // 如果下班打卡时间早于加班开始时间，不算加班
        if (overtimeMinutes <= 0) {
            return 0;
        }

        // 检查最小加班时长
        Integer minOvertimeMinutes = workShift.getMinOvertimeDuration();
        if (minOvertimeMinutes != null && overtimeMinutes < minOvertimeMinutes) {
            log.debug("[标准工时策略] 加班时长不足最小要求: overtimeMinutes={}, minOvertimeMinutes={}",
                    overtimeMinutes, minOvertimeMinutes);
            return 0;
        }

        log.debug("[标准工时策略] 计算加班: endTime={}, overtimeStartTime={}, overtimeMinutes={}",
                endTime, overtimeStartTime, overtimeMinutes);

        return (int) overtimeMinutes;
    }

    @Override
    public List<WorkTimeSpan> calculateWorkTimeSpans(List<PunchRecord> punchRecords, WorkShiftEntity workShift) {
        List<WorkTimeSpan> workTimeSpans = new ArrayList<>();

        // 按时间排序打卡记录
        List<PunchRecord> sortedRecords = punchRecords.stream()
                .sorted(Comparator.comparing(PunchRecord::getPunchTime))
                .collect(Collectors.toList());

        LocalDateTime startTime = null;
        for (int i = 0; i < sortedRecords.size(); i++) {
            PunchRecord record = sortedRecords.get(i);

            if (record.getPunchType() == PunchType.IN) {
                startTime = record.getPunchTime();
            } else if (record.getPunchType() == PunchType.OUT && startTime != null) {
                // 构建工作时段
                int workMinutes = (int) java.time.Duration.between(startTime, record.getPunchTime()).toMinutes();
                workTimeSpans.add(WorkTimeSpan.builder()
                        .spanId(workTimeSpans.size() + 1)
                        .startTime(startTime)
                        .endTime(record.getPunchTime())
                        .workMinutes(workMinutes)
                        .isValid(true)
                        .timeSpanType(TimeSpanType.NORMAL)
                        .build());
                startTime = null;
            }
        }

        return workTimeSpans;
    }

    @Override
    public boolean isValidWorkTime(LocalDateTime punchTime, WorkShiftEntity workShift) {
        if (punchTime == null || workShift == null) {
            return false;
        }

        WorkTimeSpan allowedRange = getAllowedPunchTimeRange(workShift);
        return allowedRange.containsTime(punchTime);
    }

    @Override
    public WorkTimeSpan getAllowedPunchTimeRange(WorkShiftEntity workShift) {
        if (workShift == null || workShift.getWorkStartTime() == null || workShift.getWorkEndTime() == null) {
            return null;
        }

        LocalDate today = LocalDate.now();

        // 允许提前打卡
        LocalDateTime earlyPunchTime = LocalDateTime.of(today, workShift.getWorkStartTime())
                .minusMinutes(DEFAULT_EARLY_PUNCH_MINUTES);

        // 允许延后打卡
        LocalDate endDate = isOvernightShift(workShift) ? today.plusDays(1) : today;
        LocalDateTime latePunchTime = LocalDateTime.of(endDate, workShift.getWorkEndTime())
                .plusMinutes(DEFAULT_LATE_PUNCH_MINUTES);

        return WorkTimeSpan.builder()
                .startTime(earlyPunchTime)
                .endTime(latePunchTime)
                .isValid(true)
                .build();
    }

    @Override
    public Integer calculateCoreWorkMinutes(WorkShiftEntity workShift) {
        if (workShift == null || workShift.getWorkDuration() == null) {
            // 默认8小时工作制
            return 480;
        }
        return (int) (workShift.getWorkDuration() * 60);
    }

    @Override
    public Integer calculateBreakMinutes(WorkShiftEntity workShift) {
        if (workShift == null || workShift.getBreakTime() == null) {
            return 0;
        }
        return workShift.getBreakTime();
    }

    @Override
    public boolean isOvernightShift(WorkShiftEntity workShift) {
        if (workShift == null || workShift.getWorkStartTime() == null || workShift.getWorkEndTime() == null) {
            return false;
        }

        // 如果已配置跨天标识，直接返回
        if (workShift.getIsCrossDay() != null) {
            return workShift.getIsCrossDay() == 1;
        }

        // 否则根据开始时间和结束时间判断
        return workShift.getWorkEndTime().isBefore(workShift.getWorkStartTime());
    }

    @Override
    public LocalDateTime calculateOvernightEndTime(LocalDateTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        // 跨天班次的结束时间在第二天
        return LocalDateTime.of(startTime.toLocalDate().plusDays(1), endTime);
    }

    /**
     * 获取上班打卡时间
     *
     * @param clockInRecords 上班打卡记录列表
     * @param context        计算上下文
     * @return 上班打卡时间
     */
    private LocalDateTime getClockInTime(List<PunchRecord> clockInRecords, CalculateContext context) {
        if (clockInRecords == null || clockInRecords.isEmpty()) {
            return null;
        }

        // 取第一次有效的上班打卡
        return clockInRecords.stream()
                .filter(PunchRecord::isValid)
                .min(Comparator.comparing(PunchRecord::getPunchTime))
                .map(PunchRecord::getPunchTime)
                .orElse(null);
    }

    /**
     * 获取下班打卡时间
     *
     * @param clockOutRecords 下班打卡记录列表
     * @param context         计算上下文
     * @return 下班打卡时间
     */
    private LocalDateTime getClockOutTime(List<PunchRecord> clockOutRecords, CalculateContext context) {
        if (clockOutRecords == null || clockOutRecords.isEmpty()) {
            return null;
        }

        // 取最后一次有效的下班打卡
        return clockOutRecords.stream()
                .filter(PunchRecord::isValid)
                .max(Comparator.comparing(PunchRecord::getPunchTime))
                .map(PunchRecord::getPunchTime)
                .orElse(null);
    }

    /**
     * 计算总工作时长（分钟）
     *
     * @param workTimeSpans 工作时段列表
     * @return 总工作时长（分钟）
     */
    private Integer calculateTotalWorkMinutes(List<WorkTimeSpan> workTimeSpans) {
        if (workTimeSpans == null || workTimeSpans.isEmpty()) {
            return 0;
        }

        return workTimeSpans.stream()
                .map(WorkTimeSpan::calculateNetWorkMinutes)
                .reduce(0, Integer::sum);
    }

    /**
     * 确定考勤状态
     *
     * @param lateMinutes        迟到时长
     * @param earlyLeaveMinutes  早退时长
     * @param overtimeMinutes    加班时长
     * @param totalWorkMinutes   总工作时长
     * @param standardWorkMinutes 标准工作时长
     * @return 考勤状态
     */
    private CalculateResult.AttendanceStatus determineAttendanceStatus(
            Integer lateMinutes, Integer earlyLeaveMinutes, Integer overtimeMinutes,
            Integer totalWorkMinutes, Integer standardWorkMinutes) {

        boolean isLate = lateMinutes != null && lateMinutes > 0;
        boolean isEarlyLeave = earlyLeaveMinutes != null && earlyLeaveMinutes > 0;
        boolean hasOvertime = overtimeMinutes != null && overtimeMinutes > 0;

        if (isLate && isEarlyLeave) {
            return CalculateResult.AttendanceStatus.LATE_AND_EARLY_LEAVE;
        } else if (isLate) {
            return CalculateResult.AttendanceStatus.LATE;
        } else if (isEarlyLeave) {
            return CalculateResult.AttendanceStatus.EARLY_LEAVE;
        } else if (hasOvertime) {
            return CalculateResult.AttendanceStatus.OVERTIME;
        } else {
            return CalculateResult.AttendanceStatus.NORMAL;
        }
    }
}
