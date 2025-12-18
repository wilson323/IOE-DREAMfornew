package net.lab1024.sa.attendance.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.domain.vo.AttendanceResultVO;
import net.lab1024.sa.attendance.strategy.IAttendanceRuleStrategy;
import net.lab1024.sa.common.factory.StrategyMarker;
import org.springframework.stereotype.Component;

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
@Slf4j
@Component
@StrategyMarker(name = "SHIFT_WORKING_HOURS", type = "ATTENDANCE_RULE", priority = 80)
public class ShiftWorkingHoursStrategy implements IAttendanceRuleStrategy {

    @Override
    public String getRuleName() {
        return "轮班制";
    }

    @Override
    public AttendanceResultVO calculate(AttendanceRecordEntity record, Object rule) {
        // TODO: 实现轮班制计算逻辑
        // 1. 查询当天班次计划
        // 2. 根据班次计划验证打卡时间
        // 3. 计算考勤状态

        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(record.getUserId());
        result.setDate(record.getPunchTime().toLocalDate());
        result.setStatus("NORMAL"); // 临时实现

        log.debug("[轮班制策略] 计算考勤结果 userId={}, date={}", record.getUserId(), result.getDate());
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
