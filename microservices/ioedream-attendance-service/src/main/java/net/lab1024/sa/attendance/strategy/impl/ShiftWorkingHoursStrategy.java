package net.lab1024.sa.attendance.strategy.impl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import net.lab1024.sa.attendance.domain.vo.AttendanceResultVO;
import net.lab1024.sa.common.entity.attendance.AttendanceRecordEntity;
import net.lab1024.sa.common.entity.attendance.WorkShiftEntity;
import net.lab1024.sa.attendance.strategy.IAttendanceRuleStrategy;
import net.lab1024.sa.common.factory.StrategyMarker;

import java.time.LocalTime;

/**
 * 轮班制考勤规则策略
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 验证用户是否按照轮班计划打卡
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Component
@StrategyMarker(name = "SHIFT_WORKING_HOURS", type = "ATTENDANCE_RULE", priority = 80)
@Slf4j
public class ShiftWorkingHoursStrategy implements IAttendanceRuleStrategy {


    @Override
    public String getRuleName() {
        return "轮班制";
    }

    @Override
    public AttendanceResultVO calculate(AttendanceRecordEntity record, Object rule) {
        log.info("[轮班制策略] 开始计算考勤: userId={}, shiftId={}, attendanceType={}, punchTime={}",
            record.getUserId(), record.getShiftId(), record.getAttendanceType(), record.getPunchTime());

        // 1. 参数校验和类型转换
        if (!(rule instanceof WorkShiftEntity)) {
            log.error("[轮班制策略] 规则类型错误: expected=WorkShiftEntity, actual={}",
                rule != null ? rule.getClass().getSimpleName() : "null");
            return createErrorResult(record, "规则类型错误");
        }

        WorkShiftEntity shiftRule = (WorkShiftEntity) rule;

        // 2. 验证班次类型（必须为轮班班次）
        if (shiftRule.getShiftType() == null || shiftRule.getShiftType() != 3) {
            log.warn("[轮班制策略] 班次类型不是轮班制: shiftId={}, shiftType={}",
                shiftRule.getShiftId(), shiftRule.getShiftType());
        }

        // 3. 初始化结果对象
        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(record.getUserId());
        result.setDate(record.getAttendanceDate() != null ? record.getAttendanceDate() : record.getPunchTime().toLocalDate());

        // 4. 根据打卡类型（上班/下班）分别计算
        String attendanceType = record.getAttendanceType();
        if ("CHECK_IN".equals(attendanceType)) {
            // 上班打卡计算（轮班制可能有早班、中班、晚班）
            calculateShiftCheckIn(record, shiftRule, result);
        } else if ("CHECK_OUT".equals(attendanceType)) {
            // 下班打卡计算
            calculateShiftCheckOut(record, shiftRule, result);
        } else {
            log.warn("[轮班制策略] 未知打卡类型: attendanceType={}", attendanceType);
            result.setStatus("UNKNOWN");
            result.setRemark("未知打卡类型");
        }

        // 5. 添加班次信息到备注（仅对正常状态添加班次名称）
        if (!"UNKNOWN".equals(result.getStatus()) && !"ERROR".equals(result.getStatus())) {
            String originalRemark = result.getRemark() != null ? result.getRemark() : "";
            result.setRemark(String.format("[%s] %s", shiftRule.getShiftName(), originalRemark));
        }

        log.info("[轮班制策略] 计算完成: userId={}, shiftName={}, status={}, late={}min, early={}min, overtime={}min",
            result.getUserId(), shiftRule.getShiftName(), result.getStatus(),
            result.getLateDuration(), result.getEarlyDuration(), result.getOvertimeDuration());

        return result;
    }

    /**
     * 计算轮班制上班打卡
     * <p>
     * 核心逻辑：
     * 1. 获取班次上班时间（可能是早班06:00、中班14:00、晚班22:00等）
     * 2. 迟到宽限时间（lateTolerance）
     * 3. 打卡时间 > (班次上班时间 + 宽限期) = 迟到
     * 4. 计算迟到时长
     * </p>
     */
    private void calculateShiftCheckIn(AttendanceRecordEntity record, WorkShiftEntity shiftRule, AttendanceResultVO result) {
        LocalTime punchTime = record.getPunchTime().toLocalTime();
        LocalTime workStartTime = shiftRule.getWorkStartTime();
        Integer tolerance = shiftRule.getLateTolerance() != null ? shiftRule.getLateTolerance() : 0;

        // 计算允许的最晚上班时间（含宽限期）
        LocalTime latestAllowedTime = workStartTime.plusMinutes(tolerance);

        // 判断是否迟到
        if (punchTime.isAfter(latestAllowedTime)) {
            // 迟到
            long lateMinutes = java.time.Duration.between(workStartTime, punchTime).toMinutes();
            result.setStatus("LATE");
            result.setLateDuration(lateMinutes);
            result.setRemark(String.format("迟到%d分钟（宽限%d分钟）", lateMinutes, tolerance));
            log.warn("[轮班制策略] 上班迟到: userId={}, shiftName={}, punchTime={}, workStartTime={}, lateMinutes={}",
                record.getUserId(), shiftRule.getShiftName(), punchTime, workStartTime, lateMinutes);
        } else {
            // 正常
            result.setStatus("NORMAL");
            result.setLateDuration(0L);
            result.setRemark("正常上班");
            log.debug("[轮班制策略] 上班正常: userId={}, shiftName={}, punchTime={}",
                record.getUserId(), shiftRule.getShiftName(), punchTime);
        }
    }

    /**
     * 计算轮班制下班打卡
     * <p>
     * 核心逻辑：
     * 1. 获取班次下班时间（早班14:00、中班22:00、晚班06:00次日等）
     * 2. 早退宽限时间（earlyTolerance）
     * 3. 打卡时间 < (班次下班时间 - 宽限期) = 早退
     * 4. 打卡时间 > 班次下班时间 = 加班
     * 5. 计算早退或加班时长
     * </p>
     */
    private void calculateShiftCheckOut(AttendanceRecordEntity record, WorkShiftEntity shiftRule, AttendanceResultVO result) {
        LocalTime punchTime = record.getPunchTime().toLocalTime();
        LocalTime workEndTime = shiftRule.getWorkEndTime();
        Integer tolerance = shiftRule.getEarlyTolerance() != null ? shiftRule.getEarlyTolerance() : 0;

        // 计算允许的最早下班时间（含宽限期）
        LocalTime earliestAllowedTime = workEndTime.minusMinutes(tolerance);

        // 判断早退或加班
        if (punchTime.isBefore(earliestAllowedTime)) {
            // 早退
            long earlyMinutes = java.time.Duration.between(punchTime, workEndTime).toMinutes();
            result.setStatus("EARLY_LEAVE");
            result.setEarlyDuration(earlyMinutes);
            result.setOvertimeDuration(0L);
            result.setRemark(String.format("早退%d分钟（宽限%d分钟）", earlyMinutes, tolerance));
            log.warn("[轮班制策略] 下班早退: userId={}, shiftName={}, punchTime={}, workEndTime={}, earlyMinutes={}",
                record.getUserId(), shiftRule.getShiftName(), punchTime, workEndTime, earlyMinutes);
        } else if (punchTime.isAfter(workEndTime)) {
            // 加班（轮班制加班可能需要跨天计算）
            long overtimeMinutes = java.time.Duration.between(workEndTime, punchTime).toMinutes();
            Integer minOvertime = shiftRule.getMinOvertimeDuration();

            // 判断是否满足最小加班时长
            if (minOvertime != null && overtimeMinutes < minOvertime) {
                // 不满足最小加班时长，视为正常下班
                result.setStatus("NORMAL");
                result.setEarlyDuration(0L);
                result.setOvertimeDuration(0L);
                result.setRemark(String.format("加班%d分钟未达到最小时长%d分钟", overtimeMinutes, minOvertime));
                log.info("[轮班制策略] 加班时长不足: userId={}, shiftName={}, overtime={}, minRequired={}",
                    record.getUserId(), shiftRule.getShiftName(), overtimeMinutes, minOvertime);
            } else {
                // 满足加班条件
                result.setStatus("OVERTIME");
                result.setOvertimeDuration(overtimeMinutes);
                result.setEarlyDuration(0L);
                result.setRemark(String.format("加班%d分钟", overtimeMinutes));
                log.info("[轮班制策略] 下班加班: userId={}, shiftName={}, punchTime={}, workEndTime={}, overtimeMinutes={}",
                    record.getUserId(), shiftRule.getShiftName(), punchTime, workEndTime, overtimeMinutes);
            }
        } else {
            // 正常下班（在宽限期内）
            result.setStatus("NORMAL");
            result.setEarlyDuration(0L);
            result.setOvertimeDuration(0L);
            result.setRemark("正常下班");
            log.debug("[轮班制策略] 下班正常: userId={}, shiftName={}, punchTime={}",
                record.getUserId(), shiftRule.getShiftName(), punchTime);
        }

        // 计算工作时长（从班次上班时间到下班时间，处理跨天班次）
        if (shiftRule.getWorkStartTime() != null && shiftRule.getWorkEndTime() != null) {
            long workMinutes;
            if (shiftRule.getWorkEndTime().isBefore(shiftRule.getWorkStartTime())) {
                // 跨天班次（如夜班22:00-06:00）
                long minutesToEndOfDay = java.time.Duration.between(shiftRule.getWorkStartTime(), LocalTime.MAX).toMinutes() + 1;
                long minutesFromStartOfDay = java.time.Duration.between(LocalTime.MIDNIGHT, shiftRule.getWorkEndTime()).toMinutes();
                workMinutes = minutesToEndOfDay + minutesFromStartOfDay;
            } else {
                // 正常班次
                workMinutes = java.time.Duration.between(shiftRule.getWorkStartTime(), shiftRule.getWorkEndTime()).toMinutes();
            }
            result.setWorkingMinutes(workMinutes);
        }
    }

    /**
     * 创建错误结果
     *
     * @param record 考勤记录
     * @param errorMessage 错误信息
     * @return 考勤结果
     */
    private AttendanceResultVO createErrorResult(AttendanceRecordEntity record, String errorMessage) {
        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(record.getUserId());
        // 安全处理：如果punchTime为null，使用当前日期
        result.setDate(record.getPunchTime() != null ? record.getPunchTime().toLocalDate() : java.time.LocalDate.now());
        result.setStatus("ERROR");
        result.setRemark(errorMessage);
        return result;
    }

    @Override
    public int getPriority() {
        return 80; // 轮班制优先级较低
    }

    @Override
    public String getStrategyType() {
        return "SHIFT_WORKING_HOURS";
    }
}
