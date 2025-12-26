package net.lab1024.sa.attendance.strategy.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.attendance.WorkShiftEntity;
import net.lab1024.sa.attendance.strategy.WorkTimeCalculateStrategy;
import net.lab1024.sa.attendance.strategy.WorkTimeCalculateStrategyFactory;
import net.lab1024.sa.attendance.strategy.model.CalculateContext;
import net.lab1024.sa.attendance.strategy.model.CalculateResult;
import net.lab1024.sa.attendance.strategy.model.PunchRecord;
import net.lab1024.sa.attendance.strategy.model.PunchRecord.PunchType;
import net.lab1024.sa.attendance.strategy.model.WorkTimeSpan;
import net.lab1024.sa.attendance.strategy.model.WorkTimeSpan.TimeSpanType;

/**
 * 弹性工作制计算策略
 * <p>
 * 适用于弹性工作时间的班次：
 * - 核心工作时段：必须在场（如10:00-16:00）
 * - 弹性上班时间：可提前或延后（如07:00-10:00）
 * - 弹性下班时间：可提前或延后（如16:00-20:00）
 * - 满足核心工作时长即可
 * </p>
 *
 * <p>核心计算规则：</p>
 * <ul>
 *   <li>核心工作时段：必须打卡（如10:00-16:00）</li>
 *   <li>弹性时段：灵活打卡（如07:00-10:00和16:00-20:00）</li>
 *   <li>迟到：核心时段开始后打卡</li>
 *   <li>早退：核心时段结束前离开</li>
 *   <li>加班：超过弹性时段下班</li>
 * </ul>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Component
@Tag(name = "弹性工作制计算策略", description = "弹性上下班时间的工时计算策略")
public class FlexibleWorkTimeStrategy implements WorkTimeCalculateStrategy {

    /**
     * 默认核心工作时长（小时）
     */
    private static final Integer DEFAULT_CORE_WORK_HOURS = 6;

    /**
     * 默认弹性开始时长（分钟）
     */
    private static final Integer DEFAULT_FLEXIBLE_START_MINUTES = 180; // 3小时

    /**
     * 默认弹性结束时长（分钟）
     */
    private static final Integer DEFAULT_FLEXIBLE_END_MINUTES = 240; // 4小时

    @Override
    public String getStrategyType() {
        return WorkTimeCalculateStrategyFactory.STRATEGY_FLEXIBLE;
    }

    @Override
    public String getStrategyName() {
        return "弹性工作制";
    }

    @Override
    public CalculateResult calculate(CalculateContext context) {
        log.info("[弹性工作制策略] 开始计算工时: employeeId={}, attendanceDate={}, shiftName={}",
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

            // 4. 计算核心时段
            WorkTimeSpan coreTimeSpan = calculateCoreTimeSpan(context);

            // 5. 计算迟到和早退（基于核心时段）
            Integer lateMinutes = calculateLateMinutes(clockInTime, context.getWorkShift());
            Integer earlyLeaveMinutes = calculateEarlyLeaveMinutes(clockOutTime, context.getWorkShift());

            // 6. 计算加班时长
            Integer overtimeMinutes = calculateOvertimeMinutes(clockOutTime, context.getWorkShift());

            // 7. 计算工作时段
            List<WorkTimeSpan> workTimeSpans = calculateWorkTimeSpans(punchRecords, context.getWorkShift());

            // 8. 计算实际工作时长
            Integer totalWorkMinutes = calculateTotalWorkMinutes(workTimeSpans);

            // 8.1. 计算午休时间（用于实际工作时长计算）
            Integer breakMinutes = calculateBreakMinutes(context.getWorkShift());
            Integer actualWorkMinutes = totalWorkMinutes - breakMinutes;

            // 9. 计算标准工作时长（核心工作时长，不扣除午休）
            Integer standardWorkMinutes = calculateCoreWorkMinutes(context.getWorkShift());

            // 10. 计算弹性工作时长（基于扣除午休后的实际工作时长）
            Integer flexibleWorkMinutes = calculateFlexibleWorkMinutes(actualWorkMinutes, standardWorkMinutes);

            // 11. 检查是否满足核心工作时长（使用总工作时长，不扣除午休）
            boolean meetsCoreHours = totalWorkMinutes >= standardWorkMinutes;

            // 12. 确定考勤状态
            CalculateResult.AttendanceStatus status = determineAttendanceStatus(
                    lateMinutes, earlyLeaveMinutes, overtimeMinutes,
                    totalWorkMinutes, standardWorkMinutes, meetsCoreHours);

            // 13. 构建计算结果
            return CalculateResult.builder()
                    .employeeId(context.getEmployeeId())
                    .attendanceDate(context.getAttendanceDate())
                    .scheduleId(context.getScheduleId())
                    .shiftId(context.getWorkShift().getShiftId())
                    .shiftName(context.getWorkShift().getShiftName())
                    .actualWorkHours(CalculateResult.calculateWorkHours(actualWorkMinutes))
                    .standardWorkHours(CalculateResult.calculateWorkHours(standardWorkMinutes))
                    .lateMinutes(lateMinutes)
                    .earlyLeaveMinutes(earlyLeaveMinutes)
                    .overtimeHours(CalculateResult.calculateWorkHours(overtimeMinutes))
                    .workTimeSpans(workTimeSpans)
                    .breakMinutes(breakMinutes)
                    .coreWorkHours(CalculateResult.calculateWorkHours(standardWorkMinutes))
                    .flexibleWorkHours(CalculateResult.calculateWorkHours(flexibleWorkMinutes))
                    .isOvernight(isOvernightShift(context.getWorkShift()))
                    .firstPunchTime(clockInTime)
                    .lastPunchTime(clockOutTime)
                    .punchCount(punchRecords.size())
                    .attendanceStatus(status)
                    .isValid(meetsCoreHours)
                    .calculatedAt(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("[弹性工作制策略] 计算异常: employeeId={}, error={}",
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
            log.warn("[弹性工作制策略] 打卡记录为空");
            return false;
        }

        // 弹性工作制只要求有打卡记录即可
        // 检查打卡记录是否有效
        boolean allValid = punchRecords.stream()
                .allMatch(PunchRecord::isValid);

        if (!allValid) {
            log.warn("[弹性工作制策略] 打卡记录验证失败: allValid={}", allValid);
        }

        return allValid;
    }

    @Override
    public Integer calculateLateMinutes(LocalDateTime punchTime, WorkShiftEntity workShift) {
        if (punchTime == null || workShift == null) {
            return 0;
        }

        // 获取核心时段开始时间
        LocalTime coreStartTime = calculateCoreStartTime(workShift);
        if (coreStartTime == null) {
            return 0;
        }

        // 构建核心时段开始时间
        LocalDate punchDate = punchTime.toLocalDate();
        LocalDateTime coreStartTimeDateTime = LocalDateTime.of(punchDate, coreStartTime);

        // 如果打卡时间晚于核心时段开始时间，计算迟到时长
        if (punchTime.isAfter(coreStartTimeDateTime)) {
            long lateMinutes = java.time.Duration.between(coreStartTimeDateTime, punchTime).toMinutes();
            log.debug("[弹性工作制策略] 计算迟到: punchTime={}, coreStartTime={}, lateMinutes={}",
                    punchTime, coreStartTimeDateTime, lateMinutes);
            return (int) lateMinutes;
        }

        // 弹性时段内打卡不算迟到
        return 0;
    }

    @Override
    public Integer calculateEarlyLeaveMinutes(LocalDateTime punchTime, WorkShiftEntity workShift) {
        if (punchTime == null || workShift == null) {
            return 0;
        }

        // 获取核心时段结束时间
        LocalTime coreEndTime = calculateCoreEndTime(workShift);
        if (coreEndTime == null) {
            return 0;
        }

        // 构建核心时段结束时间
        LocalDate punchDate = punchTime.toLocalDate();
        LocalDateTime coreEndTimeDateTime = LocalDateTime.of(punchDate, coreEndTime);

        // 如果下班打卡时间早于核心时段结束时间，计算早退时长
        if (punchTime.isBefore(coreEndTimeDateTime)) {
            long earlyLeaveMinutes = java.time.Duration.between(punchTime, coreEndTimeDateTime).toMinutes();
            log.debug("[弹性工作制策略] 计算早退: punchTime={}, coreEndTime={}, earlyLeaveMinutes={}",
                    punchTime, coreEndTimeDateTime, earlyLeaveMinutes);
            return (int) earlyLeaveMinutes;
        }

        // 弹性时段内下班不算早退
        return 0;
    }

    @Override
    public Integer calculateOvertimeMinutes(LocalDateTime endTime, WorkShiftEntity workShift) {
        if (endTime == null || workShift == null) {
            return 0;
        }

        // 获取标准下班时间（用于加班计算）
        LocalTime standardEndTime = workShift.getWorkEndTime();
        if (standardEndTime == null) {
            return 0;
        }

        // 构建标准下班时间
        LocalDate endDate = endTime.toLocalDate();
        LocalDateTime standardEndTimeDateTime = LocalDateTime.of(endDate, standardEndTime);

        // 如果下班打卡时间晚于标准下班时间，计算加班时长
        if (endTime.isAfter(standardEndTimeDateTime)) {
            long overtimeMinutes = java.time.Duration.between(standardEndTimeDateTime, endTime).toMinutes();

            // 检查最小加班时长
            Integer minOvertimeMinutes = workShift.getMinOvertimeDuration();
            if (minOvertimeMinutes != null && overtimeMinutes < minOvertimeMinutes) {
                log.debug("[弹性工作制策略] 加班时长不足最小要求: overtimeMinutes={}, minOvertimeMinutes={}",
                        overtimeMinutes, minOvertimeMinutes);
                return 0;
            }

            log.debug("[弹性工作制策略] 计算加班: endTime={}, standardEndTime={}, overtimeMinutes={}",
                    endTime, standardEndTimeDateTime, overtimeMinutes);
            return (int) overtimeMinutes;
        }

        return 0;
    }

    @Override
    public List<WorkTimeSpan> calculateWorkTimeSpans(List<PunchRecord> punchRecords, WorkShiftEntity workShift) {
        List<WorkTimeSpan> workTimeSpans = new ArrayList<>();

        // 按时间排序打卡记录
        List<PunchRecord> sortedRecords = punchRecords.stream()
                .sorted(Comparator.comparing(PunchRecord::getPunchTime))
                .collect(Collectors.toList());

        LocalDateTime startTime = null;
        for (PunchRecord record : sortedRecords) {
            if (record.getPunchType() == PunchType.IN) {
                startTime = record.getPunchTime();
            } else if (record.getPunchType() == PunchType.OUT && startTime != null) {
                // 判断是否为核心时段
                boolean isCoreTime = isCoreTimeSpan(startTime, record.getPunchTime(), workShift);

                int workMinutes = (int) java.time.Duration.between(startTime, record.getPunchTime()).toMinutes();

                workTimeSpans.add(WorkTimeSpan.builder()
                        .spanId(workTimeSpans.size() + 1)
                        .startTime(startTime)
                        .endTime(record.getPunchTime())
                        .workMinutes(workMinutes)
                        .isValid(true)
                        .timeSpanType(isCoreTime ? TimeSpanType.NORMAL : TimeSpanType.FLEXIBLE)
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
        return allowedRange != null && allowedRange.containsTime(punchTime);
    }

    @Override
    public WorkTimeSpan getAllowedPunchTimeRange(WorkShiftEntity workShift) {
        if (workShift == null || workShift.getWorkStartTime() == null || workShift.getWorkEndTime() == null) {
            return null;
        }

        LocalDate today = LocalDate.now();

        // 弹性时段范围
        Integer flexibleStartMinutes = calculateFlexibleStartMinutes(workShift) != null
                ? calculateFlexibleStartMinutes(workShift)
                : DEFAULT_FLEXIBLE_START_MINUTES;

        Integer flexibleEndMinutes = calculateFlexibleEndMinutes(workShift) != null
                ? calculateFlexibleEndMinutes(workShift)
                : DEFAULT_FLEXIBLE_END_MINUTES;

        // 允许的打卡时间范围
        LocalDateTime earlyPunchTime = LocalDateTime.of(today, workShift.getWorkStartTime())
                .minusMinutes(flexibleStartMinutes);

        LocalDate endDate = isOvernightShift(workShift) ? today.plusDays(1) : today;
        LocalDateTime latePunchTime = LocalDateTime.of(endDate, workShift.getWorkEndTime())
                .plusMinutes(flexibleEndMinutes);

        return WorkTimeSpan.builder()
                .startTime(earlyPunchTime)
                .endTime(latePunchTime)
                .isValid(true)
                .build();
    }

    @Override
    public Integer calculateCoreWorkMinutes(WorkShiftEntity workShift) {
        if (workShift == null || workShift.getWorkDuration() == null) {
            // 默认核心工作时长（6小时）
            return DEFAULT_CORE_WORK_HOURS * 60;
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
     * 计算核心时段
     *
     * @param context 计算上下文
     * @return 核心时段
     */
    private WorkTimeSpan calculateCoreTimeSpan(CalculateContext context) {
        LocalTime coreStartTime = calculateCoreStartTime(context.getWorkShift());
        LocalTime coreEndTime = calculateCoreEndTime(context.getWorkShift());

        if (coreStartTime == null || coreEndTime == null) {
            return null;
        }

        LocalDate date = context.getAttendanceDate() != null
                ? context.getAttendanceDate()
                : LocalDate.now();

        return WorkTimeSpan.builder()
                .startTime(LocalDateTime.of(date, coreStartTime))
                .endTime(LocalDateTime.of(date, coreEndTime))
                .isValid(true)
                .timeSpanType(TimeSpanType.NORMAL)
                .build();
    }

    /**
     * 计算核心时段开始时间
     *
     * @param workShift 班次配置
     * @return 核心时段开始时间
     */
    private LocalTime calculateCoreStartTime(WorkShiftEntity workShift) {
        if (workShift == null || workShift.getWorkStartTime() == null) {
            return null;
        }

        // 如果配置了弹性开始时间，核心时段开始时间为上班时间+弹性时长
        Integer flexibleStartMinutes = calculateFlexibleStartMinutes(workShift);
        if (flexibleStartMinutes != null && flexibleStartMinutes > 0) {
            return workShift.getWorkStartTime().plusMinutes(flexibleStartMinutes);
        }

        // 否则使用默认弹性时长
        return workShift.getWorkStartTime().plusMinutes(DEFAULT_FLEXIBLE_START_MINUTES);
    }

    /**
     * 计算核心时段结束时间
     *
     * @param workShift 班次配置
     * @return 核心时段结束时间
     */
    private LocalTime calculateCoreEndTime(WorkShiftEntity workShift) {
        if (workShift == null || workShift.getWorkEndTime() == null) {
            return null;
        }

        // 如果配置了弹性结束时间，核心时段结束时间为下班时间-弹性时长
        Integer flexibleEndMinutes = calculateFlexibleEndMinutes(workShift);
        if (flexibleEndMinutes != null && flexibleEndMinutes > 0) {
            return workShift.getWorkEndTime().minusMinutes(flexibleEndMinutes);
        }

        // 否则使用默认弹性时长
        return workShift.getWorkEndTime().minusMinutes(DEFAULT_FLEXIBLE_END_MINUTES);
    }

    /**
     * 计算弹性时段结束时间
     *
     * @param workShift 班次配置
     * @return 弹性时段结束时间
     */
    private LocalTime calculateFlexibleEndTime(WorkShiftEntity workShift) {
        if (workShift == null || workShift.getWorkEndTime() == null) {
            return null;
        }

        // 弹性时段结束时间 = 核心时段结束时间 + 弹性时长
        Integer flexibleEndMinutes = calculateFlexibleEndMinutes(workShift);
        if (flexibleEndMinutes != null && flexibleEndMinutes > 0) {
            return workShift.getWorkEndTime().plusMinutes(flexibleEndMinutes);
        }

        // 否则使用默认弹性时长
        return workShift.getWorkEndTime().plusMinutes(DEFAULT_FLEXIBLE_END_MINUTES);
    }

    /**
     * 判断是否为核心时段
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param workShift 班次配置
     * @return 是否为核心时段
     */
    private boolean isCoreTimeSpan(LocalDateTime startTime, LocalDateTime endTime, WorkShiftEntity workShift) {
        LocalTime coreStartTime = calculateCoreStartTime(workShift);
        LocalTime coreEndTime = calculateCoreEndTime(workShift);

        if (coreStartTime == null || coreEndTime == null) {
            return false;
        }

        // 检查是否完全在核心时段内
        LocalDate date = startTime.toLocalDate();
        LocalDateTime coreStart = LocalDateTime.of(date, coreStartTime);
        LocalDateTime coreEnd = LocalDateTime.of(date, coreEndTime);

        return !startTime.isBefore(coreStart) && !endTime.isAfter(coreEnd);
    }

    /**
     * 计算弹性工作时长（超出标准工作时长的部分）
     *
     * @param totalWorkMinutes    总工作时长
     * @param standardWorkMinutes 标准工作时长
     * @return 弹性工作时长
     */
    private Integer calculateFlexibleWorkMinutes(Integer totalWorkMinutes, Integer standardWorkMinutes) {
        if (totalWorkMinutes == null || standardWorkMinutes == null) {
            return 0;
        }
        return Math.max(0, totalWorkMinutes - standardWorkMinutes);
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
     * @param meetsCoreHours     是否满足核心工作时长
     * @return 考勤状态
     */
    private CalculateResult.AttendanceStatus determineAttendanceStatus(
            Integer lateMinutes, Integer earlyLeaveMinutes, Integer overtimeMinutes,
            Integer totalWorkMinutes, Integer standardWorkMinutes, boolean meetsCoreHours) {

        // 如果不满足核心工作时长，标记为缺勤
        if (!meetsCoreHours) {
            return CalculateResult.AttendanceStatus.ABSENT;
        }

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

    /**
     * 计算弹性开始时长（分钟）
     *
     * @param workShift 班次配置
     * @return 弹性开始时长（分钟）
     */
    private Integer calculateFlexibleStartMinutes(WorkShiftEntity workShift) {
        if (workShift == null || workShift.getWorkStartTime() == null) {
            return DEFAULT_FLEXIBLE_START_MINUTES;
        }
        if (workShift.getFlexStartEarliest() != null && workShift.getFlexStartLatest() != null) {
            // 计算最早和最晚开始时间之间的分钟数
            return (int) ChronoUnit.MINUTES.between(
                workShift.getWorkStartTime(),
                workShift.getFlexStartLatest()
            );
        }
        return DEFAULT_FLEXIBLE_START_MINUTES;
    }

    /**
     * 计算弹性结束时长（分钟）
     *
     * @param workShift 班次配置
     * @return 弹性结束时长（分钟）
     */
    private Integer calculateFlexibleEndMinutes(WorkShiftEntity workShift) {
        if (workShift == null || workShift.getWorkEndTime() == null) {
            return DEFAULT_FLEXIBLE_END_MINUTES;
        }
        if (workShift.getFlexEndEarliest() != null && workShift.getFlexEndLatest() != null) {
            // 计算最早和最晚结束时间之间的分钟数
            return (int) ChronoUnit.MINUTES.between(
                workShift.getFlexEndEarliest(),
                workShift.getWorkEndTime()
            );
        }
        return DEFAULT_FLEXIBLE_END_MINUTES;
    }
}
