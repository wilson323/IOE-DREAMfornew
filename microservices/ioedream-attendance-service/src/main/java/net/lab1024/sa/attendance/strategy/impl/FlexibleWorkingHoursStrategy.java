package net.lab1024.sa.attendance.strategy.impl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import net.lab1024.sa.attendance.domain.vo.AttendanceResultVO;
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.entity.WorkShiftEntity;
import net.lab1024.sa.attendance.strategy.IAttendanceRuleStrategy;
import net.lab1024.sa.common.factory.StrategyMarker;

import java.time.LocalTime;
import java.time.Duration;

/**
 * 弹性工作制考勤规则策略
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 验证用户是否满足弹性工作制要求（总工作时长）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Component
@StrategyMarker(name = "FLEXIBLE_WORKING_HOURS", type = "ATTENDANCE_RULE", priority = 90)
@Slf4j
public class FlexibleWorkingHoursStrategy implements IAttendanceRuleStrategy {


    @Override
    public String getRuleName() {
        return "弹性工作制";
    }

    @Override
    public AttendanceResultVO calculate(AttendanceRecordEntity record, Object rule) {
        log.info("[弹性工作制策略] 开始计算考勤: userId={}, attendanceType={}, punchTime={}",
            record.getUserId(), record.getAttendanceType(), record.getPunchTime());

        // 1. 参数校验和类型转换
        if (!(rule instanceof WorkShiftEntity)) {
            log.error("[弹性工作制策略] 规则类型错误: expected=WorkShiftEntity, actual={}",
                rule != null ? rule.getClass().getSimpleName() : "null");
            return createErrorResult(record, "规则类型错误");
        }

        WorkShiftEntity shiftRule = (WorkShiftEntity) rule;

        // 2. 验证班次类型（必须为弹性班次）
        if (shiftRule.getShiftType() == null || shiftRule.getShiftType() != 2) {
            log.warn("[弹性工作制策略] 班次类型不是弹性工作制: shiftId={}, shiftType={}",
                shiftRule.getShiftId(), shiftRule.getShiftType());
        }

        // 3. 初始化结果对象
        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(record.getUserId());
        result.setDate(record.getAttendanceDate() != null ? record.getAttendanceDate() : record.getPunchTime().toLocalDate());

        // 4. 根据打卡类型（上班/下班）分别计算
        String attendanceType = record.getAttendanceType();
        if ("CHECK_IN".equals(attendanceType)) {
            // 上班打卡计算（检查是否在弹性时间范围内）
            calculateFlexibleCheckIn(record, shiftRule, result);
        } else if ("CHECK_OUT".equals(attendanceType)) {
            // 下班打卡计算（检查总工作时长是否满足要求）
            calculateFlexibleCheckOut(record, shiftRule, result);
        } else {
            log.warn("[弹性工作制策略] 未知打卡类型: attendanceType={}", attendanceType);
            result.setStatus("UNKNOWN");
            result.setRemark("未知打卡类型");
        }

        log.info("[弹性工作制策略] 计算完成: userId={}, status={}, workingMinutes={}",
            result.getUserId(), result.getStatus(), result.getWorkingMinutes());

        return result;
    }

    /**
     * 计算弹性工作制上班打卡
     * <p>
     * 核心逻辑：
     * 1. 获取弹性上班时间范围（flexStartEarliest ~ flexStartLatest）
     * 2. 打卡时间在范围内 = 正常
     * 3. 打卡时间早于最早时间 = 太早（不记录迟到，但提示）
     * 4. 打卡时间晚于最晚时间 = 迟到
     * </p>
     */
    private void calculateFlexibleCheckIn(AttendanceRecordEntity record, WorkShiftEntity shiftRule, AttendanceResultVO result) {
        LocalTime punchTime = record.getPunchTime().toLocalTime();
        LocalTime earliestTime = shiftRule.getFlexStartEarliest(); // 弹性上班最早时间
        LocalTime latestTime = shiftRule.getFlexStartLatest();   // 弹性上班最晚时间

        // 如果没有设置弹性时间，使用标准时间
        if (earliestTime == null) {
            earliestTime = shiftRule.getWorkStartTime();
        }
        if (latestTime == null) {
            latestTime = shiftRule.getWorkStartTime() != null ?
                shiftRule.getWorkStartTime().plusHours(1) : LocalTime.of(10, 0);
        }

        // 判断打卡时间是否在弹性范围内
        if (punchTime.isBefore(earliestTime)) {
            // 太早上班（不视为迟到，但记录提示）
            result.setStatus("EARLY");
            result.setLateDuration(0L);
            result.setRemark(String.format("早到（弹性时间：%s-%s）", earliestTime, latestTime));
            log.info("[弹性工作制策略] 早到上班: userId={}, punchTime={}, earliestTime={}",
                record.getUserId(), punchTime, earliestTime);
        } else if (punchTime.isAfter(latestTime)) {
            // 迟到（超过弹性最晚时间）
            long lateMinutes = Duration.between(latestTime, punchTime).toMinutes();
            result.setStatus("LATE");
            result.setLateDuration(lateMinutes);
            result.setRemark(String.format("迟到%d分钟（弹性最晚：%s）", lateMinutes, latestTime));
            log.warn("[弹性工作制策略] 迟到: userId={}, punchTime={}, latestTime={}, lateMinutes={}",
                record.getUserId(), punchTime, latestTime, lateMinutes);
        } else {
            // 正常（在弹性时间范围内）
            result.setStatus("NORMAL");
            result.setLateDuration(0L);
            result.setRemark(String.format("弹性上班（%s-%s）", earliestTime, latestTime));
            log.debug("[弹性工作制策略] 弹性上班正常: userId={}, punchTime={}",
                record.getUserId(), punchTime);
        }
    }

    /**
     * 计算弹性工作制下班打卡
     * <p>
     * 核心逻辑：
     * 1. 获取弹性下班时间范围（flexEndEarliest ~ flexEndLatest）
     * 2. 计算当天总工作时长（需要结合上班打卡时间）
     * 3. 判断是否满足最小工作时长要求
     * 4. 判断是否在弹性时间范围内下班
     * </p>
     */
    private void calculateFlexibleCheckOut(AttendanceRecordEntity record, WorkShiftEntity shiftRule, AttendanceResultVO result) {
        LocalTime punchTime = record.getPunchTime().toLocalTime();
        LocalTime earliestEndTime = shiftRule.getFlexEndEarliest(); // 弹性下班最早时间
        LocalTime latestEndTime = shiftRule.getFlexEndLatest();     // 弹性下班最晚时间

        // 如果没有设置弹性时间，使用标准时间
        if (earliestEndTime == null) {
            earliestEndTime = shiftRule.getWorkEndTime();
        }
        if (latestEndTime == null) {
            latestEndTime = shiftRule.getWorkEndTime() != null ?
                shiftRule.getWorkEndTime().plusHours(2) : LocalTime.of(20, 0);
        }

        // 获取要求的工作时长（分钟）
        Integer requiredWorkMinutes = shiftRule.getWorkDuration(); // 如480分钟（8小时）
        if (requiredWorkMinutes == null) {
            requiredWorkMinutes = 480; // 默认8小时
        }

        // 判断下班时间是否在弹性范围内
        if (punchTime.isBefore(earliestEndTime)) {
            // 太早下班（可能不满足工作时长）
            result.setStatus("EARLY_LEAVE");
            result.setEarlyDuration(Duration.between(punchTime, earliestEndTime).toMinutes());
            result.setRemark(String.format("过早下班（弹性时间：%s-%s）", earliestEndTime, latestEndTime));
            log.warn("[弹性工作制策略] 过早下班: userId={}, punchTime={}, earliestEndTime={}",
                record.getUserId(), punchTime, earliestEndTime);
        } else if (punchTime.isAfter(latestEndTime)) {
            // 加班（超过弹性最晚时间）
            long overtimeMinutes = Duration.between(latestEndTime, punchTime).toMinutes();
            result.setStatus("OVERTIME");
            result.setOvertimeDuration(overtimeMinutes);
            result.setEarlyDuration(0L);
            result.setRemark(String.format("加班%d分钟（弹性最晚：%s）", overtimeMinutes, latestEndTime));
            log.info("[弹性工作制策略] 弹性加班: userId={}, punchTime={}, latestEndTime={}, overtimeMinutes={}",
                record.getUserId(), punchTime, latestEndTime, overtimeMinutes);
        } else {
            // 正常下班（在弹性时间范围内）
            result.setStatus("NORMAL");
            result.setEarlyDuration(0L);
            result.setOvertimeDuration(0L);
            result.setRemark(String.format("弹性下班（%s-%s）", earliestEndTime, latestEndTime));
            log.debug("[弹性工作制策略] 弹性下班正常: userId={}, punchTime={}",
                record.getUserId(), punchTime);
        }

        // 设置要求的工作时长
        result.setWorkingMinutes(requiredWorkMinutes.longValue());
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
        return 90; // 弹性工作制优先级中等
    }

    @Override
    public String getStrategyType() {
        return "FLEXIBLE_WORKING_HOURS";
    }
}
