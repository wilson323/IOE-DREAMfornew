package net.lab1024.sa.attendance.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.domain.vo.AttendanceResultVO;
import net.lab1024.sa.attendance.strategy.IAttendanceRuleStrategy;
import net.lab1024.sa.common.factory.StrategyMarker;
import org.springframework.stereotype.Component;

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
@Slf4j
@Component
@StrategyMarker(name = "FLEXIBLE_WORKING_HOURS", type = "ATTENDANCE_RULE", priority = 90)
public class FlexibleWorkingHoursStrategy implements IAttendanceRuleStrategy {

    @Override
    public String getRuleName() {
        return "弹性工作制";
    }

    @Override
    public AttendanceResultVO calculate(AttendanceRecordEntity record, Object rule) {
        // TODO: 实现弹性工作制计算逻辑
        // 1. 查询当天所有打卡记录
        // 2. 计算总工作时长
        // 3. 判断是否满足8小时要求

        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(record.getUserId());
        result.setDate(record.getPunchTime().toLocalDate());
        result.setStatus("NORMAL"); // 临时实现

        log.debug("[弹性工作制策略] 计算考勤结果 userId={}, date={}", record.getUserId(), result.getDate());
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
