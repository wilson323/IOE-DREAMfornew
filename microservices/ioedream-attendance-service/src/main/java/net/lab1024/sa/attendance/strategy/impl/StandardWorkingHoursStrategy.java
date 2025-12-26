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
 * 标准工时制考勤规则策略
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 验证用户是否在标准工作时间段内打卡
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Component
@StrategyMarker(name = "STANDARD_WORKING_HOURS", type = "ATTENDANCE_RULE", priority = 100)
@Slf4j
public class StandardWorkingHoursStrategy implements IAttendanceRuleStrategy {



    @Override
    public String getRuleName() {
        return "标准工时制";
    }

    @Override
    public AttendanceResultVO calculate(AttendanceRecordEntity record, Object rule) {
        log.info("[标准工时制策略] 开始计算考勤: userId={}, attendanceType={}, punchTime={}",
            record.getUserId(), record.getAttendanceType(), record.getPunchTime());

        // 1. 参数校验和类型转换
        if (!(rule instanceof WorkShiftEntity)) {
            log.error("[标准工时制策略] 规则类型错误: expected=WorkShiftEntity, actual={}",
                rule != null ? rule.getClass().getSimpleName() : "null");
            return createErrorResult(record, "规则类型错误");
        }

        WorkShiftEntity shiftRule = (WorkShiftEntity) rule;

        // 2. 初始化结果对象
        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(record.getUserId());
        result.setDate(record.getAttendanceDate() != null ? record.getAttendanceDate() : record.getPunchTime().toLocalDate());

        // 3. 根据打卡类型（上班/下班）分别计算
        String attendanceType = record.getAttendanceType();
        if ("CHECK_IN".equals(attendanceType)) {
            // 上班打卡计算
            calculateCheckIn(record, shiftRule, result);
        } else if ("CHECK_OUT".equals(attendanceType)) {
            // 下班打卡计算
            calculateCheckOut(record, shiftRule, result);
        } else {
            log.warn("[标准工时制策略] 未知打卡类型: attendanceType={}", attendanceType);
            result.setStatus("UNKNOWN");
            result.setRemark("未知打卡类型");
        }

        log.info("[标准工时制策略] 计算完成: userId={}, status={}, late={}min, early={}min, overtime={}min",
            result.getUserId(), result.getStatus(), result.getLateDuration(), result.getEarlyDuration(), result.getOvertimeDuration());

        return result;
    }

    /**
     * 计算上班打卡
     * <p>
     * 核心逻辑：
     * 1. 获取上班时间（workStartTime）
     * 2. 迟到宽限时间（lateTolerance）
     * 3. 打卡时间 > (上班时间 + 宽限期) = 迟到
     * 4. 计算迟到时长
     * </p>
     */
    private void calculateCheckIn(AttendanceRecordEntity record, WorkShiftEntity shiftRule, AttendanceResultVO result) {
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
            log.warn("[标准工时制策略] 上班迟到: userId={}, punchTime={}, workStartTime={}, lateMinutes={}",
                record.getUserId(), punchTime, workStartTime, lateMinutes);
        } else {
            // 正常
            result.setStatus("NORMAL");
            result.setLateDuration(0L);
            result.setRemark("正常上班");
            log.debug("[标准工时制策略] 上班正常: userId={}, punchTime={}", record.getUserId(), punchTime);
        }
    }

    /**
     * 计算下班打卡
     * <p>
     * 核心逻辑：
     * 1. 获取下班时间（workEndTime）
     * 2. 早退宽限时间（earlyTolerance）
     * 3. 打卡时间 < (下班时间 - 宽限期) = 早退
     * 4. 打卡时间 > 下班时间 = 加班
     * 5. 计算早退或加班时长
     * </p>
     */
    private void calculateCheckOut(AttendanceRecordEntity record, WorkShiftEntity shiftRule, AttendanceResultVO result) {
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
            log.warn("[标准工时制策略] 下班早退: userId={}, punchTime={}, workEndTime={}, earlyMinutes={}",
                record.getUserId(), punchTime, workEndTime, earlyMinutes);
        } else if (punchTime.isAfter(workEndTime)) {
            // 加班
            long overtimeMinutes = java.time.Duration.between(workEndTime, punchTime).toMinutes();
            Integer minOvertime = shiftRule.getMinOvertimeDuration();

            // 判断是否满足最小加班时长
            if (minOvertime != null && overtimeMinutes < minOvertime) {
                // 不满足最小加班时长，视为正常下班
                result.setStatus("NORMAL");
                result.setEarlyDuration(0L);
                result.setOvertimeDuration(0L);
                result.setRemark(String.format("加班%d分钟未达到最小时长%d分钟", overtimeMinutes, minOvertime));
                log.info("[标准工时制策略] 加班时长不足: userId={}, overtime={}, minRequired={}",
                    record.getUserId(), overtimeMinutes, minOvertime);
            } else {
                // 满足加班条件
                result.setStatus("OVERTIME");
                result.setOvertimeDuration(overtimeMinutes);
                result.setEarlyDuration(0L);
                result.setRemark(String.format("加班%d分钟", overtimeMinutes));
                log.info("[标准工时制策略] 下班加班: userId={}, punchTime={}, workEndTime={}, overtimeMinutes={}",
                    record.getUserId(), punchTime, workEndTime, overtimeMinutes);
            }
        } else {
            // 正常下班（在宽限期内）
            result.setStatus("NORMAL");
            result.setEarlyDuration(0L);
            result.setOvertimeDuration(0L);
            result.setRemark("正常下班");
            log.debug("[标准工时制策略] 下班正常: userId={}, punchTime={}", record.getUserId(), punchTime);
        }

        // 计算工作时长（从上班时间到当前下班时间）
        if (shiftRule.getWorkStartTime() != null && shiftRule.getWorkEndTime() != null) {
            long workMinutes = java.time.Duration.between(shiftRule.getWorkStartTime(), shiftRule.getWorkEndTime()).toMinutes();
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
        return 100; // 标准工时制优先级最高
    }

    @Override
    public String getStrategyType() {
        return "STANDARD_WORKING_HOURS";
    }
}
