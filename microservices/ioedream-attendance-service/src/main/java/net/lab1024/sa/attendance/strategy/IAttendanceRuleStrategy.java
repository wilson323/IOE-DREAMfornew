package net.lab1024.sa.attendance.strategy;

import net.lab1024.sa.common.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.attendance.domain.vo.AttendanceResultVO;

/**
 * 考勤规则策略接口
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 使用策略模式实现不同的考勤规则计算策略：
 * - 标准工时制策略
 * - 弹性工作制策略
 * - 轮班制策略
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface IAttendanceRuleStrategy {

    /**
     * 规则名称
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * </p>
     *
     * @return 规则名称
     */
    String getRuleName();

    /**
     * 计算考勤结果
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 根据打卡记录和考勤规则计算考勤结果
     * </p>
     *
     * @param record 打卡记录
     * @param rule 考勤规则
     * @return 考勤结果
     */
    AttendanceResultVO calculate(AttendanceRecordEntity record, Object rule);

    /**
     * 获取策略优先级
     * <p>
     * 用于策略工厂排序，优先级高的策略优先使用
     * </p>
     *
     * @return 优先级（数字越大优先级越高）
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 获取策略类型
     * <p>
     * 用于策略工厂识别策略类型
     * </p>
     *
     * @return 策略类型标识
     */
    String getStrategyType();
}
