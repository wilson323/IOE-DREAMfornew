package net.lab1024.sa.attendance.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.domain.vo.AttendanceResultVO;
import net.lab1024.sa.attendance.strategy.IAttendanceRuleStrategy;
import net.lab1024.sa.common.factory.StrategyMarker;
import org.springframework.stereotype.Component;

import java.time.Duration;
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
@Slf4j
@Component
@StrategyMarker(name = "STANDARD_WORKING_HOURS", type = "ATTENDANCE_RULE", priority = 100)
public class StandardWorkingHoursStrategy implements IAttendanceRuleStrategy {

    @Override
    public String getRuleName() {
        return "标准工时制";
    }

    @Override
    public AttendanceResultVO calculate(AttendanceRecordEntity record, Object rule) {
        // TODO: 实现标准工时制计算逻辑
        // 1. 解析考勤规则（workStartTime、workEndTime、lateGracePeriod等）
        // 2. 根据打卡时间和类型（上班/下班）计算考勤状态
        // 3. 计算迟到、早退、加班时长

        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(record.getUserId());
        result.setDate(record.getPunchTime().toLocalDate());
        result.setStatus("NORMAL"); // 临时实现

        log.debug("[标准工时制策略] 计算考勤结果 userId={}, date={}", record.getUserId(), result.getDate());
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
